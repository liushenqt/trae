package com.qianqian.music;

import android.view.View;
import android.widget.TextView;

import com.qianqian.music.util.LrcParser;
import com.qianqian.music.view.LrcView;

import java.util.List;

public class LyricsFragment {

    private LrcView lrcView;
    private TextView tvNoLrc;

    public LyricsFragment(View view) {
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
