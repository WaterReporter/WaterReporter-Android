package com.viableindustries.waterreporter.data;

import java.util.Map;

import retrofit.RestAdapter;
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

    String ENDPOINT = "https://api.waterreporter.org/v2";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

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

