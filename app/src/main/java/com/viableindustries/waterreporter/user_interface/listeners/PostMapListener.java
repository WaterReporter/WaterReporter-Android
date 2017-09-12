package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.TerritoryMapActivity;
import com.viableindustries.waterreporter.data.objects.post.Report;
import com.viableindustries.waterreporter.data.objects.post.ReportHolder;
import com.viableindustries.waterreporter.data.objects.territory.TerritoryHolder;

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

        ReportHolder.setReport(post);

        TerritoryHolder.setTerritory(post.properties.territory);

        Intent intent = new Intent(mContext, TerritoryMapActivity.class);

        mContext.startActivity(intent);

    }

}
