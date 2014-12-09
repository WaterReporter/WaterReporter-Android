package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/**
 * Created by Ryan Hamley on 10/6/14.
 * This class defines the Retrofit interface with all the methods used to interact with the API
 * The top-level data classes returned from the API are:
 * CommonsCloudResponse, SingleFeatureResponse, TemplateResponse and PostResponse
 */
public interface CommonsCloudService {
    final String ENDPOINT = "https://api.commonscloud.org/v2";
    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/type_2c1bd72acccf416aada3a6824731acc9.json")
    public void getReports(Callback<CommonsCloudResponse> response);

    @GET("/type_2c1bd72acccf416aada3a6824731acc9/{report}.json")
    public void getSingleReport(@Path("report") int reportId, Callback<SingleFeatureResponse> report);

    @GET("/templates/3/fields.json")
    public void getFields(Callback<TemplateResponse> fields);

    @Multipart
    @POST("/type_01b37f984f5b4d5c9b906651f62597f5.json")
    public void postReport(@Part("caption") String caption,
                                    @Part("date") String date,
                                    @Part("facility") String facility,
                                    @Part("issue") String issue,
                                    @Part("location") String location,
                                    @Part("status") String status,
                                    @Part("geometry") String geometry,
                                    Callback<PostResponse> cb);

    @Multipart
    @POST("/type_01b37f984f5b4d5c9b906651f62597f5.json")
    public void postReportWithPhoto(@Part("caption") String caption,
                                @Part("date") String date,
                                @Part("facility") String facility,
                                @Part("issue") String issue,
                                @Part("location") String location,
                                @Part("status") String status,
                                @Part("geometry") String geometry,
                                @Part("attachment_7097c3caac4149d5900559916b902ec3") TypedFile photo,
                                Callback<PostResponse> cb);
}
