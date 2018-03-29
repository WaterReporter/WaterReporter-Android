package com.viableindustries.waterreporter.api.interfaces.data.image;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.api.models.image.ImageProperties;
import com.viableindustries.waterreporter.api.models.post.Report;

import retrofit.RetrofitError;

/**
 * Created by brendanmcintyre on 3/29/18.
 */

public interface SaveImageCallbacks {

    void onSuccess(@NonNull ImageProperties imageProperties);

    void onError(@NonNull RetrofitError error);

}
