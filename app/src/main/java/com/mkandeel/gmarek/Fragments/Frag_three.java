package com.mkandeel.gmarek.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mkandeel.gmarek.R;
import com.mkandeel.gmarek.onFileChoose;

import java.util.ArrayList;
import java.util.List;

public class Frag_three extends Fragment {

    private FragmentInterActionListener listener;
    private List<Uri> listHayaa;
    private int count;
    private onFileChoose choose;

    public Frag_three() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listHayaa = new ArrayList<>();
        count = 0;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInterActionListener) {
            listener = (FragmentInterActionListener) context;
        } else {
            Toast.makeText(context,"Error in casting object",
                    Toast.LENGTH_SHORT).show();
        }
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
            try {
                choose = (onFileChoose) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_frag_one, container, false);
        TextView txt = view.findViewById(R.id.txt_num);
        //TextView choosen = view.findViewById(R.id.txt_choosen);
        LinearLayout layout = view.findViewById(R.id.layout);


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
                                for (int i=0;i<result.getData().getClipData().getItemCount();i++) {
                                    Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                                    listHayaa.add(uri);
                                    count ++;
                                }
                            } else {
                                Uri uri = result.getData().getData();
                                listHayaa.add(uri);
                                count++;
                            }
                            choose.onFileChooseListener(count);
                            //choosen.setText("تم اختيار "+count + " ملفات");
                        }
                    }
                }
        );


        txt.setText(R.string.txt_hayaa);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(Intent.ACTION_GET_CONTENT);
                mIntent.setType("*/*");
                mIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                arl.launch(mIntent);

                listener.OnFragmentThreeInterAction(listHayaa);
            }
        });


        return view;
    }

    public interface FragmentInterActionListener {
        void OnFragmentThreeInterAction(List<Uri> list);
    }

}