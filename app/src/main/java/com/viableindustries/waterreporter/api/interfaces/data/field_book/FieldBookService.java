package com.viableindustries.waterreporter.api.interfaces.data.field_book;

import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.comment.CommentPost;
import com.viableindustries.waterreporter.api.models.field_book.FieldBook;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookPostBody;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by brendanmcintyre on 3/27/18.
 */

public interface FieldBookService {

    @POST("/data/field-book")
    void postFieldBook
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body FieldBookPostBody fieldBookPostBody,
             Callback<Response> responseCallback);

}
