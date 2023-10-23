package com.mkandeel.gmarek;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.mkandeel.gmarek.databinding.ActivityInActiveAccBinding;

public class InActiveAcc extends AppCompatActivity {

    private ActivityInActiveAccBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInActiveAccBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView2.setText(getString(R.string.sorryText) +"\n"+
                getString(R.string.securityTxt) +"\n"+
                getString(R.string.solveTxt));
    }
}