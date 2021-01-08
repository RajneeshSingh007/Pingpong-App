package com.pingpong.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.pingpong.BuildConfig;
import com.pingpong.R;

import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Tools {

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static void share(Context context, String body) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody =body +"\n\n"+ context.getString(R.string.share_body) + BuildConfig.APPLICATION_ID;
        String shareSub = "Share app";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    public static void shareLink(@NonNull Context context, Uri uri) {
        try {
            URL url = new URL(URLDecoder.decode(uri.toString(), "UTF-8"));
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "PingPong Referral");
            intent.putExtra(Intent.EXTRA_TEXT, url.toString());
            Intent shareIntent = Intent.createChooser(intent, "Refer Now");
            context.startActivity(shareIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String byteToMb(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }

    public static String milliToDate(long millisecond) {
        Date date = new Date(millisecond);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:ss a");
        return dateFormat.format(date);
    }

    public static long convertTimeToMill(String timer) {
        if(TextUtils.isEmpty(timer)){
            return 0;
        }
        String split[] = timer.split(":");
        if(split.length == 0){
            return 0;
        }
        int mintoSecond = 0;
        if(split[0].charAt(0) == '0'){
            mintoSecond = Integer.parseInt(String.valueOf(split[0].charAt(1)));
        }else {
            if(split[0].equalsIgnoreCase("00")){
                mintoSecond = 0;
            }else{
                mintoSecond = Integer.parseInt(split[0]) *60;
            }
        }
        if(split.length > 1){
            mintoSecond += Integer.parseInt(split[1]);
        }
        return mintoSecond*1000;
    }

    public static boolean isValidFormat(String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }


    public static String insertPeriodically(String text, String insert, int period) {
        StringBuilder builder = new StringBuilder(text.length() + insert.length() * (text.length() / period) + 1);
        int index = 0;
        String prefix = "";
        while (index < text.length()) {
            builder.append(prefix);
            prefix = insert;
            builder.append(text.substring(index, Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }

    public static String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hexString.toString();
    }

    /**
     *
     * @param currentDate
     * @param expiry
     */
    public static boolean compareDates(String currentDate,String expiry) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date date1 = sdf.parse(currentDate);
            Date date2 = sdf.parse(expiry);
            if(date1.after(date2)){
                return false;
            }else if(date1.before(date2)){
                return true;
            }else if(date1.equals(date2)){
                return true;
            }
        } catch(ParseException ex){
            ex.printStackTrace();
        }
        return false;
    }


    public static boolean checkStoragePermission(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }


    public static  boolean isValidNumber(@NonNull  String email){
        String numberPattern = "^[0-9]*$";
        return email.matches(numberPattern);
    }

    public static void launchVipsShopping(Activity activity){
        if(activity == null){
            return;
        }
        Intent launchVips = new Intent(Intent.ACTION_VIEW);
        launchVips.setPackage("com.vipswallet.shopping");
        if(launchVips.resolveActivity(activity.getPackageManager()) != null){
            activity.startActivity(launchVips);
        }else {
            Intent launchVipsPlaystore = new Intent(Intent.ACTION_VIEW);
            launchVipsPlaystore.setData(Uri.parse(activity.getString(R.string.wallet_link)));
            if(launchVipsPlaystore.resolveActivity(activity.getPackageManager()) != null){
                activity.startActivity(launchVipsPlaystore);
            }else{
                new ToastMsg(activity).toastIconError("No App found");
            }
        }
    }

    public static void launchVipsAff(Activity activity){
        if(activity == null){
            return;
        }
        Intent launchVips = new Intent(Intent.ACTION_VIEW);
        launchVips.setPackage("com.gab.android");
        if(launchVips.resolveActivity(activity.getPackageManager()) != null){
            activity.startActivity(launchVips);
        }else {
            Intent launchVipsPlaystore = new Intent(Intent.ACTION_VIEW);
            launchVipsPlaystore.setData(Uri.parse(activity.getString(R.string.affiliate_link)));
            if(launchVipsPlaystore.resolveActivity(activity.getPackageManager()) != null){
                activity.startActivity(launchVipsPlaystore);
            }else{
                new ToastMsg(activity).toastIconError("No App found");
            }
        }
    }
}
