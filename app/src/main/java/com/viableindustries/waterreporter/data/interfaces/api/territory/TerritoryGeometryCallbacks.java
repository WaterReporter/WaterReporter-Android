package com.viableindustries.waterreporter.data.interfaces.api.territory;

import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.data.objects.territory.HucFeature;

import retrofit.RetrofitError;

/**
 * Created by brendanmcintyre on 8/24/17.
 */

public interface TerritoryGeometryCallbacks  {

    void onSuccess(@NonNull HucFeature value);

    void onError(@NonNull RetrofitError error);

}
