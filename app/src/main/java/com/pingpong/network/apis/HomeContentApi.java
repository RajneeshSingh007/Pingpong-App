package com.pingpong.network.apis;

import com.pingpong.models.home_content.HomeContent;
import com.pingpong.network.model.Notices;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface HomeContentApi {

    @GET("home_content_for_android")
    Call<HomeContent> getHomeContent(@Header("API-KEY") String apiKey);

    @GET("notice")
    Call<Notices> getNotices(@Header("API-KEY") String apiKey);


}
