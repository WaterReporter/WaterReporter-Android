package com.viableindustries.waterreporter.api.interfaces.data.post;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.api.models.post.Report;

import retrofit.RetrofitError;

/**
 * Created by brendanmcintyre on 9/6/17.
 */

public interface SendPostCallbacks {

    void onSuccess(@NonNull Report post);

    void onError(@NonNull RetrofitError error);

}
