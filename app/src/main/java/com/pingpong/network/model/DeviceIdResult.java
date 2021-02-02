package com.pingpong.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceIdResult {

    @SerializedName("status")
    @Expose
    private Boolean status = true;
    @SerializedName("message")
    @Expose
    private String message = "";

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}