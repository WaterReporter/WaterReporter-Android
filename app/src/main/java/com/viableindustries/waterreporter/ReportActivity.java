package com.viableindustries.waterreporter;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.viableindustries.waterreporter.data.Submission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This activity handles all of the report functionality.
 */
public class ReportActivity extends ActionBarActivity
        implements PhotoPickerDialogFragment.PhotoPickerDialogListener {
    @InjectView(R.id.pollution_button) Button pollutionButton;
    @InjectView(R.id.activity_button) Button activityButton;
    @InjectView(R.id.date) EditText dateField;
    @InjectView(R.id.activity_type) Spinner activitySpinner;
    @InjectView(R.id.comments) EditText commentsField;
    @InjectView(R.id.preview) ImageView mImageView;

    private static final int ACTION_SET_LOCATION = 0;
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_SELECT_PHOTO = 2;

    private String mCurrentPhotoPath;

    private Bitmap mImageBitmap;

    private static final String CAMERA_DIR = "/dcim/";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private LatLng location;

    private String dateText;
    private String commentsText;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ButterKnife.inject(this);

        initializeDateField();

        getFieldData(savedInstanceState);

        if(savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString("imagePath");
            mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mImageView.setImageBitmap(mImageBitmap);
            mImageView.setVisibility(View.VISIBLE);

            dateField.setText(savedInstanceState.getString("date"));
            commentsField.setText(savedInstanceState.getString("comments"));
            latitude = savedInstanceState.getDouble("latitude");
            longitude = savedInstanceState.getDouble("longitude");
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
        long activity;
        String activityText;
        String reportType;

        int id = item.getItemId();

        if(id == R.id.action_save){
            if(activitySpinner.getPrompt().toString().equals("Pollution Report")){
                reportType = "[{\"id\":2}]";
            } else {
                reportType = "[{\"id\":1}]";
            }
            dateText = String.valueOf(dateField.getText());
            activity = activitySpinner.getSelectedItemId();
            activityText = "[{\"id\":" + activity + "}]";
            commentsText = String.valueOf(commentsField.getText());
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                SharedPreferences prefs =
                        getSharedPreferences(getPackageName(), MODE_PRIVATE);
                latitude = prefs.getFloat("latitude", 0);
                longitude = prefs.getFloat("longitude", 0);
            }

            Submission submission = new Submission(reportType, dateText, activityText,
                    commentsText, latitude, longitude, mCurrentPhotoPath);
            submission.save();

            startActivity(new Intent(this, SubmissionsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    protected void initializeDateField(){
        UtilityMethods utilityMethods = new UtilityMethods();

        int day = utilityMethods.getCurrentDay();
        int month = utilityMethods.getCurrentMonth();
        int year = utilityMethods.getCurrentYear();

        dateField.setText(utilityMethods.getDateString(month, day, year));
    }

    protected void getFieldData(final Bundle bundle) {
        final String[] pollutionTypes = {"Pollution Type", "Discolored water", "Eroded stream banks", "Excessive algae",
        "Excessive trash", "Exposed soil", "Faulty construction entryway", "Faulty silt fences",
        "Fish kill", "Foam", "Livestock in stream", "Oil and grease", "Other", "Pipe Discharge",
        "Sewer overflow", "Stormwater", "Winter manure application"};
        final String[] activityTypes = {"Activity Type", "Canoeing", "Diving", "Fishing", "Flatwater kayaking", "Hiking",
        "Living the dream", "Rock climbing", "Sailing", "Scouting wildlife", "Snorkeling",
        "Stand-up paddleboarding", "Stream cleanup", "Surfing", "Swimming", "Tubing", "Water Skiing",
        "Whitewater kayaking", "Whitewater rafting"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_spinner_item, pollutionTypes);
        activitySpinner.setAdapter(adapter);

        pollutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollutionButton.setBackgroundColor(getResources()
                        .getColor(R.color.waterreporter_blue));
                pollutionButton.setTextColor(getResources().getColor(R.color.white));
                activityButton.setBackgroundColor(getResources()
                        .getColor(R.color.white));
                activityButton.setTextColor(getResources().getColor(R.color.primary_text_default_material_light));
                activitySpinner.setPrompt("Pollution Report");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                        android.R.layout.simple_spinner_item, pollutionTypes);
                activitySpinner.setAdapter(adapter);
            }
        });

        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollutionButton.setBackgroundColor(getResources()
                        .getColor(R.color.white));
                pollutionButton.setTextColor(getResources().getColor(R.color.primary_text_default_material_light));
                activityButton.setBackgroundColor(getResources()
                        .getColor(R.color.waterreporter_blue));
                activityButton.setTextColor(getResources().getColor(R.color.white));
                activitySpinner.setPrompt("Activity Report");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                        android.R.layout.simple_spinner_item, activityTypes);
                activitySpinner.setAdapter(adapter);
            }
        });

        if(bundle != null){
            activitySpinner.setSelection(bundle.getInt("activity"));
        }
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

    private void setPic() {
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

		/* Decode the JPEG file into a Bitmap */
        mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(mImageBitmap);
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
            setPic();
            galleryAddPic();
        }

    }

    private void processGalleryPhoto(Intent returnedImageIntent){
        Uri selectedImage = returnedImageIntent.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        mCurrentPhotoPath = cursor.getString(columnIndex);
        cursor.close();

        /* Decode the JPEG file into a Bitmap */
        mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case ACTION_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    handleBigCameraPhoto();
                }
                break;
            case ACTION_SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    processGalleryPhoto(data);
                }
                break;
            case ACTION_SET_LOCATION:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getParcelableExtra("bundle");
                    location = bundle.getParcelable("latLng");
                    CharSequence text = "Location saved successfully";
                    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }
    }

    /** onClick event to launch a device's camera from Report view **/
    public void launchCamera(View v) {
        DialogFragment newFragment = new PhotoPickerDialogFragment();
        FragmentManager fragmentManager = getFragmentManager();
        newFragment.show(fragmentManager, "photoPickerDialog");
    }

    /** onClick event to launch a map from Report view **/
    public void updateLocation(View v) {
        startActivityForResult(new Intent(this, MapActivity.class), ACTION_SET_LOCATION);
    }

    /** onClick event to launch a date picker from Report view **/
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        FragmentManager fragmentManager = getFragmentManager();
        newFragment.show(fragmentManager, "datePicker");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File f = createImageFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
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

        outState.putString("date", dateText);
        outState.putInt("activity", activitySpinner.getSelectedItemPosition());
        outState.putString("comments", commentsText);
        outState.putDouble("latitude", latitude);
        outState.putDouble("latitude", longitude);
        outState.putString("imagePath", mCurrentPhotoPath);
    }
}