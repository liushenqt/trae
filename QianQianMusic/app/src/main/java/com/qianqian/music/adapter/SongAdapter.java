package com.qianqian.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianqian.music.R;
import com.qianqian.music.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends BaseAdapter {

    private List<Song> songs = new ArrayList<>();
    private int currentPlayingIndex = -1;
    private OnSongClickListener listener;
    private Context context;

    public interface OnSongClickListener {
        void onSongClick(int position, Song song);
    }

    public SongAdapter(Context context) {
        this.context = context;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public void setCurrentPlayingIndex(int index) {
        this.currentPlayingIndex = index;
        notifyDataSetChanged();
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_song, parent, false);
            holder = new ViewHolder();
            holder.ivAlbumArt = convertView.findViewById(R.id.ivAlbumArt);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvArtist = convertView.findViewById(R.id.tvArtist);
            holder.tvDuration = convertView.findViewById(R.id.tvDuration);
            holder.ivPlaying = convertView.findViewById(R.id.ivPlaying);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song song = songs.get(position);
        holder.tvTitle.setText(song.getDisplayName());
        holder.tvArtist.setText(song.getArtistDisplay());
        holder.tvDuration.setText(song.getDurationFormatted());

        boolean isPlaying = position == currentPlayingIndex;
        holder.ivPlaying.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        holder.tvTitle.setTextColor(isPlaying ? 0xFF00E5FF : 0xFFFFFFFF);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivAlbumArt;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;
        ImageView ivPlaying;
    }
}
