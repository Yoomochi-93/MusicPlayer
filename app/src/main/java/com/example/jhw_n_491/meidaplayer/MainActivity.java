package com.example.jhw_n_491.meidaplayer;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private static final int REQUEST_EXTERNAL_STORAGE = 2;

    Intent play_intent, stop_intent;
    PendingIntent play_pending, stop_pending;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionReadStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissionReadStorage == PackageManager.PERMISSION_DENIED || permissionWriteStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_EXTERNAL_STORAGE);
            finish();
        }

        initUiComponents();

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("최신가요 MP3 파일 다운로드 중...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        // this is how you fire the downloader
        mProgressDialog.show();

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", "http://35.203.158.180/download/music.mp3");
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        startService(intent);

        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(cImage);
        Glide.with(this).load(R.drawable.backgrounds).into(gifImage);

    }
    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();
                }
            }
        }
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
