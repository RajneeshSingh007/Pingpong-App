package com.pingpong;

public class Config {

    //http://uat.vipswallet.com/api/VipsPartner/ //test
    //https://vipswallet.com/api/VipsPartner/ //prod
    public static final String BASE_URL = "https://pingpongentertainment.com/";
    public static final String MLM_BASE_URL = "https://vipswallet.com/api/VipsPartner/";

    //TestUser //test
    //Pingpong //pro
    public static final String PARTER_ID = "Pingpong";
    public static final String API_SERVER_URL = BASE_URL+ "rest-api/";
    public static final String API_KEY = "4b982cf54adca1b";

    //Basic VGVzdFVzZXI6VGVzdEAxMjM0 //test
    //Basic UGluZ3Bvbmc6MUAzNDVBYSM= //prod
    public static final String MLM_AUTH = "Basic UGluZ3Bvbmc6MUAzNDVBYSM=";

    public static final String TERMS_URL = BASE_URL+ "terms/";
    public static final String PASSOWRD = "1@345Aa#";
    public static final String SECURE_KEY = "jX8hIM36IcXdNHjwbu0QQ1bl9aeOXRkQ";

    public static final boolean ENABLE_DOWNLOAD_TO_ALL = true;
    public static boolean ENABLE_RTL = false;
    public static boolean YOUTUBE_VIDEO_AUTO_PLAY = false;
    public static final boolean ENABLE_EXTERNAL_PLAYER = false;
    public static boolean DEFAULT_DARK_THEME_ENABLE = true;
    public static final boolean ENABLE_FACEBOOK_LOGIN = false;
    public static final boolean ENABLE_PHONE_LOGIN = false;
    public static final boolean ENABLE_GOOGLE_LOGIN = false;
}
