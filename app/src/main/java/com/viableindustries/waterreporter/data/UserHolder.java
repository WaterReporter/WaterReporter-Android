package com.viableindustries.waterreporter.data;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class UserHolder {

    private static User user;

    public static User getUser() {

        return user;

    }

    public static void setUser(User user) {

        UserHolder.user = user;

    }

}