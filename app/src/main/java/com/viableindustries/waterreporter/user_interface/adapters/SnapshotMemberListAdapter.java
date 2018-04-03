package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotShallowUser;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.dialogs.UserExtrasBottomSheetDialogFragment;
import com.viableindustries.waterreporter.user_interface.listeners.UserProfileListener;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.UtilityMethods;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class SnapshotMemberListAdapter extends ArrayAdapter<SnapshotShallowUser> {

    private final Context mContext;

    private final SharedPreferences mSharedPreferences;

    final private FragmentManager mFragmentManager;

    protected String name;

    protected int id;

    private final List<SnapshotShallowUser> sourceList;

    public SnapshotMemberListAdapter(Context aContext,
                                     List<SnapshotShallowUser> features,
                                     FragmentManager fragmentManager) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

        this.mSharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);

        this.mFragmentManager = fragmentManager;

    }

    private static class ViewHolder {
        ImageView featureIcon;
        TextView featureName;
        TextView postCount;
        RelativeLayout extraActions;
        ImageView extraActionsIconView;
        LinearLayout shallowMemberItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        SnapshotMemberListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_member_item, parent, false);

            viewHolder = new SnapshotMemberListAdapter.ViewHolder();

            viewHolder.featureIcon = (ImageView) convertView.findViewById(R.id.featureIcon);
            viewHolder.featureName = (TextView) convertView.findViewById(R.id.featureName);
            viewHolder.postCount = (TextView) convertView.findViewById(R.id.postCount);
            viewHolder.extraActions = (RelativeLayout) convertView.findViewById(R.id.extraActions);
            viewHolder.extraActionsIconView = (ImageView) convertView.findViewById(R.id.extraActionsIconView);
            viewHolder.shallowMemberItem = (LinearLayout) convertView.findViewById(R.id.shallowMemberItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (SnapshotMemberListAdapter.ViewHolder) convertView.getTag();

        }

        final SnapshotShallowUser campaignMember = sourceList.get(position);

        // Populate layout elements

//        viewHolder.postCount.setText(String.valueOf(campaignLeader.posts));

        viewHolder.featureName.setText(campaignMember.name);

        Picasso.with(mContext)
                .load(campaignMember.picture)
                .placeholder(R.drawable.user_avatar_placeholder)
                .transform(new CircleTransform())
                .into(viewHolder.featureIcon);

        String subText = UtilityMethods.makeSecondaryListPostCountText(mContext.getResources(),
                campaignMember.posts, campaignMember.last_active);

        viewHolder.postCount.setTextColor(ContextCompat.getColor(mContext, R.color.black_54));

        viewHolder.postCount.setText(subText);

        long timeDelta = UtilityMethods.timeDelta(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                campaignMember.last_active);

        if (timeDelta > 0 && timeDelta < (DateUtils.DAY_IN_MILLIS * 30)) {

            viewHolder.postCount.setTextColor(ContextCompat.getColor(mContext, R.color.post_count_orange));

        }

        //
        // Add click listeners to layout elements
        //

        final User user = User.createUser(campaignMember.id, null);

        viewHolder.shallowMemberItem.setOnClickListener(new UserProfileListener(mContext, user));

        //
        // Present extra actions dialog (bottom sheet)
        //

        viewHolder.extraActions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ModelStorage.storeModel(mSharedPreferences, user, "stored_user");

                UserExtrasBottomSheetDialogFragment userExtrasBottomSheetDialogFragment =
                        new UserExtrasBottomSheetDialogFragment();

                userExtrasBottomSheetDialogFragment.show(mFragmentManager, "user-extras-dialog");

            }

        });

        return convertView;

    }

}