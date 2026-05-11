package com.ruoyi.project.system.titansrole.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

public class TitansRole extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String ROLEID;
    private String PRODUCTID;
    private String CODE;
    private String NAME;
    private String DESCRIPTION;
    private String CREATETIME;
    private String CREATEUSER;
    private String MODIFYTIME;
    private String MODIFYUSER;
    private String ISADMIN;
    private String STATUS;
    private String ISHIDDEN;
    private String UNITID;
    private String HMACVALUE;

    public String getROLEID() {
        return ROLEID;
    }

    public void setROLEID(String ROLEID) {
        this.ROLEID = ROLEID;
    }

    public String getPRODUCTID() {
        return PRODUCTID;
    }

    public void setPRODUCTID(String PRODUCTID) {
        this.PRODUCTID = PRODUCTID;
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

    public String getISADMIN() {
        return ISADMIN;
    }

    public void setISADMIN(String ISADMIN) {
        this.ISADMIN = ISADMIN;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public String getISHIDDEN() {
        return ISHIDDEN;
    }

    public void setISHIDDEN(String ISHIDDEN) {
        this.ISHIDDEN = ISHIDDEN;
    }

    public String getUNITID() {
        return UNITID;
    }

    public void setUNITID(String UNITID) {
        this.UNITID = UNITID;
    }

    public String getHMACVALUE() {
        return HMACVALUE;
    }

    public void setHMACVALUE(String HMACVALUE) {
        this.HMACVALUE = HMACVALUE;
    }

    @Override
    public String toString() {
        return "Role{" +
                "ROLEID='" + ROLEID + '\'' +
                ", PRODUCTID='" + PRODUCTID + '\'' +
                ", CODE='" + CODE + '\'' +
                ", NAME='" + NAME + '\'' +
                ", DESCRIPTION='" + DESCRIPTION + '\'' +
                ", CREATETIME='" + CREATETIME + '\'' +
                ", CREATEUSER='" + CREATEUSER + '\'' +
                ", MODIFYTIME='" + MODIFYTIME + '\'' +
                ", MODIFYUSER='" + MODIFYUSER + '\'' +
                ", ISADMIN='" + ISADMIN + '\'' +
                ", STATUS='" + STATUS + '\'' +
                ", ISHIDDEN='" + ISHIDDEN + '\'' +
                ", UNITID='" + UNITID + '\'' +
                ", HMACVALUE='" + HMACVALUE + '\'' +
                '}';
    }
}
