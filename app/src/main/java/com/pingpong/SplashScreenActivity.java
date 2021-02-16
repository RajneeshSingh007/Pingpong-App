package com.pingpong;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.pingpong.BuildConfig;
import com.pingpong.R;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.ConfigurationApi;
import com.pingpong.network.model.User;
import com.pingpong.network.model.config.ApkUpdateInfo;
import com.pingpong.network.model.config.AppConfig;
import com.pingpong.network.model.config.Configuration;
import com.pingpong.network.model.config.PaymentConfig;
import com.pingpong.utils.PreferenceUtils;
import com.pingpong.utils.ApiResources;
import com.pingpong.utils.Constants;
import com.pingpong.utils.ToastMsg;
import com.pingpong.utils.Tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private final int PERMISSION_REQUEST_CODE = 100;
    private int SPLASH_TIME = 3000;
    private Thread timer;
    private DatabaseHelper db;
    private ImageView gif;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(SplashScreenActivity.this,R.drawable.splash_bg));
        setContentView(R.layout.activity_splashscreen);
        db = new DatabaseHelper(SplashScreenActivity.this);
        gif = findViewById(R.id.gif);
        videoView = findViewById(R.id.video_app_intro);

        ColorDrawable drawable = new ColorDrawable();
        drawable.setColor(Color.TRANSPARENT);

        // checking storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Tools.checkStoragePermission(SplashScreenActivity.this)) {
                updateDb();
            }
        } else {
            updateDb();
        }
//        timer = new Thread() {
//            public void run() {
//                try {
//                    sleep(SPLASH_TIME);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    getConfigurationData();
//                }
//            }
//        };
//        try {
//            timer.start();
//        }catch (IllegalThreadStateException e){
//            //
//        }
//        if(!isDestroyed()){
//            GlideApp.with(SplashScreenActivity.this)
//                    .asGif()
//                    .load(R.raw.animation)
//                    .error(drawable)
//                    .placeholder(drawable)
//                    .into(gif);
//        }
        StringBuilder stringBuilder = new StringBuilder().append("android.resource://").append(getPackageName()).append("/").append(R.raw.splash);
        videoView.setVideoURI(Uri.parse(stringBuilder.toString()));
        videoView.start();
        videoView.setBackgroundColor(ContextCompat.getColor(this, R.color.splashbg));
        videoView.setZOrderOnTop(true);
//        float videoProportion = 1.5f;
//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        int screenHeight = getResources().getDisplayMetrics().heightPixels;
//        float screenProportion = (float) screenHeight / (float) screenWidth;
//        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();
//        if (videoProportion < screenProportion) {
//            lp.height= screenHeight;
//            lp.width = (int) ((float) screenHeight / videoProportion);
//        } else {
//            lp.width = screenWidth;
//            lp.height = (int) ((float) screenWidth * videoProportion);
//        }
//        videoView.setLayoutParams(lp);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                getConfigurationData();
                return false;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                getConfigurationData();
            }
        });
    }

    private void startApp(){
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(SplashScreenActivity.this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            //Log.e("Signup", "getDynamicLink:Success"+ deepLink.toString());
                            String parse = deepLink.toString();
                            if(!TextUtils.isEmpty(parse)){
                                Intent intent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
                                if(parse.contains("=")){
                                    String split[] = deepLink.toString().split("=");
                                    PreferenceUtils.setReferID(SplashScreenActivity.this, split[1]);
                                    intent.putExtra("referral", split[1]);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    getStarted();
                                }
                            }else{
                                getStarted();
                            }
                        }else{
                            getStarted();
                        }
                    }
                })
                .addOnFailureListener(SplashScreenActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getStarted();
                    }
                });
    }

    public void getConfigurationData() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ConfigurationApi api = retrofit.create(ConfigurationApi.class);
        Call<Configuration> call = api.getConfigurationData(Config.API_KEY);
        call.enqueue(new Callback<Configuration>() {
            @Override
            public void onResponse(Call<Configuration> call, Response<Configuration> response) {
                if (response.code() == 200) {
                    Configuration configuration = response.body();
                    if (configuration != null) {
                        //apk update check
                        if (isNeedUpdate(configuration.getApkUpdateInfo().getVersionCode())) {
                            showAppUpdateDialog(configuration.getApkUpdateInfo());
                        }else{
                            startApp();
                        }
                    }else{
                        startApp();
                    }
                }else{
                    startApp();
                }
            }

            @Override
            public void onFailure(Call<Configuration> call, Throwable t) {
                //Log.e("ConfigError", t.getLocalizedMessage());
                startApp();
            }
        });
    }

    private void updateDb(){
        Constants.genreList = Collections.emptyList();
        Constants.countryList = Collections.emptyList();
        Constants.tvCategoryList = Collections.emptyList();
        Configuration configuration = new Configuration();
        configuration.setCountry(Collections.emptyList());
        configuration.setGenre(Collections.emptyList());
        configuration.setTvCategory(Collections.emptyList());
        AppConfig appConfig = new AppConfig();
        appConfig.setMenu("vertical");
        appConfig.setGenreVisible(false);
        appConfig.setCountryVisible(false);
        appConfig.setProgramGuideEnable(false);
        appConfig.setMandatoryLogin(true);
        configuration.setAppConfig(appConfig);
        PaymentConfig config = new PaymentConfig();
        config.setCurrency("INR");
        config.setExchangeRate("73.58");
        config.setCurrencySymbol("₹");
        configuration.setPaymentConfig(config);
        db.deleteAllDownloadData();
        db.deleteAllAppConfig();
        db.insertConfigurationData(configuration);
        //db.deleteRecentData();
        //db.deleteWatchData();
        db.close();
    }

    private void showAppUpdateDialog(final ApkUpdateInfo info) {
        new AlertDialog.Builder(this)
                .setTitle("New version: " + info.getVersionName())
                .setMessage(info.getWhatsNew())
                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //update clicked
                        dialog.dismiss();
                        Tools.launchPingpongupdate(SplashScreenActivity.this);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private boolean isNeedUpdate(String versionCode) {
        return Integer.parseInt(versionCode) > BuildConfig.VERSION_CODE;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED  && grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //resume tasks needing this permission
            updateDb();
        }
    }

    public void getStarted(){
        if (PreferenceUtils.isLoggedIn(SplashScreenActivity.this)) {
            User user = PreferenceUtils.getUser(this);
            Intent intent;
            if(user != null && TextUtils.isEmpty(user.getUserId())){
                intent = new Intent(SplashScreenActivity.this, GetStarted.class);
                referSaved();
            }else{
                intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            referSaved();
            Intent intent = new Intent(SplashScreenActivity.this, GetStarted.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void  referSaved(){
        String referID = PreferenceUtils.getReferID(SplashScreenActivity.this);
        if(!TextUtils.isEmpty(referID)){
            Intent intent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
            intent.putExtra("referral", referID);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
