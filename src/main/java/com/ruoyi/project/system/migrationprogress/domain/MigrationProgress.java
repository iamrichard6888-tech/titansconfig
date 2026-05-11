package com.ruoyi.project.system.migrationprogress.domain;

import com.ruoyi.framework.web.domain.BaseEntity;

public class MigrationProgress extends BaseEntity {
    private String unitid;
    private String qzh;
    private String unitfullname;
    private Double currentData;
    private Double totalData;
    private Double progress;

    @Override
    public String toString() {
        return "MigrationProgress{" +
                "unitid='" + unitid + '\'' +
                ", qzh='" + qzh + '\'' +
                ", unitfullname='" + unitfullname + '\'' +
                ", currentData=" + currentData +
                ", totalData=" + totalData +
                ", progress='" + progress + '\'' +
                '}';
    }

    public String getUnitid() {
        return unitid;
    }

    public void setUnitid(String unitid) {
        this.unitid = unitid;
    }

    public String getQzh() {
        return qzh;
    }

    public void setQzh(String qzh) {
        this.qzh = qzh;
    }

    public String getUnitfullname() {
        return unitfullname;
    }

    public void setUnitfullname(String unitfullname) {
        this.unitfullname = unitfullname;
    }

    public Double getCurrentData() {
        return currentData;
    }

    public void setCurrentData(Double currentData) {
        this.currentData = currentData;
    }

    public Double getTotalData() {
        return totalData;
    }

    public void setTotalData(Double totalData) {
        this.totalData = totalData;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }
}
