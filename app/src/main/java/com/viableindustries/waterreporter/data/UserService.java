package com.viableindustries.waterreporter.data;

import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.Path;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public interface UserService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/me")
    public void getActiveUser(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        Callback<UserBasicResponse> userResponseCallback);

    @GET("/data/user/{user}")
    public void getUser(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Path("user") int user_id,
                           //@Body Map<String, Object> userPatch,
                           Callback<User> userResponseCallback);

    @PATCH("/data/user/{user}")
    public void updateUser(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Path("user") int user_id,
                           @Body Map<String, Object> userPatch,
                           Callback<User> userResponseCallback);

    @PATCH("/data/user/{user}")
    public void updateUserOrganization(@Header("Authorization") String authorization,
                                       @Header("Content-Type") String contentType,
                                       @Path("user") int user_id,
                                       @Body Map<String, Map> userPatch,
                                       Callback<User> userResponseCallback);

    @GET("/data/user/{user}/organization")
    public void getUserOrganization(@Header("Authorization") String authorization,
                                    @Header("Content-Type") String contentType,
                                    @Path("user") int user_id,
                                    Callback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

}

