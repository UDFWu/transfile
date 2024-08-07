package com.scsb.cicdform.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.scsb.cicdform.bean.ActionInfo;
import com.scsb.cicdform.bean.po.FileMetadataPo;
import com.scsb.cicdform.constant.DataStatusConstant;
import com.scsb.cicdform.dao.FileMetadataDao;
import com.scsb.cicdform.service.AttachFileService;
import com.scsb.cicdform.util.DateUtil;

/**
 * 附加檔案服務實作類別
 * @author Scsb6699
 * @date 2024/02/27
 * @remark Copyright 2024 © 上海商業儲蓄銀行 版權所有
 */
@Service
@ConfigurationProperties(prefix = "upload")
public class AttachFileServiceImpl implements AttachFileService {

    /** 日誌 */
    private static final Logger logger = LogManager.getLogger(AttachFileServiceImpl.class);

    /** 檔案上傳路徑 */
    private String path;

    /** 副檔名符號(.) */
    public static final String DOT = ".";

    /** 副檔名符號(.) */
    public static final String SLASH = "//";

    /** 檔案資料存取物件。 */
    @Autowired
    FileMetadataDao fileMetadataDao;

    @Override
    public String uploadFile(ActionInfo actionInfo, MultipartFile file) throws IOException {
        String dateString = DateUtil.getCurrentDate("yyyyMMdd");

        // 建立日期對應的目錄
        Path directory = Paths.get(path, dateString);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = this.getFileExtension(originalFilename);
        // 生成UUID作為文件的唯一識別
        String uuid = UUID.randomUUID().toString() + DOT + fileExtension;

        // 檢查是否有檔案上傳
        if (!file.isEmpty()) {
            Files.copy(file.getInputStream(), Paths.get(directory.toString(), uuid), StandardCopyOption.REPLACE_EXISTING);
        }

        logger.info("檔案上傳成功 uuid: " + uuid);
        return uuid;
    }

    /**
     * 獲取檔案副檔名
     * @param fileName 檔案名稱
     * @return 檔案的副檔名，如果檔案名稱為空或無副檔名，則返回 null
     * @remark
     */
    private String getFileExtension(String fileName) {
        if (StringUtils.isNotEmpty(fileName)) {
            // 使用正則表達式來解析附檔名
            Pattern pattern = Pattern.compile("\\.(\\w+)$");
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(ActionInfo actionInfo, String uuid) throws IOException {
        // 讀取檔案
        Path filePath = Paths.get(this.getFilePath(uuid));
        byte[] fileContent = Files.readAllBytes(filePath);

        // Set up HTTP headers for download
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", uuid);

        // Return the byte array as a ResponseEntity
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }

    /**
     * 獲取檔案完整路徑
     * @param uuid 檔案的 UUID
     * @return 包含檔案完整路徑的字串
     * @remark
     */
    private String getFilePath(String uuid) {
        FileMetadataPo po = fileMetadataDao.findByFileUuid(uuid);
        String uploadDate = null;

        if (po != null) {
            uploadDate = po.getUploadDate().replace("-", "");
        }

        if (StringUtils.isEmpty(uploadDate)) {
            uploadDate = DateUtil.getCurrentDate("yyyyMMdd");
        }

        StringBuilder result = new StringBuilder();
        result.append(path)
                .append(SLASH)
                .append(uploadDate)
                .append(SLASH)
                .append(uuid);
        return result.toString();
    }

    @Override
    public void deleteFile(ActionInfo actionInfo, String uuid) throws IOException {
        FileMetadataPo po = fileMetadataDao.findByFileUuid(uuid);

        if (po == null) {
            this.deleteFileByName(uuid);
        } else {
            po.setStatus(DataStatusConstant.DELETED);
            po.setUpdatedUserId(actionInfo.getUserId());
            fileMetadataDao.save(po);
        }
    }

    /**
     * 儲存檔案元資料
     * @param actionInfo 案件資訊
     * @param files 要保存的檔案元資料清單
     * @throws IOException
     * @remark
     */
    private void deleteFileByName(String uuid) throws IOException {
        // 根據您的應用程式設置的檔案存儲路徑，組合出要刪除的檔案的完整路徑
        Path filePath = Paths.get(this.getFilePath(uuid));
        logger.info("無法刪除檔案 uuid: " + uuid);
        Files.delete(filePath);
        logger.info("檔案已成功刪除 uuid: " + uuid);

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
