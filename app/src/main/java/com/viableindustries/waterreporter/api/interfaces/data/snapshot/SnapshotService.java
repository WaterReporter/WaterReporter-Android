package com.viableindustries.waterreporter.api.interfaces.data.snapshot;

import com.viableindustries.waterreporter.api.models.snapshot.CampaignLeaderboard;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignSnapshot;
import com.viableindustries.waterreporter.api.models.snapshot.OrganizationSnapshot;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotCampaignList;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotGroupList;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotMemberList;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotWatershedList;
import com.viableindustries.waterreporter.api.models.snapshot.UserSnapshot;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public interface SnapshotService {

    //
    // Campaign
    //

    @GET("/data/snapshot/campaign/{campaignId}")
    void getCampaign(@Header("Authorization") String authorization,
                     @Header("Content-Type") String contentType,
                     @Path("campaignId") int campaignId,
                     Callback<CampaignSnapshot> campaignSnapshotCallback);

    @GET("/data/snapshot/campaign/{campaignId}/leaderboard")
    void getCampaignLeaderboard(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("campaignId") int campaignId,
                                Callback<CampaignLeaderboard> campaignLeaderboardCallback);

    @GET("/data/snapshot/campaign/{campaignId}/users")
    void getCampaignMembers(@Header("Authorization") String authorization,
                            @Header("Content-Type") String contentType,
                            @Query("page") int page,
                            @Path("campaignId") int campaignId,
                            Callback<SnapshotMemberList> snapshotMemberListCallback);

    @GET("/data/snapshot/campaign/{campaignId}/groups")
    void getCampaignGroups(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Query("page") int page,
                           @Path("campaignId") int campaignId,
                           Callback<SnapshotGroupList> snapshotGroupListCallback);

    @GET("/data/snapshot/campaign/{campaignId}/watersheds")
    void getCampaignWatersheds(@Header("Authorization") String authorization,
                               @Header("Content-Type") String contentType,
                               @Query("page") int page,
                               @Path("campaignId") int campaignId,
                               Callback<SnapshotWatershedList> snapshotWatershedListCallback);

    //
    // Organization
    //

    @GET("/data/snapshot/organization/{organizationId}")
    void getOrganization(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("organizationId") int organizationId,
                         Callback<OrganizationSnapshot> organizationSnapshotCallback);

    @GET("/data/snapshot/organization/{organizationId}/campaigns")
    void getOrganizationCampaigns(@Header("Authorization") String authorization,
                                  @Header("Content-Type") String contentType,
                                  @Query("page") int page,
                                  @Path("organizationId") int organizationId,
                                  Callback<SnapshotCampaignList> snapshotCampaignListCallback);

    @GET("/data/snapshot/organization/{organizationId}/users")
    void getOrganizationUsers(@Header("Authorization") String authorization,
                              @Header("Content-Type") String contentType,
                              @Query("page") int page,
                              @Path("organizationId") int organizationId,
                              Callback<SnapshotMemberList> snapshotMemberListCallback);

    @GET("/data/snapshot/organization/{organizationId}/watersheds")
    void getOrganizationWatersheds(@Header("Authorization") String authorization,
                                   @Header("Content-Type") String contentType,
                                   @Query("page") int page,
                                   @Path("organizationId") int organizationId,
                                   Callback<SnapshotWatershedList> snapshotWatershedListCallback);

    //
    // User
    //

    @GET("/data/snapshot/user/{userId}")
    void getUser(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("userId") int userId,
                 Callback<UserSnapshot> userSnapshotCallback);

    @GET("/data/snapshot/user/{userId}/campaigns")
    void getUserCampaigns(@Header("Authorization") String authorization,
                          @Header("Content-Type") String contentType,
                          @Query("page") int page,
                          @Path("userId") int userId,
                          Callback<SnapshotCampaignList> snapshotCampaignListCallback);

    @GET("/data/snapshot/user/{userId}/groups")
    void getUserGroups(@Header("Authorization") String authorization,
                       @Header("Content-Type") String contentType,
                       @Query("page") int page,
                       @Path("userId") int userId,
                       Callback<SnapshotGroupList> snapshotGroupListCallback);

    @GET("/data/snapshot/user/{userId}/watersheds")
    void getUserWatersheds(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Query("page") int page,
                           @Path("userId") int userId,
                           Callback<SnapshotWatershedList> snapshotWatershedListCallback);

    //
    // Watershed
    //

    @GET("/data/snapshot/watershed/{watershedId}")
    void getWatershed(@Header("Authorization") String authorization,
                      @Header("Content-Type") String contentType,
                      @Query("page") int page,
                      @Path("watershedId") long watershedId,
                      Callback<OrganizationSnapshot> organizationSnapshotCallback);

    @GET("/data/snapshot/watershed/{watershedId}/campaigns")
    void getWatershedCampaigns(@Header("Authorization") String authorization,
                               @Header("Content-Type") String contentType,
                               @Query("page") int page,
                               @Path("watershedId") long watershedId,
                               Callback<SnapshotCampaignList> snapshotCampaignListCallback);

    @GET("/data/snapshot/watershed/{watershedId}/groups")
    void getWatershedGroups(@Header("Authorization") String authorization,
                            @Header("Content-Type") String contentType,
                            @Query("page") int page,
                            @Path("watershedId") long watershedId,
                            Callback<SnapshotGroupList> snapshotGroupListCallback);

    @GET("/data/snapshot/watershed/{watershedId}/users")
    void getWatershedUsers(@Header("Authorization") String authorization,
                           @Header("Content-Type") String contentType,
                           @Query("page") int page,
                           @Path("watershedId") long watershedId,
                           Callback<SnapshotMemberList> snapshotMemberListCallback);

}
