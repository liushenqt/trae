package com.qianqian.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.qianqian.music.util.LrcParser;

import java.util.List;

public class LrcView extends View {

    private List<LrcParser.LrcLine> lrcLines;
    private int currentLine = -1;
    private Paint highlightPaint;
    private Paint normalPaint;
    private Paint timePaint;
    private float lineHeight = 60f;
    private float textSize = 16f;

    public LrcView(Context context) {
        super(context);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(0xFF00E5FF);
        highlightPaint.setTextSize(textSize * getResources().getDisplayMetrics().density);
        highlightPaint.setTextAlign(Paint.Align.CENTER);
        highlightPaint.setFakeBoldText(true);

        normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        normalPaint.setColor(0x99FFFFFF);
        normalPaint.setTextSize(textSize * getResources().getDisplayMetrics().density);
        normalPaint.setTextAlign(Paint.Align.CENTER);

        lineHeight = 50f * getResources().getDisplayMetrics().density;
    }

    public void setLrcLines(List<LrcParser.LrcLine> lines) {
        this.lrcLines = lines;
        this.currentLine = -1;
        invalidate();
    }

    public void setCurrentLine(int line) {
        if (this.currentLine != line) {
            this.currentLine = line;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lrcLines == null || lrcLines.isEmpty()) return;

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        if (currentLine < 0) currentLine = 0;
        if (currentLine >= lrcLines.size()) currentLine = lrcLines.size() - 1;

        float startY = centerY - currentLine * lineHeight;

        for (int i = 0; i < lrcLines.size(); i++) {
            float y = startY + i * lineHeight;
            if (y < -lineHeight || y > getHeight() + lineHeight) continue;

            Paint paint = (i == currentLine) ? highlightPaint : normalPaint;
            String text = lrcLines.get(i).text;
            if (text != null && !text.isEmpty()) {
                canvas.drawText(text, centerX, y, paint);
            }
        }
    }
}
