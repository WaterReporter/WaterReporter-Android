package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.viableindustries.waterreporter.TerritoryMapActivity;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.territory.TerritoryHolder;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostMapListener implements View.OnClickListener {

    private final Context mContext;

    private final Report post;

    public PostMapListener(Context aContext, Report post) {
        this.mContext = aContext;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

//        ReportHolder.setReport(post);

//        TerritoryHolder.setTerritory(post.properties.territory);

        // Write models to temporary storage in SharedPreferences

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);

        ModelStorage.storeModel(sharedPreferences, post, "stored_post");

        ModelStorage.storeModel(sharedPreferences, post.properties.territory, "stored_territory");

        Intent intent = new Intent(mContext, TerritoryMapActivity.class);

        mContext.startActivity(intent);

    }

}
