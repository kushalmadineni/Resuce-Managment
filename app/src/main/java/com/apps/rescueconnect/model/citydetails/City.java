package com.apps.rescueconnect.model.citydetails;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class City implements Parcelable {
    private String cityName;
    private String createdBy;
    private String createdOn;
    private List<Taluk> talukList = new ArrayList<>();

    public City() {
    }

    protected City(Parcel in) {
        cityName = in.readString();
        createdBy = in.readString();
        createdOn = in.readString();
        talukList = in.createTypedArrayList(Taluk.CREATOR);
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    public List<Taluk> getTalukList() {
        return talukList;
    }

    public void setTalukList(List<Taluk> talukList) {
        this.talukList = talukList;
    }

    @Override
    public String toString() {
        return "City{" +
                "cityName='" + cityName + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", talukList=" + talukList +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cityName);
        parcel.writeString(createdBy);
        parcel.writeString(createdOn);
        parcel.writeTypedList(talukList);
    }
}
