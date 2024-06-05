package com.example.exe1;
import android.widget.TextView;
import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
public class HighScoreActivity extends AppCompatActivity {


    private TextView highScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        highScoreTextView = findViewById(R.id.highScoreTextView);


        SharedPreferences sharedPreferences = getSharedPreferences("MyGamePrefs", MODE_PRIVATE);
        int highScore = sharedPreferences.getInt("highScore", 0);


        highScoreTextView.setText("High Score: " + highScore);
    }
}