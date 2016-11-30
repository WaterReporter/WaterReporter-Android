package com.viableindustries.waterreporter;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
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
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;
import com.viableindustries.waterreporter.data.UserService;

import java.io.File;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class EditProfileActivity extends AppCompatActivity implements
        PhotoPickerDialogFragment.PhotoPickerDialogListener {

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

    @Bind(R.id.user_avatar)
    ImageView userAvatar;

    @Bind(R.id.change_image)
    ImageButton editPhoto;

    @Bind(R.id.save_profile)
    ImageButton saveProfileButton;

    @Bind(R.id.saving_message)
    TextView savingMessage;

    private File image;

    private String mGalleryPath;

    private String mTempImagePath;

    private static final int ACTION_ADD_PHOTO = 1;

    private User coreUser;

    private String access_token;

    private static final String JPEG_FILE_PREFIX = "IMG_";

    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final int ACTION_SELECT_PHOTO = 2;

    protected boolean photoCaptured = false;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        ButterKnife.bind(this);

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

        // Load avatar if it exists, otherwise provide a randomized default image.

        if (coreUser.properties.picture != null) {

            Picasso.with(this).load(coreUser.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

        } else {

            String[] array = getResources().getStringArray(R.array.default_avatars);

            String randomImage = array[new Random().nextInt(array.length)];

            int avatarId = getResources().getIdentifier(randomImage, "drawable", getPackageName());

            // Create new image file and path reference

            try {

                image = FileUtils.createImageFile(this);

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

                Picasso.with(this).load(image).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

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

        final ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        final UserService userService = UserService.restAdapter.create(UserService.class);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final int userId = prefs.getInt("user_id", 0);

        final String access_token = prefs.getString("access_token", "");

        File photo = new File(mTempImagePath);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        String mimeType = fileNameMap.getContentTypeFor(mTempImagePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        imageService.postImage(access_token, typedPhoto,
                new Callback<ImageProperties>() {
                    @Override
                    public void success(ImageProperties imageProperties,
                                        Response response) {

                        // Immediately delete the cached image file now that we no longer need it

                        File tempFile = new File(mTempImagePath);

                        boolean imageDeleted = tempFile.delete();

                        Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

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

    public void updateProfile(Map<String, Object> userPatch) {

        final String firstName = String.valueOf(firstNameInput.getText());

        final String lastName = String.valueOf(lastNameInput.getText());

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

        userService.updateUser(access_token,
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

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        access_token = prefs.getString("access_token", "");

        savingMessage.setVisibility(View.VISIBLE);

        savingMessage.setText(getResources().getString(R.string.saving_profile));

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

                        Log.d("taken path", f.getAbsolutePath());
                        Log.d("taken path", f.toString());
                        Log.d("taken path", f.toURI().toString());

                        Picasso.with(this)
                                .load(new File(mTempImagePath))
                                .placeholder(R.drawable.user_avatar_placeholder)
                                .transform(new CircleTransform())
                                .into(userAvatar);

                        photoCaptured = true;

                    } catch (Exception e) {

                        e.printStackTrace();

                        Log.d(null, "Save file error!");

                        mTempImagePath = null;

                        return;

                    }

                }

                break;

            case ACTION_SELECT_PHOTO:

                if (resultCode == RESULT_OK) {

                    if (data != null) {

                        try {

                            File f = FileUtils.createImageFile(this);

                            mTempImagePath = f.getAbsolutePath();

                            Log.d("filepath", mTempImagePath);

                            // Use FileProvider to comply with Android security requirements.
                            // See: https://developer.android.com/training/camera/photobasics.html
                            // https://developer.android.com/reference/android/os/FileUriExposedException.html

                            imageUri = data.getData();

                            InputStream inputStream = getContentResolver().openInputStream(imageUri);

                            Bitmap bitmap = FileUtils.decodeSampledBitmapFromStream(this, imageUri, 1080, 1080);

                            FileOutputStream fOut = new FileOutputStream(f);

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);

                            fOut.flush();

                            fOut.close();

                            Picasso.with(this)
                                    .load(f)
                                    .placeholder(R.drawable.user_avatar_placeholder)
                                    .transform(new CircleTransform())
                                    .into(userAvatar);

                            photoCaptured = true;

                        } catch (Exception e) {

                            Snackbar.make(parentLayout, "Unable to read image.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();

                        }

                    }

                } else {

                    Log.d("image", "no image data");

                }

                break;

        }

    }


    @Override
    public void onDialogPositiveClick(android.app.DialogFragment dialog) {

        // For compatibility with Android 6.0 (Marshmallow, API 23), we need to check permissions before
        // dispatching takePictureIntent, otherwise the app will crash.

        PermissionUtil.verifyPermission(this, Manifest.permission.CAMERA);

        PermissionUtil.verifyPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

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

        PermissionUtil.verifyPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Intent photoPickerIntent;

        if (Build.VERSION.SDK_INT < 19) {

            photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);

        } else {

            photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);

        }

        photoPickerIntent.setType("image/*");

        try {

            File f = FileUtils.createImageFile(this);

            mTempImagePath = f.getAbsolutePath();

            Log.d("filepath", mTempImagePath);

            // Use FileProvider to comply with Android security requirements.
            // See: https://developer.android.com/training/camera/photobasics.html
            // https://developer.android.com/reference/android/os/FileUriExposedException.html

            imageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, f);

            photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {

                startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

            }

        } catch (IOException e) {

            e.printStackTrace();

            mTempImagePath = null;

        }

    }

    protected void showPhotoPickerDialog() {

        android.app.DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

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
