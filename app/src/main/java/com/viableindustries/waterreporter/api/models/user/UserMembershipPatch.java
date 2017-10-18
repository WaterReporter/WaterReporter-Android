package com.viableindustries.waterreporter.api.models.user;

import com.viableindustries.waterreporter.api.models.group.Group;
import com.viableindustries.waterreporter.api.models.group.GroupProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public final class UserMembershipPatch {

    private UserMembershipPatch() {
    }

    private static String createJoinDate() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return format.format(calendar.getTime());

    }

    private static List<GroupProperties> buildGroupList(List<Group> groups, int userId, int orgId, String action) {

        List<GroupProperties> groupList = new ArrayList<>();

        for (Group group : groups) {

            if (action.equals("remove")) {

                // If the user is an admin of the group, retain their membership!

                if (group.properties.isAdmin || group.properties.organizationId != orgId) {

                    groupList.add(new GroupProperties(
                            group.properties.joined,
                            group.properties.organizationId,
                            group.properties.userId,
                            group.properties.isAdmin,
                            group.properties.isMember
                    ));

                }

            } else {

                groupList.add(new GroupProperties(
                        group.properties.joined,
                        group.properties.organizationId,
                        group.properties.userId,
                        group.properties.isAdmin,
                        group.properties.isMember
                ));

            }

        }

        if (action.equals("add")) {

            groupList.add(new GroupProperties(
                    createJoinDate(),
                    orgId,
                    userId,
                    false,
                    false
            ));

        }

        return groupList;

    }

    private static List<Map> buildOrganizationList(List<Group> groups, int orgId, String action) {

        List<Map> organizationList = new ArrayList<>();

        for (Group group : groups) {

            Map<String, Integer> organizationObject = new HashMap<>();

            if (action.equals("remove")) {

                if (group.properties.organizationId != orgId) {

                    organizationObject.put("id", group.properties.organizationId);

                    organizationList.add(organizationObject);

                }

            } else {

                organizationObject.put("id", group.properties.organizationId);

                organizationList.add(organizationObject);

            }

        }

        if (action.equals("add")) {

            Map<String, Integer> newOrganizationObject = new HashMap<>();

            newOrganizationObject.put("id", orgId);

            organizationList.add(newOrganizationObject);

        }

        return organizationList;

    }

    public static Map<String, List> buildRequest(List<Group> groups, int userId, int orgId, String action) {

        Map<String, List> requestData = new HashMap<>();

        requestData.put("groups", buildGroupList(groups, userId, orgId, action));

        requestData.put("organization", buildOrganizationList(groups, orgId, action));

        return requestData;

    }

}