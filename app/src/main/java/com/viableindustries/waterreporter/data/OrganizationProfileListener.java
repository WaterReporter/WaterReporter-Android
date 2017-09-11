package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.OrganizationProfileActivity;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class OrganizationProfileListener implements View.OnClickListener {

    private final Context mContext;

    private final Organization organization;

    public OrganizationProfileListener(Context aContext, Organization organization) {
        this.mContext = aContext;
        this.organization = organization;
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(mContext, OrganizationProfileActivity.class);

        OrganizationHolder.setOrganization(organization);

        mContext.startActivity(intent);

    }

}