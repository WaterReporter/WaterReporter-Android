package com.viableindustries.waterreporter.utilities;

import android.app.Activity;
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

    public static OpenGraphProperties buildOpenGraphObject(Map<String, String> tagIndex, int userId) {

        return new OpenGraphProperties(
                tagIndex.get("og_image"),
                tagIndex.get("og_description"),
                tagIndex.get("og_title"),
                tagIndex.get("og_url"),
                userId);

    }

}
