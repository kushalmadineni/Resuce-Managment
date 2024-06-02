package com.apps.rescueconnect.model.rescueRequest;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class RescueRequestMaster implements Parcelable {
    private String rescueRequestHeader;
    private String rescueRequestDescription;
    private String rescueRequestType;
    private String rescueRequestAssignAgencies;
    private String rescueRequestAccessType;
    private String rescueRequestCity;
    private String rescueRequestAcceptedOfficerId;
    private String rescueRequestAcceptedOfficerName;
    private String rescueRequestPlacePhotoId;
    private String rescueRequestPlacePhotoPath;
    private String rescueRequestPlacePhotoUploadedDate;
    private double rescueRequestPlaceLatitude;
    private double rescueRequestPlaceLongitude;
    private String rescueRequestPlaceAddress;
    private String rescueRequestCreatedBy;
    private String rescueRequestCreatedOn;

    private List<RescueRequestSubDetails> rescueRequestSubDetailsList = new ArrayList<>();

    public RescueRequestMaster() {
    }

    protected RescueRequestMaster(Parcel in) {
        rescueRequestHeader = in.readString();
        rescueRequestDescription = in.readString();
        rescueRequestType = in.readString();
        rescueRequestAssignAgencies = in.readString();
        rescueRequestAccessType = in.readString();
        rescueRequestCity = in.readString();
        rescueRequestAcceptedOfficerId = in.readString();
        rescueRequestAcceptedOfficerName = in.readString();
        rescueRequestPlacePhotoId = in.readString();
        rescueRequestPlacePhotoPath = in.readString();
        rescueRequestPlacePhotoUploadedDate = in.readString();
        rescueRequestPlaceLatitude = in.readDouble();
        rescueRequestPlaceLongitude = in.readDouble();
        rescueRequestPlaceAddress = in.readString();
        rescueRequestCreatedBy = in.readString();
        rescueRequestCreatedOn = in.readString();
        rescueRequestSubDetailsList = in.createTypedArrayList(RescueRequestSubDetails.CREATOR);
    }

    public static final Creator<RescueRequestMaster> CREATOR = new Creator<RescueRequestMaster>() {
        @Override
        public RescueRequestMaster createFromParcel(Parcel in) {
            return new RescueRequestMaster(in);
        }

        @Override
        public RescueRequestMaster[] newArray(int size) {
            return new RescueRequestMaster[size];
        }
    };

    public String getRescueRequestHeader() {
        return rescueRequestHeader;
    }

    public void setRescueRequestHeader(String rescueRequestHeader) {
        this.rescueRequestHeader = rescueRequestHeader;
    }

    public String getRescueRequestDescription() {
        return rescueRequestDescription;
    }

    public void setRescueRequestDescription(String rescueRequestDescription) {
        this.rescueRequestDescription = rescueRequestDescription;
    }

    public String getRescueRequestType() {
        return rescueRequestType;
    }

    public void setRescueRequestType(String rescueRequestType) {
        this.rescueRequestType = rescueRequestType;
    }

    public String getRescueRequestAssignAgencies() {
        return rescueRequestAssignAgencies;
    }

    public void setRescueRequestAssignAgencies(String rescueRequestAssignAgencies) {
        this.rescueRequestAssignAgencies = rescueRequestAssignAgencies;
    }

    public String getRescueRequestAccessType() {
        return rescueRequestAccessType;
    }

    public void setRescueRequestAccessType(String rescueRequestAccessType) {
        this.rescueRequestAccessType = rescueRequestAccessType;
    }

    public String getRescueRequestCity() {
        return rescueRequestCity;
    }

    public void setRescueRequestCity(String rescueRequestCity) {
        this.rescueRequestCity = rescueRequestCity;
    }

    public String getRescueRequestAcceptedOfficerId() {
        return rescueRequestAcceptedOfficerId;
    }

    public void setRescueRequestAcceptedOfficerId(String rescueRequestAcceptedOfficerId) {
        this.rescueRequestAcceptedOfficerId = rescueRequestAcceptedOfficerId;
    }

    public String getRescueRequestAcceptedOfficerName() {
        return rescueRequestAcceptedOfficerName;
    }

    public void setRescueRequestAcceptedOfficerName(String rescueRequestAcceptedOfficerName) {
        this.rescueRequestAcceptedOfficerName = rescueRequestAcceptedOfficerName;
    }

    public String getRescueRequestPlacePhotoId() {
        return rescueRequestPlacePhotoId;
    }

    public void setRescueRequestPlacePhotoId(String rescueRequestPlacePhotoId) {
        this.rescueRequestPlacePhotoId = rescueRequestPlacePhotoId;
    }

    public String getRescueRequestPlacePhotoPath() {
        return rescueRequestPlacePhotoPath;
    }

    public void setRescueRequestPlacePhotoPath(String rescueRequestPlacePhotoPath) {
        this.rescueRequestPlacePhotoPath = rescueRequestPlacePhotoPath;
    }

    public String getRescueRequestPlacePhotoUploadedDate() {
        return rescueRequestPlacePhotoUploadedDate;
    }

    public void setRescueRequestPlacePhotoUploadedDate(String rescueRequestPlacePhotoUploadedDate) {
        this.rescueRequestPlacePhotoUploadedDate = rescueRequestPlacePhotoUploadedDate;
    }

    public double getRescueRequestPlaceLatitude() {
        return rescueRequestPlaceLatitude;
    }

    public void setRescueRequestPlaceLatitude(double rescueRequestPlaceLatitude) {
        this.rescueRequestPlaceLatitude = rescueRequestPlaceLatitude;
    }

    public double getRescueRequestPlaceLongitude() {
        return rescueRequestPlaceLongitude;
    }

    public void setRescueRequestPlaceLongitude(double rescueRequestPlaceLongitude) {
        this.rescueRequestPlaceLongitude = rescueRequestPlaceLongitude;
    }

    public String getRescueRequestPlaceAddress() {
        return rescueRequestPlaceAddress;
    }

    public void setRescueRequestPlaceAddress(String rescueRequestPlaceAddress) {
        this.rescueRequestPlaceAddress = rescueRequestPlaceAddress;
    }

    public String getRescueRequestCreatedBy() {
        return rescueRequestCreatedBy;
    }

    public void setRescueRequestCreatedBy(String rescueRequestCreatedBy) {
        this.rescueRequestCreatedBy = rescueRequestCreatedBy;
    }

    public String getRescueRequestCreatedOn() {
        return rescueRequestCreatedOn;
    }

    public void setRescueRequestCreatedOn(String rescueRequestCreatedOn) {
        this.rescueRequestCreatedOn = rescueRequestCreatedOn;
    }

    public List<RescueRequestSubDetails> getRescueRequestSubDetailsList() {
        return rescueRequestSubDetailsList;
    }

    public void setRescueRequestSubDetailsList(List<RescueRequestSubDetails> rescueRequestSubDetailsList) {
        this.rescueRequestSubDetailsList = rescueRequestSubDetailsList;
    }

    @Override
    public String toString() {
        return "RescueRequestMaster{" +
                "rescueRequestHeader='" + rescueRequestHeader + '\'' +
                ", rescueRequestDescription='" + rescueRequestDescription + '\'' +
                ", rescueRequestType='" + rescueRequestType + '\'' +
                ", rescueRequestAssignAgencies='" + rescueRequestAssignAgencies + '\'' +
                ", rescueRequestAccessType='" + rescueRequestAccessType + '\'' +
                ", rescueRequestCity='" + rescueRequestCity + '\'' +
                ", rescueRequestAcceptedOfficerId='" + rescueRequestAcceptedOfficerId + '\'' +
                ", rescueRequestAcceptedOfficerName='" + rescueRequestAcceptedOfficerName + '\'' +
                ", rescueRequestPlacePhotoId='" + rescueRequestPlacePhotoId + '\'' +
                ", rescueRequestPlacePhotoPath='" + rescueRequestPlacePhotoPath + '\'' +
                ", rescueRequestPlacePhotoUploadedDate='" + rescueRequestPlacePhotoUploadedDate + '\'' +
                ", rescueRequestPlaceLatitude=" + rescueRequestPlaceLatitude +
                ", rescueRequestPlaceLongitude=" + rescueRequestPlaceLongitude +
                ", rescueRequestPlaceAddress='" + rescueRequestPlaceAddress + '\'' +
                ", rescueRequestCreatedBy='" + rescueRequestCreatedBy + '\'' +
                ", rescueRequestCreatedOn='" + rescueRequestCreatedOn + '\'' +
                ", rescueRequestSubDetailsList=" + rescueRequestSubDetailsList +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(rescueRequestHeader);
        parcel.writeString(rescueRequestDescription);
        parcel.writeString(rescueRequestType);
        parcel.writeString(rescueRequestAssignAgencies);
        parcel.writeString(rescueRequestAccessType);
        parcel.writeString(rescueRequestCity);
        parcel.writeString(rescueRequestAcceptedOfficerId);
        parcel.writeString(rescueRequestAcceptedOfficerName);
        parcel.writeString(rescueRequestPlacePhotoId);
        parcel.writeString(rescueRequestPlacePhotoPath);
        parcel.writeString(rescueRequestPlacePhotoUploadedDate);
        parcel.writeDouble(rescueRequestPlaceLatitude);
        parcel.writeDouble(rescueRequestPlaceLongitude);
        parcel.writeString(rescueRequestPlaceAddress);
        parcel.writeString(rescueRequestCreatedBy);
        parcel.writeString(rescueRequestCreatedOn);
        parcel.writeTypedList(rescueRequestSubDetailsList);
    }
}
