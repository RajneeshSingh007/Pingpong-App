
package com.pingpong.network.mlm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("MobileNo")
    @Expose
    private String mobileNo;
    @SerializedName("FName")
    @Expose
    private String fName;
    @SerializedName("LName")
    @Expose
    private String lName;
    @SerializedName("CityName")
    @Expose
    private String cityName;
    @SerializedName("StateName")
    @Expose
    private String stateName;
    @SerializedName("EmailId")
    @Expose
    private String emailId;
    @SerializedName("Balance")
    @Expose
    private String balance;

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
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

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
