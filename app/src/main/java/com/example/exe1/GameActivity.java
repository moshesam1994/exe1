package com.example.exe1;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Random;
import android.content.SharedPreferences;
public class GameActivity extends AppCompatActivity {

    private ImageView car;
    private Button moveLeftButton;
    private Button moveRightButton;
    private TextView livesTextView;
    private TextView scoreTextView;

    private int carPosition = 1; // 0: left, 1: center, 2: right
    private int lives = 3;
    private int score = 0;

    private Handler handler = new Handler();
    private Runnable gameRunnable;
    private boolean gameRunning = true;

    private ArrayList<GameObject> obstacles = new ArrayList<>();
    private ArrayList<GameObject> coins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        moveLeftButton = findViewById(R.id.moveLeftButton);
        moveRightButton = findViewById(R.id.moveRightButton);
        livesTextView = findViewById(R.id.livesTextView);
        scoreTextView = findViewById(R.id.scoreTextView);

        car = new ImageView(this);
        car.setImageResource(R.drawable.car);
        RelativeLayout.LayoutParams carParams = new RelativeLayout.LayoutParams(150, 300);
        car.setLayoutParams(carParams);

        RelativeLayout layout = findViewById(R.id.gameLayout);
        layout.addView(car);

        car.post(new Runnable() {
            @Override
            public void run() {
                updateCarPosition();
            }
        });

        moveLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carPosition > 0) {
                    carPosition--;
                    updateCarPosition();
                }
            }
        });

        moveRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carPosition < 2) {
                    carPosition++;
                    updateCarPosition();
                }
            }
        });

        startGame();
    }

    private void updateCarPosition() {
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float carWidth = car.getWidth();
        float positionX = (carPosition * (screenWidth / 3)) + ((screenWidth / 3 - carWidth) / 2);
        car.setX(positionX);
        car.setY(getResources().getDisplayMetrics().heightPixels - car.getHeight() - 50);
    }

    private void startGame() {
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    playGame();
                    handler.postDelayed(this, 30);
                }
            }
        };
        handler.post(gameRunnable);
    }

    private void playGame() {
        createObstaclesAndCoins();
        updateGameObjects();
        checkCollisions();
        updateLives();
        updateScore();
    }

    private void createObstaclesAndCoins() {
        Random random = new Random();

        if (obstacles.size() + coins.size() < 5) {
            if (random.nextInt(100) < 5) {
                runOnUiThread(() -> {
                    ImageView obstacleImageView = new ImageView(this);
                    obstacleImageView.setImageResource(R.drawable.obstacle);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
                    obstacleImageView.setLayoutParams(params);

                    RelativeLayout layout = findViewById(R.id.gameLayout);
                    layout.addView(obstacleImageView);

                    int position = random.nextInt(3);
                    float speed = 5 + random.nextInt(5);
                    GameObject obstacle = new GameObject(obstacleImageView, position, speed);
                    obstacles.add(obstacle);
                    setPosition(obstacle);
                });
            }

            if (random.nextInt(100) < 3) {
                runOnUiThread(() -> {
                    ImageView coinImageView = new ImageView(this);
                    coinImageView.setImageResource(R.drawable.coin);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
                    coinImageView.setLayoutParams(params);

                    RelativeLayout layout = findViewById(R.id.gameLayout);
                    layout.addView(coinImageView);

                    int position = random.nextInt(3);
                    float speed = 5 + random.nextInt(5);
                    GameObject coin = new GameObject(coinImageView, position, speed);
                    coins.add(coin);
                    setPosition(coin);
                });
            }
        }
    }

    private void setPosition(GameObject gameObject) {
        runOnUiThread(() -> {
            float screenWidth = getResources().getDisplayMetrics().widthPixels;
            float gameObjectWidth = gameObject.imageView.getWidth();
            float positionX = (gameObject.position * (screenWidth / 3)) + ((screenWidth / 3 - gameObjectWidth) / 2);
            gameObject.imageView.setX(positionX);
            gameObject.imageView.setY(0);

            Log.d("GameActivity", "Object positioned at X: " + positionX + " Y: " + 0);
        });
    }

    private void updateGameObjects() {
        for (GameObject obstacle : obstacles) {
            obstacle.updatePosition();
        }
        for (GameObject coin : coins) {
            coin.updatePosition();
        }
    }

    private void checkCollisions() {
        float carY = car.getY();
        float carHeight = car.getHeight();
        float carWidth = car.getWidth();

        ArrayList<GameObject> objectsToRemove = new ArrayList<>();

        for (GameObject obstacle : obstacles) {
            if (obstacle.position == carPosition && obstacle.imageView.getY() + obstacle.imageView.getHeight() > carY && obstacle.imageView.getY() < carY + carHeight) {
                lives--;
                Toast.makeText(this, "You lost a life!", Toast.LENGTH_SHORT).show();
                objectsToRemove.add(obstacle);
            } else if (obstacle.imageView.getY() > getResources().getDisplayMetrics().heightPixels) {
                objectsToRemove.add(obstacle);
            }
        }

        for (GameObject coin : coins) {
            if (coin.position == carPosition && coin.imageView.getY() + coin.imageView.getHeight() > carY && coin.imageView.getY() < carY + carHeight) {
                score++;
                objectsToRemove.add(coin);
            } else if (coin.imageView.getY() > getResources().getDisplayMetrics().heightPixels) {
                objectsToRemove.add(coin);
            }
        }

        for (GameObject objectToRemove : objectsToRemove) {
            RelativeLayout layout = findViewById(R.id.gameLayout);
            layout.removeView(objectToRemove.imageView);
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

        for (GameObject obstacle : obstacles) {
            RelativeLayout layout = findViewById(R.id.gameLayout);
            layout.removeView(obstacle.imageView);
        }

        for (GameObject coin : coins) {
            RelativeLayout layout = findViewById(R.id.gameLayout);
            layout.removeView(coin.imageView);
        }

        obstacles.clear();
        coins.clear();
    }



}
