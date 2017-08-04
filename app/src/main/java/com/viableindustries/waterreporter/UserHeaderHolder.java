package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.FavoritePostBody;
import com.viableindustries.waterreporter.data.FavoriteService;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.OpenGraph;
import com.viableindustries.waterreporter.data.OpenGraphObject;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.PostCommentListener;
import com.viableindustries.waterreporter.data.PostDirectionsListener;
import com.viableindustries.waterreporter.data.PostMapListener;
import com.viableindustries.waterreporter.data.PostShareListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.TerritoryProfileListener;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 8/4/17.
 */

public class UserHeaderHolder extends RecyclerView.ViewHolder {

    private LinearLayout mProfile;

    private TextView mName;

    private TextView mTitle;

    private TextView mDescription;

    private ImageView mAvatar;

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

                if (maxLines == 2) {

                    mDescription.setEllipsize(null);

                    animation = ObjectAnimator.ofInt(
                            mDescription,
                            "maxLines",
                            2,
                            20);

                } else {

                    mDescription.setEllipsize(TextUtils.TruncateAt.END);

                    animation = ObjectAnimator.ofInt(
                            mDescription,
                            "maxLines",
                            20,
                            2);

                }

                animation.setDuration(100);
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
