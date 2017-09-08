package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.TerritoryMapActivity;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostMapListener implements View.OnClickListener {

    private Context mContext;

    private Report post;

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
