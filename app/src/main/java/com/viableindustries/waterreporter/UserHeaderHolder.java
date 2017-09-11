package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by brendanmcintyre on 8/4/17.
 */

class UserHeaderHolder extends RecyclerView.ViewHolder {

    private final LinearLayout mProfile;

    private final TextView mName;

    private final TextView mTitle;

    private final TextView mDescription;

    private final ImageView mAvatar;

    public UserHeaderHolder(LinearLayout v) {

        super(v);
        mProfile = v;
        mName = (TextView) v.findViewById(R.id.userName);
        mTitle = (TextView) v.findViewById(R.id.userTitle);
        mDescription = (TextView) v.findViewById(R.id.userDescription);
        mAvatar = (ImageView) v.findViewById(R.id.userAvatar);

    }

    public void bindData(final Context context, String name, String title, String description, String avatarUrl) {

        mName.setText(name);

        mTitle.setText(title);

        mDescription.setText(description);

        mDescription.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ObjectAnimator animation;

                int maxLines = TextViewCompat.getMaxLines(mDescription);

                int collapsedMaxLines = 3;

                int lineCount = mDescription.getLineCount();

                int duration = (lineCount - collapsedMaxLines) > 0 ? (lineCount - collapsedMaxLines) * 10 : 100;

                Log.d("duration", String.valueOf(duration));

                if (maxLines == collapsedMaxLines) {

                    animation = ObjectAnimator.ofInt(
                            mDescription,
                            "maxLines",
                            collapsedMaxLines,
                            20);

                } else {

                    animation = ObjectAnimator.ofInt(
                            mDescription,
                            "maxLines",
                            100,
                            collapsedMaxLines);

                }

                animation.setDuration(duration);
                animation.setInterpolator(new LinearOutSlowInInterpolator());
                animation.start();

            }

        });

        Picasso.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.user_avatar_placeholder_003)
                .transform(new CircleTransform()).into(mAvatar);

    }

}
