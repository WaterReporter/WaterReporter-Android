package com.viableindustries.waterreporter;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.data.HucFeature;

import retrofit.RetrofitError;

/**
 * Created by brendanmcintyre on 8/24/17.
 */

interface TerritoryGeometryCallbacks  {

    void onSuccess(@NonNull HucFeature value);

    void onError(@NonNull RetrofitError error);

}
