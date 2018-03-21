package com.viableindustries.waterreporter.api.interfaces.data.snapshot;

import com.viableindustries.waterreporter.api.models.favorite.Favorite;
import com.viableindustries.waterreporter.api.models.favorite.FavoritePostBody;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public interface SnapshotService {

    @POST("/data/like")
    void addFavorite(@Header("Authorization") String authorization,
                     @Header("Content-Type") String contentType,
                     @Body FavoritePostBody favoritePostBody,
                     Callback<Favorite> favoriteCallback);

    @DELETE("/data/like/{id}")
    void undoFavorite(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("id") int featureId,
                      Callback<Void> callback);

}
