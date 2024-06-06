package com.example.exe1;

import android.widget.ImageView;

public class GameObject {
    ImageView imageView;
    int column;
    float speed;
    float yPosition;

    public GameObject(ImageView imageView, int column, float speed) {
        this.imageView = imageView;
        this.column = column;
        this.speed = speed;
        this.yPosition = 0; // התחלה בקודקוד המסך
    }

    public void updatePosition() {
        yPosition += speed;
        imageView.setY(yPosition);
    }
}
