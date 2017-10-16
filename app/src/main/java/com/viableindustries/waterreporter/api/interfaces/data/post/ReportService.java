package com.viableindustries.waterreporter.api.interfaces.data.post;

import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.comment.CommentCollection;
import com.viableindustries.waterreporter.api.models.favorite.FavoriteCollection;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportPatchBody;
import com.viableindustries.waterreporter.api.models.post.ReportPostBody;
import com.viableindustries.waterreporter.api.models.post.ReportStateBody;


import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Ryan Hamley on 10/6/14.
 * This class defines the Retrofit interface with all the methods used to interact with the API
 * The top-level api classes returned from the API are:
 * ReportPostResponse
 */
public interface ReportService {

    @GET("/data/report")
    void getReports(@Header("Authorization") String authorization,
                    @Header("Content-Type") String contentType,
                    @Query("page") int page,
                    @Query("results_per_page") int numResults,
                    @Query("q") String q,
                    Callback<FeatureCollection> featureCollectionCallback);

    @GET("/data/report/{report}")
    void getSingleReport(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("report") int reportId,
                         Callback<Report> report);

    @PATCH("/data/report/{report}")
    void setReportState(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Path("report") int reportId,
                        @Body ReportStateBody reportStateBody,
                        Callback<Report> report);

    @PATCH("/data/report/{report}")
    void updateReport(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("report") int reportId,
                      @Body ReportPatchBody reportPatchBody,
                      Callback<Report> report);

    @GET("/data/report/{report}/groups")
    void getReportGroups(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("report") int reportId,
                         Callback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/data/report/{report}/comments")
    void getReportComments(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Path("report") int reportId,
                           @Query("page") int page,
                           @Query("results_per_page") int numResults,
                           @Query("q") String q,
                           Callback<CommentCollection> commentCollectionCallback);

    @GET("/data/report/{report}/likes")
    void getPostLikes(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("report") int reportId,
                      @Query("page") int page,
                      @Query("results_per_page") int numResults,
                      @Query("q") String q,
                      Callback<FavoriteCollection> favoriteCollectionCallback);

    @POST("/data/report")
    void postReport
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody,
             Callback<Report> cb);

    @POST("/data/report")
    Report postReportSync
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody);

    @DELETE("/data/report/{report}")
    void deleteSingleReport(@Header("Authorization") String authorization,
                            @Path("report") int reportId,
                            Callback<Response> responseCallback);

}
