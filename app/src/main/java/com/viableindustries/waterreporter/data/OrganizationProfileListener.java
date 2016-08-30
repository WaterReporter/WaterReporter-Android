package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.OrganizationProfileActivity;
import com.viableindustries.waterreporter.UserProfileActivity;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class OrganizationProfileListener implements View.OnClickListener {

    private Context context;

    private Organization organization;

    public OrganizationProfileListener(Context context, Organization organization) {
        this.context = context;
        this.organization = organization;
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(context, OrganizationProfileActivity.class);

        OrganizationHolder.setOrganization(organization);

        context.startActivity(intent);

    }

}