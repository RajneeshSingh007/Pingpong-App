package com.pingpong.network.model;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CityData {

    @SerializedName("city")
    @Expose
    private List<String> city = new ArrayList<>();

    public List<String> getCity() {
        return city;
    }

    public void setCity(List<String> city) {
        this.city = city;
    }
}