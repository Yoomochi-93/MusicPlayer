package com.example.jhw_n_491.meidaplayer;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class MainActivity extends AppCompatActivity {


    // Compents
    ImageView cImage;
    Button btn_play, btn_stop, btn_forward, btn_backward;
    SeekBar seekbar_playtime;

    // Status Action
    final String PLAY_BUTTON = "FOREGROUND_PLAY";
    final String STOP_BUTTON = "FOREGROUND_STOP";
    final String FORWARD_BUTTON = "FOREGROUND_FORWARD";
    final String BACKWARD_BUTTON = "FOREGROUND_BACKWARD";
    final String SEEKBAR_DOWN = "SEEKBAR_DOWN";
    final String SEEKBAR_UP = "SEEKBAR_UP";

    // Service Object
    Intent play_intent, stop_intent, forward_intent, backward_intent, sbup_intent, sbdown_intent;
    PendingIntent play_pending, stop_pending, forward_pending, backward_pending, sbup_pending, sbdown_pending;

    // messenger
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;

    // Seekbar Time
    int seekbar_time = 0;
    int sync_time = 0;
    String seekbar_status;


    // Thread & Bind flag
    private boolean thread_flag = false;
    private boolean isBound = false;

    // forward, backward flag
    public static boolean wardflag = false;
    public static String wardString = "";
    Thread sampleThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUiComponents();

        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(cImage);
        Glide.with(this).load(R.drawable.backgrounds).into(gifImage);

        setStartService();
        runThread();
    }

    void initUiComponents()
    {
        // Components init
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        BtnTouchEvent onTuchListener = new BtnTouchEvent();
        SeekBarClickListener onChangeListener = new SeekBarClickListener();

        sampleThread = new wardThread();
        sampleThread.start();

        cImage = (ImageView)findViewById(R.id.gif_image);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_play.setOnClickListener(onClickListener);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(onClickListener);

        // Touch Listener
        btn_forward = (Button) findViewById(R.id.btn_forward);
        btn_forward.setOnTouchListener(onTuchListener);
        btn_backward = (Button) findViewById(R.id.btn_backward);
        btn_backward.setOnTouchListener(onTuchListener);

        seekbar_playtime = (SeekBar) findViewById(R.id.seekbar_paytime);
        seekbar_playtime.setMax(0);
        seekbar_playtime.setOnTouchListener(onChangeListener);

        // Button intent init
        play_intent = new Intent(PLAY_BUTTON);
        stop_intent = new Intent(STOP_BUTTON);
        forward_intent = new Intent(FORWARD_BUTTON);
        backward_intent = new Intent(BACKWARD_BUTTON);
        sbup_intent = new Intent(SEEKBAR_UP);
        sbdown_intent = new Intent(SEEKBAR_DOWN);

        // Button Pending Intent init
        play_pending = PendingIntent.getService(getApplicationContext(), 0, play_intent, 0);
        stop_pending = PendingIntent.getService(getApplicationContext(), 0 , stop_intent, 0);
    }

    class SeekBarClickListener implements SeekBar.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId())
            {
                case R.id.seekbar_paytime:
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        try {
                            sbdown_intent.putExtra("seekbar","SEEKBAR_DOWN");
                            sbdown_pending = PendingIntent.getService(getApplicationContext(),0,sbdown_intent,0);
                            sbdown_pending.send(getApplicationContext(),0,sbdown_intent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        try {
                            sync_time = seekbar_playtime.getProgress();
                            sbup_intent.putExtra("seekbar","SEEKBAR_UP");
                            sbup_intent.putExtra("sbposition",sync_time);
                            sbup_pending = PendingIntent.getService(getApplicationContext(),0,sbup_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            sbup_pending.send(getApplicationContext(),0,sbup_intent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            return false;
        }
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
                default:
                    break;
            }
        }
    }

    class BtnTouchEvent implements Button.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(v.getId())
            {
                case R.id.btn_forward:
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        forward_intent.putExtra("forward","FORWARD_DOWN");
                        forward_pending = PendingIntent.getService(getApplicationContext(),0,forward_intent,0);
                        wardflag = true;
                        wardString = "forward";
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        try {
                            forward_intent.putExtra("forward","FORWARD_UP");
                            forward_pending = PendingIntent.getService(getApplicationContext(),0,forward_intent,0);
                            forward_pending.send(getApplicationContext(),0,forward_intent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        wardflag = false;
                        wardString ="";
                    }
                    break;
                case R.id.btn_backward:
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        backward_intent.putExtra("backward","BACKWARD_DOWN");
                        backward_pending = PendingIntent.getService(getApplicationContext(), 0, backward_intent, 0);
                        wardflag = true;
                        wardString = "backward";
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP)
                    {
                        try {
                            backward_intent.putExtra("backward","BACKWARD_UP");
                            backward_pending = PendingIntent.getService(getApplicationContext(), 0, backward_intent, 0);
                            backward_pending.send(getApplicationContext(),0,backward_intent);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                        wardflag = false;
                        wardString = "";
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    // service start, stop, connection
    private void setStartService()
    {

        startService(new Intent(MainActivity.this, MusicService.class));
        isBound = bindService(new Intent(this, MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void setStopService()
    {
        if(mIsBound)
        {
            unbindService(mConnection);
            mIsBound = false;
        }
        stopService(new Intent(MainActivity.this, MusicService.class));
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);

            try {
                Message msg = Message.obtain(null, MusicService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    public class wardThread extends Thread
    {
        public void run()
        {
            while(true)
            {
                if(wardflag)
                {
                    if(wardString.contains("forward"))
                    {
                        try {
                            forward_pending.send(getApplicationContext(),0,forward_intent);
                            sync_time++;
                            seekbar_playtime.setProgress(sync_time);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(wardString.contains("backward")) {
                        try {
                            backward_pending.send(getApplicationContext(), 0, backward_intent);
                            if(sync_time != 0)
                            {
                                sync_time--;
                            }
                            seekbar_playtime.setProgress(sync_time);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runThread() {

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if(thread_flag && sync_time <= seekbar_playtime.getMax())
                                {
                                    sync_time++;
                                    if(sync_time == seekbar_playtime.getMax())
                                    {
                                        thread_flag = false;
                                        sync_time = 0;
                                    }
                                    seekbar_playtime.setProgress(sync_time);
                                }
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    // Service로 부터 message를 받음
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MusicService.MSG_SEND_TO_ACTIVITY:
                    seekbar_time = msg.getData().getInt("fromService");
                    seekbar_playtime.setMax(seekbar_time);
                    break;
                case MusicService.MSG_SEND_SEEKBAR:
                    seekbar_status = msg.getData().getString("fromSeekbar");
                    if(seekbar_status.contains("PLAY"))
                    {
                        if(thread_flag)
                        {
                            thread_flag = false;
                        }
                        else
                        {
                            thread_flag = true;
                        }
                    }
                    else if(seekbar_status.contains("STOP"))
                    {
                        thread_flag = false;
                        sync_time = 0;
                        seekbar_playtime.setProgress(0);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }));

    // Service로 Message 전송
    private void sendMessageToService(String str) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, MusicService.MSG_SEND_TO_SERVICE, str);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    // bind 해제
    public void onBackPressed()
    {
        if(isBound)
        {
            unbindService(mConnection);
            isBound = false;
        }
        super.onBackPressed();
    }

    @Override
    protected  void onDestroy()
    {
        if(isBound)
        {
            unbindService(mConnection);
            isBound = false;
        }
        super.onDestroy();
    }
}
