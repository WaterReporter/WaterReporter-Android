package com.viableindustries.waterreporter.data;

import org.jsoup.nodes.Document;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public interface OpenGraphResponse {
    void processFinish(Document output);
}
