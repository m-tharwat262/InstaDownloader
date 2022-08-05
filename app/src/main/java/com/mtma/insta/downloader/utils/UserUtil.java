package com.mtma.insta.downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.models.AppUserObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.protocol.HTTP;


public class UserUtil {


    private static final String LOG_TAG = UserUtil.class.getSimpleName();



    /**
     * Get the User data from Instagram server in background and display his data (user name, real
     * name and profile picture)
     *
     * @param context the current context.
     *
     * @return AppUserObject Object contains the user data (user name, real name and profile pic url).
     */
    public static AppUserObject getUserDataByUserId(Context context) {

        // get the userid and cookie from the preference that will be use to connecting to the instagram api.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = pref.getString(Constants.KEY_USER_ID, null);
        String cookie = pref.getString(Constants.KEY_COOKIE,null);



        if (userId == null) {
            return null;
        }


        // initialize a AppUserObject that will use to store the user data.
        AppUserObject appUserObject = null;

        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL("https://i.instagram.com/api/v1/users/" + userId + "/info/").openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            httpConnection.setRequestProperty(HTTP.USER_AGENT, Constants.CONNECTION_USER_AGENT);
            httpConnection.setRequestProperty(SM.COOKIE, cookie);



            // if the connection response code not equal 200 return from method early.
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }



            try {

                // get the response data as JSON object that we can work on to extract data from it.
                String result = buildResultString(httpConnection);


                // access to the "user" node that contains the data we want to get.
                JSONObject baseJsonResponse = new JSONObject(result);
                JSONObject userJasonObject = baseJsonResponse.getJSONObject("user");


                // add the username, user real name and his profile picture.
                String userName = userJasonObject.getString("username");
                String userRealName = userJasonObject.getString("full_name");
                String profilePicUrl = userJasonObject.getString("profile_pic_url");


                // initialize the AppUserObject with the data above.
                appUserObject = new AppUserObject(userName, userRealName, profilePicUrl);

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Exception from try-catch block number (1) inside " + LOG_TAG + " class in getUserDataByUserId method.", e);
            }


        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block number (1) inside " + LOG_TAG + " class in getUserDataByUserId method.", e);

        }

        return appUserObject;

    }


    /**
     * Get the result form the connection as String which can use as a json object to get
     * our data from it.
     */
    public static String buildResultString(HttpURLConnection httpConnection) throws Exception {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));

        if ("gzip".equals(httpConnection.getContentEncoding())) {
            bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(httpConnection.getInputStream())));
        }

        StringBuffer result = new StringBuffer();
        String str;

        while (true) {

            str = bufferedReader.readLine();
            if (str == null) {
                return result.toString();
            }
            result.append(str);

        }

    }


}
