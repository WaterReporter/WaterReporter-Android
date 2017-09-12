package com.viableindustries.waterreporter.map_box;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerViewOptions extends BaseMarkerViewOptions<CustomMarkerView, CustomMarkerViewOptions> {

//    private Report report;

    private int reportId;
    private String reportDescription;
    private String thumbNail;
    private String fullImage;
    private String creationDate;
    private String watershedName;
    private String groupList;
    private String commentCount;
    private String userName;
    private String userAvatar;
    private String status;
    private int inFocus;
    private int index;

    public CustomMarkerViewOptions() {
    }

    private CustomMarkerViewOptions(Parcel in) {
        position((LatLng) in.readParcelable(LatLng.class.getClassLoader()));
        snippet(in.readString());
        title(in.readString());
        flat(in.readByte() != 0);
        anchor(in.readFloat(), in.readFloat());
        selected = in.readByte() != 0;
        rotation(in.readFloat());
        if (in.readByte() != 0) {
            // this means we have an icon
            String iconId = in.readString();
            Bitmap iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
            Icon icon = IconFactory.recreate(iconId, iconBitmap);
            icon(icon);
        }
        reportId(in.readInt());
        reportDescription(in.readString());
        thumbNail(in.readString());
        fullImage(in.readString());
        creationDate(in.readString());
        watershedName(in.readString());
        groupList(in.readString());
        commentCount(in.readString());
        userName(in.readString());
        userAvatar(in.readString());
        status(in.readString());
        inFocus(in.readInt());
        index(in.readInt());
    }

    @Override
    public CustomMarkerViewOptions getThis() {
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(getPosition(), flags);
        out.writeString(getSnippet());
        out.writeString(getTitle());
        out.writeByte((byte) (isFlat() ? 1 : 0));
        out.writeFloat(getAnchorU());
        out.writeFloat(getAnchorV());
        out.writeFloat(getInfoWindowAnchorU());
        out.writeFloat(getInfoWindowAnchorV());
        out.writeByte((byte) (selected ? 1 : 0));
        out.writeFloat(getRotation());
        Icon icon = getIcon();
        out.writeByte((byte) (icon != null ? 1 : 0));
        if (icon != null) {
            out.writeString(getIcon().getId());
            out.writeParcelable(getIcon().getBitmap(), flags);
        }
        out.writeInt(reportId);
        out.writeString(reportDescription);
        out.writeString(thumbNail);
        out.writeString(fullImage);
        out.writeString(creationDate);
        out.writeString(watershedName);
        out.writeString(groupList);
        out.writeString(commentCount);
        out.writeString(userName);
        out.writeString(userAvatar);
        out.writeString(status);
        out.writeInt(inFocus);
        out.writeInt(index);
    }

    @Override
    public CustomMarkerView getMarker() {
        return new CustomMarkerView(this, reportId, reportDescription, thumbNail, fullImage, creationDate, watershedName, groupList, commentCount, userName, userAvatar, status, inFocus, index);
    }

    public CustomMarkerViewOptions reportId(int reportId) {
        this.reportId = reportId;
        return getThis();
    }

    private CustomMarkerViewOptions reportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
        return getThis();
    }

    public CustomMarkerViewOptions thumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
        return getThis();
    }

    public CustomMarkerViewOptions fullImage(String fullImage) {
        this.fullImage = fullImage;
        return getThis();
    }

    private CustomMarkerViewOptions creationDate(String creationDate) {
        this.creationDate = creationDate;
        return getThis();
    }

    private CustomMarkerViewOptions watershedName(String watershedName) {
        this.watershedName = watershedName;
        return getThis();
    }

    private CustomMarkerViewOptions groupList(String groupList) {
        this.groupList = groupList;
        return getThis();
    }

    private CustomMarkerViewOptions commentCount(String commentCount) {
        this.commentCount = commentCount;
        return getThis();
    }

    private CustomMarkerViewOptions userName(String userName) {
        this.userName = userName;
        return getThis();
    }

    private CustomMarkerViewOptions userAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
        return getThis();
    }

    public CustomMarkerViewOptions status(String status) {
        this.status = status;
        return getThis();
    }

    public CustomMarkerViewOptions inFocus(int inFocus) {
        this.inFocus = inFocus;
        return getThis();
    }

    public CustomMarkerViewOptions index(int index) {
        this.index = index;
        return getThis();
    }

    public static final Parcelable.Creator<CustomMarkerViewOptions> CREATOR
            = new Parcelable.Creator<CustomMarkerViewOptions>() {
        public CustomMarkerViewOptions createFromParcel(Parcel in) {
            return new CustomMarkerViewOptions(in);
        }

        public CustomMarkerViewOptions[] newArray(int size) {
            return new CustomMarkerViewOptions[size];
        }
    };

}
