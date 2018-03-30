package com.viableindustries.waterreporter.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.MainActivity;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.image.SaveImageCallbacks;
import com.viableindustries.waterreporter.api.interfaces.data.post.SendPostCallbacks;
import com.viableindustries.waterreporter.api.models.image.ImageProperties;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportPostBody;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserBasicResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 9/5/17.
 */

public class ApiDispatcher {

    public static int getPendingPostId(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

        return sharedPreferences.getInt("POST_SAVED_VIA_SERVICE", 0);

    }

    public static void setTransmissionActive(SharedPreferences sharedPreferences, boolean isActive) {

        sharedPreferences.edit().putBoolean("TRANSMISSION_ACTIVE", isActive).apply();

    }

    public static boolean transmissionActive(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

        return sharedPreferences.getBoolean("TRANSMISSION_ACTIVE", false);

    }

    public static void sendFullPost(String accessToken, ReportPostBody reportPostBody, @NonNull final SendPostCallbacks callbacks) {

        RestClient.getReportService().postReport(accessToken, "application/json", reportPostBody,
                new Callback<Report>() {

                    @Override
                    public void success(Report post, Response response) {

                        callbacks.onSuccess(post);

                    }

                    @Override
                    public void failure(RetrofitError error) {

                        callbacks.onError(error);

                    }

                });


    }

    public static void saveImage(String accessToken, TypedFile typedFile, @NonNull final SaveImageCallbacks callbacks) {

        RestClient.getImageService().postImageAsync(accessToken, typedFile,
                new Callback<ImageProperties>() {

                    @Override
                    public void success(ImageProperties imageProperties, Response response) {

                        callbacks.onSuccess(imageProperties);

                    }

                    @Override
                    public void failure(RetrofitError error) {

                        callbacks.onError(error);

                    }

                });


    }

    public static void getAuthenticatedUser(final Context context, final SharedPreferences sharedPreferences) {

        final String accessToken = sharedPreferences.getString("access_token", "");

        // Since the user may have arrived here after re-installing the app and
        // bypassing the registration dialog, we need to check for the presence of
        // a user id. If we don't have one stored, retrieve it via the UserService
        // by including the token obtained just now upon successful log-in.

        RestClient.getUserService().getActiveUser(accessToken, "application/json",
                new Callback<UserBasicResponse>() {
                    @Override
                    public void success(UserBasicResponse userBasicResponse,
                                        Response response) {

                        sharedPreferences.edit().putInt("user_id", userBasicResponse.getUserId()).apply();

                        RestClient.getUserService().getUser(accessToken,
                                "application/json",
                                userBasicResponse.getUserId(),
                                new Callback<User>() {
                                    @Override
                                    public void success(User user,
                                                        Response response) {

                                        // Set flag confirming successful sign-in

                                        sharedPreferences.edit().putBoolean("clean_slate", true).apply();

                                        // Clear the app api cache

                                        CacheManager.deleteCache(context);

                                        // Store authenticated user data

                                        final SharedPreferences coreProfile = context.getSharedPreferences(
                                                context.getString(R.string.active_user_profile_key),
                                                MODE_PRIVATE
                                        );

                                        ModelStorage.storeModel(coreProfile, user, "auth_user");

                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        //
                                        context.startActivity(new Intent(context, MainActivity.class));

                                    }
                                });

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //
                        context.startActivity(new Intent(context, MainActivity.class));

                    }
                });


    }

}
