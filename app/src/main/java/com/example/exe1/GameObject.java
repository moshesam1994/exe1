package com.example.exe1;

import android.widget.ImageView;

public class GameObject {
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
