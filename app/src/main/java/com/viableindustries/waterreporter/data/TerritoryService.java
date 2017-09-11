package com.viableindustries.waterreporter.data;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public interface TerritoryService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/huc-8")
    void search(@Header("Authorization") String authorization,
                @Header("Content-Type") String contentType,
                @Query("page") int page,
                @Query("results_per_page") int numResults,
                @Query("q") String q,
                CancelableCallback<TerritoryCollection> territoryCollectionCallback);

    @GET("/territory")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 CancelableCallback<TerritoryCollection> territoryCollectionCallback);

    @GET("/territory/{territory}")
    void getSingle(@Header("Authorization") String authorization,
                   @Header("Content-Type") String contentType,
                   @Path("territory") int territoryId,
                   CancelableCallback<Territory> territoryCallback);

}
