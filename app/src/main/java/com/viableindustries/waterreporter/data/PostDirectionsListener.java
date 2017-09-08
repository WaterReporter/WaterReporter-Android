package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;

import java.util.List;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostDirectionsListener implements View.OnClickListener {

    private Context mContext;

    private Report post;

    public PostDirectionsListener(Context aContext, Report post) {
        this.mContext = aContext;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        // Retrieve post location

        Geometry geometry = post.geometry.geometries.get(0);

        // Build the intent
        Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", geometry.coordinates.get(1), geometry.coordinates.get(0)));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

        // Verify that the map intent resolves
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // If safe, dispatch map intent
        if (isIntentSafe) {
            mContext.startActivity(mapIntent);
        }

    }

}
