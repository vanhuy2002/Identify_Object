package com.example.identify_object;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.identify_object.Adapter.RecyclerViewAdapter;
import com.example.identify_object.utils.Draw;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    RelativeLayout parentView;
    ImageView img_result;
    ImageButton btn_back;
    Intent intent;
    Uri photoUri;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    List<String> list;



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

                                String arr = getTextFromWeb("https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=en&tl=vi&q=" + txt);

                                Log.e("MVH", arr);

                                if (object.size()!=0) {
                                    list.add(txt);
                                    adapter.notifyDataSetChanged();
                                }
                                parentView.addView(draw);
                            }

                        })
                        .addOnFailureListener(b -> {
                            Log.e("MainActivity", "Error - " + b.getMessage());
                        });


    }

    public String getTextFromWeb(String urlString)
    {
        WebView webView = new WebView(this);
        webView.loadUrl(urlString);
        Log.e("MVH", "AA" + webView.getTitle());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.e("MVH", "AA");
            }
        });

        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        //Get the text file
        File file = new File(sdcard,"json.txt");
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        //file.delete();
        return text.toString();
    }

}

