package com.viableindustries.waterreporter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.viableindustries.waterreporter.data.PostFavoriteCountListener;
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
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by brendanmcintyre on 8/18/17.
 */

public class TimelineAdapterHelpers {

    // Download image

    public static void saveImage(Context context, Report post) {

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(post.properties.images.get(0).properties.original);

        String fileName = String.format("%s-%s.jpg", Math.random(), new Date().getTime());

        Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        downloadManager.enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                        DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle("Saving image from Water Reporter")
                .setDescription("Downloading image from Water Reporter")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        fileName));

    }

    // Add favorite

    public static void addFavorite(final Report post,
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

                textView.setText(String.format("%s", newCount));

                // Change favorite icon color

                imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);

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

    public static void undoFavorite(final Report post,
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

                    // Clear TextView contents

                    textView.setText("");

                    // Change favorite icon color

                    imageView.setColorFilter(ContextCompat.getColor(mContext, R.color.icon_gray), PorterDuff.Mode.SRC_ATOP);

                } else {

                    int newCount = currentCount - 1;

                    // Set TextView value

                    textView.setText(String.format("%s", newCount));

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

    public static void setImage(final Context context, final Report post, ImageView imageView) {

        imageView.setImageDrawable(null);

        imageView.setVisibility(View.GONE);

        if (post.properties.images.size() > 0) {

            ReportPhoto image = (ReportPhoto) post.properties.images.get(0);

            String imagePath = (String) image.properties.square_retina;

            imageView.setVisibility(View.VISIBLE);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(context));

            imageView.setLayoutParams(layoutParams);

            Picasso.with(context).load(imagePath).fit().into(imageView);

            imageView.setTag(imagePath);

        }

    }

    public static void setAuthor(final Context context,
                                 final Report post,
                                 TextView textView,
                                 ImageView imageView) {

        // Load author avatar

        Picasso.with(context).load(post.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(imageView);

        // Display author name

        textView.setText(String.format("%s %s", post.properties.owner.properties.first_name, post.properties.owner.properties.last_name));

    }

    public static void setDate(final Report post, TextView textView) {

        String creationDate = (String) AttributeTransformUtility.relativeTime(post.properties.created);

        textView.setText(creationDate);

    }

    public static void setWatershed(final Context context, final Report post, TextView textView) {

        // Extract watershed name, if any
        String watershedName = AttributeTransformUtility.parseWatershedName(post.properties.territory);

        // Display watershed name and add click listener if
        // a valid territory object is present

        textView.setText(watershedName);

        textView.setOnClickListener(new TerritoryProfileListener(context, post.properties.territory));

    }

    public static void setCaption(final Context context, final Report post, TextView textView) {

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

    public static void setActionBadge(final Report post, RelativeLayout relativeLayout) {

        relativeLayout.setVisibility(View.GONE);

        if ("closed".equals(post.properties.state)) {

            relativeLayout.setVisibility(View.VISIBLE);

        }

    }

    public static void setCommentState(final Context context,
                                       final Report post,
                                       TextView textView,
                                       ImageView imageView) {

        // Set value of comment count string
        int commentCount = post.properties.comments.size();

        // Clear display comment count

        textView.setText("");

        // Reset icon color

        imageView.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray), PorterDuff.Mode.SRC_ATOP);

        if (commentCount > 0) {

            textView.setText(String.format("%s", commentCount));

            imageView.setColorFilter(ContextCompat.getColor(context, R.color.splash_blue), PorterDuff.Mode.SRC_ATOP);

        }

    }

    public static void setOrganizations(final Context context,
                                        final LinearLayout parent,
                                        final Report post,
                                        FlexboxLayout flexboxLayout) {

        flexboxLayout.setVisibility(View.GONE);

        flexboxLayout.removeAllViews();

        if (post.properties.groups.size() > 0) {

            flexboxLayout.setVisibility(View.VISIBLE);

            for (Organization organization : post.properties.groups) {

                ImageView groupView = (ImageView) LayoutInflater.from(context).inflate(R.layout.related_group_item, parent, false);

                Picasso.with(context).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(groupView);

                groupView.setTag(organization);

                groupView.setOnClickListener(new OrganizationProfileListener(context, organization));

                flexboxLayout.addView(groupView);

            }

        }

    }

    public static void setFavoriteState(final Context context,
                                        final Report post,
                                        TextView textView,
                                        ImageView imageView) {

        // Set value of favorite count string
        int favoriteCount = post.properties.favorites.size();

        // Clear display favorite count

        textView.setText("");

        // Reset icon color

        imageView.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray), PorterDuff.Mode.SRC_ATOP);

        if (favoriteCount > 0) {

            textView.setText(String.format("%s", favoriteCount));

            imageView.setColorFilter(ContextCompat.getColor(context, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);

        }

    }

    public static void setOpenGraph(final Context context,
                                    final Report post,
                                    CardView cardView,
                                    ImageView imageView,
                                    TextView title,
                                    TextView description,
                                    TextView url) {

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

            Picasso.with(context)
                    .load(openGraphObject.properties.imageUrl)
                    .placeholder(R.drawable.open_graph_image_placeholder)
                    .fit()
                    .into(imageView);

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

    public static void setIconColors(final Context context, ImageView[] views) {

        for (ImageView v : views) {

            v.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray), PorterDuff.Mode.SRC_ATOP);

        }

    }

}
