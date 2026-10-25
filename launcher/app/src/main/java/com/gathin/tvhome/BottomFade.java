package com.gathin.tvhome;

/**
 * wallpaper_bottom_fade: a black gradient over the lower part of each wallpaper picture, painted in once at decode
 * (WallpaperPlayer) like wallpaper_dim, so it costs nothing per frame. Clear down to START, then deepening to the
 * strength at the bottom edge.
 */
final class BottomFade {
    /** Where the fade begins, as a share of the picture's height from the top. */
    static final float START = 0.4f;
    /** Gradient stops from START to the bottom edge, matching colors(). */
    static final float[] STOPS = {0f, 0.45f, 1f};

    private BottomFade() {
    }

    /** ARGB blacks for STOPS at this strength (0-100, clamped), or null when off. */
    static int[] colors(int percent) {
        int alpha = Math.max(0, Math.min(100, percent)) * 255 / 100;
        if (alpha == 0) return null;
        return new int[]{0, (alpha * 45 / 100) << 24, alpha << 24};
    }
}
