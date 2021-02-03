package com.pingpong.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.onesignal.OneSignal;
import com.pingpong.BuildConfig;
import com.pingpong.Config;
import com.pingpong.GetStarted;
import com.pingpong.GlideApp;
import com.pingpong.MainActivity;
import com.pingpong.NotificationClickHandler;
import com.pingpong.R;
import com.pingpong.SplashScreenActivity;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.payumoney.AppEnvironment;
import com.scottyab.rootbeer.RootBeer;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyAppClass extends Application implements Application.ActivityLifecycleCallbacks {

    public static final String TAG = MyAppClass.class.getSimpleName();

    public static final String NOTIFICATION_CHANNEL_ID = "pinpong";
    private static Context mContext;
    private static Certificate certificate;
    private static Certificate vipsCertificate;
    private FirebaseFirestore firebaseFirestore;
    private AppEnvironment appEnvironment;

    @Override
    public void onCreate() {
        super.onCreate();
        if (AudienceNetworkAds.isInAdsProcess(this)) {
            return;
        }
        appEnvironment = AppEnvironment.PRODUCTION;
        // Load CAs from an InputStream
        CertificateFactory certificateFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = getResources().openRawResource(R.raw.pingpong);
            certificate = certificateFactory.generateCertificate(inputStream);
            inputStream.close();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CertificateFactory certificateFactory1 = null;
        try {
            certificateFactory1 = CertificateFactory.getInstance("X.509");
            InputStream vipStream = getResources().openRawResource(R.raw.vipswallet);
            vipsCertificate = certificateFactory1.generateCertificate(vipStream);
            vipStream.close();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Picasso.setSingletonInstance(getCustomPicasso());

        mContext = this;
        createNotificationChannel();

        //OneSignal new setup
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationClickHandler(mContext))
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        SharedPreferences preferences = getSharedPreferences("push", MODE_PRIVATE);
        if (preferences.getBoolean("status", true)) {
            OneSignal.setSubscription(true);
        } else {
            OneSignal.setSubscription(false);
        }

        //

        if (!getFirstTimeOpenStatus()) {
            //if (Config.DEFAULT_DARK_THEME_ENABLE) {
                changeSystemDarkMode(true);
            //} else {
                //changeSystemDarkMode(false);
            //}
            saveFirstTimeOpenStatus(true);
        }

        // fetched and save the user active status if user is logged in
        String userId = PreferenceUtils.getUserId(this);
        if (userId != null && !userId.equals("")) {
            updateActiveStatus(userId);
        }
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
        FacebookSdk.sdkInitialize(this);
        FacebookSdk.setAutoLogAppEventsEnabled(true);
        AppEventsLogger.activateApp(this);
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        FacebookSdk.setApplicationId(BuildConfig.APPLICATION_ID);

        FirebaseFirestoreSettings.Builder firebaseFirestoreSettings = new FirebaseFirestoreSettings.Builder();
        firebaseFirestoreSettings.setPersistenceEnabled(false);
              //  .setTimestampsInSnapshotsEnabled(true);
        firebaseFirestore  = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(firebaseFirestoreSettings.build());

        registerActivityLifecycleCallbacks(this);

        clearCache();
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    private Picasso getCustomPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        //set 12% of available app memory for image cachecc
        builder.memoryCache(new LruCache(getBytesForMemCache(12)));
        //set request transformer
        Picasso.RequestTransformer requestTransformer =  new Picasso.RequestTransformer() {
            @Override
            public Request transformRequest(Request request) {
                //Log.d("image request", request.toString());
                return request;
            }
        };
        builder.requestTransformer(requestTransformer);

        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri,
                                          Exception exception) {
                //Log.d("image load error", uri.getPath());
            }
        });

        return builder.build();
    }

    private int getBytesForMemCache(int percent) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)
                getSystemService(ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.getMemoryInfo(mi);
        }

        double availableMemory= mi.availMem;

        return (int)(percent*availableMemory/100);
    }


    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NotificationName",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }


    public void changeSystemDarkMode(boolean dark) {
        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("dark", dark);
        editor.apply();
    }

    public void saveFirstTimeOpenStatus(boolean dark) {
        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("firstTimeOpen", true);
        editor.apply();

    }

    public boolean getFirstTimeOpenStatus() {
        SharedPreferences preferences = getSharedPreferences("push", MODE_PRIVATE);
        return preferences.getBoolean("firstTimeOpen", false);
    }

    public static Context getContext() {
        return mContext;
    }

    private void updateActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    db.close();
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    public static Certificate getCertificate() {
        return certificate;
    }

    public static Certificate getVipsCertificate() {
        return vipsCertificate;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        //enable only during production
        //rootCheck(activity);
        showAlert(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * logout from device
     * @param activity
     */
    private void showAlert(Activity activity){
        String userId = PreferenceUtils.getUserId(getContext());
        if(!TextUtils.isEmpty(userId)){
            DocumentReference ref = firebaseFirestore.collection("users")
                    .document(userId);
            ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        String newFlag = (String) documentSnapshot.get("newflag");
                        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        if(!TextUtils.isEmpty(newFlag) && !newFlag.equalsIgnoreCase(deviceId)){
                            if(!activity.isDestroyed() && !activity.isFinishing()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(activity).setMessage("You're Logout from this session")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                        if (user != null){
                                                            FirebaseAuth.getInstance().signOut();
                                                        }
                                                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                                                        editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
                                                        editor.apply();
                                                        editor.commit();
                                                        PreferenceUtils.deleteAllData(activity,true,true);
                                                        if(dialog != null){
                                                            dialog.dismiss();
                                                        }
                                                        Intent intent = new Intent(activity, GetStarted.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    }
                                                }).create().show();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    private void rootCheck(Activity activity){
        RootBeer rootBeer = new RootBeer(getContext());
        if (rootBeer.isRooted()) {
            if(!activity.isDestroyed() && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(activity).setMessage("Your device is rooted!")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user != null){
                                            FirebaseAuth.getInstance().signOut();
                                        }
                                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                                        editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
                                        editor.apply();
                                        editor.commit();
                                        PreferenceUtils.deleteAllData(activity,true,true);
                                        System.exit(0);
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    }
                });
            }
        }
    }

    private void clearCache(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                Glide.get(getApplicationContext()).clearDiskCache();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                GlideApp.get(getApplicationContext()).clearMemory();
            }
        }.execute();
    }
}
