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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.mkandeel.gmarek.databinding.ActivityDisplayCertBinding;
import com.mkandeel.gmarek.models.Certificate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class DisplayCert extends AppCompatActivity {



    private ActivityDisplayCertBinding binding;
    private String cert_num;
    private Certificate cert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDisplayCertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            //list = getIntent().getStringArrayListExtra("urls");
            Certificate certificate = getIntent().getParcelableExtra("cert_data");

            if (certificate != null) {
                binding.txtViewCertnum.setText(certificate.getCert_num());
                binding.txtViewCertdate.setText(certificate.getCert_date());
                binding.txtViewCompname.setText(certificate.getComp_name());
                binding.txtViewCompnum.setText(certificate.getComp_num());
                binding.txtViewCountry.setText(certificate.getCountry());
                binding.txtViewTrans.setText(certificate.getTrans());
                String model13 = (certificate.isModel_13()) ? "نعم" : "لا";
                binding.txtViewModel13.setText(model13);
                String build_fact = (certificate.isChk_fact()) ? "نعم" : "لا";
                binding.txtViewBuildfact.setText(build_fact);
                binding.txtViewOffers.setText(certificate.getOffers());

                cert_num = certificate.getCert_num();

            } else {
                cert_num = getIntent().getExtras().getString("cert_num");

                getCertDataFromRTDB();
            }




            binding.btnDownload.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("UnspecifiedRegisterReceiverFlag")
                @Override
                public void onClick(View v) {
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        Download(list, Objects.requireNonNull(getIntent().getExtras()).getString("num"));
                    } else {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED) {
                            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                            Download(list, Objects.requireNonNull(getIntent().getExtras()).getString("num"));
                        } else {
                            requestStoragePermission();
                        }
                    }*/
                    Intent intent = new Intent(DisplayCert.this,DownloadFiles.class);
                    intent.putExtra("cert_num",cert_num);
                    intent.putExtra("cert_data",cert);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            Tools.showDialog(this);
        }
    }

    private void getCertDataFromRTDB() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database")
                .child("Certificates");
        Query query = reference.orderByChild("cert_num")
                .equalTo(cert_num);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        cert = ds.getValue(Certificate.class);
                    }
                    if (cert!= null) {
                        binding.txtViewCertnum.setText(cert.getCert_num());
                        binding.txtViewCertdate.setText(cert.getCert_date());
                        binding.txtViewCompname.setText(cert.getComp_name());
                        binding.txtViewCompnum.setText(cert.getComp_num());
                        binding.txtViewCountry.setText(cert.getCountry());
                        binding.txtViewTrans.setText(cert.getTrans());
                        binding.txtViewOffers.setText(cert.getOffers());
                        String model13 = (cert.isModel_13()) ? "نعم" : "لا";
                        String fact = (cert.isChk_fact()) ? "نعم" : "لا";
                        binding.txtViewModel13.setText(model13);
                        binding.txtViewBuildfact.setText(fact);
                        binding.btnDownload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(DisplayCert.this,DownloadFiles.class);
                                intent.putExtra("cert_num",cert_num);
                                startActivity(intent);
                                finish();
                            }
                        });

                    }
                } else {
                    Toast.makeText(DisplayCert.this, "لا يوجد ملفات لهذه الشهادة", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DisplayCert.this, Show.class);
        startActivity(intent);
        finish();
    }
}