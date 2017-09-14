package com.viableindustries.waterreporter;

import android.app.Activity;
import android.os.Bundle;

import com.airbnb.deeplinkdispatch.DeepLinkHandler;

@DeepLinkHandler({ WaterReporterDeepLinkModule.class })
public class DeepLinkActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        DeepLinkDelegate deepLinkDelegate =
                new DeepLinkDelegate(new WaterReporterDeepLinkModuleLoader());
        // Delegate the deep link handling to DeepLinkDispatch.
        // It will start the correct Activity based on the incoming Intent URI
        deepLinkDelegate.dispatchFrom(this);

        finish();

    }

}
