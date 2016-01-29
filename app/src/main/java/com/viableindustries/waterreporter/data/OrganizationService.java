package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public interface OrganizationService {

    final String ENDPOINT = "http://stg.api.waterreporter.org/v1";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/organization")
    public void getOrganizations(@Header("Authorization") String authorization,
                                 @Header("Content-Type") String contentType,
                                 @Query("results_per_page") int numResults,
                                 @Query("q") String q,
                                 Callback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

}
