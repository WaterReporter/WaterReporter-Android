package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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
import com.viableindustries.waterreporter.SignInActivity;
import com.viableindustries.waterreporter.TagProfileActivity;
import com.viableindustries.waterreporter.api.interfaces.data.comment.DeleteCommentCallbacks;
import com.viableindustries.waterreporter.api.interfaces.data.comment.DeleteCommentSuccessCallback;
import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.post.ReportPhoto;
import com.viableindustries.waterreporter.user_interface.listeners.UserProfileListener;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.PatternEditableBuilder;

import java.util.List;
import java.util.regex.Pattern;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private final Context mContext;

    private final View mParentLayout;

    private final SharedPreferences mSharedPreferences;

    protected String groupList;

    public CommentAdapter(Context aContext, SharedPreferences sharedPreferences, View parentLayout, List<Comment> features) {
        super(aContext, 0, features);
        this.mContext = aContext;
        this.mParentLayout = parentLayout;
        this.mSharedPreferences = sharedPreferences;
    }

    static class ViewHolder {
        TextView reportDate;
        TextView reportOwner;
        TextView reportCaption;
        ImageView ownerAvatar;
        ImageView postThumb;
        LinearLayout actionTaken;
        ImageView actionBadge;
        LinearLayout reportStub;
        TextView tracker;

        // Open Graph

        CardView openGraphData;
        ImageView ogImage;
        TextView ogTitle;
        TextView ogDescription;
        TextView ogUrl;

        // Delete comment

        RelativeLayout deleteComment;

    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_container, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.reportDate = (TextView) convertView.findViewById(R.id.post_date);
            viewHolder.reportOwner = (TextView) convertView.findViewById(R.id.post_owner);
            viewHolder.reportCaption = (TextView) convertView.findViewById(R.id.post_caption);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
            viewHolder.postThumb = (ImageView) convertView.findViewById(R.id.postThumb);
            viewHolder.actionBadge = (ImageView) convertView.findViewById(R.id.action_badge);
            viewHolder.actionTaken = (LinearLayout) convertView.findViewById(R.id.action_taken);
            viewHolder.reportStub = (LinearLayout) convertView.findViewById(R.id.post_stub);
            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            // Open Graph

            viewHolder.openGraphData = (CardView) convertView.findViewById(R.id.ogData);
            viewHolder.ogImage = (ImageView) convertView.findViewById(R.id.ogImage);
            viewHolder.ogTitle = (TextView) convertView.findViewById(R.id.ogTitle);
            viewHolder.ogDescription = (TextView) convertView.findViewById(R.id.ogDescription);
            viewHolder.ogUrl = (TextView) convertView.findViewById(R.id.ogUrl);

            // Delete comment

            viewHolder.deleteComment = (RelativeLayout) convertView.findViewById(R.id.deleteComment);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the api item for this position
        final Comment feature = getItem(position);

        try {

            ReportPhoto image = feature != null ? feature.properties.images.get(0) : null;

            String imagePath = image != null ? image.properties.square_retina : null;

            viewHolder.postThumb.setVisibility(View.VISIBLE);

            if (feature != null && feature.properties.open_graph.size() < 1) {

                Picasso.with(mContext).load(imagePath).fit().centerCrop().into(viewHolder.postThumb);

            }

        } catch (IndexOutOfBoundsException ib) {

            viewHolder.postThumb.setVisibility(View.GONE);

        }

        String creationDate = (String) AttributeTransformUtility.relativeTime(feature != null ? feature.properties.created : null);

        Integer featureId = feature.id;

        viewHolder.tracker.setText(String.valueOf(featureId));

        // Attach click listeners to active UI components

        viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

        viewHolder.reportOwner.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

        // Populate the api into the template view using the api object
        viewHolder.reportDate.setText(creationDate);

        viewHolder.reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));

        if (feature.properties.body != null && (feature.properties.body.length() > 0)) {

            viewHolder.reportCaption.setVisibility(View.VISIBLE);

            viewHolder.reportCaption.setText(feature.properties.body.trim());

            new PatternEditableBuilder().
                    addPattern(mContext, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(mContext, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(mContext, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    mContext.startActivity(intent);

                                }
                            }).into(viewHolder.reportCaption);

        } else {

            viewHolder.reportCaption.setVisibility(View.GONE);

        }

        // Display badge if report is closed

        if ("closed".equals(feature.properties.post_state)) {

            viewHolder.actionTaken.setVisibility(View.VISIBLE);

        } else {

            viewHolder.actionTaken.setVisibility(View.GONE);

        }

        Picasso.with(mContext).load(feature.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.ownerAvatar);

        // Display Open Graph information, if any

        TimelineAdapterHelpers.setCommentOpenGraph(
                mContext,
                feature,
                viewHolder.openGraphData,
                viewHolder.ogImage,
                viewHolder.ogTitle,
                viewHolder.ogDescription,
                viewHolder.ogUrl);

        // Display delete affordance if the authenticated user owns this comment

        if (mSharedPreferences.getInt("user_id", 0) == feature.properties.owner_id) {

            viewHolder.deleteComment.setVisibility(View.VISIBLE);

            viewHolder.deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    TimelineAdapterHelpers.deleteComment(mContext, feature, new DeleteCommentCallbacks() {

                        @Override
                        public void onSuccess(@NonNull Response response) {

                            ((DeleteCommentSuccessCallback) mContext).onCommentDelete(feature);

                        }

                        @Override
                        public void onError(@NonNull RetrofitError error) {

                            Response errorResponse = error.getResponse();

                            // If we have a valid response object, check the status code and redirect to log in view if necessary

                            if (errorResponse != null) {

                                int status = errorResponse.getStatus();

                                if (status == 403) {

                                    mContext.startActivity(new Intent(mContext, SignInActivity.class));

                                } else {

                                    Snackbar.make(mParentLayout, "Unable to delete comment.",
                                            Snackbar.LENGTH_SHORT)
                                            .show();

                                }

                            }

                        }

                    });

                }
            });

        } else {

            viewHolder.deleteComment.setVisibility(View.GONE);

        }

        // Return the completed view to render on screen
        return convertView;

    }

}