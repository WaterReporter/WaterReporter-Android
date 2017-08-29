package com.viableindustries.waterreporter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by brendanmcintyre on 10/31/16.
 */

public class ConnectionUtility {

    public static boolean connectionActive(Context aContext) {

        ConnectivityManager connMgr = (ConnectivityManager)
                aContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();

    }

}
