package com.viableindustries.waterreporter.api.interfaces.data.trending;

import com.viableindustries.waterreporter.api.models.hashtag.TrendingTags;
import com.viableindustries.waterreporter.api.models.organization.TrendingGroups;
import com.viableindustries.waterreporter.api.models.territory.TrendingTerritories;
import com.viableindustries.waterreporter.api.models.user.TrendingPeople;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public interface TrendingService {

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
