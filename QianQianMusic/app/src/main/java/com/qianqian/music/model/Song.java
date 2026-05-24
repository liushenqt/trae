package com.qianqian.music.model;

import android.net.Uri;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private Uri uri;
    private String filePath;
    private long albumId;

    public Song() {}

    public Song(long id, String title, String artist, String album, long duration, Uri uri, String filePath, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.uri = uri;
        this.filePath = filePath;
        this.albumId = albumId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public Uri getUri() { return uri; }
    public void setUri(Uri uri) { this.uri = uri; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getAlbumId() { return albumId; }
    public void setAlbumId(long albumId) { this.albumId = albumId; }

    public String getDisplayName() {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        if (filePath != null) {
            int lastSlash = filePath.lastIndexOf('/');
            int lastDot = filePath.lastIndexOf('.');
            if (lastSlash >= 0 && lastDot > lastSlash) {
                return filePath.substring(lastSlash + 1, lastDot);
            }
        }
        return "未知歌曲";
    }

    public String getArtistDisplay() {
        return (artist == null || artist.isEmpty() || "<unknown>".equals(artist)) ? "未知艺术家" : artist;
    }

    public String getAlbumDisplay() {
        return (album == null || album.isEmpty() || "<unknown>".equals(album)) ? "未知专辑" : album;
    }

    public String getDurationFormatted() {
        int totalSeconds = (int) (duration / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
