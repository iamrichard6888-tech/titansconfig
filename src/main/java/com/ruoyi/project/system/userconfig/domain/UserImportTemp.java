package com.ruoyi.project.system.userconfig.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 用户导入中间暂存表对象 T_S_USER_IMPORT_TEMP
 */
public class UserImportTemp extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String id;
    private String organQzh;
    private String organId;
    private String loginAccount;
    private String userName;
    private String mobilePhone;
    private String deptName;
    private String userType;
    private String description;
    private String source;
    private String status;
    private String errorMsg;
    private String personType;
    private String postName;
    private String roleCode;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganQzh() {
        return organQzh;
    }

    public void setOrganQzh(String organQzh) {
        this.organQzh = organQzh;
    }

    public String getOrganId() {
        return organId;
    }

    public void setOrganId(String organId) {
        this.organId = organId;
    }

    public String getLoginAccount() {
        return loginAccount;
    }

    public void setLoginAccount(String loginAccount) {
        this.loginAccount = loginAccount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    @Override
    public String toString() {
        return "UserImportTemp{" +
                "id='" + id + '\'' +
                ", organQzh='" + organQzh + '\'' +
                ", organId='" + organId + '\'' +
                ", loginAccount='" + loginAccount + '\'' +
                ", userName='" + userName + '\'' +
                ", mobilePhone='" + mobilePhone + '\'' +
                ", deptName='" + deptName + '\'' +
                ", userType='" + userType + '\'' +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                ", status='" + status + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", personType='" + personType + '\'' +
                ", postName='" + postName + '\'' +
                ", roleCode='" + roleCode + '\'' +
                '}';
    }
}