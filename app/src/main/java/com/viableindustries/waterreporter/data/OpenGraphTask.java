package com.viableindustries.waterreporter.data;

import android.os.AsyncTask;

import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class OpenGraphTask extends AsyncTask<String, Void, Document> {

    private OpenGraphResponse delegate = null;

    public OpenGraphTask(OpenGraphResponse openGraphResponse) {
        delegate = openGraphResponse;
    }

    protected Document doInBackground(String... urls) {

        try {
            return OpenGraph.fetchPage(urls[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//    protected void onProgressUpdate(Integer... progress) {
//        //setProgressPercent(progress[0]);
//    }

    protected void onPostExecute(Document result) {
        delegate.processFinish(result);
    }

}
