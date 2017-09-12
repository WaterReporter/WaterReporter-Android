package com.viableindustries.waterreporter.api.interfaces.data.territory;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.api.models.territory.HucFeature;

import retrofit.RetrofitError;

/**
 * Created by brendanmcintyre on 8/24/17.
 */

public interface TerritoryGeometryCallbacks  {

    void onSuccess(@NonNull HucFeature value);

    void onError(@NonNull RetrofitError error);

}
