package com.viableindustries.waterreporter.utilities;

import com.viableindustries.waterreporter.data.objects.organization.Organization;

import java.util.Comparator;


public class GroupNameComparator implements Comparator<Organization> {
    @Override
    public int compare(Organization o1, Organization o2) {
        return o1.properties.name.compareTo(o2.properties.name);
    }
}
