package com.example.identify_object;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Size;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.identify_object.utils.Draw;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ObjectDetector objectDetector;
    public PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    public static BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager2 = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_menu);
        bottomNavigationView.setItemIconTintList(null);
        ViewPagerBottomNavigationAdapter viewPagerAdapter = new ViewPagerBottomNavigationAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setUserInputEnabled(false);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.scan) {
                viewPager2.setCurrentItem(0, false);
            } else if (id == R.id.history) {
                viewPager2.setCurrentItem(1, false);
            } else if (id == R.id.study){
                viewPager2.setCurrentItem(2, false);
            }
            return true;
        });

        registerOnPage();


        previewView = findViewById(R.id.preview);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();
                bindPreview(processCameraProvider);
                Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("object_detection.tflite").build();
        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build();
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        //Build preview
        Preview preview = new Preview.Builder().build();
        //Build camera
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Build image capture
        //ImageCapture imageCapture = new ImageCapture.Builder().build();
        //Build image analysis
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        //Set analyzer for image analysis
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy ->{
            int rotation = imageProxy.getImageInfo().getRotationDegrees();
            @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();

            if (image != null){
                InputImage inputImage = InputImage.fromMediaImage(image,rotation);
                objectDetector.process(inputImage)
                        .addOnSuccessListener(object -> {
                            RelativeLayout relativeLayout = findViewById(R.id.parent_layout);
                            for (DetectedObject i : object ){
                                if (relativeLayout.getChildCount() > 1) relativeLayout.removeViewAt(1);
                                String txt = "Undefined";
                                if (i.getLabels().size() != 0)
                                    txt = i.getLabels().get(0).getText().toString();

                                Draw draw = new Draw(this,i.getBoundingBox()
                                        ,txt);

                                relativeLayout.addView(draw);
                            }

                            imageProxy.close();
                        })
                        .addOnFailureListener(b -> {
                            Log.v("MainActivity", "Error - " + b.getMessage());
                            imageProxy.close();
                        });
            }

        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis,preview);
    }
    public void registerOnPage(){
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.scan).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.history).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.study).setChecked(true);
                        break;
                }
            }
        });
    }
}