package com.pingpong;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pingpong.R;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.LoginApi;
import com.pingpong.network.mlm.MlmLogin;
import com.pingpong.network.mlm.postmodel.Login;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.User;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.Constants;
import com.pingpong.utils.RtlUtils;
import com.pingpong.utils.ToastMsg;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GetStarted extends AppCompatActivity {
    private EditText etEmail;
    private Button btnLogin;
    private ProgressDialog dialog;
    private View backgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(GetStarted.this,R.drawable.main_bg));
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getstarted);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "getstarted_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etEmail=findViewById(R.id.email);
        btnLogin=findViewById(R.id.signin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                if(TextUtils.isEmpty(email)) {
                    new ToastMsg(GetStarted.this).toastIconError("Mobile number empty");
                }else if(email.length() < 10) {
                    new ToastMsg(GetStarted.this).toastIconError("Please, Enter Correct mobile number");
                }else {
                    //startRegister();
                    login(email);
                }
            }
        });

    }


    private void login(String email){
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<User> call = api.postLoginStatus(Config.API_KEY, email, "dummy", true);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        hideDialog();
                        startLogin(0, email);
                    } else {
                        Retrofit mlmRetorofit = RetrofitClient.getRetrofitMLMInstance();
                        LoginApi mlmapi = mlmRetorofit.create(LoginApi.class);
                        Call<MlmLogin> loginCall = mlmapi.postMlMLoginStatus(Config.MLM_AUTH, new Login(email, "", Config.PARTER_ID));
                        loginCall.enqueue(new Callback<MlmLogin>() {
                            @Override
                            public void onResponse(Call<MlmLogin> call, Response<MlmLogin> response) {
                                if (response.code() == 200) {
                                    hideDialog();
                                    MlmLogin mlmLogin = response.body();
                                    if (mlmLogin.getStatus().equalsIgnoreCase("success")) {
                                        startLogin(1, email);
                                    }else {
                                        startRegister();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MlmLogin> call, Throwable t) {
                                cancelDialog();
                                new ToastMsg(GetStarted.this).toastIconError(getString(R.string.error_toast));
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                cancelDialog();
                new ToastMsg(GetStarted.this).toastIconError(getString(R.string.error_toast));
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

    private void startLogin(int mode, String phone){
        Intent intent = new Intent(GetStarted.this, LoginActivity.class);
        intent.putExtra("isMlm", mode);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }

    private void startRegister(){
        Intent intent = new Intent(GetStarted.this, SignUpActivity.class);
        String email = etEmail.getText().toString();
        intent.putExtra("phone", email);
        startActivity(intent);
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

}
