package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class BottomFadeTest {
    @Test public void zeroIsOff() {
        assertNull(BottomFade.colors(0));
        assertNull(BottomFade.colors(-5));
    }

    @Test public void clearAtTheTopDeepestAtTheBottom() {
        // 100: opaque black at the edge, a bit under half way at the middle stop.
        assertArrayEquals(new int[]{0, 114 << 24, 0xFF000000}, BottomFade.colors(100));
        assertArrayEquals(new int[]{0, 114 << 24, 0xFF000000}, BottomFade.colors(150));
        // 85 (tried on a BRAVIA BF1): alpha 216 at the edge.
        assertArrayEquals(new int[]{0, 97 << 24, 216 << 24}, BottomFade.colors(85));
    }
}
