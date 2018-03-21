package com.viableindustries.waterreporter.api.interfaces.data.campaign;

import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.campaign.CampaignCollection;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public interface CampaignService {

    @GET("/data/campaign")
    void getMany(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Query("page") int page,
                 @Query("results_per_page") int numResults,
                 @Query("q") String q,
                 Callback<CampaignCollection> campaignCollectionCallback);

    @GET("/data/campaign/{campaignId}")
    void getSingle(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("campaignId") int campaignId,
                         Callback<Campaign> campaignCallback);

}