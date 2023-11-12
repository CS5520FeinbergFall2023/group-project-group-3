package edu.northeastern.tipmate;

import com.google.android.gms.maps.model.LatLng;

public class TipHistory {
    private LatLng latLng;
    private String title;
    private String desc;
    private long timestamp;

    public TipHistory(LatLng latLng, String title, String desc, long timestamp) {
        this.latLng = latLng;
        this.title = title;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
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
