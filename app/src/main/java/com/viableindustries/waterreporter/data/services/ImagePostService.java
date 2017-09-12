package com.viableindustries.waterreporter.data.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.MainActivity;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.constants.Constants;
import com.viableindustries.waterreporter.data.interfaces.api.image.ImageService;
import com.viableindustries.waterreporter.data.interfaces.api.post.ReportService;
import com.viableindustries.waterreporter.data.objects.image.ImageProperties;
import com.viableindustries.waterreporter.data.objects.post.Report;
import com.viableindustries.waterreporter.data.objects.post.ReportPostBody;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;
import com.viableindustries.waterreporter.utilities.BroadcastNotifier;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import retrofit.mime.TypedFile;

/**
 * Created by brendanmcintyre on 9/5/17.
 */

public class ImagePostService extends IntentService {

    private static final int NOTIFICATION_ID = 1;

    // Defines and instantiates an object for handling status updates.
    private final BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ImagePostService() {
        super("ImagePostService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        // Gets api from the incoming Intent
        Bundle extras = workIntent.getExtras();

        String filePath = extras.getString("file_path");

        String accessToken = extras.getString("access_token");

        String storedPost = extras.getString("stored_post");

        ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        final File photo = new File(filePath != null ? filePath : null);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        String mimeType = fileNameMap.getContentTypeFor(filePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        ImageProperties imageProperties = imageService.postImage(accessToken, typedPhoto);

//        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);
//        sharedPreferences.edit().putInt("PENDING_IMAGE_ID", imageProperties.id).apply();

        ReportPostBody reportPostBody = new Gson().fromJson(storedPost, ReportPostBody.class);

        reportPostBody.images = AttributeTransformUtility.buildImageRelation(imageProperties.id);

        Report post = ReportService.restAdapter.create(ReportService.class).postReportSync(accessToken, "application/json", reportPostBody);

        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);
        sharedPreferences.edit().putInt("POST_SAVED_VIA_SERVICE", post.id).apply();

        sendNotification(post);

        // Reports that the image upload is complete.
        mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_COMPLETE);

    }

    private void sendNotification(Report post) {

        String msg;

        String watershedName = AttributeTransformUtility.parseWatershedName(post.properties.territory, false);

        if (watershedName.startsWith("Watershed")) {

            msg = "Finished syncing your post in an unknown watershed.";

        } else {

            msg = String.format("Finished syncing your post in the %s watershed.", watershedName);

        }

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentTitle("Water Reporter")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_sync_white_24dp))
                .setSmallIcon(R.drawable.ic_sync_white_24dp);

        b.setContentIntent(contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, b.build());

    }

}
