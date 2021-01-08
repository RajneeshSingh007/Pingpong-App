package com.pingpong.bottomshit;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pingpong.Config;
import com.pingpong.R;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.UserDataApi;
import com.pingpong.network.apis.WalletApi;
import com.pingpong.network.mlm.Data;
import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.model.User;
import com.pingpong.network.model.config.PaymentConfig;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PaymentBottomShitDialog extends BottomSheetDialogFragment {

    public static final String PAYPAL = "paypal";
    public static final String STRIP = "strip";
    public static final String RAZOR_PAY = "razorpay";
    //private DatabaseHelper databaseHelper;

    private OnBottomShitClickListener bottomShitClickListener;
    private String password = "";
    private double walletBal= 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_payment_bottom_shit, container,
                false);
        //databaseHelper = new DatabaseHelper(getContext());
        //PaymentConfig config = databaseHelper.getConfigurationData().getPaymentConfig();
        CardView paypalBt, stripBt, razorpayBt,walletbtn;
        paypalBt = view.findViewById(R.id.paypal_btn);
        stripBt = view.findViewById(R.id.stripe_btn);
        razorpayBt = view.findViewById(R.id.razorpay_btn);
        Space space = view.findViewById(R.id.space2);
        walletbtn = view.findViewById(R.id.wallet_btn);
        TextView walletText = view.findViewById(R.id.walletText);
        RelativeLayout progressLayout = view.findViewById(R.id.progress_layout);
        password = PreferenceUtils.getPassword(getContext());
        ////Log.e("PaymentBottomShitDialog", "password: "+password);

//        if (!config.getPaypalEnable()) {
//            paypalBt.setVisibility(View.GONE);
//            space.setVisibility(View.GONE);
//        }
//
//        if (!config.getStripeEnable()) {
//            stripBt.setVisibility(View.GONE);
//            space.setVisibility(View.GONE);
//        }
//        if (!config.getRazorpayEnable())
//            razorpayBt.setVisibility(View.GONE);

        paypalBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomShitClickListener.onBottomShitClick(PAYPAL);
            }
        });

        stripBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomShitClickListener.onBottomShitClick(STRIP);
            }
        });

        razorpayBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomShitClickListener.onBottomShitClick(RAZOR_PAY);
            }
        });

        walletbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(walletBal > 0){
                    bottomShitClickListener.onBottomShitClick("wallet");
                }else{
                    new ToastMsg(getContext()).toastIconError("Wallet Balance is low");
                }
            }
        });

        progressLayout.setVisibility(View.VISIBLE);

        Retrofit retrofit = RetrofitClient.getRetrofitMLMInstance();
        WalletApi walletApi = retrofit.create(WalletApi.class);
        User user = PreferenceUtils.getUser(getContext());
        //Call<MlmLogin> balCall = walletApi.getBalance(Config.MLM_AUTH,"9923290044", "Xyz_123", Config.PARTER_ID);
        Retrofit phoneRetrofit = RetrofitClient.getRetrofitInstance();
        UserDataApi api = phoneRetrofit.create(UserDataApi.class);
        Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> clresponse) {
                if(clresponse.code() == 200){
                    if(clresponse.body() != null){
                        User user1 = clresponse.body();
                        Call<MlmLogin> balCall = walletApi.getBalance(Config.MLM_AUTH,user1.getPhone(),password, Config.PARTER_ID);
                        balCall.enqueue(new Callback<MlmLogin>() {
                            @Override
                            public void onResponse(Call<MlmLogin> call, Response<MlmLogin> response) {
                                progressLayout.setVisibility(View.GONE);
                                if (response.code() == 200){
                                    if(response.body() != null){
                                        MlmLogin mlmLogin = response.body();
                                        if(mlmLogin.getStatus().equalsIgnoreCase("Success")){
                                            Data data = mlmLogin.getData();
                                            walletbtn.setVisibility(View.VISIBLE);
                                            walletBal = Double.parseDouble(data.getBalance());
                                            walletText.setText("Wallet (Available Balance "+data.getBalance()+"₹)");
                                        }else{
                                            walletbtn.setVisibility(View.GONE);
                                        }
                                    }else{
                                        walletbtn.setVisibility(View.GONE);
                                    }
                                }else{
                                    walletbtn.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onFailure(Call<MlmLogin> call, Throwable t) {
                                walletbtn.setVisibility(View.GONE);
                                progressLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
        return view;

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            bottomShitClickListener = (OnBottomShitClickListener) context;
        } catch (Exception e) {
            throw new ClassCastException(context.toString() + " must be implemented");
        }

    }

    public interface OnBottomShitClickListener {
        void onBottomShitClick(String paymentMethodName);
    }

}

