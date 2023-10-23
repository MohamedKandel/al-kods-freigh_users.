package com.mkandeel.gmarek;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mkandeel.gmarek.databinding.ActivityUserOptBinding;
import com.mkandeel.gmarek.models.Model;
import com.mkandeel.gmarek.rvAdapter.optAdapter;

import java.util.ArrayList;
import java.util.List;

public class userOpt extends AppCompatActivity {

    private ActivityUserOptBinding binding;
    private optAdapter adapter;
    private ClickListener listener;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserOptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            mAuth = FirebaseAuth.getInstance();

            List<Model> list = new ArrayList<>();
            list.add(new Model("فتح شهادة جديدة", R.drawable.add));
            list.add(new Model("الشهادات", R.drawable.view_all));
            list.add(new Model("تواصل معنا", R.drawable.contact_us));
            list.add(new Model("تسجيل خروج", R.drawable.sign_out));

            listener = new ClickListener() {
                @Override
                public void click(int index) {
                    Intent intent;
                    switch (index) {
                        case 0:
//                        Toast.makeText(getApplicationContext(),"Add new",
//                                Toast.LENGTH_SHORT).show();
                            intent = new Intent(userOpt.this, Add_Cert.class);
                            startActivity(intent);
                            finish();
                            break;
                        case 1:
                            intent = new Intent(userOpt.this, Show.class);
                            startActivity(intent);
                            finish();
                            break;
                        case 2:
                            intent = new Intent(userOpt.this, contactUs.class);
                            startActivity(intent);
                            finish();
                            break;
                        case 3:
                            if (mAuth.getCurrentUser() != null) {
                                mAuth.signOut();
                                intent = new Intent(userOpt.this, Login.class);
                                startActivity(intent);
                                finish();
                            }
                            break;
                    }
                }
            };

            adapter = new optAdapter(list, getApplicationContext(), listener);

            binding.rv.setAdapter(adapter);

            binding.rv.setLayoutManager(new LinearLayoutManager(this));

        } else {
            Tools.showDialog(this);
        }
    }

}