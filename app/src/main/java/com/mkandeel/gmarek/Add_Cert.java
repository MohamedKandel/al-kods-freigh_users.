package com.mkandeel.gmarek;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import com.mkandeel.gmarek.databinding.ActivityAddCertBinding;
import com.mkandeel.gmarek.models.Certificate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Add_Cert extends AppCompatActivity {

    private ActivityAddCertBinding binding;
    private List<String> list;

    private DBConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddCertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        list = new ArrayList<>();

        if (Tools.isNetworkAvailable(this)) {

            connection = DBConnection.getInstance(this);

            binding.txtCertNum.requestFocus();

            binding.btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cert_num = Objects.requireNonNull(binding.txtCertNum.getText()).toString();
                    String cert_date = Objects.requireNonNull(binding.txtCertDate.getText()).toString();
                    String cert_name = Objects.requireNonNull(binding.txtCertName.getText()).toString();
                    String comp_num = Objects.requireNonNull(binding.txtCompNum.getText()).toString();
                    String country = Objects.requireNonNull(binding.txtCountry.getText()).toString();
                    String trans = Objects.requireNonNull(binding.txtTrans.getText()).toString();
                    String offers = Objects.requireNonNull(binding.txtOffers.getText()).toString();
                    if (Tools.isEmpty(cert_num, cert_date, cert_name, comp_num, country,
                            trans, offers)) {
                        Toast.makeText(getApplicationContext(), "يجب عليك ملئ جميع الحفول", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        boolean model_13 = binding.chk13.isChecked();
                        boolean chk_fact = binding.chkFact.isChecked();
                        Certificate c = new Certificate(cert_num, cert_date,
                                cert_name, comp_num, country, trans, offers, model_13, chk_fact);

                        connection.insertIntoCertificates(cert_num, cert_date, cert_name,
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
                Intent intent = new Intent(Add_Cert.this,userOpt.class);
                startActivity(intent);
                finish();
            }
        });

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