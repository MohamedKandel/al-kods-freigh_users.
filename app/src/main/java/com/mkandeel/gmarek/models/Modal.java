package com.mkandeel.gmarek.models;

import java.util.List;
import java.util.Map;

public class Modal {
    private String cert_num;
    private String cert_date;
    private String comp_name;
    private String comp_num;
    private String country;
    private String trans;
    private boolean model_13;
    private boolean isFact;
    private String offers;
    private String userKey;

    public Modal(){}

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public Modal(String userKey, String cert_num, String cert_date, String comp_name, String comp_num, String country, String trans, boolean model_13, boolean isFact, String offers) {
        this.userKey = userKey;
        this.cert_num = cert_num;
        this.cert_date = cert_date;
        this.comp_name = comp_name;
        this.comp_num = comp_num;
        this.country = country;
        this.trans = trans;
        this.model_13 = model_13;
        this.isFact = isFact;
        this.offers = offers;
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

    public String getComp_name() {
        return comp_name;
    }

    public void setComp_name(String comp_name) {
        this.comp_name = comp_name;
    }

    public String getComp_num() {
        return comp_num;
    }

    public void setComp_num(String comp_num) {
        this.comp_num = comp_num;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public boolean isModel_13() {
        return model_13;
    }

    public void setModel_13(boolean model_13) {
        this.model_13 = model_13;
    }

    public boolean isFact() {
        return isFact;
    }

    public void setFact(boolean fact) {
        isFact = fact;
    }

    public String getOffers() {
        return offers;
    }

    public void setOffers(String offers) {
        this.offers = offers;
    }
}
