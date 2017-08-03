//package com.viableindustries.waterreporter;
//
//import android.app.Fragment;
//import android.app.FragmentTransaction;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.FragmentActivity;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.google.gson.Gson;
//import com.mapbox.mapboxsdk.MapboxAccountManager;
//import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
//import com.mapbox.mapboxsdk.camera.CameraPosition;
//import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.geometry.LatLngBounds;
//import com.mapbox.mapboxsdk.maps.MapView;
//import com.mapbox.mapboxsdk.maps.MapboxMap;
//import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
//import com.squareup.picasso.Picasso;
//import com.viableindustries.waterreporter.data.FeatureCollection;
//import com.viableindustries.waterreporter.data.Geometry;
//import com.viableindustries.waterreporter.data.QueryFilter;
//import com.viableindustries.waterreporter.data.QueryParams;
//import com.viableindustries.waterreporter.data.QuerySort;
//import com.viableindustries.waterreporter.data.Report;
//import com.viableindustries.waterreporter.data.ReportService;
//import com.viableindustries.waterreporter.mbox.CustomMarkerView;
//import com.viableindustries.waterreporter.mbox.CustomMarkerViewOptions;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import retrofit.Callback;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
//import static android.content.Context.MODE_PRIVATE;
//
///**
// * Created by brendanmcintyre on 8/10/16.
// */
//
//public class LocationDetailFragment extends Fragment {
//
//    private MapView mapView;
//
//    private RelativeLayout mapContainer;
//
//    private MapboxMap mMapboxMap;
//
//    private double latitude;
//    private double longitude;
//
//    private static int reportId;
//    private static String reportDescription;
//    private static String thumbNail;
//    private static String fullImage;
//    private static String creationDate;
//    private static String watershedName;
//    private static String groupList;
//    private static String commentCount;
//    private static String userName;
//    private static String userAvatar;
//    private static String status;
//
//    private SharedPreferences sharedPreferences;
//
//    private String mappedReports;
//
//    // The onCreateView method is called when Fragment should create its View object hierarchy,
//    // either dynamically or via XML layout inflation.
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        // Defines the xml file for the fragment
//        return inflater.inflate(R.layout.location_detail_fragment, parent, false);
//
//    }
//
//    // This event is triggered soon after onCreateView().
//    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//
//        super.onViewCreated(view, savedInstanceState);
//
//        // Mapbox access token only needs to be configured once in your app
//        MapboxAccountManager.start(getActivity(), getResources().getString(R.string.mapBoxToken));
//
//        if (view != null) {
//
//            mapView = (MapView) view.findViewById(R.id.mapview);
//
//            mapContainer = (RelativeLayout) view.findViewById(R.id.map_detail);
//
//            // Set a reference to SharedPreferences
//            sharedPreferences = getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);
//
//            // Reset stored list of mapped reports
//            sharedPreferences.edit().putString("mappedReports", "").apply();
//
//            // Retrieve report attributes
//            reportId = getArguments().getInt("REPORT_ID", 0);
//            reportDescription = getArguments().getString("REPORT_DESCRIPTION", "");
//            thumbNail = getArguments().getString("THUMBNAIL_URL", "");
//            fullImage = getArguments().getString("FULL_IMAGE_URL", "");
//            creationDate = getArguments().getString("REPORT_CREATED", "");
//            watershedName = getArguments().getString("REPORT_WATERSHED", "");
//            groupList = getArguments().getString("REPORT_GROUPS", "");
//            commentCount = getArguments().getString("COMMENT_COUNT", "");
//            userName = getArguments().getString("USER_NAME", "");
//            userAvatar = getArguments().getString("USER_AVATAR", null);
//            status = getArguments().getString("STATUS", "");
//
//            // Retrieve location data
//            latitude = getArguments().getDouble("REPORT_LATITUDE", 38.904722);
//            longitude = getArguments().getDouble("REPORT_LONGITUDE", -77.016389);
//
//            Log.d("lat", String.format("%s", latitude));
//            Log.d("lng", String.format("%s", longitude));
//
//            mapView.onCreate(savedInstanceState);
//
//            mapView.getMapAsync(new OnMapReadyCallback() {
//                @Override
//                public void onMapReady(final MapboxMap mapboxMap) {
//
//                    mMapboxMap = mapboxMap;
//
//                    final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();
//
//                    CameraPosition position = new CameraPosition.Builder()
//                            .target(new LatLng(latitude, longitude)) // Sets the new camera position
//                            .zoom(14) // Sets the zoom
//                            .build(); // Creates a CameraPosition from the builder
//
//                    Log.d("position", position.toString());
//
//                    mapboxMap.animateCamera(CameraUpdateFactory
//                            .newCameraPosition(position), 4000);
//
//                    // Add custom ViewMarker
//                    CustomMarkerViewOptions options = new CustomMarkerViewOptions();
//                    options.position(new LatLng(latitude, longitude));
//                    options.flat(true);
//                    options.reportId(reportId);
//                    options.reportDescription(reportDescription);
//                    options.thumbNail(thumbNail);
//                    options.fullImage(fullImage);
//                    options.creationDate(creationDate);
//                    options.watershedName(watershedName);
//                    options.groupList(groupList);
//                    options.commentCount(commentCount);
//                    options.userName(userName);
//                    options.userAvatar(userAvatar);
//                    options.status(status);
//                    options.inFocus(1);
//                    //options.anchor(0.5f, 0.5f);
//                    mapboxMap.addMarker(options);
//
//                    trackId(reportId);
//
////                mapboxMap.addMarker(new MarkerOptions()
////                        .position(new LatLng(latitude, longitude)));
//
//                    // if you want to customise a ViewMarker you need to extend ViewMarker and provide an adapter implementation
//                    // set adapters for child classes of ViewMarker
//                    markerViewManager.addMarkerViewAdapter(new MarkerAdapter(getActivity(), mapboxMap));
//
//                    mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
//                        @Override
//                        public void onCameraChange(CameraPosition position) {
//
//                            Log.d("mapPosition", position.toString());
//
//                            if (position.zoom >= 14) {
//
//                                LatLngBounds latLngBounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
//
//                                double north = latLngBounds.getLatNorth();
//                                double south = latLngBounds.getLatSouth();
//                                double east = latLngBounds.getLonEast();
//                                double west = latLngBounds.getLonWest();
//
//                                String polygon = String.format("SRID=4326;POLYGON((%s %s,%s %s,%s %s,%s %s,%s %s))", west, north, east, north, east, south, west, south, west, north);
//
//                                Log.d("polygonString", polygon);
//
//                                fetchNearbyReports(polygon, reportId);
//
//                            }
//
//                            Log.d("mapPosition", position.toString());
//
//                        }
//                    });
//                }
//            });
//
//        }
//
//    }
//
//    // Adapter for our custom marker view
//
//    private static class MarkerAdapter extends MapboxMap.MarkerViewAdapter<CustomMarkerView> {
//
//        private LayoutInflater inflater;
//        private MapboxMap mapboxMap;
//
//        public MarkerAdapter(@NonNull Context context, @NonNull MapboxMap mapboxMap) {
//            super(context);
//            this.inflater = LayoutInflater.from(context);
//            this.mapboxMap = mapboxMap;
//        }
//
//        @Nullable
//        @Override
//        public View getView(@NonNull CustomMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {
//            LocationDetailFragment.MarkerAdapter.ViewHolder viewHolder;
//            if (convertView == null) {
//                viewHolder = new LocationDetailFragment.MarkerAdapter.ViewHolder();
//                convertView = inflater.inflate(R.layout.view_custom_marker, parent, false);
//                viewHolder.image = (ImageView) convertView.findViewById(R.id.imageView);
//                viewHolder.actionBadge = (ImageView) convertView.findViewById(R.id.actionBadge);
//                viewHolder.markerPin = (ImageView) convertView.findViewById(R.id.markerPin);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (LocationDetailFragment.MarkerAdapter.ViewHolder) convertView.getTag();
//            }
//
//            // Display badge if report is closed
//            if (marker.getStatus().equals("closed")) {
//
//                viewHolder.actionBadge.setVisibility(View.VISIBLE);
//
//            } else {
//
//                viewHolder.actionBadge.setVisibility(View.GONE);
//
//            }
//
//            // Display active marker pin if report is "source"
//            if (marker.isInFocus() == 1) {
//
//                //viewHolder.actionBadge.setVisibility(View.VISIBLE);
//                //viewHolder.markerPin.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_marker_pin));
//
//                //viewHolder.markerPin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.active_marker_pin));
//
//                viewHolder.markerPin.setBackgroundResource(R.drawable.active_marker_pin);
//
//                viewHolder.image.setBackgroundResource(R.drawable.active_marker_border);
//
//            } else {
//
//                //viewHolder.actionBadge.setVisibility(View.GONE);
//                //viewHolder.markerPin.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_marker_pin));
//
//                viewHolder.markerPin.setBackgroundResource(R.drawable.default_marker_pin);
//
//                viewHolder.image.setBackgroundResource(R.drawable.marker_icon_border);
//
//            }
//
//            Picasso.with(getContext()).load(marker.getThumbNail()).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.image);
//
//            return convertView;
//        }
//
//        @Override
//        public boolean onSelect(@NonNull final CustomMarkerView marker, @NonNull final View convertView, boolean reselectionForViewReuse) {
//
//            //ImageView image = (ImageView) convertView.findViewById(R.id.imageView);
//
//            //image.setBackgroundResource(R.drawable.active_marker_border);
//
//            Log.d("anchor", String.format("anchorU %s", marker.getAnchorU()));
//            Log.d("anchor", String.format("anchorV %s", marker.getAnchorV()));
//
//            CameraPosition position = new CameraPosition.Builder()
//                    .target(marker.getPosition()) // Sets the new camera position
//                    //.zoom(14) // Sets the zoom
//                    .build(); // Creates a CameraPosition from the builder
//
//            mapboxMap.animateCamera(CameraUpdateFactory
//                    .newCameraPosition(position), 1000);
//
//            // Build new MarkerDetailFragment
//            MarkerDetailFragment markerDetailFragment = new MarkerDetailFragment();
//
//            Bundle markerAttrs = new Bundle();
//
//            markerAttrs.putInt("reportId", marker.getReportId());
//            markerAttrs.putString("reportDescription", marker.getReportDescription());
//            markerAttrs.putString("thumbNail", marker.getThumbNail());
//            markerAttrs.putString("fullImage", marker.getFullImage());
//            markerAttrs.putString("creationDate", marker.getCreationDate());
//            markerAttrs.putString("watershedName", marker.getWatershedName());
//            markerAttrs.putString("groupList", marker.getGroupList());
//            markerAttrs.putString("commentCount", marker.getCommentCount());
//            markerAttrs.putString("userName", marker.getUserName());
//            markerAttrs.putString("userAvatar", marker.getUserAvatar());
//            markerAttrs.putString("status", marker.getStatus());
//            markerAttrs.putDouble("latitude", marker.getPosition().getLatitude());
//            markerAttrs.putDouble("longitude", marker.getPosition().getLongitude());
//
//            markerDetailFragment.setArguments(markerAttrs);
//
//            FragmentTransaction fragmentTransaction = ((FragmentActivity) getContext()).getFragmentManager().beginTransaction();
//
//            fragmentTransaction.replace(R.id.map_detail, markerDetailFragment);
//            fragmentTransaction.addToBackStack(null);
//
//            fragmentTransaction.commit();
//
//            return false;
//
//            // false indicates that we are calling selectMarker after our animation ourselves
//            // true will let the system call it for you, which will result in showing an InfoWindow instantly
//            //return false;
//        }
//
//        @Override
//        public void onDeselect(@NonNull CustomMarkerView marker, @NonNull final View convertView) {
//
//            //ImageView image = (ImageView) convertView.findViewById(R.id.imageView);
//
//            //image.setBackgroundResource(R.drawable.marker_icon_border);
//
//            //convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//            //ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(convertView, View.ROTATION, 360, 0);
//            //rotateAnimator.addListener(new AnimatorListenerAdapter() {
////                @Override
////                public void onAnimationEnd(Animator animation) {
////                    super.onAnimationEnd(animation);
////                    convertView.setLayerType(View.LAYER_TYPE_NONE, null);
////                }
////            });
//            //rotateAnimator.start();
//        }
//
//        private static class ViewHolder {
//            ImageView image;
//            ImageView actionBadge;
//            ImageView markerPin;
//        }
//    }
//
//    private void trackId(int reportId) {
//
//        mappedReports = sharedPreferences.getString("mappedReports", "");
//
//        List<Integer> ids = new ArrayList<Integer>();
//
//        if (!mappedReports.isEmpty()) {
//
//            mappedReports = mappedReports.replaceAll("[\\p{Z}\\s]+", "");
//
//            String[] array = mappedReports.substring(1, mappedReports.length() - 1).split(",");
//
//            for (String id : array) {
//
//                ids.add(Integer.parseInt(id));
//
//            }
//
//        }
//
//        ids.add(reportId);
//
//        sharedPreferences.edit().putString("mappedReports", ids.toString()).apply();
//
//    }
//
//    private void onFetchSuccess(List<Report> reports) {
//
//        for (Report report : reports) {
//
//            Geometry geometry = report.geometry.geometries.get(0);
//
//            CustomMarkerViewOptions options = new CustomMarkerViewOptions();
//            options.position(new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0)));
//            options.flat(true);
//            options.reportId(report.id);
//            options.reportDescription(report.properties.description.trim());
//            options.thumbNail(report.properties.images.get(0).properties.icon_retina);
//            options.fullImage(report.properties.images.get(0).properties.square_retina);
//            options.creationDate(AttributeTransformUtility.parseDate(new SimpleDateFormat("MMM dd, yyyy", Locale.US), report.properties.created));
//            options.watershedName(AttributeTransformUtility.parseWatershedName(report.properties.territory));
//            options.groupList(AttributeTransformUtility.groupListSize(report.properties.groups));
//            options.commentCount(AttributeTransformUtility.countComments(report.properties.comments));
//            options.userName(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));
//            options.userAvatar(report.properties.owner.properties.picture);
//            options.status(report.properties.state);
//            options.inFocus(0);
//            //options.anchor(0.5f, 0.5f);
//            mMapboxMap.addMarker(options);
//
//            // Store report id for exclusion from future queries
//            trackId(report.id);
//
//        }
//
//    }
//
//    private void fetchNearbyReports(String polygon, int reportId) {
//
//        QueryFilter idFilter;
//
//        final String accessToken = sharedPreferences.getString("access_token", "");
//
//        Log.d("", accessToken);
//
//        // Retrieve the user id
//
//        int user_id = sharedPreferences.getInt("user_id", 0);
//
//        // Add query filters to retrieve the user's reports
//        // Create filters list and add a filter for owner_id
//
//        List<QueryFilter> queryFilters = new ArrayList<QueryFilter>();
//
//        QueryFilter geometryFilter = new QueryFilter("geometry", "intersects", polygon);
//
//        // Exclude mapped reports
//        mappedReports = sharedPreferences.getString("mappedReports", "");
//
//        if (!mappedReports.isEmpty()) {
//
//            List<Integer> ids = new ArrayList<Integer>();
//
//            mappedReports = mappedReports.replaceAll("[\\p{Z}\\s]+", "");
//
//            String[] array = mappedReports.substring(1, mappedReports.length() - 1).split(",");
//
//            for (String id : array) {
//
//                ids.add(Integer.parseInt(id));
//
//            }
//
//            idFilter = new QueryFilter("id", "not_in", ids);
//
//        } else {
//
//            idFilter = new QueryFilter("id", "neq", reportId);
//
//        }
//
//        queryFilters.add(geometryFilter);
//        queryFilters.add(idFilter);
//
//        // Create order_by list and add a sort parameter
//
//        List<QuerySort> queryOrder = new ArrayList<QuerySort>();
//
//        QuerySort querySort = new QuerySort("created", "desc");
//
//        queryOrder.add(querySort);
//
//        // Create query string from new QueryParams
//
//        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);
//
//        String query = new Gson().toJson(queryParams);
//
//        Log.d("URL", query);
//
//        ReportService service = ReportService.restAdapter.create(ReportService.class);
//
//        service.getReports(accessToken, "application/json", 1, 25, query, new Callback<FeatureCollection>() {
//
//            @Override
//            public void success(FeatureCollection featureCollection, Response response) {
//
//                List<Report> reports = featureCollection.getFeatures();
//
//                Log.v("list", reports.toString());
//
//                if (!reports.isEmpty()) {
//
//                    onFetchSuccess(reports);
//
//                }
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                Response response = error.getResponse();
//
//                RetrofitError.Kind r = error.getKind();
//
//                Log.d("HTTP Error:", response.toString());
//
//                Log.d("HTTP Error:", error.getMessage() + r);
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (response != null) {
//
//                    int status = response.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(getActivity(), MainActivity.class));
//
//                    }
//
//                }
//
//            }
//
//        });
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mapView.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//}
