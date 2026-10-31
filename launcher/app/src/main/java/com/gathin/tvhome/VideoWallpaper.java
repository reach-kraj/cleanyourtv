package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * wallpaper_video: a silent, looping video (wallpaper.mp4 in the theme folder) above the still wallpaper, which stays
 * underneath as the fallback. The player exists only while Home is on screen: stop() releases it, so the TV's video
 * decoder is free for the app or input that comes next, and start() resumes where it left off. The wallpaper's dim
 * and bottom fade are drawn over it by one static shade view, since they can't be painted into the frames.
 */
final class VideoWallpaper implements TextureView.SurfaceTextureListener {

    private final File file;
    private final TextureView video;
    private final View shade;
    private MediaPlayer player;
    private Surface surface;
    private boolean active;
    private int position;

    VideoWallpaper(Context ctx, FrameLayout root, File file, int dimPercent, int fadePercent) {
        this.file = file;
        video = new TextureView(ctx);
        video.setAlpha(0f);
        video.setSurfaceTextureListener(this);
        shade = new View(ctx);
        shade.setAlpha(0f);
        shade.setBackground(new Shade(dimPercent, fadePercent));
        // Right above the still wallpaper (child 0 when there is one), below everything else on Home.
        int at = root.getChildCount() > 0 && root.getChildAt(0) instanceof ImageView ? 1 : 0;
        FrameLayout.LayoutParams match = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        root.addView(video, at, match);
        root.addView(shade, at + 1, new FrameLayout.LayoutParams(match));
    }

    void start() {
        active = true;
        if (surface != null && player == null) open();
    }

    void stop() {
        active = false;
        release();
        video.animate().cancel();
        shade.animate().cancel();
        video.setAlpha(0f);
        shade.setAlpha(0f);
    }

    private void open() {
        MediaPlayer mp = new MediaPlayer();
        player = mp;
        try {
            mp.setDataSource(file.getPath());
            mp.setSurface(surface);
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
            mp.setOnPreparedListener(p -> {
                if (p != player) return;
                if (position > 0) p.seekTo(position);
                p.start();
            });
            mp.setOnInfoListener((p, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START && p == player) {
                    video.animate().alpha(1f).setDuration(600).start();
                    shade.animate().alpha(1f).setDuration(600).start();
                }
                return false;
            });
            mp.setOnErrorListener((p, what, extra) -> {
                if (p == player) release();   // the still wallpaper stays
                return true;
            });
            mp.prepareAsync();
        } catch (Exception e) {
            release();
        }
    }

    private void release() {
        if (player == null) return;
        try {
            position = player.getCurrentPosition();
        } catch (IllegalStateException ignored) {
        }
        player.release();
        player = null;
    }

    @Override public void onSurfaceTextureAvailable(@NonNull SurfaceTexture texture, int width, int height) {
        surface = new Surface(texture);
        if (active && player == null) open();
    }

    @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture texture) {
        release();
        if (surface != null) surface.release();
        surface = null;
        return true;
    }

    @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture texture, int width, int height) {
    }

    @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture texture) {
    }

    /** wallpaper_dim as a flat black, then wallpaper_bottom_fade from 40% of the height down (as BottomFade paints it). */
    private static final class Shade extends Drawable {
        private final int dim;
        private final int fade;
        private final Paint paint = new Paint();

        Shade(int dimPercent, int fadePercent) {
            dim = Math.max(0, Math.min(90, dimPercent)) * 255 / 100;
            fade = Math.max(0, Math.min(100, fadePercent)) * 255 / 100;
        }

        @Override protected void onBoundsChange(@NonNull Rect b) {
            paint.setShader(fade == 0 ? null : new LinearGradient(0, b.top + b.height() * 0.4f, 0, b.bottom,
                    new int[]{0, (fade * 45 / 100) << 24, fade << 24}, new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        }

        @Override public void draw(@NonNull Canvas c) {
            Rect b = getBounds();
            if (dim != 0) c.drawColor(dim << 24);
            if (fade != 0) c.drawRect(b.left, b.top + b.height() * 0.4f, b.right, b.bottom, paint);
        }

        @Override public void setAlpha(int alpha) {
        }

        @Override public void setColorFilter(ColorFilter filter) {
        }

        @Override public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
