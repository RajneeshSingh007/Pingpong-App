package com.pingpong;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.payumoney.core.PayUmoneyConfig;
import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;
import com.pingpong.R;
import com.pingpong.adapters.PackageAdapter;
import com.pingpong.bottomshit.PaymentBottomShitDialog;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.CityApi;
import com.pingpong.network.apis.PackageApi;
import com.pingpong.network.apis.PaymentApi;
import com.pingpong.network.apis.PayuHashApi;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.apis.UserDataApi;
import com.pingpong.network.apis.WalletApi;
import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.AllPackage;
import com.pingpong.network.model.CityData;
import com.pingpong.network.model.Package;
import com.pingpong.network.model.PayumoneyHash;
import com.pingpong.network.model.User;
import com.pingpong.network.model.config.PaymentConfig;
import com.pingpong.payumoney.AppEnvironment;
import com.pingpong.utils.MyAppClass;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;
import com.pingpong.utils.Tools;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PurchasePlanActivity extends AppCompatActivity implements PackageAdapter.OnItemClickListener, PaymentBottomShitDialog.OnBottomShitClickListener, PaymentResultListener {

    private static final String TAG = PurchasePlanActivity.class.getSimpleName();
    private static final int PAYPAL_REQUEST_CODE = 100;
    private TextView noTv;
    private ProgressBar progressBar;
    private ImageView closeIv;
    private RecyclerView packageRv;
    private List<Package> packages = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();
    private String currency = "";
    private String exchangeRate;
    private boolean isDark;
    private User user;
    private String type = "", id = "";

    private Package packageItem;
    private PaymentBottomShitDialog paymentBottomShitDialog;

    private PayUmoneySdkInitializer.PaymentParam mPaymentParams;

    private ProgressDialog dialog;
    private String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);
        ((MyAppClass) getApplication()).setAppEnvironment(AppEnvironment.PRODUCTION);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        user = PreferenceUtils.getUser(PurchasePlanActivity.this);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        setContentView(R.layout.activity_purchase_plan);

        initView();

        type = getIntent().getStringExtra("vType");
        id = getIntent().getStringExtra("id");

        packageRv.setHasFixedSize(true);
        packageRv.setLayoutManager(new LinearLayoutManager(this));

        getPurchasePlanInfo();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);
        password = PreferenceUtils.getPassword(this);
    }

    private void getPurchasePlanInfo() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        final PackageApi packageApi = retrofit.create(PackageApi.class);
        Call<AllPackage> call = packageApi.getAllPackage(Config.API_KEY);
        call.enqueue(new Callback<AllPackage>() {
            @Override
            public void onResponse(Call<AllPackage> call, Response<AllPackage> response) {
                progressBar.setVisibility(View.GONE);
                if(response.body() != null){
                    AllPackage allPackage = response.body();
                    packages = allPackage.getPackage();
                    if (allPackage.getPackage().size() > 0) {
                        noTv.setVisibility(View.GONE);
                        //Log.e(TAG, "onResponse: "+ packages);
                        PackageAdapter adapter = new PackageAdapter(PurchasePlanActivity.this, allPackage.getPackage(), currency);
                        adapter.setItemClickListener(PurchasePlanActivity.this);
                        packageRv.setAdapter(adapter);
                    } else {
                        noTv.setVisibility(View.VISIBLE);
                    }
                }else{
                    noTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<AllPackage> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                //t.printStackTrace();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager
                    .INTENT_EXTRA_TRANSACTION_RESPONSE);

            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            // Check which object is non-null
            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    String payuResponse = transactionResponse.getPayuResponse();
                    ////Log.e(TAG, "onActivityResult: "+payuResponse);
                    try {
                        JSONObject object = new JSONObject(payuResponse);
                        if(object == null){
                            new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("Failed to process transaction");
                            return;
                        }
                        JSONObject result = object.optJSONObject("result");
                        if(result == null){
                            new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("Failed to process transaction");
                            return;
                        }
                        ////Log.e(TAG, "onActivityResult: "+result);
//                        String status = result.optString("status");
//                        if(TextUtils.isEmpty(status) || !status.equalsIgnoreCase("success")){
//                            new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("Failed to process transaction");
//                            return;
//                        }
                        dialog.show();
                        String txnid = result.optString("txnid");
                        String mode = result.optString("mode");
                        String paymentid = result.optString("paymentId");
                        String merge = txnid;
                        if(!TextUtils.isEmpty(paymentid)){
                            merge +="&";
                            merge += paymentid;
                        }
                        String acharges = result.optString("additionalCharges");

                        double total = Double.parseDouble(result.getString("amount"));

                        if(!TextUtils.isEmpty(acharges)){
                            double price = Double.parseDouble(acharges);;
                            total += price;
                        }
                        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
                        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
                        Call<ResponseBody> activatecall = paymentApi.savePayment(Config.API_KEY, packageItem.getPlanId(),
                                user.getUserId(),
                                String.valueOf(total),
                                merge, mode);
                        Retrofit mlmInstance = RetrofitClient.getRetrofitMLMInstance();
                        WalletApi walletApi = mlmInstance.create(WalletApi.class);
                        //String packageName = "PINGPONG_MEMBERSHIP_199";
                        String packageName = "PINGPONG_MEMBERSHIP_"+packageItem.getPrice();
                        Retrofit phoneRetrofit = RetrofitClient.getRetrofitInstance();
                        UserDataApi api = phoneRetrofit.create(UserDataApi.class);
                        Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
                        call.enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> cllresponse) {
                                if (cllresponse.code() == 200) {
                                    if (cllresponse.body() != null) {
                                        User user1 = cllresponse.body();
                                        Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH,user1.getPhone(),user1.getPassword(), Config.PARTER_ID, "PG",packageName);
                                        //Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH, "9923290044", "Xyz_123",Config.PARTER_ID, "PG",packageName);
                                        activatecall.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (response.code() == 200) {
                                                    updateActiveStatus(user1.getUserId());
                                                    balCall.enqueue(new Callback<MlmLogin>() {
                                                        @Override
                                                        public void onResponse(Call<MlmLogin> call, Response<MlmLogin> ctresponse) {
                                                            if(ctresponse.code() == 200){
                                                                storeTxLog(user1.getUserId(), ctresponse.body().getStatus() +"#"+ctresponse.body().getRemarks(),"PG");
                                                                //Log.e(TAG, "onResponse: "+ctresponse.body().getStatus()+" "+ctresponse.body().getRemarks());
                                                            }
                                                            hideDialog();
                                                            new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("You've subscribed successfully");
                                                            Intent i = new Intent(PurchasePlanActivity.this, MainActivity.class);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onFailure(Call<MlmLogin> call, Throwable t) {
                                                            //Log.e(TAG, "onResponse: "+t.getMessage());
                                                            hideDialog();
                                                            new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("You've subscribed successfully");
                                                            Intent i = new Intent(PurchasePlanActivity.this, MainActivity.class);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                            finish();
                                                        }
                                                    });
                                                } else {
                                                    hideDialog();
                                                    new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                hideDialog();
                                                new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                hideDialog();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Failed to process transaction");
                }
            } else if (resultModel != null && resultModel.getError() != null) {
                //Log.d(TAG, "Error response : " + resultModel.getError().getTransactionResponse());
            } else {
                //Log.d(TAG, "Both objects are null!");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void completePayment(String paymentDetails) {
        try {
            JSONObject jsonObject = new JSONObject(paymentDetails);
            sendDataToServer(jsonObject.getJSONObject("response"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendDataToServer(JSONObject response) {
        try {
            String payId = response.getString("id");
            final String state = response.getString("state");
            final String userId = PreferenceUtils.getUserId(PurchasePlanActivity.this);

            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            Call<ResponseBody> call = paymentApi.savePayment(Config.API_KEY, packageItem.getPlanId(), userId, packageItem.getPrice(),
                    payId, "Paypal");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {

                        updateActiveStatus(userId);

                    } else {
                        new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    t.printStackTrace();
                    //Log.e("PAYMENT", "error: " + t.getLocalizedMessage());
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activiStatus = response.body();
                    saveActiveStatus(activiStatus);
                } else {
                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents wrong");
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                new ToastMsg(PurchasePlanActivity.this).toastIconError(t.getMessage());
                t.printStackTrace();
            }
        });

    }

    private void storeTxLog(String userId, String response, String mode) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.saveMlmTxLog(Config.API_KEY, userId,response, mode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void saveActiveStatus(ActiveStatus activeStatus) {
        DatabaseHelper db = new DatabaseHelper(PurchasePlanActivity.this);
        if (db.getActiveStatusCount() > 1) {
            db.deleteAllActiveStatusData();
        }
        if (db.getActiveStatusCount() == 0) {
            db.insertActiveStatusData(activeStatus);
        } else {
            db.updateActiveStatus(activeStatus, 1);
        }
        db.close();
    }


    private void initView() {

        noTv = findViewById(R.id.no_tv);
        progressBar = findViewById(R.id.progress_bar);
        packageRv = findViewById(R.id.pacakge_rv);
        closeIv = findViewById(R.id.close_iv);

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(paymentBottomShitDialog != null){
                    paymentBottomShitDialog.dismissAllowingStateLoss();
                }
                finish();
            }
        });
    }

    @Override
    public void onItemClick(Package pac) {
        packageItem = pac;

        paymentBottomShitDialog = new PaymentBottomShitDialog();
        paymentBottomShitDialog.show(getSupportFragmentManager(), "PaymentBottomShitDialog");
    }

    @Override
    public void onBottomShitClick(String paymentMethodName) {
//        if (paymentMethodName.equals(PaymentBottomShitDialog.PAYPAL)) {
//           processPaypalPayment(packageItem);
//
//        } else if (paymentMethodName.equals(PaymentBottomShitDialog.STRIP)) {
//            Intent intent = new Intent(PurchasePlanActivity.this, StripePaymentActivity.class);
//            intent.putExtra("package", packageItem);
//            intent.putExtra("currency", currency);
//            startActivity(intent);
//
//        }else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.RAZOR_PAY)){
//            Intent intent = new Intent(PurchasePlanActivity.this, RazorPayActivity.class);
//            intent.putExtra("package", packageItem);
//            intent.putExtra("currency", currency);
//            startActivity(intent);
//        }

//        if(paymentMethodName.equalsIgnoreCase("wallet")){
//            dialog.show();
//            double total = Double.parseDouble(packageItem.getPrice());
//            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
//            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
//            Call<ResponseBody> activationCall = paymentApi.savePayment(Config.API_KEY, packageItem.getPlanId(),
//                    user.getUserId(),
//                    String.valueOf(total), "Wallet", "Vips Wallet");
//            Retrofit mlmInstance = RetrofitClient.getRetrofitMLMInstance();
//            WalletApi walletApi = mlmInstance.create(WalletApi.class);
//            //String packageName = "PINGPONG_MEMBERSHIP_199";
//            String packageName = "PINGPONG_MEMBERSHIP_"+packageItem.getPrice();
//            Retrofit phoneRetrofit = RetrofitClient.getRetrofitInstance();
//            UserDataApi api = phoneRetrofit.create(UserDataApi.class);
//            Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
//            call.enqueue(new Callback<User>() {
//                @Override
//                public void onResponse(Call<User> call, Response<User> callrepsons) {
//                    if (callrepsons.code() == 200) {
//                        if (callrepsons.body() != null) {
//                            User user1 = callrepsons.body();
//                            Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH,user1.getPhone(),user1.getPassword(), Config.PARTER_ID, "VIPSWALLET_APP",packageName);
//                            //Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH, "9923290044", "Xyz_123",Config.PARTER_ID, "PG",packageName);
//                            balCall.enqueue(new Callback<MlmLogin>() {
//                                @Override
//                                public void onResponse(Call<MlmLogin> call, Response<MlmLogin> response) {
//                                    if (response.code() == 200){
//                                        if(response.body() != null){
//                                            MlmLogin mlmLogin = response.body();
//                                            //rx log
//                                            storeTxLog(user1.getUserId(), mlmLogin.getRemarks()+ "#"+mlmLogin.getStatus(),"Wallet");
//                                            if(mlmLogin.getStatus().equalsIgnoreCase("Success")){
//                                                //subscription...
//                                                activationCall.enqueue(new Callback<ResponseBody>() {
//                                                    @Override
//                                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> retresponse) {
//                                                        hideDialog();
//                                                        if (retresponse.code() == 200) {
//                                                            updateActiveStatus(user.getUserId());
//                                                            new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("You've subscribed successfully");
//                                                            Intent i = new Intent(PurchasePlanActivity.this, MainActivity.class);
//                                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                            startActivity(i);
//                                                            finish();
//                                                        } else {
//                                                            new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                                                        hideDialog();
//                                                        new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
//                                                    }
//                                                });
//                                            }else{
//                                                hideDialog();
//                                                new ToastMsg(PurchasePlanActivity.this).toastIconError("Failed to purchase membership");
//                                            }
//                                        }else{
//                                            hideDialog();
//                                            new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents Wrong");
//                                        }
//                                    }else{
//                                        hideDialog();
//                                        new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents Wrong");
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<MlmLogin> call, Throwable t) {
//                                    hideDialog();
//                                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents Wrong");
//                                }
//                            });
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<User> call, Throwable t) {
//                    hideDialog();
//                }
//            });
//        }else
        if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.RAZOR)){
            //razor pay
            Checkout.preload(getApplicationContext());
            Checkout checkout = new Checkout();
            checkout.setKeyID("");
            checkout.setImage(R.drawable.logo);
            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            UserDataApi api = retrofit.create(UserDataApi.class);
            Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            User user = response.body();
                            if (packageItem != null && !TextUtils.isEmpty(packageItem.getName())) {
                                JSONObject options = new JSONObject();
                                try {
                                    options.put("theme.color", "#6BFF1C");
                                    options.put("name",  getString(R.string.app_name)+" Payment");
                                    options.put("description", packageItem.getName());
                                    //options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
                                    //options.put("currency", config.getCurrency());
                                    options.put("currency", "INR");
                                    options.put("amount",packageItem.getPrice());
                                    JSONObject prefill = new JSONObject();
                                    prefill.put("email", user.getEmail());
                                    prefill.put("contact", user.getPhone());
                                    options.put("prefill", prefill);
                                    checkout.open(PurchasePlanActivity.this, options);
                                } catch(Exception e) {
                                    Log.e(TAG, "Error in starting Razorpay Checkout", e);
                                }
                            }else{
                                new ToastMsg(PurchasePlanActivity.this).toastIconError("Please, Select Package");
                            }
                        }else{
                            new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong! Please, Login & try again");
                            Intent intent = new Intent(PurchasePlanActivity.this, GetStarted.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong! Please, Login & try again");
                        Intent intent = new Intent(PurchasePlanActivity.this, GetStarted.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {

                }
            });


        }else{
            launchPayUMoneyFlow();
        }
    }

    @Override
    public void onBottomShitClick(String paymentMethodName, double bal) {
        if(paymentMethodName.equalsIgnoreCase("wallet")){
            dialog.show();
            double total = Double.parseDouble(packageItem.getPrice());
            if(bal >= total) {
                Retrofit retrofit = RetrofitClient.getRetrofitInstance();
                PaymentApi paymentApi = retrofit.create(PaymentApi.class);
                Call<ResponseBody> activationCall = paymentApi.savePayment(Config.API_KEY, packageItem.getPlanId(),
                        user.getUserId(),
                        String.valueOf(total), "Wallet", "Vips Wallet");
                Retrofit mlmInstance = RetrofitClient.getRetrofitMLMInstance();
                WalletApi walletApi = mlmInstance.create(WalletApi.class);
                //String packageName = "PINGPONG_MEMBERSHIP_199";
                String packageName = "PINGPONG_MEMBERSHIP_" + packageItem.getPrice();
                Retrofit phoneRetrofit = RetrofitClient.getRetrofitInstance();
                UserDataApi api = phoneRetrofit.create(UserDataApi.class);
                Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> callrepsons) {
                        if (callrepsons.code() == 200) {
                            if (callrepsons.body() != null) {
                                User user1 = callrepsons.body();
                                Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH, user1.getPhone(), user1.getPassword(), Config.PARTER_ID, "VIPSWALLET_APP", packageName);
                                //Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH, "9923290044", "Xyz_123",Config.PARTER_ID, "PG",packageName);
                                balCall.enqueue(new Callback<MlmLogin>() {
                                    @Override
                                    public void onResponse(Call<MlmLogin> call, Response<MlmLogin> response) {
                                        if (response.code() == 200) {
                                            if (response.body() != null) {
                                                MlmLogin mlmLogin = response.body();
                                                //rx log
                                                storeTxLog(user1.getUserId(), mlmLogin.getRemarks() + "#" + mlmLogin.getStatus(), "Wallet");
                                                if (mlmLogin.getStatus().equalsIgnoreCase("Success")) {
                                                    //subscription...
                                                    activationCall.enqueue(new Callback<ResponseBody>() {
                                                        @Override
                                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> retresponse) {
                                                            hideDialog();
                                                            if (retresponse.code() == 200) {
                                                                updateActiveStatus(user.getUserId());
                                                                new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("You've subscribed successfully");
                                                                Intent i = new Intent(PurchasePlanActivity.this, MainActivity.class);
                                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(i);
                                                                finish();
                                                            } else {
                                                                new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                            hideDialog();
                                                            new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                                                        }
                                                    });
                                                } else {
                                                    hideDialog();
                                                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Failed to purchase membership");
                                                }
                                            } else {
                                                hideDialog();
                                                new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents Wrong");
                                            }
                                        } else {
                                            hideDialog();
                                            new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents Wrong");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<MlmLogin> call, Throwable t) {
                                        hideDialog();
                                        new ToastMsg(PurchasePlanActivity.this).toastIconError("Something Wents Wrong");
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        hideDialog();
                    }
                });
            }else{
                new ToastMsg(PurchasePlanActivity.this).toastIconError("Your bal is low");
            }
        }
    }


    /**
     *
     */
    private void launchPayUMoneyFlow() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserDataApi api = retrofit.create(UserDataApi.class);
        Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        User user = response.body();
                        if(packageItem != null && !TextUtils.isEmpty(packageItem.getName())){
                            PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();

                            //Use this to set your custom text on result screen button
                            payUmoneyConfig.setDoneButtonText(getString(R.string.payment_help));

                            //payUmoneyConfig.setAccentColor("#6BFF1C");
                            //payUmoneyConfig.setColorPrimary("#6BFF1C");
                            //payUmoneyConfig.setColorPrimaryDark("#9eff2c");

                            //Use this to set your custom title for the activity
                            payUmoneyConfig.setPayUmoneyActivityTitle(getString(R.string.app_name)+" Payment");

                            PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();

                            double amount = 0;
                            try {
                                amount = Double.parseDouble(packageItem.getPrice());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String txnId = "TXNID"+user.getUserId();
                            String phone = user.getPhone();
                            String productName = packageItem.getName();
                            String firstName = user.getName();
                            String email = user.getEmail();
                            String udf1 = "";
                            String udf2 = "";
                            String udf3 = "";
                            String udf4 = "";
                            String udf5 = "";
                            String udf6 = "";
                            String udf7 = "";
                            String udf8 = "";
                            String udf9 = "";
                            String udf10 = "";

                            AppEnvironment appEnvironment = ((MyAppClass) getApplication()).getAppEnvironment();
                            builder.setAmount(String.valueOf(amount))
                                    .setTxnId(txnId)
                                    .setPhone(phone)
                                    .setProductName(productName)
                                    .setFirstName(firstName)
                                    .setEmail(email)
                                    .setsUrl(appEnvironment.surl())
                                    .setfUrl(appEnvironment.furl())
                                    .setUdf1(udf1)
                                    .setUdf2(udf2)
                                    .setUdf3(udf3)
                                    .setUdf4(udf4)
                                    .setUdf5(udf5)
                                    .setUdf6(udf6)
                                    .setUdf7(udf7)
                                    .setUdf8(udf8)
                                    .setUdf9(udf9)
                                    .setUdf10(udf10)
                                    .setIsDebug(appEnvironment.debug())
                                    .setKey(appEnvironment.merchant_Key())
                                    .setMerchantId(appEnvironment.merchant_ID());
                            try {
                                //mPaymentParams = builder.build();
                                mPaymentParams = calculateServerSideHashAndInitiatePayment1(builder.build());
                                PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams,PurchasePlanActivity.this, R.style.AppTheme_default, false);
//                                Retrofit retrofit = RetrofitClient.getOtherRetrofitInstance();
//                                PayuHashApi payuHashApi = retrofit.create(PayuHashApi.class);
//                                Call<PayumoneyHash> payumoneyHashCall = payuHashApi.getHash("",txnId,String.valueOf(amount),productName, firstName, email,udf1, udf2, udf3, udf4, udf5);
//                                payumoneyHashCall.enqueue(new Callback<PayumoneyHash>() {
//                                    @Override
//                                    public void onResponse(Call<PayumoneyHash> call, Response<PayumoneyHash> response) {
//                                        if (response.code() == 200){
//                                            PayumoneyHash hash = response.body();
//                                            if(hash != null &&  hash.getResult() != null && !TextUtils.isEmpty(hash.getResult())){
//                                                mPaymentParams.setMerchantHash(hash.getResult());
//                                                PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams,PurchasePlanActivity.this, R.style.AppTheme_default, false);
//                                            }else{
//                                                new ToastMsg(PurchasePlanActivity.this).toastIconError("Failed to process payment");
//                                            }
//                                        }else{
//                                            new ToastMsg(PurchasePlanActivity.this).toastIconError("Something wents wrong!");
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<PayumoneyHash> call, Throwable t) {
//                                        new ToastMsg(PurchasePlanActivity.this).toastIconError("Something wents wrong!");
//                                        t.printStackTrace();
//                                    }
//                                });
                            } catch (Exception e) {
                                //new ToastMsg(PurchasePlanActivity.this).toastIconError("Se");
                                new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong!, Please, try again after sometime");
                                // e.printStackTrace();
                            }
                        }else{
                            new ToastMsg(PurchasePlanActivity.this).toastIconError("Please, Select Package");
                        }

                    }else{
                        new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong! Please, Login & try again");
                        Intent intent = new Intent(PurchasePlanActivity.this, GetStarted.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong! Please, Login & try again");
                    Intent intent = new Intent(PurchasePlanActivity.this, GetStarted.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong!, Please, try again after sometime");
                // t.printStackTrace();
            }
        });

    }

    /**
     * Thus function calculates the hash for transaction
     *
     * @param paymentParam payment params of transaction
     * @return payment params along with calculated merchant hash
     */
    private PayUmoneySdkInitializer.PaymentParam calculateServerSideHashAndInitiatePayment1(final PayUmoneySdkInitializer.PaymentParam paymentParam) {
        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> params = paymentParam.getParams();
        stringBuilder.append(params.get(PayUmoneyConstants.KEY) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.TXNID) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.AMOUNT) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.PRODUCT_INFO) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.FIRSTNAME) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.EMAIL) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF1) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF2) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF3) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF4) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF5) + "||||||");
        AppEnvironment appEnvironment = ((MyAppClass) getApplication()).getAppEnvironment();
        stringBuilder.append(appEnvironment.salt());
        String hash = Tools.hashCal(stringBuilder.toString());
        paymentParam.setMerchantHash(hash);
        return paymentParam;
    }

    @Override
    protected void onDestroy() {
        if(dialog != null){
            if(dialog.isShowing() && !isFinishing()){
                dialog.dismiss();
            }
        }
        dialog = null;
        super.onDestroy();
    }

    public void hideDialog(){
        if(dialog != null){
            if(!isFinishing()){
                dialog.dismiss();
            }
        }
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
            dialog.show();
            String txnid = user.getUserId()+"#"+razorpayPaymentID;
            double total = Double.parseDouble(packageItem.getPrice());

            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            Call<ResponseBody> activatecall = paymentApi.savePayment(Config.API_KEY, packageItem.getPlanId(),
                    user.getUserId(),
                    String.valueOf(total),
                    txnid, "");
            Retrofit mlmInstance = RetrofitClient.getRetrofitMLMInstance();
            WalletApi walletApi = mlmInstance.create(WalletApi.class);
            String packageName = "PINGPONG_MEMBERSHIP_"+packageItem.getPrice();
            Retrofit phoneRetrofit = RetrofitClient.getRetrofitInstance();
            UserDataApi api = phoneRetrofit.create(UserDataApi.class);
            Call<User> call = api.getUserData(Config.API_KEY, user.getUserId());
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> cllresponse) {
                    if (cllresponse.code() == 200) {
                        if (cllresponse.body() != null) {
                            User user1 = cllresponse.body();
                            Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH,user1.getPhone(),user1.getPassword(), Config.PARTER_ID, "PG",packageName);
                            //Call<MlmLogin> balCall = walletApi.purchaseMemberShip(Config.MLM_AUTH, "9923290044", "Xyz_123",Config.PARTER_ID, "PG",packageName);
                            activatecall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.code() == 200) {
                                        updateActiveStatus(user1.getUserId());
                                        balCall.enqueue(new Callback<MlmLogin>() {
                                            @Override
                                            public void onResponse(Call<MlmLogin> call, Response<MlmLogin> ctresponse) {
                                                if(ctresponse.code() == 200){
                                                    storeTxLog(user1.getUserId(), ctresponse.body().getStatus() +"#"+ctresponse.body().getRemarks(),"PG");
                                                    //Log.e(TAG, "onResponse: "+ctresponse.body().getStatus()+" "+ctresponse.body().getRemarks());
                                                }
                                                hideDialog();
                                                new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("You've subscribed successfully");
                                                Intent i = new Intent(PurchasePlanActivity.this, MainActivity.class);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(i);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Call<MlmLogin> call, Throwable t) {
                                                //Log.e(TAG, "onResponse: "+t.getMessage());
                                                hideDialog();
                                                new ToastMsg(PurchasePlanActivity.this).toastIconSuccess("You've subscribed successfully");
                                                Intent i = new Intent(PurchasePlanActivity.this, MainActivity.class);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(i);
                                                finish();
                                            }
                                        });
                                    } else {
                                        hideDialog();
                                        new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    hideDialog();
                                    new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    hideDialog();
                }
            });
        } catch (Exception e) {
            new ToastMsg(PurchasePlanActivity.this).toastIconError("Something went wrong!, Please, try again after sometime");
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        try {
            new ToastMsg(PurchasePlanActivity.this).toastIconError("Failed to process payment");
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError", e);
        }
    }
}
