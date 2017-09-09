package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public interface TagService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/hashtag")
    public void getMany(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Query("page") int page,
                        @Query("results_per_page") int numResults,
                        @Query("q") String q,
                        CancelableCallback<HashtagCollection> hashtagCollectionCallback);

    @GET("/data/hashtag/{tag}")
    public void getOrganization(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("tag") int tagId,
                                @Query("q") String q,
                                CancelableCallback<HashTag> hashTagCallback);

}
