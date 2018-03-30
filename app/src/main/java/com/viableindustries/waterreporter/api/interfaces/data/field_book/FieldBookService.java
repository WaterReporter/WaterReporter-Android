package com.viableindustries.waterreporter.api.interfaces.data.field_book;

import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.comment.CommentPost;
import com.viableindustries.waterreporter.api.models.field_book.FieldBook;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookPatchBody;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookPostBody;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportPatchBody;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;

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

    @PATCH("/data/field-book/{fieldBookId}")
    void updateSingle(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("fieldBookId") int fieldBookId,
                      @Body FieldBookPatchBody fieldBookPatchBody,
                      Callback<Response> responseCallback);

}
