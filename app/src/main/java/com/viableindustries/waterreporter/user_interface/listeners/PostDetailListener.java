package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.PostDetailActivity;
import com.viableindustries.waterreporter.data.objects.post.Report;
import com.viableindustries.waterreporter.data.objects.post.ReportHolder;

/**
 * Created by brendanmcintyre on 8/28/17.
 */

public class PostDetailListener implements View.OnClickListener {

    private final Context mContext;

    private final Report post;

    public PostDetailListener(Context aContext, Report post) {
        this.mContext = aContext;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        ReportHolder.setReport(post);

        Intent intent = new Intent(mContext, PostDetailActivity.class);

        mContext.startActivity(intent);

    }

}