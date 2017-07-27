package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

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
                                Callback<Report> cb);

    @DELETE("/data/like/{id}")
    public void undoFavorite(@Header("Authorization") String authorization,
                            @Header("Content-Type") String contentType,
                            @Path("id") int featureId,
                            Callback<Void> callback);

}
