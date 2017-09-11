package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 9/2/15.
 */
public class RegistrationBody {

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    public RegistrationBody (String aEmail, String aPassword) {

        this.email = aEmail;

        this.password = aPassword;

    }

}
