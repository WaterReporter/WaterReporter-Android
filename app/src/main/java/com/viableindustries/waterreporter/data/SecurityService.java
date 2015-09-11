package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public interface SecurityService {

    final String ENDPOINT = "http://api.waterreporter.org";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @Headers({"Content-Type: application/json"})
    @POST("/v1/auth/remote")
    public void save(@Body LogInBody logInBody,
                           Callback<AuthResponse> authResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/v1/user/register")
    public void register(@Body RegistrationBody registrationBody,
                         Callback<RegistrationResponse> registrationResponseCallback);

//    @Headers({"Content-Type: application/json",
//            "Authorization: Bearer tbk7K6hlDITIH6mybfvgSZhBfBaR2c"})
//    @POST("//api.waterreporter.org/reset")
//    public void reset
//            (@Body ReportPostBody reportPostBody,
//             Callback<Report> cb);

}

