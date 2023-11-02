package com.mkandeel.gmarek;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mkandeel.gmarek.databinding.ActivityMainBinding;
import com.mkandeel.gmarek.models.UserObj;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private String UUID;
    private FirebaseAuth mAuth;
    private LoadingDialog dialog;
    private DBConnection connection;
    private String access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Tools.isNetworkAvailable(this)) {
            //dialog = new LoadingDialog(MainActivity.this);
            // read database value if 1 application opened else application stopped working
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database");
            reference.child("available").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String value = snapshot.getValue(String.class);
                    if (value.trim().equals("A1")) {
                        mainFun();
                    } else {
                        startActivity(new Intent(MainActivity.this, ClosedApp.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Tools.showDialog(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.POST_NOTIFICATIONS)) {
            new AlertDialog.Builder(this)
                    .setTitle("السماح مطلوب")
                    .setMessage("يتطلب تطبيق شهادات القدس فريت السماح بارسال اشعارات")
                    .setPositiveButton("حسنًا", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                    306);
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
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    306);
        }
    }

    private void mainFun() {
        //binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
        connection = DBConnection.getInstance(this);
        String user = connection.getUserID();
        //FirebaseUser user = mAuth.getCurrentUser();
        if (!user.equals("")) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestStoragePermission();
            }

            mAuth = FirebaseAuth.getInstance();
            dialog = LoadingDialog.getInstance(MainActivity.this);

            binding.btnReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //binding.progBar.setVisibility(View.VISIBLE);

                    dialog.startDialog();

                    String mail = String.valueOf(binding.txtMail.getText());
                    String pass = String.valueOf(binding.txtPass.getText());
                    String username = String.valueOf(binding.txtName.getText());
                    access = String.valueOf(binding.txtAccess.getText());

                    if (Tools.isEmpty(mail, pass, username, access)) {
                        dialog.closeDialog();
                        Toast.makeText(getApplicationContext(), "برجاء ملئ جميع الحقول المطلوبة",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        RegisterUser(mail, pass, username);
                    }

                }
            });
        }

    }


    private void RegisterUser(String mail, String pass, String userName) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Database").child("accessCode");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if (value == null) {
                    /*Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT)
                            .show();*/
                } else {
                    if (value.equals(access)) {
                        //////////////////////////////////////////////////////////////////////
                        mAuth.createUserWithEmailAndPassword(mail, pass)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        //binding.progBar.setVisibility(View.GONE);
                                        dialog.closeDialog();
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(userName).build();
                                            if (user != null) {
                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("username", "username setted");


                                                                    UUID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                                                    connection.insertIntoUsers(UUID, mail, pass, userName);

                                                                    AddUserObject(UUID, userName, mail, getDeviceID(), "Active");

                                                                    String newAccessCode = Tools.generateAccessCode();
                                                                    access = newAccessCode;
                                                                    DatabaseReference ref = FirebaseDatabase.getInstance()
                                                                            .getReference("Database").child("accessCode");
                                                                    ref.setValue(newAccessCode).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    //Toast.makeText(MainActivity.this, "changed", Toast.LENGTH_SHORT).show();
                                                                                    Log.d("Access Code","Changed");
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.d("Access Code","Failed to Change");
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });
                    } else {
                        dialog.closeDialog();
                        Toast.makeText(getApplicationContext(), "رمز الوصول خاطئ",
                                Toast.LENGTH_SHORT).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        AuthCredential credential = EmailAuthProvider.getCredential(mail, pass);
                        if (user != null) {
                            String UserID = user.getUid();
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                connection.deleteUser(UserID);
                                                                Log.d("TAG", "User account deleted.");
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private String getDeviceID() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void AddUserObject(String userKey, String userName, String mail, String DeviceID, String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Database").child("users");
        UserObj obj = UserObj.getInstance(userKey, userName, mail, DeviceID, status);
        reference.child(userKey).setValue(obj)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, Login.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 306: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                    Tools.setNotificationValueToSP(true,this);
                } else {
                    Tools.showNotificationDialog(this);
                    //Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}