package com.ruoyi.project.system.titansuser.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

public class TitansUser extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String USERID;
    private String CODE;
    private String NAME;
    private String NICKNAME;
    private String DESCRIPTION;
    private String CREATETIME;
    private String CREATEUSER;
    private String MODIFYTIME;
    private String MODIFYUSER;
    private String PWD;
    private String EXPIRESTIME;
    private String STATUS;
    private String CARDID;
    private String ORDERNO;
    private String USERTYPE;
    private String PKIKEY;
    private String CHANGEDATE;
    private String PORTALSTYLE;
    private String ROLEDESC;
    private String ENABLESTATUS;
    private String STARTTIME;
    private String STOPTIME;
    private String WRONGNUM;
    private String LASTLOGINTIME;
    private String UPHMACVALUE;
    private String PHONE;
    private String HMACVALUE;
    private String ENCPWD;
    private String UKEYID;
    private String DEPARTMENT;

    public String getWRONGNUM() {
        return WRONGNUM;
    }

    public void setWRONGNUM(String WRONGNUM) {
        this.WRONGNUM = WRONGNUM;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getCODE() {
        return CODE;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getNICKNAME() {
        return NICKNAME;
    }

    public void setNICKNAME(String NICKNAME) {
        this.NICKNAME = NICKNAME;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public void setDESCRIPTION(String DESCRIPTION) {
        this.DESCRIPTION = DESCRIPTION;
    }

    public String getCREATETIME() {
        return CREATETIME;
    }

    public void setCREATETIME(String CREATETIME) {
        this.CREATETIME = CREATETIME;
    }

    public String getCREATEUSER() {
        return CREATEUSER;
    }

    public void setCREATEUSER(String CREATEUSER) {
        this.CREATEUSER = CREATEUSER;
    }

    public String getMODIFYTIME() {
        return MODIFYTIME;
    }

    public void setMODIFYTIME(String MODIFYTIME) {
        this.MODIFYTIME = MODIFYTIME;
    }

    public String getMODIFYUSER() {
        return MODIFYUSER;
    }

    public void setMODIFYUSER(String MODIFYUSER) {
        this.MODIFYUSER = MODIFYUSER;
    }

    public String getPWD() {
        return PWD;
    }

    public void setPWD(String PWD) {
        this.PWD = PWD;
    }

    public String getEXPIRESTIME() {
        return EXPIRESTIME;
    }

    public void setEXPIRESTIME(String EXPIRESTIME) {
        this.EXPIRESTIME = EXPIRESTIME;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getCARDID() {
        return CARDID;
    }

    public void setCARDID(String CARDID) {
        this.CARDID = CARDID;
    }

    public String getORDERNO() {
        return ORDERNO;
    }

    public void setORDERNO(String ORDERNO) {
        this.ORDERNO = ORDERNO;
    }

    public String getUSERTYPE() {
        return USERTYPE;
    }

    public void setUSERTYPE(String USERTYPE) {
        this.USERTYPE = USERTYPE;
    }

    public String getPKIKEY() {
        return PKIKEY;
    }

    public void setPKIKEY(String PKIKEY) {
        this.PKIKEY = PKIKEY;
    }

    public String getCHANGEDATE() {
        return CHANGEDATE;
    }

    public void setCHANGEDATE(String CHANGEDATE) {
        this.CHANGEDATE = CHANGEDATE;
    }

    public String getPORTALSTYLE() {
        return PORTALSTYLE;
    }

    public void setPORTALSTYLE(String PORTALSTYLE) {
        this.PORTALSTYLE = PORTALSTYLE;
    }

    public String getROLEDESC() {
        return ROLEDESC;
    }

    public void setROLEDESC(String ROLEDESC) {
        this.ROLEDESC = ROLEDESC;
    }

    public String getENABLESTATUS() {
        return ENABLESTATUS;
    }

    public void setENABLESTATUS(String ENABLESTATUS) {
        this.ENABLESTATUS = ENABLESTATUS;
    }

    public String getSTARTTIME() {
        return STARTTIME;
    }

    public void setSTARTTIME(String STARTTIME) {
        this.STARTTIME = STARTTIME;
    }

    public String getSTOPTIME() {
        return STOPTIME;
    }

    public void setSTOPTIME(String STOPTIME) {
        this.STOPTIME = STOPTIME;
    }

    public String getLASTLOGINTIME() {
        return LASTLOGINTIME;
    }

    public void setLASTLOGINTIME(String LASTLOGINTIME) {
        this.LASTLOGINTIME = LASTLOGINTIME;
    }

    public String getUPHMACVALUE() {
        return UPHMACVALUE;
    }

    public void setUPHMACVALUE(String UPHMACVALUE) {
        this.UPHMACVALUE = UPHMACVALUE;
    }

    public String getPHONE() {
        return PHONE;
    }

    public void setPHONE(String PHONE) {
        this.PHONE = PHONE;
    }

    public String getHMACVALUE() {
        return HMACVALUE;
    }

    public void setHMACVALUE(String HMACVALUE) {
        this.HMACVALUE = HMACVALUE;
    }

    public String getENCPWD() {
        return ENCPWD;
    }

    public void setENCPWD(String ENCPWD) {
        this.ENCPWD = ENCPWD;
    }

    public String getUKEYID() {
        return UKEYID;
    }

    public void setUKEYID(String UKEYID) {
        this.UKEYID = UKEYID;
    }

    public String getDEPARTMENT() {
        return DEPARTMENT;
    }

    public void setDEPARTMENT(String DEPARTMENT) {
        this.DEPARTMENT = DEPARTMENT;
    }

    @Override
    public String toString() {
        return "TitansUser{" +
                "USERID='" + USERID + '\'' +
                ", CODE='" + CODE + '\'' +
                ", NAME='" + NAME + '\'' +
                ", NICKNAME='" + NICKNAME + '\'' +
                ", DESCRIPTION='" + DESCRIPTION + '\'' +
                ", CREATETIME='" + CREATETIME + '\'' +
                ", CREATEUSER='" + CREATEUSER + '\'' +
                ", MODIFYTIME='" + MODIFYTIME + '\'' +
                ", MODIFYUSER='" + MODIFYUSER + '\'' +
                ", PWD='" + PWD + '\'' +
                ", EXPIRESTIME='" + EXPIRESTIME + '\'' +
                ", STATUS='" + STATUS + '\'' +
                ", CARDID='" + CARDID + '\'' +
                ", ORDERNO='" + ORDERNO + '\'' +
                ", USERTYPE='" + USERTYPE + '\'' +
                ", PKIKEY='" + PKIKEY + '\'' +
                ", CHANGEDATE='" + CHANGEDATE + '\'' +
                ", PORTALSTYLE='" + PORTALSTYLE + '\'' +
                ", ROLEDESC='" + ROLEDESC + '\'' +
                ", ENABLESTATUS='" + ENABLESTATUS + '\'' +
                ", STARTTIME='" + STARTTIME + '\'' +
                ", STOPTIME='" + STOPTIME + '\'' +
                ", WRONGNUM='" + WRONGNUM + '\'' +
                ", LASTLOGINTIME='" + LASTLOGINTIME + '\'' +
                ", UPHMACVALUE='" + UPHMACVALUE + '\'' +
                ", PHONE='" + PHONE + '\'' +
                ", HMACVALUE='" + HMACVALUE + '\'' +
                ", ENCPWD='" + ENCPWD + '\'' +
                ", UKEYID='" + UKEYID + '\'' +
                ", DEPARTMENT='" + DEPARTMENT + '\'' +
                '}';
    }
}
