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

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @POST("/data/like")
    public void addFavorite(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Body FavoritePostBody favoritePostBody,
                                CancelableCallback<Favorite> favoriteCallback);

    @DELETE("/data/like/{id}")
    public void undoFavorite(@Header("Authorization") String authorization,
                            @Header("Content-Type") String contentType,
                            @Path("id") int featureId,
                            CancelableCallback<Void> callback);

}
