package com.viableindustries.waterreporter.api.interfaces.data.comment;

import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.comment.CommentPost;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public interface CommentService {

    @POST("/data/comment")
    void postComment
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body CommentPost commentPost,
             CancelableCallback<Comment> commentCallback);

}
