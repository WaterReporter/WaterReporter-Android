package com.viableindustries.waterreporter.api.interfaces.data.territory;

import com.viableindustries.waterreporter.api.models.territory.HucGeometryCollection;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public interface HucGeometryService {

    String ENDPOINT = "https://huc.waterreporter.org/8";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/{code}")
    void getGeometry(@Header("Content-Type") String contentType,
                            @Path("code") String code,
                            Callback<HucGeometryCollection> hucGeometryCollectionCallback);

}
