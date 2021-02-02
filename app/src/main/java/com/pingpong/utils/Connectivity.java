package com.pingpong.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class Connectivity {

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @param type
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    public static int isConnectedFast(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        if(info != null && info.isConnected()){
            return Connectivity.isConnectionFast(info.getType(),info.getSubtype());
        }else {
            return -1;
        }
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    public static int isConnectionFast(int type, int subType){
        if(type==ConnectivityManager.TYPE_WIFI){
            return -1;
        }else if(type==ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                    return 0;

                case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                    return 1;
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11  // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13 // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11 // ~ 10+ Mbps
                    return 2;
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8 // ~25 kbps
                    return 3;
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return -1;
            }
        }else{
            return -1;
        }
    }

}
