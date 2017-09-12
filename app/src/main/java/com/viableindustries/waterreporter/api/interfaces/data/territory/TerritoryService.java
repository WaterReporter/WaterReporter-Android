package com.viableindustries.waterreporter.api.interfaces.data.territory;

import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.api.models.territory.TerritoryCollection;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public interface TerritoryService {

    @GET("/data/huc-8")
    void search(@Header("Authorization") String authorization,
                @Header("Content-Type") String contentType,
                @Query("page") int page,
                @Query("results_per_page") int numResults,
                @Query("q") String q,
                CancelableCallback<TerritoryCollection> territoryCollectionCallback);

    @GET("/data/territory")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 CancelableCallback<TerritoryCollection> territoryCollectionCallback);

    @GET("/data/territory/{territory}")
    void getSingle(@Header("Authorization") String authorization,
                   @Header("Content-Type") String contentType,
                   @Path("territory") int territoryId,
                   CancelableCallback<Territory> territoryCallback);

}
