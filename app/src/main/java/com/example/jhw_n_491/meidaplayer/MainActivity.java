package com.example.jhw_n_491.meidaplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class MainActivity extends AppCompatActivity {

    ImageView cImage;
    Button btn_play, btn_pause, btn_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUiComponents();

        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(cImage);
        Glide.with(this).load(R.drawable.backgrounds).into(gifImage);
    }

    void initUiComponents()
    {
        cImage = (ImageView)findViewById(R.id.gif_image);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_pause = (Button) findViewById(R.id.btn_stop);
    }
}
