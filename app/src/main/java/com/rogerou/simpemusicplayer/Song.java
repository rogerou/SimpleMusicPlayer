package com.rogerou.simpemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rogerou on 2016/7/13.
 */
public class Song implements Parcelable {

    private long songId;

    private String song_name;

    private long duaration;

    private String url;

    private String author_id;

    private String author_name;

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSong_album() {
        return song_album;
    }

    public void setSong_album(String song_album) {
        this.song_album = song_album;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDuaration() {
        return duaration;
    }

    public void setDuaration(long duaration) {
        this.duaration = duaration;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    private String song_album;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.songId);
        dest.writeString(this.song_name);
        dest.writeLong(this.duaration);
        dest.writeString(this.url);
        dest.writeString(this.author_id);
        dest.writeString(this.author_name);
        dest.writeString(this.song_album);
    }

    public Song() {
    }

    protected Song(Parcel in) {
        this.songId = in.readLong();
        this.song_name = in.readString();
        this.duaration = in.readLong();
        this.url = in.readString();
        this.author_id = in.readString();
        this.author_name = in.readString();
        this.song_album = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
