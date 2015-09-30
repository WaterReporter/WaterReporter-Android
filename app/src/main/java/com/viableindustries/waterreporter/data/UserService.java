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

    final String ENDPOINT = "http://api.waterreporter.org";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/v1/data/me")
    public void getUser(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        Callback<UserBasicResponse> userResponseCallback);

    @PATCH("/v1/data/user/{user}")
    public void updateUser(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Path("user") int user_id,
                           @Body Map<String, String> userPatch,
                           Callback<User> userResponseCallback);


}
