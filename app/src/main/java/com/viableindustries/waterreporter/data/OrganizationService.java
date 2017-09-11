package com.viableindustries.waterreporter.data;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public interface OrganizationService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/organization")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/organization")
    void getOrganizations(@Header("Authorization") String authorization,
                          @Header("Content-Type") String contentType,
                          @Query("page") int page,
                          @Query("results_per_page") int numResults,
                          @Query("q") String q,
                          CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/organization/{organization}")
    void getOrganization(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("organization") int organizationId,
                         @Query("q") String q,
                         CancelableCallback<Organization> organizationCallback);

    @GET("/organization/{organization}/reports")
    void getOrganizationReports(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("organization") int organizationId,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                @Query("q") String q,
                                CancelableCallback<FeatureCollection> featureCollectionCallback);

    @GET("/organization/{organization}/users")
    void getOrganizationMembers(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("organization") int organizationId,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                @Query("q") String q,
                                CancelableCallback<UserCollection> userCollectionCallback);

}
