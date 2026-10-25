package com.gathin.tvhome;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HdmiInputsTest {
    @Test public void aBarePortNumberGetsTheHdmiPrefix() {
        assertEquals("HDMI 2", HdmiInputs.label("2"));
        assertEquals("HDMI 1", HdmiInputs.label("HDMI 1"));
        assertEquals("hdmi 3 (eARC)", HdmiInputs.label("hdmi 3 (eARC)"));
    }

    @Test public void portsSortByNumberNotAlphabetically() {
        // MediaTek lists them as HW2..HW5 in any order; "HDMI 10" must not come before "HDMI 2".
        assertEquals(Arrays.asList(2, 0, 3, 1),
                HdmiInputs.order(Arrays.asList("HDMI 2", "HDMI 10", "HDMI 1", "HDMI 3")));
    }

    @Test public void theHdmiRowStartsOnARowOfItsOwn() {
        assertEquals(0, HdmiInputs.rowPadding(12, 4));
        assertEquals(1, HdmiInputs.rowPadding(11, 4));
        assertEquals(3, HdmiInputs.rowPadding(9, 4));
        assertEquals(4, HdmiInputs.rowPadding(2, 6));
    }

    @Test public void everyPortConnectedMeansTheStateIsUnknown() {
        // Sony and some others report all four ports as connected with one device plugged in.
        assertFalse(HdmiInputs.trustState(4, 4));
        assertTrue(HdmiInputs.trustState(4, 1));
        assertTrue(HdmiInputs.trustState(4, 0));
        // A TV with a single port can be really connected.
        assertTrue(HdmiInputs.trustState(1, 1));
    }
}
