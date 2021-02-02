package com.pingpong.network.apis;

import com.pingpong.models.GetCommentsModel;
import com.pingpong.network.model.CityData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface CityApi {

    @GET("city.php")
    Call<CityData> getCityList(@Query("state") String state);

}
