package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.UserProfileActivity;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class UserProfileListener implements View.OnClickListener {

    private Context context;

    private User user;

    public UserProfileListener(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    @Override
    public void onClick(View view) {

        if (user.properties.picture == null && user.properties.images.size() > 0) {

            user.properties.picture = user.properties.images.get(0).properties.thumbnail_retina;

        }

        UserHolder.setUser(user);

        Intent intent = new Intent(context, UserProfileActivity.class);

        context.startActivity(intent);

    }

}
