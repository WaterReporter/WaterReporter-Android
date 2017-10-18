package com.viableindustries.waterreporter.api.interfaces.data.comment;

import android.support.annotation.NonNull;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 10/17/17.
 */

public interface DeleteCommentCallbacks {

    void onSuccess(@NonNull Response response);

    void onError(@NonNull RetrofitError error);

}
