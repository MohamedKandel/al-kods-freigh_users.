package com.mkandeel.gmarek;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.customModel;

import java.util.ArrayList;

public class DBConnection extends SQLiteOpenHelper {
    private static final String DBname = "mydbUsers.db";
    private static final int DBVersion = 1;

    private static DBConnection connection;

    private DBConnection(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DBname, factory, DBVersion);
    }

    public static synchronized DBConnection getInstance(Context context) {
        if (connection == null) {
            connection = new DBConnection(context,DBname,null,DBVersion);
        }
        return connection;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS = "create table if not exists users (" +
                "userKey     int primary key," +
                "username    text," +
                "pass        text," +
                "email       text);";
        String CREATE_CERT = "create table if not exists certificates (" +
                "cert_num       text primary key," +
                "cert_date      text," +
                "comp_name      text," +
                "comp_num       text," +
                "country        text," +
                "trans          text," +
                "offers         text," +
                "model_13       integer," +
                "fact           integer);";

        String CREATE_FILES = "create table if not exists files (" +
                "cert_num                text," +
                "file_download_url       text," +
                "foreign key (\"cert_num\") references \"certificates\" (\"cert_num\"));";

        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_CERT);
        db.execSQL(CREATE_FILES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists users");
        db.execSQL("drop table if exists files");
        db.execSQL("drop table if exists certificates");
        onCreate(db);
    }

    public void insertIntoUsers(String userKey,String mail,String pass,String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("userKey",userKey);
        cv.put("username",username);
        cv.put("pass",pass);
        cv.put("email",mail);
        db.insert("users",null,cv);
    }

    @SuppressLint("Range")
    public Certificate getCertData(String cert_num) {
        //ArrayList<Certificate> list = new ArrayList();
        Certificate c = new Certificate();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from certificates " +
                "where cert_num = ?",new String[]{cert_num});
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            String num = cursor.getString(cursor.getColumnIndex("cert_num"));
            String date = cursor.getString(cursor.getColumnIndex("cert_date"));
            String comp_name = cursor.getString(cursor.getColumnIndex("comp_name"));
            String comp_num = cursor.getString(cursor.getColumnIndex("comp_num"));
            String country = cursor.getString(cursor.getColumnIndex("country"));
            String trans = cursor.getString(cursor.getColumnIndex("trans"));
            String offers = cursor.getString(cursor.getColumnIndex("offers"));
            int model_13 = cursor.getInt(cursor.getColumnIndex("model_13"));
            int fact = cursor.getInt(cursor.getColumnIndex("fact"));

            Certificate certificate = new Certificate(num,date,comp_name,comp_num,
                    country,trans,offers,Tools.BooleanParseBoolean(model_13)
                    ,Tools.BooleanParseBoolean(fact));
            //list.add(certificate);
            c = certificate;
            cursor.moveToNext();
        }
        return c;
    }

    @SuppressLint("Range")
    public ArrayList<customModel> getCustomCertData() {
        ArrayList<customModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("select cert_num,cert_date,trans from certificates",null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            customModel model = new customModel(cursor.getString(cursor.getColumnIndex("cert_num")),
                    cursor.getString(cursor.getColumnIndex("cert_date")),
                    cursor.getString(cursor.getColumnIndex("trans")));
            list.add(model);
            cursor.moveToNext();
        }
        return list;
    }

    public void insertIntoCertificates(String cert_num,String date,String comp_name,
                                       String comp_num,String country,String trans,
                                       String offers,int model_13,int fact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("cert_num",cert_num);
        cv.put("cert_date",date);
        cv.put("comp_name",comp_name);
        cv.put("comp_num",comp_num);
        cv.put("country",country);
        cv.put("trans",trans);
        cv.put("offers",offers);
        cv.put("model_13",model_13);
        cv.put("fact",fact);
        db.insert("certificates",null,cv);
    }

    public void insertIntoFiles(String cert_num,String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("cert_num",cert_num);
        cv.put("file_download_url",url);
        db.insert("files",null,cv);
    }

    @SuppressLint("Range")
    public ArrayList<String> getFilesForCert(String cert_num) {
        ArrayList<String> list = new ArrayList();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select file_download_url from files " +
                "where cert_num = ?",new String[]{cert_num});
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            list.add(cursor.getString(cursor.getColumnIndex("file_download_url")));
            cursor.moveToNext();
        }
        return list;
    }

    @SuppressLint("Range")
    public String getUserID() {
        String UUID = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select userKey from users ",null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            UUID = cursor.getString(cursor.getColumnIndex("userKey"));
            cursor.moveToNext();
        }
        return UUID;
    }

    @SuppressLint("Range")
    public String getMail() {
        String UUID = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select email from users ",null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            UUID = cursor.getString(cursor.getColumnIndex("email"));
            cursor.moveToNext();
        }
        return UUID;
    }

    public void deleteUser(String userKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.rawQuery("delete from users where userKey = ?",new String[]{userKey});
    }

    public void deleteCertificate(String cert_num) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("certificates","cert_num = ?",new String[]{cert_num});
        db.delete("files","cert_num = ?",new String[]{cert_num});
    }

    public void deleteAllCertificate() {
        SQLiteDatabase db = this.getWritableDatabase();
        /*db.rawQuery("delete from certificates",
                null);
        db.rawQuery("delete from files",
                null);*/
        db.delete("certificates",null,null);
        db.delete("files",null,null);
    }
}
