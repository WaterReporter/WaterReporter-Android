package com.viableindustries.waterreporter.api.models.organization;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class OrganizationHolder {

    private static Organization organization;

    public static Organization getOrganization() {

        return organization;

    }

    public static void setOrganization(Organization organization) {

        OrganizationHolder.organization = organization;

    }

}
