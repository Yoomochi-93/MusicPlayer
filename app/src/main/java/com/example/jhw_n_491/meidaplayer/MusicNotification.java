package com.example.jhw_n_491.meidaplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class MusicNotification {

    Context mContext;
    RemoteViews rContentView;
    NotificationCompat.Builder mBuilder;
    Intent main_back, music_play, music_stop;
    PendingIntent main_pintent, play_pintent, stop_pintent;
    NotificationManager mNotificationManager = null;
    final static int NOTIFICATION_PLAYER_ID = 0x342;


    public MusicNotification(Context recv_context)
    {
        mContext = recv_context;
        init();
    }

    public void ExpandedlayoutNotification()
    {
        NotificationCompat.Builder mBuilder = createNotification();
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("Music Start");
        inboxStyle.setSummaryText("Music App");

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public NotificationCompat.Builder createNotification()
    {
        rContentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_player);
        rContentView.setOnClickPendingIntent(R.id.foreground_play, play_pintent);
        rContentView.setOnClickPendingIntent(R.id.foreground_stop, stop_pintent);

        mBuilder = new NotificationCompat.Builder(mContext.getApplicationContext())
                .setSmallIcon(R.drawable.notificationimage)
                .setContent(rContentView)
                .setContentIntent(main_pintent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mBuilder.setCategory(Notification.CATEGORY_PROGRESS)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PRIVATE);
        }

        return mBuilder;
    }

    public void init()
    {
        main_back = new Intent(mContext.getApplicationContext(), MainActivity.class);
        main_back.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        music_play = new Intent("FOREGROUND_PLAY");
        music_stop = new Intent("FOREGROUND_STOP");

        main_pintent = PendingIntent.getActivity(mContext.getApplicationContext(),0,main_back,0);
        play_pintent = PendingIntent.getService(mContext.getApplicationContext(), 0, music_play, 0);
        stop_pintent = PendingIntent.getService(mContext.getApplicationContext(), 0, music_stop,0);
    }
}