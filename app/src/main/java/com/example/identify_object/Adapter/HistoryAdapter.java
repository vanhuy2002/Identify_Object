package com.example.identify_object.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;
import com.example.identify_object.History.HistoryItem;
import com.example.identify_object.OnClickItemInterface;
import com.example.identify_object.R;
import com.example.identify_object.ResultActivity;
import com.example.identify_object.iLoadImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.CreateViewHolder> {
    List<HistoryItem> ItemList;

    public String url= "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=en&tl=vi&q=";

    Context context;
    TextToSpeech tts;
    TextToSpeech textToSpeech;
    String uid;
    OnClickItemInterface onClick;
    iLoadImage iLoadImage;
    private RequestManager glideRequest;


    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();


    public HistoryAdapter(Context context, OnClickItemInterface onClick, iLoadImage iLoadImage, String uid, RequestManager glideRequest){
        this.context = context;
        this.onClick = onClick;
        this.iLoadImage = iLoadImage;
        this.uid = uid;
        this.glideRequest = glideRequest;
    }
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
        tts = new TextToSpeech(context, status -> {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(new Locale("vi","VN"));
            }
        });
        textToSpeech = new TextToSpeech(context, status -> {
            if(status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
        holder.layoutItem.setOnClickListener(v -> onClick.itemClick(historyItem, holder.imgObj));
        holder.btnSound.setOnClickListener(click -> {
            String toSpeak = holder.name.getText().toString();


            String vn = toSpeak.substring(0,toSpeak.indexOf("("));
            String en = toSpeak.substring(toSpeak.indexOf("("),toSpeak.indexOf(")"));
            Toast.makeText(context, toSpeak,Toast.LENGTH_SHORT).show();
            tts.speak(vn,TextToSpeech.QUEUE_FLUSH,null);
            textToSpeech.speak(en, TextToSpeech.QUEUE_FLUSH, null);
        });

        holder.imgObj.setImageResource(R.drawable.avt);

        String path = "Users/" + uid + "/" + historyItem.getId() + ".jpg";
        storageReference.child(path).getDownloadUrl()
                .addOnSuccessListener(uri -> holder.glideTarget = iLoadImage.setImage(glideRequest, holder, uri))
                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()){
//                        holder.btnEdit.setOnClickListener(view -> iOnClickStudent.onClickPopMenu(holder, studentModel, position,
//                                task.getResult()));
//                        holder.avatar.setOnClickListener(view -> iOnClickStudent.onClickAvatar(task.getResult(), studentModel));
//                    }
//                    else {
//                        holder.btnEdit.setOnClickListener(view -> iOnClickStudent.onClickPopMenu(holder, studentModel, position,
//                                null));
//                        holder.avatar.setOnClickListener(view -> iOnClickStudent.onClickAvatar(null, studentModel));
//                    }

                });
        holder.btnDelete.setOnClickListener(view -> {
            onClick.deleteItem(historyItem);
            ItemList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        });

    }



    @Override
    public int getItemCount() {
        return ItemList == null ? 0 : ItemList.size();
    }

    public class CreateViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutItem;
        private TextView name;
        public ImageView imgObj;
        private AppCompatImageButton btnSound, btnDelete;
        public Target<Drawable> glideTarget;

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
