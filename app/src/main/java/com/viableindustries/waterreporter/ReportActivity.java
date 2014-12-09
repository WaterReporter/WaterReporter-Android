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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.viableindustries.waterreporter.data.CommonsCloudService;
import com.viableindustries.waterreporter.data.Field;
import com.viableindustries.waterreporter.data.Submission;
import com.viableindustries.waterreporter.data.TemplateResponse;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This activity handles all of the report functionality.
 */
public class ReportActivity extends ActionBarActivity
        implements PhotoPickerDialogFragment.PhotoPickerDialogListener {
    @InjectView(R.id.date) EditText dateField;
    @InjectView(R.id.location_spinner) Spinner locationSpinner;
    @InjectView(R.id.issue_spinner) Spinner issueSpinner;
    @InjectView(R.id.facility_spinner) Spinner facilitySpinner;
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

    private String[] locationOptions;
    private String[] issueOptions;
    private String[] facilityOptions;
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
        String locationText;
        String issueText;
        String facilityText;

        int id = item.getItemId();

        if(id == R.id.action_save){
            dateText = String.valueOf(dateField.getText());
            locationText = locationSpinner.getSelectedItem().toString();
            issueText = issueSpinner.getSelectedItem().toString();
            facilityText = facilitySpinner.getSelectedItem().toString();
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

            Submission submission = new Submission(dateText, locationText, issueText,
                    facilityText, commentsText, latitude, longitude, mCurrentPhotoPath);
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

    protected String[] addTitle(String[] array, String title){
        List<String> list = new LinkedList<String>(Arrays.asList(array));
        list.add(0, title);
        return list.toArray(new String[list.size()]);
    }

    protected void getFieldData(final Bundle bundle) {
        RestAdapter restAdapter = CommonsCloudService.restAdapter;

        CommonsCloudService service = restAdapter.create(CommonsCloudService.class);

        service.getFields(new Callback<TemplateResponse>() {
            @Override
            public void success(TemplateResponse templateResponse, Response response) {
                List<Field> fields = templateResponse.fieldsObject.getFields();

                for(Field field : fields){
                    String label = field.getLabel();

                    if(label.equals("Location")){
                        locationOptions = addTitle(field.getOptions(), "Select location of problem");
                        final ArrayAdapter<String> adapter =
                                new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, locationOptions);
                        locationSpinner.setAdapter(adapter);
                        if(bundle != null){
                            locationSpinner.setSelection(bundle.getInt("location"));
                        }
                    }
                    else if(label.equals("Issue")){
                        issueOptions = addTitle(field.getOptions(), "Select issue");
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, issueOptions);
                        issueSpinner.setAdapter(adapter);
                        if(bundle != null){
                            issueSpinner.setSelection(bundle.getInt("issue"));
                        }
                    }
                    else if(label.equals("Facility")){
                        facilityOptions = addTitle(field.getOptions(), "Select facility type");
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, facilityOptions);
                        facilitySpinner.setAdapter(adapter);
                        if(bundle != null){
                            facilitySpinner.setSelection(bundle.getInt("facility"));
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
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
//            mCurrentPhotoPath = null;
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
        outState.putInt("location", locationSpinner.getSelectedItemPosition());
        outState.putInt("issue", issueSpinner.getSelectedItemPosition());
        outState.putInt("facility", facilitySpinner.getSelectedItemPosition());
        outState.putString("comments", commentsText);
        outState.putDouble("latitude", latitude);
        outState.putDouble("latitude", longitude);
        outState.putString("imagePath", mCurrentPhotoPath);
    }
}