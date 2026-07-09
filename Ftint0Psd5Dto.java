package com.scsb.ncbs.dep.batch.bean.jtd220d;

import com.ibm.cbmp.fabric.foundation.fixedformat.annotation.Spec;
import lombok.Data;

import java.math.BigDecimal;

/**
 * FTINT0.PSD5 定存主檔－帳卡明細（182 BYTES）
 * 註：FIXED DEC 為 PACKED DECIMAL，長度以 (precision/2)+1 計算
 */
@Data
public class Ftint0Psd5Dto {

    /** 帳號 */
    @Spec(length = 14)
    private String tdAcn;

    /** 轉期次數 */
    @Spec(length = 2)
    private Integer txCnt;

    /** 交易變動次數 */
    @Spec(length = 4)
    private Integer txChgCnt;

    /** 交易日期(付息/存入/提領日期) */
    @Spec(length = 8)
    private String txDte;

    /** 交易序號 */
    @Spec(length = 6)
    private Long txSrlno;

    /** 利率方式 1:固定 2:機動 */
    @Spec(length = 1)
    private String intTyp;

    /** 利率依據 1:總行牌告 2:分行牌告 3:郵匯局牌告 */
    @Spec(length = 1)
    private String intBas;

    /** 存單利率 */
    @Spec(length = 4, decimal = 5)
    private BigDecimal intRate;

    /** 牌告利率加減碼 */
    @Spec(length = 4, decimal = 5)
    private BigDecimal pstRate;

    /** 稅率 */
    @Spec(length = 4, decimal = 5)
    private BigDecimal taxRate;

    /** 計息起日 */
    @Spec(length = 8)
    private String intSatDte;

    /** 計息止日 */
    @Spec(length = 8)
    private String intEndDte;

    /** 免扣證號 */
    @Spec(length = 9)
    private String untaxDocNo;

    /** 免扣證登記金額 */
    @Spec(length = 8, decimal = 2)
    private BigDecimal untaxAmt;

    /** 轉帳帳號 */
    @Spec(length = 14)
    private String trAcn;

    /** 借貸別 1:借方 2:貸方 */
    @Spec(length = 1)
    private String drcr;

    /** 本金金額 */
    @Spec(length = 8, decimal = 2)
    private BigDecimal prtAmt;

    /** 利息金額 */
    @Spec(length = 8, decimal = 2)
    private BigDecimal intAmt;

    /** 交易別 */
    @Spec(length = 2)
    private String txtyp;

    /** 摘要 */
    @Spec(length = 2)
    private String memo;

    /** 客戶備註 */
    @Spec(length = 16)
    private String custMemo;

    /** 所得稅 */
    @Spec(length = 8, decimal = 2)
    private BigDecimal taxAmt;

    /** 印花稅 */
    @Spec(length = 8, decimal = 2)
    private BigDecimal stmpAmt;

    /** 主管代號 */
    @Spec(length = 6)
    private String spvNo;

    /** 櫃員代號 */
    @Spec(length = 6)
    private String tlrNo;

    /** 中途解約折扣 */
    @Spec(length = 3)
    private Integer discnt;

    /** 零存整付存入日 */
    @Spec(length = 8)
    private String dpDte;

    /** 存期計息月數 */
    @Spec(length = 2)
    private Integer dprMon;

    /** 存期計息日數 */
    @Spec(length = 2)
    private Integer dprDay;

    /** RESERVE（前5 bytes 供組合式定存借用） */
    @Spec(length = 7)
    private String reserve;
}
