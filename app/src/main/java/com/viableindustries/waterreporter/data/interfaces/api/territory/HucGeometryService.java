package com.viableindustries.waterreporter.data.interfaces.api.territory;

import com.viableindustries.waterreporter.data.objects.territory.HucGeometryCollection;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

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
                            CancelableCallback<HucGeometryCollection> hucGeometryCollectionCallback);

}
