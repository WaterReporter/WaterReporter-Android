package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.FieldBookActivity;
import com.viableindustries.waterreporter.PostDetailActivity;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 3/30/18.
 */

public class PostFieldBookListener implements View.OnClickListener {

    private final Context mContext;

    private final Report post;

    public PostFieldBookListener(Context aContext, Report post) {
        this.mContext = aContext;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        // Write model to temporary storage in SharedPreferences

        ModelStorage.storeModel(mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE), post, "stored_post");

        Intent intent = new Intent(mContext, FieldBookActivity.class);

        mContext.startActivity(intent);

    }

}
