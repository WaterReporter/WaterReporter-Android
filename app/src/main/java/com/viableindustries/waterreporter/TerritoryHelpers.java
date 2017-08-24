package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.viableindustries.waterreporter.data.HUCFeature;
import com.viableindustries.waterreporter.data.HUCGeometryCollection;
import com.viableindustries.waterreporter.data.HUCGeometryService;
import com.viableindustries.waterreporter.data.Territory;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 8/24/17.
 */

public class TerritoryHelpers {

//    private Territory mTerritory;
//
//    private HUCFeature mHucFeature;
//
//    private Context mContext;
//
//    public TerritoryHelpers(Context context, Territory territory){
//        this.mContext= context;
//        this.mTerritory = territory;
//    };

    public static void fetchTerritoryGeometry(final Context context, Territory territory, @Nullable final TerritoryGeometryCallbacks callbacks) {

        RestAdapter restAdapter = HUCGeometryService.restAdapter;

        HUCGeometryService service = restAdapter.create(HUCGeometryService.class);

        String code = AttributeTransformUtility.getTerritoryCode(territory);

        service.getGeometry("application/json", code, new Callback<HUCGeometryCollection>() {

            @Override
            public void success(HUCGeometryCollection hucGeometryCollection, Response response) {

                callbacks.onSuccess(hucGeometryCollection.features.get(0));

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        context.startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

}
