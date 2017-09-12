package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.UserProfileActivity;
import com.viableindustries.waterreporter.data.objects.user.User;
import com.viableindustries.waterreporter.data.objects.user.UserHolder;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class UserProfileListener implements View.OnClickListener {

    private final Context mContext;

    private final User user;

    public UserProfileListener(Context aContext, User user) {
        this.mContext = aContext;
        this.user = user;
    }

    @Override
    public void onClick(View view) {

        if (user.properties.picture == null) {

            if (user.properties.images != null) {

                try {

                    user.properties.picture = user.properties.images.get(0).properties.thumbnail_retina;

                } catch (IndexOutOfBoundsException e) {

                    user.properties.picture = null;

                }

            }

        }

        UserHolder.setUser(user);

        Intent intent = new Intent(mContext, UserProfileActivity.class);

        mContext.startActivity(intent);

    }

}
