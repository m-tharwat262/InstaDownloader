package com.mtma.insta.downloader.models;


/**
 * that to save the users friends and data from the stories.
 */
public class UserHasStoryObject {


    private String userName = "";
    private String userId = "";
    private String realName = "";
    private String profilePictureUrl = "";


    public UserHasStoryObject() { }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }


    @Override
    public String toString() {

        String text = "userName : " + this.userName +"\n"
                + "user_id : " + this.userId +"\n"
                + "real_name : " + this.realName +"\n"
                + "profile_picture_url : " + this.profilePictureUrl +"\n";

        return text;

    }

}
