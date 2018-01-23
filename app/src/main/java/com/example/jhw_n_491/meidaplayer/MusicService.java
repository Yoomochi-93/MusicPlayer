package com.example.jhw_n_491.meidaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;

public class MusicService extends Service{

    MediaPlayer mMediaPlayer = null;
    boolean isPrepared;

    MusicNotification music_notification;


    @Override
    public void onCreate()
    {
        super.onCreate();

        //mMediaPlayer = new MediaPlayer();
        music_notification = new MusicNotification(getApplicationContext());
        music_notification.ExpandedlayoutNotification();
        startForeground(music_notification.NOTIFICATION_PLAYER_ID, music_notification.mBuilder.build());

        mMediaPlayer = MediaPlayer.create(this, R.raw.rain);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

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
                try {
                    mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://com.example.jhw_n_491.meidaplayer/"+R.raw.rain));
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            }
        }
    }

    public boolean isPlaying()
    {
        return mMediaPlayer.isPlaying();
    }

    public void Play_music()
    {
        if(isPrepared)
        {
            mMediaPlayer.start();
        }
    }

    public void Stop_music()
    {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    public void Pause_music()
    {
        if(isPrepared)
        {
            mMediaPlayer.pause();
        }
    }
}
