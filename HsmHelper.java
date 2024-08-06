package com.scsb.cicdform.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.scsb.cicdform.bean.DecryptResponseData;
import com.scsb.cicdform.bean.GetTokenResponseData;
import com.scsb.cicdform.bean.dto.HsmResponseDto;
import com.scsb.cicdform.constant.HsmTypeConstant;
import com.scsb.cicdform.enums.ErrorMessageEnum;
import com.scsb.cicdform.exception.EfsException;
import com.scsb.cicdform.util.ErrorMessageUtil;

/**
 * Hsm 輔助類別
 * @author Scsb6598
 * @date 2024/02/17
 * @remark Copyright 2024 © 上海商業儲蓄銀行 版權所有
 */
@Component
@ConfigurationProperties(prefix = "hsm")
public class HsmHelper {
    private static final Logger logger = LogManager.getLogger(HsmHelper.class);

    /** HSM登入網址 */
    String tokenUrl;

    /** HSM解密網址 */
    String decryptUrl;

    /** 程式名稱 */
    private static final String APPLICATION_NAME = "E_FORM";
    String accessToken = null;

    /** 過期時限 */
    int expiresIn = 0;

    /** Header參數 */
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_HEADER_CONTENTTYPE = "Content-Type";
    private static final String HTTP_HEADER_AUTH = "Authorization";
    private static final String HTTP_CONTENTTYPE_JSON_UTF8 = "application/json;charset=utf8";
    private static final int HTTP_CONNECT_TIMEOUT = 10000;
    private static final int HTTP_READ_TIMEOUT = 15000;

    /** pem檔路徑 */
    private static final String API_KEY_FILE_PATH = "apiKey.pem";

    /** 叢集 */
    private static final String CLUSTER = "CHANGING";
    /** 叢集類型 */
    private static final int CLUSTER_TYPE = 1;

    /**
     * 依據yaml的網址設定登入網址
     * @param tokenUrl 登入網址
     */
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    /**
     * 依據yaml的網址設定解密網址
     * @param decryptUrl 解密網址
     */
    public void setDecryptUrl(String decryptUrl) {
        this.decryptUrl = decryptUrl;
    }

    /**
     * 呼叫HSM的API取得TOKEN
     */
    public void login() {
        StringBuilder content = httpConnect(HsmTypeConstant.LOGIN, "");

        // 處理http response
        Type type = new TypeToken<HsmResponseDto<GetTokenResponseData>>() {
        }.getType();
        HsmResponseDto<GetTokenResponseData> response = new Gson().fromJson(content.toString(), type);
        accessToken = response.getData().getAccessToken();
        expiresIn = response.getData().getExpiresIn();
    }

    /**
     * 呼叫HSM的API解密CIPHER
     * @param cipher 需解密的密文
     * @return 解密後的明文
     */
    public String decrypt(String cipher) {
        StringBuilder content = httpConnect(HsmTypeConstant.DECRYPT, cipher);

        // 處理http response
        Type type = new TypeToken<HsmResponseDto<DecryptResponseData>>() {
        }.getType();
        HsmResponseDto<DecryptResponseData> response = new Gson().fromJson(content.toString(), type);
        return response.getData().getPlain();
    }

    /**
     * 產生簽章需要的密鑰
     * @return 從pem檔產生的密鑰
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private PrivateKey createPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 使用 ClassLoader 獲取文件的 InputStream
        InputStream inputStream = HsmHelper.class.getClassLoader().getResourceAsStream(API_KEY_FILE_PATH);
        String key = "";

        // 從pem檔獲取金鑰
        if (inputStream != null) {
            // 使用 InputStreamReader 和 BufferedReader 讀取資源內容
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder keyBuilder = new StringBuilder();
                String strTemp = null;
                while ((strTemp = bf.readLine()) != null) {
                    if (!strTemp.startsWith("-----")) {
                        keyBuilder.append(strTemp);
                    }
                }
                key = keyBuilder.toString();
            } catch (Exception e) {
                logger.error("從pem檔獲取金鑰失敗", e);
                throw new EfsException(ErrorMessageUtil.createErrorMessages(ErrorMessageEnum.E0015));
            }
        } else {
            logger.info("File not found: " + API_KEY_FILE_PATH);
        }

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * 產生簽章值
     * @param data 簽章資料
     * @param priKey 密鑰
     * @return 簽章值
     * @throws NoSuchAlgorithmException 
     * @throws SignatureException 
     * @throws InvalidKeyException 
     */
    public byte[] sign(String data, PrivateKey priKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(priKey);
        rsa.update(data.getBytes(StandardCharsets.UTF_8));
        return rsa.sign();
    }

    /**
     * 建立http連線
     * @param type 連線類型
     * @param cipher 密文
     * @return http連線response
     */
    public StringBuilder httpConnect(String type, String cipher) {
        HttpURLConnection con = null;

        JsonObject bodyData = createBodyData(type, cipher);

        try {
            // 初始化 http connection
            URL url = new URL(type.equals(HsmTypeConstant.LOGIN) ? tokenUrl : decryptUrl);
            con = (HttpURLConnection) url.openConnection();
            // 設定 http method "POST"
            con.setRequestMethod(HTTP_METHOD_POST);
            // 設定 http header, ContentType為json, 編碼為utf-8
            con.setRequestProperty(HTTP_HEADER_CONTENTTYPE, HTTP_CONTENTTYPE_JSON_UTF8);
            // 設定連線逾時，送到Server的時間
            con.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            // 設定回應逾時，送到Server，但Server沒回, AP不想等, 想中斷的時間
            con.setReadTimeout(HTTP_READ_TIMEOUT);
            // 如果是要打AsymmetryDecrypt API要多設定token
            if (type.equals(HsmTypeConstant.DECRYPT)) {
                // 設定取得的token
                con.setRequestProperty(HTTP_HEADER_AUTH, "Bearer " + accessToken);
            }
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            os.write(bodyData.toString().getBytes(StandardCharsets.UTF_8));

            // 確認回傳HTTP status為200
            int status = con.getResponseCode();
            if (status == 200) {
                // 200 的話是讀取input stream
                return readResponseContent(con.getInputStream());
            } else {
                // 非200 讀取error stream
                return readResponseContent(con.getErrorStream());
            }

        } catch (Exception e) {
            logger.error("與Hsm API連線失敗", e);
            throw new EfsException(ErrorMessageUtil.createErrorMessages(ErrorMessageEnum.E0015));
        } finally {
            // Close HttpURLConnection
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * 設定http request body資料
     * @param type 連線類型
     * @param cipher 密文
     * @return body資料
     */
    private JsonObject createBodyData(String type, String cipher) {

        JsonObject returnBody = new JsonObject();

        try {
            if (type.equals(HsmTypeConstant.LOGIN)) {
                // 產生簽章值
                JsonObject tobeSignData = new JsonObject();
                tobeSignData.addProperty("applicationName", APPLICATION_NAME);
                tobeSignData.addProperty("signTime", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                byte[] signature = sign(tobeSignData.toString(), createPrivateKey());

                returnBody.addProperty("signature", Base64.getEncoder().encodeToString(signature));
                returnBody.add("data", tobeSignData);

            } else if (type.equals(HsmTypeConstant.DECRYPT)) {
                returnBody.addProperty("cluster", CLUSTER);
                returnBody.addProperty("keyName", APPLICATION_NAME);
                returnBody.addProperty("cipher", cipher);
                returnBody.addProperty("rtnEncoding", StandardCharsets.UTF_8.toString());
                returnBody.addProperty("cipherType", CLUSTER_TYPE);
            }
        } catch (Exception e) {
            logger.error("設定Hsm request body失敗", e);
            throw new EfsException(ErrorMessageUtil.createErrorMessages(ErrorMessageEnum.E0015));
        }

        return returnBody;
    }

    /**
     * 讀取http response內容
     * @param inputStream http response
     * @return 轉成StringBuilder後的內容
     * @throws IOException
     */
    private StringBuilder readResponseContent(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();

        try (InputStreamReader streamReader = new InputStreamReader(inputStream);
                BufferedReader in = new BufferedReader((streamReader))) {
            String line;
            // 一次讀取一行並加入到content後方
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
        }
        return content;
    }
}