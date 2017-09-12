package com.viableindustries.waterreporter.api.interfaces.data.image;

import com.viableindustries.waterreporter.api.models.image.ImageProperties;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.RestAdapter;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public interface ImageService {

    @Multipart
    @POST("data/media/image")
    ImageProperties postImage
            (@Header("Authorization") String authorization,
             @Part("image") TypedFile photo);

    @Multipart
    @POST("data/media/image")
    void postImageAsync
            (@Header("Authorization") String authorization,
             @Part("image") TypedFile photo,
             CancelableCallback<ImageProperties> imagePropertiesCallback);

}
