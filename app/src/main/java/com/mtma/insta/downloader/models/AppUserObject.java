package com.mtma.insta.downloader.models;


public class AppUserObject {


    private String userName;
    private String realName;
    private String profilePicUrl;


    public AppUserObject (String userName, String realName, String profilePicUrl) {

        this.userName = userName;
        this.realName = realName;
        this.profilePicUrl = profilePicUrl;

    }


    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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
}
