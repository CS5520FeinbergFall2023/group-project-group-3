package edu.northeastern.tipmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class TipHistoryAdapter extends RecyclerView.Adapter<TipHistoryViewHolder> {

    private List<TipHistory> historyList;
    private final Context context;

    private final GoogleMap googleMap;
    private final ClusterManager<HistoryMarker> clusterManager;
    private final DatabaseAPI databaseAPI;
    private final Map<String, List<HistoryMarker>> itemsCache;

    private static final SimpleDateFormat format = new SimpleDateFormat(" yyyy/MM/dd HH:mm:ss ", Locale.US);

    private static final String DEFAULT_DELETE_LIST = "itemsDeleted";

    private static final String DEFAULT_ADDED_LIST = "itemsAdded";

    public TipHistoryAdapter(List<TipHistory> historyList, Context context, GoogleMap googleMap,
            ClusterManager<HistoryMarker> clusterManager, DatabaseAPI databaseAPI,
            Map<String, List<HistoryMarker>> itemsCache) {
        this.historyList = historyList;
        this.context = context;
        this.googleMap = googleMap;
        this.clusterManager = clusterManager;
        this.databaseAPI = databaseAPI;
        this.itemsCache = itemsCache;
    }

    @NonNull
    @Override
    public TipHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TipHistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent, false));
    }

    public void setHistoryList(List<TipHistory> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    private void retractMarkers() {
        clusterManager.removeItems(itemsCache.get(DEFAULT_ADDED_LIST));
        clusterManager.addItems(itemsCache.get(DEFAULT_DELETE_LIST));
        clusterManager.cluster();

        itemsCache.get(DEFAULT_ADDED_LIST).clear();
        itemsCache.get(DEFAULT_DELETE_LIST).clear();
    }

    private HistoryMarker addHistoryMarker(TipHistory tipHistory) {
        HistoryMarker ret = new HistoryMarker(tipHistory.getLatitude(), tipHistory.getLongitude(),
                tipHistory.getTitle(), tipHistory.getDesc());
        clusterManager.addItem(ret);
        clusterManager.cluster();
        return ret;
    }

    @Override
    public void onBindViewHolder(@NonNull TipHistoryViewHolder holder, int position) {
        TipHistory cur = historyList.get(position);
        holder.title.setText(cur.getTitle());

        Date d = new Date(historyList.get(position).getTimestamp());
        holder.time.setText(format.format(d));
        holder.marker = addHistoryMarker(cur);

        holder.itemView.setOnClickListener(view -> {
            int pos = holder.getAdapterPosition();
            LatLng latLng = new LatLng(historyList.get(pos).getLatitude(), historyList.get(pos).getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel()));
        });
        holder.itemView.setOnLongClickListener(view -> {
            int pos = holder.getAdapterPosition();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Edit");
            View dialogView = LayoutInflater.from(context).inflate(R.layout.edit_history_dialog, null);
            builder.setView(dialogView);
            EditText title_view = dialogView.findViewById(R.id.new_history_title_input);
            EditText desc_view = dialogView.findViewById(R.id.new_history_desc_input);
            String old_title = historyList.get(pos).getTitle();
            String old_desc = historyList.get(pos).getDesc();
            title_view.setText(old_title);
            desc_view.setText(old_desc);
            builder.setPositiveButton("Edit", (dialog, which) -> {
                RecyclerView r = ((Activity) context).findViewById(R.id.HistoryRecyclerView);
                String title = title_view.getText().toString().trim();
                String desc = desc_view.getText().toString().trim();
                if (title.isEmpty()) {
                    Snackbar.make(r, "Title cannot be empty", Snackbar.LENGTH_SHORT).show();
                } else {
                    retractMarkers();
                    TipHistory target = historyList.get(pos);
                    target.setTitle(title);
                    target.setDesc(desc);
                    notifyItemChanged(pos);
                    databaseAPI.getTipHistoryById(target.getId()).setValue(target);

                    Snackbar success = Snackbar.make(r, "Successfully edited", Snackbar.LENGTH_SHORT);
                    success.setAction("UNDO", v -> {
                        retractMarkers();
                        TipHistory tmp = historyList.get(pos);
                        tmp.setTitle(old_title);
                        tmp.setDesc(old_desc);
                        databaseAPI.getTipHistoryById(tmp.getId()).setValue(tmp);
                        notifyItemChanged(pos);
                    });
                    success.show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_button);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
