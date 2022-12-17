package com.example.identify_object.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.identify_object.History.HistoryItem;
import com.example.identify_object.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.CreateViewHolder> {
    List<HistoryItem> ItemList;
    Context context;

    public HistoryAdapter(Context context){ this.context = context;}
    public void setData(List<HistoryItem> data) {
        this.ItemList = data;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public HistoryAdapter.CreateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recylerview, parent, false);
        return new CreateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.CreateViewHolder holder, int position) {
        HistoryItem historyItem = ItemList.get(position);
        if(historyItem == null) {
            return;
        }
        holder.name.setText(historyItem.getName());
        holder.imgObj.setImageResource(CreateImage());
        holder.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private int CreateImage() {
        return 1 ;
    }

    @Override
    public int getItemCount() {
        return ItemList == null ? 0 : ItemList.size();
    }

    public class CreateViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutItem;
        private TextView name;
        private ImageView imgObj;

        public CreateViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutItem = itemView.findViewById(R.id.history_item);
            name = itemView.findViewById(R.id.name);
            imgObj = itemView.findViewById(R.id.object_image_history);

        }
    }
}
