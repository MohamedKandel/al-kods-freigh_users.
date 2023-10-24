package com.mkandeel.gmarek.rvAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mkandeel.gmarek.ClickListener;
import com.mkandeel.gmarek.R;
import com.mkandeel.gmarek.Show;
import com.mkandeel.gmarek.models.customModel;

import java.util.Collections;
import java.util.List;

public class showAdapter extends RecyclerView.Adapter<certHolder> {
    private List<customModel> list = Collections.emptyList();
    private Context context;
    private ClickListener listener;
    private ItemClicked clicked;
    public certHolder holder;

    public showAdapter(List<customModel> list,Context context) {
        this.list = list;
        this.context = context;
//        this.clicked = clicked;
        //this.listener = listener;
    }

    @NonNull
    @Override
    public certHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.show_cert,parent,false);
        return new certHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull certHolder holder, int position) {
        //int index = holder.getAdapterPosition();
        int index = position;
        holder.txt_num.setText(list.get(position).getCert_num());
        holder.txt_date.setText(list.get(position).getCert_date());
        holder.txt_trans.setText(list.get(position).getTrans());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clicked != null) {
                    clicked.onItemClickListener(holder.txt_num.getText().toString(),
                            index);
                }
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (clicked!=null) {
                            clicked.onItemLongClickListener(holder.txt_num.getText().toString(),
                                    item.getItemId(),index);
                        }
                        return true;
                    }
                });
                popupMenu.show();

                return true;
            }
        });

    }

    public void setOnClickListener(ItemClicked clicked) {
        this.clicked = clicked;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public interface ItemClicked {
        void onItemClickListener(String str,int position);
        void onItemLongClickListener(String str,int itemId,int index);
    }
}
