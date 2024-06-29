package com.example.exe1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private boolean isFastGame = false;
    private boolean useSensors = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button buttonModeSlow = findViewById(R.id.buttonModeSlow);
        Button buttonModeFast = findViewById(R.id.buttonModeFast);
        Spinner sensorModeSpinner = findViewById(R.id.sensorModeSpinner);

        buttonModeSlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFastGame = false;
                startGame();
            }
        });

        buttonModeFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFastGame = true;
                startGame();
            }
        });

        sensorModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                useSensors = position == 1; // Assuming the first item in spinner is "Use Sensors"
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void startGame() {
        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
        intent.putExtra("isFastGame", isFastGame);
        intent.putExtra("useSensors", useSensors);
        startActivity(intent);
    }
}
