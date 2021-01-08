package com.pingpong.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ActiveStatus {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("package_title")
    @Expose
    private String packageTitle;
    @SerializedName("expire_date")
    @Expose
    private String expireDate;

    @SerializedName("trxid")
    @Expose
    private String trxid;

    @SerializedName("paidamt")
    @Expose
    private String paidamt;

    @SerializedName("planamt")
    @Expose
    private String planamt;

    @SerializedName("payment_method")
    @Expose
    private String payment_method;

    private long expireTime;

    public ActiveStatus() {
    }

    public String getStatus() {
        return status;
    }

    public String getPackageTitle() {
        return packageTitle;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPackageTitle(String packageTitle) {
        this.packageTitle = packageTitle;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getTrxid() {
        return trxid;
    }

    public void setTrxid(String trxid) {
        this.trxid = trxid;
    }

    public String getPaidamt() {
        return paidamt;
    }

    public void setPaidamt(String paidamt) {
        this.paidamt = paidamt;
    }

    public String getPlanamt() {
        return planamt;
    }

    public void setPlanamt(String planamt) {
        this.planamt = planamt;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }
}
