package com.pingpong.network.apis;

import com.pingpong.network.model.PasswordReset;
import com.pingpong.network.model.PayumoneyHash;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PayuHashApi {
    @FormUrlEncoded
    @POST("payumoney.php")
    Call<PayumoneyHash> getHash(@Field("key") String key, @Field("txnid") String txnid, @Field("amount") String amount, @Field("productInfo") String productInfo, @Field("firstName") String name, @Field("email") String email, @Field("udf1") String udf1, @Field("udf2") String udf2, @Field("udf3") String udf3, @Field("udf4") String udf4, @Field("udf5") String udf5);

}