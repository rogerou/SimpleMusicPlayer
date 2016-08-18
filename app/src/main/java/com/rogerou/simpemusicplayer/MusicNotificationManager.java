package com.rogerou.simpemusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;


/**
 * Created by Seven on 2016/7/13.
 * 通知栏
 */
public class MusicNotificationManager {
    private static final int NOTIFICATION_ID = 9527777;
    private final Context mContext;

    private final MusicService mService;

    private boolean mNotifyActive;
    private Notification mNotification;

    private android.app.NotificationManager mNotificationManagerCompat;

    public MusicNotificationManager(Context context, MusicService service) {
        mContext = context;
        mService = service;
        mNotificationManagerCompat = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private NotificationCompat.Builder createNotification(boolean removable) {
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(MusicService.ACTION_NOTI_CLICK);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(MusicService.ACTION_NOTI_REMOVE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(mContext, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (removable) {
            return new NotificationCompat.Builder(mContext)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.default_art)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
        } else {
            return new NotificationCompat.Builder(mContext)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.default_art)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
        }
    }

    public void setNotificationPlayer(boolean removable) {
        mNotification = createNotification(removable).
                setPriority(NotificationCompat.PRIORITY_MAX).
                setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_art)).build();
        RemoteViews noti_big = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
        RemoteViews noti_CollapsedView = new RemoteViews(mContext.getPackageName(), R.layout.notification_small);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotification.bigContentView = noti_big;
        }
        mNotification.contentView = noti_CollapsedView;
        if (!removable) {
            mService.startForeground(NOTIFICATION_ID, mNotification);
        }

        mNotificationManagerCompat.notify(NOTIFICATION_ID, mNotification);
        mNotifyActive = true;
    }


    public void changeNotificationDetails(String songName, String artistName, String cover, boolean isPlaying) {
        Intent playClick = new Intent(MusicService.ACTION_PAUSE_SONG);
        PendingIntent pendingClick = PendingIntent.getBroadcast(mContext, 21021, playClick, 0);
        Intent prevClick = new Intent(MusicService.ACTION_PREV_SONG);
        PendingIntent pendingPrev = PendingIntent.getBroadcast(mContext, 21121, prevClick, 0);
        Intent nextClick = new Intent(MusicService.ACTION_NEXT_SONG);
        PendingIntent pendingNext = PendingIntent.getBroadcast(mContext, 21221, nextClick, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotification.bigContentView.setTextViewText(R.id.noti_name, songName);
            mNotification.bigContentView.setTextViewText(R.id.noti_artist, artistName);
            mNotification.bigContentView.setOnClickPendingIntent(R.id.noti_play_button, pendingClick);
            mNotification.bigContentView.setOnClickPendingIntent(R.id.noti_prev_button, pendingPrev);
            mNotification.bigContentView.setOnClickPendingIntent(R.id.noti_next_button, pendingNext);
            mNotification.bigContentView.setImageViewResource(R.id.noti_play_button, isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
        }
        mNotification.contentView.setTextViewText(R.id.noti_name, songName);
        mNotification.contentView.setTextViewText(R.id.noti_artist, artistName);
        mNotification.contentView.setOnClickPendingIntent(R.id.noti_play_button, pendingClick);
        mNotification.contentView.setOnClickPendingIntent(R.id.noti_prev_button, pendingPrev);
        mNotification.contentView.setOnClickPendingIntent(R.id.noti_next_button, pendingNext);
        mNotification.contentView.setImageViewResource(R.id.noti_play_button, isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);

        Glide.with(mContext).load(cover).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mNotification.bigContentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                }
                mNotification.contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                mNotificationManagerCompat.notify(NOTIFICATION_ID, mNotification);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);

            }
        });

    }

    public void updateNotificatonView() {
        mNotificationManagerCompat.notify(NOTIFICATION_ID, mNotification);

    }


    public boolean isNotifyActive() {
        return mNotifyActive;
    }

    public void setNotifyActive(boolean active) {
        mNotifyActive = active;
    }

    public Notification getNotification() {
        return mNotification;

    }


}
