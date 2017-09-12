package com.viableindustries.waterreporter.utilities;

import android.os.AsyncTask;

import com.viableindustries.waterreporter.data.objects.open_graph.OpenGraphResponse;

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

    protected void onPostExecute(Document result) {
        delegate.processFinish(result);
    }

}
