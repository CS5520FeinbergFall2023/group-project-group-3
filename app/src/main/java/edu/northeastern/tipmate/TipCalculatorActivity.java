package edu.northeastern.tipmate;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class TipCalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        Spinner spinnerTipPercentage = findViewById(R.id.spinnerTipPercentage);

        // Populate the Spinner with tip percentage options
        String[] tipPercentages = {"10%", "15%", "20%", "25%", "Custom"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipPercentages);
        spinnerTipPercentage.setAdapter(adapter);
    }
}