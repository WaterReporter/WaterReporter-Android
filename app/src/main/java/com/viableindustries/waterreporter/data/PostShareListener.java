package com.viableindustries.waterreporter.data;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;

import com.viableindustries.waterreporter.CommentActivity;
import com.viableindustries.waterreporter.R;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostShareListener implements View.OnClickListener {

    private Context context;

    private Report post;

    public PostShareListener(Context context, Report post) {
        this.context = context;
        this.post = post;
    }

    @Override
    public void onClick(View view) {

        // Display the system share dialog

        Resources res = context.getResources();

        String shareUrl = res.getString(R.string.share_post_url, post.id);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, res.getText(R.string.share_post_chooser_title)));

    }

}
