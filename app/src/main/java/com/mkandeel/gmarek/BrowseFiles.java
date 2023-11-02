package com.mkandeel.gmarek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
import com.mkandeel.gmarek.Fragments.Frag_eight;
import com.mkandeel.gmarek.Fragments.Frag_eleven;
import com.mkandeel.gmarek.Fragments.Frag_five;
import com.mkandeel.gmarek.Fragments.Frag_four;
import com.mkandeel.gmarek.Fragments.Frag_nine;
import com.mkandeel.gmarek.Fragments.Frag_one;
import com.mkandeel.gmarek.Fragments.Frag_seven;
import com.mkandeel.gmarek.Fragments.Frag_six;
import com.mkandeel.gmarek.Fragments.Frag_ten;
import com.mkandeel.gmarek.Fragments.Frag_three;
import com.mkandeel.gmarek.Fragments.Frag_twelve;
import com.mkandeel.gmarek.Fragments.Frag_two;
import com.mkandeel.gmarek.NotificationHandler.ApiUtils;
import com.mkandeel.gmarek.NotificationHandler.NotificationData;
import com.mkandeel.gmarek.NotificationHandler.PushNotification;
import com.mkandeel.gmarek.databinding.ActivityBrowseFilesBinding;
import com.mkandeel.gmarek.models.Certificate;
import com.mkandeel.gmarek.models.Modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowseFiles extends AppCompatActivity implements Frag_one.FragmentInterActionListener,
        Frag_two.FragmentInterActionListener, Frag_three.FragmentInterActionListener,
        Frag_four.FragmentInterActionListener, Frag_five.FragmentInterActionListener,
        Frag_six.FragmentInterActionListener, Frag_seven.FragmentInterActionListener,
        Frag_eight.FragmentInterActionListener, Frag_nine.FragmentInterActionListener,
        Frag_ten.FragmentInterActionListener, Frag_eleven.FragmentInterActionListener,
        Frag_twelve.FragmentInterActionListener, onFileChoose {

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
    private List<Uri> listRelease;
    private List<Uri> listComp;
    private List<Uri> listBill;
    private List<Uri> listFoodHealth;
    private List<Uri> listAgriOffers;
    private List<Uri> listHealth;
    private boolean upload;
    private String TOPIC = "adminsTopic";
    private int i = 0;
    private Map<String, List<Uri>> listMap;
    private Map<Integer, String> fragmentsIndx;
    private LoadingDialog dialog;
    private String returned_num;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Tools.isNetworkAvailable(this)) {

            listMap = new LinkedHashMap<>();
            fragmentsIndx = new LinkedHashMap<>();
            FillMap();

            fragment_index = 1;
            upload = false;
            dialog = new LoadingDialog(this);
            listGomrok = new ArrayList<>();
            listAgri = new ArrayList<>();
            listFact = new ArrayList<>();
            listFloor = new ArrayList<>();
            listFood = new ArrayList<>();
            listHayaa = new ArrayList<>();

            listRelease = new ArrayList<>();
            listComp = new ArrayList<>();
            listAgriOffers = new ArrayList<>();
            listBill = new ArrayList<>();
            listHealth = new ArrayList<>();
            listFoodHealth = new ArrayList<>();

            connection = DBConnection.getInstance(this);

            sReference = FirebaseStorage.getInstance().getReference("Certificates");
            String UUID = connection.getUserID();
            mycertificate = getIntent().getParcelableExtra("cert_data");

            showFragment();

            binding.btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.btnNext.getText().equals("رفع الملفات")) {
                        uploadFilesToStorage(UUID,listMap);
                    } else {
                        if (fragment_index <= 11) {
                            fragment_index++;
                            String keyForAnother = fragmentsIndx.get(fragment_index);
                            if (listMap.get(keyForAnother) != null) {
                                int count = Objects.requireNonNull(listMap.get(keyForAnother)).size();
                                binding.txtChoosen.setText("تم اختيار " + count + " ملفات");
                            } else {
                                binding.txtChoosen.setText("");
                            }
                            showFragment();
                            upload = false;
                        } else {
                            binding.btnNext.setText("رفع الملفات");
                            upload = true;
                        }
                    }
                }
            });

            binding.btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fragment_index > 1) {
                        fragment_index--;
                        String keyForAnother = fragmentsIndx.get(fragment_index);
                        int count = listMap.get(keyForAnother).size();
                        //Toast.makeText(BrowseFiles.this, count+"", Toast.LENGTH_SHORT).show();
                        binding.txtChoosen.setText("تم اختيار " + count + " ملفات");
                        showFragment();
                        upload = false;
                    }
                }
            });

            binding.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (fragment_index) {
                        case 1:
                            listGomrok.clear();
                            listGomrok = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 2:
                            listFloor.clear();
                            listFloor = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 3:
                            listHayaa.clear();
                            listHayaa = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 4:
                            listFood.clear();
                            listFood = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 5:
                            listAgri.clear();
                            listAgri = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 6:
                            listFact.clear();
                            listFact = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 7:
                            listRelease.clear();
                            listRelease = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 8:
                            listComp.clear();
                            listComp = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 9:
                            listBill.clear();
                            listBill = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 10:
                            listFoodHealth.clear();
                            listFoodHealth = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 11:
                            listAgriOffers.clear();
                            listAgriOffers = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                        case 12:
                            listHealth.clear();
                            listHealth = new ArrayList<>();
                            Toast.makeText(BrowseFiles.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            binding.txtChoosen.setText("");
                            break;
                    }
                    showFragment();
                }
            });
        } else {
            Tools.showDialog(this);
        }

    }

    private void FillMap() {
        fragmentsIndx.put(1, "Gomrok");
        fragmentsIndx.put(2, "Floor");
        fragmentsIndx.put(3, "Hayaa");
        fragmentsIndx.put(4, "Food");
        fragmentsIndx.put(5, "Agri");
        fragmentsIndx.put(6, "Fact");
        fragmentsIndx.put(7, "Release");
        fragmentsIndx.put(8, "Comp");
        fragmentsIndx.put(9, "Bill");
        fragmentsIndx.put(10, "FoodHealth");
        fragmentsIndx.put(11, "AgriOffers");
        fragmentsIndx.put(12, "Health");
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
            case 7:
                frag = new Frag_seven();
                break;
            case 8:
                frag = new Frag_eight();
                break;
            case 9:
                frag = new Frag_nine();
                break;
            case 10:
                frag = new Frag_ten();
                break;
            case 11:
                frag = new Frag_eleven();
                break;
            case 12:
                frag = new Frag_twelve();
                break;
        }
        if (frag != null) {
            ft.replace(R.id.frm, frag);
            ft.commit();
        } else {
            binding.btnNext.setText("رفع الملفات");
            upload = true;
        }
    }

    @Override
    public void OnFragmentInterAction(List<Uri> list) {
        listGomrok = list;
        listMap.put("Gomrok", listGomrok);
    }

    @Override
    public void OnFragmentTwoInterAction(List<Uri> list) {
        listFloor = list;
        listMap.put("Floor", listFloor);
    }

    @Override
    public void OnFragmentThreeInterAction(List<Uri> list) {
        listHayaa = list;
        listMap.put("Hayaa", listHayaa);
    }

    @Override
    public void OnFragmentFourInterAction(List<Uri> list) {
        listFood = list;
        listMap.put("Food", listFood);
    }

    @Override
    public void OnFragmentFiveInterAction(List<Uri> list) {
        listAgri = list;
        listMap.put("Agri", listAgri);
    }

    @Override
    public void OnFragmentSixInterAction(List<Uri> list) {
        listFact = list;
        listMap.put("Fact", listFact);
    }

    @Override
    public void OnFragmentSevenInterAction(List<Uri> list) {
        listRelease = list;
        listMap.put("Release", listRelease);
    }

    @Override
    public void OnFragmentEightInterAction(List<Uri> list) {
        listComp = list;
        listMap.put("Comp", listComp);
    }

    @Override
    public void OnFragmentNineInterAction(List<Uri> list) {
        listBill = list;
        listMap.put("Bill", listBill);
    }

    @Override
    public void OnFragmentTenInterAction(List<Uri> list) {
        listFoodHealth = list;
        listMap.put("FoodHealth", listFoodHealth);
    }

    @Override
    public void OnFragmentElevenInterAction(List<Uri> list) {
        listAgriOffers = list;
        listMap.put("AgriOffers", listAgriOffers);
    }

    @Override
    public void OnFragmentTwelveInterAction(List<Uri> list) {
        listHealth = list;
        listMap.put("Health", listHealth);
    }

    private void uploadFilesToStorage(String userKey, Map<String, List<Uri>> map) {
        int k = 0;
        StorageReference mReference = sReference.child(mycertificate.getCert_num()
                + "/");
        Log.e("Sizes",map.size()+"");
        for (Map.Entry<String, List<Uri>> entry : map.entrySet()) {
            final int[] f = {0};
            for (Uri uri:entry.getValue()) {
                StorageReference sr = mReference.child(entry.getKey() + "/"
                        + System.currentTimeMillis() + "." +
                        Tools.getFileExtn(BrowseFiles.this, uri));
                int finalK = k;
                sr.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Modal model = new
                                Modal(userKey,mycertificate.getCert_num(),
                                mycertificate.getCert_date(),mycertificate.getComp_name(),
                                mycertificate.getComp_num(),mycertificate.getCountry(),
                                mycertificate.getTrans(),mycertificate.isModel_13(),
                                mycertificate.isChk_fact(),mycertificate.getOffers());
                        uploadDataToRTDB(model);
                        f[0]++;
                        Log.e("Sizes I and J", finalK +"\t"+ f[0]);
                        Log.e("Sizes List",entry.getValue().size()+"");
                        if (finalK == map.size()-1 && f[0] == entry.getValue().size()) {
                            SendMsg();
                            Toast.makeText(BrowseFiles.this, "تم رفع الملفات بنجاح", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            k++;
        }
    }

    private void uploadDataToRTDB(Modal model) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Database").child("Certificates");
        reference.child(model.getCert_num()).setValue(model);
    }

    @Override
    public void onFileChooseListener(int count) {
        binding.txtChoosen.setText("تم اختيار " + count + " ملفات");
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
                                            Log.d("TOPIC", "Subscribed");
                                            ApiUtils.getClient().sendNotification(new PushNotification(
                                                    new NotificationData("تم اضافة شهادة", "تم اضافة شهادة جديدة في التطبيق"),
                                                    "/topics/adminsTopic"
                                            )).enqueue(new Callback<PushNotification>() {
                                                @Override
                                                public void onResponse(Call<PushNotification> call, Response<com.mkandeel.gmarek.NotificationHandler.PushNotification> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.d("Notification", "Sent Notification");

                                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                    } else {
                                                        Toast.makeText(BrowseFiles.this, "فشل الارسال للمديرين", Toast.LENGTH_SHORT).show();
                                                        Log.e("Notification", "Failed Sent Notification");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                    Toast.makeText(BrowseFiles.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
                                                            new NotificationData("تم اضافة شهادة", "تم اضافة شهادة جديدة في التطبيق")
                                                            , "/topics/adminsTopic"))
                                                    .enqueue(new Callback<PushNotification>() {
                                                        @Override
                                                        public void onResponse(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Response<com.mkandeel.gmarek.NotificationHandler.PushNotification> response) {
                                                            if (response.isSuccessful()) {
                                                                Log.d("Notification", "Notification send");
                                                                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                            } else {
                                                                Log.d("Notification", "Notification failed");
                                                                Toast.makeText(BrowseFiles.this, "فشل الارسال للمديرين", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(BrowseFiles.this, userOpt.class);
        startActivity(intent);
        finish();
    }
}