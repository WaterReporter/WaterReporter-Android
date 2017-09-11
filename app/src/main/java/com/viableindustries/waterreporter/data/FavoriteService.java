package com.viableindustries.waterreporter.data;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public interface FavoriteService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @POST("/like")
    void addFavorite(@Header("Authorization") String authorization,
                     @Header("Content-Type") String contentType,
                     @Body FavoritePostBody favoritePostBody,
                     CancelableCallback<Favorite> favoriteCallback);

    @DELETE("/like/{id}")
    void undoFavorite(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("id") int featureId,
                      CancelableCallback<Void> callback);

}
