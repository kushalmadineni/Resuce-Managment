package com.apps.rescueconnect.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.apps.rescueconnect.model.citydetails.Taluk;

import java.util.ArrayList;
import java.util.List;

public class RescueType implements Parcelable {
    private String rescueName;
    private String createdBy;
    private String createdOn;

    public RescueType() {
    }

    protected RescueType(Parcel in) {
        rescueName = in.readString();
        createdBy = in.readString();
        createdOn = in.readString();
    }

    public static final Creator<RescueType> CREATOR = new Creator<RescueType>() {
        @Override
        public RescueType createFromParcel(Parcel in) {
            return new RescueType(in);
        }

        @Override
        public RescueType[] newArray(int size) {
            return new RescueType[size];
        }
    };

    public String getRescueName() {
        return rescueName;
    }

    public void setRescueName(String rescueName) {
        this.rescueName = rescueName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "RescueType{" +
                "rescueName='" + rescueName + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdOn='" + createdOn + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(rescueName);
        parcel.writeString(createdBy);
        parcel.writeString(createdOn);
    }
}
