package com.viableindustries.waterreporter.data;

import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public interface TrendingService {

    String ENDPOINT = "https://api.waterreporter.org/v2";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/trending/hashtag")
    void getTrendingTags(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Query("page") int page,
                                @Query("results_per_page") int numResults,
                                CancelableCallback<TrendingTags> trendingTagsCallback);

    @GET("/data/trending/group")
    void getTrendingGroups(@Header("Authorization") String authorization,
                                  @Header("Content-Type") String contentType,
                                  @Query("page") int page,
                                  @Query("results_per_page") int numResults,
                                  CancelableCallback<TrendingGroups> trendingGroupsCallback);

    @GET("/data/trending/people")
    void getTrendingPeople(@Header("Authorization") String authorization,
                                  @Header("Content-Type") String contentType,
                                  @Query("page") int page,
                                  @Query("results_per_page") int numResults,
                                  CancelableCallback<TrendingPeople> trendingPeopleCallback);

    @GET("/data/trending/territory")
    void getTrendingTerritories(@Header("Authorization") String authorization,
                                       @Header("Content-Type") String contentType,
                                       @Query("page") int page,
                                       @Query("results_per_page") int numResults,
                                       CancelableCallback<TrendingTerritories> trendingTerritoriesCallback);

}
