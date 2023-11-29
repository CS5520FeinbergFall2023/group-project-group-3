package edu.northeastern.tipmate;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private double totalPrice = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button history_btn = findViewById(R.id.history_button);
        history_btn.setOnClickListener(this);

        Button tipCalculatorBtn = findViewById(R.id.tip_calculator_button);
        tipCalculatorBtn.setOnClickListener(this);
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
            intent.putExtra("totalPrice",totalPrice);
            startActivity(intent);
        }
        if (clicked_id == R.id.tip_calculator_button) {
            Intent tipCalculatorIntent = new Intent(this, TipCalculatorActivity.class);
            startActivity(tipCalculatorIntent);
        }
    }
}