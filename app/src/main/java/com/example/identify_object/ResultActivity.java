package com.example.identify_object;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.example.identify_object.Database.CreateDatabase;
import com.example.identify_object.History.HistoryItem;
import com.example.identify_object.utils.Draw;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        list = new ArrayList<>();
        btn_back = findViewById(R.id.btnBack);
        parentView = findViewById(R.id.parent_view);
        img_result = findViewById(R.id.imageview);
        recyclerView = findViewById(R.id.recycleView);
        setRecyclerView();
        setRecyclerAdapter();
        intent = getIntent();
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
//                                if (relativeLayout.getChildCount() > 1) relativeLayout.removeViewAt(1);

                                String txt = "Undefined";
                                if (i.getLabels().size() != 0)
                                    txt = i.getLabels().get(0).getText().toString();

                                Draw draw = new Draw(this,i.getBoundingBox()
                                        ,txt);

                                String str = txt.replaceAll("\\s+","");
                                OkHttpHandler okHttpHandler= new OkHttpHandler();
                                okHttpHandler.execute(url+str);

                                name = txt;
                                 new Handler().postDelayed(new Runnable() {
                                     @Override
                                     public void run() {
                                         if (object.size()!=0){
                                             list.add(newName + " \n(" + name + ")");
                                             adapter.notifyDataSetChanged();
                                             CreateDatabase.getInstance(ResultActivity.this).createItemDAO().insertItem(new HistoryItem(newName + " (" + name + ")" , photoUri.toString() ));

                                         }
                                     }
                                 },2000);



                                parentView.addView(draw);
                            }

                        })
                        .addOnFailureListener(b -> {
                            Log.e("MainActivity", "Error - " + b.getMessage());
                        });


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

