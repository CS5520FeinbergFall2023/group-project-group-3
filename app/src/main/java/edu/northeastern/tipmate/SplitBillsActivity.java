package edu.northeastern.tipmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class SplitBillsActivity extends AppCompatActivity {
    private SeekBar splitSeekBar;
    private TextView totalPriceView;
    private TextView splitTextView;
    private TextView totalPP;
    private double totalPrice = 0.0;
    private double totalPerperson = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_bills);

        totalPriceView = (TextView) findViewById(R.id.totalPrice);
        splitSeekBar = (SeekBar) findViewById(R.id.splitSeekBar);
        splitTextView = (TextView) findViewById(R.id.splitNumber);
        totalPP = (TextView) findViewById(R.id.totalPP_price);
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("totalPrice",100.0);
        totalPriceView.setText(String.format(getResources().getString(R.string.price),totalPrice));
        splitTextView.setText("Number of person:" + 2);
        totalPP.setText(String.format(getResources().getString(R.string.price),totalPrice/2));

        splitSeekBar.setMax(20);
        splitSeekBar.setMin(2);
        splitSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                totalPerperson = totalPrice/progress;
                splitTextView.setText("Number of person:" + Integer.toString(progress));
                totalPP.setText(String.format(getResources().getString(R.string.price),totalPerperson));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e("------------", "start scrolling!");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e("------------", "stop scrolling!");
            }
        });
    }
}