package com.example.exe1;
import android.widget.TextView;
import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.widget.Toast;

public class HighScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        TopScoresFragment topScoresFragment = new TopScoresFragment();
        ScoreLocationsFragment scoreLocationsFragment = new ScoreLocationsFragment();

        fragmentTransaction.add(R.id.topScoresContainer, topScoresFragment);
        fragmentTransaction.add(R.id.scoreLocationsContainer, scoreLocationsFragment);

        fragmentTransaction.commit();
    }

}