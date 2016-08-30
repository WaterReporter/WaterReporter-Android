package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public interface OrganizationService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

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

    @GET("/data/organization/{organization}")
    public void getOrganization(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("organization") int organizationId,
                                @Query("q") String q,
                                Callback<Organization> organizationCallback);

    @GET("/data/organization/{organization}/reports")
    public void getOrganizationReports(@Header("Authorization") String authorization,
                                       @Header("Content-Type") String contentType,
                                       @Path("organization") int organizationId,
                                       @Query("page") int page,
                                       @Query("results_per_page") int numResults,
                                       @Query("q") String q,
                                       Callback<FeatureCollection> featureCollectionCallback);

    @GET("/data/organization/{organization}/users")
    public void getOrganizationMembers(@Header("Authorization") String authorization,
                                       @Header("Content-Type") String contentType,
                                       @Path("organization") int organizationId,
                                       @Query("results_per_page") int numResults,
                                       @Query("q") String q,
                                       Callback<UserFeatureCollection> userFeatureCollectionCallback);

}
