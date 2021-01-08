package com.pingpong.network.mlm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AmountCredit {

    @SerializedName("Username")
    @Expose
    private String username = "";
    @SerializedName("Amount")
    @Expose
    private Integer amount = 0;
    @SerializedName("Securekey")
    @Expose
    private String securekey = "";

    public AmountCredit(String username, Integer amount, String securekey) {
        this.username = username;
        this.amount = amount;
        this.securekey = securekey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getSecurekey() {
        return securekey;
    }

    public void setSecurekey(String securekey) {
        this.securekey = securekey;
    }

}
