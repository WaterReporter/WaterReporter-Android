package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public interface CommentService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @POST("/data/comment")
    public void postComment
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body CommentPost commentPost,
             CancelableCallback<Comment> commentCallback);

}
