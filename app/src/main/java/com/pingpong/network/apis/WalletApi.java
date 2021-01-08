package com.pingpong.network.apis;

import com.pingpong.models.home_content.Video;
import com.pingpong.network.mlm.MlmLogin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WalletApi {

    @FormUrlEncoded
    @POST("CheckBalance")
    Call<MlmLogin> getBalance(@Header("Authorization") String basicauth, @Field("Username") String Username, @Field("Password") String Password, @Field("PartnerId") String PartnerId);

    @FormUrlEncoded
    @POST("PurchaseMembership")
    Call<MlmLogin> purchaseMemberShip(@Header("Authorization") String basicauth, @Field("Username") String Username, @Field("Password") String Password,@Field("PartnerId") String PartnerId, @Field("WalletType") String WalletType, @Field("ProductName") String ProductName);

}
