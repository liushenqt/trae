package com.qianqian.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qianqian.music.util.LrcParser;
import com.qianqian.music.view.LrcView;

import java.util.List;

public class LyricsFragment extends Fragment {

    private LrcView lrcView;
    private TextView tvNoLrc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lyrics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lrcView = view.findViewById(R.id.lrcView);
        tvNoLrc = view.findViewById(R.id.tvNoLrc);
    }

    public void setLrcLines(List<LrcParser.LrcLine> lines) {
        if (lrcView != null) {
            lrcView.setLrcLines(lines);
        }
        if (tvNoLrc != null) {
            tvNoLrc.setVisibility((lines == null || lines.isEmpty()) ? View.VISIBLE : View.GONE);
        }
        if (lrcView != null) {
            lrcView.setVisibility((lines == null || lines.isEmpty()) ? View.GONE : View.VISIBLE);
        }
    }

    public void setCurrentLine(int line) {
        if (lrcView != null) {
            lrcView.setCurrentLine(line);
        }
    }
}
