package com.viableindustries.waterreporter.data;

import java.util.Map;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public interface SecurityService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @Headers({"Content-Type: application/json"})
    @POST("/auth/remote")
    void save(@Body LogInBody logInBody,
              CancelableCallback<AuthResponse> authResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/user/register")
    void register(@Body RegistrationBody registrationBody,
                  CancelableCallback<RegistrationResponse> registrationResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/reset")
    void reset
            (@Body Map<String, String> resetBody,
             CancelableCallback<RegistrationResponse> cb);

}

