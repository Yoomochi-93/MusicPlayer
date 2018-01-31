package com.example.jhw_n_491.meidaplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import java.io.IOException;

public class MusicService extends Service{

    // mediaplayer, notification
    MediaPlayer mMediaPlayer = null;
    MusicNotification music_notification;

    // music prepard check
    boolean isPrepared;
    boolean mp3Check = true;

    // Service Message\
    public static final int MSG_REGISTER_CLIENT = 10;
    public static final int MSG_SEND_TO_SERVICE = 11;
    public static final int MSG_SEND_TO_ACTIVITY = 12;
    public static final int MSG_SEND_SEEKBAR = 13;
    public static final int MSG_SEND_STOPSERVICE = 14;

    // Activity 에서 가져온 Messenger
    private Messenger mClient = null;
    private int sbposition = 0;
    private boolean mp3check = true;
    public static boolean playcheck = false;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mMediaPlayer = new MediaPlayer();

        music_notification = new MusicNotification(getApplicationContext());

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                sendMsgToActivity((mp.getDuration()/1000));
            }
        });

        // 음악이 끝나고 다시 시작하기 위해 재설정
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mp.stop();
                    mp.reset();
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                    mp.setDataSource(Environment.getExternalStorageDirectory().toString() + "/music.mp3");
                    mp.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        //throw new UnsupportedOperationException("Not yet implemented");
        return mMessenger.getBinder();
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
            else if("FOREGROUND_FORWARD".equals(action))
            {
                if(intent.getStringExtra("forward").contains("FORWARD_DOWN"))
                {
                    if(isPlaying())
                    {
                        Pause_music();
                    }
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 1000);
                }
                else if(intent.getStringExtra("forward").contains("FORWARD_UP"))
                {
                    Play_music();
                }
            }
            else if("FOREGROUND_BACKWARD".equals(action))
            {
                if(intent.getStringExtra("backward").contains("BACKWARD_DOWN"))
                {
                    if(isPlaying())
                    {
                        Pause_music();
                    }
                    mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 1000);
                }
                else if(intent.getStringExtra("backward").contains("BACKWARD_UP"))
                {
                    Play_music();
                }
            }
            else if("SEEKBAR_DOWN".equals(action))
            {
                if(intent.getStringExtra("seekbar").contains("SEEKBAR_DOWN"))
                {
                    if(isPlaying())
                    {
                        Pause_music();
                    }
                }

            }
            else if("SEEKBAR_UP".equals(action))
            {
                if(intent.getStringExtra("seekbar").contains("SEEKBAR_UP"))
                {
                    sbposition = intent.getIntExtra("sbposition",0);
                    mMediaPlayer.seekTo((sbposition*1000));
                    Play_music();
                }
            }
            else if("MP3CHECK".equals(action))
            {
                if(intent.getStringExtra("mp3Check").contains("exists"))
                {
                    if(mp3check == true)
                    {
                        Preparse_music();
                        mp3check = false;
                    }

                }
            }
        }

        return START_NOT_STICKY;
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
            sendMsgToSEEKBAR("PLAY");
            playcheck = true;

            music_notification.ExpandedlayoutNotification();
            startForeground(music_notification.NOTIFICATION_PLAYER_ID, music_notification.mBuilder.build());
        }
    }

    // Stop Music
    public void Stop_music()
    {

        playcheck = false;
        if(mMediaPlayer != null)
        {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            Preparse_music();
            sendMsgToSEEKBAR("STOP");
            stopService((new Intent(getApplicationContext(), MainActivity.class)));
            stopService(new Intent(getApplicationContext(), MusicService.class));
            stopService(new Intent(getApplicationContext(), MusicNotification.class));
            sendStopServicemsg("STOP");

            if(music_notification.mNotificationManager != null)
            {
                music_notification.mNotificationManager.cancel(music_notification.NOTIFICATION_PLAYER_ID);
                music_notification.mNotificationManager = null;
                stopForeground(true);
            }
        }
    }

    // Pause Music
    public void Pause_music()
    {
        if(isPrepared)
        {
            mMediaPlayer.pause();
            sendMsgToSEEKBAR("PLAY");
            playcheck = false;
        }
    }

    //Init mMediaPlayer
    public void Preparse_music()
    {
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mMediaPlayer.setDataSource(Environment.getExternalStorageDirectory().toString() + "/music.mp3");
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Binder Setting
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    break;
                default:
                    break;
            }
            return false;
        }
    }));

    // Activity로 메시지 보내기
    private void sendMsgToActivity(int sendValue)
    {
        try{
            Bundle bundle = new Bundle();
            bundle.putInt("fromService", sendValue);
            Message msg = Message.obtain(null, MSG_SEND_TO_ACTIVITY);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }

    private void sendStopServicemsg(String stopmsg)
    {
        try{
            Bundle bundle = new Bundle();
            bundle.putString("ServiceStop", stopmsg);
            Message msg = Message.obtain(null, MSG_SEND_STOPSERVICE);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }

    private void sendMsgToSEEKBAR(String seekbar_status)
    {
        try{
            Bundle bundle = new Bundle();
            bundle.putString("fromSeekbar", seekbar_status);
            Message msg = Message.obtain(null, MSG_SEND_SEEKBAR);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }
}