package com.example.jhw_n_491.meidaplayer;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.io.File;

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
    final String MP3CHECK_SERVICE = "MP3CHECK";
    private static final int REQUEST_EXTERNAL_STORAGE = 2;

    // Service Object
    Intent play_intent, stop_intent, forward_intent, backward_intent, sbup_intent, sbdown_intent, mp3check_intent;
    PendingIntent play_pending, stop_pending, forward_pending, backward_pending, sbup_pending, sbdown_pending, mp3check_pending;

    //ProgressDialog
    ProgressDialog mProgressDialog;

    // messengee
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;

    // Seekbar Time
    int seekbar_time = 0;
    int sync_time = 0;
    String seekbar_status;


    // Thread & Bind flag
    private boolean thread_flag = false;
    private boolean isBound = false;
    private boolean isThread;

    // forward, backward flag
    public static boolean wardflag = false;
    public static String wardString = "";
    Thread wardbtnThread, runTimeThread;

    // MP3 File Check
    private File mp3file;
    private boolean fileflag = true;

    // Service stop
    String testmsg = "IMSI";


    // Audio manager
    AudioManager am;


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
            fileflag = false;
            finish();
        }

        initUiComponents();

        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(cImage);
        Glide.with(this).load(R.drawable.backgrounds).into(gifImage);

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("최신가요 MP3 파일 다운로드 중...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMax(100);

        // this is how you fire the downloader
        mProgressDialog.show();

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("url", "http://35.203.158.180/download/music.mp3");
        intent.putExtra("receiver", new DownloadReceiver(new Handler()));
        startService(intent);


        setStartService();
        isThread = true;
        runTimeThread.start();

    }

    // UI Init
    void initUiComponents()
    {
        focusChangeListener audioChangeListener = new focusChangeListener();

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int focusResult = am.requestAudioFocus(audioChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        // Components init
        BtnOnClickListener onClickListener = new BtnOnClickListener();
        BtnTouchEvent onTuchListener = new BtnTouchEvent();
        SeekBarClickListener onChangeListener = new SeekBarClickListener();
        SharedPreferences sp = getSharedPreferences("Music_Player", Activity.MODE_PRIVATE);

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

        //Seekbar Listener
        seekbar_playtime = (SeekBar) findViewById(R.id.seekbar_paytime);
        int sync_max;
        try
        {
            sync_max = Integer.parseInt(sp.getString("sync_time_max", null));
        } catch (NumberFormatException e) {
            sync_max = 0;
        }
        seekbar_playtime.setMax(sync_max);
        seekbar_playtime.setOnTouchListener(onChangeListener);
        if (isServiceRunningCheck()) {
            try
            {
                sync_time = Integer.parseInt(sp.getString("sync_time", null));
            } catch (NumberFormatException e) {
                sync_time = 0;
            }

            seekbar_playtime.setProgress(sync_time);
        }

        // Thread Init
        wardbtnThread = new wardThread();
        wardbtnThread.start();

        runTimeThread = new runThread();

        // Button intent init
        play_intent = new Intent(PLAY_BUTTON);
        stop_intent = new Intent(STOP_BUTTON);
        forward_intent = new Intent(FORWARD_BUTTON);
        backward_intent = new Intent(BACKWARD_BUTTON);
        sbup_intent = new Intent(SEEKBAR_UP);
        sbdown_intent = new Intent(SEEKBAR_DOWN);
        mp3check_intent = new Intent(MP3CHECK_SERVICE);

        // Button Pending Intent init
        play_pending = PendingIntent.getService(getApplicationContext(), 0, play_intent, 0);
        stop_pending = PendingIntent.getService(getApplicationContext(), 0 , stop_intent, 0);

        // File Check
        mp3file = new File(Environment.getExternalStorageDirectory().toString()+ "/music.mp3");

    }

    // Service Check
    private boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.jhw_n_491.meidaplayer.MusicService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // SeekBar ClickListener
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
                            SharedPreferences sp = getSharedPreferences("Music_Player", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();


                            sync_time = seekbar_playtime.getProgress();
                            editor.putString("sync_time", String.valueOf(sync_time));
                            editor.apply();

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

    // Btn ClickListener
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
                        setStopService();
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

    // Btn TouchEvent
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
                    dismissProgressDialog();
                }
            }
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing())
            return;
        Context context = getApplicationContext();

        if (context instanceof Activity) {
            if(!((Activity)context).isFinishing()) {
                mProgressDialog.dismiss();
            }
        } else {
            mProgressDialog.dismiss();
        }

        if(fileflag == true)
        {
            CheckMPFile();
        }
    }

    // service start, stop, connection
    private void setStartService()
    {
        // bind to the Service
        final Intent serviceIntent = new Intent(MainActivity.this,  MusicService.class);
        startService(serviceIntent);
        isBound = bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void setStopService()
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


    public void CheckMPFile()
    {
        try {
            mp3check_intent.putExtra("mp3Check","exists");
            mp3check_pending = PendingIntent.getService(getApplicationContext(),0,mp3check_intent,0);
            mp3check_pending.send(getApplicationContext(), 0, mp3check_intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    // Backward, forward Thread
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

    public class runThread extends Thread
    {
        public void run()
        {
            while (isThread) {
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if(thread_flag && sync_time <= seekbar_playtime.getMax())
                            {
                                sync_time++;
                                if(sync_time >= seekbar_playtime.getMax())
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
    }

    // Service로 부터 message를 받음
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MusicService.MSG_SEND_TO_ACTIVITY:
                    seekbar_time = msg.getData().getInt("fromService");
                    seekbar_playtime.setMax(seekbar_time);

                    SharedPreferences sp = getSharedPreferences("Music_Player", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("sync_time_max", String.valueOf(seekbar_time));
                    editor.apply();
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
                case MusicService.MSG_SEND_STOPSERVICE:
                    testmsg = msg.getData().getString("ServiceStop");
                    if(testmsg.contains("STOP"))
                    {
                        setStopService();
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
    protected void onDestroy() {
        dismissProgressDialog();
        isThread = false;

        if(isBound)
        {
            unbindService(mConnection);
            isBound = false;
        }
        super.onDestroy();
    }

    // Audio force
    class focusChangeListener implements AudioManager.OnAudioFocusChangeListener{

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange)
            {
                case (AudioManager.AUDIOFOCUS_LOSS):
                    try {
                        if(MusicService.playcheck == true)
                        {
                            play_pending.send(getApplicationContext(), 1, play_intent);
                        }
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    break;
                case (AudioManager.AUDIOFOCUS_GAIN):
                    try {
                        if(MusicService.playcheck == false) {
                            play_pending.send(getApplicationContext(), 1, play_intent);
                        }
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}