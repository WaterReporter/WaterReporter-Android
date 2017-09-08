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
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.CacheManager;
import com.viableindustries.waterreporter.data.DisplayDecimal;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;
import com.viableindustries.waterreporter.data.UserService;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class EditProfileActivity extends AppCompatActivity implements
        PhotoPickerDialogFragment.PhotoPickerDialogListener,
        EasyPermissions.PermissionCallbacks {

    @Bind(R.id.new_user_profile)
    LinearLayout parentLayout;

    @Bind(R.id.first_name)
    EditText firstNameInput;

    @Bind(R.id.last_name)
    EditText lastNameInput;

    @Bind(R.id.user_title)
    EditText userTitleInput;

    @Bind(R.id.user_organization_name)
    EditText userOrganizationNameInput;

    @Bind(R.id.user_public_email)
    EditText userPublicEmailInput;

    @Bind(R.id.user_telephone)
    EditText userTelephoneInput;

    @Bind(R.id.user_bio)
    EditText userBioInput;

    @Bind(R.id.current_user_avatar)
    ImageView userAvatar;

    @Bind(R.id.current_user_avatar_preview)
    ImageView avatarPreview;

    @Bind(R.id.change_image)
    ImageButton editPhoto;

    @Bind(R.id.save_profile)
    ImageButton saveProfileButton;

    @Bind(R.id.saving_message)
    TextView savingMessage;

    private File image;

    private String mTempImagePath;

    private User coreUser;

    private SharedPreferences prefs;

    private String accessToken;

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final int ACTION_SELECT_PHOTO = 2;

    protected boolean photoCaptured = false;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    private Uri imageUri;

    private static final int RC_ALL_PERMISSIONS = 100;

    private static final int RC_SETTINGS_SCREEN = 125;

    private static final String TAG = "ProfileBasicActivity";

    private String firstName;

    private String lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        int coreId = coreProfile.getInt("id", 0);

        UserProperties userProperties = new UserProperties(coreId, coreProfile.getString("description", ""),
                coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

        coreUser = User.createUser(coreId, userProperties);

        lastNameInput.setText(coreUser.properties.last_name);

        firstNameInput.setText(coreUser.properties.first_name);

        userTitleInput.setText(coreUser.properties.title);

        userOrganizationNameInput.setText(coreUser.properties.organization_name);

        userPublicEmailInput.setText(coreUser.properties.public_email);

        userBioInput.setText(coreUser.properties.description);

        verifyPermissions();

    }

    @AfterPermissionGranted(RC_ALL_PERMISSIONS)
    private void loadRandomAvatar() {

        // Load avatar if it exists, otherwise provide a randomized default image.

        if (coreUser.properties.picture != null) {

            Picasso.with(this)
                    .load(coreUser.properties.picture)
                    .placeholder(R.drawable.user_avatar_placeholder)
                    .transform(new CircleTransform())
                    .into(userAvatar);

        } else {

            String[] array = getResources().getStringArray(R.array.default_avatars);

            String randomImage = array[new Random().nextInt(array.length)];

            int avatarId = getResources().getIdentifier(randomImage, "drawable", getPackageName());

            // Create new image file and path reference

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

                Picasso.with(this)
                        .load(image)
                        .placeholder(R.drawable.user_avatar_placeholder)
                        .transform(new CircleTransform())
                        .into(userAvatar);

            } catch (Exception e) {

                e.printStackTrace();

                Log.d(null, "Save file error!");

                mTempImagePath = null;

                return;

            }

        }

        // Set click listeners

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfilePic();
            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfilePic();
            }
        });

    }

    private void addProfilePic() {

        showPhotoPickerDialog();

    }

    private void postImage() {

        savingMessage.setVisibility(View.VISIBLE);

        savingMessage.setText(getResources().getString(R.string.saving_profile));

        final ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        final UserService userService = UserService.restAdapter.create(UserService.class);

        final int userId = prefs.getInt("user_id", 0);

        accessToken = prefs.getString("access_token", "");

        String filePath = mTempImagePath;

        if (filePath == null) {

            filePath = FileUtils.getPathFromUri(this, imageUri);

        }

        if (filePath != null) {

            final File photo = new File(filePath);

            FileNameMap fileNameMap = URLConnection.getFileNameMap();

            String mimeType = fileNameMap.getContentTypeFor(filePath);

            TypedFile typedPhoto = new TypedFile(mimeType, photo);

            imageService.postImageAsync(accessToken, typedPhoto,
                    new Callback<ImageProperties>() {
                        @Override
                        public void success(ImageProperties imageProperties,
                                            Response response) {

                            // Revoke Uri permissions

                            getBaseContext().revokeUriPermission(imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            // Clear the app data cache

                            CacheManager.deleteCache(getBaseContext());

                            // Retrieve the image id and add relation to PATCH request body

                            Map<String, Object> userPatch = new HashMap<String, Object>();

                            final Map<String, Integer> image_id = new HashMap<String, Integer>();

                            image_id.put("id", imageProperties.id);

                            List<Map<String, Integer>> images = new ArrayList<Map<String, Integer>>();

                            images.add(image_id);

                            userPatch.put("images", images);

                            // The value of the `picture` attribute must be supplied
                            // manually as the system doesn't populate this field
                            // automatically.

                            userPatch.put("picture", imageProperties.icon_retina);

                            // Complete request body and send PATCH request

                            updateProfile(userPatch);

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            savingMessage.setVisibility(View.GONE);
                            savingMessage.setText(getResources().getString(R.string.save));
                        }

                    });

        }

    }

    public void updateProfile(Map<String, Object> userPatch) {

        savingMessage.setVisibility(View.VISIBLE);

        savingMessage.setText(getResources().getString(R.string.saving_profile));

        final String title = String.valueOf(userTitleInput.getText());

        final String organizationName = String.valueOf(userOrganizationNameInput.getText());

        final String publicEmail = String.valueOf(userPublicEmailInput.getText());

        final String telephone = String.valueOf(userTelephoneInput.getText());

        final String description = String.valueOf(userBioInput.getText());

        if (firstName.isEmpty() || lastName.isEmpty()) {

            CharSequence text = "Please enter both your first and last names.";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();

            return;

        }

        // Build out remaining values

        userPatch.put("first_name", firstName);
        userPatch.put("last_name", lastName);

        if (!title.isEmpty()) userPatch.put("title", title);

        if (!organizationName.isEmpty())
            userPatch.put("organization_name", organizationName);

        if (!publicEmail.isEmpty()) userPatch.put("public_email", publicEmail);

        if (!description.isEmpty()) userPatch.put("description", description);

        if (!telephone.isEmpty()) {

            List<Map<String, String>> telephones = new ArrayList<>();

            Map<String, String> phoneNumber = new HashMap<String, String>();

            phoneNumber.put("number", telephone);

            telephones.add(phoneNumber);

            userPatch.put("telephone", telephones);

        }

        final UserService userService = UserService.restAdapter.create(UserService.class);

        userService.updateUser(accessToken,
                "application/json",
                coreUser.id,
                userPatch,
                new Callback<User>() {
                    @Override
                    public void success(User user,
                                        Response response) {

                        final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                        coreProfile.edit()
                                .putInt("id", user.id)
                                .apply();

                        // Update stored values of user's string type attributes

                        Map<String, String> userStringProperties = user.properties.getStringProperties();

                        for (Map.Entry<String, String> entry : userStringProperties.entrySet()) {

                            coreProfile.edit().putString(entry.getKey(), entry.getValue()).apply();

                        }

                        UserProperties userProperties = new UserProperties(user.id, coreProfile.getString("description", ""),
                                coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                                coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                                coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

                        User coreUser = User.createUser(user.id, userProperties);

                        UserHolder.setUser(coreUser);

                        final Handler handler = new Handler();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                startActivity(new Intent(EditProfileActivity.this, ProfileSettingsActivity.class));

                            }

                        }, 100);

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        savingMessage.setVisibility(View.GONE);
                        savingMessage.setText(getResources().getString(R.string.save));
                    }

                });


    }

    public void saveProfile(View view) {

        firstName = String.valueOf(firstNameInput.getText());

        lastName = String.valueOf(lastNameInput.getText());

        if (firstName.isEmpty() || lastName.isEmpty()) {

            CharSequence text = "Please enter both your first and last names.";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();

            return;

        }

        accessToken = prefs.getString("access_token", "");

        if (photoCaptured) {

            postImage();

        } else {

            updateProfile(new HashMap<String, Object>());

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

                        Picasso.with(EditProfileActivity.this)
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

                                    Picasso.with(EditProfileActivity.this)
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

                    Log.d("image", "no image data");

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
    public void onDialogNegativeClick(DialogFragment dialog) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        photoPickerIntent.setType("image/*");

        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

        }

    }

    protected void showPhotoPickerDialog() {

        android.app.DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

    }

    protected void verifyPermissions() {

        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {

            loadRandomAvatar();

        } else {

            // Ask for all permissions since the app is useless without them
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_files),
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

                            // If the user really doesn't want to play ball, at least them browse reports

                            startActivity(new Intent(getBaseContext(), MainActivity.class));

                        }
                    })
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();

        }

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

    }

}
