package com.mtma.insta.downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.SavedPostObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.protocol.HTTP;


public class CollectionUtil {

    private static final String LOG_TAG = CollectionUtil.class.getSimpleName();
    private static final String SAVED_POSTS_UR = "https://i.instagram.com/api/v1/feed/saved";


    public static ArrayList<SavedPostObject> getSavedPostsData(Context context) {

        // get the cookie from the preference that will be use to connecting to the instagram api.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = pref.getString(Constants.KEY_COOKIE,null);


        // create a list that will contains all SavedPostObject for each saved post.
        ArrayList<SavedPostObject> savedPostObjectArrayList = new ArrayList<>();

        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(SAVED_POSTS_UR).openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            // necessary provide user-agent.
            httpConnection.setRequestProperty(HTTP.USER_AGENT, Constants.CONNECTION_USER_AGENT);
            httpConnection.setRequestProperty(SM.COOKIE, cookie);



            // if the connection response code not equal 200 return from method early.
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }



            // get the response data as JSON object that we can work on to extract data from it.
            String responseResult = buildResultString(httpConnection);

            try {

                // get the root object for the json response from the server.
                JSONObject rootJSONObject = new JSONObject(responseResult);


                // in case the user don't have any saved posts the value for "num_results" node will
                // be equal 0 and no need to complete the method (just return an ArrayList with size 0).
                int itemsNumber = rootJSONObject.getInt("num_results");
                if (itemsNumber == 0) {
                    return savedPostObjectArrayList;
                }


                // access to each item for get each saved post data.
                JSONArray itemsArray = rootJSONObject.getJSONArray("items");
                for (int i = 0; i < itemsArray.length(); i++) {


                    // create a new DownloadingObject for fill it with required data about media.
                    SavedPostObject savedPostObject = new SavedPostObject();


                    // access to the "media" node which contains the data we want.
                    JSONObject itemObject = itemsArray.getJSONObject(i);
                    JSONObject mediaObject = itemObject.getJSONObject("media");


                    // set the media unique code.
                    String mediaCode = mediaObject.getString("code");
                    savedPostObject.setMediaCode(mediaCode);


                    // set the media product type (Post - Reel - Igtv).
                    String productType = mediaObject.getString("product_type");
                    savedPostObject.setProductType(getProductType(productType));



                    // set the media type (image - video - multiple).
                    if (productType.equals("carousel_container")) {
                        savedPostObject.setMediaType(MediaEntry.MEDIA_TYPE_MULTIPLE);
                    } else if (mediaObject.optJSONArray("video_versions") != null) {
                        savedPostObject.setMediaType(MediaEntry.MEDIA_TYPE_VIDEO);

                    } else {
                        savedPostObject.setMediaType(MediaEntry.MEDIA_TYPE_IMAGE);
                    }


                    // get the media url as image (even for videos) to display it as thumbnails.
                    JSONObject imageVersionsObject = mediaObject.getJSONObject("image_versions2");
                    JSONArray candidatesArray = imageVersionsObject.getJSONArray("candidates");
                    JSONObject firstObjectInCandidatesArray = candidatesArray.getJSONObject(0);
                    String thumbnailUrl = firstObjectInCandidatesArray.getString("url");
                    savedPostObject.setThumbnailUrl(thumbnailUrl);



                    // for the user data (username and his profile picture).
                    JSONObject userObject = mediaObject.getJSONObject("user");

                    // for the username.
                    String userName = userObject.getString("username");
                    savedPostObject.setUserName(userName);


                    // for the profile picture url.
                    String profilePicUrl = userObject.getString("profile_pic_url");
                    savedPostObject.setProfilePicUrl(profilePicUrl);



                    // add the DownloadingObject to the Arraylist after fill it with the required data.
                    savedPostObjectArrayList.add(savedPostObject);


                }


            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block number (1) inside " + LOG_TAG + " class in getSavedPostsData method.", e);
            }


        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block number (2) inside " + LOG_TAG + " class in getSavedPostsData method.", e);
        }


        // return ArrayList of DownloadingObjects after get all required data for each saved posts.
        return savedPostObjectArrayList;

    }


    private static int getProductType(String productTypeFromResponse) {

        if (productTypeFromResponse.equals("clips")) {
            return MediaEntry.MEDIA_PRODUCT_TYPE_REEL;
        } else if (productTypeFromResponse.equals("igtv")) {
            return MediaEntry.MEDIA_PRODUCT_TYPE_IGTV;
        } else {
            return MediaEntry.MEDIA_PRODUCT_TYPE_POST;
        }

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
