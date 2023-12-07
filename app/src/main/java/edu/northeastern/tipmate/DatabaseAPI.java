package edu.northeastern.tipmate;

import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

public class DatabaseAPI {
    private final DatabaseReference mDatabase;
    private final String deviceId;

    public DatabaseAPI(DatabaseReference mDatabase, Context context) {
        this.mDatabase = mDatabase;
        this.deviceId = getDeviceId(context);
    }

    private String getDeviceId(Context context) {
        // Retrieve the device ID from the Android system
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public Task<Void> storeTipHistory(TipHistory tipHistory) {
        String tipHistoryId = tipHistory.getId();
        if (tipHistoryId == null) {
            // Generate a unique key for the new tip history entry
            tipHistoryId = mDatabase.child("tipHistory").child(deviceId).push().getKey();
            assert tipHistoryId != null;
            tipHistory.setId(tipHistoryId);
        }
        // Store the tip history with the device ID in the path
        return mDatabase.child("tipHistory").child(deviceId).child(tipHistoryId).setValue(tipHistory);
    }

    public DatabaseReference getTipHistory() {
        // Return a reference to the 'tipHistory' node for this device in the database
        return mDatabase.child("tipHistory").child(deviceId);
    }

    public DatabaseReference getTipHistoryById(String id) {
        // Retrieve a specific tip history entry for this device
        return getTipHistory().child(id);
    }
}
