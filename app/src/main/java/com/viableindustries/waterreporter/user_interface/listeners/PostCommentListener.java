package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.CommentActivity;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostCommentListener implements View.OnClickListener {

    private final Context mContext;

    private final Report post;

    public PostCommentListener(Context aContext, Report post) {
        this.mContext = aContext;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        ReportHolder.setReport(post);

        // Write model to temporary storage in SharedPreferences

        ModelStorage.storeModel(mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE), post, "stored_post");

        Intent intent = new Intent(mContext, CommentActivity.class);

        mContext.startActivity(intent);

    }

}
