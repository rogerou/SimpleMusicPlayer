package com.rogerou.simpemusicplayer;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by Roger on 2016/7/12.
 * <p/>
 * 后台音乐播放的Service
 */
public class MusicService extends Service {

    public static final String ACTION_PLAY_SINGLE = "ACTION_PLAY_SINGLE";
    public static final String ACTION_PLAY_ALL_SONGS = "ACTION_PLAY_ALL_SONGS";
    public static final String ACTION_NOTI_CLICK = "ACTION_NOTI_CLICK";
    public static final String ACTION_NOTI_REMOVE = "ACTION_NOTI_REMOVE";
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final String ACTION_SEEK_SONG = "ACTION_SEEK_SONG";
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PREV_SONG = "ACTION_PREV_SONG";
    public static final String ACTION_PAUSE_SONG = "ACTION_PAUSE_SONG";
    public static final String ACTION_ADD_QUEUE = "ACTION_ADD_QUEUE";


    private AudioManager mAudioManager;

    private FMPlayer mPlayer;

    private MusicNotificationManager mNotificationManager;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                handleBrocastReceived(context, intent);
            } catch (Exception e) {
                Toast.makeText(MusicService.this, "音乐播放出错啦", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mPlayer == null) {
            mPlayer = new FMPlayer(MusicService.this, this);
        }
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(ACTION_PLAY_SINGLE);
        intentfilter.addAction(ACTION_PLAY_ALL_SONGS);
        intentfilter.addAction(ACTION_NEXT_SONG);
        intentfilter.addAction(ACTION_PREV_SONG);
        intentfilter.addAction(ACTION_PAUSE_SONG);
        intentfilter.addAction(ACTION_SEEK_SONG);
        intentfilter.addAction(ACTION_CHANGE_SONG);
        intentfilter.addAction(ACTION_NOTI_CLICK);
        intentfilter.addAction(ACTION_NOTI_REMOVE);
        intentfilter.addAction(ACTION_ADD_QUEUE);
        registerReceiver(mBroadcastReceiver, intentfilter);
        mNotificationManager = new MusicNotificationManager(MusicService.this, this);
        mNotificationManager.setNotificationPlayer(false);
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }


    private void handleBrocastReceived(Context context, Intent intent) throws IOException {
        if (requestAudio() != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Toast.makeText(context, "获取音乐焦点失败", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (intent.getAction()) {
            case ACTION_PLAY_SINGLE:
                Song s = intent.getParcelableExtra("song");
                mPlayer.playSingleSong(s);
                updatePlayerAndNotification(PlayControlFragment.PLAYING, s);
                break;
            case ACTION_PLAY_ALL_SONGS:
                List<Song> songList = intent.getParcelableArrayListExtra("songList");
                int startPos = intent.getIntExtra("pos", 0);
                mPlayer.playListSongs(songList, startPos);
                updatePlayerAndNotification(PlayControlFragment.PLAYING, songList.get(startPos));
                break;
            case ACTION_NEXT_SONG:
                mPlayer.playNextSong(mPlayer.getCurrentPlayingPos() + 1);
                break;
            case ACTION_PREV_SONG:
                mPlayer.playPrevSong(mPlayer.getCurrentPlayingPos() - 1);
                updatePlayerAndNotification(PlayControlFragment.PLAYING, mPlayer.getCurrentPlayingSong());
                break;
            case ACTION_PAUSE_SONG:
                mPlayer.playOrStop(mNotificationManager);
                updatePlayerAndNotification(mPlayer.getMediaPlayer().isPlaying() ? PlayControlFragment.PLAYING : PlayControlFragment.PAUSE, mPlayer.getCurrentPlayingSong());
                break;
            case ACTION_SEEK_SONG:
                mPlayer.seek(intent.getIntExtra("seek", 0));
                break;
            case ACTION_CHANGE_SONG:
                mPlayer.playNextSong(intent.getIntExtra("pos", 0));
                break;
            case ACTION_NOTI_CLICK:
                final Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setClass(context, MainActivity.class);
//                i.putExtra(Constants.START_CHANNEL_ACTIVITY_FM, true);
                i.putExtra("channelId", mPlayer.getCurrentPlaySong().getSongId());
                startActivity(i);
                break;
            case ACTION_NOTI_REMOVE:
                mNotificationManager.setNotifyActive(false);
                mPlayer.getMediaPlayer().stop();
                break;
            case ACTION_ADD_QUEUE:
                Song song = intent.getParcelableExtra("song");
                mPlayer.addSongToQueue(song);
                break;
        }

    }

    public void updatePlayerAndNotification(@PlayControlFragment.STATE String action, Song song) {
        Intent i = new Intent();
        i.setAction(action);
        i.putExtra("song", song);
        sendBroadcast(i);
        if (mNotificationManager.isNotifyActive()) {
            mNotificationManager.setNotifyActive(false);
        }
        mNotificationManager.changeNotificationDetails(song.getSong_name(), song.getAuthor_name(), song.getSong_album(), mPlayer.getMediaPlayer().isPlaying());

    }


    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            /**
             * AUDIOFOCUS_GAIN：获得音频焦点。
             * AUDIOFOCUS_LOSS：失去音频焦点，并且会持续很长时间。这是我们需要停止MediaPlayer的播放。
             * AUDIOFOCUS_LOSS_TRANSIENT：失去音频焦点，但并不会持续很长时间，需要暂停MediaPlayer的播放，等待重新获得音频焦点。
             * AUDIOFOCUS_REQUEST_GRANTED 永久获取媒体焦点（播放音乐）
             * AUDIOFOCUS_GAIN_TRANSIENT 暂时获取焦点 适用于短暂的音频
             * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK Duck我们应用跟其他应用共用焦点
             * 我们播放的时候其他音频会降低音量
             */
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                    Log.e("长时间失去焦点");
                    mPlayer.stopPlayer();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                    LogUtils.e("短暂失去焦点");
                    mPlayer.setVolumn(0.2f, 0.2f);
                    break;

                case AudioManager.AUDIOFOCUS_GAIN:
//                    LogUtils.e("获得焦点");
                    mPlayer.setVolumn(1.0f, 1.0f);

                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
//                    LogUtils.e("失去焦点");
                    mPlayer.stopPlayer();
                    break;

                default:
//                    LogUtils.e("focusChanged" + focusChange);
                    break;
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }


    public int requestAudio() {
        return mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
    //    private void tryToGetAudioFocus() {
//        LogUtils.e("tryToGetAudioFocus");
//        if (mAudioFocus != AudioManager.AUDIOFOCUS_GAIN) {
//            int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
//                    AudioManager.AUDIOFOCUS_GAIN);
//            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                mAudioFocus = AudioManager.AUDIOFOCUS_GAIN;
//            }
//        }
//    }
//
//    private void giveUpAudioFocus() {
//        LogUtils.e("giveUpAudioFocus");
//        if (mAudioFocus == AudioManager.AUDIOFOCUS_GAIN) {
//            if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                mAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
//            }
//        }
//    }

//    private void createMediaPlayerIfNeeded() {
//        LogUtils.e("createMediaPlayerIfNeeded. needed? ");
//        if (mMediaPlayer == null) {
//            mMediaPlayer = new MediaPlayer();
//
//            mMediaPlayer.setWakeMode(getApplicationContext(),
//                    PowerManager.PARTIAL_WAKE_LOCK);
//            mMediaPlayer.setOnPreparedListener(this);
//            mMediaPlayer.setOnCompletionListener(this);
//            mMediaPlayer.setOnErrorListener(this);
//            mMediaPlayer.setOnSeekCompleteListener(this);
//        } else {
//            mMediaPlayer.reset();
//        }
//    }
//
//    private void relaxResources(boolean releaseMediaPlayer) {
//        if (releaseMediaPlayer && mMediaPlayer != null) {
//            mMediaPlayer.reset();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
//    }

}
