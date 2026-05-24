package com.qianqian.music.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qianqian.music.R;
import com.qianqian.music.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs = new ArrayList<>();
    private int currentPlayingIndex = -1;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(int position, Song song);
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public void setCurrentPlayingIndex(int index) {
        int oldIndex = this.currentPlayingIndex;
        this.currentPlayingIndex = index;
        if (oldIndex >= 0) {
            notifyItemChanged(oldIndex);
        }
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.tvTitle.setText(song.getDisplayName());
        holder.tvArtist.setText(song.getArtistDisplay());
        holder.tvDuration.setText(song.getDurationFormatted());

        boolean isPlaying = position == currentPlayingIndex;
        holder.ivPlaying.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        holder.tvTitle.setTextColor(isPlaying ? 0xFF00E5FF : 0xFFFFFFFF);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        ImageView ivAlbumArt;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;
        ImageView ivPlaying;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.ivAlbumArt);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            ivPlaying = itemView.findViewById(R.id.ivPlaying);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSongClick(pos, songs.get(pos));
                }
            });
        }
    }
}
