package com.mtma.insta.downloader.models;


import java.io.Serializable;
import java.util.ArrayList;


public class DownloadingObject implements Serializable {


    private boolean isNeedLogin = false;

    private String mediaCode = "";

    private int mediaType = -1;
    private int productType = -1;

    private String textAndHashtags = "";

    private ArrayList<String> allMediaUrls = new ArrayList<>();

    private String userName = "";
    private String profilePicUrl = "";



    public DownloadingObject() { }


    public boolean isNeedLogin() {
        return isNeedLogin;
    }

    public void setNeedLogin(boolean needLogin) {
        isNeedLogin = needLogin;
    }

    public String getMediaCode() {
        return mediaCode;
    }

    public void setMediaCode(String mediaCode) {
        this.mediaCode = mediaCode;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public String getTextAndHashtags() {
        return textAndHashtags;
    }

    public void setTextAndHashtags(String textAndHashtags) {
        this.textAndHashtags = textAndHashtags;
    }

    public ArrayList<String> getAllMediaUrls() {
        return allMediaUrls;
    }

    public void setAllMediaUrls(ArrayList<String> allMediaUrls) {
        this.allMediaUrls = allMediaUrls;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }




    @Override
    public String toString() {

        String text = "is_need_login : " + this.isNeedLogin +"\n"
                + "media_code : " + this.mediaCode +"\n"
                + "media_type : " + this.mediaType +"\n"
                + "product_type : " + this.productType +"\n"
                + "text_and_hashtag : " + this.textAndHashtags +"\n"
                + "all_media_url : " + this.allMediaUrls +"\n"
                + "user_name : " + this.userName +"\n"
                + "profile_pic : " + this.profilePicUrl +"\n";


        return text;

    }


}
