package org.tester.app.sample;

import org.onosproject.ui.UiTopoOverlay;

public class AppUiTopovOverlay extends UiTopoOverlay {
    private static final String OVERLAY_ID = "traffic-monitor-overlay";

    public AppUiTopovOverlay() {
        super(OVERLAY_ID);
    }
}
