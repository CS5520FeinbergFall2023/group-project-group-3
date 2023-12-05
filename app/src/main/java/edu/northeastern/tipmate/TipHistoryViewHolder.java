package edu.northeastern.tipmate;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.Marker;

public class TipHistoryViewHolder extends RecyclerView.ViewHolder{
    public TextView title;
    public TextView time;
    public HistoryMarker marker;
    public TipHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = itemView.findViewById(R.id.history_title);
        this.time = itemView.findViewById(R.id.history_time);
    }
}
