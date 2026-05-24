package com.qianqian.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.qianqian.music.model.Song;
import com.qianqian.music.service.MusicService;
import com.qianqian.music.util.LrcParser;
import com.qianqian.music.util.MusicScanner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private TextView tvSongCount;
    private ImageButton btnPlayPause;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnPlayMode;
    private ImageButton btnPlaylist;
    private LinearLayout layoutNowPlaying;
    private TextView tvNowPlayingTitle;
    private TextView tvNowPlayingArtist;
    private ImageButton btnNowPlayingPlayPause;

    private PlaylistFragment playlistFragment;
    private LyricsFragment lyricsFragment;

    private MusicService musicService;
    private boolean isBound = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Song> songs = new ArrayList<>();
    private boolean isSeekBarTracking = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            onServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupControls();
        checkPermissionAndScan();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvSongCount = findViewById(R.id.tvSongCount);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnPlayMode = findViewById(R.id.btnPlayMode);
        btnPlaylist = findViewById(R.id.btnPlaylist);
        layoutNowPlaying = findViewById(R.id.layoutNowPlaying);
        tvNowPlayingTitle = findViewById(R.id.tvNowPlayingTitle);
        tvNowPlayingArtist = findViewById(R.id.tvNowPlayingArtist);
        btnNowPlayingPlayPause = findViewById(R.id.btnNowPlayingPlayPause);
    }

    private void setupViewPager() {
        playlistFragment = new PlaylistFragment();
        lyricsFragment = new LyricsFragment();

        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? playlistFragment : lyricsFragment;
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? R.string.tab_playlist : R.string.tab_lyrics);
        }).attach();
    }

    private void setupControls() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNowPlayingPlayPause.setOnClickListener(v -> togglePlayPause());

        btnPrevious.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playPrevious();
                updateUI();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playNext();
                updateUI();
            }
        });

        btnPlayMode.setOnClickListener(v -> cyclePlayMode());

        btnPlaylist.setOnClickListener(v -> {
            viewPager.setCurrentItem(0, true);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isBound && musicService != null) {
                    musicService.seekTo(seekBar.getProgress());
                }
                isSeekBarTracking = false;
            }
        });

        playlistFragment.setOnSongClickListener((position, song) -> {
            if (isBound && musicService != null) {
                musicService.play(position);
                updateUI();
            }
        });
    }

    private void togglePlayPause() {
        if (isBound && musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pause();
            } else {
                if (musicService.getCurrentSong() == null && !songs.isEmpty()) {
                    musicService.play(0);
                } else {
                    musicService.resume();
                }
            }
            updateUI();
        }
    }

    private void cyclePlayMode() {
        if (!isBound || musicService == null) return;

        int mode = musicService.getPlayMode();
        mode = (mode + 1) % 4;
        musicService.setPlayMode(mode);
        updatePlayModeIcon(mode);
    }

    private void updatePlayModeIcon(int mode) {
        int iconRes;
        switch (mode) {
            case MusicService.PLAY_MODE_LOOP:
                iconRes = R.drawable.ic_play_mode_loop;
                break;
            case MusicService.PLAY_MODE_SHUFFLE:
                iconRes = R.drawable.ic_play_mode_shuffle;
                break;
            case MusicService.PLAY_MODE_SINGLE:
                iconRes = R.drawable.ic_play_mode_single;
                break;
            default:
                iconRes = R.drawable.ic_play_mode_sequence;
                break;
        }
        btnPlayMode.setImageResource(iconRes);
    }

    private void checkPermissionAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        scanMusic();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanMusic();
            }
        }
    }

    private void scanMusic() {
        playlistFragment.showScanning(true);

        new Thread(() -> {
            List<Song> result = MusicScanner.scanMusic(getContentResolver());
            runOnUiThread(() -> {
                songs = result;
                playlistFragment.setSongs(songs);
                playlistFragment.showScanning(false);
                tvSongCount.setText(getString(R.string.song_count, songs.size()));

                if (isBound && musicService != null) {
                    musicService.setPlaylist(songs);
                }
            });
        }).start();
    }

    private void onServiceBound() {
        if (musicService == null) return;

        if (!songs.isEmpty()) {
            musicService.setPlaylist(songs);
        }

        updatePlayModeIcon(musicService.getPlayMode());
        updateUI();
        startProgressUpdate();
    }

    private void updateUI() {
        if (!isBound || musicService == null) return;

        Song song = musicService.getCurrentSong();
        boolean playing = musicService.isPlaying();

        btnPlayPause.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play_large);
        btnNowPlayingPlayPause.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);

        if (song != null) {
            layoutNowPlaying.setVisibility(LinearLayout.VISIBLE);
            tvNowPlayingTitle.setText(song.getDisplayName());
            tvNowPlayingArtist.setText(song.getArtistDisplay());
            tvTotalTime.setText(song.getDurationFormatted());
            seekBar.setMax(musicService.getDuration());

            playlistFragment.setCurrentPlayingIndex(musicService.getCurrentIndex());
            lyricsFragment.setLrcLines(musicService.getCurrentLrcLines());
        }

        startServiceForeground();
    }

    private void startServiceForeground() {
        Intent intent = new Intent(this, MusicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound && musicService != null && !isSeekBarTracking) {
                int pos = musicService.getCurrentPosition();
                int dur = musicService.getDuration();
                if (dur > 0) {
                    seekBar.setMax(dur);
                    seekBar.setProgress(pos);
                    tvCurrentTime.setText(formatTime(pos));
                    tvTotalTime.setText(formatTime(dur));
                }

                List<LrcParser.LrcLine> lrcLines = musicService.getCurrentLrcLines();
                if (lrcLines != null && !lrcLines.isEmpty()) {
                    int line = LrcParser.findCurrentLine(lrcLines, pos);
                    lyricsFragment.setCurrentLine(line);
                }
            }
            handler.postDelayed(this, 300);
        }
    };

    private void startProgressUpdate() {
        handler.removeCallbacks(progressRunnable);
        handler.post(progressRunnable);
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacks(progressRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBound) {
            updateUI();
            startProgressUpdate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
