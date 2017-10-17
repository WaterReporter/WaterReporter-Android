package com.viableindustries.waterreporter.utilities;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.viableindustries.waterreporter.api.models.open_graph.OpenGraphProperties;
import com.viableindustries.waterreporter.api.models.open_graph.OpenGraphResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class OpenGraph {

    public static OpenGraphProperties openGraphProperties;

    public static Document fetchPage(String url) throws IOException {

        return Jsoup.connect(url).get();

    }

    public static String parseTag(Document doc, String tag) {

        String tagContent = "";

        Elements ogTags = doc.select(String.format("meta[property=%s]", tag));

        if (ogTags.size() < 1) {
            return tagContent;
        }

        for (int i = 0; i < ogTags.size(); i++) {

            Element t = ogTags.get(i);

            String text = t.attr("property");

            if (tag.equals(text)) {

                tagContent = t.attr("content");

            }

        }

        return tagContent;

    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private static OpenGraphProperties buildOpenGraphObject(Map<String, String> tagIndex, int userId){

        OpenGraph.openGraphProperties = new OpenGraphProperties(
                tagIndex.get("og_image"),
                tagIndex.get("og_description"),
                tagIndex.get("og_title"),
                tagIndex.get("og_url"),
                userId);

    }

    public static void fetchOpenGraphData(
            final Activity activity,
            final View parentLayout,
            final int userId,
            final String url) throws IOException {

        final String[] ogTags = new String[]{
                "og:url",
                "og:title",
                "og:description",
                "og:image"
        };

        final Map<String, String> ogIdx = new HashMap<>();

        OpenGraphTask openGraphTask = new OpenGraphTask(new OpenGraphResponse() {

            @Override
            public void processFinish(Document output) {
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                try {

                    for (String tag : ogTags) {
                        String tagContent = OpenGraph.parseTag(output, tag);
                        Log.v(tag, tagContent);
                        ogIdx.put(tag.replace(":", "_"), tagContent);
                    }

                    buildOpenGraphObject(ogIdx, userId);

//                    if (ogIdx.get("og_title").length() > 0) {
//
//                        displayOpenGraphObject(openGraphProperties, openGraphProperties.url);
//
//                    }

                } catch (NullPointerException e) {

                    try {

                        Snackbar.make(parentLayout, "Unable to read URL.",
                                Snackbar.LENGTH_SHORT)
                                .show();

                    } catch (IllegalArgumentException i) {

                        // Open Graph retrieval task finished in background
                        // but layout references are unbound.

                        activity.finish();

                    }

                }

            }

        });

        openGraphTask.execute(url);

    }

}
