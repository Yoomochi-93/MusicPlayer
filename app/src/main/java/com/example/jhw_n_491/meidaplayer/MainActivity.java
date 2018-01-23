package com.example.jhw_n_491.meidaplayer;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class MainActivity extends AppCompatActivity {

    ImageView cImage;
    Button btn_play, btn_stop;
    SeekBar seekbar_playtime;

    final String PLAY_BUTTON = "FOREGROUND_PLAY";
    final String STOP_BUTTON = "FOREGROUND_STOP";

    Intent play_intent, stop_intent;
    PendingIntent play_pending, stop_pending;

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
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        cImage = (ImageView)findViewById(R.id.gif_image);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_play.setOnClickListener(onClickListener);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(onClickListener);
        seekbar_playtime = (SeekBar) findViewById(R.id.seekbar_paytime);

        play_intent = new Intent(PLAY_BUTTON);
        stop_intent = new Intent(STOP_BUTTON);

        play_pending = PendingIntent.getService(getApplicationContext(), 0, play_intent, 0);
        stop_pending = PendingIntent.getService(getApplicationContext(), 0 , stop_intent, 0);

    }


    class BtnOnClickListener implements Button.OnClickListener{

        @Override
        public void onClick(View v) {
            switch(v.getId())
            {
                case R.id.btn_play:
                    try {
                        play_pending.send(getApplicationContext(), 1, play_intent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.btn_stop:
                    try {
                        stop_pending.send(getApplicationContext(),2,stop_intent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
