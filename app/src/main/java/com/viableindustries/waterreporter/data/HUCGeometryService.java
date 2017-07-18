package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public interface HUCGeometryService {

    final String ENDPOINT = "https://huc.waterreporter.org/8";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/{code}.json")
    public void getGeometry(@Header("Content-Type") String contentType,
                            @Path("code") String code,
                            Callback<HUCGeometryCollection> hucGeometryCollectionCallback);

}
