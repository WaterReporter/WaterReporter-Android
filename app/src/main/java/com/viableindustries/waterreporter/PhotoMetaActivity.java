package com.viableindustries.waterreporter;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Submission;

import java.io.File;
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
 * Created by Ryan Hamley on 10/28/14.
 * This activity handles all of the report functionality.
 */
public class PhotoMetaActivity extends AppCompatActivity
        implements PhotoPickerDialogFragment.PhotoPickerDialogListener {

    @Bind(R.id.date)
    EditText dateField;

    @Bind(R.id.comments)
    EditText commentsField;

    @Bind(R.id.preview)
    ImageView mImageView;

    @Bind(R.id.static_map)
    ImageView staticMap;

    @Bind(R.id.location_button)
    Button locationButton;

    private static final int ACTION_SET_LOCATION = 0;
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_SELECT_PHOTO = 2;

    private String mCurrentPhotoPath;

    private int mImageId;

    private Bitmap mImageBitmap;

    private static final String CAMERA_DIR = "/dcim/";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private LatLng location;

    private String dateText;

    private String commentsText;

    protected double latitude;

    protected double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_metadata);

        ButterKnife.bind(this);

        initializeDateField();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            mCurrentPhotoPath = extras.getString("image_path", "");

            mImageId = extras.getInt("image_id", 0);

            Log.d("image_path", mCurrentPhotoPath);

            Log.d("image_id", mImageId + "");

            //Picasso.with(this).load(new File(mCurrentPhotoPath)).into(mImageView);

            Picasso.with(this)
                    .load(new File(mCurrentPhotoPath))
                    .placeholder(R.drawable.square_placeholder)
                    .into(mImageView);

            mImageView.setVisibility(View.VISIBLE);

        }

        if (savedInstanceState != null) {

            mCurrentPhotoPath = savedInstanceState.getString("image_path", "");

            mImageId = savedInstanceState.getInt("image_id", 0);

            Log.d("image_path", mCurrentPhotoPath);

            Log.d("image_id", mImageId + "");

            //Picasso.with(this).load(new File(mCurrentPhotoPath)).into(mImageView);

            Picasso.with(this)
                    .load(new File(mCurrentPhotoPath))
                    .placeholder(R.drawable.square_placeholder)
                    .into(mImageView);

            mImageView.setVisibility(View.VISIBLE);

            dateField.setText(savedInstanceState.getString("report_date"));

            commentsField.setText(savedInstanceState.getString("report_description"));

            latitude = savedInstanceState.getDouble("latitude", 0);

            longitude = savedInstanceState.getDouble("longitude", 0);

            if (longitude > 0 && latitude > 0) {

                staticMap.setVisibility(View.VISIBLE);

                String url = "http://api.tiles.mapbox.com/v4/bmcintyre.ibo4mn2f/" + "pin-m+0094d6(" + longitude + "," + latitude + ",14)/" + longitude + ',' + latitude + ",14/640x640@2x.png?access_token=pk.eyJ1IjoiYm1jaW50eXJlIiwiYSI6IjdST3dWNVEifQ.ACCd6caINa_d4EdEZB_dJw";

                Picasso.with(this)
                        .load(url)
                        //.placeholder(R.drawable.square_placeholder)
                        .fit()
                        .centerCrop()
                        .into(staticMap);

                locationButton.setText("Edit location");

            }

            mImageId = savedInstanceState.getInt("image_id", 0);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_save) {

            dateText = String.valueOf(dateField.getText());

            commentsText = String.valueOf(commentsField.getText());

            // Step through comment and location checks and warn the user if anything's missing.

            if (commentsText.isEmpty()) {

                CharSequence text = "Please add a comment.";
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();

                return false;

            }

            if (location != null) {

                latitude = location.getLatitude();

                longitude = location.getLongitude();

                if (latitude == 0 || longitude == 0) {

                    CharSequence text = "Please update your location.";
                    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                    toast.show();

                    return false;

                }

            } else {

                SharedPreferences prefs =
                        getSharedPreferences(getPackageName(), MODE_PRIVATE);

                latitude = prefs.getFloat("latitude", 0);

                longitude = prefs.getFloat("longitude", 0);

                if (latitude == 0 || longitude == 0) {

                    CharSequence text = "Please update your location.";
                    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                    toast.show();

                    return false;

                }

            }

            Submission submission = new Submission(dateText,
                    commentsText, latitude, longitude, mCurrentPhotoPath, mImageId);

            submission.save();

            startActivity(new Intent(this, SubmissionsActivity.class));

            return false;

        }

        return super.onOptionsItemSelected(item);

    }

    protected void initializeDateField() {

        UtilityMethods utilityMethods = new UtilityMethods();

        int day = utilityMethods.getCurrentDay();
        int month = utilityMethods.getCurrentMonth();
        int year = utilityMethods.getCurrentYear();

        dateField.setText(utilityMethods.getDateString(month, day, year));

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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

        File albumF = getAlbumDir();

        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

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

    private void setPic(String filePath) {

        Bitmap bitmap = decodeSampledBitmapFromResource(filePath, 1280, 1280);

        Log.d(null, filePath + " " + bitmap.getWidth() + " " + bitmap.getHeight());

        int dimension = getSquareCropDimensionForBitmap(bitmap);

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);

        try {

            File file = new File(filePath);

            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            fOut.flush();

            fOut.close();

        } catch (Exception e) {

            e.printStackTrace();

            Log.d(null, "Save file error!");

//            return false;

        }


        Picasso.with(this).load(new File(filePath)).into(mImageView);

        mImageView.setVisibility(View.VISIBLE);

    }

    private void galleryAddPic() {

        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");

        File f = new File(mCurrentPhotoPath);

        Uri contentUri = Uri.fromFile(f);

        mediaScanIntent.setData(contentUri);

        this.sendBroadcast(mediaScanIntent);

    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic(mCurrentPhotoPath);
            galleryAddPic();
        }

    }

    private void processGalleryPhoto(Intent returnedImageIntent) {

        Uri selectedImage = returnedImageIntent.getData();

        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

        mCurrentPhotoPath = cursor.getString(columnIndex);

        cursor.close();

        setPic(mCurrentPhotoPath);

        /* Decode the JPEG file into a Bitmap */
//        mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
//
//		/* Associate the Bitmap to the ImageView */
//        mImageView.setImageBitmap(mImageBitmap);
//
//        mImageView.setVisibility(View.VISIBLE);

    }

    private android.hardware.Camera.Size getSupportedSize() {

        Camera camera = android.hardware.Camera.open();

//        android.hardware.Camera.Parameters cameraParams = new android.hardware.Camera.Parameters();

//        android.hardware.Camera.Parameters cameraParams = camera.getParameters();

        List<Camera.Size> supportedSizes = camera.getParameters().getSupportedPictureSizes();

        List<android.hardware.Camera.Size> targetSizes = new ArrayList<Camera.Size>();

        for (android.hardware.Camera.Size size : supportedSizes) {

            Log.d("supported size", size.height + "x" + size.width);

            if ((size.height >= 1280) && (size.width != size.height)) {

                targetSizes.add(size);

            }

        }

        Collections.sort(targetSizes, new Comparator<Camera.Size>() {

            public int compare(android.hardware.Camera.Size size1, android.hardware.Camera.Size size2) {

                return Double.compare(size1.width, size2.width);

            }

        });

        camera.release();

        if (!targetSizes.isEmpty()) {

            return targetSizes.get(0);

        } else {

            return supportedSizes.get(supportedSizes.size() - 1);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ACTION_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            case ACTION_SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    processGalleryPhoto(data);
                }
                break;
            case ACTION_SET_LOCATION:

                if (resultCode == RESULT_OK) {

                    Bundle bundle = data.getParcelableExtra("bundle");

                    location = bundle.getParcelable("latLng");

                    Log.d("location", location.getLatitude() + "");

                    if (location != null) {

                        staticMap.setVisibility(View.VISIBLE);

                        longitude = location.getLongitude();

                        latitude = location.getLatitude();

                        Log.d("position", longitude + latitude + "");

                        String url = "http://api.tiles.mapbox.com/v4/bmcintyre.ibo4mn2f/" + "pin-m+0094d6(" + longitude + "," + latitude + ",14)/" + longitude + ',' + latitude + ",14/640x640@2x.png?access_token=pk.eyJ1IjoiYm1jaW50eXJlIiwiYSI6IjdST3dWNVEifQ.ACCd6caINa_d4EdEZB_dJw";

                        Log.d("url", url);

                        Picasso.with(this)
                                .load(url)
                                //.placeholder(R.drawable.square_placeholder)
                                .fit()
                                .centerCrop()
                                .into(staticMap);

                        locationButton.setText("Edit location");

                        CharSequence text = "Location saved successfully";

                        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);

                        toast.show();

                    }

                }
                break;
        }
    }

    /**
     * onClick event to launch a map from Report view
     **/
    public void updateLocation(View v) {

        startActivityForResult(new Intent(this, LocationActivity.class), ACTION_SET_LOCATION);

    }

    /**
     * onClick event to launch a date picker from Report view
     **/
    public void showDatePickerDialog(View v) {

        DialogFragment newFragment = new DatePickerFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "datePicker");

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        android.hardware.Camera.Size size = getSupportedSize();

        Log.d("size", size.height + "x" + size.width);

        try {

            File f = createImageFile();

            mCurrentPhotoPath = f.getAbsolutePath();

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        } catch (IOException e) {

            e.printStackTrace();

            mCurrentPhotoPath = null;

        }

        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
        ;

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        photoPickerIntent.setType("image/*");

        startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("report_date", dateText);

        outState.putString("report_description", commentsText);

        outState.putDouble("latitude", latitude);

        outState.putDouble("latitude", longitude);

        outState.putString("image_path", mCurrentPhotoPath);

        outState.putInt("image_id", mImageId);

    }
}