package com.mkandeel.gmarek;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
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
import com.mkandeel.gmarek.NotificationHandler.ApiUtils;
import com.mkandeel.gmarek.NotificationHandler.NotificationData;
import com.mkandeel.gmarek.NotificationHandler.PushNotification;
import com.mkandeel.gmarek.databinding.ActivityAddCertBinding;
import com.mkandeel.gmarek.models.Certificate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Add_Cert extends AppCompatActivity {

    private ActivityAddCertBinding binding;
    private List<String> list;

    private DBConnection connection;
    private final String TOPIC = "adminsTopic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddCertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        list = new ArrayList<>();

        if (Tools.isNetworkAvailable(this)) {

            connection = DBConnection.getInstance(this);

            binding.txtCertNum.requestFocus();


            binding.btnUploadWithoutFiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userKey = connection.getUserID();
                    String cert_num = Objects.requireNonNull(binding.txtCertNum.getText()).toString();
                    String cert_date = Objects.requireNonNull(binding.txtCertDate.getText()).toString();
                    String comp_name = Objects.requireNonNull(binding.txtCertName.getText()).toString();
                    String comp_num = Objects.requireNonNull(binding.txtCompNum.getText()).toString();
                    String country = Objects.requireNonNull(binding.txtCountry.getText()).toString();
                    String trans = Objects.requireNonNull(binding.txtTrans.getText()).toString();
                    String offers = Objects.requireNonNull(binding.txtOffers.getText()).toString();
                    if (Tools.isEmpty(cert_num, cert_date, comp_name, comp_num, country,
                            trans, offers)) {
                        Toast.makeText(getApplicationContext(), "يجب عليك ملئ جميع الحفول", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        boolean model_13 = binding.chk13.isChecked();
                        boolean chk_fact = binding.chkFact.isChecked();
                        Certificate c = new Certificate(userKey,cert_num, cert_date,
                                comp_name, comp_num, country, trans, offers, model_13, chk_fact);
                        uploadCertificate(c);

                        connection.insertIntoCertificates(cert_num, cert_date, comp_name,
                                comp_num, country, trans, offers, Tools.IntegerParseInt(model_13),
                                Tools.IntegerParseInt(chk_fact));
                    }
                }
            });

            binding.btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userKey = connection.getUserID();
                    String cert_num = Objects.requireNonNull(binding.txtCertNum.getText()).toString();
                    String cert_date = Objects.requireNonNull(binding.txtCertDate.getText()).toString();
                    String comp_name = Objects.requireNonNull(binding.txtCertName.getText()).toString();
                    String comp_num = Objects.requireNonNull(binding.txtCompNum.getText()).toString();
                    String country = Objects.requireNonNull(binding.txtCountry.getText()).toString();
                    String trans = Objects.requireNonNull(binding.txtTrans.getText()).toString();
                    String offers = Objects.requireNonNull(binding.txtOffers.getText()).toString();
                    if (Tools.isEmpty(cert_num, cert_date, comp_name, comp_num, country,
                            trans, offers)) {
                        Toast.makeText(getApplicationContext(), "يجب عليك ملئ جميع الحفول", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        boolean model_13 = binding.chk13.isChecked();
                        boolean chk_fact = binding.chkFact.isChecked();
                        Certificate c = new Certificate(userKey,cert_num, cert_date,
                                comp_name, comp_num, country, trans, offers, model_13, chk_fact);

                        connection.insertIntoCertificates(cert_num, cert_date, comp_name,
                                comp_num, country, trans, offers, Tools.IntegerParseInt(model_13),
                                Tools.IntegerParseInt(chk_fact));

                    /*list.add(cert_num);
                    list.add(cert_date);
                    list.add(cert_name);
                    list.add(comp_num);
                    list.add(country);
                    list.add(trans);
                    list.add(offers);*/

                        Intent intent = new Intent(Add_Cert.this, BrowseFiles.class);
                        //pass values from this activity to next activity
                        /////////////////////////////////////////////////
                        intent.putExtra("cert_data", c);
                        startActivity(intent);
                        finish();
                    }
                }
            });

            binding.layoutDate.setStartIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayDatePicker();
                }
            });

            binding.txtCertDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.txtCertDate.getWindowToken(), 0);
                    displayDatePicker();
                }
            });
        } else {
            Tools.showDialog(this);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Add_Cert.this, userOpt.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void uploadCertificate(Certificate certificate) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Database").child("Certificates");
        reference.child(certificate.getCert_num()).setValue(certificate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            SendMsg();
                        }
                    }
                });
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
                                                public void onResponse(Call<PushNotification> call, Response<PushNotification> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.d("Notification", "Sent Notification");

                                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                                                    } else {
                                                        Toast.makeText(Add_Cert.this, "فشل الارسال للمديرين", Toast.LENGTH_SHORT).show();
                                                        Log.e("Notification", "Failed Sent Notification");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                    Toast.makeText(Add_Cert.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
                                                                Toast.makeText(Add_Cert.this, "فشل الارسال للمديرين", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<com.mkandeel.gmarek.NotificationHandler.PushNotification> call, Throwable t) {
                                                            Toast.makeText(Add_Cert.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();
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

    private void displayDatePicker() {
        final Calendar c = Calendar.getInstance();

        // on below line we are getting
        // our day, month and year.
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // on below line we are creating a variable for date picker dialog.
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                // on below line we are passing context.
                Add_Cert.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        binding.txtCertDate.
                                setText(dayOfMonth + " - " + (monthOfYear + 1) + " - " + year);

                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}