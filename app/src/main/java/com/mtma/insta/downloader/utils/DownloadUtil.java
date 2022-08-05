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

import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.cookie.SM;
import cz.msebera.android.httpclient.protocol.HTTP;


public class DownloadUtil {


    private static final String LOG_TAG = DownloadUtil.class.getSimpleName();


    /**
     * Get the required data about the media (Post, Reel, IGTV) that the user want to download it
     *
     * @param context the activity context.
     * @param url the media url.
     *
     * @return DownloadingObject contains the media data.
     */
    public static DownloadingObject getPostOrReelOrIgtvData(Context context, String url) {


        // add required property to the link to access to the instagram api.
        String finalUrl = url + "?__a=1&__d=dis";


        // get the cookie from the preference that will be use to connecting to the instagram api.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = pref.getString(Constants.KEY_COOKIE,null);

        Log.i(LOG_TAG, "  UNIX  cookie  :  " + cookie);

        try {

            // create the HttpURLConnection object and put the required property to it before start connection.
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(finalUrl).openConnection();
            httpConnection.setRequestMethod(HttpGet.METHOD_NAME);
            if (cookie != null) {
                httpConnection.setRequestProperty(SM.COOKIE, cookie);
            } else {
                httpConnection.setRequestProperty(HTTP.USER_AGENT, Constants.CONNECTION_USER_AGENT);
            }


            // if the connection response code not equal 200 return from method early.
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null;
            }


            // get the response data as JSON object that we can work on to extract data from it.
            String result = buildResultString(httpConnection);


            // if the result contains statement say "not-logged-in" means that the user need to login
            // and if it contains a JSON response, there is two phases :
            // 1. the JSON response start with a node "graphql".
            // 2. the JSON response start with a node "items"
            if (result.contains("not-logged-in") || result.contains("login_required")) {

                // initialize DownloadingObject and set the isNeedLogin value to true.
                DownloadingObject downloadingObject = new DownloadingObject();
                downloadingObject.setNeedLogin(true);
                return downloadingObject;

            } else if (result.contains("graphql")) {
                Log.i(LOG_TAG, "  UNIX  place  : graphql node");
                return getPostOrReelOrIgtvDataFromGraphqlNode(result, url);

            } else {
                Log.i(LOG_TAG, "  UNIX  place  : items node");
                return getPostOrReelOrIgtvDataFromItemArrayNode(result, url);

            }



        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in getPostOrReelOrIgtvData method.", e);
        }


        // return the  object after get all required data from the api.
        return null;

    }


    private static DownloadingObject getPostOrReelOrIgtvDataFromGraphqlNode(String responseResult, String url) {


        // initialize a DownloadingObject that will use to store the media data.
        DownloadingObject downloadingObject = new DownloadingObject();

        try {

            // go to the nodes we will use to get the required data.
            JSONObject rootJSONObject = new JSONObject(responseResult);
            JSONObject graphqlObject= rootJSONObject.getJSONObject("graphql");
            JSONObject mediaObject = graphqlObject.getJSONObject("shortcode_media");


            // the media unique code (can be get from the url directly also).
            String mediaCode = mediaObject.getString("shortcode");
            downloadingObject.setMediaCode(mediaCode);


            // determine the product type (post - reel - igtv) from the url.
            downloadingObject.setProductType(getProductType(url));


            // get the url for (post or reel or igtv) and add it to an arrayList
            // using the ArrayList for that because there is multimedia sometimes.
            ArrayList<String> allMediaUrls = new ArrayList<>();


            // that case for the {multimedia} (images or videos or both of them).
            if (responseResult.contains("edge_sidecar_to_children")) {

                downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_MULTIPLE);
                JSONObject objectSidecar = mediaObject.getJSONObject("edge_sidecar_to_children");
                JSONArray jsonArray = objectSidecar.getJSONArray("edges");

                String mediaUrl;

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = jsonArray.getJSONObject(i);
                    JSONObject node = object.getJSONObject("node");

                    boolean isVideo = node.getBoolean("is_video");
                    if (isVideo) {
                        mediaUrl = node.getString("video_url");
                    } else {
                        mediaUrl = node.getString("display_url");
                    }
                    allMediaUrls.add(mediaUrl);

                }

            }
            // any other case (post with single media - reel - igtv).
            else {

                // that case for any {video} (Single vide Post OR reel OR igtv).
                boolean isVideo = mediaObject.getBoolean("is_video");

                String firstMediaLink;
                if (isVideo) {
                    downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_VIDEO);
                    firstMediaLink = mediaObject.getString("video_url");
                } else {
                    downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_IMAGE);
                    firstMediaLink = mediaObject.getString("display_url");
                }
                allMediaUrls.add(firstMediaLink);

                downloadingObject.setAllMediaUrls(allMediaUrls);

            }


            // try to get the text and hashtags on the media and add it to
            // the DownloadingObject if it is exist.
            JSONObject textAndHashtagsObject = mediaObject.optJSONObject("edge_media_to_caption");
            if (textAndHashtagsObject != null) {
                JSONArray edgesArray = textAndHashtagsObject.getJSONArray("edges");
                if (edgesArray.length() != 0) {
                    JSONObject firstObjectInEdgesArray = edgesArray.getJSONObject(0);
                    JSONObject nodeObject = firstObjectInEdgesArray.getJSONObject("node");
                    String textAndHashtags = nodeObject.getString("text");
                    downloadingObject.setTextAndHashtags(textAndHashtags);
                }
            }


            // for the user data (username and his profile picture).
            JSONObject ownerObject = mediaObject.getJSONObject("owner");
            String userName = ownerObject.getString("username");
            downloadingObject.setUserName(userName);


            // for the profile picture url.
            String profilePicUrl = ownerObject.getString("profile_pic_url");
            downloadingObject.setProfilePicUrl(profilePicUrl);



        } catch (JSONException e) {
            Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in getPostOrReelOrIgtvDataFromGraphqlNode method.", e);
        }


        // return the DownloadingObject object after get all required data from the api.
        return downloadingObject;

    }


    private static DownloadingObject getPostOrReelOrIgtvDataFromItemArrayNode(String responseResult, String url) {


        // initialize a DownloadingObject that will use to store the media data.
        DownloadingObject downloadingObject = new DownloadingObject();

        try {

            // go to the nodes we will use to get the required data.
            JSONObject rootJSONObject = new JSONObject(responseResult);
            JSONArray itemsArray = rootJSONObject.getJSONArray("items");
            JSONObject itemObject = itemsArray.getJSONObject(0);


            // the media unique code (can be get from the url directly also).
            String mediaCode = itemObject.getString("code");
            downloadingObject.setMediaCode(mediaCode);


            // determine the product type (post - reel - igtv) from the url.
            downloadingObject.setProductType(getProductType(url));


            // get the url for (post or reel or igtv) and add it to an arrayList
            // using the ArrayList for that because there is multimedia sometimes.
            ArrayList<String> mediaUrls = new ArrayList<>();


            // that case for the {multimedia} (images or videos or both of them).
            if (responseResult.contains("carousel_media")) {

                downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_MULTIPLE);

                JSONArray carouselMediaArray = itemObject.getJSONArray("carousel_media");
                for (int i = 0; i < carouselMediaArray.length(); i++) {

                    JSONObject itemObjectInLoop = carouselMediaArray.getJSONObject(i);
                    JSONArray videoVersionsArray = itemObjectInLoop.optJSONArray("video_versions");

                    String mediaUrl;
                    if (videoVersionsArray == null) {

                        JSONObject imageVersions2Object = itemObjectInLoop.getJSONObject("image_versions2");
                        JSONArray candidatesArray = imageVersions2Object.getJSONArray("candidates");
                        JSONObject firstObjectInCandidatesArray = candidatesArray.getJSONObject(0);
                        mediaUrl = firstObjectInCandidatesArray.getString("url");
                    } else {
                        JSONObject firstObjectInCandidatesArray = videoVersionsArray.getJSONObject(0);
                        mediaUrl = firstObjectInCandidatesArray.getString("url");
                    }

                    mediaUrls.add(mediaUrl);

                }

            }
            // that case for any {video} (Single vide Post OR reel OR igtv).
            else if (responseResult.contains("video_versions")) {

                downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_VIDEO);

                JSONArray videoVersionsArray = itemObject.getJSONArray("video_versions");
                JSONObject firstObjectInCandidatesArray = videoVersionsArray.getJSONObject(0);
                String videoUrl = firstObjectInCandidatesArray.getString("url");

                mediaUrls.add(videoUrl);

            }
            // that case for single {image} Post.
            else {

                downloadingObject.setMediaType(MediaEntry.MEDIA_TYPE_IMAGE);

                JSONObject imageVersionsObject = itemObject.getJSONObject("image_versions2");
                JSONArray candidatesArray = imageVersionsObject.getJSONArray("candidates");
                JSONObject firstObjectInCandidatesArray = candidatesArray.getJSONObject(0);
                String imageUrl = firstObjectInCandidatesArray.getString("url");

                mediaUrls.add(imageUrl);

            }


            // add the ArrayList for urls to the DownloadingObject.
            downloadingObject.setAllMediaUrls(mediaUrls);


            // try to get the text and hashtags on the media and add it to
            // the DownloadingObject if it is exist.
            JSONObject captionObject = itemObject.optJSONObject("caption");
            if (captionObject != null) {
                String textAndHashtags = captionObject.getString("text");
                downloadingObject.setTextAndHashtags(textAndHashtags);
            }


            // for the user data (username and his profile picture).
            JSONObject userObject = itemObject.getJSONObject("user");

            // for the username.
            String userName = userObject.getString("username");
            downloadingObject.setUserName(userName);


            // for the profile picture url.
            String profilePicUrl = userObject.getString("profile_pic_url");
            downloadingObject.setProfilePicUrl(profilePicUrl);


        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG + " class in getPostOrReelOrIgtvDataFromItemArrayNode method.", e);
        }


        // return the DownloadingObject object after get all required data from the api.
        return downloadingObject;

    }

    private static int getProductType(String url) {

        if (url.contains("/reel/")) {
            return MediaEntry.MEDIA_PRODUCT_TYPE_REEL;
        } else if (url.contains("/tv/")) {
            return MediaEntry.MEDIA_PRODUCT_TYPE_IGTV;
        } else {
            return MediaEntry.MEDIA_PRODUCT_TYPE_POST;
        }

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