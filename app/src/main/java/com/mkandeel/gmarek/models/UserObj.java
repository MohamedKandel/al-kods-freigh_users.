package com.mkandeel.gmarek.models;

public class UserObj {
    private String userKey;
    private String deviceID;
    private String status;
    private String userName;
    private String mail;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    private static UserObj mUser;

    private UserObj(){}

    private UserObj(String userKey, String userName, String mail, String deviceID, String status) {
        this.userKey = userKey;
        this.deviceID = deviceID;
        this.mail = mail;
        this.status = status;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static UserObj getInstance(String userKey,String userName,String mail, String deviceID,
                                      String status) {
        if (mUser == null) {
            mUser = new UserObj(userKey,userName,mail,deviceID,status);
        }
        return mUser;
    }

    public static UserObj getInstance() {
        if (mUser == null) {
            mUser = new UserObj();
        }
        return mUser;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}