package com.mkandeel.gmarek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.mkandeel.gmarek.databinding.ActivityDisplayCertBinding;
import com.mkandeel.gmarek.models.Certificate;

import java.util.ArrayList;
import java.util.Objects;

public class DisplayCert extends AppCompatActivity {


    private final int STORAGE_PERMISSION_CODE = 1;
    private ActivityDisplayCertBinding binding;
    private ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDisplayCertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            list = getIntent().getStringArrayListExtra("urls");
            Certificate certificate = getIntent().getParcelableExtra("cert_list");

            if (certificate != null) {
                binding.txtViewCertnum.setText(certificate.getCert_num());
                binding.txtViewCertdate.setText(certificate.getCert_date());
                binding.txtViewCompname.setText(certificate.getCert_name());
                binding.txtViewCompnum.setText(certificate.getComp_num());
                binding.txtViewCountry.setText(certificate.getCountry());
                binding.txtViewTrans.setText(certificate.getTrans());
                String model13 = (certificate.isModel_13()) ? "نعم" : "لا";
                binding.txtViewModel13.setText(model13);
                String build_fact = (certificate.isChk_fact()) ? "نعم" : "لا";
                binding.txtViewBuildfact.setText(build_fact);
                binding.txtViewOffers.setText(certificate.getOffers());
            } else {
                Toast.makeText(getApplicationContext(), "خطأ في جلب بيانات الشهادة...\nتواصل مع المديرين في حالة استمرار الخطأ", Toast.LENGTH_SHORT)
                        .show();
            }

            binding.btnDownload.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("UnspecifiedRegisterReceiverFlag")
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        Download(list, Objects.requireNonNull(getIntent().getExtras()).getString("num"));
                    } else {
                        requestStoragePermission();
                    }
                }
            });
        } else {
            Tools.showDialog(this);
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("السماح مطلوب")
                    .setMessage("اذن الوصول لذاكرة التخزين مطلوب لتحميل الملفات الخاصة بالشهادات إلى هاتفك المحمول")
                    .setPositiveButton("حسنًا", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(DisplayCert.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("تجاهل", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    private void Download(ArrayList<String> urls,String txt) {
        for (String url:urls) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("جارِ تحميل ملفات الشهادة");
            request.setTitle("الشهادة رقم "+txt);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            }
            request.setDestinationInExternalFilesDir(getApplicationContext(),Environment.DIRECTORY_DOCUMENTS+"/"+txt,
                    System.currentTimeMillis()+"."+getExtn(url));
            // get download service and enqueue file
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        }
    }

    private String getExtn(String url) {
        String extn = "";
        String[] arr = url.split("\\?");
        extn = arr[0].substring(arr[0].length()-3);
        return extn;
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            // your code
            Toast.makeText(ctxt, "تم تحميل الملف بنجاح", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"تم اعطاء السماح ... من فضلك اضغط على زر التحميل مرة اخرى لتحميل الملفات",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),"لن تتمكن من تحميل الملفات على هاتفك إلا عن طريق السماح للتطبيق بالوصول للذاكرة",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}