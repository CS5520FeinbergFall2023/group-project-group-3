package edu.northeastern.tipmate;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class TipCalculatorActivity extends AppCompatActivity {

    private EditText editTextBillAmount;
    private Spinner spinnerTipPercentage;
    private Switch switchRoundUp;
    private TextView textViewTipAmount;
    private TextView textViewTotalBill;

    private final int[] tipPercentages = {10, 15, 20, 25};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        // Initialize views
        editTextBillAmount = findViewById(R.id.editTextBillAmount);
        spinnerTipPercentage = findViewById(R.id.spinnerTipPercentage);
        switchRoundUp = findViewById(R.id.switch1);
        textViewTipAmount = findViewById(R.id.textViewTipAmount);
        textViewTotalBill = findViewById(R.id.textViewTotalBill);

        // Populate the Spinner with tip percentage options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getFormattedTipPercentages());
        spinnerTipPercentage.setAdapter(adapter);

        // Set up listeners
        editTextBillAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                calculateTipAndTotal();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        spinnerTipPercentage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateTipAndTotal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        switchRoundUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calculateTipAndTotal();
            }
        });
    }

    private void calculateTipAndTotal() {
        double billAmount = parseDoubleOrDefault(editTextBillAmount.getText().toString(), 0.0);
        int tipPercentage = tipPercentages[spinnerTipPercentage.getSelectedItemPosition()];
        double tipAmount = billAmount * tipPercentage / 100.0;
        double totalAmount = switchRoundUp.isChecked() ? Math.ceil(billAmount + tipAmount) : billAmount + tipAmount;

        // Format and display the calculated values
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        textViewTipAmount.setText(df.format(tipAmount));
        textViewTotalBill.setText(df.format(totalAmount));
    }

    private String[] getFormattedTipPercentages() {
        String[] formattedPercentages = new String[tipPercentages.length];
        for (int i = 0; i < tipPercentages.length; i++) {
            formattedPercentages[i] = tipPercentages[i] + "%";
        }
        return formattedPercentages;
    }

    private double parseDoubleOrDefault(String str, double defaultValue) {
        try {
            return Double.parseDouble(str.replace('$','\u0000'));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}