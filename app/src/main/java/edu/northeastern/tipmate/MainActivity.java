package edu.northeastern.tipmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button history_btn = findViewById(R.id.history_button);
        history_btn.setOnClickListener(this);

        Button tipCalculatorBtn = findViewById(R.id.tip_calculator_button);
        tipCalculatorBtn.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public void onClick(View view) {
        int clicked_id = view.getId();
        if(clicked_id==R.id.history_button){
            Intent about_me_intent = new Intent(this,HistoryMapActivity.class);
            startActivity(about_me_intent);
        }
        if(clicked_id==R.id.splitBills_button){
            Intent intent = new Intent(this,SplitBillsActivity.class);
            startActivity(intent);
        }
        if (clicked_id == R.id.tip_calculator_button) {
            Intent tipCalculatorIntent = new Intent(this, TipCalculatorActivity.class);
            startActivity(tipCalculatorIntent);
        }
    }
}