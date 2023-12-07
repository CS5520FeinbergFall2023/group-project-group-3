package edu.northeastern.tipmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.northeastern.tipmate.databinding.ActivityHistoryMapBinding;

public class HistoryMapActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, ClusterManager.OnClusterClickListener<HistoryMarker> {

    @Nullable
    private GoogleMap mMap;
    private LocationManager locationManager;

    private Marker currentMarker;
    private View root;
    private TipHistoryAdapter historyAdapter;
    private List<TipHistory> historyList;
    private DatabaseReference databaseReference;
    private DatabaseAPI databaseAPI;

    private ClusterManager<HistoryMarker> clusterManager;

    private DefaultClusterRenderer<HistoryMarker> clusterRenderer;

    private static final String DEFAULT_DELETE_LIST = "itemsDeleted";

    private static final String DEFAULT_ADDED_LIST = "itemsAdded";
    private Map<String, List<HistoryMarker>> itemsCache = new HashMap<>();

    private boolean mapReady;

    private double latitude;
    private double longitude;

    private ValueEventListener historyListner;

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }


    private void createRecyclerView() {

        RecyclerView historyRecyclerView = findViewById(R.id.HistoryRecyclerView);

        //This defines the way in which the RecyclerView is oriented
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Associates the adapter with the RecyclerView
        historyRecyclerView.setAdapter(new TipHistoryAdapter(historyList, this, mMap, clusterManager,databaseAPI));

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
                clusterManager.removeItem(((TipHistoryViewHolder) viewHolder).marker);
                clusterManager.cluster();
                databaseAPI.getTipHistoryById(tmp.getId()).setValue(null);

                Snackbar deleted = Snackbar.make(root, "Deleted an record", Snackbar.LENGTH_SHORT);
                deleted.setAction("UNDO", view -> {
                    historyList.add(position, tmp);
                    historyAdapter.notifyItemInserted(position);
                    databaseAPI.storeTipHistory(tmp);
                });
                deleted.show();
            }
        });
        itemTouchHelper.attachToRecyclerView(historyRecyclerView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemsCache.put(DEFAULT_ADDED_LIST, new ArrayList<HistoryMarker>());
        itemsCache.put(DEFAULT_DELETE_LIST, new ArrayList<HistoryMarker>());
        mapReady = false;
        historyList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseAPI = new DatabaseAPI(databaseReference, this);
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
            if (savedInstanceState.keySet().contains("LOCATION_LONG_KEY")) {
                longitude = savedInstanceState.getDouble("LOCATION_LONG_KEY");
            }
            if (savedInstanceState.keySet().contains("LOCATION_LAT_KEY")) {
                latitude = savedInstanceState.getDouble("LOCATION_LAT_KEY");
            }
        } else {
            startLocationUpdates();
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
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
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        databaseAPI.getTipHistory().removeEventListener(historyListner);
    }

    private boolean itemsInSameLocation(Cluster<HistoryMarker> cluster) {
        LinkedList<HistoryMarker> items = new LinkedList<>(cluster.getItems());
        HistoryMarker item = items.remove(0);

        double longitude = item.getPosition().longitude;
        double latitude = item.getPosition().latitude;

        for (HistoryMarker t : items) {
            if (Double.compare(longitude, t.getPosition().longitude) != 0 && Double.compare(latitude, t.getPosition().latitude) != 0) {
                return false;
            }
        }

        return true;
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

//        currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("You").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),14.0f));
        clusterManager = new ClusterManager<HistoryMarker>(this, mMap);
        clusterRenderer = new DefaultClusterRenderer<HistoryMarker>(this, mMap, clusterManager);
        clusterManager.setRenderer(clusterRenderer);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.getMarkerCollection().setInfoWindowAdapter(new HistoryInfoWindowAdapter(LayoutInflater.from(this)));

        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {

                // get markers back to the original position if they were relocated
                if (mMap.getCameraPosition().zoom < mMap.getMaxZoomLevel()) {
                    clusterManager.removeItems(itemsCache.get(DEFAULT_ADDED_LIST));
                    clusterManager.addItems(itemsCache.get(DEFAULT_DELETE_LIST));
                    clusterManager.cluster();

                    itemsCache.get(DEFAULT_ADDED_LIST).clear();
                    itemsCache.get(DEFAULT_DELETE_LIST).clear();
                }
            }
        });
        clusterManager.setOnClusterClickListener(this);
        createRecyclerView();
//        addExamples();
        loadHistory();

        mapReady = true;
    }

    private void loadHistory(){
        historyListner = databaseAPI.getTipHistory().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyList.clear();
                for (DataSnapshot postSnapshot :dataSnapshot.getChildren()) {
                    TipHistory t = postSnapshot.getValue(TipHistory.class);
                    if(t!=null){
                        t.setId(dataSnapshot.getKey());
                        historyList.add(t);
                    }
                }
                historyAdapter.setHistoryList(historyList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            historyList.add(new TipHistory(l.latitude, l.longitude,"test","test desc",System.currentTimeMillis()));
            historyAdapter.notifyItemInserted(i++);
        }
    }

    @Override
    public boolean onClusterClick(Cluster<HistoryMarker> cluster) {
        float maxZoomLevel = mMap.getMaxZoomLevel();
        float currentZoomLevel = mMap.getCameraPosition().zoom;

        // only show markers if users is in the max zoom level
        if(!itemsInSameLocation(cluster)){
            return false;
        }
        if (currentZoomLevel != maxZoomLevel) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),mMap.getMaxZoomLevel()));
            Toast.makeText(this,"Click one more to expand",Toast.LENGTH_SHORT).show();
        }else {
            // relocate the markers around the current markers position
            int counter = 0;
            float rotateFactor = (360f / cluster.getItems().size());
            for (HistoryMarker item : cluster.getItems()) {
                double lat = item.getPosition().latitude + (0.00003 * Math.cos(++counter * rotateFactor));
                double lng = item.getPosition().longitude + (0.00003 * Math.sin(counter * rotateFactor));
                HistoryMarker copy = new HistoryMarker(lat, lng, item.getTitle(), item.getSnippet());

                clusterManager.removeItem(item);
                clusterManager.addItem(copy);
                clusterManager.cluster();

                itemsCache.get(DEFAULT_ADDED_LIST).add(copy);
                itemsCache.get(DEFAULT_DELETE_LIST).add(item);
            }
        }
        return true;
    }
}