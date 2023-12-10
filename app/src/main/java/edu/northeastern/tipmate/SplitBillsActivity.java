package edu.northeastern.tipmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class SplitBillsActivity extends AppCompatActivity implements LocationListener {
    private static SplitBillsActivity splitBillsActivity;
    private SeekBar splitSeekBar;
    private EditText totalPriceView;
    private TextView splitTextView;
    private TextView totalPP;
    private double totalPrice;
    private int numofPerson = 2;
    private double totalPerperson = 0.0;
    private DatabaseReference databaseReference;
    private DatabaseAPI databaseAPI;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;

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

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bills);

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


        splitBillsActivity = this;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseAPI = new DatabaseAPI(databaseReference, this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        totalPriceView = (EditText) findViewById(R.id.totalPrice);
        splitSeekBar = (SeekBar) findViewById(R.id.splitSeekBar);
        splitTextView = (TextView) findViewById(R.id.splitNumber);
        totalPP = (TextView) findViewById(R.id.totalPP_price);
        totalPrice = parseDoubleOrDefault(totalPriceView.getText().toString(), 0.0);
        totalPriceView.setText(String.format(getResources().getString(R.string.price),totalPrice));
        splitTextView.setText("Number of person:" + 2);
        totalPerperson = totalPrice/numofPerson;
        totalPP.setText(String.format(getResources().getString(R.string.price),totalPerperson));

        splitSeekBar.setMax(20);
        splitSeekBar.setMin(2);

        totalPriceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                calculateSplitBills(numofPerson);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateSplitBills(numofPerson);
            }
        });
        splitSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numofPerson = progress;
                calculateSplitBills(numofPerson);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("------------", "start scrolling!");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("------------", "stop scrolling!");
            }
        });
    }

    private double parseDoubleOrDefault(String str, double defaultValue) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    private void calculateSplitBills(int numofPerson){
        totalPrice = parseDoubleOrDefault(totalPriceView.getText().toString(), 0.0);
        splitTextView.setText("Number of person:" + numofPerson);
        totalPerperson = totalPrice/numofPerson;
        totalPP.setText(String.format(getResources().getString(R.string.price),totalPerperson));
    }
    public void onClick(View view) {
        int clicked_id = view.getId();
        if(clicked_id==R.id.saveSplitBills){
            // Create a CountDownLatch for waiting for the response
            CountDownLatch latch = new CountDownLatch(1);

            long time = System.currentTimeMillis();
            String title = numofPerson + " people - $" + totalPrice;
            String description = "Total person: "+ numofPerson + " people\nTotal price: " + totalPrice
                    + "\nTotal per person: " + String.format(Locale.US,"%.2f", totalPerperson) + "\nTime: " + DateFormat.getDateTimeInstance().format(time);
            // Create a sample TipHistory object
            TipHistory tipHistory = new TipHistory(latitude, longitude, title, description, time);

            // Store the TipHistory object
            databaseAPI.storeTipHistory(tipHistory).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Database_store_split_bills","saved success!");
                    Snackbar snackbar = Snackbar.make(view, "Success!", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("OK to exit", click -> {
                        snackbar.dismiss();
                        splitBillsActivity.finish();
                    });
                    snackbar.show();
                }
            });
        }
    }
}