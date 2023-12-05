package edu.northeastern.tipmate;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.jetbrains.annotations.NotNull;

public class HistoryInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private  final LayoutInflater mInflater;

    public HistoryInfoWindowAdapter(LayoutInflater mInflater) {
        this.mInflater = mInflater;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        return initialData(marker);
    }
    @Override
    public View getInfoContents(Marker marker) {
        return initialData(marker);
    }

    @NotNull
    private View initialData(Marker marker) {
        final View popup=mInflater.inflate(R.layout.history_info_window,null);
        TextView title = popup.findViewById(R.id.history_info_title);
        title.setText(marker.getTitle());

        TextView desc = popup.findViewById(R.id.history_info_desc);
        desc.setText(marker.getSnippet());
        return popup;
    }
}
