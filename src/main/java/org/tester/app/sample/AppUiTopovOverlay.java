/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tester.app.sample;

import org.onosproject.net.DeviceId;
import org.onosproject.ui.UiTopoOverlay;
import org.onosproject.ui.topo.ButtonId;
import org.onosproject.ui.topo.PropertyPanel;
import org.onosproject.ui.topo.TopoConstants.CoreButtons;
import org.onosproject.ui.topo.TopoConstants.Glyphs;

import java.util.HashMap;
import java.util.Map;

import static org.onosproject.ui.topo.TopoConstants.Properties.FLOWS;
import static org.onosproject.ui.topo.TopoConstants.Properties.INTENTS;
import static org.onosproject.ui.topo.TopoConstants.Properties.LATITUDE;
import static org.onosproject.ui.topo.TopoConstants.Properties.LONGITUDE;
import static org.onosproject.ui.topo.TopoConstants.Properties.TOPOLOGY_SSCS;
import static org.onosproject.ui.topo.TopoConstants.Properties.TUNNELS;
import static org.onosproject.ui.topo.TopoConstants.Properties.VERSION;

/**
 * Our topology overlay.
 */
public class AppUiTopovOverlay extends UiTopoOverlay {

    // NOTE: this must match the ID defined in sampleTopov.js
    private static final String OVERLAY_ID = "meowster-overlay";

    private static final String MY_TITLE = "My App Rocks!";
    private static final String MY_VERSION = "Beta-1.0.0042";
    private static final String MY_DEVICE_TITLE = "I changed the title";

    private static final ButtonId FOO_BUTTON = new ButtonId("foo");
    private static final ButtonId BAR_BUTTON = new ButtonId("bar");

    private static long counter = 0;

    public AppUiTopovOverlay() {
        super(OVERLAY_ID);
    }

    @Override
    public void modifySummary(PropertyPanel pp) {
        HashMap<String, String> elementStats = AppUiTopovMessageHandler.getCurrentElementStats();

        pp.title(MY_TITLE)
                .typeId(Glyphs.CROWN)
                .removeProps(
                        TOPOLOGY_SSCS,
                        INTENTS,
                        TUNNELS,
                        FLOWS,
                        VERSION
                )
                .addProp(VERSION, MY_VERSION)
                .addProp("counter: ", counter++);
        for (Map.Entry<String, String> entry : elementStats.entrySet())
            pp.addProp(entry.getKey(), entry.getValue());
    }

    @Override
    public void modifyDeviceDetails(PropertyPanel pp, DeviceId deviceId) {
        pp.title(MY_DEVICE_TITLE);
        pp.removeProps(LATITUDE, LONGITUDE);

        pp.addButton(FOO_BUTTON)
                .addButton(BAR_BUTTON);

        pp.removeButtons(CoreButtons.SHOW_PORT_VIEW)
                .removeButtons(CoreButtons.SHOW_GROUP_VIEW)
                .removeButtons(CoreButtons.SHOW_METER_VIEW);
    }
}
