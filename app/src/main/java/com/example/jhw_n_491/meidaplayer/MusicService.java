package com.example.jhw_n_491.meidaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;

public class MusicService extends Service{

    MediaPlayer mMediaPlayer = null;
    MusicNotification music_notification;

    boolean isPrepared;

    // Notification & Media init
    @Override
    public void onCreate()
    {
        super.onCreate();

        //mMediaPlayer = new MediaPlayer();
        music_notification = new MusicNotification(getApplicationContext());
        music_notification.ExpandedlayoutNotification();
        startForeground(music_notification.NOTIFICATION_PLAYER_ID, music_notification.mBuilder.build());

        Preparse_music();

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mp.start();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPrepared = false;;
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                return false;
            }
        });

        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Service Action
    @Override
    public int onStartCommand(Intent intent, int flag, int startId)
    {
        if(intent != null)
        {
            String action = intent.getAction();
            if("FOREGROUND_PLAY".equals(action))
            {
                if(isPlaying())
                {
                    Pause_music();
                }else
                {
                    Play_music();

                }
            }
            else if("FOREGROUND_STOP".equals(action))
            {
                Stop_music();
            }
        }

        return START_NOT_STICKY;
    }

    // Destory Service
    @Override
    public void onDestroy()
    {
        if(mMediaPlayer != null)
        {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;

            if(music_notification.mNotificationManager != null)
            {
                music_notification.mNotificationManager.cancel(0x342);
                music_notification.mNotificationManager = null;
                stopForeground(true);
            }
        }
    }

    // Playing music?
    public boolean isPlaying()
    {
        return mMediaPlayer.isPlaying();
    }

    // Play Music
    public void Play_music()
    {
        if(isPrepared)
        {
            mMediaPlayer.start();
        }
    }

    // Stop Music
    public void Stop_music()
    {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        Preparse_music();
    }

    // Pause Music
    public void Pause_music()
    {
        if(isPrepared)
        {
            mMediaPlayer.pause();
        }
    }

    //Init mMediaPlayer
    public void Preparse_music()
    {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mMediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/music.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
    }
}
