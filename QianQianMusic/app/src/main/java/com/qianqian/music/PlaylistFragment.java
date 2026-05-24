package com.qianqian.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qianqian.music.adapter.SongAdapter;
import com.qianqian.music.model.Song;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutScanning;
    private SongAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutScanning = view.findViewById(R.id.layoutScanning);

        adapter = new SongAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
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
        if (adapter != null) {
            adapter.setOnSongClickListener(listener);
        }
    }

    public void showScanning(boolean scanning) {
        if (layoutScanning != null) {
            layoutScanning.setVisibility(scanning ? View.VISIBLE : View.GONE);
        }
        if (layoutEmpty != null && !scanning) {
            layoutEmpty.setVisibility((adapter == null || adapter.getItemCount() == 0)
                    ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null && scanning) {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void updateEmptyState(List<Song> songs) {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility((songs == null || songs.isEmpty()) ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility((songs == null || songs.isEmpty()) ? View.GONE : View.VISIBLE);
        }
        if (layoutScanning != null) {
            layoutScanning.setVisibility(View.GONE);
        }
    }
}
