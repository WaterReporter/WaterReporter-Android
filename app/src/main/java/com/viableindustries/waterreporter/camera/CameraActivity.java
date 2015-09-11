package com.viableindustries.waterreporter.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.PhotoMetaActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brendanmcintyre on 8/31/15.
 */
public class CameraActivity extends Activity {

    @Bind(R.id.camera_preview)
    FrameLayout preview;

    @Bind(R.id.button_capture)
    Button captureButton;

//    @Bind(R.id.preview) ImageView mImageView;

    private android.hardware.Camera mCamera;

    private android.hardware.Camera.Parameters mParams;

    //    private Camera mCamera;
    private CameraPreview mPreview;

    private String mCurrentPhotoPath;

    private static final String CAMERA_DIR = "/dcim/";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_preview);

        ButterKnife.bind(this);

        // Create an instance of Camera
        mCamera = android.hardware.Camera.open();

        mCamera.setDisplayOrientation(90);

        // Camera params config
        mParams = mCamera.getParameters();

        // Set orientation to portrait
        mParams.set("orientation", "portrait");

        // Set size to the smallest supported size with width greater >= 1280
        android.hardware.Camera.Size size = getSupportedSize();

        Log.d("size", size.width + "x" + size.height);

        mParams.setPictureSize(size.width, size.height);

//        mParams.setPreviewSize(size.width, size.height);

        mCamera.setParameters(mParams);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        preview.addView(mPreview);

        // Add a listener to the Capture button

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // get an image from the camera

                        mCamera.takePicture(null, null, mPicture);

                    }
                }
        );

    }


//    private int findFrontFacingCamera() {
//
//        int id = -1;
//
//        int numCameras = android.hardware.Camera.getNumberOfCameras();
//
//        for (int i = 0; i < numCameras; i++) {
//
//            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//
//            android.hardware.Camera.getCameraInfo(i, info);
//
//            if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
//
//                id = i;
//
//                break;
//
//            }
//
//        }
//
//        return id;
//
//    }

    private android.hardware.Camera.Size getSupportedSize() {

        List<android.hardware.Camera.Size> supportedSizes = mParams.getSupportedPictureSizes();

        List<android.hardware.Camera.Size> targetSizes = new ArrayList<android.hardware.Camera.Size>();

        for (android.hardware.Camera.Size size : supportedSizes) {

            Log.d("supported size", size.height + "x" + size.width);

            if ((size.height >= 1280) && (size.width != size.height)) {

                targetSizes.add(size);

            }

        }

        Collections.sort(targetSizes, new Comparator<android.hardware.Camera.Size>() {

            public int compare(android.hardware.Camera.Size size1, android.hardware.Camera.Size size2) {

                return Double.compare(size1.width, size2.width);

            }

        });

        return targetSizes.get(0);

    }

    private android.hardware.Camera.PictureCallback mPicture = new android.hardware.Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

//            galleryAddPic();

            File pictureFile = getOutputMediaFile(1);

            if (pictureFile == null) {

                Log.d("error", "Error creating media file, check storage permissions");

                return;

            }

            try {

                FileOutputStream fos = new FileOutputStream(pictureFile);

                fos.write(data);

                fos.close();

            } catch (FileNotFoundException e) {

                Log.d("error", "File not found: " + e.getMessage());

            } catch (IOException e) {

                Log.d("error", "Error accessing file: " + e.getMessage());

            }

//            releaseCamera();

            startActivity(new Intent(getApplicationContext(), PhotoMetaActivity.class));

        }
    };

//    public void takePicture(View v) {
////        try {
////
////            mCamera.setPreviewDisplay(((SurfaceView) findViewById(R.id.surface_view)).getHolder());
////
////        } catch (IOException e) {
////
////            // TODO Auto-generated catch block
////            e.printStackTrace();
////
////        }
////
////        mCamera.startPreview();
//
//        android.hardware.Camera.Size size = getSupportedSize();
//
//        Log.d("size", size.width + "x" + size.height);
//
//        mParams.setPictureSize(size.width, size.height);
//
//        mCamera.takePicture(null, null, null);
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

//    public class PhotoHandler implements android.hardware.Camera.PictureCallback {
//
//        @Override
//        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            if (bitmap != null) {
//                Matrix matrix = new Matrix();
//                matrix.postRotate(270);
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                ImageView v = (ImageView) findViewById(R.id.thumbnail);
//                v.setImageBitmap(bitmap);
//            }
//        }
//    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WaterReporter");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }

        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;

        if (type == 1) {

            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

        } else {

            return null;

        }

        return mediaFile;

    }

    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");

        File f = new File(mCurrentPhotoPath);

        Uri contentUri = Uri.fromFile(f);

        mediaScanIntent.setData(contentUri);

        this.sendBroadcast(mediaScanIntent);

    }

    private File getAlbumDir() {

        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File (
                    Environment.getExternalStorageDirectory()
                            + CAMERA_DIR
                            + getString(R.string.album_name)
            );


            if (! storageDir.mkdirs()) {
                if (! storageDir.exists()){
                    return null;
                }
            }
        }

        return storageDir;

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

        File albumF = getAlbumDir();

        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

    }

    @Override
    protected void onPause() {
        super.onPause();

        ButterKnife.unbind(this);

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mPreview != null) {
            preview.removeView(mPreview);
            mPreview = null;
        }
//        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
//        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

}