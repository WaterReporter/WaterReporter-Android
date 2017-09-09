package com.viableindustries.waterreporter.data;

import retrofit.RestAdapter;
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
 * The top-level data classes returned from the API are:
 * ReportPostResponse
 */
public interface ReportService {

    String ENDPOINT = "https://api.waterreporter.org/v2";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/report")
    void getReports(@Header("Authorization") String authorization,
                    @Header("Content-Type") String contentType,
                    @Query("page") int page,
                    @Query("results_per_page") int numResults,
                    @Query("q") String q,
                    CancelableCallback<FeatureCollection> featureCollectionCallback);

    @GET("/data/report/{report}")
    void getSingleReport(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("report") int reportId,
                         CancelableCallback<Report> report);

    @PATCH("/data/report/{report}")
    void setReportState(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Path("report") int reportId,
                        @Body ReportStateBody reportStateBody,
                        CancelableCallback<Report> report);

    @PATCH("/data/report/{report}")
    void updateReport(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("report") int reportId,
                      @Body ReportPatchBody reportPatchBody,
                      CancelableCallback<Report> report);

    @GET("/data/report/{report}/groups")
    void getReportGroups(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("report") int reportId,
                         CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/data/report/{report}/comments")
    void getReportComments(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Path("report") int reportId,
                           @Query("page") int page,
                           @Query("results_per_page") int numResults,
                           @Query("q") String q,
                           CancelableCallback<CommentCollection> commentCollectionCallback);

    @GET("/data/report/{report}/likes")
    void getPostLikes(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("report") int reportId,
                      @Query("page") int page,
                      @Query("results_per_page") int numResults,
                      @Query("q") String q,
                      CancelableCallback<FavoriteCollection> favoriteCollectionCallback);

    @POST("/data/report")
    void postReport
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody,
             CancelableCallback<Report> cb);

    @POST("/data/report")
    Report postReportSync
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody);

    @DELETE("/data/report/{report}")
    void deleteSingleReport(@Header("Authorization") String authorization,
                            @Path("report") int reportId,
                            CancelableCallback<Response> responseCallback);

}
