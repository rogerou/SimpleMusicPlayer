package com.rogerou.simpemusicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven on 2016/7/13.
 * <p
 * 播放器
 */
public class FMPlayer implements MediaPlayer.OnBufferingUpdateListener {
    private Context context;
    private MusicService service;
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> currentPlayingSongs;
    private int currentPlayingPos = -1;
    int bufferPercent;

    public FMPlayer(Context context, MusicService service) {
        this.context = context;
        this.service = service;
        this.mediaPlayer = new MediaPlayer();
        currentPlayingSongs = new ArrayList<>();
        mediaPlayer.setOnBufferingUpdateListener(this);
    }

//    public void playAlbumSongs(long albumId) throws IOException {
//        playAlbumSongs(albumId, 0);
//    }

    public void playListSongs(List<Song> songList, final int startSongPos) throws IOException {
        currentPlayingSongs.clear();
        currentPlayingSongs.addAll(songList);
        stopPlayer();
        mediaPlayer.reset();
        mediaPlayer.setDataSource(currentPlayingSongs.get(startSongPos).getUrl());
        mediaPlayer.prepare();
        mediaPlayer.start();
        setPlayingPos(startSongPos);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                try {
                    playNextSong(startSongPos + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "播放音乐出错",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setCurrentPlayingSongs(Song currentPlayingSong) {
        ArrayList<Song> newSongsList = new ArrayList<>();
        newSongsList.add(currentPlayingSong);
        setCurrentPlayingSongs(newSongsList);
    }

    public void stopPlayer() {
        setPlayingPos(-1);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }


//    public void playPlaylist(int id, final int currentPlayingPos) throws IOException {
//        stopPlayer();
//        setCurrentPlayingSongs(dbHelper.getAllPlaylistSongs(id));
//        configurePlayer(currentPlayingPos);
//    }

//    public void playArtistSongs(String name, int pos) throws IOException {
//        stopPlayer();
//        ArrayList<Song> songs = ListSongs.getSongsListOfArtist(context, name);
//        setCurrentPlayingSongs(songs);
//        configurePlayer(pos);
//    }

//    public void playAllSongs(long songId) throws IOException {
//        stopPlayer();
//        setCurrentPlayingSongs(ListSongs.getSongList(context));
//        final int songToPlayPos = findForASongInArrayList(songId);
//        configurePlayer(songToPlayPos);
//    }

    public void playOrStop(MusicNotificationManager notifactionHandler) {
//        boolean state;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
//            state = false;
            service.stopForeground(false);
            notifactionHandler.setNotificationPlayer(true);
        } else {
            mediaPlayer.start();
//            state = true;
            notifactionHandler.setNotificationPlayer(false);
        }
//        notifactionHandler.updateNotificationView();
//        notifactionHandler.changeNotificationDetails(getCurrentPlayingSong().getName(),
//                getCurrentPlayingSong().getArtist(), getCurrentPlayingSong().getAlbumId(), state);
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

    public void configurePlayer(final int pos) throws IOException {
        stopPlayer();
        setPlayingPos(pos);
        mediaPlayer.reset();
        mediaPlayer.setDataSource(currentPlayingSongs
                .get(pos).getUrl());
        mediaPlayer.prepare();
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                try {
                    playNextSong(getNextSongPosition(pos));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void playNextSong(final int nextSongPos) throws IOException {
        if (nextSongPos < currentPlayingSongs.size()) {
            //If there is some song available play it or repeat is enabled.
            configurePlayer(nextSongPos);
        } else {
            configurePlayer(0);
        }
        service.updatePlayerAndNotification(PlayControlFragment.PLAYING, currentPlayingSongs.get(currentPlayingPos));
    }

    public void playPrevSong(final int prevSongPos) throws IOException {
        if ((mediaPlayer.getCurrentPosition() / 1000) <= 2) {
            int position;
         /*   if (prevSongPos == -1 && preferenceHandler.isRepeatAllEnabled()) {
                //If song pos is more than 0
                position = getCurrentPlayingSongs().size() - 1;
            } else */
            if (prevSongPos == -1) {
                position = 0;
            } else {
                position = prevSongPos;
            }
            final int pos = position;
            stopPlayer();
            setPlayingPos(pos);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentPlayingSongs.get(pos).getUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    try {
                        playNextSong(getNextSongPosition(prevSongPos));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "播放音乐失败",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            mediaPlayer.seekTo(0);
        }
    }


    public void playSingleSong(Song song) throws IOException {
        setPlayingPos(0);
        stopPlayer();
        mediaPlayer.reset();
        mediaPlayer.setDataSource(song.getUrl());
        mediaPlayer.prepare();
        setCurrentPlayingSongs(song);
        mediaPlayer.start();
    }

    public void setPlayingPos(int pos) {
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

    public void setCurrentPlayingSongs(ArrayList<Song> currentPlayingSongs) {
        this.currentPlayingSongs.clear();
        this.currentPlayingSongs = currentPlayingSongs;
    }

    public Song getCurrentPlayingSong() {
        return currentPlayingSongs.get(currentPlayingPos);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public Song getSongFromId(final long id) {
        Song song = null;
        for (Song song1 : currentPlayingSongs) {
            if (song1.getSongId() == id) {
                song = song1;
            }
        }
        return song;
    }

    public int getCurrentPlayingPos() {
        return currentPlayingPos;
    }

    public void seek(int seek) {
        mediaPlayer.seekTo(seek);
    }

    public void addSongToQueue(Song s) {
        currentPlayingSongs.add(s);
    }

    public void setVolumn(float left, float riht) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(left, riht);
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        bufferPercent = percent;
    }
    
}
