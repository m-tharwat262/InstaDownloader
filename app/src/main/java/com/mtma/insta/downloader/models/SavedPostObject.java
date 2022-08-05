package com.mtma.insta.downloader.models;


public class SavedPostObject {


    private String mediaCode = "";

    private int mediaType = -1;
    private int productType = -1;

    private String userName = "";
    private String profilePicUrl = "";

    private String thumbnailUrl = "";


    public SavedPostObject() { }


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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }


    @Override
    public String toString() {

        String text = "media_code : " + this.mediaCode +"\n"
                + "media_type : " + this.mediaType +"\n"
                + "product_type : " + this.productType +"\n"
                + "thumbnail_url : " + this.thumbnailUrl +"\n"
                + "user_name : " + this.userName +"\n"
                + "profile_pic : " + this.profilePicUrl +"\n";


        return text;

    }


}
