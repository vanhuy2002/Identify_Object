package com.example.identify_object.Adapter;

import android.content.Context;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.identify_object.History.HistoryItem;
import com.example.identify_object.R;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.CreateViewHolder> {
    List<HistoryItem> ItemList;
    Context context;
    TextToSpeech tts;
    TextToSpeech textToSpeech;

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
        holder.imgObj.setImageURI(Uri.parse(historyItem.getImageResult()));
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("vi","VN"));
                }
            }
        });
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
        holder.btnSound.setOnClickListener(click -> {
            String toSpeak = holder.name.getText().toString();
            String vn = toSpeak.substring(0,toSpeak.indexOf("("));
            String en = toSpeak.substring(toSpeak.indexOf("("),toSpeak.indexOf(")"));
            Toast.makeText(context, toSpeak,Toast.LENGTH_SHORT).show();
            tts.speak(vn,TextToSpeech.QUEUE_FLUSH,null);
            textToSpeech.speak(en, TextToSpeech.QUEUE_FLUSH, null);
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });

    }



    @Override
    public int getItemCount() {
        return ItemList == null ? 0 : ItemList.size();
    }

    public class CreateViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutItem;
        private TextView name;
        private ImageView imgObj;
        private AppCompatImageButton btnSound, btnDelete;

        public CreateViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutItem = itemView.findViewById(R.id.history_item);
            name = itemView.findViewById(R.id.name);
            imgObj = itemView.findViewById(R.id.object_image_history);
            btnSound = itemView.findViewById(R.id.btn_sound);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
