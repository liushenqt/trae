package com.qianqian.music;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.qianqian.music.adapter.SongAdapter;
import com.qianqian.music.model.Song;

import java.util.List;

public class PlaylistFragment {

    private ListView listView;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutScanning;
    private SongAdapter adapter;

    public PlaylistFragment(View view) {
        listView = view.findViewById(R.id.listView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutScanning = view.findViewById(R.id.layoutScanning);

        adapter = new SongAdapter(view.getContext());
        listView.setAdapter(adapter);
    }

    public void setSongs(List<Song> songs) {
        if (adapter != null) {
            adapter.setSongs(songs);
        }
        updateEmptyState(songs);
    }

    public void setCurrentPlayingIndex(int index) {
        if (adapter != null) {
            adapter.setCurrentPlayingIndex(index);
        }
    }

    public void setOnSongClickListener(SongAdapter.OnSongClickListener listener) {
        if (listView != null) {
            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (listener != null && adapter != null) {
                    listener.onSongClick(position, (Song) adapter.getItem(position));
                }
            });
        }
    }

    public void showScanning(boolean scanning) {
        if (layoutScanning != null) {
            layoutScanning.setVisibility(scanning ? View.VISIBLE : View.GONE);
        }
        if (layoutEmpty != null && !scanning) {
            layoutEmpty.setVisibility((adapter == null || adapter.getCount() == 0)
                    ? View.VISIBLE : View.GONE);
        }
        if (listView != null && scanning) {
            listView.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState(List<Song> songs) {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility((songs == null || songs.isEmpty()) ? View.VISIBLE : View.GONE);
        }
        if (listView != null) {
            listView.setVisibility((songs == null || songs.isEmpty()) ? View.GONE : View.VISIBLE);
        }
        if (layoutScanning != null) {
            layoutScanning.setVisibility(View.GONE);
        }
    }
}
