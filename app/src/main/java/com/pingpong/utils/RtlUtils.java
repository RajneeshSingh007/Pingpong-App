package com.pingpong.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.pingpong.Config;

import java.util.Locale;

public class RtlUtils {

    public static void setScreenDirection(Context context){
        if(context == null){
            return;
        }
        Resources resources = context.getResources();
        if(resources != null){
            Configuration config = resources.getConfiguration();
            if(config != null){
                String language = config.locale.getLanguage();
                if (Config.ENABLE_RTL){
                    if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        config.setLayoutDirection(new Locale(language));
                    } else {
                        config.setLayoutDirection(new Locale("en"));
                    }
                }else {
                    //use ltr
                    config.setLayoutDirection(new Locale("en"));
                }
            }
        }
    }
}
