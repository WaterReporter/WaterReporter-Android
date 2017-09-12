package com.viableindustries.waterreporter.data.interfaces.api.hashtag;

import com.viableindustries.waterreporter.data.objects.hashtag.HashTag;
import com.viableindustries.waterreporter.data.objects.hashtag.HashtagCollection;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public interface HashTagService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/hashtag")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 CancelableCallback<HashtagCollection> hashtagCollectionCallback);

    @GET("/hashtag/{tag}")
    void getOrganization(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("tag") int tagId,
                         @Query("q") String q,
                         CancelableCallback<HashTag> hashTagCallback);

}
