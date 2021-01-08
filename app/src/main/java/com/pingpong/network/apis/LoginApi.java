package com.pingpong.network.apis;

import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.mlm.postmodel.Login;
import com.pingpong.network.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginApi {

    @FormUrlEncoded
    @POST("login")
    Call<User> postLoginStatus(@Header("API-KEY") String apiKey,
                               @Field("email") String email,
                               @Field("password") String password,
                               @Field("flag") boolean flag);

    @POST("LoginExternalUser")
    Call<MlmLogin> postMlMLoginStatus(@Header("Authorization") String basicauth, @Body Login login);

}
