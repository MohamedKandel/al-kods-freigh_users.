package com.mkandeel.gmarek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.mkandeel.gmarek.databinding.ActivityDownloadFilesBinding;
import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.downloadModel;
import com.mkandeel.gmarek.rvAdapter.DownloadAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class DownloadFiles extends AppCompatActivity {

    private final int STORAGE_PERMISSION_CODE = 1;
    private String cert_num;
    private Certificate certificate;
    private Intent myIntent;
    private FirebaseListener flistener;
    private List<downloadModel> list;
    private ActivityDownloadFilesBinding binding;
    private ClickListener listener;
    private DownloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cert_num = getIntent().getExtras().getString("cert_num");
        certificate = getIntent().getExtras().getParcelable("cert_data");

        FillList();

        final String[] folderName = {""};
        listener = new ClickListener() {
            @Override
            public void click(int index) {
                switch (index) {
                    case 0:
                        folderName[0] = "ايصالات الجمرك";
                        break;
                    case 1:
                        folderName[0] = "ايصالات الارضية";
                        break;
                    case 2:
                        folderName[0] = "ايصالات الهيئة";
                        break;
                    case 3:
                        folderName[0] = "ايصالات سلامة الغذاء";
                        break;
                    case 4:
                        folderName[0] = "ايصالات الزراعة";
                        break;
                    case 5:
                        folderName[0] = "ايصالات المنشأ";
                        break;
                    case 6:
                        folderName[0] = "صور إذن الافراج";
                        break;
                    case 7:
                        folderName[0] = "صور البوصلة";
                        break;
                    case 8:
                        folderName[0] = "صور الفاتورة";
                        break;
                    case 9:
                        folderName[0] = "صور عرض سلامة الغذاء";
                        break;
                    case 10:
                        folderName[0] = "صور عروض الزراعة";
                        break;
                    case 11:
                        folderName[0] = "صور الشهادة الصحية";
                        break;
                    case 12:
                        folderName[0] = "ملفات أخرى (نموذج 13 و المنشأ)";
                        break;
                }
                getFiles(cert_num, folderName[0]);
            }
        };

        flistener = new FirebaseListener() {
            @Override
            public void onComplete(Set<String> list) {
                downloadWhenButtonClick(list, folderName[0]);
            }
        };

        adapter = new DownloadAdapter(listener, list);
        binding.rv.setAdapter(adapter);
    }

    private void FillList() {
        list = new ArrayList<>();
        list.add(new downloadModel("ايصال الجمرك"));
        list.add(new downloadModel("ايصال الارضة"));
        list.add(new downloadModel("ايصال الهيئة"));
        list.add(new downloadModel("ايصال سلامة غذاء"));
        list.add(new downloadModel("ايصال زراعة"));
        list.add(new downloadModel("ايصال منشأ"));
        list.add(new downloadModel("صورة اذن الافراج"));
        list.add(new downloadModel("صورة بوصلة"));
        list.add(new downloadModel("صورة الفاتورة"));
        list.add(new downloadModel("صورة عرض سلامة الغذاء"));
        list.add(new downloadModel("صورة عروض الزراعة"));
        list.add(new downloadModel("صورة الشهادة الصحية"));
        list.add(new downloadModel("ملفات اخرى (نموذج 13 و المنشأ)"));
    }

    private void getFiles(String cert_num, String folderName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StorageReference reference = FirebaseStorage.getInstance()
                    .getReference("Certificates").child(cert_num)
                    .child(folderName);
            ArrayList<Task> tasks = new ArrayList<>();
            reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    Set<String> urls = new HashSet<>();
                    if (listResult.getItems().size() > 0) {
                        listResult.getItems().forEach(new Consumer<StorageReference>() {
                            @Override
                            public void accept(StorageReference storageReference) {
                                Task task = storageReference.getDownloadUrl();
                                tasks.add(task);
                                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        urls.add(uri.toString());
                                    }
                                });
                            }
                        });
                        Tasks.whenAllSuccess(tasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Object>> task) {
                                flistener.onComplete(urls);
                            }
                        });

                    } else {
                        Toast.makeText(DownloadFiles.this, "لايوجد ملفات مرفقة لهذا الايصال", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void downloadWhenButtonClick(Set<String> urls, String folderName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            myIntent = registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            DownloadCertFiles(urls, cert_num, folderName);
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                myIntent = registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                DownloadCertFiles(urls, cert_num, folderName);
            } else {
                requestStoragePermission();
            }
        }
    }

    private void DownloadCertFiles(Set<String> list, String txt, String folderName) {
        for (String url : list) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("جارِ تحميل ملفات الشهادة");
            request.setTitle("الشهادة رقم " + txt);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (Tools.getNotificationValueFromSP(this)) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
                    }
                } else {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
                }
            }
            request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOCUMENTS + "/" + txt + "/" + folderName,
                    System.currentTimeMillis() + "." + getExtn(url));
            // get download service and enqueue file
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("السماح مطلوب")
                    .setMessage("اذن الوصول لذاكرة التخزين مطلوب لتحميل الملفات الخاصة بالشهادات إلى هاتفك المحمول")
                    .setPositiveButton("حسنًا", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(DownloadFiles.this,
                                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
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

    private String getExtn(String url) {
        String extn = "";
        String[] arr = url.split("\\?");
        extn = arr[0].substring(arr[0].length() - 3);
        return extn;
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            // your code
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Toast.makeText(ctxt, "تم تحميل الملف بنجاح", Toast.LENGTH_SHORT)
                        .show();
            } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dm);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(onComplete);
        } catch (Exception e) {
            Log.d("Receiver", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DownloadFiles.this,DisplayCert.class);
        intent.putExtra("cert_list",certificate);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "تم اعطاء السماح ... من فضلك اضغط على زر التحميل مرة اخرى لتحميل الملفات",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "لن تتمكن من تحميل الملفات على هاتفك إلا عن طريق السماح للتطبيق بالوصول للذاكرة",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}