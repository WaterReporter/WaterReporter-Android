package com.viableindustries.waterreporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.AuthResponse;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.LogInBody;
import com.viableindustries.waterreporter.data.RegistrationBody;
import com.viableindustries.waterreporter.data.RegistrationResponse;
import com.viableindustries.waterreporter.data.SecurityService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserBasicResponse;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static java.security.AccessController.getContext;

// Activity shown as dialog to handle final step in first-time user registration.

public class ProfileBasicActivity extends AppCompatActivity {

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

    @Bind(R.id.save_profile)
    ImageButton saveProfileButton;

    @Bind(R.id.saving_message)
    TextView savingMessage;

    private File image;

    private String mGalleryPath;

    private String mTempImagePath;

    protected boolean photoCaptured = false;

    private static final int ACTION_ADD_PHOTO = 1;

    private static final String JPEG_FILE_PREFIX = "IMG_";

    private static final String JPEG_FILE_SUFFIX = ".jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_basic);

        ButterKnife.bind(this);

        String[] array = getResources().getStringArray(R.array.default_avatars);

        String randomImage = array[new Random().nextInt(array.length)];

        int avatarId = getResources().getIdentifier(randomImage, "drawable", getPackageName());

        // Create new image file and path reference

        try {

            image = createImageFile(true);

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

        Picasso.with(this).load(image).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

        // Set click listeners

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfilePic(view);
            }
        });

    }

    private File createImageFile(boolean temp) throws IOException {

        File outputDir;

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

        outputDir = this.getCacheDir();

        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, outputDir);

    }

    public void addProfilePic(View v) {

        startActivityForResult(new Intent(this, PhotoActivity.class), ACTION_ADD_PHOTO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ACTION_ADD_PHOTO:

                if (resultCode == RESULT_OK) {

                    mTempImagePath = data.getStringExtra("file_path");

                    if (mTempImagePath != null) {

                        photoCaptured = true;

                        userAvatar.setVisibility(View.VISIBLE);

                        File photo = new File(mTempImagePath);

                        Picasso.with(this).load(photo).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

                    }

                }

                break;

        }

    }

    public void saveProfile(View view) {

        if (photoCaptured) {

            savingMessage.setVisibility(View.VISIBLE);

            savingMessage.setText(getResources().getString(R.string.saving_profile));

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

                            // Retrieve the image id and add relation to PATCH request body

                            Map<String, Object> userPatch = new HashMap<String, Object>();

                            final Map<String, Integer> image_id = new HashMap<String, Integer>();

                            image_id.put("id", imageProperties.id);

                            List<Map<String, Integer>> images = new ArrayList<Map<String, Integer>>();

                            images.add(image_id);

                            userPatch.put("images", images);

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

                            userService.updateUser(access_token,
                                    "application/json",
                                    userId,
                                    userPatch,
                                    new Callback<User>() {
                                        @Override
                                        public void success(User user,
                                                            Response response) {

                                            // Immediately delete the cached image file now that we no longer need it

                                            File tempFile = new File(mTempImagePath);

                                            boolean imageDeleted = tempFile.delete();

                                            Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

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
                                        public void failure(RetrofitError error) {
                                            savingMessage.setVisibility(View.GONE);
                                            savingMessage.setText(getResources().getString(R.string.save));
                                        }

                                    });

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            savingMessage.setVisibility(View.GONE);
                            savingMessage.setText(getResources().getString(R.string.save));
                        }

                    });

        } else {

            Snackbar.make(parentLayout, "Please add a photo. You don't need to use a self-portrait, so feel free to pick something like your favorite animal.",
                    Snackbar.LENGTH_SHORT)
                    .show();

        }

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
