package com.pingpong.network.apis;


import com.pingpong.network.mlm.AmountCredit;
import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.mlm.postmodel.Register;
import com.pingpong.network.model.DeviceIdResult;
import com.pingpong.network.model.User;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SignUpApi {
    @FormUrlEncoded
    @POST("signup")
    Call<User> signUp(@Header("API-KEY") String apiKey,
                      @Field("email") String email,
                      @Field("password") String password,
                      @Field("name") String name,
                      @Field("phone") String phone,
                      @Field("ref") int ref,
                      @Field("city") String city,
                      @Field("State") String state,
                      @Field("deviceid") String deviceid);

    @POST("AddExternalUser")
    Call<MlmLogin> signUpMlm(@Header("Authorization") String basicauth, @Body Register register);

    @POST("CreditUserWalletBalance")
    Call<MlmLogin> creditAmount(@Header("Authorization") String basicauth, @Body AmountCredit amountCredit);

    @FormUrlEncoded
    @POST("userdevicecheck")
    Call<DeviceIdResult> checkUserDevice(@Header("API-KEY") String apiKey, @Field("deviceid") String deviceid);

}
