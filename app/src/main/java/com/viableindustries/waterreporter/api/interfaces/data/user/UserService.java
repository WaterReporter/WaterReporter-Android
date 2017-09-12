package com.viableindustries.waterreporter.api.interfaces.data.user;

import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserBasicResponse;
import com.viableindustries.waterreporter.api.models.user.UserCollection;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import java.util.Map;

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
                       CancelableCallback<UserBasicResponse> userResponseCallback);

    @GET("/data/user")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 CancelableCallback<UserCollection> userCollectionCallback);

    @GET("/data/user")
    void getUsers(@Header("Authorization") String authorization,
                  @Header("Content-Type") String contentType,
                  @Query("page") int page,
                  @Query("results_per_page") int numResults,
                  @Query("q") String q,
                  CancelableCallback<UserCollection> userCollectionCallback);

    @GET("/data/user/{user}")
    void getUser(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("user") int user_id,
                 CancelableCallback<User> userResponseCallback);

    @PATCH("/data/user/{user}")
    void updateUser(@Header("Authorization") String authorization,
                    @Header("Content-Type") String contentType,
                    @Path("user") int user_id,
                    @Body Map<String, ?> userPatch,
                    CancelableCallback<User> userResponseCallback);

    @PATCH("/data/user/{user}")
    void updateUserOrganization(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("user") int user_id,
                                @Body Map<String, Map> userPatch,
                                CancelableCallback<User> userResponseCallback);

    @GET("/data/user/{user}/organization")
    void getUserOrganization(@Header("Authorization") String authorization,
                             @Header("Content-Type") String contentType,
                             @Path("user") int user_id,
                             CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

}

