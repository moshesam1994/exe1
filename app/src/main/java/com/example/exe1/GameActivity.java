package com.example.exe1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;


    private int previousCarRow = 8;
    private int previousCarColumn = 2;
    private Button moveLeftButton;
    private Button moveRightButton;
    private TextView livesTextView;
    private TextView scoreTextView;
    private int distance = 0;
    private TextView odometerTextView;
    private int carRow = 8; // 10 rows, index 9 for the last row
    private int carColumn = 2; // 5 columns, index 2 for the middle column
    private int lives = 3;
    private int score = 0;
    private int fastOrSlow;

    private Handler handler = new Handler();
    private Runnable gameRunnable;
    private boolean gameRunning = true;

    private ArrayList<GameObject> obstacles = new ArrayList<>();
    private ArrayList<GameObject> coins = new ArrayList<>();

    private ImageView[][] gridCells;

    private MediaPlayer hitSound;
    private MediaPlayer collectSound;

    // Sensor related variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration;
    private static final float ACC_THRESHOLD = 3.0f;
    private boolean useSensors = false; // Default to false
    private boolean isMoving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initializeGame();
        }

        // Initialize UI elements and other variables
        initializeUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("GameActivity", "Location permission granted");
                initializeGame();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This game requires location access to function properly. Please grant location permission.")
                        .setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(GameActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(GameActivity.this, "Location permission denied", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        }
    }

    private void initializeUI() {
        Log.d("GameActivity", "Initializing UI elements");

        moveLeftButton = findViewById(R.id.moveLeftButton);
        moveRightButton = findViewById(R.id.moveRightButton);
        livesTextView = findViewById(R.id.livesTextView);
       // scoreTextView = findViewById(R.id.scoreTextView);
        odometerTextView = findViewById(R.id.odometerTextView);

        // Initialize MediaPlayer for hit sound
        hitSound = MediaPlayer.create(this, R.raw.hit_sound);
        collectSound = MediaPlayer.create(this, R.raw.collect_coin_sound);

        gridCells = new ImageView[10][5]; // 10 rows, 5 columns
        LinearLayout gridLayout = findViewById(R.id.gridLayout);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        boolean isFastGame = extras.getBoolean("isFastGame");
        useSensors = extras.getBoolean("useSensors");

        if (isFastGame) {
            fastOrSlow = 600;
        } else {
            fastOrSlow = 1000;
        }

        // Hide buttons if using sensors
        if (useSensors) {
            moveLeftButton.setVisibility(View.GONE);
            moveRightButton.setVisibility(View.GONE);
        }

        // Setup sensor manager and accelerometer if useSensors is true
        if (useSensors) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            } else {
                Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
                useSensors = false; // Disable sensor usage
            }
        }

        for (int row = 0; row < 10; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1));
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 5; col++) {
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
                if (!isMoving && carColumn > 0) { // Check if not moving and within bounds
                    moveCarLeft();
                }
            }
        });

        moveRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMoving && carColumn < 4) { // Check if not moving and within bounds
                    moveCarRight();
                }
            }
        });
    }
    private void initializeGame() {
        Log.d("GameActivity", "Initializing game");

        getLastKnownLocation();
        startGame();
    }
    private void getLastKnownLocation() {
        if (fusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();
                                Log.d("GameActivity", "Location: " + currentLatitude + ", " + currentLongitude);
                            } else {
                                Log.d("GameActivity", "Location is null");
                            }
                        }
                    });
        } else {
            Log.e("GameActivity", "FusedLocationClient is null");
        }
    }

    private void startGame() {
        Log.d("GameActivity", "Starting game");

        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    playGame();
                    handler.postDelayed(this, fastOrSlow);
                    updateDistance();
                }
            }
        };
        handler.post(gameRunnable);
    }



    private void moveCarLeft() {
        isMoving = true;
        previousCarColumn = carColumn;
        carColumn--;
        updateCarPosition();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isMoving = false;
            }
        }, 0); // השתמש בעיכוב המתאים
    }

    private void moveCarRight() {
        isMoving = true;
        previousCarColumn = carColumn;
        carColumn++;
        updateCarPosition();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isMoving = false;
            }
        }, 0);
    }

    private void updateCarPosition() {

        gridCells[previousCarRow][previousCarColumn].setImageResource(0);

        gridCells[carRow][carColumn].setImageResource(R.drawable.car);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float xAcceleration = event.values[0];
            if (Math.abs(xAcceleration) > ACC_THRESHOLD) {
                if (xAcceleration > 0 && carColumn > 0) { // Move car left
                    carColumn--;
                    updateCarPosition();
                } else if (xAcceleration < 0 && carColumn < 4) { // Move car right
                    carColumn++;
                    updateCarPosition();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void updateDistance() {
        distance++;
        odometerTextView.setText("Distance: " + distance);
    }




    private void playGame() {
        clearGrid();
        createObstaclesAndCoins();
        updateGameObjects();
        updateCarPosition();
        checkCollisions();
        updateLives();
        //updateScore();
    }

    private void createObstaclesAndCoins() {
        Random random = new Random();

        if (obstacles.size() + coins.size() < 5) {
            if (random.nextInt(100) < 20) {
                int column = random.nextInt(5); // 5 columns
                float speed = 1;
                GameObject obstacle = new GameObject(R.drawable.obstacle, column, speed);
                obstacles.add(obstacle);
            }

            if (random.nextInt(100) < 10) {
                int column = random.nextInt(5); // 5 columns
                float speed = 1;
                GameObject coin = new GameObject(R.drawable.coin, column, speed);
                coins.add(coin);
            }
        }
    }

    private void updateGameObjects() {
        for (GameObject obstacle : obstacles) {
            obstacle.updatePosition();
            if (obstacle.row < 10) { // 10 rows
                gridCells[obstacle.row][obstacle.column].setImageResource(obstacle.imageResId);
            }
        }
        for (GameObject coin : coins) {
            coin.updatePosition();
            if (coin.row < 10) { // 10 rows
                gridCells[coin.row][coin.column].setImageResource(coin.imageResId);
            }
        }
    }

    private void checkCollisions() {
        ArrayList<GameObject> objectsToRemove = new ArrayList<>();

        for (GameObject obstacle : obstacles) {
            if (obstacle.row == carRow && obstacle.column == carColumn) {
                lives--;
                if (hitSound != null) {
                    hitSound.start(); // Play hit sound
                }
                Toast.makeText(this, "You lost a life!", Toast.LENGTH_SHORT).show();
                objectsToRemove.add(obstacle);
            } else if (obstacle.row >= 10) { // 10 rows
                objectsToRemove.add(obstacle);
            }
        }

        for (GameObject coin : coins) {
            if (coin.row == carRow && coin.column == carColumn) {
                score++;
                distance += 10;
                if (collectSound != null) {
                    collectSound.start(); // Play hit sound
                }
                Toast.makeText(this, "Distance Has Increased", Toast.LENGTH_SHORT).show();
                objectsToRemove.add(coin);
            } else if (coin.row >= 10) { // 10 rows
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
            saveTopDistance(distance); // Save the distance when the game ends
            goToStartScreen();
        }
    }




    private void saveTopDistance(int newDistance) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyGamePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        List<Integer> distances = new ArrayList<>();
        List<String> locations = new ArrayList<>();

        // Retrieve existing distances and locations
        for (int i = 0; i < 10; i++) {
            int distance = sharedPreferences.getInt("distance_" + i, 0);
            String location = sharedPreferences.getString("location_" + i, "0.0,0.0");
            distances.add(distance);
            locations.add(location);
        }

        // Add new distance and location
        distances.add(newDistance);
        locations.add(currentLatitude + "," + currentLongitude);
        Log.d("saveTopDistance", "New location: " + currentLatitude + "," + currentLongitude);

        // Sort distances and corresponding locations in descending order
        for (int i = 0; i < distances.size() - 1; i++) {
            for (int j = i + 1; j < distances.size(); j++) {
                if (distances.get(i) < distances.get(j)) {
                    // Swap distances
                    int tempDistance = distances.get(i);
                    distances.set(i, distances.get(j));
                    distances.set(j, tempDistance);

                    // Swap locations
                    String tempLocation = locations.get(i);
                    locations.set(i, locations.get(j));
                    locations.set(j, tempLocation);
                }
            }
        }

        // Save top 10 distances and locations back to SharedPreferences
        for (int i = 0; i < 10 && i < distances.size(); i++) {
            editor.putInt("distance_" + i, distances.get(i));
            editor.putString("location_" + i, locations.get(i));
            Log.d("saveTopDistance", "Distance: " + distances.get(i) + " Location: " + locations.get(i));
        }

        // Commit the changes to SharedPreferences
        editor.apply();
    }


    private void goToStartScreen() {
        cleanupGame();
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    // New method to handle fragment transactions
    public void loadScoreLocationsFragment(String location) {
        ScoreLocationsFragment fragment = new ScoreLocationsFragment();
        Bundle args = new Bundle();
        args.putString("location", location);
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapView, fragment)
                .addToBackStack(null)
                .commit();
    }



    private void cleanupGame() {
        gameRunning = false;
        handler.removeCallbacks(gameRunnable);
        clearGrid();
        obstacles.clear();
        coins.clear();
        // Unregister sensor listener if registered
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void clearGrid() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {
                gridCells[row][col].setImageResource(0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hitSound != null) {
            hitSound.release();
            hitSound = null;
        }
        if (collectSound != null) {
            collectSound.release();
            collectSound = null;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        gameRunning = false;  // Stop the game loop
        handler.removeCallbacks(gameRunnable);  // Remove any pending gameRunnable callbacks
        if (useSensors && accelerometer != null) {
            sensorManager.unregisterListener(this);  // Unregister sensor listener if registered
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (useSensors && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (!gameRunning) {
            gameRunning = true;
            handler.post(gameRunnable);  // Restart the game loop
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (useSensors && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (!gameRunning) {
            gameRunning = true;
            handler.post(gameRunnable);  // Restart the game loop
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameRunning = false;
        if (useSensors && accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }


}


