package com.example.alo_i.smartsmsbox;

public class DBMessage {
    public static final int PERSONAL=0;
    public static final int COMMERCIAL=1;
    public static final int SPAM=2;
    public static final int OTP=3;
    public String categoryToString(){
        StringBuilder result = new StringBuilder(11);
        switch (category){
            case 0:
                result.append("Personal");
                break;
            case 1:
                result.append("Commercial");
                break;
            case 2:
                result.append("Spam");
                break;
            case 3:
                result.append("OTP");
                break;
            default:
                result.append("ERROR");
        }
        return result.toString();
    }
    String id;
    double latitude;
    double longitude;
    double altitude;
    int category ;
    String text;

    public DBMessage(String id, double latitude, double longitude, double altitude, int category, String text) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.category = category;
        this.text = text;
    }

    public DBMessage(String id, int category, double latitude, double longitude, double altitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
