package com.viableindustries.waterreporter.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.post.ReportService;
import com.viableindustries.waterreporter.api.interfaces.data.post.SendPostCallbacks;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportPostBody;

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

    public static void sendFullPost(String accessToken, ReportPostBody reportPostBody, @NonNull final SendPostCallbacks callbacks) {

        RestClient.getReportService().postReport(accessToken, "application/json", reportPostBody,
                new CancelableCallback<Report>() {

                    @Override
                    public void onSuccess(Report post, Response response) {

                        callbacks.onSuccess(post);

                    }

                    @Override
                    public void onFailure(RetrofitError error) {

                        callbacks.onError(error);

                    }

                });


    }

}
