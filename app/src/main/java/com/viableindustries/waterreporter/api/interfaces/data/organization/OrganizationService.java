package com.viableindustries.waterreporter.api.interfaces.data.organization;

import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.user.UserCollection;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public interface OrganizationService {

    @GET("/data/organization")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/data/organization")
    void getOrganizations(@Header("Authorization") String authorization,
                          @Header("Content-Type") String contentType,
                          @Query("page") int page,
                          @Query("results_per_page") int numResults,
                          @Query("q") String q,
                          CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/data/organization/{organization}")
    void getOrganization(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("organization") int organizationId,
                         @Query("q") String q,
                         CancelableCallback<Organization> organizationCallback);

    @GET("/data/organization/{organization}/reports")
    void getOrganizationReports(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("organization") int organizationId,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                @Query("q") String q,
                                CancelableCallback<FeatureCollection> featureCollectionCallback);

    @GET("/data/organization/{organization}/users")
    void getOrganizationMembers(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("organization") int organizationId,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                @Query("q") String q,
                                CancelableCallback<UserCollection> userCollectionCallback);

}
