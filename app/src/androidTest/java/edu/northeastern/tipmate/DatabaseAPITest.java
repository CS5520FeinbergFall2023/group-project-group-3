package edu.northeastern.tipmate;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import edu.northeastern.tipmate.DatabaseAPI;

public class DatabaseAPITest {

    private DatabaseReference databaseReference;
    private DatabaseAPI databaseAPI;

    @Before
    public void setup() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseAPI = new DatabaseAPI(databaseReference);
    }

    @Test
    public void testStoreTipHistory() throws InterruptedException {
        // Create a CountDownLatch for waiting for the response
        CountDownLatch latch = new CountDownLatch(1);

        // Create a sample TipHistory object
        TipHistory sampleTipHistory = new TipHistory(40.7128, -74.0060, "Test Title", "Test Description", 1704067200);

        // Store the TipHistory object
        databaseAPI.storeTipHistory(sampleTipHistory).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Check if data is stored correctly
                DatabaseReference ref = databaseReference.child("tipHistory");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean found = false;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            // Access individual fields of TipHistory
                            double latitude = child.child("latLng").child("latitude").getValue(Double.class);
                            double longitude = child.child("latLng").child("longitude").getValue(Double.class);
                            String title = child.child("title").getValue(String.class);
                            String desc = child.child("desc").getValue(String.class);
                            Long timestamp = child.child("timestamp").getValue(Long.class);
                            // Check if the retrieved data matches the expected values
                            if (title != null && title.equals("Test Title") &&
                                    desc != null && desc.equals("Test Description") &&
                                    timestamp != null && timestamp == 1704067200) {
                                found = true;
                                break;
                            }
                        }
                        assertTrue("TipHistory object not found in database", found);
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("testStoreTipHistory", "loadPost:onCancelled", databaseError.toException());
                        fail("Database error: " + databaseError.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fail("Failed to store TipHistory: " + e.getMessage());
            }
        });

        // Wait for the response
        latch.await();
    }

}
