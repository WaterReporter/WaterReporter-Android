package com.viableindustries.waterreporter.data.objects.user;

import com.viableindustries.waterreporter.data.objects.organization.Organization;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/22/16.
 */

public class UserGroupList {

    private static ArrayList<Organization> organizationList;

    public static ArrayList<Organization> getList() {

        return organizationList;

    }

    public static void setList(ArrayList<Organization> organizationList) {

        UserGroupList.organizationList = organizationList;

    }

}
