package com.pingpong;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.listener.OtpCallback;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.CityApi;
import com.pingpong.network.apis.SignUpApi;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.mlm.AmountCredit;
import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.mlm.postmodel.Register;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.CityData;
import com.pingpong.network.model.DeviceIdResult;
import com.pingpong.network.model.OTPResult;
import com.pingpong.network.model.User;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.Constants;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity implements OtpCallback {

    private EditText etName, etEmail, etPass, etcPass, etLName, etphone, etreferral, otpEditText;
    private Button btnSignup;
    private ProgressDialog dialog;
    private View backgorundView;
    private Spinner citySpinner, stateSpinner;
    private String selectedState = "", selectedCity = "";
    private ArrayAdapter cityListAdapter;
    private LinearLayout otpLayout, registerLayout;
    private String IMEI_NO = "";
    private String fromPlayStore = "";
    private boolean otpMode = true;
    private Integer otp_number = -1;
    private TextView resendOTP;
    private long oldTime = -1;
    private boolean isFromLoginMlmUser = false;
    private String email = "",pass="",name="",lname="",cpass="",phone="",ref="";
    private int mlmuser = 0;
    private FirebaseFirestore firebaseFirestore;

    private static final int PERMISSION_REQUEST_CODE = 5555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Back");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "sign_up_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        FirebaseFirestoreSettings.Builder firebaseFirestoreSettings = new FirebaseFirestoreSettings.Builder();
        firebaseFirestoreSettings.setPersistenceEnabled(false)
                .setTimestampsInSnapshotsEnabled(true);
        firebaseFirestore  = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(firebaseFirestoreSettings.build());

        //filterMap = CityHelper.getCityMap();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        etcPass = findViewById(R.id.cpassword);
        etLName = findViewById(R.id.lname);
        btnSignup = findViewById(R.id.signup);
        etphone = findViewById(R.id.phone);
        citySpinner = findViewById(R.id.city_spinner);
        stateSpinner = findViewById(R.id.state_spinner);
        etreferral = findViewById(R.id.referral);
        otpLayout = findViewById(R.id.otpLayout);
        registerLayout = findViewById(R.id.registerLayout);
        otpEditText = findViewById(R.id.otpEditText);
        resendOTP = findViewById(R.id.resendOTP);

        registerLayout.setVisibility(View.VISIBLE);
        otpLayout.setVisibility(View.GONE);

        String refer = getIntent().getStringExtra("referral");

        if (!TextUtils.isEmpty(refer)) {
            fromPlayStore = refer;
            etreferral.setText(refer);
            etreferral.setEnabled(false);
            etreferral.setFocusableInTouchMode(false);
            etreferral.setFocusable(false);
        }

        phone = getIntent().getStringExtra("phone");
        if (!TextUtils.isEmpty(phone)) {
            etphone.setText(phone);
        }

        isFromLoginMlmUser = getIntent().getBooleanExtra("login", false);

        if(isFromLoginMlmUser){

            oldTime = -1;
            otp_number = -1;

            mlmuser  = 1;

            email = getIntent().getStringExtra("email");
            etEmail.setText(email);

            pass = getIntent().getStringExtra("password");
            etPass.setText(pass);
            etcPass.setText(pass);

            name = getIntent().getStringExtra("name");
            etName.setText(name);


            lname = getIntent().getStringExtra("lname");
            etLName.setText(lname);

            phone = getIntent().getStringExtra("mobile");
            etphone.setText(phone);

            selectedCity = getIntent().getStringExtra("city");

            selectedState = getIntent().getStringExtra("state");

            ref = getIntent().getStringExtra("referby");
            etreferral.setText(ref);

            otpLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.GONE);
            sendotp(SignUpActivity.this, false);
        }


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                pass = etPass.getText().toString();
                name = etName.getText().toString();
                lname = etLName.getText().toString();
                cpass = etcPass.getText().toString();
                phone = etphone.getText().toString();
                ref = etreferral.getText().toString();
                if (!isValidEmailAddress(email)) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter valid email");
                } else if (pass.toString().equals("")) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter password");
                } else if (cpass.equals("")) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter confirm password");
                } else if (name.equals("")) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter name");
                } else if (lname.equals("")) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter last name");
                } else if (phone.equalsIgnoreCase("")) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter mobile number");
                }else if (phone.length() < 10) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter correct mobile number");
                }else if (!TextUtils.isEmpty(ref) &&  ref.length() < 10) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter correct referby number");
                }
//                else if (selectedCity.equalsIgnoreCase("")){
//                    new ToastMsg(SignUpActivity.this).toastIconError("Please select city");
//                }else if (selectedState.equalsIgnoreCase("")){
//                    new ToastMsg(SignUpActivity.this).toastIconError("Please select State");
//                }
                else if (pass.length() < 6) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Password is short");
                } else if (cpass.length() < 6) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Confirm Password is short");
                } else if (!pass.equals(cpass)) {
                    new ToastMsg(SignUpActivity.this).toastIconError("Please correct confirm password");
                } else {
                    if(otpMode){
                        sendotp(SignUpActivity.this, false);
                    }else{
                        String otpText = otpEditText.getText().toString();
                        if(TextUtils.isEmpty(otpText)){
                            new ToastMsg(SignUpActivity.this).toastIconError("OTP Empty");
                        }else if(otpText.length() < 6){
                            new ToastMsg(SignUpActivity.this).toastIconError("OTP is short");
                        }else if(otp_number != Integer.parseInt(otpText)){
                            new ToastMsg(SignUpActivity.this).toastIconError("Failed to match OTP");
                        }else{
                            if(mlmuser == 1){
                                pingponRegisterAccount(email, pass, name, lname, phone, ref, mlmuser);
                            }else{
                                registerAccount(email, pass, name, lname, phone, ref,mlmuser);
                            }
                        }
                    }
                }
            }
        });


        Retrofit retrofit = RetrofitClient.getOtherRetrofitInstance();
        CityApi cityApi = retrofit.create(CityApi.class);

        ArrayAdapter<String> stateList = new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.state_list));
        stateList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateList);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    selectedState = (String) stateList.getItem(i);
                    dialog.show();
                    Call<CityData> cityDataCall = cityApi.getCityList(selectedState);
                    cityDataCall.enqueue(new Callback<CityData>() {
                        @Override
                        public void onResponse(Call<CityData> call, Response<CityData> response) {
                            if (response.code() == 200) {
                                hideDialog();
                                CityData cityData = response.body();
                                if (cityData != null && cityData.getCity() != null && cityData.getCity().size() > 0) {
                                    List<String> cityArray = new ArrayList<>();
                                    cityArray.add("Select City");
                                    cityArray.addAll(cityData.getCity());
                                    cityListAdapter = new ArrayAdapter(SignUpActivity.this, android.R.layout.simple_spinner_item, cityArray);
                                    cityListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    citySpinner.setAdapter(cityListAdapter);
                                }
                            } else {
                                cancelDialog();
                            }
                        }

                        @Override
                        public void onFailure(Call<CityData> call, Throwable t) {
                            cancelDialog();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    if (cityListAdapter.getItem(i) != null) {
                        selectedCity = (String) cityListAdapter.getItem(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//        try {
//            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(otpMode == false){
                    if(oldTime == -1){
                        sendotp(SignUpActivity.this,true);
                    }else {
                        long currentTime = System.currentTimeMillis();
                        long minus = oldTime - currentTime;
                        int minutes = (int) ((minus / (1000*60)) % 60);
                        if(minutes >= 1) {
                            sendotp(SignUpActivity.this,true);
                        }else{
                            new ToastMsg(SignUpActivity.this).toastIconError("Please, wait 1 min to send again OTP request");
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(mlmuser == 1) {
            super.onBackPressed();
            finish();
        }else if (otp_number != -1){
            oldTime = -1;
            otp_number = -1;
            otpLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        }else{
            super.onBackPressed();
        }
    }

    private void sendotp(OtpCallback otpCallback, boolean otpbytext){
        String phone = etphone.getText().toString();
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SignUpApi signUpApi = retrofit.create(SignUpApi.class);
        Call<OTPResult> call = signUpApi.sendOTP(Config.API_KEY, phone);
        call.enqueue(new Callback<OTPResult>() {
            @Override
            public void onResponse(Call<OTPResult> call, Response<OTPResult> response) {
                if (response.body() != null) {
                    hideDialog();
                    OTPResult result = response.body();
                    //Log.e("otp_number", "onResponse: "+result.getOtp() );
                    if (result.getSuccess()) {
                        otpMode = false;
                        oldTime = System.currentTimeMillis();
                        otp_number = result.getOtp();
                        if(otpbytext){
                            new ToastMsg(SignUpActivity.this).toastIconSuccess("OTP request sent successfully");
                        }else{
                            otpCallback.success();
                        }
                    } else {
                        new ToastMsg(SignUpActivity.this).toastIconError("Failed to send OTP on your number");
                    }
                } else {
                    hideDialog();
                    new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong.");
                }
            }

            @Override
            public void onFailure(Call<OTPResult> call, Throwable t) {
                hideDialog();
                new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong.");
            }
        });
    }

    private void registerAccount(String email, String pass, String name, String lname, String phone, String ref, int mlmuser) {
        dialog.show();
        Retrofit retrofitMLMInstance = RetrofitClient.getRetrofitMLMInstance();
        SignUpApi signUpApi = retrofitMLMInstance.create(SignUpApi.class);
        Call<MlmLogin> loginCall = signUpApi.signUpMlm(Config.MLM_AUTH, new Register(phone, pass, name, lname, email, ref, selectedState, "0", Config.PARTER_ID, selectedCity));
        loginCall.enqueue(new Callback<MlmLogin>() {
            @Override
            public void onResponse(Call<MlmLogin> callx, Response<MlmLogin> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        MlmLogin mlmLogin = response.body();
                        if (mlmLogin.getStatus().equalsIgnoreCase("success")) {
                            pingponRegisterAccount(email, pass, name, lname, phone, ref, mlmuser);
                        } else {
                            hideDialog();
                            if (response.body().getRemarks().contains("Password")) {
                                new ToastMsg(SignUpActivity.this).toastIconError("Password must contain letters,numbers and special characters");
                            } else {
                                new ToastMsg(SignUpActivity.this).toastIconError(response.body().getRemarks());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MlmLogin> call, Throwable t) {
                cancelDialog();
                new ToastMsg(SignUpActivity.this).toastIconError("Something Wents wrong.");
            }
        });

    }

    private void pingponRegisterAccount(String email, String pass, String name, String lname, String phone, String ref, int mlmuser){
        if(mlmuser == 1){
            dialog.show();
        }
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SignUpApi signUpApi = retrofit.create(SignUpApi.class);
        Call<User> call = signUpApi.signUp(Config.API_KEY, email, pass, name + " " + lname, phone, mlmuser, selectedCity, selectedState, IMEI_NO, ref);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        User user = response.body();
                        if (user.getStatus().equals("success")) {
                            if(mlmuser == 1){
                                amountCreditApi(user, ref, 5, Config.SECURE_KEY);
                            }else{
                                if (!TextUtils.isEmpty(ref)) {
                                    amountCreditApi(user, ref, 5, Config.SECURE_KEY);
                                }else{
                                    PreferenceUtils.setReferID(SignUpActivity.this, "");
                                    hideDialog();
                                    new ToastMsg(SignUpActivity.this).toastIconSuccess("Successfully registered");
                                    // save user info to sharedPref
                                    saveUserInfo(user, user.getName(), etEmail.getText().toString(),
                                            user.getUserId());
                                }
                            }
                        } else if (user.getStatus().equals("error")) {
                            cancelDialog();
                            new ToastMsg(SignUpActivity.this).toastIconError(user.getData());
                        }
                    } else {
                        cancelDialog();
                        new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong.");
                    }
                } else {
                    cancelDialog();
                    new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                cancelDialog();
                new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong." + t.getMessage());
            }
        });
    }

    private void amountCreditApi(User user, String phone, int amount, String secureKey) {
        Retrofit retrofitMLMInstance = RetrofitClient.getRetrofitMLMInstance();
        SignUpApi signUpApi = retrofitMLMInstance.create(SignUpApi.class);
        Call<MlmLogin> amountCall = signUpApi.creditAmount(Config.MLM_AUTH, new AmountCredit(phone, amount, secureKey));
        amountCall.enqueue(new Callback<MlmLogin>() {
            @Override
            public void onResponse(Call<MlmLogin> callx, Response<MlmLogin> response) {
                if (response.body() != null) {
                    //Log.e("Credit", "onResponse: " + response.body().getRemarks());
                    PreferenceUtils.setReferID(SignUpActivity.this, "");
                    hideDialog();
                    new ToastMsg(SignUpActivity.this).toastIconSuccess("Successfully registered");
                    // save user info to sharedPref
                    saveUserInfo(user, user.getName(), etEmail.getText().toString(),
                            user.getUserId());
                }
            }

            @Override
            public void onFailure(Call<MlmLogin> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void saveUserInfo(User user, String name, String email, String id) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("id", id);
        editor.putBoolean("status", true);
        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
        editor.apply();

        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
        db.deleteUserData();
        db.insertUserData(user);
        db.close();
        ApiResources.USER_PHONE = user.getPhone();

        HashMap<String, String> data = new HashMap<>();
        data.put("id", id);
        DocumentReference ref = firebaseFirestore.collection("users")
                .document(id);
        ref.get()
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null && task.getResult().exists()){
                            data.put("newflag", deviceId);
                            ref.set(data, SetOptions.merge())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                        }else {
                            data.put("flag", deviceId);
                            ref.set(data)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            if (dialog.isShowing() && !isFinishing()) {
                dialog.dismiss();
            }
        }
        dialog = null;
        super.onDestroy();
    }

    public void cancelDialog() {
        if (dialog != null) {
            dialog.cancel();
        }
    }

    public void hideDialog() {
        if (dialog != null) {
            if (!isFinishing()) {
                dialog.dismiss();
            }
        }
    }

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
//                    new ToastMsg(SignUpActivity.this).toastIconError("Please, grant permission");
//                }
//                break;
//        }
//    }

    @Override
    public void success() {
        registerLayout.setVisibility(View.GONE);
        otpLayout.setVisibility(View.VISIBLE);
    }



}
