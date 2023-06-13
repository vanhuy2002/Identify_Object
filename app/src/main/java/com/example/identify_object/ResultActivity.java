package com.example.identify_object;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.identify_object.Adapter.RecyclerViewAdapter;
import com.example.identify_object.History.HistoryItem;
import com.example.identify_object.utils.Draw;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ResultActivity extends AppCompatActivity {

    RelativeLayout parentView;
    ImageView img_result;
    ImageButton btn_back;
    Intent intent;
    Uri photoUri;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    String newName;
    List<String> list;
    String name =  "";
    OkHttpClient client = new OkHttpClient();

    public String url= "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=en&tl=vi&q=";

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private int check = 0;
    private final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        list = new ArrayList<>();
        btn_back = findViewById(R.id.btnBack);
        parentView = findViewById(R.id.parent_view);
        img_result = findViewById(R.id.imageview);
        recyclerView = findViewById(R.id.recycleView);
        setRecyclerView();
        setRecyclerAdapter();
        intent = getIntent();
        check = intent.getIntExtra("type",0);
        photoUri = Uri.parse(intent.getStringExtra("photoUri"));
        img_result.setImageURI(photoUri);
        try {
            bindPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_back.setOnClickListener(back -> {
            this.finish();
        });



    }



    private void setRecyclerAdapter() {
        adapter = new RecyclerViewAdapter(list, getApplicationContext());
        recyclerView.setAdapter(adapter);
    }

    private void setRecyclerView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }
    @SuppressLint("NotifyDataSetChanged")
    private void bindPreview() throws IOException {
        ObjectDetector objectDetector;
        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("object_detection.tflite").build();
        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(4)
                .build();
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
                objectDetector.process(inputImage)
                        .addOnSuccessListener(object -> {
                            for (DetectedObject i : object ){
                                String txt = "Undefined";
                                if (!i.getLabels().isEmpty())
                                    txt = i.getLabels().get(0).getText();

                                Draw draw = new Draw(this,i.getBoundingBox()
                                        ,txt);

                                String str = txt.replaceAll("\\s+","%20");
                                OkHttpHandler okHttpHandler= new OkHttpHandler();
                                okHttpHandler.execute(url+str);

                                name = txt;
                                 new Handler().postDelayed(() -> {
                                     if (!object.isEmpty()){
                                         list.add(newName + " \n(" + name + ")");
                                         adapter.notifyDataSetChanged();
                                         if (check != 1)
                                            uploadDB(bitmap, newName + " \n(" + name + ")");
                                     }
                                 },2000);



                                parentView.addView(draw);


                            }

                        })
                        .addOnFailureListener(b -> Log.e("MainActivity", "Error - " + b.getMessage()));


    }

    private void uploadDB(Bitmap bitmap, String txt) {
        HistoryItem item = new HistoryItem(txt);
        db.collection("Users").document(user.getUid()).collection("Items")
                .document(item.getId()).set(item);

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, item.getId(), "image");
        Uri uri = Uri.parse(path);
        StorageReference img = storageReference.child("Users/" + user.getUid() + "/" + item.getId() + ".jpg");
        img.putFile(uri).addOnCompleteListener(task -> {
            File fdelete = new File(getRealPathFromURI(this, uri));
            if (fdelete.exists()) {
                fdelete.delete();
            }
        });
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.MediaColumns.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    String doGetRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();

    }

    public class OkHttpHandler extends AsyncTask {

        OkHttpClient client = new OkHttpClient();


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Request.Builder builder = new Request.Builder();
            builder.url((String) objects[0]);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();

               String s = response.body().string();
               newName = s.substring(2,s.length() - 2);

               Log.e("MVH", newName);
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

}

