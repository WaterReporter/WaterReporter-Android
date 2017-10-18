package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

import com.viableindustries.waterreporter.TerritoryActivity;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.api.models.territory.TerritoryHolder;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 4/13/17.
 */

public class TerritoryProfileListener implements View.OnClickListener {

    private final Context mContext;

    private final Territory territory;

    public TerritoryProfileListener(Context aContext, @Nullable Territory territory) {
        this.mContext = aContext;
        this.territory = territory;
    }

    @Override
    public void onClick(View view) {

        if (territory != null) {

            Intent intent = new Intent(mContext, TerritoryActivity.class);

//            TerritoryHolder.setTerritory(territory);

            // Write model to temporary storage in SharedPreferences

            ModelStorage.storeModel(mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE), territory, "stored_territory");

            mContext.startActivity(intent);

        }

    }

}
