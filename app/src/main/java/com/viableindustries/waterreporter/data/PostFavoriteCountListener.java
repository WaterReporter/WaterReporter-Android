package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.CommentActivity;
import com.viableindustries.waterreporter.PostFavoriteListActivity;

/**
 * Created by brendanmcintyre on 8/17/17.
 */

public class PostFavoriteCountListener implements View.OnClickListener {

    private Context context;

    private Report post;

    public PostFavoriteCountListener(Context context, Report post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        ReportHolder.setReport(post);

        Intent intent = new Intent(context, PostFavoriteListActivity.class);

        context.startActivity(intent);

    }

}
