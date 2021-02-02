package com.pingpong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pingpong.Config;
import com.pingpong.DetailsActivity;
import com.pingpong.database.DatabaseHelper;
import com.pingpong.models.CommonModels;
import com.pingpong.network.RetrofitClient;
import com.pingpong.network.apis.SubscriptionApi;
import com.pingpong.network.model.ActiveStatus;
import com.pingpong.network.model.CommonModel;
import com.pingpong.network.model.User;
import com.pingpong.network.model.config.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class PreferenceUtils {
    public static final String TAG = "PreferenceUtils";


    public static boolean isActivePlan(Context context) {
        String status = getSubscriptionStatus(context);
        if (status != null) {
            return status.equals("active");
        } else {
            return false;
        }
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_LOGIN_STATUS, Context.MODE_PRIVATE);
        return preferences.getBoolean(Constants.USER_LOGIN_STATUS, false);
    }

    public static String getReferID(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_LOGIN_STATUS, Context.MODE_PRIVATE);
        return preferences.getString(Constants.REFERBY, "");
    }

    public static void setReferID(Context context, String referID) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_LOGIN_STATUS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.REFERBY,referID);
    }

    public static boolean isMandatoryLogin(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        boolean getMandatoryLogin = db.getConfigurationData().getAppConfig().getMandatoryLogin();
        db.close();
        return getMandatoryLogin;
    }

    public static String getSubscriptionStatus(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        String status = db.getActiveStatusData().getStatus();
        db.close();
        return status;
    }

    public static ActiveStatus getStatus(Context context){
        DatabaseHelper db = new DatabaseHelper(context);
        ActiveStatus activeStatus = db.getActiveStatusData();
        db.close();
        return activeStatus;
    }

    public static long getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String currentDateAndTime = sdf.format(new Date());

        Date date = null;
        try {
            date = sdf.parse(currentDateAndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.getTimeInMillis();
    }

    public static String getCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDateAndTime = sdf.format(new Date());
        return currentDateAndTime;
    }

    public static long getExpireTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String currentDateAndTime = sdf.format(new Date());

        Date date = null;
        try {
            date = sdf.parse(currentDateAndTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, 2);

        return calendar.getTimeInMillis();
    }

    public static long getparseDate(String dates) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        Date date = null;
        try {
            date = sdf.parse(dates);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, 2);

        return calendar.getTimeInMillis();
    }

    public static boolean isValid(Context context) {
        String savedTime = getUpdateTime(context);
        long currentTime = getCurrentTime();
        return Long.parseLong(savedTime) > currentTime;
    }

    public static boolean isValid(Context context, String savedTime) {
        long currentTime = getCurrentTime();
        return getparseDate(savedTime) > currentTime;
    }

    private static String getUpdateTime(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        String exp = String.valueOf(db.getActiveStatusData().getExpireTime());
        db.close();
        return exp;
    }

    public static void updateSubscriptionStatus(@NonNull Context context) {
        String userId = getUserId(context);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    if(activeStatus != null){
                        DatabaseHelper db = new DatabaseHelper(context);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);
                        db.close();
                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
               // t.printStackTrace();
            }
        });
    }

    public static void clearSubscriptionSavedData(Context context) {
        //now save to sharedPreference
        DatabaseHelper db = new DatabaseHelper(context);
        db.deleteAllActiveStatusData();
        db.close();
    }

    public static String getUserId(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        String userid = db.getUserData().getUserId();
        db.close();
        return userid;
    }

    public static User getUser(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        User user = db.getUserData();
        db.close();
        return user;
    }

    public static boolean isProgramGuideEnabled(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        boolean getProgramGuideEnable = databaseHelper.getConfigurationData().getAppConfig().getProgramGuideEnable();
        databaseHelper.close();
        return getProgramGuideEnable;
    }

    public static List<CommonModels> getRecentlyWatched(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        List<CommonModels> commonModelsList = db.getAllRecentWatched();
        db.close();
        return commonModelsList;
    }

    public static void insertRecent(Context context, String imagurl, String type, String id){
        CommonModels cm = new CommonModels();
        cm.setImageUrl(imagurl);
        cm.setVideoType(type);
        cm.setId(id);
        DatabaseHelper db = new DatabaseHelper(context);
        db.insertRecentWatched(cm);
        db.close();
    }

    public static Configuration getConfiguration(Context context){
        DatabaseHelper db = new DatabaseHelper(context);
        Configuration configuration = db.getConfigurationData();
        db.close();
        return configuration;
    }

    public static void deleteAllData(Context context, boolean recent, boolean subcri){
        DatabaseHelper db = new DatabaseHelper(context);
        db.deleteUserData();
        if(recent){
            db.deleteRecentData();
        }
        if(subcri){
            db.deleteAllActiveStatusData();
        }
        db.close();
    }

    public static String getPassword(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_LOGIN_STATUS, Context.MODE_PRIVATE);
        return preferences.getString(Constants.USER_PASSWORD_AVAILABLE, "");
    }

    public static void addWatchHistory(Context context, String vid, long duration, String type,String epsodeId, String imagurl, boolean enableRecent){
        //Log.e(TAG, "addWatchHistory: "+vid +" "+duration);
        DatabaseHelper db = new DatabaseHelper(context);
        CommonModels cm = new CommonModels();
        cm.setImageUrl(imagurl);
        cm.setVideoType(type);
        cm.setId(vid);
        if(enableRecent){
            db.insertRecentWatched(cm);
        }else{
            db.insertWatchedHistory(vid, duration,type,epsodeId);
        }
        db.close();
    }

    public static long getWatchedDuration(Context context, String vid, String type, String epsodeId){
        DatabaseHelper db = new DatabaseHelper(context);
        long dur = 0;
        dur = db.getWatchedDuration(Integer.parseInt(vid),type,epsodeId);
        db.close();
        return dur;
    }

}
