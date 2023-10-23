package com.mkandeel.gmarek.models;

public class Model {
    private String text;

    //private String selected;
    private int img;

    //private static userModel model;

    public Model(){}

    public Model(String text, int img) {
        this.text = text;
        this.img = img;

    }

    /*
    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
*/
    /*public static userModel getInstance(String text,int img) {
        if (model == null) {
            model = new userModel(text,img);
        }
        return model;
    }

    public static userModel getInstance() {
        if (model== null) {
            model = new userModel();
        }
        return model;
    }*/

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
