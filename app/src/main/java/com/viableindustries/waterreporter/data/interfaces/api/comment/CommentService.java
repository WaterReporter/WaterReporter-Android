package com.viableindustries.waterreporter.data.interfaces.api.comment;

import com.viableindustries.waterreporter.data.objects.comment.Comment;
import com.viableindustries.waterreporter.data.objects.comment.CommentPost;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public interface CommentService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
            .setEndpoint(ENDPOINT)
            .build();

    @POST("/comment")
    void postComment
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body CommentPost commentPost,
             CancelableCallback<Comment> commentCallback);

}
