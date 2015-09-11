package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public interface ImageService {

    final String ENDPOINT = "http://api.waterreporter.org/v1";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @Multipart
    @POST("/media/image")
    public void postImage
            (@Header("Authorization") String authorization,
             @Part("image") TypedFile photo,
             Callback<ReportPhoto> cb);

//    @Multipart
//    @POST("/media/image")
//    ReportPhoto postImage
//            (@Header("Authorization") String authorization,
//             @Part("image") TypedFile photo);

}
