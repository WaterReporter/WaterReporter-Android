package com.viableindustries.waterreporter.data;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;

import com.viableindustries.waterreporter.MainActivity;
import com.viableindustries.waterreporter.PhotoMetaActivity;
import com.viableindustries.waterreporter.R;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 9/5/17.
 */

public class ApiDispatcher {

    public static void sendFullPost(String accessToken, ReportPostBody reportPostBody) {

        ReportService reportService = ReportService.restAdapter.create(ReportService.class);

        reportService.postReport(accessToken, "application/json", reportPostBody,
                new Callback<Report>() {
                    @Override
                    public void success(Report report,
                                        Response response) {
                        //
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //
                    }

                });


    }

}
