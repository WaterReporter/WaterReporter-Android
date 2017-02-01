package com.viableindustries.waterreporter;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.dialogs.CommentActionDialog;
import com.viableindustries.waterreporter.dialogs.CommentActionDialogListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialog;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.viableindustries.waterreporter.R.id.imageView;

public class TimelineAdapter extends ArrayAdapter {

    private final Context context;

    private final boolean isProfile;

    protected String creationDate;

    protected Integer featureId;

    protected String imagePath;

    protected String watershedName;

    protected List<String> groups;

    protected String groupList;

    protected String commentCount;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    public TimelineAdapter(Context context, List features, boolean isProfile) {
        super(context, 0, features);
        this.context = context;
        this.isProfile = isProfile;
    }

    protected static class ViewHolder {
        TextView reportDate;
        TextView reportOwner;
        TextView reportWatershed;
        TextView reportComments;
        TextView reportCaption;
        LinearLayout reportGroups;
        ImageView ownerAvatar;
        ImageView reportThumb;
        RelativeLayout actionBadge;
        LinearLayout reportStub;
        RelativeLayout locationIcon;
        RelativeLayout directionsIcon;
        RelativeLayout commentIcon;
        RelativeLayout shareIcon;
        RelativeLayout actionsEllipsis;
        TextView tracker;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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
            viewHolder.reportGroups = (LinearLayout) convertView.findViewById(R.id.reportGroups);
            viewHolder.reportThumb = (ImageView) convertView.findViewById(R.id.report_thumb);
            viewHolder.actionBadge = (RelativeLayout) convertView.findViewById(R.id.action_badge);
            viewHolder.reportStub = (LinearLayout) convertView.findViewById(R.id.report_stub);
            viewHolder.locationIcon = (RelativeLayout) convertView.findViewById(R.id.location_icon);
            viewHolder.directionsIcon = (RelativeLayout) convertView.findViewById(R.id.directions_icon);
            viewHolder.commentIcon = (RelativeLayout) convertView.findViewById(R.id.comment_icon);
            viewHolder.shareIcon = (RelativeLayout) convertView.findViewById(R.id.share_icon);
            viewHolder.actionsEllipsis = (RelativeLayout) convertView.findViewById(R.id.action_ellipsis);
            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the data item for this position
        final Report feature = (Report) getItem(position);

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

                Geometry geometry = feature.geometry.geometries.get(0);

                Log.d("geometry", geometry.toString());

                intent.putExtra("REPORT_LATITUDE", geometry.coordinates.get(1));
                intent.putExtra("REPORT_LONGITUDE", geometry.coordinates.get(0));

                intent.putExtra("REPORT_ID", feature.id);
                intent.putExtra("THUMBNAIL_URL", feature.properties.images.get(0).properties.icon_retina);
                intent.putExtra("FULL_IMAGE_URL", feature.properties.images.get(0).properties.square_retina);
                intent.putExtra("REPORT_CREATED", creationDate);

                try {

                    intent.putExtra("REPORT_DESCRIPTION", feature.properties.report_description.trim());

                } catch (NullPointerException ne) {

                    intent.putExtra("REPORT_DESCRIPTION", "");
                }
                intent.putExtra("REPORT_WATERSHED", watershedName);
                intent.putExtra("REPORT_GROUPS", groupList);
                intent.putExtra("COMMENT_COUNT", commentCount);
                intent.putExtra("USER_NAME", String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));
                intent.putExtra("USER_AVATAR", feature.properties.owner.properties.picture);
                intent.putExtra("STATUS", feature.properties.state);

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

        // Allow user to share report content with other applications

        viewHolder.shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Click Event", "Share button clicked.");

                Uri imageUri = null;

                try {

                    File image = FileUtils.createImageFile(context);

                    // Use FileProvider to comply with Android security requirements.
                    // See: https://developer.android.com/training/camera/photobasics.html
                    // https://developer.android.com/reference/android/os/FileUriExposedException.html

                    imageUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, image);

                    // Using v4 Support Library FileProvider and Camera intent on pre-Marshmallow devices
                    // requires granting FileUri permissions at runtime

                    context.grantUriPermission(context.getPackageName(), imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    BitmapDrawable drawable = (BitmapDrawable) viewHolder.reportThumb.getDrawable();

                    Bitmap bitmap = drawable.getBitmap();

                    Log.d("BitmapDrawable", bitmap.toString());

//                    FileOutputStream stream = new FileOutputStream(image + "/shared_image.jpg"); // overwrites this image every time

                    FileOutputStream stream = new FileOutputStream(image); // overwrites this image every time

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    stream.close();

//                    mTempImagePath = image.getAbsolutePath();
//
//                    InputStream inputStream = getResources().openRawResource(avatarId);
//
//                    OutputStream out = new FileOutputStream(image);
//
//                    byte buf[] = new byte[1024];
//                    int len;
//
//                    while ((len = inputStream.read(buf)) > 0)
//                        out.write(buf, 0, len);
//
//                    out.close();
//
//                    inputStream.close();

                } catch (Exception e) {

                    e.printStackTrace();

                    Log.d(null, "Save file error!");

                    return;

                }

//                BitmapDrawable drawable = (BitmapDrawable) viewHolder.reportThumb.getDrawable();
//
//                Bitmap bitmap = drawable.getBitmap();
//
//                Log.d("BitmapDrawable", bitmap.toString());

                // Build the intent

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);

                // Set MIME type of content

                shareIntent.setType("*/*");

                // Set flag for temporary read Uri permission

                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
//                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));

                // Add image content

                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

                // Add text body

                String snippet = (feature.properties.report_description != null) ? feature.properties.report_description.trim() : null;

                if (snippet != null && !snippet.isEmpty()) {

                    int snippetLength = snippet.length();

                    if (snippetLength > 100) {

                        snippet = String.format("%s\u2026", snippet.substring(0, 99).trim());

                    } else {

                        if (".".equals(snippet.substring(snippetLength - 1))) {

                            snippet = String.format("%s\u2026", snippet.substring(0, snippetLength - 1).trim());

                        } else {

                            snippet = String.format("%s\u2026", snippet);

                        }

                    }

                }

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.share_report_email_subject));
                shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getContext().getResources().getString(R.string.share_report_text_body),
                        snippet, String.valueOf(feature.id)));

//                shareIntent.setType("text/plain");

                // Verify it resolves
                PackageManager packageManager = getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(shareIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                // Start an activity if it's safe
                if (isIntentSafe) {

                    getContext().startActivity(Intent.createChooser(shareIntent, getContext().getResources().getText(R.string.share_report_chooser_title)));

                }

            }
        });

        // Populate the data into the template view using the data object
        viewHolder.reportDate.setText(creationDate);
        viewHolder.reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));
        viewHolder.reportWatershed.setText(watershedName);

        if (feature.properties.report_description != null && (feature.properties.report_description.length() > 0)) {

            viewHolder.reportCaption.setVisibility(View.VISIBLE);

            viewHolder.reportCaption.setText(feature.properties.report_description.trim());

        } else {

            viewHolder.reportCaption.setVisibility(View.GONE);

        }

        // Add clickable organization views, if any

        viewHolder.reportGroups.setVisibility(View.VISIBLE);

        viewHolder.reportGroups.removeAllViews();

        if (feature.properties.groups.size() > 0) {

            for (Organization organization : feature.properties.groups) {

                TextView groupName = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.related_group_item, parent, false);

                groupName.setText(organization.properties.name);

                groupName.setTag(organization);

                groupName.setOnClickListener(new OrganizationProfileListener(getContext(), organization));

                viewHolder.reportGroups.addView(groupName);

            }

        } else {

            viewHolder.reportGroups.setVisibility(View.GONE);

        }

        // Display badge if report is closed
        if ("closed".equals(feature.properties.state)) {

            viewHolder.actionBadge.setVisibility(View.VISIBLE);

            viewHolder.commentIcon.setVisibility(View.GONE);

        } else {

            viewHolder.actionBadge.setVisibility(View.GONE);

            viewHolder.commentIcon.setVisibility(View.VISIBLE);

        }

        // Set value of comment count string
        commentCount = AttributeTransformUtility.countComments(feature.properties.comments);

        viewHolder.reportComments.setText(commentCount);

        // Load report image and user avatar

        Log.v("url", imagePath);

        Picasso.with(context).load(feature.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.ownerAvatar);

        Picasso.with(context).load(imagePath).fit().centerCrop().into(viewHolder.reportThumb);

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

                        String[] options = res.getStringArray(R.array.report_action_options);

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

