package com.example.alo_i.smartsmsbox;

import java.io.Serializable;

public class Location implements Serializable{
    double latitude;
    double longitute;
    double altitude;

    public Location(double latitude, double longitute, double altitude) {
        this.latitude = latitude;
        this.longitute = longitute;
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitute() {
        return longitute;
    }

    public void setLongitute(double longitute) {
        this.longitute = longitute;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
