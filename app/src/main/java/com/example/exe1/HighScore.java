package com.example.exe1;

public class HighScore {

    private String playerName;
    private int score;
    private double latitude;
    private double longitude;

    public HighScore(String playerName, int score, double latitude, double longitude) {
        this.playerName = playerName;
        this.score = score;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return playerName + " - " + score;
    }
}
