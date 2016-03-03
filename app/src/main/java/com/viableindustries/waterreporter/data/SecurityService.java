package com.viableindustries.waterreporter.data;

import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public interface SecurityService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @Headers({"Content-Type: application/json"})
    @POST("/auth/remote")
    public void save(@Body LogInBody logInBody,
                           Callback<AuthResponse> authResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/user/register")
    public void register(@Body RegistrationBody registrationBody,
                         Callback<RegistrationResponse> registrationResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/reset")
    public void reset
            (@Body Map<String, String> resetBody,
             Callback<RegistrationResponse> cb);

}

