package edu.northeastern.tipmate;

import com.google.android.gms.maps.model.LatLng;

public class TipHistory {
    private String id;
    private double latitude;
    private double longitude;
    private String title;
    private String desc;
    private long timestamp;

    // No-argument constructor
    public TipHistory() {
    }

    public TipHistory(double latitude, double longitude, String title, String desc, long timestamp) {
        this.id = null;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.desc = desc;
        this.timestamp = timestamp;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
