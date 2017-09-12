package com.viableindustries.waterreporter.api.interfaces.data.favorite;

import com.viableindustries.waterreporter.api.models.favorite.Favorite;
import com.viableindustries.waterreporter.api.models.favorite.FavoritePostBody;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public interface FavoriteService {

    @POST("/data/like")
    void addFavorite(@Header("Authorization") String authorization,
                     @Header("Content-Type") String contentType,
                     @Body FavoritePostBody favoritePostBody,
                     CancelableCallback<Favorite> favoriteCallback);

    @DELETE("/data/like/{id}")
    void undoFavorite(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("id") int featureId,
                      CancelableCallback<Void> callback);

}
