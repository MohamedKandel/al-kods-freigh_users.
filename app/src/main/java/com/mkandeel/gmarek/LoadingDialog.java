package com.mkandeel.gmarek;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog;

    private static LoadingDialog loadDialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public static synchronized LoadingDialog getInstance(Activity activity) {
        if (loadDialog == null) {
            loadDialog = new LoadingDialog(activity);
        }
        return loadDialog;
    }

    public void startDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading,null));
        builder.setCancelable(false);
        dialog = builder.create();
        if (!activity.isFinishing()) {
            dialog.show();
        }
    }

    public void closeDialog() {
        dialog.dismiss();
        dialog.cancel();
    }
}