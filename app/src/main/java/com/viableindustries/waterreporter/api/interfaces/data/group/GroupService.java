package com.viableindustries.waterreporter.api.interfaces.data.group;

import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.group.Group;
import com.viableindustries.waterreporter.api.models.group.GroupFeatureCollection;
import com.viableindustries.waterreporter.api.models.user.UserCollection;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 10/16/17.
 */

public interface GroupService {

    @GET("/data/group")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 Callback<GroupFeatureCollection> groupCollectionResponseCallback);

    @GET("/data/group")
    void getGroups(@Header("Authorization") String authorization,
                          @Header("Content-Type") String contentType,
                          @Query("page") int page,
                          @Query("results_per_page") int numResults,
                          @Query("q") String q,
                          Callback<GroupFeatureCollection> groupCollectionResponseCallback);

    @GET("/data/group/{group}")
    void getGroup(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("group") int groupId,
                         @Query("q") String q,
                         Callback<Group> groupCallback);

    @GET("/data/group/{group}/reports")
    void getGroupReports(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("group") int groupId,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                @Query("q") String q,
                                Callback<FeatureCollection> featureCollectionCallback);

    @GET("/data/group/{group}/users")
    void getGroupMembers(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("group") int groupId,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                @Query("q") String q,
                                Callback<UserCollection> userCollectionCallback);

}