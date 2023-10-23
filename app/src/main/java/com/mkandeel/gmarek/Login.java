package com.mkandeel.gmarek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mkandeel.gmarek.databinding.ActivityLoginBinding;
import com.mkandeel.gmarek.models.UserObj;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private LoadingDialog dialog;
    private DBConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {
            mAuth = FirebaseAuth.getInstance();
            connection = DBConnection.getInstance(this);
            dialog = new LoadingDialog(this);

            binding.txtMail.setText(connection.getMail());
            binding.txtPass.requestFocus();

            binding.btnLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.startDialog();
                    String mail = String.valueOf(binding.txtMail.getText());
                    String pass = String.valueOf(binding.txtPass.getText());

                    mAuth.signInWithEmailAndPassword(mail, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database")
                                                .child("users");
                                        Query userID = reference.orderByChild("userKey")
                                                .equalTo(mAuth.getUid());

                                        userID.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot sp : snapshot.getChildren()) {
                                                        UserObj obj = sp.getValue(UserObj.class);
                                                        if (obj != null) {
                                                            if (obj.getStatus().equals("Active")) {
                                                                dialog.closeDialog();
                                                                if (obj.getDeviceID().equals(getDeviceID())) {
                                                                    // matching (same device)
                                                                    Intent intent = new Intent(Login.this, userOpt.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    // mis-match (another device with the same account)
                                                                    updateUser(obj.getDeviceID(), "InActive", obj.getUserKey(), obj.getUserName());
                                                                }
                                                            } else {
                                                                dialog.closeDialog();
                                                                // Account is InActive
                                                                Intent intent = new Intent(Login.this, InActiveAcc.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }

                                                        }
                                                    }
                                                } else {
                                                    dialog.closeDialog();
                                                    Toast.makeText(getApplicationContext(), "خطأ في اسم المستخدم او كلمة المرور",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.closeDialog();
                                                Toast.makeText(getApplicationContext(), "لايمكن تسجيل دخولك بهذا الحساب",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    } else {
                                        dialog.closeDialog();
                                        Toast.makeText(getApplicationContext(),
                                                        "خطأ في اسم المستخدم او كلمة المرور", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });
                }
            });
        } else {
            Tools.showDialog(this);
        }

    }


    @SuppressLint("HardwareIds")
    private String getDeviceID() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void updateUser(String deviceID, String status, String userKey, String userName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("deviceID", deviceID);
        map.put("status", status);
        map.put("userKey", userKey);
        map.put("userName", userName);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database")
                .child("users").child(userKey);


        reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(Login.this, InActiveAcc.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }
}