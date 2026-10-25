package com.gathin.tvhome;

import android.content.Context;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The TV's HDMI ports in port order, for hdmi_tiles and inputs_pill_direct. CEC child inputs are left out (the
 * port stands for them), and so are ports hidden in Settings > Inputs.
 */
final class HdmiInputs {

    static final class Port {
        final String id;
        final String label;
        /** INPUT_STATE_CONNECTED, and only when the TV does not report every port that way (see list()). */
        final boolean connected;

        Port(String id, String label, boolean connected) {
            this.id = id;
            this.label = label;
            this.connected = connected;
        }
    }

    private HdmiInputs() {
    }

    static List<Port> list(Context ctx) {
        List<Port> ports = new ArrayList<>();
        TvInputManager tim = (TvInputManager) ctx.getSystemService(Context.TV_INPUT_SERVICE);
        if (tim == null) return ports;
        List<TvInputInfo> inputs;
        try {
            inputs = tim.getTvInputList();
        } catch (RuntimeException e) {
            return ports;
        }
        Set<String> ids = new HashSet<>();
        for (TvInputInfo info : inputs) ids.add(info.getId());
        List<TvInputInfo> hdmi = new ArrayList<>();
        for (TvInputInfo info : inputs) {
            if (info.getType() != TvInputInfo.TYPE_HDMI || !info.isPassthroughInput() || info.isHidden(ctx)) continue;
            if (info.getParentId() != null && ids.contains(info.getParentId())) continue;
            hdmi.add(info);
        }
        List<String> labels = new ArrayList<>();
        for (TvInputInfo info : hdmi) {
            CharSequence l = info.loadLabel(ctx);
            String label = l == null ? info.getId() : l.toString();
            if (!label.toUpperCase(Locale.ROOT).contains("HDMI")) label = "HDMI " + label;
            labels.add(label);
        }
        // "HDMI 2" before "HDMI 10": shorter labels first, then alphabetical.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < hdmi.size(); i++) order.add(i);
        Collections.sort(order, (a, b) -> {
            String la = labels.get(a), lb = labels.get(b);
            return la.length() != lb.length() ? Integer.compare(la.length(), lb.length()) : la.compareToIgnoreCase(lb);
        });
        // Some TVs report every port as connected whatever is plugged in (showInputs() drops the state then too).
        int connected = 0;
        boolean[] state = new boolean[hdmi.size()];
        for (int i = 0; i < hdmi.size(); i++) {
            state[i] = tim.getInputState(hdmi.get(i).getId()) == TvInputManager.INPUT_STATE_CONNECTED;
            if (state[i]) connected++;
        }
        boolean bogus = hdmi.size() > 1 && connected == hdmi.size();
        for (int i : order) ports.add(new Port(hdmi.get(i).getId(), labels.get(i), state[i] && !bogus));
        return ports;
    }

    /** The lowest-numbered connected port, or null when none is (or the TV cannot tell). */
    static String firstConnected(Context ctx) {
        for (Port p : list(ctx)) if (p.connected) return p.id;
        return null;
    }
}
