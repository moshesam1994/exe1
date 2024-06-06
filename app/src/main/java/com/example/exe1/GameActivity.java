package com.example.exe1;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Random;
import android.content.SharedPreferences;

public class GameActivity extends AppCompatActivity {

    private Button moveLeftButton;
    private Button moveRightButton;
    private TextView livesTextView;
    private TextView scoreTextView;

    private int carRow = 5;
    private int carColumn = 1;
    private int lives = 3;
    private int score = 0;

    private Handler handler = new Handler();
    private Runnable gameRunnable;
    private boolean gameRunning = true;

    private ArrayList<GameObject> obstacles = new ArrayList<>();
    private ArrayList<GameObject> coins = new ArrayList<>();

    private ImageView[][] gridCells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        moveLeftButton = findViewById(R.id.moveLeftButton);
        moveRightButton = findViewById(R.id.moveRightButton);
        livesTextView = findViewById(R.id.livesTextView);
        scoreTextView = findViewById(R.id.scoreTextView);

        // Initialize gridCells
        gridCells = new ImageView[6][3];
        LinearLayout gridLayout = findViewById(R.id.gridLayout);

        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1));
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 3; col++) {
                ImageView cell = new ImageView(this);
                cell.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                rowLayout.addView(cell);
                gridCells[row][col] = cell;
            }
            gridLayout.addView(rowLayout);
        }

        updateCarPosition();

        moveLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carColumn > 0) {
                    carColumn--;
                    updateCarPosition();
                }
            }
        });

        moveRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carColumn < 2) {
                    carColumn++;
                    updateCarPosition();
                }
            }
        });

        startGame();
    }

    private void updateCarPosition() {
        gridCells[carRow][carColumn].setImageResource(R.drawable.car);
    }

    private void startGame() {
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    playGame();
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(gameRunnable);
    }

    private void playGame() {
        clearGrid();
        createObstaclesAndCoins();
        updateGameObjects();
        updateCarPosition();
        checkCollisions();
        updateLives();
        updateScore();
    }

    private void createObstaclesAndCoins() {
        Random random = new Random();

        if (obstacles.size() + coins.size() < 5) {
            if (random.nextInt(100) < 20) {
                int column = random.nextInt(3);
                float speed = 1;
                GameObject obstacle = new GameObject(R.drawable.obstacle, column, speed);
                obstacles.add(obstacle);
            }

            if (random.nextInt(100) < 10) {
                int column = random.nextInt(3);
                float speed = 1;
                GameObject coin = new GameObject(R.drawable.coin, column, speed);
                coins.add(coin);
            }
        }
    }

    private void updateGameObjects() {
        for (GameObject obstacle : obstacles) {
            obstacle.updatePosition();
            if (obstacle.row < 6) {
                gridCells[obstacle.row][obstacle.column].setImageResource(obstacle.imageResId);
            }
        }
        for (GameObject coin : coins) {
            coin.updatePosition();
            if (coin.row < 6) {
                gridCells[coin.row][coin.column].setImageResource(coin.imageResId);
            }
        }
    }

    private void checkCollisions() {
        ArrayList<GameObject> objectsToRemove = new ArrayList<>();

        for (GameObject obstacle : obstacles) {
            if (obstacle.row == carRow && obstacle.column == carColumn) {
                lives--;
                Toast.makeText(this, "You lost a life!", Toast.LENGTH_SHORT).show();
                objectsToRemove.add(obstacle);
            } else if (obstacle.row >= 6) {
                objectsToRemove.add(obstacle);
            }
        }

        for (GameObject coin : coins) {
            if (coin.row == carRow && coin.column == carColumn) {
                score++;
                objectsToRemove.add(coin);
            } else if (coin.row >= 6) {
                objectsToRemove.add(coin);
            }
        }

        for (GameObject objectToRemove : objectsToRemove) {
            obstacles.remove(objectToRemove);
            coins.remove(objectToRemove);
        }
    }

    private void updateLives() {
        livesTextView.setText("Lives: " + lives);

        if (lives <= 0) {
            goToStartScreen();
        }
    }

    private void goToStartScreen() {
        cleanupGame();
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateScore() {
        scoreTextView.setText("Score: " + score);

        SharedPreferences sharedPreferences = getSharedPreferences("MyGamePrefs", MODE_PRIVATE);
        int highScore = sharedPreferences.getInt("highScore", 0);

        if (score > highScore) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("highScore", score);
            editor.apply();
        }
    }

    private void cleanupGame() {
        gameRunning = false;
        handler.removeCallbacks(gameRunnable);
        clearGrid();
        obstacles.clear();
        coins.clear();
    }

    private void clearGrid() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 3; col++) {
                gridCells[row][col].setImageResource(0);
            }
        }
    }

    class GameObject {
        int imageResId;
        int column;
        float speed;
        int row;

        GameObject(int imageResId, int column, float speed) {
            this.imageResId = imageResId;
            this.column = column;
            this.speed = speed;
            this.row = 0;
        }

        void updatePosition() {
            row += speed;
        }
    }
}
