package com.viableindustries.waterreporter.api.models.open_graph;

import org.jsoup.nodes.Document;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public interface OpenGraphResponse {
    void processFinish(Document output);
}
