package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.PostDetailActivity;

/**
 * Created by brendanmcintyre on 8/28/17.
 */

public class PostDetailListener implements View.OnClickListener {

    private Context context;

    private Report post;

    public PostDetailListener(Context context, Report post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        ReportHolder.setReport(post);

        Intent intent = new Intent(context, PostDetailActivity.class);

        context.startActivity(intent);

    }

}
