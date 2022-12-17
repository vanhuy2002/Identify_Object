package com.example.identify_object;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.identify_object.utils.Draw;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.concurrent.ExecutionException;

public class ScanFragment extends Fragment {

    ImageView btn_gallery, btn_take, btn_flash;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageCapture imageCapture;
    private ObjectDetector objectDetector;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        btn_flash = view.findViewById(R.id.btn_flash);
        btn_gallery = view.findViewById(R.id.btn_gallary);
        btn_take = view.findViewById(R.id.btn_take);
        previewView = view.findViewById(R.id.preview);

        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();
                bindPreview(processCameraProvider, view);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));

        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("object_detection.tflite").build();
        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build();
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
        return view;
    }

    private void bindPreview(ProcessCameraProvider cameraProvider, View view) {
        //Build preview
        Preview preview = new Preview.Builder().build();
        //Build camera
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //Build image capture
        ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();
        //Build image analysis
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();


        //Set analyzer for image analysis
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getContext()), imageProxy ->{
            int rotation = imageProxy.getImageInfo().getRotationDegrees();
            @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();

            if (image != null){
                InputImage inputImage = InputImage.fromMediaImage(image,rotation);
                objectDetector.process(inputImage)
                        .addOnSuccessListener(object -> {
                            RelativeLayout relativeLayout = view.findViewById(R.id.parent_layout);
                            for (DetectedObject i : object ){
//                                if (relativeLayout.getChildCount() > 1) relativeLayout.removeViewAt(1);
//                                String txt = "Undefined";
//                                if (i.getLabels().size() != 0)
//                                    txt = i.getLabels().get(0).getText().toString();
//
//                                Draw draw = new Draw(getContext(),i.getBoundingBox()
//                                        ,txt);
//
//                                relativeLayout.addView(draw);
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
    private void takePhoto(){

    }
}
