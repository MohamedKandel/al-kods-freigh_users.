package com.mkandeel.gmarek;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mkandeel.gmarek.databinding.ActivityShowBinding;
import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.customModel;
import com.mkandeel.gmarek.rvAdapter.showAdapter;

import java.util.ArrayList;
import java.util.List;

public class Show extends AppCompatActivity {

    private ActivityShowBinding binding;
    private DBConnection connection;
    private List<customModel> list;
    private showAdapter adapter;
    private ArrayList<String> download_urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            connection = DBConnection.getInstance(this);
            //list = new ArrayList<>();
            list = connection.getCustomCertData();

            adapter = new showAdapter(list, getApplicationContext());
            binding.rvCert.setAdapter(adapter);
            binding.rvCert.setLayoutManager(new LinearLayoutManager(this));

            adapter.setOnClickListener(new showAdapter.ItemClicked() {
                @Override
                public void onItemClickListener(String txt, int position) {
                    Certificate cert = connection.getCertData(txt);
                    download_urls = connection.getFilesForCert(txt);
                    Intent intent = new Intent(Show.this, DisplayCert.class);
                    intent.putExtra("cert_list", cert);
                    intent.putStringArrayListExtra("urls", download_urls);
                    intent.putExtra("num", txt);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            Tools.showDialog(this);
        }
    }

}