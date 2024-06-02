package com.apps.rescueconnect.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Agencies implements Parcelable {
    private String name;
    private String path;

    public Agencies() {
    }

    protected Agencies(Parcel in) {
        name = in.readString();
        path = in.readString();
    }

    public static final Creator<Agencies> CREATOR = new Creator<Agencies>() {
        @Override
        public Agencies createFromParcel(Parcel in) {
            return new Agencies(in);
        }

        @Override
        public Agencies[] newArray(int size) {
            return new Agencies[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Agencies{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(path);
    }
}
