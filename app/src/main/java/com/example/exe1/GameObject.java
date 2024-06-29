package com.example.exe1;

import android.widget.ImageView;

public class GameObject {
    public int imageResId;
    public int column;
    public int row;
    public float speed;

    public GameObject(int imageResId, int column, float speed) {
        this.imageResId = imageResId;
        this.column = column;
        this.row = 0; // Start at the top
        this.speed = speed;
    }

    public void updatePosition() {
        row += speed; // Move down the grid
    }
}
