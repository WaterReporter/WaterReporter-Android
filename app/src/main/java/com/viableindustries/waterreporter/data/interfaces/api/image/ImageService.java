package com.viableindustries.waterreporter.data.interfaces.api.image;

import com.viableindustries.waterreporter.data.objects.image.ImageProperties;
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

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @Multipart
    @POST("/media/image")
    ImageProperties postImage
            (@Header("Authorization") String authorization,
             @Part("image") TypedFile photo);

    @Multipart
    @POST("/media/image")
    void postImageAsync
            (@Header("Authorization") String authorization,
             @Part("image") TypedFile photo,
             CancelableCallback<ImageProperties> imagePropertiesCallback);
//    @Multipart
//    @POST("/media/image")
//    ReportPhoto postImage
//            (@Header("Authorization") String authorization,
//             @Part("image") TypedFile photo);

}
