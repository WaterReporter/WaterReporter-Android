package com.viableindustries.waterreporter.data.interfaces.api.post;

import com.viableindustries.waterreporter.data.objects.FeatureCollection;
import com.viableindustries.waterreporter.data.objects.comment.CommentCollection;
import com.viableindustries.waterreporter.data.objects.favorite.FavoriteCollection;
import com.viableindustries.waterreporter.data.objects.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.objects.post.Report;
import com.viableindustries.waterreporter.data.objects.post.ReportPatchBody;
import com.viableindustries.waterreporter.data.objects.post.ReportPostBody;
import com.viableindustries.waterreporter.data.objects.post.ReportStateBody;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

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
 * The top-level api classes returned from the API are:
 * ReportPostResponse
 */
public interface ReportService {

    String ENDPOINT = "https://api.waterreporter.org/v2/data";

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/report")
    void getReports(@Header("Authorization") String authorization,
                    @Header("Content-Type") String contentType,
                    @Query("page") int page,
                    @Query("results_per_page") int numResults,
                    @Query("q") String q,
                    CancelableCallback<FeatureCollection> featureCollectionCallback);

    @GET("/report/{report}")
    void getSingleReport(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("report") int reportId,
                         CancelableCallback<Report> report);

    @PATCH("/report/{report}")
    void setReportState(@Header("Authorization") String authorization,
                        @Header("Content-Type") String contentType,
                        @Path("report") int reportId,
                        @Body ReportStateBody reportStateBody,
                        CancelableCallback<Report> report);

    @PATCH("/report/{report}")
    void updateReport(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("report") int reportId,
                      @Body ReportPatchBody reportPatchBody,
                      CancelableCallback<Report> report);

    @GET("/report/{report}/groups")
    void getReportGroups(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("report") int reportId,
                         CancelableCallback<OrganizationFeatureCollection> organizationCollectionResponseCallback);

    @GET("/report/{report}/comments")
    void getReportComments(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Path("report") int reportId,
                           @Query("page") int page,
                           @Query("results_per_page") int numResults,
                           @Query("q") String q,
                           CancelableCallback<CommentCollection> commentCollectionCallback);

    @GET("/report/{report}/likes")
    void getPostLikes(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Path("report") int reportId,
                      @Query("page") int page,
                      @Query("results_per_page") int numResults,
                      @Query("q") String q,
                      CancelableCallback<FavoriteCollection> favoriteCollectionCallback);

    @POST("/report")
    void postReport
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody,
             CancelableCallback<Report> cb);

    @POST("/report")
    Report postReportSync
            (@Header("Authorization") String authorization,
             @Header("Content-Type") String contentType,
             @Body ReportPostBody reportPostBody);

    @DELETE("/report/{report}")
    void deleteSingleReport(@Header("Authorization") String authorization,
                            @Path("report") int reportId,
                            CancelableCallback<Response> responseCallback);

}
