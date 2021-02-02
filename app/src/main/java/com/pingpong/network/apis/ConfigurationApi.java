package com.pingpong.network.apis;

import com.pingpong.network.model.config.Configuration;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ConfigurationApi {

    @GET("config")
    Call<Configuration> getConfigurationData(@Header("API-KEY") String apiKey);
}
