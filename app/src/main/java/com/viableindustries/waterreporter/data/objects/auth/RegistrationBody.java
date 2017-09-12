package com.viableindustries.waterreporter.data.objects.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 9/2/15.
 */
public class RegistrationBody {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public RegistrationBody (String aEmail, String aPassword) {

        this.email = aEmail;

        this.password = aPassword;

    }

}
