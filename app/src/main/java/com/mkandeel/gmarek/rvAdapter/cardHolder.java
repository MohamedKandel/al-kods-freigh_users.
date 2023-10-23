package com.mkandeel.gmarek.rvAdapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mkandeel.gmarek.R;

public class cardHolder extends RecyclerView.ViewHolder {

    TextView txt;
    ImageView img;

    View view;

    public cardHolder(@NonNull View itemView) {
        super(itemView);

        txt = itemView.findViewById(R.id.txt_num);
        img = itemView.findViewById(R.id.img);

        view = itemView;
    }
}
