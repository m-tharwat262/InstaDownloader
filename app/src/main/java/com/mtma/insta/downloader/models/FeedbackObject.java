package com.mtma.insta.downloader.models;


public class FeedbackObject {


    private String email;
    private String message;
    private long time;


    public FeedbackObject() { }

    public FeedbackObject(String email, String message, long time) {
        this.email = email;
        this.message = message;
        this.time = time;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
