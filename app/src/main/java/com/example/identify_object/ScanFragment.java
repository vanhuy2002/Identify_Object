package com.example.identify_object;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.identify_object.utils.Draw;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ScanFragment extends Fragment {

    ImageView btn_gallery, btn_take;
    CheckBox btn_flash;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    ImageCapture imageCapture;

    private ObjectDetector objectDetector;
    Camera camera;
    private static final int PERMISSION_REQUEST_CAMERA = 101;
    View view;
    Preview preview;
    CameraSelector cameraSelector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scan, container, false);

        btn_flash = view.findViewById(R.id.btn_flash_on);
        btn_gallery = view.findViewById(R.id.btn_gallary);
        btn_take = view.findViewById(R.id.btn_take);
        previewView = view.findViewById(R.id.preview);


        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            initializeCamera();
        }

//        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
//        //Build preview
//        Preview preview = new Preview.Builder().build();
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//        //Build camera
//        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();
//                imageCapture = new ImageCapture.Builder().build();
//                try {
//                    processCameraProvider.unbindAll();
//                    processCameraProvider.bindToLifecycle(requireActivity(), cameraSelector, preview, imageCapture);
//                }catch (Exception e) {
//                    Log.e("TAG", "Use case binding failed");
//                }
//                //bindPreview(processCameraProvider, view);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(getContext()));
//
        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("object_detection.tflite").build();
        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build();
        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);

        btn_take.setOnClickListener(take -> {
            try {
                capturePhoto();
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        scanByGalley();
        return view;
    }

    private void bindPreview(ProcessCameraProvider cameraProvider, View view) {
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
                            for (DetectedObject i : object ){
//                                if (relativeLayout.getChildCount() > 1) relativeLayout.removeViewAt(1);
                                String txt = "Undefined";
                                if (i.getLabels().size() != 0)
                                    txt = i.getLabels().get(0).getText().toString();

                                Draw draw = new Draw(getContext(),i.getBoundingBox()
                                        ,txt);

                                previewView.addView(draw);
                            }

                            imageProxy.close();
                        })
                        .addOnFailureListener(b -> {
                            Log.v("MainActivity", "Error - " + b.getMessage());
                            imageProxy.close();
                        });
            }

        });

       camera = cameraProvider.bindToLifecycle(getActivity(), cameraSelector, preview,
                imageCapture, imageAnalysis);
        flashSwitch(camera);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecuter());
        btn_flash.setChecked(false);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        }
    }
    private void capturePhoto() throws IOException, ExecutionException, InterruptedException {
        ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();
        bindPreview(processCameraProvider, view);
        File photoFile = createImageFile();
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                getExecuter(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Intent intent = new Intent(getContext(), ResultActivity.class);
                        Uri contentUri = Uri.fromFile(photoFile);
                        intent.putExtra("photoUri", contentUri.toString());
                        Log.e("PathMVH", contentUri.getPath());
                        startActivity(intent);

                        Toast.makeText(getContext(), "Photo has been saved successfully", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
    private File createImageFile() throws IOException {
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //        currentPhotoPath = image.getAbsolutePath();
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }
    private Executor getExecuter() {
        return ContextCompat.getMainExecutor(getContext());
    }
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
//Camera Selector use case
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
//Preview use case
        preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

//ImageCapture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private void scanByGalley(){
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                btn_gallery.setEnabled(false);
                btn_flash.setChecked(false);
                startActivityForResult(intent, 102);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 102:
                btn_gallery.setEnabled(true);
                if(data == null || data.getData() == null) {
                    return;
                }

                Uri uri = data.getData();
                InputStream inputStream = null;

                try {
                    inputStream = getActivity().getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
                break;
            case 103:
                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    initializeCamera();
                break;

        }
    }

    private void flashSwitch(Camera camera) {
        camera.getCameraControl().enableTorch(false);
        btn_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_flash.isChecked()) {
                    camera.getCameraControl().enableTorch(true);
                    btn_flash.setBackgroundResource(R.drawable.ic_flash);
                } else {
                    camera.getCameraControl().enableTorch(false);
                    btn_flash.setBackgroundResource(R.drawable.ic_flash_off);
                }
            }
        });
    }

}
