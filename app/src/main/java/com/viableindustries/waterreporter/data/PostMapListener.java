package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.MapDetailActivity;
import com.viableindustries.waterreporter.TerritoryMapActivity;
import com.viableindustries.waterreporter.UserProfileActivity;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostMapListener implements View.OnClickListener {

    private Context context;

    private Report post;

    public PostMapListener(Context context, Report post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        ReportHolder.setReport(post);

        TerritoryHolder.setTerritory(post.properties.territory);

        Intent intent = new Intent(context, TerritoryMapActivity.class);

        context.startActivity(intent);

    }

}
