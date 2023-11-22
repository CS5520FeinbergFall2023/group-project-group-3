package edu.northeastern.tipmate;

import com.google.android.gms.maps.model.LatLng;

public class TipHistory {
    private double latitude;
    private double longitude;
    private String title;
    private String desc;
    private long timestamp;

    // No-argument constructor
    public TipHistory() {
    }

    public TipHistory(double latitude, double longitude, String title, String desc, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.desc = desc;
        this.timestamp = timestamp;
    }


    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void setLatLng(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;;
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
