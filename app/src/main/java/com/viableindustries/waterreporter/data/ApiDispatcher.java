package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;

import com.viableindustries.waterreporter.MainActivity;
import com.viableindustries.waterreporter.PhotoMetaActivity;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.SendPostCallbacks;
import com.viableindustries.waterreporter.SignInActivity;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

        return  sharedPreferences.getBoolean("TRANSMISSION_ACTIVE", false);

    }

    public static void sendFullPost(String accessToken, ReportPostBody reportPostBody, @Nullable final SendPostCallbacks callbacks) {

        ReportService reportService = ReportService.restAdapter.create(ReportService.class);

        reportService.postReport(accessToken, "application/json", reportPostBody,
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

}
