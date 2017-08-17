package com.viableindustries.waterreporter.data;

import android.content.SharedPreferences;

import java.util.Map;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Ryan Hamley on 10/6/14.
 * This class defines the Retrofit interface with all the methods used to interact with the API
 * The top-level data classes returned from the API are:
 * ReportPostResponse
 */
public interface ReportService {

    final String ENDPOINT = "https://api.waterreporter.org/v2";

    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/data/report")
    public void getReports(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Query("page") int page,
                           @Query("results_per_page") int numResults,
                           @Query("q") String q,
                           Callback<FeatureCollection> featureCollectionCallback);

    @GET("/data/report/{report}")
    public void getSingleReport(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("report") int reportId,
                                Callback<Report> report);

    @PATCH("/data/report/{report}")
    public void setReportState(@Header("Authorization") String authorization,
                               @Header("Content-Type") String contentType,
                               @Path("report") int reportId,
                               @Body ReportStateBody reportStateBody,
                               Callback<Report> report);

    @PATCH("/data/report/{report}")
    public void updateReport(@Header("Authorization") String authorization,
                               @Header("Content-Type") String contentType,
                               @Path("report") int reportId,
                               @Body ReportPatchBody reportPatchBody,
                               Callback<Report> report);

    @GET("/data/report/{report}/groups")
    public void getReportGroups(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("report") int reportId,
                                Callback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/data/report/{report}/comments")
    public void getReportComments(@Header("Authorization") String authorization,
                                  @Header("Content-Type") String contentType,
                                  @Path("report") int reportId,
                                  @Query("page") int page,
                                  @Query("results_per_page") int numResults,
                                  @Query("q") String q,
                                  Callback<CommentCollection> commentCollectionCallback);

    @GET("/data/report/{report}/likes")
    public void getPostLikes(@Header("Authorization") String authorization,
                                  @Header("Content-Type") String contentType,
                                  @Path("report") int reportId,
                                  @Query("page") int page,
                                  @Query("results_per_page") int numResults,
                                  @Query("q") String q,
                                  Callback<FavoriteCollection> favoriteCollectionCallback);

    @POST("/data/report")
    public void postReport
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody,
             Callback<Report> cb);

    @DELETE("/data/report/{report}")
    public void deleteSingleReport(@Header("Authorization") String authorization,
                                   @Path("report") int reportId,
                                   Callback<Response> responseCallback);

}
