package com.example.identify_object.History;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.Target;
import com.example.identify_object.Adapter.HistoryAdapter;
import com.example.identify_object.OnClickItemInterface;
import com.example.identify_object.R;
import com.example.identify_object.ResultActivity;
import com.example.identify_object.iLoadImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements iLoadImage {
    RecyclerView recyclerView;
    HistoryAdapter adapter;
    List<HistoryItem> createList;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private RequestManager glideRequest;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        glideRequest = Glide.with(this);

        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new HistoryAdapter(getContext(), new OnClickItemInterface() {
            @Override
            public boolean itemClick(HistoryItem model, ImageView img) {
                Intent intent = new Intent(getContext(), ResultActivity.class);

                intent.putExtra("type", 1);
                intent.putExtra("photoUri", getUriFromImageView(img).toString());

                startActivity(intent);
                return true;
            }
            @Override
            public void deleteItem(HistoryItem model) {
                db.collection("Users").document(user.getUid()).collection("Items")
                        .document(model.getId()).delete();
                String path = "Users/" + user.getUid() + "/" + model.getId() + ".jpg";
                storageReference.child(path).delete();

            }
        }, this, user.getUid(), glideRequest);

        createList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        createList.clear();
        db.collection("Users").document(user.getUid()).collection("Items")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot itemSnap : task.getResult()){
                            HistoryItem item = itemSnap.toObject(HistoryItem.class);
                            createList.add(item);
                        }
                        adapter.setData(createList);
                    }
                });
    }

    public Uri getUriFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();

            return getImageUri(requireContext(), bitmap);
        }

        return null;
    }

    // Method to get the URI from a bitmap
    public Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    @Override
    public Target<Drawable> setImage(RequestManager glideRequest, HistoryAdapter.CreateViewHolder holder, Uri uri) {
        return glideRequest.load(uri)
                .placeholder(R.drawable.avt)
                .error(R.drawable.avt)
                .into(holder.imgObj);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            HistoryAdapter.CreateViewHolder holder = (HistoryAdapter.CreateViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder.glideTarget != null) {
                glideRequest.clear(holder.glideTarget);
            }
        }
    }
}
