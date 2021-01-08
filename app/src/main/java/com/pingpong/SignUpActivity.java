package com.pingpong;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pingpong.database.DatabaseHelper;
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
import com.pingpong.network.model.User;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.Constants;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName,etEmail,etPass,etcPass,etLName,etphone,etreferral,otpEditText;
    private Button btnSignup;
    private ProgressDialog dialog;
    private View backgorundView;
    private Spinner citySpinner, stateSpinner;
    private String selectedState = "", selectedCity ="";
    private ArrayAdapter cityListAdapter;
    private LinearLayout otpLayout,registerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //filterMap = CityHelper.getCityMap();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etName=findViewById(R.id.name);
        etEmail=findViewById(R.id.email);
        etPass=findViewById(R.id.password);
        etcPass = findViewById(R.id.cpassword);
        etLName = findViewById(R.id.lname);
        btnSignup=findViewById(R.id.signup);
        etphone = findViewById(R.id.phone);
        citySpinner = findViewById(R.id.city_spinner);
        stateSpinner = findViewById(R.id.state_spinner);
        etreferral = findViewById(R.id.referral);
        otpLayout = findViewById(R.id.otpLayout);
        registerLayout = findViewById(R.id.registerLayout);
        otpEditText = findViewById(R.id.otpEditText);

        registerLayout.setVisibility(View.VISIBLE);
        otpLayout.setVisibility(View.GONE);

        //backgorundView=findViewById(R.id.background_view);
//        if (isDark) {
//            backgorundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
//            btnSignup.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
//        }
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String pass = etPass.getText().toString();
                String name = etName.getText().toString();
                String lname = etLName.getText().toString();
                String cpass = etcPass.getText().toString();
                String phone = etphone.getText().toString();
                String ref = etreferral.getText().toString();
                String city = selectedCity;
                String state = selectedState;
                if (!isValidEmailAddress(email)){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter valid email");
                }else if(pass.toString().equals("")){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter password");
                }else if(cpass.equals("")){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter confirm password");
                }else if (name.equals("")){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter name");
                }else if (lname.equals("")){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter last name");
                }else if (phone.equalsIgnoreCase("")){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please enter mobile number");
                }
//                else if (city.equalsIgnoreCase("")){
//                    new ToastMsg(SignUpActivity.this).toastIconError("Please select city");
//                }else if (state.equalsIgnoreCase("")){
//                    new ToastMsg(SignUpActivity.this).toastIconError("Please select State");
//                }
                else if (pass.length() < 6){
                    new ToastMsg(SignUpActivity.this).toastIconError("Password is short");
                }else if (cpass.length() < 6){
                    new ToastMsg(SignUpActivity.this).toastIconError("Confirm Password is short");
                }else if (!pass.equals(cpass)){
                    new ToastMsg(SignUpActivity.this).toastIconError("Please correct confirm password");
                }else {
                    signUp(email, pass, name,lname,phone,ref);
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
                if(i > 0) {
                    selectedState = (String) stateList.getItem(i);
                    dialog.show();
                    Call<CityData> cityDataCall = cityApi.getCityList(selectedState);
                    cityDataCall.enqueue(new Callback<CityData>() {
                        @Override
                        public void onResponse(Call<CityData> call, Response<CityData> response) {
                            if (response.code() == 200){
                                hideDialog();
                                CityData cityData = response.body();
                                if(cityData != null && cityData.getCity() != null && cityData.getCity().size() > 0){
                                    List<String> cityArray = new ArrayList<>();
                                    cityArray.add("Select City");
                                    cityArray.addAll(cityData.getCity());
                                    cityListAdapter = new ArrayAdapter(SignUpActivity.this, android.R.layout.simple_spinner_item, cityArray);
                                    cityListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    citySpinner.setAdapter(cityListAdapter);
                                }
                            }else{
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
                if(i > 0){
                    if(cityListAdapter.getItem(i) !=null) {
                        selectedCity = (String) cityListAdapter.getItem(i);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        String refer = getIntent().getStringExtra("referral");
        if(!TextUtils.isEmpty(refer)){
            etreferral.setText(refer);
            etreferral.setEnabled(false);
            etreferral.setFocusableInTouchMode(false);
            etreferral.setFocusable(false);
        }

        String phone = getIntent().getStringExtra("phone");
        if(!TextUtils.isEmpty(phone)){
            etphone.setText(phone);
        }
    }

    private void signUp(String email, String pass, String name,String lname, String phone, String ref){
        dialog.show();
        Retrofit retrofitMLMInstance = RetrofitClient.getRetrofitMLMInstance();
        SignUpApi signUpApi = retrofitMLMInstance.create(SignUpApi.class);
        Call<MlmLogin> loginCall = signUpApi.signUpMlm(Config.MLM_AUTH, new Register(phone,pass,name, lname,email,ref,selectedState,"0",Config.PARTER_ID, selectedCity));
        loginCall.enqueue(new Callback<MlmLogin>() {
            @Override
            public void onResponse(Call<MlmLogin> callx, Response<MlmLogin> response) {
                if (response.code() == 200) {
                    if(response.body() != null){
                        MlmLogin mlmLogin = response.body();
                        if (mlmLogin.getStatus().equalsIgnoreCase("success")) {
                            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
                            SignUpApi signUpApi = retrofit.create(SignUpApi.class);
                            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                            Call<User> call = signUpApi.signUp(Config.API_KEY, email, pass, name+" "+lname,phone,0,selectedCity, selectedState,deviceId);
                            call.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    if(response.code() == 200){
                                        if(response.body() != null){
                                            User user = response.body();
                                            if (user.getStatus().equals("success")) {
                                                if(!TextUtils.isEmpty(ref)){
                                                    Call<DeviceIdResult> objectCall = signUpApi.checkUserDevice(Config.API_KEY,deviceId);
                                                    objectCall.enqueue(new Callback<DeviceIdResult>() {
                                                        @Override
                                                        public void onResponse(Call<DeviceIdResult> call, Response<DeviceIdResult> response) {
                                                            if(response.body() != null){
                                                                DeviceIdResult object = response.body();
                                                                if(object.getStatus()){
                                                                    //ignored
                                                                }else{
                                                                    amountCredit(ref,5,Config.SECURE_KEY);
                                                                }
                                                                PreferenceUtils.setReferID(SignUpActivity.this, "");
                                                                hideDialog();
                                                                new ToastMsg(SignUpActivity.this).toastIconSuccess("Successfully registered");
                                                                // save user info to sharedPref
                                                                saveUserInfo(user, user.getName(), etEmail.getText().toString(),
                                                                        user.getUserId());
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<DeviceIdResult> call, Throwable t) {

                                                        }
                                                    });
                                                }else{
                                                    PreferenceUtils.setReferID(SignUpActivity.this, "");
                                                    hideDialog();
                                                    new ToastMsg(SignUpActivity.this).toastIconSuccess("Successfully registered");
                                                    // save user info to sharedPref
                                                    saveUserInfo(user, user.getName(), etEmail.getText().toString(),
                                                            user.getUserId());
                                                }
                                            } else if (user.getStatus().equals("error")){
                                                cancelDialog();
                                                new ToastMsg(SignUpActivity.this).toastIconError(user.getData());
                                            }
                                        }else {
                                            cancelDialog();
                                            new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong.");
                                        }
                                    }else{
                                        cancelDialog();
                                        new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong.");
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    cancelDialog();
                                    new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong."+ t.getMessage());
                                }
                            });
                        }else {
                            hideDialog();
                            if(response.body().getRemarks().contains("Password")){
                                new ToastMsg(SignUpActivity.this).toastIconError("Password must contain letters,numbers and special characters");
                            }else{
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

    private void sendOTP(String mobile){

    }

    private void amountCredit(String phone, int amount, String secureKey){
        Retrofit retrofitMLMInstance = RetrofitClient.getRetrofitMLMInstance();
        SignUpApi signUpApi = retrofitMLMInstance.create(SignUpApi.class);
        Call<MlmLogin> amountCall = signUpApi.creditAmount(Config.MLM_AUTH, new AmountCredit(phone,amount,secureKey));
        amountCall.enqueue(new Callback<MlmLogin>() {
            @Override
            public void onResponse(Call<MlmLogin> callx, Response<MlmLogin> response) {
                if(response.body() != null){
                    Log.e("Credit", "onResponse: "+response.body().getRemarks());
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
                finish();
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
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("id", id);
        editor.putBoolean("status",true);
        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
        editor.apply();

        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
        db.deleteUserData();
        db.insertUserData(user);
        db.close();
        ApiResources.USER_PHONE = user.getPhone();
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        //save user login time, expire time
        //updateSubscriptionStatus(user.getUserId());

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
            dialog.cancel();
        }
    }

    public void hideDialog(){
        if(dialog != null){
            if(!isFinishing()){
                dialog.dismiss();
            }
        }
    }
}
