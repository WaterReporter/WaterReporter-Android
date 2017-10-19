package com.viableindustries.waterreporter.api.interfaces.data.hashtag;

import com.viableindustries.waterreporter.api.models.hashtag.HashTag;
import com.viableindustries.waterreporter.api.models.hashtag.HashtagCollection;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public interface HashTagService {

    @GET("/data/hashtag")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 Callback<HashtagCollection> hashtagCollectionCallback);

    @GET("/data/hashtag/{tag}")
    void getOrganization(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("tag") int tagId,
                         @Query("q") String q,
                         Callback<HashTag> hashTagCallback);

}
