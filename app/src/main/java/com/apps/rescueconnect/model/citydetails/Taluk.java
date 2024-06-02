package com.apps.rescueconnect.model.citydetails;

import android.os.Parcel;
import android.os.Parcelable;

public class Taluk implements Parcelable {
    private String talukName;
    private String createdBy;
    private String createdOn;

    public Taluk() {
    }

    protected Taluk(Parcel in) {
        talukName = in.readString();
        createdBy = in.readString();
        createdOn = in.readString();
    }

    public static final Creator<Taluk> CREATOR = new Creator<Taluk>() {
        @Override
        public Taluk createFromParcel(Parcel in) {
            return new Taluk(in);
        }

        @Override
        public Taluk[] newArray(int size) {
            return new Taluk[size];
        }
    };

    public String getTalukName() {
        return talukName;
    }

    public void setTalukName(String talukName) {
        this.talukName = talukName;
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
        return "Taluk{" +
                "talukName='" + talukName + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdOn='" + createdOn + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(talukName);
        parcel.writeString(createdBy);
        parcel.writeString(createdOn);
    }
}
