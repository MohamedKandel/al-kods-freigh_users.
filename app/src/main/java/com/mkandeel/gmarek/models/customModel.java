package com.mkandeel.gmarek.models;

public class customModel {
    private String cert_num;
    private String cert_date;
    private String trans;


    public customModel(String cert_num, String cert_date, String trans) {
        this.cert_num = cert_num;
        this.cert_date = cert_date;
        this.trans = trans;
    }

    public String getCert_num() {
        return cert_num;
    }

    public void setCert_num(String cert_num) {
        this.cert_num = cert_num;
    }

    public String getCert_date() {
        return cert_date;
    }

    public void setCert_date(String cert_date) {
        this.cert_date = cert_date;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }
}
