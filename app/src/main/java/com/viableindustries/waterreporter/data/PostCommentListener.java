package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.CommentActivity;
import com.viableindustries.waterreporter.MapDetailActivity;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostCommentListener implements View.OnClickListener {

    private Context context;

    private Report post;

    public PostCommentListener(Context context, Report post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        ReportHolder.setReport(post);

        Intent intent = new Intent(context, CommentActivity.class);

        context.startActivity(intent);

    }

}
