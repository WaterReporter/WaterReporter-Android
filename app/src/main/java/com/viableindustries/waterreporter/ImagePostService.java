package com.viableindustries.waterreporter;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import retrofit.mime.TypedFile;

/**
 * Created by brendanmcintyre on 9/5/17.
 */

public class ImagePostService extends IntentService {

    // Defines and instantiates an object for handling status updates.
    private BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public ImagePostService() {
        super("ImagePostService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        // Gets data from the incoming Intent
        Bundle extras = workIntent.getExtras();

        String filePath = extras.getString("file_path");

        String accessToken = extras.getString("access_token");

        String storedPost = extras.getString("stored_post");

        ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        final File photo = new File(filePath);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        String mimeType = fileNameMap.getContentTypeFor(filePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        ImageProperties imageProperties = imageService.postImage(accessToken, typedPhoto);

        // Reports that the image upload is complete.
        mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_COMPLETE, imageProperties.id, storedPost);

    }

}
