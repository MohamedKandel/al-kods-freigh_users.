package com.mkandeel.gmarek;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mkandeel.gmarek.NotificationHandler.ApiUtils;
import com.mkandeel.gmarek.NotificationHandler.NotificationData;
import com.mkandeel.gmarek.NotificationHandler.PushNotification;
import com.mkandeel.gmarek.databinding.ActivityShowBinding;
import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.Modal;
import com.mkandeel.gmarek.models.customModel;
import com.mkandeel.gmarek.rvAdapter.showAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Show extends AppCompatActivity {

    private ActivityShowBinding binding;
    private DBConnection connection;
    private List<customModel> list;
    private showAdapter adapter;
    private ArrayList<String> download_urls;
    private LoadingDialog dialog;
    private List<Uri> mlist;
    private String mystr;
    private boolean isModel13;
    private String TOPIC = "adminsTopic";
    private List<String> urls = new ArrayList<>();
    private List<String> mUrls = new ArrayList<>();
    private boolean addModelOrFact;
    private FirebaseListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            connection = DBConnection.getInstance(this);
            //list = connection.getCustomCertData();
            dialog = new LoadingDialog(this);

            mlist = new ArrayList<>();

            /////////////////////////////////////////////////////////////////////
            ActivityResultLauncher<Intent> arl = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                if (result.getData() == null) {
                                    return;
                                }

                                if (result.getData().getClipData() != null) {
                                    for (int i = 0; i < result.getData().getClipData().getItemCount(); i++) {
                                        Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                                        mlist.add(uri);
                                    }
                                } else {
                                    Uri uri = result.getData().getData();
                                    mlist.add(uri);
                                }
                                Log.d("my list", mlist + "");
                                if (addModelOrFact) {
                                    AddDataToCert(mystr, isModel13, true);
                                } else {
                                    uploadExtrasToStorage(mystr,mlist);
                                }
                            }
                        }
                    }
            );
            /////////////////////////////////////////////////////////////////////
            //SearchForCertificates(UUID);
            //list = connection.getCustomCertData();
            list = new ArrayList<>();
            String UUID = connection.getUserID();
            searchCertificates(UUID);

            flistener = new FirebaseCompleteListener() {
                @Override
                public void onComplete(List<customModel> list) {
                    adapter = new showAdapter(list, getApplicationContext());
                    binding.rvCert.setAdapter(adapter);
                    binding.rvCert.setLayoutManager(new LinearLayoutManager(Show.this));
                    ///////////////////////////////////////////////////////////////////////
                    adapter.setOnClickListener(new showAdapter.ItemClicked() {
                        @Override
                        public void onItemClickListener(String str, int position) {
                            //Certificate cert = connection.getCertData(str);
                            //download_urls = connection.getFilesForCert(str);
                            //intent.putStringArrayListExtra("urls", download_urls);
                            Intent intent = new Intent(Show.this, DisplayCert.class);
                            intent.putExtra("cert_num", str);
                            //intent.putExtra("cert_list", cert);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onItemLongClickListener(String str, int itemId, int index) {
                            mystr = str;
                            if (itemId == R.id.add_13) {
                                Intent mIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                mIntent.setType("*/*");
                                mIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                arl.launch(mIntent);
                                addModelOrFact = true;
                                isModel13 = true;
                            } else if (itemId == R.id.add_fact) {
                                Intent mIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                mIntent.setType("*/*");
                                mIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                arl.launch(mIntent);
                                addModelOrFact = true;
                                isModel13 = false;
                            } else if (itemId == R.id.edit_files) {
                                Intent intent = new Intent(Show.this,UploadExtras.class);
                                intent.putExtra("cert_num",mystr);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            };




        } else {
            Tools.showDialog(this);
        }
    }

    private FirebaseCompleteListener flistener;

    private void searchCertificates(String UUID) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Database").child("Certificates");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Certificate certificate = ds.getValue(Certificate.class);
                        if (certificate != null) {
                            if (certificate.getUserKey().equals(UUID)) {
                                customModel model = new customModel(certificate.getCert_num(),
                                        certificate.getCert_date(),certificate.getTrans());
                                list.add(model);
                            }
                        }
                    }
                    flistener.onComplete(list);

                    //Log.e("Data in RTDB","found");
                } else {
                    Toast.makeText(Show.this, "لايوجد أي شهادات حتى الآن", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Data in RTDB",error.getMessage());
            }
        });
    }

    private void AddDataToCert(String cert_num, boolean isModel13, boolean value) {
        String childName = isModel13 ? "model_13" : "fact";
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Database").child("Certificates")
                .child(cert_num);
        ref.child(childName).setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //uploadExtrasToStorage(cert_num, mlist);
                        uploadExtrasToStorage(cert_num, mlist);
                    }
                });
    }

    private void uploadExtrasToStorage(String cert_num, List<Uri> list) {
        StorageReference mReference = FirebaseStorage.getInstance()
                .getReference("Certificates")
                .child(cert_num);
        final int[] k = {0};
        for (Uri uri : list) {
            StorageReference sr = mReference.child("Other")
                    .child(System.currentTimeMillis() + "."
                            + Tools.getFileExtn(Show.this, uri));
            sr.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        k[0]++;
                        if (k[0] == list.size()) {
                            SendMsg();
                        }
                    }
                }
            });
        }
    }


    /*private void SearchForCertificates(String UUID) {
        DatabaseReference reference = FirebaseDatabase
                .getInstance().getReference("Database");
        reference.child("Certificates");
        Query query = reference.orderByChild("userKey").equalTo(UUID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        customModel model = ds.getValue(customModel.class);
                        if (model!= null) {
                            Log.e("Model Certificate", model.getCert_num());
                            list.add(model);
                        } else {
                            Log.e("Model Certificate", "null");
                        }
                    }
                    listener.onComplete(list);
                } else {
                    Toast.makeText(Show.this, "لا يوجد أية شهادات حتى الآن", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    /*private void AddDataToCert(String cert_num, boolean isModel13, boolean value) {
        String childName = isModel13 ? "model_13" : "fact";
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Database").child("Certificates")
                .child(cert_num);
        ref.child(childName).setValue(value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        uploadExtrasToStorage(cert_num, mlist);
                    }
                });
    }*/

    /*@SuppressLint("NotifyDataSetChanged")
    private void deleteCertByNum(String cert_num, int index) {
        //dialog.startDialog();
        connection.deleteCertificate(cert_num);
        list.remove(index);
        Log.e("ListSize",list.size()+"");
        adapter.notifyDataSetChanged();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database")
                .child("Certificates");
        reference.child(cert_num).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        StorageReference sr = FirebaseStorage.getInstance()
                                .getReference("Certificates");
                        sr.child(cert_num).listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        List<StorageReference> lreference =
                                                listResult.getItems();
                                        for (StorageReference item : lreference) {
                                            item.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                @Override
                                                public void onSuccess(ListResult listResult) {
                                                    List<StorageReference> mlist =
                                                            listResult.getItems();
                                                    for (StorageReference storage : mlist) {
                                                        storage.delete();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("onDelete",e.getMessage());
                    }
                });
    }*/

    /*private void uploadExtrasToStorage(String cert_num, List<Uri> list) {
        dialog.startDialog();
        StorageReference sRef = FirebaseStorage.getInstance()
                .getReference("Certificates")
                .child(cert_num);
        for (Uri uri : list) {
            StorageReference mRef = sRef.child(System.currentTimeMillis() +
                    "." + Tools.getFileExtn(getApplicationContext(), uri));
            mRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                        .getReference("Database")
                                        .child("Certificates")
                                        .child(cert_num)
                                        .child("list");
                                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String key = ds.getKey();
                                            if (key != null) {
                                                s.add(Integer.parseInt(key));
                                            }
                                        }
                                        int max_key = Collections.max(s);
                                        max_key++;
                                        s.add(max_key);
                                        Log.e("myNextKey",String.valueOf(max_key));
                                        dbRef.child(String.valueOf(max_key)).setValue(uri.toString());
                                        connection.insertIntoFiles(cert_num,uri.toString());
                                        i++;
                                        if (i == list.size()) {
                                            dialog.closeDialog();
                                            Toast.makeText(Show.this, "تم اضافة الملفات بنجاح", Toast.LENGTH_SHORT).show();
                                            SendMsg();
                                        }
                                        
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        dialog.closeDialog();
                                        Toast.makeText(Show.this, "حدث خطأ ما...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }*/

    private void SendMsg() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance()
                    .signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseMessaging.getInstance()
                                    .subscribeToTopic(TOPIC).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("TOPIC", "Subscribed");
                                            ApiUtils.getClient().sendNotification(new PushNotification(
                                                    new NotificationData("تم اضافة ملفات جديدة لشهادة", "تم اضافة ملفات جديدة لشهادة موجودة في التطبيق"),
                                                    "/topics/adminsTopic"
                                            )).enqueue(new Callback<PushNotification>() {
                                                @Override
                                                public void onResponse(Call<PushNotification> call, Response<PushNotification> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.d("Notification", "Sent Notification");

                                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                    } else {
                                                        Toast.makeText(Show.this, "فشل الارسال للمديرين", Toast.LENGTH_SHORT).show();
                                                        Log.e("Notification", "Failed Sent Notification");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                    Toast.makeText(Show.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                    Log.e("Notification", "Error sending");
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("TOPIC", "Failed");
                                            Log.d("TOPIC", e.getMessage().toString());
                                        }
                                    });
                        }
                    });


        } else {
            FirebaseMessaging.getInstance()
                    .getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("TOPIC", "mohamed Subscribed");
                                            ApiUtils.getClient().sendNotification(new PushNotification(
                                                            new NotificationData("تم اضافة ملفات جديدة لشهادة", "تم اضافة ملفات جديدة لشهادة موجودة في التطبيق")
                                                            , "/topics/adminsTopic"))
                                                    .enqueue(new Callback<PushNotification>() {
                                                        @Override
                                                        public void onResponse(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Response<com.mkandeel.gmarek.NotificationHandler.PushNotification> response) {
                                                            if (response.isSuccessful()) {
                                                                Log.d("Notification", "Notification send");
                                                                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                            } else {
                                                                Log.d("Notification", "Notification failed");
                                                                Toast.makeText(Show.this, "فشل الارسال للمديرين", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                            Toast.makeText(Show.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("TOPIC", "failed");
                                            Log.d("TOPIC", e.getMessage().toString());
                                        }
                                    });

                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Show.this, userOpt.class);
        startActivity(intent);
        finish();
    }
}