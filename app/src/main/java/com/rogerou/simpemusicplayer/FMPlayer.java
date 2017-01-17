package com.opencom.dgc.channel.fm;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.opencom.db.bean.Song;

import java.io.IOException;
import java.lang.ref.SoftReference;
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
public class PlayerController implements MediaPlayerStateWrapper.StateListener {
    private SoftReference<Context> mContext;
    private PlayerCallBack mPlayerCallBack;
    private ArrayList<Song> mCurrentPlayingSongs;
    private volatile int currentPlayingPos;
    private MediaPlayerStateWrapper mWrapper;
    private Subscription mSubscription;
    private Subscription mCheckBufferSub;
    private boolean isBuffing;

    public PlayerController(Context context, PlayerCallBack service) {
        this.mContext = new SoftReference<>(context);
        this.mPlayerCallBack = service;
        mCurrentPlayingSongs = new ArrayList<>();
        mWrapper = new MediaPlayerStateWrapper(this);
    }

    public void playListSongs(List<Song> SongList, final int startSongPos) throws IOException {
        mCurrentPlayingSongs.clear();
        mCurrentPlayingSongs.addAll(SongList);
        resetState();
        mWrapper.setDataSource(mCurrentPlayingSongs.get(startSongPos).getUrl());
        mWrapper.prepareAsync();
        setPlayingPos(startSongPos);
    }

    public void setCurrentPlayingSong(Song currentPlayingSong) {
        mCurrentPlayingSongs.clear();
        mCurrentPlayingSongs.add(currentPlayingSong);
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
        this.mCurrentPlayingSongs = list;
        Song Song = list.get(0);
        setPlayingPos(0);
        mPlayerCallBack.updatePlayerAndNotification(Song, false);
    }


    public void playOrStop() {
        Song Song = getCurrentPlayingSong();
        if (Song == null) {
            return;
        }
        if (!isPlaying()) {
            checkBuffer();
        } else {
            stopChecking();
        }
        switch (mWrapper.getState()) {
            case IDLE:
                String url = mPlayerCallBack.getProxyUrl(Song.getSong_id());
                mWrapper.setDataSource(url);
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
                    if (!isBuffing) {
                        mWrapper.start();
                    } else {
                        resetState();
                    }

                }
                break;
        }
    }


    public int getNextSongPosition(int currentPos) {
        if (currentPos == mCurrentPlayingSongs.size() - 1) {
            //跳到第一首
            return 0;
        } else {
            //下一首
            return currentPos + 1;
        }
    }


    public void playNextSong(int nextSongPos, boolean isAutoPlay) throws IOException {
        if (nextSongPos < mCurrentPlayingSongs.size()) {
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

    public void resetState() {
        if (mWrapper.getState() != MediaPlayerStateWrapper.State.IDLE) {
            sendCachingBroadcast(false);
            stopChecking();
            stopPushing();
            mPlayerCallBack.stopCurrentConnection();
            stopPlayer();
            mWrapper.reset();
        }
    }

    public MediaPlayerStateWrapper.State getCurrentState() {
        return mWrapper.getState();
    }


    public void configurePlayer(int pos, boolean isAutoPlay) throws IOException {
        Song Song = mCurrentPlayingSongs.get(pos);
        setPlayingPos(pos);
        resetState();
        String url = mPlayerCallBack.getProxyUrl(Song.getSong_id());
        mWrapper.setDataSource(url);
        if (isAutoPlay) {
            mWrapper.prepareAsync();
        }
        mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), isAutoPlay);
    }

    public void playSingleSong(Song Song) throws IOException {
        if (Song == null || Song.getUrl() == null) {
            return;
        }
        Song currentSong = getCurrentPlayingSong();

        if (isBuffing) {
            sendCachingBroadcast(true);
            return;
        }
        //当前播放的歌曲一样
        if (currentSong != null && Song.equals(currentSong) && isPlaying()) {
            return;
        }
        if (getCurrentPlayingSongs().contains(Song)) {
            int pos = getCurrentPlayingSongs().indexOf(Song);
            configurePlayer(pos, true);
        } else {
            mCurrentPlayingSongs.add(Song);
            configurePlayer(mCurrentPlayingSongs.size() - 1, true);
        }
    }

    public void setPlayingPos(int pos) {
        currentPlayingPos = pos;
    }

    public Song getCurrentPlaySong() {
        if (currentPlayingPos == -1) {
            return null;
        }
        return mCurrentPlayingSongs.get(currentPlayingPos);
    }

    public ArrayList<Song> getCurrentPlayingSongs() {
        return mCurrentPlayingSongs;
    }

    public void setCurrentPlayingSong(ArrayList<Song> currentPlayingSongs) {
        this.mCurrentPlayingSongs.clear();
        this.mCurrentPlayingSongs = currentPlayingSongs;
    }

    public Song getCurrentPlayingSong() {
        if (mCurrentPlayingSongs.size() == 0) {
            return null;
        }
        return mCurrentPlayingSongs.get(currentPlayingPos);
    }

    public Song getSongFromId(long id) {
        for (Song Song : mCurrentPlayingSongs) {
            if (Song.getSong_id() == id) {
                return Song;
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
        mCurrentPlayingSongs.add(s);
    }

    public void addSonsListToQueue(List<Song> SongList) {
        mCurrentPlayingSongs.addAll(SongList);
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
        int duration = mWrapper.getDuration();
        if (duration > 0) {
            return duration;
        }
        Song Song = getCurrentPlayingSong();
        return Song.getDuration().intValue();
    }

    public boolean isPlaying() {
        return mWrapper.isPlaying();
    }

    private void SendToTopic() {
        if (mSubscription == null || mSubscription.isUnsubscribed()) {
            mSubscription = Observable.interval(1000, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            Intent intent = new Intent(ACTION_AUDIO_PROGRESS_CALLBACK);
                            intent.putExtra(FmPostDetailView.AUDIO_CACHE_PROGRESS, getBufferPercent());
                            int position = getSongPosition();
                            intent.putExtra(FmPostDetailView.AUDIO_PLAY_PROGRESS, position < 0 ? (int) aLong.longValue() * 1000 : position);
                            intent.putExtra(FmPostDetailView.AUDIO_PLAY_TIME, getSongDuration());
                            mContext.get().sendBroadcast(intent);
                        }
                    });
        }

    }

    //控制缓冲与播放进度的关系，防止坑爹服务器的流导致播放卡顿
    private void checkBuffer() {
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
                                    float position = getSongPosition();
                                    float duration = getSongDuration();
                                    int percent = (int) (position / duration * 100);
                                    int phasePercent = getBufferPercent() - percent;
                                    if (phasePercent > 20 && mSubscription != null && !mSubscription.isUnsubscribed()) {
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
                                }
                                stopChecking();
                                sendCachingBroadcast(false);
                            }
                        }
                    });
        }

    }

    private void sendCachingBroadcast(boolean isCaching) {
        isBuffing = isCaching;
        Intent intent = new Intent(AUDIO_IS_CACHING);
        intent.putExtra("Caching", isCaching);
        mContext.get().sendBroadcast(intent);
    }


    private void stopPushing() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    private void stopChecking() {
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
        sendCachingBroadcast(true);
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
    }

    @Override
    public void onCompleted() {
        try {
            playNextSong(getCurrentPlayingPos() + 1, true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext.get(), "播放音乐出错",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onError() {
        release();
        mWrapper = new MediaPlayerStateWrapper(this);
    }

    @Override
    public void onReset() {
        mPlayerCallBack.updatePlayerAndNotification(getCurrentPlayingSong(), false);
    }

    public void release() {
        resetState();
        mWrapper.release();
        mWrapper = null;
    }

    interface PlayerCallBack {
        void updatePlayerAndNotification(Song Song, boolean isPlaying);

        String getProxyUrl(long id);

        void stopCurrentConnection();
    }
}
