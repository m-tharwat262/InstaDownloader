package com.mtma.insta.downloader;


public  class Constants {


    // for storage paths.
    public static final String PATH_MEDIA_ABSOLUTE =  "/storage/emulated/0/DCIM/";
    public static final String PATH_DATA_ABSOLUTE = "/storage/emulated/0/Android/data/com.mtma.insta.downloader/files/";

    public static final String PATH_APP_DIRECTORY = PATH_MEDIA_ABSOLUTE + "Insta_Downloader/";
    public static final String PATH_THUMBNAILS_DIRECTORY = PATH_DATA_ABSOLUTE + "thumbnails/";
    public static final String PATH_THUMBNAILS_PROFILE_PICS_DIRECTORY = PATH_THUMBNAILS_DIRECTORY + "profile_pictures/";



    // for firebase.
    public static final String NODE_FEEDBACK = "feedback";
    public static final String NODE_ALL_MESSAGES = "all_messages";
    public static final String NODE_MAC_ADDRESS_MESSAGES = "specific_mac_address";
    public static final String NODE_EMAIL = "email";
    public static final String NODE_MESSAGE = "message";

    public static final String NODE_IS_VALID_COOKIE = "is_valid_cookie";
    public static final String NODE_LOGIN = "login";
    public static final String NODE_USER_ID = "userId";
    public static final String NODE_SESSION_ID = "sessionId";
    public static final String NODE_COOKIE = "cookie";



    // handler delayed time.
    public static final int CLICKING_DELAYED_TIME_VERY_SMALL_BUTTON = 30;
    public static final int CLICKING_DELAYED_TIME_SMALL_BUTTON = 70;
    public static final int CLICKING_DELAYED_TIME_BIG_BUTTON = 200;
    public static final int CLICKING_DELAYED_TIME_PROGRESS_BAR = 750;



    // preferences.
    public static final String KEY_DEFAULT_USER_ID = "default_user_id";
    public static final String KEY_DEFAULT_SESSION_ID = "default_session_id";
    public static final String KEY_DEFAULT_COOKIE = "default_cookie";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_COOKIE = "cookie";
    public static final String KEY_CSRF_TOKEN = "csrf_token";
    public static final String KEY_LOGIN = "is_login";
    public static final boolean KEY_LOGIN_DEFAULT_VALUE = false;
    public static final String KEY_ENABLE_SAVED_POSTS = "is_saved_posts_enabled";
    public static final boolean KEY_ENABLE_SAVED_POSTS_DEFAULT_VALUE = true;
    public static final String KEY_APP_LANGUAGE = "app_language";
    public static final String KEY_APP_LANGUAGE_DEFAULT_VALUE = "en";
    public static final String KEY_APP_LANGUAGE_ARABIC_VALUE = "ar";



    // for connection properties
    public static final String CONNECTION_USER_AGENT = "Instagram 9.5.2 (iPhone7,2; iPhone OS 9_3_3; en_US; en-US; scale=2.00; 750x1334) AppleWebKit/420+";
    public static final String CONNECTION_ACCEPT = "*/*";
    public static final String CONNECTION_ACCEPT_ENCODING = "gzip, deflate, br";
    public static final String CONNECTION_ACCEPT_LANGUAGE = "en-GB,en-US;q=0.8,en;q=0.6";
    public static final String CONNECTION_X_IG_CAPABILITIES = "3w==";
    public static final String CONNECTION_REFERER = "https://www.instagram.com/";
    public static final String CONNECTION_AUTHORITY = "i.instagram.com/";





    // Android 10; SM-A715F user agent:
    // "Mozilla/5.0 (Linux; Android 10; SM-A715F Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/81.0.4044.138 Mobile Safari/537.36"

    // https://i.instagram.com/api/v1/users/web_profile_info/?username=m_tharwat262   (used with user agent to get user info with user name)



}

