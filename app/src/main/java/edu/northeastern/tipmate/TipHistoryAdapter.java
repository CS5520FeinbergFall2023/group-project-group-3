package edu.northeastern.tipmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TipHistoryAdapter extends RecyclerView.Adapter<TipHistoryViewHolder> {

    private final List<TipHistory> historyList;
    private final Context context;

    private final GoogleMap googleMap;

    private static final SimpleDateFormat format = new SimpleDateFormat(" yyyy/MM/dd HH:mm:ss ", Locale.US);

    public TipHistoryAdapter(List<TipHistory> historyList, Context context, GoogleMap googleMap) {
        this.historyList = historyList;
        this.context = context;
        this.googleMap = googleMap;
    }

    @NonNull
    @Override
    public TipHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TipHistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.history_item, parent,false));
    }

    private Marker addHistoryMarker(TipHistory tipHistory){
        return googleMap.addMarker(new MarkerOptions().position(tipHistory.getLatLng()).title(tipHistory.getTitle()).snippet(tipHistory.getDesc()));
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
            LatLng latLng = historyList.get(pos).getLatLng();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14.0f));
        });
        holder.itemView.setOnLongClickListener(view -> {
            int pos = holder.getAdapterPosition();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Please edit title and desc");
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
                }else {
                    TipHistory target = historyList.get(pos);
                    target.setTitle(title);
                    target.setDesc(desc);
                    holder.marker.remove();
                    notifyItemChanged(pos);

                    Snackbar success = Snackbar.make(r, "Successfully edited", Snackbar.LENGTH_SHORT);
                    success.setAction("UNDO", v -> {
                        TipHistory tmp = historyList.get(pos);
                        tmp.setTitle(old_title);
                        tmp.setDesc(old_desc);
                        notifyItemChanged(pos);
                    });
                    success.show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
