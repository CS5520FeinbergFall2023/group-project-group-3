package edu.northeastern.tipmate;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.List;

public class DatabaseAPI {
    private final DatabaseReference mDatabase;

    public DatabaseAPI(DatabaseReference mDatabase) {
        this.mDatabase = mDatabase;
    }

    public Task<Void> storeTipHistory(TipHistory tipHistory) {
        String tipHistoryId = tipHistory.getId();
        if(tipHistoryId==null){
            // Generate a unique key for the new tip history entry
            tipHistoryId = mDatabase.child("tipHistory").push().getKey();
            assert tipHistoryId!=null;
            tipHistory.setId(tipHistoryId);
        }
        return mDatabase.child("tipHistory").child(tipHistoryId).setValue(tipHistory);
    }

    // example for how to use get method is in test code
    public DatabaseReference getTipHistory() {
        // Return a reference to the 'tipHistory' node in the database
        return mDatabase.child("tipHistory");
    }

    public DatabaseReference getTipHistoryById(String id){
        return getTipHistory().child(id);
    }
}

