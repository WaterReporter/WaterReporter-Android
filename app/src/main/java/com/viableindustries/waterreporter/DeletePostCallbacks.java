package com.viableindustries.waterreporter;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.data.Report;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 9/7/17.
 */

public interface DeletePostCallbacks {

    void onSuccess(@NonNull Response response);

    void onError(@NonNull RetrofitError error);

}
