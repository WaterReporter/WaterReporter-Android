package com.viableindustries.waterreporter.api.interfaces.data.territory;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.viableindustries.waterreporter.SignInActivity;
import com.viableindustries.waterreporter.api.models.territory.HucGeometryCollection;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 8/24/17.
 */

public final class TerritoryHelpers {

    public static void fetchTerritoryGeometry(final Context context, Territory territory, @NonNull final TerritoryGeometryCallbacks callbacks) {

        RestAdapter restAdapter = HucGeometryService.restAdapter;

        HucGeometryService service = restAdapter.create(HucGeometryService.class);

        String code = AttributeTransformUtility.getTerritoryCode(territory);

        service.getGeometry("application/json", code, new Callback<HucGeometryCollection>() {

            @Override
            public void success(HucGeometryCollection hucGeometryCollection, Response response) {

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
