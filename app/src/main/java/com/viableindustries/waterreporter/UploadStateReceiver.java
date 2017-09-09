package com.viableindustries.waterreporter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by brendanmcintyre on 9/5/17.
 */

public class UploadStateReceiver extends BroadcastReceiver {

    public UploadStateReceiver() {

        // prevents instantiation by other packages.
    }
    /**
     *
     * This method is called by the system when a broadcast Intent is matched by this class'
     * intent filters
     *
     * @param context An Android context
     * @param intent The incoming broadcast Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

            /*
             * Gets the status from the Intent's extended data, and chooses the appropriate action
             */
        switch (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS,
                Constants.STATE_ACTION_COMPLETE)) {

            // Logs "started" state
            case Constants.STATE_ACTION_STARTED:
                //
                break;
            // Logs "connecting to network" state
            case Constants.STATE_ACTION_CONNECTING:
                //
                break;
            // Logs "parsing the RSS feed" state
            case Constants.STATE_ACTION_PARSING:
                //
                break;
            // Logs "Writing the parsed data to the content provider" state
            case Constants.STATE_ACTION_WRITING:
                //
                break;
            // Starts displaying data when the RSS download is complete
            case Constants.STATE_ACTION_COMPLETE:
                // Logs the status
                Log.d("UploadService", "State: COMPLETE");

                break;
            default:
                break;
        }
    }
}
