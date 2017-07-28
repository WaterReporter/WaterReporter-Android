package com.viableindustries.waterreporter;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.FavoritePostBody;
import com.viableindustries.waterreporter.data.FavoriteService;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.HashTag;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.TagProfileListener;
import com.viableindustries.waterreporter.data.TerritoryProfileListener;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.data.UserService;
import com.viableindustries.waterreporter.dialogs.CommentActionDialog;
import com.viableindustries.waterreporter.dialogs.CommentActionDialogListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialog;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;
import com.viableindustries.waterreporter.dialogs.ShareActionDialogListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.viableindustries.waterreporter.R.id.imageView;

public class TimelineAdapter extends ArrayAdapter<Report> {

    private final Context context;

    private final boolean isProfile;

    private List<Report> features;

    protected String creationDate;

    protected Integer featureId;

    protected String imagePath;

    protected String watershedName;

    protected List<String> groups;

    protected String groupList;

    protected int commentCount;

    protected int favoriteCount;

    protected SharedPreferences prefs;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    public TimelineAdapter(Activity activity, List<Report> aFeatures, boolean isProfile) {
        super(activity, 0, aFeatures);
        this.context = activity;
        this.isProfile = isProfile;
        features = aFeatures;
        prefs = context.getSharedPreferences(context.getPackageName(), 0);
    }

    protected static class ViewHolder {
        TextView reportDate;
        TextView reportOwner;
        TextView reportWatershed;
        TextView reportComments;
        TextView reportCaption;
        FlexboxLayout reportGroups;
        ImageView ownerAvatar;
        ImageView reportThumb;
        RelativeLayout actionBadge;
        LinearLayout reportStub;
        RelativeLayout locationIcon;
        RelativeLayout directionsIcon;
        RelativeLayout commentIcon;
        RelativeLayout favoriteIcon;
        RelativeLayout shareIcon;
        RelativeLayout actionsEllipsis;
        ImageView commentIconView;
        ImageView favoriteIconView;
        TextView favoriteCounter;
        TextView tracker;
    }

    protected void presentShareDialog(final Report report) {

        Resources res = context.getResources();

        String shareUrl = res.getString(R.string.share_post_url, report.id);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent, res.getText(R.string.share_report_chooser_title)));

    }

    private void addFavorite(final int position, final Report feature, final int currentCount, final TextView textView, final ImageView imageView) {

        // Retrieve API token

        final String accessToken = prefs.getString("access_token", "");

        // Build request object

        FavoritePostBody favoritePostBody = new FavoritePostBody(feature.id);

        FavoriteService service = FavoriteService.restAdapter.create(FavoriteService.class);

        service.addFavorite(accessToken, "application/json", favoritePostBody, new Callback<Favorite>() {

            @Override
            public void success(Favorite favorite, Response response) {

                int newCount = currentCount + 1;

                textView.setText(String.valueOf(newCount));

                // Make favorite icon opaque

                imageView.setAlpha(1.0f);

                // Change favorite icon color

                Drawable myIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
                myIcon.setColorFilter(ContextCompat.getColor(context, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);
                imageView.setImageDrawable(myIcon);

                // Replace the target post item with the
                // updated API response object

                feature.properties.favorites.add(favorite);
//                notifyDataSetChanged();

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    private void undoFavorite(final Report feature, final int favoriteId, final int currentCount, final TextView textView, final ImageView imageView) {

        // Retrieve API token

        final String accessToken = prefs.getString("access_token", "");

        // Build request object

        FavoriteService service = FavoriteService.restAdapter.create(FavoriteService.class);

        service.undoFavorite(accessToken, "application/json", favoriteId, new Callback<Void>() {

            @Override
            public void success(Void v, Response response) {

                if (currentCount == 1) {

                    textView.setText("");

                    // Make favorite icon opaque

                    imageView.setAlpha(0.4f);

                    // Change favorite icon color

                    Drawable myIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
                    myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    imageView.setImageDrawable(myIcon);

                } else {

                    int newCount = currentCount - 1;

                    textView.setText(String.valueOf(newCount));

                }

                for (Iterator<Favorite> iter = feature.properties.favorites.listIterator(); iter.hasNext(); ) {
                    Favorite favorite = iter.next();
                    if (favorite.properties.id == favoriteId) {
                        iter.remove();
                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.reportDate = (TextView) convertView.findViewById(R.id.report_date);
            viewHolder.reportOwner = (TextView) convertView.findViewById(R.id.report_owner);
            viewHolder.reportWatershed = (TextView) convertView.findViewById(R.id.report_watershed);
            viewHolder.reportComments = (TextView) convertView.findViewById(R.id.comment_count);
            viewHolder.reportCaption = (TextView) convertView.findViewById(R.id.report_caption);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
            viewHolder.reportGroups = (FlexboxLayout) convertView.findViewById(R.id.report_groups);
            viewHolder.reportThumb = (ImageView) convertView.findViewById(R.id.report_thumb);
            viewHolder.actionBadge = (RelativeLayout) convertView.findViewById(R.id.action_badge);
            viewHolder.reportStub = (LinearLayout) convertView.findViewById(R.id.report_stub);
            viewHolder.locationIcon = (RelativeLayout) convertView.findViewById(R.id.location_icon);
            viewHolder.directionsIcon = (RelativeLayout) convertView.findViewById(R.id.directions_icon);
            viewHolder.commentIcon = (RelativeLayout) convertView.findViewById(R.id.comment_icon);
            viewHolder.favoriteIcon = (RelativeLayout) convertView.findViewById(R.id.favorite_icon);
            viewHolder.shareIcon = (RelativeLayout) convertView.findViewById(R.id.share_icon);
            viewHolder.actionsEllipsis = (RelativeLayout) convertView.findViewById(R.id.action_ellipsis);
            viewHolder.commentIconView = (ImageView) convertView.findViewById(R.id.commentIconView);
            viewHolder.favoriteIconView = (ImageView) convertView.findViewById(R.id.favoriteIconView);
            viewHolder.favoriteCounter = (TextView) convertView.findViewById(R.id.favorite_count);
            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the data item for this position
        final Report feature = (Report) getItem(position);

        Log.d("target-report", feature.properties.toString());

        ReportPhoto image = (ReportPhoto) feature.properties.images.get(0);

        imagePath = (String) image.properties.square_retina;

        creationDate = (String) AttributeTransformUtility.relativeTime(feature.properties.created);

        featureId = (Integer) feature.id;

        viewHolder.tracker.setText(String.valueOf(featureId));

        // Extract watershed name, if any
        watershedName = AttributeTransformUtility.parseWatershedName(feature.properties.territory);

        // Extract group names, if any
        groupList = AttributeTransformUtility.groupListSize(feature.properties.groups);

        // Attach click listeners to active UI components

        viewHolder.commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(feature);

                Intent intent = new Intent(context, CommentActivity.class);

                context.startActivity(intent);

            }
        });

        viewHolder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve authenticated user's ID

                int authUserId = prefs.getInt("user_id", 0);

                // Loop over this post's list of favorites

                for (Favorite favorite : feature.properties.favorites) {

                    // The can only be one favorite per use per post,
                    // so if the `owner_id` matches the authenticated
                    // user's ID, target that favorite for removal.

                    if (favorite.properties.owner_id == authUserId) {

                        undoFavorite(feature,
                                favorite.properties.id,
                                feature.properties.favorites.size(),
                                viewHolder.favoriteCounter,
                                viewHolder.favoriteIconView);

                        return;

                    }

                }

                addFavorite(position, feature, feature.properties.favorites.size(), viewHolder.favoriteCounter, viewHolder.favoriteIconView);


            }
        });

        viewHolder.actionBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(feature);

                Intent intent = new Intent(context, CommentActivity.class);

                context.startActivity(intent);

            }
        });


        viewHolder.locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(feature);

                Intent intent = new Intent(context, MapDetailActivity.class);

                context.startActivity(intent);

            }
        });

        viewHolder.directionsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Geometry geometry = feature.geometry.geometries.get(0);

                Log.d("geometry", geometry.toString());

                // Build the intent
                Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", geometry.coordinates.get(1), geometry.coordinates.get(0)));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

                // Verify it resolves
                PackageManager packageManager = getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                // Start an activity if it's safe
                if (isIntentSafe) {
                    getContext().startActivity(mapIntent);
                }

            }
        });

        // Allow user to share report content on Facebook/Twitter
        // if either or both of those applications is installed

//        if (socialOptions != 0) {

        viewHolder.reportThumb.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                presentShareDialog(feature);

                return true;

            }

        });

        viewHolder.shareIcon.setVisibility(View.VISIBLE);

        viewHolder.shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                presentShareDialog(feature);

            }
        });

//        }

        // Populate the data into the template view using the data object
        viewHolder.reportDate.setText(creationDate);
        viewHolder.reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));

        // Display watershed name and add click listener if
        // a valid territory object is present

        viewHolder.reportWatershed.setText(watershedName);

        viewHolder.reportWatershed.setOnClickListener(new TerritoryProfileListener(getContext(), feature.properties.territory));

        if (feature.properties.report_description != null && (feature.properties.report_description.length() > 0)) {

            viewHolder.reportCaption.setVisibility(View.VISIBLE);

            viewHolder.reportCaption.setText(feature.properties.report_description.trim());

            new PatternEditableBuilder().
                    addPattern(context, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(context, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(context, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    context.startActivity(intent);

                                }
                            }).into(viewHolder.reportCaption);

        } else {

            viewHolder.reportCaption.setVisibility(View.GONE);

        }

        // Add clickable organization views, if any

        viewHolder.reportGroups.setVisibility(View.VISIBLE);

        viewHolder.reportGroups.removeAllViews();

        if (feature.properties.groups.size() > 0) {

            for (Organization organization : feature.properties.groups) {

//                TextView groupView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.related_group_item, parent, false);

                ImageView groupView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.related_group_item, parent, false);

//                groupView.setText(organization.properties.name);

                Picasso.with(context).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(groupView);

                groupView.setTag(organization);

                groupView.setOnClickListener(new OrganizationProfileListener(getContext(), organization));

                viewHolder.reportGroups.addView(groupView);

            }

        } else {

            viewHolder.reportGroups.setVisibility(View.GONE);

        }

        // Display badge if report is closed
        if ("closed".equals(feature.properties.state)) {

            viewHolder.actionBadge.setVisibility(View.VISIBLE);

        } else {

            viewHolder.actionBadge.setVisibility(View.GONE);

        }

        // Set value of comment count string
        commentCount = feature.properties.comments.size();

        if (commentCount > 0) {

            viewHolder.reportComments.setText(String.valueOf(commentCount));

            // Make comment icon opaque

            viewHolder.commentIconView.setAlpha(1.0f);

            // Change comment icon color

            Drawable myIcon = ContextCompat.getDrawable(context, R.drawable.ic_mode_comment_black_24dp);
            myIcon.setColorFilter(ContextCompat.getColor(context, R.color.splash_blue), PorterDuff.Mode.SRC_ATOP);
            viewHolder.commentIconView.setImageDrawable(myIcon);

        } else {

            viewHolder.reportComments.setText("");

            // Revert comment icon opacity

            viewHolder.commentIconView.setAlpha(0.4f);

            // Revert comment icon color

            Drawable myIcon = ContextCompat.getDrawable(context, R.drawable.ic_mode_comment_black_24dp);
            myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            viewHolder.commentIconView.setImageDrawable(myIcon);

        }

        favoriteCount = feature.properties.favorites.size();

        if (favoriteCount > 0) {

            viewHolder.favoriteCounter.setText(String.valueOf(favoriteCount));

            // Make favorite icon opaque

            viewHolder.favoriteIconView.setAlpha(1.0f);

            // Change favorite icon color

            Drawable myIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
            myIcon.setColorFilter(ContextCompat.getColor(context, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);
            viewHolder.favoriteIconView.setImageDrawable(myIcon);

        } else {

            viewHolder.favoriteCounter.setText("");

            // Revert favorite icon opacity

            viewHolder.favoriteIconView.setAlpha(0.4f);

            // Revert icon color

            Drawable myIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
            myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            viewHolder.favoriteIconView.setImageDrawable(myIcon);

        }

        // Load report image and user avatar

        Log.v("url", imagePath);

        Picasso.with(context).load(feature.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.ownerAvatar);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(context));

        viewHolder.reportThumb.setLayoutParams(layoutParams);

        Picasso.with(context).load(imagePath).fit().into(viewHolder.reportThumb);

        viewHolder.reportThumb.setTag(imagePath);

        // Context-dependent configuration

        if (!isProfile) {

            viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

            viewHolder.reportOwner.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

            viewHolder.actionsEllipsis.setVisibility(View.GONE);

        } else {

            // Here we're inside the profile context

            // Even within the profile context, we need to account for the fact that users will
            // take action on reports that they don't own. Therefore, profile routing should be
            // enabled when viewing a person's "actions" feed. We can determine the condition by
            // comparing the transient user id stored in the UserHolder class and the `owner_id`
            // field of the current report.

            if (UserHolder.getUser().properties.id != feature.properties.owner_id) {

                viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

                viewHolder.reportOwner.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

            }

            // Determine whether or not we can expose the "additional actions" ellipsis for access to edit/delete.
            // This is a slightly different condition from the above because the id comparison must be against
            // the id of the authenticated user.

            final SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

            if (prefs.getInt("user_id", 0) == feature.properties.owner_id) {

                viewHolder.actionsEllipsis.setVisibility(View.VISIBLE);

                viewHolder.actionsEllipsis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Resources res = context.getResources();

                        String[] options = res.getStringArray(R.array.post_action_options);

                        CharSequence[] renders = new CharSequence[2];

                        for (int i = 0; i < options.length; i++) {

                            renders[i] = HtmlCompat.fromHtml(options[i]);

                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setItems(renders, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ReportHolder.setReport(feature);

                                // The 'which' argument contains the index position
                                // of the selected item
                                ReportActionDialogListener activity = (ReportActionDialogListener) context;

                                activity.onSelectAction(which);

                            }
                        });

                        // Create the AlertDialog object and return it
                        builder.create().show();

                    }
                });

            } else {

                viewHolder.actionsEllipsis.setVisibility(View.GONE);

            }

        }

        // Return the completed view to render on screen
        return convertView;

    }

}

