package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.FavoritePostBody;
import com.viableindustries.waterreporter.data.FavoriteService;
import com.viableindustries.waterreporter.data.Geometry;
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
import java.util.List;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder

public class PostCardHolder extends RecyclerView.ViewHolder {

    LinearLayout postCard;
    TextView postDate;
    TextView postOwner;
    TextView postWatershed;
    TextView postComments;
    TextView postCaption;
    FlexboxLayout postGroups;
    ImageView ownerAvatar;
    ImageView postThumb;
    RelativeLayout actionBadge;
    LinearLayout postStub;
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

    // Open Graph

    CardView openGraphData;
    ImageView ogImage;
    TextView ogTitle;
    TextView ogDescription;
    TextView ogUrl;

    public PostCardHolder(LinearLayout v) {

        super(v);
        postCard = v;

        postDate = (TextView) v.findViewById(R.id.post_date);
        postOwner = (TextView) v.findViewById(R.id.post_owner);
        postWatershed = (TextView) v.findViewById(R.id.post_watershed);
        postComments = (TextView) v.findViewById(R.id.comment_count);
        postCaption = (TextView) v.findViewById(R.id.postCaption);
        ownerAvatar = (ImageView) v.findViewById(R.id.owner_avatar);
        postGroups = (FlexboxLayout) v.findViewById(R.id.post_groups);
        postThumb = (ImageView) v.findViewById(R.id.post_thumb);
        actionBadge = (RelativeLayout) v.findViewById(R.id.action_badge);
        postStub = (LinearLayout) v.findViewById(R.id.post_stub);
        locationIcon = (RelativeLayout) v.findViewById(R.id.location_icon);
        directionsIcon = (RelativeLayout) v.findViewById(R.id.directions_icon);
        commentIcon = (RelativeLayout) v.findViewById(R.id.comment_icon);
        favoriteIcon = (RelativeLayout) v.findViewById(R.id.favorite_icon);
        shareIcon = (RelativeLayout) v.findViewById(R.id.share_icon);
        actionsEllipsis = (RelativeLayout) v.findViewById(R.id.action_ellipsis);
        commentIconView = (ImageView) v.findViewById(R.id.commentIconView);
        favoriteIconView = (ImageView) v.findViewById(R.id.favoriteIconView);
        favoriteCounter = (TextView) v.findViewById(R.id.favorite_count);
        tracker = (TextView) v.findViewById(R.id.tracker);

        // Open Graph

        openGraphData = (CardView) v.findViewById(R.id.ogData);
        ogImage = (ImageView) v.findViewById(R.id.ogImage);
        ogTitle = (TextView) v.findViewById(R.id.ogTitle);
        ogDescription = (TextView) v.findViewById(R.id.ogDescription);
        ogUrl = (TextView) v.findViewById(R.id.ogUrl);

    }

    // Add favorite

    private void addFavorite(final int position,
                             final Report post,
                             final int currentCount,
                             final TextView textView,
                             final ImageView imageView,
                             final Context mContext,
                             final SharedPreferences mPreferences) {

        // Retrieve API token

        final String accessToken = mPreferences.getString("access_token", "");

        // Build request object

        FavoritePostBody favoritePostBody = new FavoritePostBody(post.id);

        FavoriteService service = FavoriteService.restAdapter.create(FavoriteService.class);

        service.addFavorite(accessToken, "application/json", favoritePostBody, new Callback<Favorite>() {

            @Override
            public void success(Favorite favorite, Response response) {

                int newCount = currentCount + 1;

                textView.setText(String.valueOf(newCount));

                // Make favorite icon opaque

                imageView.setAlpha(1.0f);

                // Change favorite icon color

                Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp);
                myIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);
                imageView.setImageDrawable(myIcon);

                // Replace the target post item with the
                // updated API response object

                post.properties.favorites.add(favorite);

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    // Undo favorite

    private void undoFavorite(final Report post,
                              final int favoriteId,
                              final int currentCount,
                              final TextView textView,
                              final ImageView imageView,
                              final Context mContext,
                              final SharedPreferences mPreferences) {

        // Retrieve API token

        final String accessToken = mPreferences.getString("access_token", "");

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

                    Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp);
                    myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    imageView.setImageDrawable(myIcon);

                } else {

                    int newCount = currentCount - 1;

                    textView.setText(String.valueOf(newCount));

                }

                for (Iterator<Favorite> iter = post.properties.favorites.listIterator(); iter.hasNext(); ) {
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

    private void setImage(final Context context, final Report post, ImageView imageView) {

        imageView.setImageDrawable(null);

        imageView.setVisibility(View.GONE);

        if (post.properties.images.size() > 0) {

            ReportPhoto image = (ReportPhoto) post.properties.images.get(0);

            String imagePath = (String) image.properties.square_retina;

            imageView.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(context));

            imageView.setLayoutParams(layoutParams);

            Picasso.with(context).load(imagePath).fit().into(imageView);

            imageView.setTag(imagePath);

        }

    }
    
    private void setAuthor(final Context context, final Report post, TextView textView, ImageView imageView) {

        // Load author avatar

        Picasso.with(context).load(post.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(imageView);

        // Display author name

        textView.setText(String.format("%s %s", post.properties.owner.properties.first_name, post.properties.owner.properties.last_name));
        
    }

    private void setDate(final Report post, TextView textView) {

        String creationDate = (String) AttributeTransformUtility.relativeTime(post.properties.created);

        textView.setText(creationDate);

    }

    private void setWatershed(final Context context, final Report post, TextView textView) {

        // Extract watershed name, if any
        String watershedName = AttributeTransformUtility.parseWatershedName(post.properties.territory);

        // Display watershed name and add click listener if
        // a valid territory object is present

        textView.setText(watershedName);

        textView.setOnClickListener(new TerritoryProfileListener(context, post.properties.territory));

    }

    private void setCaption(final Context context, final Report post, TextView textView) {

        textView.setText("");

        textView.setVisibility(View.GONE);

        if (post.properties.description != null && (post.properties.description.length() > 0)) {

            textView.setText(post.properties.description.trim());

            textView.setVisibility(View.VISIBLE);

            new PatternEditableBuilder().
                    addPattern(context, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(context, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(context, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    context.startActivity(intent);

                                }
                            }).into(textView);

        }

    }

    private void setActionBadge(final Report post, RelativeLayout relativeLayout) {

        relativeLayout.setVisibility(View.GONE);

        if ("closed".equals(post.properties.state)) {

            relativeLayout.setVisibility(View.VISIBLE);

        }

    }

    private void setCommentState(final Context context, final Report post, RelativeLayout tapView, TextView textView, ImageView imageView) {

        // Set value of comment count string
        int commentCount = post.properties.comments.size();

        // Clear display comment count

        textView.setText("");

        // Revert comment icon opacity

        tapView.setAlpha(0.4f);

        // Revert comment icon color

        Drawable commentIcon = ContextCompat.getDrawable(context, R.drawable.ic_mode_comment_black_24dp);
        commentIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        if (commentCount > 0) {

            textView.setText(String.valueOf(commentCount));

            // Make comment icon opaque

            imageView.setAlpha(1.0f);

            // Change comment icon color

            commentIcon.setColorFilter(ContextCompat.getColor(context, R.color.splash_blue), PorterDuff.Mode.SRC_ATOP);

        }

        imageView.setImageDrawable(commentIcon);

    }

    private void setOrganizations(final Context context, final Report post, FlexboxLayout flexboxLayout) {

        flexboxLayout.setVisibility(View.GONE);

        flexboxLayout.removeAllViews();

        if (post.properties.groups.size() > 0) {

            flexboxLayout.setVisibility(View.VISIBLE);

            for (Organization organization : post.properties.groups) {

                ImageView groupView = (ImageView) LayoutInflater.from(context).inflate(R.layout.related_group_item, postCard, false);

                Picasso.with(context).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(groupView);

                groupView.setTag(organization);

                groupView.setOnClickListener(new OrganizationProfileListener(context, organization));

                flexboxLayout.addView(groupView);

            }

        }

    }

    private void setFavoriteState(final Context context, final Report post, TextView textView, ImageView imageView) {

        // Set value of favorite count string
        int favoriteCount = post.properties.favorites.size();

        // Clear display favorite count

        textView.setText("");

        // Revert favorite icon opacity

        imageView.setAlpha(0.4f);

        // Revert icon color

        Drawable favoriteIcon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
        favoriteIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        if (favoriteCount > 0) {

            textView.setText(String.valueOf(favoriteCount));

            // Make favorite icon opaque

            imageView.setAlpha(1.0f);

            // Change favorite icon color

            favoriteIcon.setColorFilter(ContextCompat.getColor(context, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);

        }

        imageView.setImageDrawable(favoriteIcon);

    }

    private void setOpenGraph(final Context context, final Report post, CardView cardView, ImageView imageView, TextView title, TextView description, TextView url) {

        // Hide CardView

        cardView.setVisibility(View.GONE);

        // Reset image

        imageView.setImageDrawable(null);

        // Reset title, description and URL

        title.setText("");

        description.setText("");

        url.setText("");

        if (post.properties.open_graph.size() > 0) {

            final OpenGraphObject openGraphObject = post.properties.open_graph.get(0);

            // Make CardView visible

            cardView.setVisibility(View.VISIBLE);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri webpage = Uri.parse(openGraphObject.properties.url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    }
                }
            });

            // Load image

            Picasso.with(context).load(openGraphObject.properties.imageUrl).fit().into(imageView);

            // Load title, description and URL

            title.setText(openGraphObject.properties.openGraphTitle);

            description.setText(openGraphObject.properties.description);

            try {

                url.setText(OpenGraph.getDomainName(openGraphObject.properties.url));

            } catch (URISyntaxException e) {

                url.setText("");

            }

        }

    }

    public void bindPost(final Report post, final Context context, final SharedPreferences sharedPreferences, final boolean mIsProfile) {

        boolean openGraphOnly = true;

        Log.d("target-post", post.properties.toString());

        tracker.setText(String.valueOf(post.id));

        // Set date

        setDate(post, postDate);
        
        // Set author (owner)
        
        setAuthor(context, post, postOwner, ownerAvatar);

        // Set post image

        setImage(context, post, postThumb);

        // Set watershed

        setWatershed(context, post, postWatershed);

        // Set caption

        setCaption(context, post, postCaption);

        // Set comment state

        setCommentState(context, post, commentIcon, postComments, commentIconView);

        // Add clickable organization logos

        setOrganizations(context, post, postGroups);

        // Set action badge state

        setActionBadge(post, actionBadge);

        // Set favorite state

        setFavoriteState(context, post, favoriteCounter, favoriteIconView);

        // Display Open Graph information, if any

        setOpenGraph(context, post, openGraphData, ogImage, ogTitle, ogDescription, ogUrl);

        // Attach click listeners to active UI components

        commentIcon.setOnClickListener(new PostCommentListener(context, post));

        actionBadge.setOnClickListener(new PostCommentListener(context, post));

        locationIcon.setOnClickListener(new PostMapListener(context, post));

        directionsIcon.setOnClickListener(new PostDirectionsListener(context, post));

        shareIcon.setOnClickListener(new PostShareListener(context, post));

        favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve authenticated user's ID

                int authUserId = sharedPreferences.getInt("user_id", 0);

                // Loop over this post's list of favorites

                for (Favorite favorite : post.properties.favorites) {

                    // The can only be one favorite per use per post,
                    // so if the `owner_id` matches the authenticated
                    // user's ID, target that favorite for removal.

                    if (favorite.properties.owner_id == authUserId) {

                        undoFavorite(post,
                                favorite.properties.id,
                                post.properties.favorites.size(),
                                favoriteCounter,
                                favoriteIconView,
                                context,
                                sharedPreferences);

                        return;

                    }

                }

                addFavorite(getAdapterPosition(), post, post.properties.favorites.size(), favoriteCounter, favoriteIconView, context, sharedPreferences);

            }
        });

        // Context-dependent configuration

        if (!mIsProfile) {

            ownerAvatar.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            postOwner.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            actionsEllipsis.setVisibility(View.GONE);

        } else {

            // Here we're inside the profile context

            // Even within the profile context, we need to account for the fact that users will
            // take action on posts that they don't own. Therefore, profile routing should be
            // enabled when viewing a person's "actions" feed. We can determine the condition by
            // comparing the transient user id stored in the UserHolder class and the `owner_id`
            // field of the current post.

            if (UserHolder.getUser().properties.id != post.properties.owner_id) {

                ownerAvatar.setOnClickListener(new UserProfileListener(context, post.properties.owner));

                postOwner.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            }

            // Determine whether or not we can expose the "additional actions" ellipsis for access to edit/delete.
            // This is a slightly different condition from the above because the id comparison must be against
            // the id of the authenticated user.

            if (sharedPreferences.getInt("user_id", 0) == post.properties.owner_id) {

                actionsEllipsis.setVisibility(View.VISIBLE);

                actionsEllipsis.setOnClickListener(new View.OnClickListener() {
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

                                ReportHolder.setReport(post);

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

                actionsEllipsis.setVisibility(View.GONE);

            }

        }
        
    }

}
