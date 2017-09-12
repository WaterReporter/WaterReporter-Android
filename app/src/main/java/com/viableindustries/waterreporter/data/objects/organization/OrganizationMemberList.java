package com.viableindustries.waterreporter.data.objects.organization;

import com.viableindustries.waterreporter.data.objects.user.User;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class OrganizationMemberList {

    private static ArrayList<User> memberList;

    public static ArrayList<User> getList() {

        return memberList;

    }

    public static void setList(ArrayList<User> memberList) {

        OrganizationMemberList.memberList = memberList;

    }

}
