package com.mtma.insta.downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.DownloadingObject;
import com.mtma.insta.downloader.models.UserHasStoryObject;

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


public class StoryUtil {


    private static final String LOG_TAG = StoryUtil.class.getSimpleName();




    private static final String USER_HAS_STORIES_URL = "https://i.instagram.com/api/v1/feed/reels_tray/";


    /**
     * Get the list of users who has stories
     *
     * @param context the activity context.
     *
     * @return ArrayList of UserHasStoryObject contains all the users who has stories.
     */
    public static ArrayList<UserHasStoryObject> getListOfUsersHasStories(Context context){

        // get the cookie from the preference that will be use to connecting to the instagram api.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = pref.getString(Constants.KEY_COOKIE,null);

        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(USER_HAS_STORIES_URL).openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            httpConnection.setRequestProperty(HTTP.USER_AGENT, Constants.CONNECTION_USER_AGENT);
            httpConnection.setRequestProperty(SM.COOKIE, cookie);



            // if the connection response code not equal 200 return from method early.
            int responseCode = httpConnection.getResponseCode();
            Log.i(LOG_TAG, "  UNIX  responseCode  :  " + responseCode);
            if (responseCode != 200) {
                return null;
            }


            // if the result from connection empty or not contains node "tray" return from method early.
            String result = buildResultString(httpConnection);
            Log.i(LOG_TAG, "  UNIX  result  :  " + result);


            // start get required data from response string.
            ArrayList<UserHasStoryObject> usersHasStoriesList = new ArrayList<>();
            try {

                // get the response data as JSON object that we can work on to extract data from it.
                JSONObject rootJSONObject = new JSONObject(result);
                JSONArray trayJSONArray = rootJSONObject.getJSONArray("tray");

                // each item inside array for a single user so we collect required data from each
                // user and store it at the end inside an ArrayList.
                for (int i = 0; i < trayJSONArray.length(); i++) {

                    // access to "user" node which contains user data we want.
                    JSONObject userObj = trayJSONArray.getJSONObject(i).getJSONObject("user");

                    // save the user data inside UserHasStoryObject object.
                    UserHasStoryObject object = new UserHasStoryObject();
                    object.setProfilePictureUrl(userObj.get("profile_pic_url").toString());
                    object.setRealName(userObj.get("full_name").toString());
                    object.setUserName(userObj.get("username").toString());
                    object.setUserId(userObj.get("pk").toString());

                    // store UserHasStoryObject object inside the ArrayList.
                    usersHasStoriesList.add(object);

                }

                // return an ArrayList contains
                return usersHasStoriesList;

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block number (1) inside " + LOG_TAG + " class in getListOfUsersHasStories method.", e);
            }



        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block number (2) inside " + LOG_TAG + " class in getListOfUsersHasStories method.", e);
        }

        return null;

    }









    public static ArrayList<DownloadingObject> getUserStoriesData(String userId, Context context) {


        // get the cookie from the preference that will be use to connecting to the instagram api.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = pref.getString(Constants.KEY_COOKIE,null);

        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL("https://i.instagram.com/api/v1/feed/user/" + userId + "/reel_media/").openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            httpConnection.setRequestProperty(HTTP.USER_AGENT, Constants.CONNECTION_USER_AGENT);
            httpConnection.setRequestProperty(SM.COOKIE, cookie);



            // if the connection response code not equal 200 return from method early.
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }


            // get the response data as JSON object that we can work on to extract data from it.
            String result = buildResultString(httpConnection);


            // try to get story/ies required data and collected them inside a DownloadingObject.
            try {


                // get the response data as JSON object that we can work on to extract data from it.
                JSONObject rootJSONObject = new JSONObject(result);


                // access to the "user" node to get the user data required.
                JSONObject userJSONObject = rootJSONObject.getJSONObject("user");


                // get the username and his profile picture.
                String userName = userJSONObject.getString("username");
                String profilePicUrl = userJSONObject.getString("profile_pic_url");



                // create a list that will contains all DownloadingObject for each story.
                ArrayList<DownloadingObject> downloadingObjectArrayList = new ArrayList<>();


                // access to the "items" node to get each story data.
                JSONArray itemsJSONArray = rootJSONObject.getJSONArray("items");
                for (int i = 0; i < itemsJSONArray.length(); i++) {


                    // create the DownloadingObject that will be used to store
                    DownloadingObject downloadingObject = new DownloadingObject();

                    // add the username and the profile picture we get before above the loop
                    // to the DownloadingObject.
                    downloadingObject.setUserName(userName);
                    downloadingObject.setProfilePicUrl(profilePicUrl);


                    // the media product type here is always a Story type.
                    downloadingObject.setProductType(MediaEntry.MEDIA_PRODUCT_TYPE_STORY);


                    // get the current object inside the JSONArray in the loop.
                    JSONObject currentItemObject = itemsJSONArray.getJSONObject(i);


                    // add the media unique code.
                    String pkFromItem = currentItemObject.get("pk").toString();
                    downloadingObject.setMediaCode(pkFromItem);



                    // check the media type (image or video) and add it to the object.
                    JSONArray videoJSONArray = currentItemObject.optJSONArray("video_versions");
                    String mediaUrl;
                    if (videoJSONArray != null) {

                        downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_VIDEO);

                        JSONObject firstItemInVideoArray = videoJSONArray.getJSONObject(0);
                        mediaUrl = firstItemInVideoArray.getString("url");

                    } else {

                        downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_IMAGE);

                        JSONObject imageVersion2Object = currentItemObject.getJSONObject("image_versions2");
                        JSONArray candidatesArray = imageVersion2Object.getJSONArray("candidates");
                        JSONObject firstItemInCandidatesArray = candidatesArray.getJSONObject(0);
                        mediaUrl = firstItemInCandidatesArray.getString("url");

                    }


                    // add the media Url to the object.
                    ArrayList<String> allMediaUrls = new ArrayList<>();
                    allMediaUrls.add(mediaUrl);
                    downloadingObject.setAllMediaUrls(allMediaUrls);




                    // try to get hashtags that put with the story if it is exist.
                    // Hint: there is no text can get from story.
                    JSONArray storyHashtagsArray = currentItemObject.optJSONArray("story_hashtags");
                    if (storyHashtagsArray != null) {

                        StringBuilder allHashtags = new StringBuilder();
                        for (int j = 0 ; i < storyHashtagsArray.length() ; i++) {
                            JSONObject hashtagItem = storyHashtagsArray.getJSONObject(j);
                            JSONObject hashtagObjectInsideHashtagItem = hashtagItem.getJSONObject("hashtag");
                            String hashtag = hashtagObjectInsideHashtagItem.getString("name");
                            allHashtags.append("#").append(hashtag).append(" ");
                        }

                        // add all hashtags to the object.
                        downloadingObject.setTextAndHashtags(allHashtags.toString());

                    }


                    downloadingObjectArrayList.add(downloadingObject);

                }


                // add the DownloadingObject to the Arraylist after fill it with the required data.
                return downloadingObjectArrayList;


            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception from try-catch block number (1) inside " + LOG_TAG + " class in getUserStoriesData method.", e);
            }


        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block number (2) inside " + LOG_TAG + " class in getUserStoriesData method.", e);
        }


        // in case there something went wrong above the method will return null value.
        return null;

    }



    /**
     * Get the result form the connection as String which can use as a json object to get
     * our data from it.
     */
    private static String buildResultString(HttpURLConnection httpConnection) throws Exception {

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
