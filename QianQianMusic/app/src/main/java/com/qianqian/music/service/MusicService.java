package com.qianqian.music.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.qianqian.music.MainActivity;
import com.qianqian.music.R;
import com.qianqian.music.model.Song;
import com.qianqian.music.util.LrcParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {

    public static final String ACTION_PLAY_PAUSE = "com.qianqian.music.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.qianqian.music.NEXT";
    public static final String ACTION_PREVIOUS = "com.qianqian.music.PREVIOUS";
    public static final String ACTION_STOP = "com.qianqian.music.STOP";
    public static final String ACTION_SEEK = "com.qianqian.music.SEEK";

    public static final int PLAY_MODE_SEQUENCE = 0;
    public static final int PLAY_MODE_LOOP = 1;
    public static final int PLAY_MODE_SHUFFLE = 2;
    public static final int PLAY_MODE_SINGLE = 3;

    private static final String CHANNEL_ID = "qianqian_music_channel";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private Handler handler;
    private List<Song> playlist = new ArrayList<>();
    private List<LrcParser.LrcLine> currentLrcLines = new ArrayList<>();
    private int currentIndex = -1;
    private int playMode = PLAY_MODE_SEQUENCE;
    private boolean isPrepared = false;

    private final IBinder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        initMediaPlayer();
        initMediaSession();
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, new IntentFilter[]{
                    new IntentFilter(ACTION_PLAY_PAUSE),
                    new IntentFilter(ACTION_NEXT),
                    new IntentFilter(ACTION_PREVIOUS),
                    new IntentFilter(ACTION_STOP)
            }, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificationReceiver, new IntentFilter[]{
                    new IntentFilter(ACTION_PLAY_PAUSE),
                    new IntentFilter(ACTION_NEXT),
                    new IntentFilter(ACTION_PREVIOUS),
                    new IntentFilter(ACTION_STOP)
            });
        }
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            mp.start();
            updateNotification();
            updateMediaSessionPlaybackState();
            startProgressUpdate();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            playNext();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            isPrepared = false;
            playNext();
            return true;
        });

        mediaPlayer.setOnSeekCompleteListener(mp -> {
            updateMediaSessionPlaybackState();
        });
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "QianQianMusic");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                resume();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                playPrevious();
            }

            @Override
            public void onSeekTo(long pos) {
                seekTo((int) pos);
            }

            @Override
            public void onStop() {
                stopSelf();
            }
        });
        mediaSession.setActive(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "千千静听音乐播放",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("音乐播放控制通知");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case ACTION_PLAY_PAUSE:
                    if (isPlaying()) pause();
                    else resume();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
                case ACTION_PREVIOUS:
                    playPrevious();
                    break;
                case ACTION_STOP:
                    stopSelf();
                    break;
            }
        }
    };

    private Notification buildNotification() {
        Song song = getCurrentSong();
        String title = song != null ? song.getDisplayName() : "千千静听";
        String artist = song != null ? song.getArtistDisplay() : "";

        Intent contentIntent = new Intent(this, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent prevPendingIntent = getPendingIntent(ACTION_PREVIOUS);
        PendingIntent playPausePendingIntent = getPendingIntent(ACTION_PLAY_PAUSE);
        PendingIntent nextPendingIntent = getPendingIntent(ACTION_NEXT);

        int playPauseIcon = isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this, PlaybackStateCompat.ACTION_STOP))
                .addAction(R.drawable.ic_previous, "上一首", prevPendingIntent)
                .addAction(playPauseIcon, isPlaying() ? "暂停" : "播放", playPausePendingIntent)
                .addAction(R.drawable.ic_next, "下一首", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying());

        return builder.build();
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(action);
        return PendingIntent.getBroadcast(this, action.hashCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void updateNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private void updateMediaSessionMetadata() {
        Song song = getCurrentSong();
        if (song == null) return;

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getDisplayName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtistDisplay())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbumDisplay())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration());

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void updateMediaSessionPlaybackState() {
        int state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        long position = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO |
                        PlaybackStateCompat.ACTION_STOP)
                .setState(state, position, 1.0f);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying()) {
                updateMediaSessionPlaybackState();
            }
            handler.postDelayed(this, 500);
        }
    };

    private void startProgressUpdate() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    public void setPlaylist(List<Song> songs) {
        this.playlist = new ArrayList<>(songs);
    }

    public List<Song> getPlaylist() {
        return playlist;
    }

    public void play(int index) {
        if (index < 0 || index >= playlist.size()) return;

        currentIndex = index;
        Song song = playlist.get(index);

        try {
            mediaPlayer.reset();
            isPrepared = false;
            mediaPlayer.setDataSource(this, song.getUri());
            mediaPlayer.prepareAsync();
            updateMediaSessionMetadata();
            loadLrc(song);
        } catch (IOException e) {
            e.printStackTrace();
            playNext();
        }
    }

    private void loadLrc(Song song) {
        currentLrcLines.clear();
        if (song.getFilePath() != null) {
            java.io.File lrcFile = LrcParser.findLrcFile(song.getFilePath());
            if (lrcFile != null) {
                currentLrcLines = LrcParser.parseLrc(lrcFile);
            }
        }
    }

    public void resume() {
        if (mediaPlayer != null && isPrepared && !isPlaying()) {
            mediaPlayer.start();
            updateNotification();
            updateMediaSessionPlaybackState();
            startProgressUpdate();
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
            updateMediaSessionPlaybackState();
            handler.removeCallbacks(progressRunnable);
        }
    }

    public void playNext() {
        if (playlist.isEmpty()) return;

        int nextIndex;
        switch (playMode) {
            case PLAY_MODE_SHUFFLE:
                Random random = new Random();
                nextIndex = random.nextInt(playlist.size());
                break;
            case PLAY_MODE_SINGLE:
                nextIndex = currentIndex;
                break;
            case PLAY_MODE_LOOP:
                nextIndex = (currentIndex + 1) % playlist.size();
                break;
            default:
                nextIndex = currentIndex + 1;
                if (nextIndex >= playlist.size()) {
                    nextIndex = -1;
                }
                break;
        }

        if (nextIndex >= 0) {
            play(nextIndex);
        }
    }

    public void playPrevious() {
        if (playlist.isEmpty()) return;

        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
            play(currentIndex);
            return;
        }

        int prevIndex;
        switch (playMode) {
            case PLAY_MODE_SHUFFLE:
                Random random = new Random();
                prevIndex = random.nextInt(playlist.size());
                break;
            default:
                prevIndex = currentIndex - 1;
                if (prevIndex < 0) {
                    prevIndex = playlist.size() - 1;
                }
                break;
        }

        play(prevIndex);
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int mode) {
        this.playMode = mode;
    }

    public List<LrcParser.LrcLine> getCurrentLrcLines() {
        return currentLrcLines;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        try {
            unregisterReceiver(notificationReceiver);
        } catch (Exception ignored) {}
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.cancel(NOTIFICATION_ID);
        }
    }
}
