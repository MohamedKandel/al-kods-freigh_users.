package com.mkandeel.gmarek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mkandeel.gmarek.Fragments.Frag_five;
import com.mkandeel.gmarek.Fragments.Frag_four;
import com.mkandeel.gmarek.Fragments.Frag_one;
import com.mkandeel.gmarek.Fragments.Frag_six;
import com.mkandeel.gmarek.Fragments.Frag_three;
import com.mkandeel.gmarek.Fragments.Frag_two;
import com.mkandeel.gmarek.NotificationHandler.ApiUtils;
import com.mkandeel.gmarek.NotificationHandler.PushNotification;
import com.mkandeel.gmarek.databinding.ActivityBrowseFilesBinding;
import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.Modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseFiles extends AppCompatActivity implements Frag_one.FragmentInterActionListener,
        Frag_two.FragmentInterActionListener, Frag_three.FragmentInterActionListener,
        Frag_four.FragmentInterActionListener, Frag_five.FragmentInterActionListener,
        Frag_six.FragmentInterActionListener {

    private ActivityBrowseFilesBinding binding;
    private StorageReference sReference;
    private DBConnection connection;
    private Certificate mycertificate;
    private List<String> urls;
    private int fragment_index;
    private List<Uri> listGomrok;
    private List<Uri> listFloor;
    private List<Uri> listHayaa;
    private List<Uri> listFood;
    private List<Uri> listAgri;
    private List<Uri> listFact;
    private boolean upload;
    private String TOPIC = "adminsTopic";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragment_index = 1;
        upload = false;
        listGomrok = new ArrayList<>();
        listAgri = new ArrayList<>();
        listFact = new ArrayList<>();
        listFloor = new ArrayList<>();
        listFood = new ArrayList<>();
        listHayaa = new ArrayList<>();

        connection = DBConnection.getInstance(this);

        sReference = FirebaseStorage.getInstance().getReference("Certificates");
        /////////////////////////////////////////////////////////////
        String UUID = connection.getUserID();
        // replace with fetching UUID from Local DB
        //FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //String UUID = mAuth.getCurrentUser().getUid();
        ////////////////////////////////////////////////////////////
        mycertificate = getIntent().getParcelableExtra("cert_data");

        showFragment();

        binding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!upload) {
                    fragment_index++;
                    showFragment();
                } else {

                    // upload Files to firebase
                    uploadDataToFirebase(UUID,MergeLists(listGomrok,
                            listFloor,listHayaa,listFood,
                            listAgri,listFact));

                }*/
                SendMsg();

            }
        });

        if (fragment_index == 5) {
            binding.btn.setText("رفع الملفات");
            upload = true;
        }

    }

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
                                            Log.d("TOPIC","Subscribed");
                                            ApiUtils.getClient().sendNotification(new PushNotification(
                                                    new com.mkandeel.gmarek.NotificationHandler.NotificationData("title", "body"),
                                                    "/topics/adminsTopic"
                                            )).enqueue(new Callback<PushNotification>() {
                                                @Override
                                                public void onResponse(Call<PushNotification> call, Response<com.mkandeel.gmarek.NotificationHandler.PushNotification> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.d("Notification","Sent Notification");

                                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                    } else {
                                                        Toast.makeText(BrowseFiles.this, "Failed to send to admin", Toast.LENGTH_SHORT).show();
                                                        Log.e("Notification","Failed Sent Notification");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                    Toast.makeText(BrowseFiles.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                    Log.e("Notification","Error sending");
                                                }
                                            });
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
                                            Log.d("TOPIC","mohamed Subscribed");
                                            ApiUtils.getClient().sendNotification(new com.mkandeel.gmarek.NotificationHandler.PushNotification(
                                                    new com.mkandeel.gmarek.NotificationHandler.NotificationData("title", "body"),
                                                    TOPIC
                                            )).enqueue(new Callback<com.mkandeel.gmarek.NotificationHandler.PushNotification>() {
                                                @Override
                                                public void onResponse(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Response<com.mkandeel.gmarek.NotificationHandler.PushNotification> response) {
                                                    if (response.isSuccessful()) {

                                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                    } else {
                                                        Toast.makeText(BrowseFiles.this, "Failed to send to admin", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                    Toast.makeText(BrowseFiles.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("TOPIC","failed");
                                            Log.d("TOPIC",e.getMessage().toString());
                                        }
                                    });

                        }
                    });
        }
    }

    private void showFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment frag = null;
        switch (fragment_index) {
            case 1:
                frag = new Frag_one();
                break;
            case 2:
                frag = new Frag_two();
                break;
            case 3:
                frag = new Frag_three();
                break;
            case 4:
                frag = new Frag_four();
                break;
            case 5:
                frag = new Frag_five();
                break;
            case 6:
                frag = new Frag_six();
                break;
        }
        if (frag != null) {
            ft.replace(R.id.frm, frag);
            ft.commit();
        } else {
            binding.btn.setText("رفع الملفات");
            upload = true;
        }
    }

    @Override
    public void OnFragmentInterAction(List<Uri> list) {
        listGomrok = list;
    }

    @Override
    public void OnFragmentTwoInterAction(List<Uri> list) {
        listFloor = list;
    }

    @Override
    public void OnFragmentThreeInterAction(List<Uri> list) {
        listHayaa = list;
    }

    @Override
    public void OnFragmentFourInterAction(List<Uri> list) {
        listFood = list;
    }

    @Override
    public void OnFragmentFiveInterAction(List<Uri> list) {
        listAgri = list;
    }

    @Override
    public void OnFragmentSixInterAction(List<Uri> list) {
        listFact = list;
    }

    private List<List<Uri>> MergeLists(List<Uri>... lists) {
        return new ArrayList<>(Arrays.asList(lists));
    }

    private String getFileExtn(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mtm = MimeTypeMap.getSingleton();
        return mtm.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadDataToFirebase(String userKey, List<List<Uri>> list) {

        StorageReference mReference = sReference.child(userKey + "/");
        urls = new ArrayList<>();
        int size = 0;
        for (List<Uri> uris : list) {
            size++;
            int finalI = size;
            for (int i = 0; i < uris.size(); i++) {
                StorageReference sr = mReference.child(System.currentTimeMillis() + "" +
                        "." + getFileExtn(uris.get(i)));

                sr.putFile(uris.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        urls.add(uri.toString());
                                        connection.insertIntoFiles(mycertificate.getCert_num(), uri.toString());
                                        Log.d("Inserting_cert_num", mycertificate.getCert_num());
                                        Log.d("Inserting_uri", uri.toString());

                                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                                .getReference("Database").child("Certificates");
                                        Modal modal = new Modal(userKey, mycertificate.getCert_num(),
                                                mycertificate.getCert_date(), mycertificate.getCert_name(),
                                                mycertificate.getComp_num(), mycertificate.getCountry(),
                                                mycertificate.getTrans(), mycertificate.isModel_13(),
                                                mycertificate.isChk_fact(), mycertificate.getOffers(), urls);
                                        reference.child(mycertificate.getCert_num()).setValue(modal);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),
                                                "فشل رفع الملف", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (finalI == list.get(list.size() - 1).size()) {
                                        Toast.makeText(getApplicationContext(),
                                                        "تم رفع الملفات بنجاح", Toast.LENGTH_SHORT)
                                                .show();
                                        SendMsg();
//                                        FirebaseMessaging.getInstance()
//                                                .subscribeToTopic(TOPIC)
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        if (task.isSuccessful()) {
//                                                            Log.d("Topic", "Subscribed");
//                                                            PushNotification pn = new PushNotification(new NotificationData("تم رفع شهادة جديدة", "تم رفع الشهادة"),
//                                                                    TOPIC);
//                                                            new Sending().sendNotification(pn);
//
//                                                            FirebaseMessaging.getInstance()
//                                                                    .unsubscribeFromTopic(TOPIC).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                        @Override
//                                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                                            if (task.isSuccessful()) {
//                                                                                Log.d("Topic", "unsubscribed");
//                                                                            }
//                                                                        }
//                                                                    });
//                                                        }
//                                                    }
//                                                });
                                    }
                                }
                            }
                        });
            }
        }
    }

}