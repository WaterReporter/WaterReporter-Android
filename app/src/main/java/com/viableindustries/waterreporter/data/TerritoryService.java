package com.viableindustries.waterreporter.data;

import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public interface TerritoryService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/huc-8")
    public void search(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Query("page") int page,
                        @Query("results_per_page") int numResults,
                        @Query("q") String q,
                        CancelableCallback<TerritoryCollection> territoryCollectionCallback);

    @GET("/data/territory")
    public void getMany(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Query("page") int page,
                        @Query("results_per_page") int numResults,
                        @Query("q") String q,
                        CancelableCallback<TerritoryCollection> territoryCollectionCallback);

    @GET("/data/territory/{territory}")
    public void getSingle(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Path("territory") int territoryId,
                        CancelableCallback<Territory> territoryCallback);

}
