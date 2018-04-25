package com.viableindustries.waterreporter.api.interfaces.data.user;

import com.viableindustries.waterreporter.api.models.group.GroupFeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.role.RoleListResponse;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserBasicResponse;
import com.viableindustries.waterreporter.api.models.user.UserCollection;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public interface UserService {

    @GET("/data/me")
    void getActiveUser(@Header("Authorization") String authorization,
                       @Header("Content-Type") String contentType,
                       Callback<UserBasicResponse> userResponseCallback);

    @GET("/data/user")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 Callback<UserCollection> userCollectionCallback);

    @GET("/data/user")
    void getUsers(@Header("Authorization") String authorization,
                  @Header("Content-Type") String contentType,
                  @Query("page") int page,
                  @Query("results_per_page") int numResults,
                  @Query("q") String q,
                  Callback<UserCollection> userCollectionCallback);

    @GET("/data/user/{user}")
    void getUser(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("user") int user_id,
                 Callback<User> userResponseCallback);

    @GET("/data/user/{user}/roles")
    void getUserRoles(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("user") int user_id,
                 Callback<RoleListResponse> roleListResponseCallback);

    @PATCH("/data/user/{user}")
    void updateUser(@Header("Authorization") String authorization,
                    @Header("Content-Type") String contentType,
                    @Path("user") int user_id,
                    @Body Map<String, ?> userPatch,
                    Callback<User> userResponseCallback);

    @PATCH("/data/user/{user}")
    void updateUserMemberships(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("user") int user_id,
                                @Body Map<String, List> userPatch,
                                Callback<User> userResponseCallback);

    @GET("/data/user/{user}/organization")
    void getUserOrganizations(@Header("Authorization") String authorization,
                             @Header("Content-Type") String contentType,
                             @Path("user") int user_id,
                             Callback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/data/user/{user}/groups")
    void getUserGroups(@Header("Authorization") String authorization,
                             @Header("Content-Type") String contentType,
                             @Path("user") int user_id,
                             Callback<GroupFeatureCollection> groupFeatureCollectionCallback);

}