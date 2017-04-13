package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

import com.viableindustries.waterreporter.OrganizationProfileActivity;
import com.viableindustries.waterreporter.TerritoryActivity;

/**
 * Created by brendanmcintyre on 4/13/17.
 */

public class TerritoryProfileListener implements View.OnClickListener {

    private Context context;

    private Territory territory;

    public TerritoryProfileListener(Context context, @Nullable Territory territory) {
        this.context = context;
        this.territory = territory;
    }

    @Override
    public void onClick(View view) {

        if (territory != null) {

            Intent intent = new Intent(context, TerritoryActivity.class);

            TerritoryHolder.setTerritory(territory);

            context.startActivity(intent);

        }

    }

}
