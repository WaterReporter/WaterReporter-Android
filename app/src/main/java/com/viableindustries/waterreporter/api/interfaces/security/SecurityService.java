package com.viableindustries.waterreporter.api.interfaces.security;

import com.viableindustries.waterreporter.api.models.auth.AuthResponse;
import com.viableindustries.waterreporter.api.models.auth.LogInBody;
import com.viableindustries.waterreporter.api.models.auth.RegistrationBody;
import com.viableindustries.waterreporter.api.models.auth.RegistrationResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public interface SecurityService {

    @Headers({"Content-Type: application/json"})
    @POST("/auth/remote")
    void save(@Body LogInBody logInBody,
              Callback<AuthResponse> authResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/user/register")
    void register(@Body RegistrationBody registrationBody,
                  Callback<RegistrationResponse> registrationResponseCallback);

    @Headers({"Content-Type: application/json"})
    @POST("/reset")
    void reset
            (@Body Map<String, String> resetBody,
             Callback<RegistrationResponse> cb);

}

