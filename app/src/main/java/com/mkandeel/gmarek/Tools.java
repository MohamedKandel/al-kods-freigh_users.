package com.mkandeel.gmarek;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tools {

    private static SharedPreferences sp;
    private static SharedPreferences.Editor edit;

    public static boolean isEmpty(String ... strs) {
        boolean flag = false;
        for (String str:strs) {
            if (str.trim().isEmpty()) {
                flag = true;
                break;
            }
        }
        return flag;
    }
    public static boolean BooleanParseBoolean(int value) {
        return value == 1;
    }
    public static int IntegerParseInt(boolean value) {
        return (value) ? 1 : 0;
    }
    public static boolean isNetworkAvailable(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
    public static void showDialog(Activity activity) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.internet_dialog,null));
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btn = dialog.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                dialog.dismiss();
                activity.finish();
            }
        });
    }

    public static String generateAccessCode() {
        String str = "ZXCVBNMASDFGHJKLQWERTYUIOPzxcvbnmasdfghjklqwertyuiop@!#123456789";
        Random random = new Random();
        StringBuilder accessCode = new StringBuilder();
        for (int i=0;i<5;i++) {
            int randIndx = random.nextInt(str.length());
            char c = str.charAt(randIndx);
            accessCode.append(c);
        }
        return accessCode.toString();
    }

    public static String getFileExtn(Context context,Uri uri) {
        ContentResolver cr = context.getContentResolver();
        MimeTypeMap mtm = MimeTypeMap.getSingleton();
        return mtm.getExtensionFromMimeType(cr.getType(uri));
    }

    public static void setNotificationValueToSP(boolean value,Context context) {
        initSP(context);
        edit.putBoolean("notification",value);
        edit.commit();
        edit.apply();
    }

    public static boolean getNotificationValueFromSP(Context context) {
        initSP(context);
        return sp.getBoolean("notification",false);
    }

    private static void initSP(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        edit = sp.edit();
    }

    public static void showNotificationDialog(Activity activity) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.request_notification_layout,null));
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button btn_agree = dialog.findViewById(R.id.btn_agree);
        Button btn_reject = dialog.findViewById(R.id.btn_rej);

        btn_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getApplicationContext().getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });

        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
    }

    public static List<String> mergeLists(List<String>... lists) {
        List<String> newList = new ArrayList<>();
        for (List<String> list : lists) {
            newList.addAll(list);
        }
        return newList;
    }


}
