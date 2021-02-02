package com.pingpong.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OTPResult {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("otp")
    @Expose
    private Integer otp;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getOtp() {
        return otp;
    }

    public void setOtp(Integer otp) {
        this.otp = otp;
    }

}