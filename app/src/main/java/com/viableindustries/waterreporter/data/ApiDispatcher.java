package com.viableindustries.waterreporter.data;

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

/**
 * Created by brendanmcintyre on 9/5/17.
 */

public class ApiDispatcher {

    public static void setTransmissionActive(SharedPreferences sharedPreferences, boolean isActive) {

        sharedPreferences.edit().putBoolean("TRANSMISSION_ACTIVE", isActive).apply();

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
