package com.apps.rescueconnect.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    private String mobileNumber;
    private String mPin;
    private String mainRole;
    private String userCity;
    private String userType;
    private String userAgency;
    private String userAgencyPath;
    private String fullName;
    private String gender;
    private String isActive;

    public User() {
    }

    protected User(Parcel in) {
        mobileNumber = in.readString();
        mPin = in.readString();
        mainRole = in.readString();
        userCity = in.readString();
        userType = in.readString();
        userAgency = in.readString();
        userAgencyPath = in.readString();
        fullName = in.readString();
        gender = in.readString();
        isActive = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getmPin() {
        return mPin;
    }

    public void setmPin(String mPin) {
        this.mPin = mPin;
    }

    public String getMainRole() {
        return mainRole;
    }

    public void setMainRole(String mainRole) {
        this.mainRole = mainRole;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserAgency() {
        return userAgency;
    }

    public void setUserAgency(String userAgency) {
        this.userAgency = userAgency;
    }

    public String getUserAgencyPath() {
        return userAgencyPath;
    }

    public void setUserAgencyPath(String userAgencyPath) {
        this.userAgencyPath = userAgencyPath;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "User{" +
                "mobileNumber='" + mobileNumber + '\'' +
                ", mPin='" + mPin + '\'' +
                ", mainRole='" + mainRole + '\'' +
                ", userCity='" + userCity + '\'' +
                ", userType='" + userType + '\'' +
                ", userAgency='" + userAgency + '\'' +
                ", userAgencyPath='" + userAgencyPath + '\'' +
                ", fullName='" + fullName + '\'' +
                ", gender='" + gender + '\'' +
                ", isActive='" + isActive + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(mobileNumber);
        parcel.writeString(mPin);
        parcel.writeString(mainRole);
        parcel.writeString(userCity);
        parcel.writeString(userType);
        parcel.writeString(userAgency);
        parcel.writeString(userAgencyPath);
        parcel.writeString(fullName);
        parcel.writeString(gender);
        parcel.writeString(isActive);
    }
}
