package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.UserProfileActivity;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

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

//        if (user.properties.picture == null) {
//
//            if (user.properties.images != null) {
//
//                try {
//
//                    user.properties.picture = user.properties.images.get(0).properties.thumbnail_retina;
//
//                } catch (IndexOutOfBoundsException e) {
//
//                    user.properties.picture = null;
//
//                }
//
//            }
//
//        }

        // Write model to temporary storage in SharedPreferences

        ModelStorage.storeModel(mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE), user, "stored_user");

        Intent intent = new Intent(mContext, UserProfileActivity.class);

        mContext.startActivity(intent);

    }

}
