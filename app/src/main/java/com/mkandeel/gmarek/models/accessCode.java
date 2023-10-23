package com.mkandeel.gmarek.models;

public class accessCode {
    private String accessCode;
    private String userKey;

    public accessCode(String accessCode, String userKey) {
        this.accessCode = accessCode;
        this.userKey = userKey;
    }

    public accessCode(){}

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
