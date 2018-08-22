package com.thecoffeecoders.chatex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView fullScreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        fullScreenImage = (ImageView) findViewById(R.id.image_full_screen);
        String imageURL = getIntent().getStringExtra("image");

        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.loading_icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL) //use this to cache
                .crossFade()
                .into(fullScreenImage);
    }
}
