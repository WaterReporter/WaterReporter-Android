//package com.viableindustries.waterreporter;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.support.annotation.Nullable;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.FileProvider;
//import android.text.format.DateUtils;
//import android.util.Log;
//
//import com.facebook.CallbackManager;
//import com.facebook.share.model.ShareLinkContent;
//import com.facebook.share.widget.ShareDialog;
//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;
//import com.viableindustries.waterreporter.data.Comment;
//import com.viableindustries.waterreporter.data.Organization;
//import com.viableindustries.waterreporter.data.Report;
//import com.viableindustries.waterreporter.data.ReportHolder;
//import com.viableindustries.waterreporter.data.ReportPhoto;
//import com.viableindustries.waterreporter.data.Territory;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
///**
// * Created by brendanmcintyre on 4/6/17.
// */
//
//public class SocialShareUtility {
//
//    final private static String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";
//
//    protected static String buildDisplayName(Report report, boolean lowerCase) {
//
//        String userName;
//
//        String title;
//
//        try {
//
//            userName = String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name);
//
//        } catch (NullPointerException e) {
//
//            userName = lowerCase ? "a citizen" : "A citizen";
//
//        }
//
//        return userName;
//
//    }
//
//    public static void shareOnFacebook(final Activity context) {
//
//        Report feature = ReportHolder.getReport();
//
//        ReportPhoto image = (ReportPhoto) feature.properties.images.get(0);
//
//        String imagePath = (String) image.properties.square_retina;
//
//        ShareDialog shareDialog = new ShareDialog(context);
//
//        CallbackManager callbackManager = CallbackManager.Factory.create();
//
//        String body;
//
//        String description;
//
//        String userName = SocialShareUtility.buildDisplayName(feature, false);
//
//        String title;
//
//        description = String.format("A post by %s on Water Reporter.", userName);
//
//        try {
//
//            body = feature.properties.description.trim();
//
//            int snippetLength = body.length();
//
//            if (snippetLength > 100) {
//
//                body = String.format("%s\u2026", body.substring(0, 99).trim());
//
//            } else {
//
//                if (".".equals(body.substring(snippetLength - 1))) {
//
//                    body = String.format("%s\u2026", body.substring(0, snippetLength - 1).trim());
//
//                } else {
//
//                    body = String.format("%s\u2026", body);
//
//                }
//
//            }
//
//            title = String.format("%s on Water Reporter: \"%s\"", userName, body);
//
//        } catch (NullPointerException e) {
//
//            title = String.format("%s posted on Water Reporter.", userName);
//
//        }
//
//        if (ShareDialog.canShow(ShareLinkContent.class)) {
//            ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                    .setContentTitle(title)
//                    .setContentDescription(description)
//                    .setImageUrl(Uri.parse(imagePath))
//                    .setContentUrl(Uri.parse(String.format("https://www.waterreporter.org/community/reports/%s", feature.id)))
//                    .build();
//
//            shareDialog.show(linkContent);
//        }
//
//    }
//
//    public static void shareOnTwitter(final Context context) {
//
//        final Report feature = ReportHolder.getReport();
//
//        ReportPhoto img = (ReportPhoto) feature.properties.images.get(0);
//
//        String imagePath = (String) img.properties.square_retina;
//
//        Picasso.with(context).load(imagePath).into(new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//
//                try {
//
//                    File image = FileUtils.createImageFile(context);
//
//                    // Use FileProvider to comply with Android security requirements.
//                    // See: https://developer.android.com/training/camera/photobasics.html
//                    // https://developer.android.com/reference/android/os/FileUriExposedException.html
//
//                    Uri shareImageUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, image);
//
//                    // Using v4 Support Library FileProvider and Camera intent on pre-Marshmallow devices
//                    // requires granting FileUri permissions at runtime
//
//                    context.grantUriPermission(context.getPackageName(), shareImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                    FileOutputStream stream = new FileOutputStream(image); // overwrites this image every time
//
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//
//                    stream.close();
//
//                    // Build the intent
//
//                    Intent shareIntent = new Intent();
//                    shareIntent.setAction(Intent.ACTION_SEND);
//
//                    // Set MIME type of content
//
//                    shareIntent.setType("*/*");
//
//                    // Set flag for temporary read Uri permission
//
//                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
//
//                    // Add image content
//
//                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareImageUri);
//
//                    // Add text body
//
//                    String userName = SocialShareUtility.buildDisplayName(feature, true);
//
//                    String title = userName.equals("a citizen") ? "Check out this Water Reporter post: " : String.format("Check out this Water Reporter post from %s: ", userName);
//
//                    shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(context.getResources().getString(R.string.share_post_text_body),
//                            title, String.valueOf(feature.id)));
//
//                    // Set the target application to Twitter.
//                    // A prior check confirmed that the user has
//                    // the app installed so it's safe to proceed.
//                    shareIntent.setPackage("com.twitter.android");
//
//                    context.startActivity(shareIntent);
//
//                } catch (Exception e) {
//
//                    e.printStackTrace();
//
//                    Log.d(null, "Save file error!");
//
//                }
//
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//            }
//        });
//
//    }
//
//    public static boolean verifyTarget(Context aContext, String packageName) {
//
//        boolean packagedInstalled;
//
//        try {
//
//            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
//
//            packagedInstalled = true;
//
//        } catch (PackageManager.NameNotFoundException e) {
//
//            packagedInstalled = false;
//
//        }
//
//        return packagedInstalled;
//
//    }
//
//    public static int getShareOptions(Context aContext) {
//
//        int socialOptions;
//
//        boolean hasFacebook = SocialShareUtility.verifyTarget(context, context.getResources().getString(R.string.fb_package_name));
//        boolean hasTwitter = SocialShareUtility.verifyTarget(context, context.getResources().getString(R.string.tt_package_name));
//
//        if (hasFacebook && hasTwitter) {
//
//            socialOptions = R.array.post_share_options_all;
//
//        } else if (hasFacebook) {
//
//            socialOptions = R.array.post_share_options_fb;
//
//        } else if (hasTwitter) {
//
//            socialOptions = R.array.post_share_options_tt;
//
//        } else {
//
//            socialOptions = 0;
//
//        }
//
//        return socialOptions;
//
//    }
//
//}
