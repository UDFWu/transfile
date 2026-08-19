package com.scsb.ncbs.dep.batch.bean.ndmdmt2;

import com.ibm.cbmp.fabric.foundation.fixedformat.annotation.Spec;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SEGMENT NAME: FSALN1 LINE 數位帳戶扣繳次數回饋檔（100 BYTES）
 * 註：FIXED DEC 為 PACKED DECIMAL，長度以 ceil((precision+1)/2) 計算
 */
@Data
public class Fsaln1LineDto {

    /**
     * 帳號(存款帳號-數位) 1-14
     */
    @Spec(length = 14)
    private String actNo;

    /**
     * 數位帳戶優惠專案 15-15（A:自然人+視訊 1:自然人+無視訊 2:本行卡 3:它行卡201803）
     */
    @Spec(length = 1)
    private String lineProj;

    /**
     * 前月代扣繳成功次數 16-19
     */
    @Spec(length = 4)
    private Integer agnCnt;

    /**
     * 客戶統一編號 20-30
     */
    @Spec(length = 11)
    private String inactNo;

    /**
     * 手續費回饋次數 31-32
     */
    @Spec(length = 2)
    private Integer freeChgCnt;

    /**
     * 悠遊自動加值回饋倍數 33-35
     */
    @Spec(length = 3, decimal = 1)
    private BigDecimal rebate;

    /**
     * 點數加碼倍數 36-38
     */
    @Spec(length = 3, decimal = 1)
    private BigDecimal pointTimes;

    /**
     * 點數 39-43
     */
    @Spec(length = 5)
    private Integer point;

    /**
     * 專案代號 44-47（P001:數位存款上線優惠）
     */
    @Spec(length = 4)
    private String projNo;

    /**
     * 維護日期 48-55
     */
    @Spec(length = 8)
    private String entDat;

    /**
     * DEBIT加值總金額（PACKED DECIMAL(12,2)） 56-62
     */
    @Spec(length = 7, decimal = 2)
    private BigDecimal txAmt;

    /**
     * 請款卡號(DEBIT)最新 63-78
     */
    @Spec(length = 16)
    private String stlDebitNo;

    /**
     * FILLER 79-96
     */
    @Spec(length = 18)
    private String filler;

    /**
     * 表示數位帳戶類型(JSA324M暫放數位BIT) 97-100
     */
    @Spec(length = 4)
    private String filler1;
}
