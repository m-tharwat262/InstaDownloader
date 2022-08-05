package com.mtma.insta.downloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.data.InstaContractor.MediaEntry;
import com.mtma.insta.downloader.models.DownloadingObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.protocol.HTTP;


/**
 * This Class For Downloading a Single Story with a Link.
 */
public class DownloadStoryUtil {


    private static final String LOG_TAG = DownloadStoryUtil.class.getSimpleName();


    /**
     * Get the required data about the story that the user want to download it
     *
     * @param context the activity context.
     * @param storyUrl the story url.
     *
     * @return DownloadingObject contains the media data.
     */
    public static DownloadingObject getStoryData(Context context, String storyUrl) {

        // get the cookie from the preference that will be use to connecting to the instagram api.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = pref.getString(Constants.KEY_COOKIE,null);


        // get the userId who own the story.
        String userId = getStoryUserId(storyUrl);
        if ((userId.isEmpty())) {
            return null;
        }


        // extract the story unique code from the Url.
        String[] segments = storyUrl.split("/");
        String pkNumber = segments[(segments.length - 1)];

        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL("https://i.instagram.com/api/v1/feed/user/" + userId + "/reel_media/").openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            httpConnection.setRequestProperty(SM.COOKIE, cookie);
            httpConnection.setRequestProperty(HTTP.USER_AGENT, Constants.CONNECTION_USER_AGENT);


            // if the connection response code not equal 200 return from method early.
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;

            }


            // get the response data as JSON object that we can work on to extract data from it.
            String result = buildResultString(httpConnection);

            // if the result contains statement say "not-logged-in" means that the user need to login
            // and if it contains a JSON response, we get data from the JSON data.
            if (result.contains("not-logged-in") || result.contains("login_required")) {

                // initialize DownloadingObject and set the isNeedLogin value to true.
                DownloadingObject downloadingObject = new DownloadingObject();
                downloadingObject.setNeedLogin(true);
                return downloadingObject;

            } else  {

                // initialize a DownloadingObject that will use to store the media data.
                DownloadingObject downloadingObject = new DownloadingObject();

                try {


                    // go to the nodes we will use to get the required data.
                    JSONObject rootJSONObject = new JSONObject(result);

                    // array contains all the user stories data.
                    JSONArray itemsArray = rootJSONObject.getJSONArray("items");

                    // get the required story form all the user stories by the story pk code.
                    for (int i = 0; i < itemsArray.length(); i++) {

                        // get the pk code for the story item and if it not match with the pk code
                        // required stop the code in loop and check the next item until get matching.
                        JSONObject currentItemObject = itemsArray.getJSONObject(i);
                        String pkFromItem = currentItemObject.get("pk").toString();
                        if (!pkNumber.equals(pkFromItem)) {
                            continue;
                        }



                        // add media unique code to the object.
                        downloadingObject.setMediaCode(pkNumber);



                        // add the product type (always will be "story").
                        downloadingObject.setProductType(MediaEntry.MEDIA_PRODUCT_TYPE_STORY);



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


                    }



                    // for the user data.
                    JSONObject userObject = rootJSONObject.getJSONObject("user");

                    // for the username.
                    String userName = userObject.getString("username");
                    downloadingObject.setUserName(userName);


                    // for the profile picture url.
                    String profilePicUrl = userObject.getString("profile_pic_url");
                    downloadingObject.setProfilePicUrl(profilePicUrl);


                    return downloadingObject;


                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception from try-catch block number (1) inside " + LOG_TAG + " class in getStoryData method.", e);
                }

            }



        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block number (2) inside " + LOG_TAG + " class in getStoryData method.", e);
        }

        // return the DownloadingObject after get all required data from the api.
        return null;

    }





    /**
     * Get the userId for the person who own the story which user want to download it.
     *
     * @param storyUrl the story Url.
     *
     * @return the userId or empty value if there is something wrong when getting it.
     */
    private static String getStoryUserId(String storyUrl) {


        String[] splitString = storyUrl.split("/");
        String username = splitString[splitString.length - 2];

        String profileInfoUrl = "https://i.instagram.com/api/v1/users/web_profile_info/?username=";
        String userId = "";
        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(profileInfoUrl + username).openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            httpConnection.setRequestProperty(HttpHeaders.USER_AGENT, Constants.CONNECTION_USER_AGENT);



            // check first if the connection successfully before continue to below
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {


                // get the response data as JSON object that we can work on to extract data from it.
                String result = buildResultString(httpConnection);
                try {

                    // go to the nodes we will use to get the required data to extract the userId.
                    JSONObject rootJSONObject = new JSONObject(result);
                    JSONObject dataJSONObject = rootJSONObject.getJSONObject("data");
                    JSONObject userObject = dataJSONObject.getJSONObject("user");
                    userId = userObject.getString("id");

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in getStoryUserId method when get JSON Object value.", e);
                }

            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in getStoryUserId method when connecting to the server.", e);
        }

        return userId;

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
