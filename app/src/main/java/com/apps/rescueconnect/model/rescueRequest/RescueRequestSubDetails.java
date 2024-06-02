package com.apps.rescueconnect.model.rescueRequest;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class RescueRequestSubDetails implements Parcelable {
    private String rescueRequestId;
    private String rescueRequestStatus;
    private String rescueRequestAcceptedId;
    private String rescueRequestAcceptedRole;
    private String rescueRequestModifiedOn;
    private String rescueRequestModifiedBy;

    public RescueRequestSubDetails() {
    }

    protected RescueRequestSubDetails(Parcel in) {
        rescueRequestId = in.readString();
        rescueRequestStatus = in.readString();
        rescueRequestAcceptedId = in.readString();
        rescueRequestAcceptedRole = in.readString();
        rescueRequestModifiedOn = in.readString();
        rescueRequestModifiedBy = in.readString();
    }

    public static final Creator<RescueRequestSubDetails> CREATOR = new Creator<RescueRequestSubDetails>() {
        @Override
        public RescueRequestSubDetails createFromParcel(Parcel in) {
            return new RescueRequestSubDetails(in);
        }

        @Override
        public RescueRequestSubDetails[] newArray(int size) {
            return new RescueRequestSubDetails[size];
        }
    };

    public String getRescueRequestId() {
        return rescueRequestId;
    }

    public void setRescueRequestId(String rescueRequestId) {
        this.rescueRequestId = rescueRequestId;
    }

    public String getRescueRequestStatus() {
        return rescueRequestStatus;
    }

    public void setRescueRequestStatus(String rescueRequestStatus) {
        this.rescueRequestStatus = rescueRequestStatus;
    }

    public String getRescueRequestAcceptedId() {
        return rescueRequestAcceptedId;
    }

    public void setRescueRequestAcceptedId(String rescueRequestAcceptedId) {
        this.rescueRequestAcceptedId = rescueRequestAcceptedId;
    }

    public String getRescueRequestAcceptedRole() {
        return rescueRequestAcceptedRole;
    }

    public void setRescueRequestAcceptedRole(String rescueRequestAcceptedRole) {
        this.rescueRequestAcceptedRole = rescueRequestAcceptedRole;
    }

    public String getRescueRequestModifiedOn() {
        return rescueRequestModifiedOn;
    }

    public void setRescueRequestModifiedOn(String rescueRequestModifiedOn) {
        this.rescueRequestModifiedOn = rescueRequestModifiedOn;
    }

    public String getRescueRequestModifiedBy() {
        return rescueRequestModifiedBy;
    }

    public void setRescueRequestModifiedBy(String rescueRequestModifiedBy) {
        this.rescueRequestModifiedBy = rescueRequestModifiedBy;
    }

    @Override
    public String toString() {
        return "RescueRequestSubDetails{" +
                "rescueRequestId='" + rescueRequestId + '\'' +
                ", rescueRequestStatus='" + rescueRequestStatus + '\'' +
                ", rescueRequestAcceptedId='" + rescueRequestAcceptedId + '\'' +
                ", rescueRequestAcceptedRole='" + rescueRequestAcceptedRole + '\'' +
                ", rescueRequestModifiedOn='" + rescueRequestModifiedOn + '\'' +
                ", rescueRequestModifiedBy='" + rescueRequestModifiedBy + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(rescueRequestId);
        parcel.writeString(rescueRequestStatus);
        parcel.writeString(rescueRequestAcceptedId);
        parcel.writeString(rescueRequestAcceptedRole);
        parcel.writeString(rescueRequestModifiedOn);
        parcel.writeString(rescueRequestModifiedBy);
    }
}
