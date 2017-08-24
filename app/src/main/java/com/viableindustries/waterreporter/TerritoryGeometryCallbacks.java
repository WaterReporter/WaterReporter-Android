package com.viableindustries.waterreporter;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.data.HUCFeature;

import retrofit.RetrofitError;

/**
 * Created by brendanmcintyre on 8/24/17.
 */

public interface TerritoryGeometryCallbacks  {

    void onSuccess(@NonNull HUCFeature value);

    void onError(@NonNull RetrofitError error);

}
