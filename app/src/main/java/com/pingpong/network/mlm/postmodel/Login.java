package com.pingpong.network.mlm.postmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login {

    @SerializedName("MobileNo")
    @Expose
    private String mobileNo;
    @SerializedName("Password")
    @Expose
    private String password;
    @SerializedName("PartnerId")
    @Expose
    private String partnerId;

    public Login(String mobileNo, String password, String partnerId) {
        this.mobileNo = mobileNo;
        this.password = password;
        this.partnerId = partnerId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

}