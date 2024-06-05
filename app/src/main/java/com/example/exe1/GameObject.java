package com.example.exe1;
import android.widget.ImageView;
public class GameObject {
    public ImageView imageView;
    public int position; // 0: left, 1: center, 2: right
    public float speed;

    public GameObject(ImageView imageView, int position, float speed) {
        this.imageView = imageView;
        this.position = position;
        this.speed = speed;
    }


    public void updatePosition() {
        imageView.setY(imageView.getY() + speed);
    }





}
