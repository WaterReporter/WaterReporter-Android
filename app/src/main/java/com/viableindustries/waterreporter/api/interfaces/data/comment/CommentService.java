package com.viableindustries.waterreporter.api.interfaces.data.comment;

import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.comment.CommentPost;


import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public interface CommentService {

    @POST("/data/comment")
    void postComment
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body CommentPost commentPost,
             Callback<Comment> commentCallback);

    @DELETE("/data/comment/{comment}")
    void deleteSingleComment(@Header("Authorization") String authorization,
                            @Path("comment") int commentId,
                            Callback<Response> responseCallback);

}
