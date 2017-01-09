package com.opencom.dgc.channel.fm;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.opencom.dgc.MainActivity;
import com.opencom.dgc.entity.Song;
import com.opencom.dgc.util.NetStatusUtil;
import com.waychel.tools.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ibuger.lbbs.LbbsPostViewActivity;

/**
 * Created by Seven on 2016/7/12.
 * <p/>
 * 后台音乐播放的Service
 */
public class MusicService extends Service implements FMPlayer.PlayerCallBack {

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
    public static final String ACTION_ADD_LIST_QUEUE = "ACTION_ADD_LIST_QUEUE";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_DESTROY = "ACTION_DESTROY";
    public static final String ACTION_CLICK = "FM_CLICK";
    public static final String ACTION_REFRESH_LIST = "ACTION_REFRESH_LIST";

    private AudioManager mAudioManager;

    private FMPlayer mPlayer;

    private FMNotificationManager mNotificationManager;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                handleBroadcastReceived(context, intent);


            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("FMService", e);
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
            mPlayer = new FMPlayer(this, this);
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
        intentfilter.addAction(ACTION_ADD_LIST_QUEUE);
        intentfilter.addAction(ACTION_STOP);
        intentfilter.addAction(ACTION_DESTROY);
        intentfilter.addAction(ACTION_CLICK);
        intentfilter.addAction(ACTION_REFRESH_LIST);
        registerReceiver(mBroadcastReceiver, intentfilter);
        mNotificationManager = new FMNotificationManager(MusicService.this, this);
        mNotificationManager.setNotificationPlayer(false);
        LogUtils.e("开启service");
        return START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }


    private void handleBroadcastReceived(Context context, Intent intent) throws IOException {
        if (requestAudio() != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Toast.makeText(context, "获取音乐焦点失败", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (intent.getAction()) {
            case ACTION_PLAY_SINGLE:
                Song s = intent.getParcelableExtra("song");
                mPlayer.playSingleSong(s);
//                updatePlayerAndNotification(s, true);
                if (NetStatusUtil.getNetStatus(this).equals("2G/3G")) {
                    Toast.makeText(this, "当前网络为移动网络！", Toast.LENGTH_LONG).show();
                }
                break;
            case ACTION_PLAY_ALL_SONGS:
                List<Song> songList = intent.getParcelableArrayListExtra("songList");
                if (songList == null || songList.isEmpty() || mPlayer.getCurrentPlayingSongs().containsAll(songList)) {
                    return;
                }
                int startPos = intent.getIntExtra("pos", 0);
                mPlayer.playListSongs(songList, startPos);
                updatePlayerAndNotification(songList.get(startPos), true);
                if (NetStatusUtil.getNetStatus(this).equals("2G/3G")) {
                    Toast.makeText(this, "当前网络为移动网络！", Toast.LENGTH_LONG).show();
                }
                break;
            case ACTION_NEXT_SONG:
                mPlayer.playNextSong(mPlayer.getCurrentPlayingPos() + 1, mPlayer.isPlaying());
                break;
            case ACTION_PREV_SONG:
                mPlayer.playPrevSong(mPlayer.getCurrentPlayingPos() - 1);
                break;
            case ACTION_PAUSE_SONG:
                if (mPlayer.getCurrentPlayingSong() == null) {
                    return;
                }
                if (!mPlayer.isPlaying()) {
                    mPlayer.checkBuffer();
                    mPlayer.SendToTopic();
                } else {
                    mPlayer.stopChecking();
                    mPlayer.stopPushing();
                }
                updatePlayerAndNotification(mPlayer.getCurrentPlayingSong(), !mPlayer.isPlaying());
                mPlayer.playOrStop();
                break;
            case ACTION_SEEK_SONG:
                mPlayer.seek(intent.getLongExtra("seek", 0));
                break;
            case ACTION_CHANGE_SONG:
                mPlayer.playNextSong(intent.getIntExtra("pos", 0), true);
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
//                mNotificationManager.setNotifyActive(false);
//                mPlayer.getMediaPlayer().stop();
                break;
            case ACTION_ADD_QUEUE:
                Song song = intent.getParcelableExtra("song");
                mPlayer.addSongToQueue(song);
                break;
            case ACTION_ADD_LIST_QUEUE:
                List<Song> songs = intent.getParcelableArrayListExtra("songList");
                mPlayer.addSonsListToQueue(songs);
                break;

            case ACTION_STOP:
                mPlayer.stopPlayer();
                break;

            case ACTION_DESTROY:
                stop();
                break;

            case ACTION_CLICK:
                toTopic(context);
                break;
            case ACTION_REFRESH_LIST:
                ArrayList<Song> newSongs = intent.getParcelableArrayListExtra("songList");
                mPlayer.setPlayList(newSongs);
                break;
        }

    }

    private void toTopic(Context context) {
        Intent intent = new Intent(context, LbbsPostViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("kind_id", mPlayer.getCurrentPlayingSong().getKind_id());
        intent.putExtra("post_id", mPlayer.getCurrentPlaySong().getPost_id());
        context.startActivity(intent);
    }


    public void updatePlayerAndNotification(Song song, boolean isPlaying) {
        if (song == null) {
            return;
        }
        Intent i = new Intent();
        @PlayControlFragment.STATE
        String action = isPlaying ? PlayControlFragment.PLAYING : PlayControlFragment.PAUSE;
        i.setAction(action);
        i.putExtra("song", song);
        sendBroadcast(i);
        if (mNotificationManager.isNotifyActive()) {
            mNotificationManager.setNotifyActive(false);
        }
        mNotificationManager.changeNotificationDetails(song.getSong_name(), song.getAuthor_name(), song.getSong_album(), isPlaying);
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
                    mPlayer.setVolumn(0.0f, 0.0f);
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
                    Song song = mPlayer.getCurrentPlayingSong();
                    if (song != null)
                        updatePlayerAndNotification(song, false);
                    mPlayer.stopPushing();
//                    LogUtils.e("失去焦点");
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

    private void stop() {
        Intent i = new Intent(PlayControlFragment.PAUSE);
        i.putExtra("song", mPlayer.getCurrentPlayingSong());
        sendBroadcast(i);
        stopForeground(true);
        if (mPlayer != null) {
            mPlayer.resetState();
        }

        if (mNotificationManager != null) {
            mNotificationManager.setNotifyActive(false);
            mNotificationManager.setNotificationPlayer(true);
            mNotificationManager.remove();
        }
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


}

