package com.opencom.dgc.channel.fm;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.opencom.dgc.entity.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static com.opencom.dgc.channel.fm.FmPostDetailView.ACTION_AUDIO_PROGRESS_CALLBACK;
import static com.opencom.dgc.channel.fm.FmPostDetailView.AUDIO_IS_CACHING;

/**
 * Created by Seven on 2016/7/13.
 * <p
 * 播放Fm和音乐
 */
public class FMPlayer implements MediaPlayerStateWrapper.StateListener {
    private Context context;
    private PlayerCallBack mPlayerCallBack;
    private ArrayList<Song> currentPlayingSongs;
    private int currentPlayingPos;
    private MediaPlayerStateWrapper mWrapper;
    private Subscription mSubscription;

    private Subscription mCheckBufferSub;

    public FMPlayer(Context context, PlayerCallBack service) {
        this.context = context;
        this.mPlayerCallBack = service;
        currentPlayingSongs = new ArrayList<>();
        mWrapper = new MediaPlayerStateWrapper(this);
    }

    public void playListSongs(List<Song> songList, final int startSongPos) throws IOException {
        currentPlayingSongs.clear();
        currentPlayingSongs.addAll(songList);
        resetState();
        mWrapper.setDataSource(currentPlayingSongs.get(startSongPos).getUrl());
        mWrapper.prepareAsync();
        setPlayingPos(startSongPos);
    }

    public void setCurrentPlayingSong(Song currentPlayingSong) {
        currentPlayingSongs.clear();
        currentPlayingSongs.add(currentPlayingSong);
    }

    public void stopPlayer() {
        if (mWrapper.isPlaying())
            mWrapper.stop();
    }


    public void setPlayList(ArrayList<Song> list) {
        resetState();
        if (list == null || list.isEmpty()) {
            return;
        }
        this.currentPlayingSongs = list;
        Song song = list.get(0);
        setPlayingPos(0);
        mPlayerCallBack.updatePlayerAndNotification(song, false);
    }


    public void playOrStop() {
        Song song = getCurrentPlayingSong();
        if (song == null) {
            return;
        }
        switch (mWrapper.getState()) {
            case IDLE:
                mWrapper.setDataSource(song.getUrl());
                mWrapper.prepareAsync();
                break;

            case INITIALIZED:
                mWrapper.prepareAsync();
                break;

            case PREPARING:
                resetState();
                break;
            default:
                if (mWrapper.isPlaying()) {
                    mWrapper.pause();
                } else {
                    mWrapper.start();
                }
                break;
        }
    }


    public int getNextSongPosition(int currentPos) {
        if (currentPos == currentPlayingSongs.size() - 1) {
            //跳到第一首
            return 0;
        } else {
            //下一首
            return currentPos + 1;
        }
    }


    public void playNextSong(int nextSongPos, boolean isAutoPlay) throws IOException {
        if (nextSongPos < currentPlayingSongs.size()) {
            configurePlayer(nextSongPos, isAutoPlay);
        } else {
            configurePlayer(0, isAutoPlay);
        }
    }

    public void playPrevSong(final int prevSongPos) throws IOException {
        int position;

        if (prevSongPos <= 0) {
            position = 0;
        } else {
            position = prevSongPos;
        }
        final int pos = position;
        configurePlayer(pos, mWrapper.isPlaying());
    }

    void resetState() {
        if (mWrapper.getState() != MediaPlayerStateWrapper.State.IDLE) {
            stopChecking();
            stopPushing();
            stopPlayer();
            mWrapper.reset();
        }
    }


    public void configurePlayer(int pos, boolean isAutoPlay) throws IOException {
        Song song = currentPlayingSongs.get(pos);
        setPlayingPos(pos);
        resetState();
        mWrapper.setDataSource(song.getUrl());
        if (isAutoPlay) {
            mWrapper.prepareAsync();
            mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), true);
        } else {
            mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), false);
        }
    }

    public void playSingleSong(Song song) throws IOException {
        if (song == null || song.getUrl() == null) {
            return;
        }
        Song currentSong = getCurrentPlayingSong();
        //当前播放的歌曲一样
        if (currentSong != null && song.equals(currentSong) && mWrapper.isPlaying()) {
            return;
        }
        if (getCurrentPlayingSongs().contains(song)) {
            int pos = getCurrentPlayingSongs().indexOf(song);
            configurePlayer(pos, true);
        } else {
            setCurrentPlayingSong(song);
            configurePlayer(0, true);
        }
    }

    public synchronized void setPlayingPos(int pos) {
        currentPlayingPos = pos;
    }

    public Song getCurrentPlaySong() {
        if (currentPlayingPos == -1) {
            return null;
        }
        return currentPlayingSongs.get(currentPlayingPos);
    }

    public ArrayList<Song> getCurrentPlayingSongs() {
        return currentPlayingSongs;
    }

    public void setCurrentPlayingSong(ArrayList<Song> currentPlayingSongs) {
        this.currentPlayingSongs.clear();
        this.currentPlayingSongs = currentPlayingSongs;
    }

    public Song getCurrentPlayingSong() {
        if (currentPlayingSongs.size() == 0) {
            return null;
        }
        return currentPlayingSongs.get(currentPlayingPos);
    }

    public Song getSongFromId(long id) {
        for (Song song : currentPlayingSongs) {
            if (song.getSongId() == id) {
                return song;
            }
        }
        return null;
    }

    public int getCurrentPlayingPos() {
        return currentPlayingPos;
    }

    public void seek(long seek) {
        int duration = mWrapper.getDuration();
        int seekBuffer = getBufferPercent() / 100 * duration;
        if (seek <= duration && seek <= seekBuffer) {
            mWrapper.seekTo((int) seek);
        }
    }

    public void addSongToQueue(Song s) {
        currentPlayingSongs.add(s);
    }

    public void addSonsListToQueue(List<Song> songList) {
        currentPlayingSongs.addAll(songList);
    }

    public void setVolumn(float left, float riht) {
        if (mWrapper != null) {
            mWrapper.setVolumn(left, riht);
        }
    }

    public int getBufferPercent() {
        return mWrapper.getBufferPercent();
    }

    public int getSongPosition() {
        return mWrapper.getCurrentPosition();
    }

    public int getSongDuration() {
        return mWrapper.getDuration();
    }

    public boolean isPlaying() {
        return mWrapper.isPlaying();
    }

    public void SendToTopic() {
        if (mSubscription == null || mSubscription.isUnsubscribed()) {
            mSubscription = Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            Intent intent = new Intent(ACTION_AUDIO_PROGRESS_CALLBACK);
                            intent.putExtra(FmPostDetailView.AUDIO_CACHE_PROGRESS, getBufferPercent());
                            if (isPlaying()) {
                                int position = getSongPosition();
                                intent.putExtra(FmPostDetailView.AUDIO_PLAY_PROGRESS, position < 0 ? (int) aLong.longValue() * 1000 : position);
                            }
                            intent.putExtra(FmPostDetailView.AUDIO_PLAY_TIME, getSongDuration());
                            context.sendBroadcast(intent);
                        }
                    });
        }

    }

    //控制缓冲与播放进度的关系，防止坑爹服务器的流导致播放卡顿
    void checkBuffer() {
        if (mCheckBufferSub == null || mCheckBufferSub.isUnsubscribed()) {
            mCheckBufferSub = Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            if (getBufferPercent() < 100) {
                                if (getBufferPercent() <= 25) {
                                    if (isPlaying()) {
                                        mWrapper.pause();
                                        sendCachingBroadcast(true);
                                    }

                                } else {
                                    int phasePercent = (getBufferPercent() - (getSongPosition() / getSongDuration()));
                                    if (phasePercent > 10 && mSubscription != null && !mSubscription.isUnsubscribed()) {
                                        if (mWrapper.getState() == MediaPlayerStateWrapper.State.PAUSED) {
                                            mWrapper.start();
                                            sendCachingBroadcast(false);
                                        }
                                    } else {
                                        if (isPlaying()) {
                                            mWrapper.pause();
                                            sendCachingBroadcast(true);
                                        }
                                    }
                                }
                            } else {
                                if (mWrapper.getState() == MediaPlayerStateWrapper.State.PAUSED && mSubscription != null && !mSubscription.isUnsubscribed()) {
                                    mWrapper.start();
                                    sendCachingBroadcast(false);
                                } else {
                                    stopChecking();
                                }
                            }
                        }
                    });
        }

    }

    private void sendCachingBroadcast(boolean isCaching) {
        Intent intent = new Intent(AUDIO_IS_CACHING);
        intent.putExtra("Caching", isCaching);
        context.sendBroadcast(intent);
    }


    void stopPushing() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    void stopChecking() {
        if (mCheckBufferSub != null && !mCheckBufferSub.isUnsubscribed()) {
            sendCachingBroadcast(false);
            mCheckBufferSub.unsubscribe();
        }
    }

    @Override
    public void onInitialized() {
    }

    @Override
    public void onPreParing() {
        SendToTopic();
    }

    @Override
    public void onStarted() {
        checkBuffer();
    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {
        mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), false);
    }

    @Override
    public void onCompleted() {
        try {
            stopPushing();
            playNextSong(getCurrentPlayingPos() + 1, true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "播放音乐出错",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError() {
        mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), false);
        stopPushing();
        resetState();
    }

    @Override
    public void onReset() {
        mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), false);
    }


    interface PlayerCallBack {
        void updatePlayerAndNotification(Song song, boolean isPlaying);
    }
}
