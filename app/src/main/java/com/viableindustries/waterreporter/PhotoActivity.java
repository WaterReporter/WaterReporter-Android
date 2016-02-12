package com.viableindustries.waterreporter;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.progress.CountingTypedFile;
import com.viableindustries.waterreporter.progress.ProgressListener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;

/**
 * Created by Brendan McIntyre on 2015-09-01.
 * This activity handles all of the photo functionality.
 */

public class PhotoActivity extends AppCompatActivity
        implements PhotoPickerDialogFragment.PhotoPickerDialogListener {

    @Bind(R.id.parent_layout)
    RelativeLayout parentLayout;

    @Bind(R.id.button_capture)
    ImageButton captureButton;

    @Bind(R.id.preview)
    ImageView mImageView;

    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_SELECT_PHOTO = 2;

    private static final int PERMISSIONS_REQUEST_USE_CAMERA = 1;

    private static final int PERMISSIONS_REQUEST_USE_STORAGE = 2;

    protected boolean photoCaptured = false;

    private String mCurrentPhotoPath;
    private String newFilePath;

    protected Bitmap mImageBitmap;

    private static final String CAMERA_DIR = "/dcim/";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {

            mCurrentPhotoPath = savedInstanceState.getString("gallery_path");

            if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) photoCaptured = true;

            mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

            mImageView.setImageBitmap(mImageBitmap);

            mImageView.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.photo, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_save) {

            savePhoto();

        } else {

            startActivity(new Intent(this, MainActivity.class));

        }

        return super.onOptionsItemSelected(item);

    }

    protected boolean connectionActive() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();

    }

    private File getAlbumDir() {

        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File(
                    Environment.getExternalStorageDirectory()
                            + CAMERA_DIR
                            + getString(R.string.album_name)
            );


            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    return null;
                }
            }
        }

        return storageDir;

    }

    private File createImageFile(boolean temp) throws IOException {

        File outputDir;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

        if (temp) {

            outputDir = this.getCacheDir();

        } else {

            outputDir = getAlbumDir();

        }

        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, outputDir);

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;

        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {

                inSampleSize *= 2;

            }

        }

        Log.d("sampleSize", String.valueOf(inSampleSize));

        return inSampleSize;

    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {

        int dimension;

        //If the bitmap is wider than it is tall
        //use the height as the square crop dimension
        if (bitmap.getWidth() >= bitmap.getHeight()) {

            dimension = (1280 <= bitmap.getHeight()) ? 1280 : bitmap.getHeight();

        }
        //If the bitmap is taller than it is wide
        //use the width as the square crop dimension
        else {

            dimension = (1280 <= bitmap.getWidth()) ? 1280 : bitmap.getWidth();

        }

        return dimension;

    }

    public static Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);

    }

    protected Bitmap decodeSampledBitmapFromStream(Uri uri, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        Bitmap bitmap = null;

        ParcelFileDescriptor parcelFD = null;

        InputStream inputStream;

        try {

            parcelFD = getContentResolver().openFileDescriptor(uri, "r");

            if (parcelFD != null) {

                FileDescriptor imageSource = parcelFD.getFileDescriptor();

                options.inJustDecodeBounds = true;

                BitmapFactory.decodeFileDescriptor(imageSource, null, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;

                bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, options);

                //inputStream = this.getContentResolver().openInputStream(uri);

            }

        } catch (Exception e) {

            return null;

        } finally {

            if (parcelFD != null) {

                try {

                    parcelFD.close();

                    //return bitmap;

                } catch (IOException e) {

                    // ignored

                }

            }

        }

        return bitmap;

        // First decode with inJustDecodeBounds=true to check dimensions
        //final BitmapFactory.Options options = new BitmapFactory.Options();

//        options.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeFileDescriptor(inputStream, null, options);
//
//        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//
//        return BitmapFactory.decodeStream(inputStream, null, options);

    }

    private void setPic(String filePath) {

        Bitmap scaledBitmap = decodeSampledBitmapFromResource(filePath, 1080, 1080);

        //Log.d(null, filePath + " " + bitmap.getWidth() + " " + bitmap.getHeight());

        //Log.d(null, bitmap.getWidth() + " " + bitmap.getHeight());

        //int dimension = getSquareCropDimensionForBitmap(bitmap);

        //Bitmap scaledBitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);

//        int maxHeight = (1280 <= bitmap.getHeight()) ? 1280 : bitmap.getHeight();
//        int maxWidth = (1280 <= bitmap.getWidth()) ? 1280 : bitmap.getWidth();
//
//        float scale = Math.min(((float) maxHeight / bitmap.getWidth()), ((float) maxWidth / bitmap.getHeight()));
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(scale, scale);
//
//        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap,
//                0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //bitmap.recycle();

        try {

            // Create new image file and path reference

            File file = createImageFile(true);

            newFilePath = file.getAbsolutePath();

            FileOutputStream fOut = new FileOutputStream(file);

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            fOut.flush();

            fOut.close();

            // Create instances of ExifInterface for new and existing image files

            ExifInterface oldExif = new ExifInterface(filePath);

            ExifInterface newExif = new ExifInterface(newFilePath);

            String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);

            if (exifOrientation != null) {

                Log.d("orientation", exifOrientation);
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
                newExif.saveAttributes();

            }

            String exifDateTime = oldExif.getAttribute(ExifInterface.TAG_DATETIME);

            if (exifDateTime != null) {

                Log.d("iso", exifDateTime);
                newExif.setAttribute(ExifInterface.TAG_DATETIME, exifDateTime);
                newExif.saveAttributes();

            }

            String exifMake = oldExif.getAttribute(ExifInterface.TAG_MAKE);

            if (exifMake != null) {

                Log.d("make", exifMake);
                newExif.setAttribute(ExifInterface.TAG_MAKE, exifMake);
                newExif.saveAttributes();

            }

            String exifModel = oldExif.getAttribute(ExifInterface.TAG_MODEL);

            if (exifModel != null) {

                Log.d("model", exifModel);
                newExif.setAttribute(ExifInterface.TAG_MODEL, exifModel);
                newExif.saveAttributes();

            }

            String exifLatitude = oldExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

            if (exifLatitude != null) {

                Log.d("lat", exifLatitude);
                newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exifLatitude);
                newExif.saveAttributes();

            }

            // Don't forget to check for and retrieve the latitude ref ("S", "N")

            String exifLatitudeRef = oldExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);

            if (exifLatitudeRef != null) {

                Log.d("latRef", exifLatitudeRef);
                newExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, exifLatitudeRef);
                newExif.saveAttributes();

            }

            // Don't forget to check for and retrieve the longitude ref ("E", "W")

            String exifLongitude = oldExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            if (exifLongitude != null) {

                Log.d("long", exifLongitude);
                newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exifLongitude);
                newExif.saveAttributes();

            }

            String exifLongitudeRef = oldExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            if (exifLongitudeRef != null) {

                Log.d("longRef", exifLongitudeRef);
                newExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, exifLongitude);
                newExif.saveAttributes();

            }

        } catch (Exception e) {

            e.printStackTrace();

            Log.d(null, "Save file error!");

            newFilePath = null;

            return;

        }

        mImageView.setImageBitmap(scaledBitmap);

        mImageView.setVisibility(View.VISIBLE);

        photoCaptured = true;

    }

    private void galleryAddPic(String filePath) {

        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");

        File f = new File(filePath);

        Uri contentUri = Uri.fromFile(f);

        mediaScanIntent.setData(contentUri);

        this.sendBroadcast(mediaScanIntent);

    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {

            setPic(mCurrentPhotoPath);

        }

    }

    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);

        Cursor cursor = loader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String result = cursor.getString(column_index);

        cursor.close();

        return result;

    }

//    private void processGalleryPhoto(Intent returnedImageIntent) {
//
//        Uri selectedImage = returnedImageIntent.getData();
//
//        String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//        try {
//
//            ContentResolver contentResolver = getApplicationContext().getContentResolver();
//
//            Cursor cursor = contentResolver.query(
//                    selectedImage, filePathColumn, null, null, null);
//
//            cursor.moveToFirst();
//
//            if (cursor.moveToFirst()) {
//
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//
//                mCurrentPhotoPath = cursor.getString(columnIndex);
//
//                cursor.close();
//
//                setPic(mCurrentPhotoPath);
//
//            }
//
//        } catch (NullPointerException ne) {
//
//            Snackbar.make(parentLayout, "Unable to open image.",
//                    Snackbar.LENGTH_SHORT)
//                    .show();
//
//        }
//
//    }

    protected static int getImageProperty(Context context, Uri photoUri, String column) {
    /* it's on the external media. */
//        Cursor cursor = context.getContentResolver().query(photoUri,
//                new

        String[] proj = {MediaStore.Images.ImageColumns.ORIENTATION};

//        if (cursor.getCount() != 1) {
//            return -1;
//        }
//
//        cursor.moveToFirst();
//
//        return cursor.getInt(0);

        CursorLoader loader = new CursorLoader(context, photoUri, proj, null, null, null);

        Cursor cursor = loader.loadInBackground();

        if (cursor.getCount() != 1) {

            return -1;

        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

        cursor.moveToFirst();

        //String result = cursor.getString(column_index);

        cursor.close();

        return cursor.getInt(0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ACTION_TAKE_PHOTO:

                if (resultCode == RESULT_OK) {

                    //if (data != null) {

                    captureButton.setImageResource(R.drawable.ic_edit_white_24dp);

                    handleBigCameraPhoto();

                    //Uri selectedImageUri = data.getData();

                    //String filestring = selectedImageUri.getPath();

                    //setPic(filestring);

                    //}

                }

                break;

            case ACTION_SELECT_PHOTO:

                if (resultCode == RESULT_OK) {

                    if (data != null) {

                        captureButton.setImageResource(R.drawable.ic_edit_white_24dp);

                        Uri selectedImageUri = data.getData();

                        try {

                            Bitmap scaledBitmap = decodeSampledBitmapFromStream(selectedImageUri, 1080, 1080);

//                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
//
//                            int maxHeight = (1280 <= bitmap.getHeight()) ? 1280 : bitmap.getHeight();
//                            int maxWidth = (1280 <= bitmap.getWidth()) ? 1280 : bitmap.getWidth();
//
//                            float scale = 1;
//
//                            if (maxHeight >= 1280 && maxWidth >= 1280) {
//
//                                scale = Math.min(((float) maxHeight / bitmap.getWidth()), ((float) maxWidth / bitmap.getHeight()));
//
//                            }
//
//                            Matrix matrix = new Matrix();
//                            matrix.postScale(scale, scale);
//
//                            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap,
//                                    0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//
//                            int dimension = getSquareCropDimensionForBitmap(scaledBitmap);

                            //Bitmap resizedBitmap = ThumbnailUtils.extractThumbnail(scaledBitmap, dimension, dimension);

//                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
//                                    bitmap, 1280, 1280, false);

                            //bitmap.recycle();

                            try {

                                // Create new image file and path reference

                                File file = createImageFile(true);

                                newFilePath = file.getAbsolutePath();

                                FileOutputStream fOut = new FileOutputStream(file);

                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                                fOut.flush();

                                fOut.close();

                                // Create instances of ExifInterface for new and existing image files

                                ExifInterface oldExif = new ExifInterface(selectedImageUri.getPath());
//
                                ExifInterface newExif = new ExifInterface(newFilePath);
//
//                                int exifOrientation = getImageProperty(this, selectedImageUri, "orientation");
//
//                                if (exifOrientation > -1) {
//
//                                    Log.d("orientation", String.valueOf(exifOrientation));
//                                    newExif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(exifOrientation));
//                                    newExif.saveAttributes();
//
//                                }

                                String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);

                                if (exifOrientation != null) {

                                    Log.d("orientation", exifOrientation);
                                    newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
                                    newExif.saveAttributes();

                                }

                            } catch (Exception e) {

                                e.printStackTrace();

                                Log.d(null, "Save file error!");

                                newFilePath = null;

                                return;

                            }

                            mImageView.setImageBitmap(scaledBitmap);

//                            Picasso.with(this)
//                                    .load(new File (newFilePath))
//                                    .fit()
//                                    //.resize(50, 50)
//                                    //.centerCrop()
//                                    .into(mImageView);

                            mImageView.setVisibility(View.VISIBLE);

                            photoCaptured = true;

                        } catch (Exception e) {

                            Snackbar.make(parentLayout, "Unable to read image.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();

                        }

                        //String fileString = getRealPathFromURI(selectedImageUri);

                        //Log.d("path", fileString);

                        //setPic(fileString);

                    }

                }

                break;

        }
    }

    // Check storage permissions. If present, launch the photo picker dialog.

    public void checkStoragePermission(View v) {

        if (ContextCompat.checkSelfPermission(PhotoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(PhotoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(PhotoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(PhotoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                //Toast.makeText(PhotoActivity.this, "Water Reporter needs to read and write .", Toast.LENGTH_LONG).show();
                Snackbar.make(parentLayout, "Storage permission is needed to create and retrieve images.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(PhotoActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_USE_STORAGE);
                            }
                        })
                        .show();

            } else {

                // REQUEST PERMISSION
                // See: https://developer.android.com/reference/android/Manifest.permission.html#READ_EXTERNAL_STORAGE
                // Any app that declares the WRITE_EXTERNAL_STORAGE permission is implicitly granted permission to READ_EXTERNAL_STORAGE.

                ActivityCompat.requestPermissions(PhotoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_USE_STORAGE);

            }

        } else {

            showPhotoPickerDialog();

        }

    }

    protected void showPhotoPickerDialog() {

        DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

    }

    // Handle the permissions request response for Camera

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case PERMISSIONS_REQUEST_USE_CAMERA: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted.

                    try {

                        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), ACTION_TAKE_PHOTO);

                    } catch (Exception e) {

                        //Toast.makeText(PhotoActivity.this, "Can\'t connect to the camera. Please select an image from your photo gallery instead.", Toast.LENGTH_LONG).show();
                        Snackbar.make(parentLayout, "Can\'t connect to the camera. Please select an image from your photo gallery instead.",
                                Snackbar.LENGTH_SHORT)
                                .show();

                    }

                    // Continued problems and possible solutions - - permission is granted but still can't connect to camera

                    // https://code.google.com/p/android/issues/detail?id=192357
                    // E/Surface: getSlotFromBufferLocked: unknown buffer: 0xa00d04a0
                    // https://github.com/journeyapps/zxing-android-embedded/issues/89

                    // https://android.googlesource.com/platform/tools/emulator/+/android-6.0.1_r7

                } else {

                    // Permission denied. Disable the functionality that depends on this permission.

                    //Toast.makeText(PhotoActivity.this, "Camera access denied. Please select an image from your photo gallery instead.", Toast.LENGTH_LONG).show();
                    Snackbar.make(parentLayout, "Camera access denied. Please select an image from your photo gallery instead.",
                            Snackbar.LENGTH_SHORT)
                            .show();

                }

            }

            case PERMISSIONS_REQUEST_USE_STORAGE: {

                // If request is cancelled, the result arrays are empty.

                if (PermissionUtil.verifyPermissions(grantResults)) {

                    // Permission granted.

                    showPhotoPickerDialog();

                } else {

                    // Permission denied. Disable the functionality that depends on this permission.

                    //Toast.makeText(PhotoActivity.this, "Camera access denied. Please select an image from your photo gallery instead.", Toast.LENGTH_LONG).show();
                    Snackbar.make(parentLayout, "Storage access denied. Please enable this feature to submit reports.",
                            Snackbar.LENGTH_SHORT)
                            .show();

                }

            }

        }

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {

            File f = createImageFile(false);

            mCurrentPhotoPath = f.getAbsolutePath();

            Log.d("filepath", mCurrentPhotoPath);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        } catch (IOException e) {

            e.printStackTrace();

            mCurrentPhotoPath = null;

        }

        // For compatibility with Android 6.0 (Marshmallow, API 23), we need to check permissions before
        // dispatching takePictureIntent, otherwise the app will crash.

        if (ContextCompat.checkSelfPermission(PhotoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);

        } else {

            if (ActivityCompat.shouldShowRequestPermissionRationale(PhotoActivity.this, Manifest.permission.CAMERA)) {

                //Toast.makeText(PhotoActivity.this, "Water Reporter needs to access your camera.", Toast.LENGTH_LONG).show();
                Snackbar.make(parentLayout, "Camera permission is need to capture an image for your report.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(PhotoActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSIONS_REQUEST_USE_CAMERA);
                            }
                        })
                        .show();

            } else {

                //REQUEST PERMISSION

                ActivityCompat.requestPermissions(PhotoActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_USE_CAMERA);

            }

        }

    }

    protected void savePhoto() {

        // If we don't have a data connection, abort and send the user back to the main activity

        if (!connectionActive()) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't start your report. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            return;

        }

        if (photoCaptured) {

            Intent intent = new Intent(PhotoActivity.this, PhotoMetaActivity.class);

            // Pass the on-device file path with the intent
            intent.putExtra("image_path", newFilePath);

            startActivity(intent);

        } else {

            CharSequence text = "Please add a photo.";

            Toast toast = Toast.makeText(getBaseContext(), text,
                    Toast.LENGTH_SHORT);
            toast.show();

        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);

        photoPickerIntent.setType("image/*");

        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("image_path", newFilePath);

        outState.putString("gallery_path", mCurrentPhotoPath);

    }

    @Override
    public void onResume() {

        super.onResume();

        if (newFilePath != null && !newFilePath.isEmpty()) photoCaptured = true;

    }

}