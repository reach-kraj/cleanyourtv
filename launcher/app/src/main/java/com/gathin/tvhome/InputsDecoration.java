package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

/** hdmi_tiles: a gap above the row of HDMI tiles at the end of the grid, with an "INPUTS" label and a thin line. */
public class InputsDecoration extends RecyclerView.ItemDecoration {

    private final AppAdapter adapter;
    private final int columns;
    private final int gap;
    private final String title;
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint();

    InputsDecoration(Context ctx, AppAdapter adapter, int columns, float density) {
        this.adapter = adapter;
        this.columns = columns;
        this.gap = Math.round(44 * density);
        this.title = ctx.getString(R.string.inputs).toUpperCase(Locale.getDefault());
        text.setColor(0x99FFFFFF);
        text.setTextSize(13 * density);
        text.setLetterSpacing(0.18f);
        text.setTypeface(UiFont.get(ctx, R.string.font_medium));
        line.setColor(0x33FFFFFF);
        line.setStrokeWidth(Math.max(1f, density));
    }

    @Override
    public void getItemOffsets(@NonNull Rect out, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int first = adapter.firstInputPosition();
        int pos = parent.getChildAdapterPosition(view);
        if (first >= 0 && pos >= first && pos < first + columns) out.top = gap;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int first = adapter.firstInputPosition();
        if (first < 0) return;
        RecyclerView.ViewHolder vh = parent.findViewHolderForAdapterPosition(first);
        if (vh == null) return;
        View card = vh.itemView.findViewById(R.id.card);
        float left = vh.itemView.getLeft() + (card != null ? card.getLeft() : 0);
        float right = parent.getWidth() - left;
        float baseline = vh.itemView.getTop() - gap * 0.42f;
        c.drawText(title, left, baseline, text);
        float lineY = baseline - text.getTextSize() * 0.35f;
        float lineStart = left + text.measureText(title) + gap * 0.35f;
        if (lineStart < right) c.drawLine(lineStart, lineY, right, lineY, line);
    }
}
