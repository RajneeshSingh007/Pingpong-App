package com.pingpong.network.mlm.postmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Register {

    @SerializedName("MobileNo")
    @Expose
    private String mobileNo;
    @SerializedName("Password")
    @Expose
    private String password;
    @SerializedName("FName")
    @Expose
    private String fName;
    @SerializedName("LName")
    @Expose
    private String lName;
    @SerializedName("Emailid")
    @Expose
    private String emailid;
    @SerializedName("RefeId")
    @Expose
    private String refeId;
    @SerializedName("State")
    @Expose
    private String state;
    @SerializedName("AreaId")
    @Expose
    private String areaId;
    @SerializedName("PartnerId")
    @Expose
    private String partnerId;
    @SerializedName("City")
    @Expose
    private String city;


    public Register(String mobileNo, String password, String fName, String lName, String emailid, String refeId, String state, String areaId, String partnerId, String city) {
        this.mobileNo = mobileNo;
        this.password = password;
        this.fName = fName;
        this.lName = lName;
        this.emailid = emailid;
        this.refeId = refeId;
        this.state = state;
        this.areaId = areaId;
        this.partnerId = partnerId;
        this.city = city;
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

    public String getFName() {
        return fName;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public String getLName() {
        return lName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public String getRefeId() {
        return refeId;
    }

    public void setRefeId(String refeId) {
        this.refeId = refeId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}