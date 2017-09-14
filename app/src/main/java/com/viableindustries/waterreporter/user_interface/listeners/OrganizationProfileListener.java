package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.OrganizationProfileActivity;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationHolder;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

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

        // Write model to temporary storage in SharedPreferences

        ModelStorage.storeModel(mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE), organization, "stored_group");

        mContext.startActivity(intent);

    }

}