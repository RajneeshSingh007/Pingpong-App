package com.pingpong;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.pingpong.R;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.LoginApi;
import com.pingpong.network.apis.SignUpApi;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.mlm.AmountCredit;
import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.mlm.postmodel.Login;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.DeviceIdResult;
import com.pingpong.network.model.User;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.Constants;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail,etPass, etParterId;
    private TextView tvSignUp,tvReset,passText;
    private TextInputLayout passLayout;
    private Button btnLogin;
    private ProgressDialog dialog;
    private View backgroundView;
    private int isMlm = -1;
    private String phone = "";
    private FirebaseFirestore firebaseFirestore;
    private String IMEI_NO = "";
    private static final int PERMISSION_REQUEST_CODE = 5555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "login_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etEmail=findViewById(R.id.email);
        etPass=findViewById(R.id.password);
        tvSignUp=findViewById(R.id.signup);
        btnLogin=findViewById(R.id.signin);
        tvReset=findViewById(R.id.reset_pass);
        etParterId = findViewById(R.id.partnerId);

        isMlm = getIntent().getIntExtra("isMlm", -1);
        phone = getIntent().getStringExtra("phone");

        etEmail.setText(phone);
//        backgroundView=findViewById(R.id.background_view);
//        if (isDark) {
//            backgroundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
//            btnLogin.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
//        }

        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,PassResetActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String pass = etPass.getText().toString();
                //String parterid = etParterId.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    new ToastMsg(LoginActivity.this).toastIconError("Please email or mobile number empty");
                }else if(pass.equals("")){
                    new ToastMsg(LoginActivity.this).toastIconError("Please enter password");
                }else {
                    login(email,pass,Config.PARTER_ID);
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }

        });
        FirebaseFirestoreSettings.Builder firebaseFirestoreSettings = new FirebaseFirestoreSettings.Builder();
        firebaseFirestoreSettings.setPersistenceEnabled(false);
                //.setTimestampsInSnapshotsEnabled(true);
        firebaseFirestore  = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(firebaseFirestoreSettings.build());

//        try {
//            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
//            } else {
//                String imei = "";
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    imei = telephonyManager.getImei();
//                } else {
//                    imei = telephonyManager.getDeviceId();
//                }
//                if (imei != null && !imei.isEmpty()) {
//                    IMEI_NO = imei;
//                } else if (android.os.Build.SERIAL != null && TextUtils.isEmpty(android.os.Build.SERIAL)) {
//                    IMEI_NO = android.os.Build.SERIAL;
//                }
//            }
//            Log.e("imei", "=" + IMEI_NO);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    private void login(String email, final String password, String parterid){
        dialog.show();
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //MLM Check
        if(isMlm == 0) {
            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            LoginApi api = retrofit.create(LoginApi.class);
            Call<User> call = api.postLoginStatus(Config.API_KEY, email, password,false);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.code() == 200) {
                        if (response.body().getStatus().equalsIgnoreCase("success")) {
                            User user = response.body();
                            DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
                            db.deleteUserData();
                            db.insertUserData(user);
                            ApiResources.USER_PHONE = user.getPhone();
                            SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                            preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                            preferences.putString(Constants.USER_PASSWORD_AVAILABLE, password);
                            preferences.apply();
                            preferences.commit();
                            hideDialog();
                            HashMap<String, String> data = new HashMap<>();
                            data.put("id", user.getUserId());
                            DocumentReference ref = firebaseFirestore.collection("users")
                                    .document(user.getUserId());
                            ref.get()
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful() && task.getResult() != null && task.getResult().exists()){
                                                   data.put("newflag", deviceId);
                                                   ref.set(data, SetOptions.merge())
                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if(task.isSuccessful()){
                                                                       startMainActivity();
                                                                       //updateSubscriptionStatus(db.getUserData().getUserId());
                                                                   }else{
                                                                       new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
                                                                   }
                                                               }
                                                           });
                                            }else {
                                                data.put("flag", deviceId);
                                                ref.set(data)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    startMainActivity();
                                                                    //updateSubscriptionStatus(db.getUserData().getUserId());
                                                                }else{
                                                                    new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            cancelDialog();
                                            new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
                                        }
                                    });
                        } else {
                            hideDialog();
                            new ToastMsg(LoginActivity.this).toastIconError(response.body().getData());
                        }
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    cancelDialog();
                    new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
                }
            });
        }else{
            Retrofit retrofit = RetrofitClient.getRetrofitMLMInstance();
            LoginApi api = retrofit.create(LoginApi.class);
            Call<MlmLogin> loginCall = api.postMlMLoginStatus(Config.MLM_AUTH, new Login(email, password, parterid));
            loginCall.enqueue(new Callback<MlmLogin>() {
                @Override
                public void onResponse(Call<MlmLogin> call, Response<MlmLogin> response) {
                    hideDialog();
                    if (response.code() == 200) {
                        MlmLogin mlmLogin = response.body();
                        if (mlmLogin.getStatus().equalsIgnoreCase("success")) {
                            String city = "";
                            if(!TextUtils.isEmpty(mlmLogin.getData().getCityName())){
                                city = mlmLogin.getData().getCityName();
                            }

                            String state = "";
                            if(!TextUtils.isEmpty(mlmLogin.getData().getStateName())){
                                state = mlmLogin.getData().getStateName();
                            }

                            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                            intent.putExtra("login", true);
                            intent.putExtra("email", mlmLogin.getData().getEmailId());
                            intent.putExtra("password", password);
                            intent.putExtra("name", mlmLogin.getData().getFName());
                            intent.putExtra("lname", mlmLogin.getData().getLName());
                            intent.putExtra("mobile", mlmLogin.getData().getMobileNo());
                            intent.putExtra("city",city);
                            intent.putExtra("state",state);
                            intent.putExtra("referby",mlmLogin.getData().getMobileNo());
                            startActivity(intent);

//                            Retrofit retrofitx = RetrofitClient.getRetrofitInstance();
//                            SignUpApi signUpApi = retrofitx.create(SignUpApi.class);
//                            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//                            Call<User> callval = signUpApi.signUp(Config.API_KEY, mlmLogin.getData().getEmailId(), password, mlmLogin.getData().getFName() + " "+mlmLogin.getData().getLName(),mlmLogin.getData().getMobileNo(),1, city, state,deviceId,mlmLogin.getData().getMobileNo());
//                            callval.enqueue(new Callback<User>() {
//                                @Override
//                                public void onResponse(Call<User> call, Response<User> response) {
//                                    if(response.code() == 200){
//                                        User signup = response.body();
//                                        if (signup.getStatus().equals("success")) {
//                                            amountCredit(mlmLogin.getData().getMobileNo(), 5, Config.SECURE_KEY);
////                                                Call<DeviceIdResult> objectCall = signUpApi.checkUserDevice(Config.API_KEY, IMEI_NO);
////                                                objectCall.enqueue(new Callback<DeviceIdResult>() {
////                                                    @Override
////                                                    public void onResponse(Call<DeviceIdResult> call, Response<DeviceIdResult> response) {
////                                                        if (response.body() != null) {
////                                                            DeviceIdResult object = response.body();
////                                                            if (object.getStatus()) {
////                                                                //ignored
////                                                            } else {
////                                                                if (!TextUtils.isEmpty(IMEI_NO)) {
////                                                                }
////                                                            }
////                                                        }
////                                                    }
////
////                                                    @Override
////                                                    public void onFailure(Call<DeviceIdResult> call, Throwable t) {
////
////                                                    }
////                                                });
//
//                                            hideDialog();
//                                            DatabaseHelper db = new DatabaseHelper(LoginActivity.this);
//                                            db.deleteUserData();
//                                            db.insertUserData(signup);
//                                            ApiResources.USER_PHONE = signup.getPhone();
//
//                                            SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
//                                            preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
//                                            preferences.putString(Constants.USER_PASSWORD_AVAILABLE, password);
//                                            preferences.apply();
//                                            preferences.commit();
//
//                                            HashMap<String, String> data = new HashMap<>();
//                                            data.put("id", signup.getUserId());
//                                            DocumentReference ref = firebaseFirestore.collection("users")
//                                                    .document(signup.getUserId());
//                                            ref.get()
//                                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<DocumentSnapshot>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                            if(task.isSuccessful() && task.getResult() != null && task.getResult().exists()){
//                                                                data.put("newflag", deviceId);
//                                                                ref.set(data, SetOptions.merge())
//                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                            @Override
//                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                                if(task.isSuccessful()){
//                                                                                    startMainActivity();
//                                                                                    //updateSubscriptionStatus(db.getUserData().getUserId());
//                                                                                }else{
//                                                                                    new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
//                                                                                }
//                                                                            }
//                                                                        });
//                                                            }else {
//                                                                data.put("flag", deviceId);
//                                                                ref.set(data)
//                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                            @Override
//                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                                if(task.isSuccessful()){
//                                                                                    startMainActivity();
//                                                                                    //updateSubscriptionStatus(db.getUserData().getUserId());
//                                                                                }else{
//                                                                                    new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
//                                                                                }
//                                                                            }
//                                                                        });
//                                                            }
//                                                        }
//                                                    })
//                                                    .addOnFailureListener(new OnFailureListener() {
//                                                        @Override
//                                                        public void onFailure(@NonNull Exception e) {
//                                                            e.printStackTrace();
//                                                            new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
//                                                        }
//                                                    });
//
//                                        } else if (signup.getStatus().equals("error")){
//                                            cancelDialog();
//                                            new ToastMsg(LoginActivity.this).toastIconError(signup.getData());
//                                        }
//                                    }else {
//                                        cancelDialog();
//                                        new ToastMsg(LoginActivity.this).toastIconError("Something went wrong.");
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<User> call, Throwable t) {
//                                    cancelDialog();
//                                    new ToastMsg(LoginActivity.this).toastIconError("Something went wrong.");
//                                }
//                            });
                        }else {
                            new ToastMsg(LoginActivity.this).toastIconError(response.body().getRemarks());
                        }
                    }
                }

                @Override
                public void onFailure(Call<MlmLogin> call, Throwable t) {
                    cancelDialog();
                    new ToastMsg(LoginActivity.this).toastIconError("Something Wents wrong.");
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public  boolean isValidNumber(String email){
        String numberPattern = "^[0-9]*$";
        return email.matches(numberPattern);
    }


    public void startMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
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

    public  void cancelDialog(){
        if(dialog != null){
            if(!isFinishing()){
                dialog.cancel();
            }
        }
    }

    public void hideDialog(){
        if(dialog != null){
            if(!isFinishing()){
                dialog.dismiss();
            }
        }
    }

//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    String imei = "";
//                    try {
//                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                                // TODO: Consider calling
//                                //    ActivityCompat#requestPermissions
//                                // here to request the missing permissions, and then overriding
//                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                //                                          int[] grantResults)
//                                // to handle the case where the user grants the permission. See the documentation
//                                // for ActivityCompat#requestPermissions for more details.
//                                return;
//                            }
//                            imei = telephonyManager.getImei();
//                        }else{
//                            imei = telephonyManager.getDeviceId();
//                        }
//                        if (imei != null && !imei.isEmpty()) {
//                            IMEI_NO = imei;
//                        } else if(android.os.Build.SERIAL != null && TextUtils.isEmpty(android.os.Build.SERIAL)) {
//                            IMEI_NO = android.os.Build.SERIAL;
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                } else {
//                    new ToastMsg(LoginActivity.this).toastIconError("Please, grant permission");
//                }
//                break;
//        }
//    }

    private void amountCredit(String phone, int amount, String secureKey) {
        Retrofit retrofitMLMInstance = RetrofitClient.getRetrofitMLMInstance();
        SignUpApi signUpApi = retrofitMLMInstance.create(SignUpApi.class);
        Call<MlmLogin> amountCall = signUpApi.creditAmount(Config.MLM_AUTH, new AmountCredit(phone, amount, secureKey));
        amountCall.enqueue(new Callback<MlmLogin>() {
            @Override
            public void onResponse(Call<MlmLogin> callx, Response<MlmLogin> response) {
                if (response.body() != null) {
                    Log.e("Credit", "onResponse: " + response.body().getRemarks());
                }
            }

            @Override
            public void onFailure(Call<MlmLogin> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

}
