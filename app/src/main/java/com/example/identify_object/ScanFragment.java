package com.example.identify_object;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.graphics.BitmapFactory;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.provider.MediaStore;

import android.speech.tts.TextToSpeech;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    RelativeLayout root;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scan, container, false);

        root = view.findViewById(R.id.root_view);
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


    @Override
    public void onPause() {
//        ProcessCameraProvider processCameraProvider = null;
//        try {
//            processCameraProvider = cameraProviderFuture.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        assert processCameraProvider != null;
//        processCameraProvider.unbindAll();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (cameraProviderFuture != null){
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            }



        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
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

    public static Bitmap loadBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        }

    }
    @SuppressLint("RestrictedApi")
    private void capturePhoto() throws IOException, ExecutionException, InterruptedException {
        //ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();
        //bindPreview(processCameraProvider, view);
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
//                        Bitmap bitmap = null;
//                        try {
//                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), contentUri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        View view = new View(getContext());
//                        Canvas canvas = new Canvas(bitmap);
//                        view.draw(canvas);
//
//                        ObjectDetector objectDetector;
//                        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("object_detection.tflite").build();
//                        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
//                                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
//                                .enableClassification()
//                                .setClassificationConfidenceThreshold(0.5f)
//                                .setMaxPerObjectLabelCount(4)
//                                .build();
//                        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
//                        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
//                        objectDetector.process(inputImage)
//                                .addOnSuccessListener(object -> {
//                                    Log.e("MVH", object.size() + "");
//                                    for (DetectedObject i : object ){
////                                if (relativeLayout.getChildCount() > 1) relativeLayout.removeViewAt(1);
//                                        Log.e("MVH", "bindPreview for nè");
//                                        String txt = "Undefined";
//                                        if (i.getLabels().size() != 0)
//                                            txt = i.getLabels().get(0).getText().toString();
//
//                                        Draw draw = new Draw(getContext(),i.getBoundingBox()
//                                                ,txt);
//                                        Log.e("MVH", i.getBoundingBox().top + "|" + i.getBoundingBox().bottom + "|" + i.getBoundingBox().left + "|" + i.getBoundingBox().right);
//
//                                    }
//
//                                })
//                                .addOnFailureListener(b -> {
//                                    Log.e("MainActivity", "Error - " + b.getMessage());
//                                });
//
//                        try {
//                            OutputStream fOut = new FileOutputStream(createImageFile());
//                            Bitmap bitmap1 = loadBitmapFromView(root.getRootView());
//                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
//                            fOut.flush();
//                            fOut.close();
////                            MediaStore.Images.Media.insertImage(getContext().getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }


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
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
        flashSwitch(camera);
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
                Intent intent = new Intent(getContext(),ResultActivity.class);
                intent.putExtra("photoUri", uri.toString());
                startActivity(intent);
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
