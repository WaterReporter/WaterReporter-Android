package com.viableindustries.waterreporter.user_interface.view_holders;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.UserGroupsActivity;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.utilities.CircleTransform;

import jp.wasabeef.picasso.transformations.BlurTransformation;

import static java.lang.Boolean.TRUE;

/**
 * Created by brendanmcintyre on 9/12/17.
 */

public class UserProfileHeaderView {

    public TextView userName;
    public TextView userTitle;
    public TextView userDescription;
    public ImageView userAvatar;
    public ImageView headerCanvas;
    public TextView reportCounter;
    public TextView actionCounter;
    public TextView groupCounter;
//    public TextView reportCountLabel;
//    public TextView actionCountLabel;
//    public TextView groupCountLabel;
    public RelativeLayout reportStat;
    public RelativeLayout actionStat;
    public RelativeLayout groupStat;
    public LinearLayout promptBlock;
    public TextView promptMessage;
    public Button startPostButton;

    public interface UserProfileHeaderCallback {

        void resetStats();

        void showActions();

    }

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
        headerCanvas = (ImageView) header.findViewById(R.id.headerCanvas);
        reportCounter = (TextView) header.findViewById(R.id.reportCount);
        actionCounter = (TextView) header.findViewById(R.id.actionCount);
        groupCounter = (TextView) header.findViewById(R.id.groupCount);
//        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);
//        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);
//        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);
        reportStat = (RelativeLayout) header.findViewById(R.id.reportStat);
        actionStat = (RelativeLayout) header.findViewById(R.id.actionStat);
        groupStat = (RelativeLayout) header.findViewById(R.id.groupStat);

        String userTitleText = user.properties.title;
        String userDescriptionText = user.properties.description;
        String userNameText = String.format("%s %s", user.properties.first_name, user.properties.last_name);
        String userOrganization = user.properties.organization_name;

        // Locate valid avatar field

        String userAvatarUrl = user.properties.picture;

        if (userAvatarUrl.contains("b68f1074c0ed485ba0c30cba2632189f")) {

            headerCanvas.setBackgroundColor(ContextCompat.getColor(context, R.color.base_purple));

        } else if (userAvatarUrl.contains("c45aea8bfa7c4196ae24847a9920009c")) {

            headerCanvas.setBackgroundColor(ContextCompat.getColor(context, R.color.splash_blue));

        } else if (userAvatarUrl.contains("36d4719efa3d4c12aeaff27a1aa06521")) {

            headerCanvas.setBackgroundColor(ContextCompat.getColor(context, R.color.base_cyan));

        } else {

            Picasso.with(context)
                    .load(userAvatarUrl)
                    .transform(new BlurTransformation(context, 20, 1)).into(headerCanvas);

        }

        Picasso.with(context)
                .load(userAvatarUrl)
                .placeholder(R.drawable.user_avatar_placeholder_003)
                .transform(new CircleTransform()).into(userAvatar);

        userName.setText(userNameText);

        try {

            if (!userOrganization.isEmpty() && !userTitleText.isEmpty()) {

                userTitle.setText(String.format("%s at %s", userTitleText, userOrganization));

            } else if (!userOrganization.isEmpty() && userTitleText.isEmpty()) {

                userTitle.setText(userOrganization);

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

        reportStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                reportCounter.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
//                reportCountLabel.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
//
//                actionCounter.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));
//                actionCountLabel.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));

                resetStats(context, v);

            }
        });

        actionStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                actionCounter.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
//                actionCountLabel.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
//
//                reportCounter.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));
//                reportCountLabel.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));

                showActions(context, v);

            }
        });

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

    public void showActions(Context context, View view) {

        ((UserProfileHeaderView.UserProfileHeaderCallback) context).showActions();

    }

    public void resetStats(Context context, View view) {

        ((UserProfileHeaderView.UserProfileHeaderCallback) context).resetStats();

    }

}