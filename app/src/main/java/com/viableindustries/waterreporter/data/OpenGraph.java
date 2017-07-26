package com.viableindustries.waterreporter.data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

}
