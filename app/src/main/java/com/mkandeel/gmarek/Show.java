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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mkandeel.gmarek.databinding.ActivityShowBinding;
import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.Modal;
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
    private LoadingDialog dialog;
    private List<Uri> mlist;
    private String mystr;
    private boolean isModel13;
    private List<String> urls = new ArrayList<>();
    private List<String> mUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            connection = DBConnection.getInstance(this);
            list = connection.getCustomCertData();
            dialog = new LoadingDialog(this);

            mlist = new ArrayList<>();

            adapter = new showAdapter(list, getApplicationContext());
            binding.rvCert.setAdapter(adapter);
            binding.rvCert.setLayoutManager(new LinearLayoutManager(this));
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
                                AddDataToCert(mystr, isModel13, true);
                            }
                        }
                    }
            );
            /////////////////////////////////////////////////////////////////////
            adapter.setOnClickListener(new showAdapter.ItemClicked() {
                @Override
                public void onItemClickListener(String str, int position) {
                    Certificate cert = connection.getCertData(str);
                    download_urls = connection.getFilesForCert(str);
                    Intent intent = new Intent(Show.this, DisplayCert.class);
                    intent.putExtra("cert_list", cert);
                    intent.putStringArrayListExtra("urls", download_urls);
                    intent.putExtra("num", str);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onItemLongClickListener(String str, int itemId) {
                    mystr = str;
                    if (itemId == R.id.add_13) {
                        Intent mIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        mIntent.setType("*/*");
                        mIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        arl.launch(mIntent);
                        isModel13 = true;
                    } else if (itemId == R.id.add_fact) {
                        Intent mIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        mIntent.setType("*/*");
                        mIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        arl.launch(mIntent);
                        isModel13 = false;
                    } else {
                        //connection.deleteCertificate(str);
                        //list = connection.getCustomCertData();
                        //adapter.notifyDataSetChanged();
                        deleteCertByNum(str);
                    }
                }
            });
        } else {
            Tools.showDialog(this);
        }
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
                        uploadExtrasToStorage(cert_num, mlist);
                    }
                });
    }

    private void deleteCertByNum(String cert_num) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Database")
                .child("Certificates");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        Modal modal = ds.getValue(Modal.class);
                        if (modal!=null) {
                            if (modal.getCert_num().equals(cert_num)) {
                                mUrls = modal.getList();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (mUrls.size() > 0) {
            for (String url:mUrls) {
                StorageReference sr = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(url);
                sr.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(cert_num).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Show.this, "تم حذف الشهادة بنجاح", Toast.LENGTH_SHORT).show();
                                    connection.deleteCertificate(cert_num);
                                    list = connection.getCustomCertData();
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });

                        //Toast.makeText(Show.this, "تم الحذف من ال storage", Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Show.this, "حدث خطأ", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }

    }

    private void uploadExtrasToStorage(String cert_num, List<Uri> list) {
        StorageReference sRef = FirebaseStorage.getInstance()
                .getReference("Certificates").child(cert_num);
        for (Uri uri : list) {
            StorageReference mRef = sRef.child(System.currentTimeMillis() +
                    "." + Tools.getFileExtn(getApplicationContext(), uri));
            mRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d("Upload", "Uploading successfull");
                        mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                urls.add(uri.toString());
                                //////////////////////////////////////////
                                DatabaseReference dbRef = FirebaseDatabase
                                        .getInstance().getReference("Database")
                                        .child("Certificates");
                                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                Modal modal = ds.getValue(Modal.class);
                                                if (modal != null) {
                                                    if (modal.getCert_num().equals(cert_num)) {
                                                        List<String> oldUrls = modal.getList();
                                                        urls = Tools.mergeLists(oldUrls, urls);
                                                        Modal model =
                                                                new Modal(modal.getUserKey(),
                                                                        modal.getCert_num(), modal.getCert_date(),
                                                                        modal.getComp_name(), modal.getComp_num(),
                                                                        modal.getCountry(), modal.getTrans(),
                                                                        modal.isModel_13(), modal.isFact(),
                                                                        modal.getOffers(), urls);

                                                        uploadDataToRTDB(model);
                                                    }
                                                } else {
                                                    Toast.makeText(Show.this, "null", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                /////////////////////////////////////////
                            }
                        });
                    }
                }
            });
        }
    }

    private void uploadDataToRTDB(Modal model) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Database").child("Certificates");
        reference.child(model.getCert_num()).setValue(model);
    }
}