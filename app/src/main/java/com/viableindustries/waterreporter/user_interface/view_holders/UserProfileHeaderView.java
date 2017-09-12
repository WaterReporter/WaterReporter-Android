package com.viableindustries.waterreporter.user_interface.view_holders;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.UserGroupsActivity;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.utilities.CircleTransform;

import static java.lang.Boolean.TRUE;

/**
 * Created by brendanmcintyre on 9/12/17.
 */

public class UserProfileHeaderView {

    public TextView userName;
    public TextView userTitle;
    public TextView userDescription;
    public ImageView userAvatar;
    public TextView reportCounter;
    public TextView actionCounter;
    public TextView groupCounter;
    public TextView reportCountLabel;
    public TextView actionCountLabel;
    public TextView groupCountLabel;
    public LinearLayout reportStat;
    public LinearLayout actionStat;
    public LinearLayout groupStat;
    public LinearLayout promptBlock;
    public TextView promptMessage;
    public Button startPostButton;

    public UserProfileHeaderView() {
    }

    public void buildHeader(final Context context, ViewGroup header, User user) {

        promptBlock = (LinearLayout) header.findViewById(R.id.promptBlock);
        promptMessage = (TextView) header.findViewById(R.id.prompt);
        startPostButton = (Button) header.findViewById(R.id.startPost);
        userName = (TextView) header.findViewById(R.id.userName);
        userTitle = (TextView) header.findViewById(R.id.userTitle);
        userDescription = (TextView) header.findViewById(R.id.userDescription);
        userAvatar = (ImageView) header.findViewById(R.id.userAvatar);
        reportCounter = (TextView) header.findViewById(R.id.reportCount);
        actionCounter = (TextView) header.findViewById(R.id.actionCount);
        groupCounter = (TextView) header.findViewById(R.id.groupCount);
        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);
        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);
        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);
        reportStat = (LinearLayout) header.findViewById(R.id.reportStat);
        actionStat = (LinearLayout) header.findViewById(R.id.actionStat);
        groupStat = (LinearLayout) header.findViewById(R.id.groupStat);

        String userTitleText = user.properties.title;
        String userDescriptionText = user.properties.description;
        String userNameText = String.format("%s %s", user.properties.first_name, user.properties.last_name);
        String userOrganization = user.properties.organization_name;

        // Locate valid avatar field

        String userAvatarUrl = user.properties.picture;

        Picasso.with(context)
                .load(userAvatarUrl)
                .placeholder(R.drawable.user_avatar_placeholder_003)
                .transform(new CircleTransform()).into(userAvatar);

        userName.setText(userNameText);

        try {

            if (!userOrganization.isEmpty()) {

                userTitle.setText(String.format("%s at %s", userTitleText, userOrganization));

            } else {

                userTitle.setText(userTitleText);

            }

        } catch (NullPointerException ne) {

            userTitle.setVisibility(View.GONE);

        }

        try {

            userDescription.setText(userDescriptionText);

        } catch (NullPointerException ne) {

            userDescription.setVisibility(View.GONE);

        }

        userDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectAnimator animation;

                int duration;

                int maxLines = TextViewCompat.getMaxLines(userDescription);

                if (maxLines == 2) {

                    userDescription.setEllipsize(null);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
                            "maxLines",
                            2,
                            1000);

                    duration = 400;

                } else {

                    userDescription.setEllipsize(TextUtils.TruncateAt.END);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
                            "maxLines",
                            1000,
                            2);

                    duration = 200;

                }

                animation.setDuration(duration);
                animation.setInterpolator(new LinearOutSlowInInterpolator());
                animation.start();

            }
        });

        // Attach click listeners to stat elements

        groupStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, UserGroupsActivity.class);

                intent.putExtra("GENERIC_USER", TRUE);

                context.startActivity(intent);

            }

        });

        header.setTag(this);

    }

}
