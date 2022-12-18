package com.example.identify_object.Adapter;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.identify_object.R;

import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    public List<String> list;
    TextToSpeech textToSpeech;
    Context context;
    TextToSpeech tts;

    public RecyclerViewAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String name = list.get(position);
        holder.tv_name.setText(name);
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

        holder.btn_sound.setOnClickListener(click -> {
            String toSpeak = holder.tv_name.getText().toString();

            String vn = toSpeak.substring(0,toSpeak.indexOf("("));
            String en = toSpeak.substring(toSpeak.indexOf("("),toSpeak.indexOf(")"));
            Toast.makeText(context, toSpeak,Toast.LENGTH_SHORT).show();
            tts.speak(vn,TextToSpeech.QUEUE_FLUSH,null);
            textToSpeech.speak(en, TextToSpeech.QUEUE_FLUSH, null);
        });



    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tv_name;
        AppCompatImageButton btn_sound;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.name_result);
            btn_sound = itemView.findViewById(R.id.btn_sound_result);
        }
    }
}
