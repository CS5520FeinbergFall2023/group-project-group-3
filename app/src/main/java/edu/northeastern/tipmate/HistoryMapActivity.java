package edu.northeastern.tipmate;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.tipmate.databinding.ActivityHistoryMapBinding;

public class HistoryMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private Marker currentMarker;

    private View root;
    private TipHistoryAdapter historyAdapter;
    private static final List<TipHistory> historyList = new ArrayList<>();

    private void createRecyclerView() {

        RecyclerView historyRecyclerView = findViewById(R.id.HistoryRecyclerView);

        //This defines the way in which the RecyclerView is oriented
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Associates the adapter with the RecyclerView
        historyRecyclerView.setAdapter(new TipHistoryAdapter(historyList, this,mMap));

        historyRecyclerView.setHasFixedSize(true);

        historyAdapter = (TipHistoryAdapter) historyRecyclerView.getAdapter();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getLayoutPosition();

                TipHistory tmp = historyList.get(position);
                historyList.remove(position);
                historyAdapter.notifyItemRemoved(position);
                ((TipHistoryViewHolder) viewHolder).marker.remove();

                Snackbar deleted = Snackbar.make(root, "Deleted an record", Snackbar.LENGTH_SHORT);
                deleted.setAction("UNDO", view -> {
                    historyList.add(position,tmp);
                    historyAdapter.notifyItemInserted(position);
                });
                deleted.show();
            }
        });
        itemTouchHelper.attachToRecyclerView(historyRecyclerView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = getWindow().getDecorView().findViewById(android.R.id.content);

        edu.northeastern.tipmate.databinding.ActivityHistoryMapBinding binding = ActivityHistoryMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PackageManager.PERMISSION_GRANTED);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (savedInstanceState != null) {
            if(savedInstanceState.keySet().contains("LOCATION_LONG_KEY")){
                longitude = savedInstanceState.getDouble("LOCATION_LONG_KEY");
            }
            if(savedInstanceState.keySet().contains("LOCATION_LAT_KEY")){
                latitude = savedInstanceState.getDouble("LOCATION_LAT_KEY");
            }
        }else{
            startLocationUpdates();
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location!=null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putDouble("LOCATION_LAT_KEY",
                latitude);
        outState.putDouble("LOCATION_LONG_KEY",
                longitude);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,30000,0,this);
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        updateMarker();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    private void updateMarker(){
        currentMarker.setPosition(new LatLng(latitude,longitude));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),14.0f));

        createRecyclerView();
        addExamples();
    }

    private void addExamples(){
        LatLng[] latLngArray = new LatLng[]{
                new LatLng(47.6195,-122.3454),
                new LatLng(47.4262,-122.3373),
                new LatLng(47.6218,-122.3374),
                new LatLng(47.6190,-122.3364),
                new LatLng(47.6185,-122.3464),
                new LatLng(47.4252,-122.3363),
                new LatLng(47.6208,-122.3364),
                new LatLng(47.6180,-122.3374),
        };
        int i = 0;
        for(LatLng l:latLngArray){
            historyList.add(new TipHistory(l,"test","test desc",System.currentTimeMillis()));
            historyAdapter.notifyItemInserted(i++);
        }
    }
}