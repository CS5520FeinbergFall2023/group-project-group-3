package edu.northeastern.tipmate;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Objects;

public class HistoryMarker implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;

    public HistoryMarker(double lat, double lng, String title, String snippet) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryMarker that = (HistoryMarker) o;
        return position.latitude==that.position.latitude && position.longitude==that.position.longitude && Objects.equals(title, that.title) && Objects.equals(snippet, that.snippet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, title, snippet);
    }
}
