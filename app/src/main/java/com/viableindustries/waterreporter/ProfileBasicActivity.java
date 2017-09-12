package com.viableindustries.waterreporter;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.image.ImageService;
import com.viableindustries.waterreporter.api.interfaces.data.user.UserService;
import com.viableindustries.waterreporter.api.models.image.ImageProperties;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.dialogs.PhotoPickerDialogFragment;
import com.viableindustries.waterreporter.utilities.CacheManager;
import com.viableindustries.waterreporter.utilities.CancelableCallback;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.FileUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

// Activity shown as dialog to handle final step in first-time user registration.

public class ProfileBasicActivity extends AppCompatActivity implements
        PhotoPickerDialogFragment.PhotoPickerDialogListener,
        EasyPermissions.PermissionCallbacks{

    @Bind(R.id.new_user_profile)
    LinearLayout parentLayout;

    @Bind(R.id.first_name)
    EditText firstNameInput;

    @Bind(R.id.last_name)
    EditText lastNameInput;

    @Bind(R.id.user_bio)
    EditText userBioInput;

    @Bind(R.id.new_user_avatar)
    ImageView userAvatar;

    @Bind(R.id.new_user_avatar_preview)
    ImageView avatarPreview;

    @Bind(R.id.save_profile)
    ImageButton saveProfileButton;

    @Bind(R.id.saving_message)
    TextView savingMessage;

    private String mTempImagePath;

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final int ACTION_SELECT_PHOTO = 2;

    private boolean photoCaptured = false;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    private Uri imageUri;

    private static final int RC_ALL_PERMISSIONS = 100;

    private static final int RC_SETTINGS_SCREEN = 125;

    private static final String TAG = "ProfileBasicActivity";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_basic);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        verifyPermissions();

    }

    @AfterPermissionGranted(RC_ALL_PERMISSIONS)
    private void loadRandomAvatar() {

        String[] array = getResources().getStringArray(R.array.default_avatars);

        String randomImage = array[new Random().nextInt(array.length)];

        int avatarId = getResources().getIdentifier(randomImage, "drawable", getPackageName());

        // Create new image file and path reference

        File image;
        try {

            image = FileUtils.createImageFile(this);

            // Use FileProvider to comply with Android security requirements.
            // See: https://developer.android.com/training/camera/photobasics.html
            // https://developer.android.com/reference/android/os/FileUriExposedException.html

            imageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, image);

            // Using v4 Support Library FileProvider and Camera intent on pre-Marshmallow devices
            // requires granting FileUri permissions at runtime

            this.grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            mTempImagePath = image.getAbsolutePath();

            InputStream inputStream = getResources().openRawResource(avatarId);

            OutputStream out = new FileOutputStream(image);

            byte buf[] = new byte[1024];
            int len;

            while ((len = inputStream.read(buf)) > 0)
                out.write(buf, 0, len);

            out.close();

            inputStream.close();

            photoCaptured = true;

        } catch (Exception e) {

            e.printStackTrace();

            Log.d(null, "Save file error!");

            mTempImagePath = null;

            return;

        }

        Picasso.with(this)
                .load(image)
                .placeholder(R.drawable.user_avatar_placeholder)
                .transform(new CircleTransform())
                .into(userAvatar);

        // Set click listeners

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfilePic(view);
            }
        });

    }

    private void addProfilePic(View v) {

        showPhotoPickerDialog();

    }

    public void saveProfile(View view) {

        if (photoCaptured) {

            final String firstName = String.valueOf(firstNameInput.getText());

            final String lastName = String.valueOf(lastNameInput.getText());

            final String description = String.valueOf(userBioInput.getText());

            if (firstName.isEmpty() || lastName.isEmpty()) {

                CharSequence text = "Please enter both your first and last names.";
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();

                return;

            }

            savingMessage.setVisibility(View.VISIBLE);

            savingMessage.setText(getResources().getString(R.string.saving_profile));

            final int userId = prefs.getInt("user_id", 0);

            final String accessToken = prefs.getString("access_token", "");

            String filePath = mTempImagePath;

            if (filePath == null) {

                filePath = FileUtils.getPathFromUri(this, imageUri);

            }

            if (filePath != null) {

                final File photo = new File(filePath);

                FileNameMap fileNameMap = URLConnection.getFileNameMap();

                String mimeType = fileNameMap.getContentTypeFor(filePath);

                TypedFile typedPhoto = new TypedFile(mimeType, photo);

                RestClient.getImageService().postImageAsync(accessToken, typedPhoto,
                        new CancelableCallback<ImageProperties>() {
                            @Override
                            public void onSuccess(ImageProperties imageProperties,
                                                Response response) {

                                // Retrieve the image id and add relation to PATCH request body

                                Map<String, Object> userPatch = new HashMap<>();

                                final Map<String, Integer> image_id = new HashMap<>();

                                image_id.put("id", imageProperties.id);

                                List<Map<String, Integer>> images = new ArrayList<>();

                                images.add(image_id);

                                userPatch.put("images", images);

                                // Build out remaining values

                                userPatch.put("first_name", firstName);
                                userPatch.put("last_name", lastName);

                                // The value of the `picture` attribute must be supplied
                                // manually as the system doesn't populate this field
                                // automatically.

                                userPatch.put("picture", imageProperties.icon_retina);

                                if (!description.isEmpty())
                                    userPatch.put("description", description);

                                RestClient.getUserService().updateUser(accessToken,
                                        "application/json",
                                        userId,
                                        userPatch,
                                        new CancelableCallback<User>() {
                                            @Override
                                            public void onSuccess(User user,
                                                                Response response) {

                                                // Clear the app api cache

                                                CacheManager.deleteCache(getBaseContext());

                                                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                                                coreProfile.edit()
                                                        //.putBoolean("active", user.properties.active)
                                                        .putInt("id", user.id)
                                                        .putString("picture", user.properties.images.get(0).properties.icon_retina)
                                                        .apply();

                                                // Model strings
                                                String[] KEYS = {"description", "first_name",
                                                        "last_name", "organization_name", //"picture",
                                                        "public_email", "title"};

                                                for (String key : KEYS) {

                                                    coreProfile.edit().putString(key, user.properties.getStringProperties().get(key)).apply();

                                                }

                                                final Handler handler = new Handler();

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        Intent intent = new Intent(ProfileBasicActivity.this, GroupActionListActivity.class);

                                                        intent.putExtra("POST_REGISTER", true);

                                                        startActivity(intent);

                                                    }

                                                }, 100);

                                            }

                                            @Override
                                            public void onFailure(RetrofitError error) {
                                                savingMessage.setVisibility(View.GONE);
                                                savingMessage.setText(getResources().getString(R.string.save));
                                            }

                                        });

                            }

                            @Override
                            public void onFailure(RetrofitError error) {
                                savingMessage.setVisibility(View.GONE);
                                savingMessage.setText(getResources().getString(R.string.save));
                            }

                        });

            }

        } else {

            Snackbar.make(parentLayout, "Please add a photo. You don't need to use a self-portrait, so feel free to pick something like your favorite animal.",
                    Snackbar.LENGTH_SHORT)
                    .show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ACTION_TAKE_PHOTO:

                if (resultCode == RESULT_OK) {

                    FileUtils.galleryAddPic(this, mTempImagePath);

                    Log.d("path", mTempImagePath);

                    // Display thumbnail

                    try {

                        File f = new File(mTempImagePath);

                        String thumbName = String.format("%s-%s.jpg", Math.random(), new Date());

                        File thumb = File.createTempFile(thumbName, null, this.getCacheDir());

                        Log.d("taken path", f.getAbsolutePath());
                        Log.d("taken path", f.toString());
                        Log.d("taken path", f.toURI().toString());

                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions);

                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 192, 192);

                        FileOutputStream fOut = new FileOutputStream(thumb);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);

                        fOut.flush();

                        fOut.close();

                        Log.d("thumb", thumb.toString());
                        Log.d("thumb", thumb.toURI().toString());
                        Log.d("thumb", thumb.getAbsolutePath());
                        Log.d("thumb", thumb.getPath());

                        userAvatar.setVisibility(View.GONE);

                        avatarPreview.setVisibility(View.VISIBLE);

                        Picasso.with(ProfileBasicActivity.this)
                                .load(thumb)
                                .placeholder(R.drawable.user_avatar_placeholder)
                                .transform(new CircleTransform())
                                .into(avatarPreview);

                        photoCaptured = true;

                    } catch (Exception e) {

                        e.printStackTrace();

                        Log.d("path error", "Save file error!");

                        mTempImagePath = null;

                        photoCaptured = false;

                        return;

                    }

                }

                break;

            case ACTION_SELECT_PHOTO:

                if (resultCode == RESULT_OK) {

                    if (data != null) {

                        try {

                            imageUri = data.getData();

                            String thumbName = String.format("%s-%s.jpg", Math.random(), new Date());

                            File thumb = File.createTempFile(thumbName, null, this.getCacheDir());

                            try {

                                ParcelFileDescriptor parcelFileDescriptor =
                                        getContentResolver().openFileDescriptor(imageUri, "r");

                                if (parcelFileDescriptor != null) {

                                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                                    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                                    parcelFileDescriptor.close();

                                    image = ThumbnailUtils.extractThumbnail(image, 192, 192);

                                    FileOutputStream fOut = new FileOutputStream(thumb);

                                    image.compress(Bitmap.CompressFormat.JPEG, 90, fOut);

                                    fOut.flush();

                                    fOut.close();

                                    Log.d("thumb", thumb.toString());
                                    Log.d("thumb", thumb.toURI().toString());
                                    Log.d("thumb", thumb.getAbsolutePath());
                                    Log.d("thumb", thumb.getPath());

                                    Log.d("thumb bitmap", image.toString());

                                    userAvatar.setVisibility(View.GONE);

                                    avatarPreview.setVisibility(View.VISIBLE);

                                    Picasso.with(ProfileBasicActivity.this)
                                            .load(thumb)
                                            .placeholder(R.drawable.user_avatar_placeholder)
                                            .transform(new CircleTransform())
                                            .into(avatarPreview);

                                    photoCaptured = true;

                                }

                            } catch (IOException e) {

                                e.printStackTrace();

                            }

                        } catch (Exception e) {

                            Snackbar.make(parentLayout, "Unable to read image.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();

                            mTempImagePath = null;

                            photoCaptured = false;

                        }

                    }

                } else {

                    Log.d("image", "no image api");

                }

                break;

        }

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        // For compatibility with Android 6.0 (Marshmallow, API 23), we need to check permissions before
        // dispatching takePictureIntent, otherwise the app will crash.

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {

            File f = FileUtils.createImageFile(this);

            mTempImagePath = f.getAbsolutePath();

            Log.d("filepath", mTempImagePath);

            // Use FileProvider to comply with Android security requirements.
            // See: https://developer.android.com/training/camera/photobasics.html
            // https://developer.android.com/reference/android/os/FileUriExposedException.html

            imageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, f);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            // Using v4 Support Library FileProvider and Camera intent on pre-Marshmallow devices
            // requires granting FileUri permissions at runtime

            List<ResolveInfo> resolvedIntentActivities = this.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {

                String packageName = resolvedIntentInfo.activityInfo.packageName;

                this.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            }

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);

            }

        } catch (IOException e) {

            e.printStackTrace();

            mTempImagePath = null;

        }

    }

    @Override
    public void onDialogNegativeClick(android.app.DialogFragment dialog) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        photoPickerIntent.setType("image/*");

        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

        }

    }

    private void showPhotoPickerDialog() {

        android.app.DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

    }

    private void verifyPermissions() {

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {

            loadRandomAvatar();

        } else {

            // Ask for all permissions since the app is useless without them
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_all_permissions),
                    RC_ALL_PERMISSIONS, permissions);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // In this context we really cannot continue without the required permissions.
                            // Close the application and set an `incomplete` flag on the authenticated
                            // user's profile.

                            resetStoredUserData();

                            Intent a = new Intent(Intent.ACTION_MAIN);
                            a.addCategory(Intent.CATEGORY_HOME);
                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(a);

                            ActivityCompat.finishAffinity(ProfileBasicActivity.this);

                        }
                    })
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();

        }

    }

    private void resetStoredUserData() {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        prefs.edit().clear().apply();

    }

    @Override
    protected void onResume() {

        super.onResume();

        verifyPermissions();

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
