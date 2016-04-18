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

import com.google.common.collect.HashMultimap;
import org.onosproject.incubator.net.PortStatisticsService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.onlab.osgi.ServiceDirectory;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.statistic.Load;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiConnection;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.topo.*;
import org.onosproject.ui.topo.NodeBadge.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.onosproject.net.DefaultEdgeLink.createEdgeLink;

/**
 * Skeletal ONOS UI Topology-Overlay message handler.
 */
public class AppUiTopovMessageHandler extends UiMessageHandler {
    private static final String SAMPLE_TOPOV_DISPLAY_START = "sampleTopovDisplayStart";
    private static final String SAMPLE_TOPOV_DISPLAY_UPDATE = "sampleTopovDisplayUpdate";
    private static final String SAMPLE_TOPOV_DISPLAY_STOP = "sampleTopovDisplayStop";

    private static final String ID = "id";
    private static final String MODE = "mode";

    private static final long UPDATE_PERIOD_MS = 200;

    private static final Link[] EMPTY_LINK_SET = new Link[0];

    private static Element currentlySelectedElement;

    private enum Mode {IDLE, MOUSE, LINK}

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static DeviceService deviceService;
    private static HostService hostService;
    private static LinkService linkService;
    private static PortStatisticsService portStatisticsService;

    private final Timer timer = new Timer("sample-overlay");
    private TimerTask demoTask = null;
    private Mode currentMode = Mode.IDLE;
    private Link[] linkSet = EMPTY_LINK_SET;
    private int linkIndex;

    @Override
    public void init(UiConnection connection, ServiceDirectory directory) {
        super.init(connection, directory);
        deviceService = directory.get(DeviceService.class);
        hostService = directory.get(HostService.class);
        linkService = directory.get(LinkService.class);
        portStatisticsService = directory.get(PortStatisticsService.class);
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new DisplayStartHandler(),
                new DisplayUpdateHandler(),
                new DisplayStopHandler()
        );
    }

    public static HashMap<String, String> getCurrentElementStats() {
        HashMap<String, String> elementStats = new HashMap<>();

        if (currentlySelectedElement != null && currentlySelectedElement instanceof Device) {
            DeviceId id = (DeviceId) currentlySelectedElement.id();
            Set<Link> links = linkService.getDeviceLinks(id);
            for (Link l : links) {
                long rate = portStatisticsService.load(l.src()).rate();
                elementStats.put("rate: ", TopoUtils.formatBitRate(rate));
            }
        } else if (currentlySelectedElement != null && currentlySelectedElement instanceof Host) {
            HostId id = (HostId) currentlySelectedElement.id();
            TrafficLinkMap linkMap = new TrafficLinkMap();
            Host h = hostService.getHost(id);
            linkMap.add(createEdgeLink(h, true));
            linkMap.add(createEdgeLink(h, false));

            for (TrafficLink l : linkMap.biLinks()) {
                Load egressSrc = portStatisticsService.load(l.one().src());
                Load egressDst = portStatisticsService.load(l.one().dst());

                if (egressSrc != null)
                    elementStats.put("source rate: ", TopoUtils.formatBitRate(egressSrc.rate()));
                if (egressDst != null)
                    elementStats.put("Destination rate: ", TopoUtils.formatBitRate(egressDst.rate()));
            }
        }
        return elementStats;
    }

    // Handler classes

    private final class DisplayStartHandler extends RequestHandler {
        public DisplayStartHandler() {
            super(SAMPLE_TOPOV_DISPLAY_START);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String mode = string(payload, MODE);

            log.debug("Start Display: mode [{}]", mode);
            clearState();
            clearAllHighlightedLinksInTopology();

            switch (mode) {
                case "mouse":
                    currentMode = Mode.MOUSE;
                    cancelTask();
                    sendMouseData();
                    break;

                case "link":
                    currentMode = Mode.LINK;
                    scheduleTask();
                    initLinkSet();
                    //sendLinkData();
                    sendAllPortTraffic();
                    break;

                default:
                    currentMode = Mode.IDLE;
                    cancelTask();
                    break;
            }
        }
    }

    private final class DisplayUpdateHandler extends RequestHandler {
        public DisplayUpdateHandler() {
            super(SAMPLE_TOPOV_DISPLAY_UPDATE);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String id = string(payload, ID);
            log.debug("Update Display: id [{}]", id);
            if (!Strings.isNullOrEmpty(id)) {
                updateForMode(id);
            } else {
                clearAllHighlightedLinksInTopology();
            }
        }
    }

    private final class DisplayStopHandler extends RequestHandler {
        public DisplayStopHandler() {
            super(SAMPLE_TOPOV_DISPLAY_STOP);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            log.debug("Stop Display");
            cancelTask();
            clearState();
            clearAllHighlightedLinksInTopology();
        }
    }

    private void clearState() {
        currentMode = Mode.IDLE;
        currentlySelectedElement = null;
        linkSet = EMPTY_LINK_SET;
    }

    private void updateForMode(String id) {
        log.debug("host service: {}", hostService);
        log.debug("device service: {}", deviceService);

        try {
            HostId hid = HostId.hostId(id);
            log.debug("host id {}", hid);
            currentlySelectedElement = hostService.getHost(hid);
            log.debug("host element {}", currentlySelectedElement);

        } catch (Exception e) {
            try {
                DeviceId did = DeviceId.deviceId(id);
                log.debug("device id {}", did);
                currentlySelectedElement = deviceService.getDevice(did);
                log.debug("device element {}", currentlySelectedElement);

            } catch (Exception e2) {
                log.debug("Unable to process ID [{}]", id);
                currentlySelectedElement = null;
            }
        }

        switch (currentMode) {
            case MOUSE:
                sendMouseData();
                break;

            case LINK:
                //sendLinkData();
                sendAllPortTraffic();
                break;

            default:
                break;
        }

    }

    private void clearAllHighlightedLinksInTopology() {
        sendHighlights(new Highlights());
    }

    private void sendHighlights(Highlights highlights) {
        sendMessage(TopoJson.highlightsMessage(highlights));
    }

    private void sendMouseData() {
        if (currentlySelectedElement != null && currentlySelectedElement instanceof Device) {
            DeviceId id = (DeviceId) currentlySelectedElement.id();
            Set<Link> links = linkService.getDeviceEgressLinks(id);
            Highlights highlights = fromLinks(links, id);
            addDeviceBadge(highlights, id, links.size());
            sendHighlights(highlights);
        }
    }

    private void addDeviceBadge(Highlights h, DeviceId devId, int n) {
        DeviceHighlight dh = new DeviceHighlight(devId.toString());
        dh.setBadge(createBadge(n));
        h.add(dh);
    }

    private NodeBadge createBadge(int n) {
        Status status = n > 3 ? Status.ERROR : Status.WARN;
        String noun = n > 3 ? "(critical)" : "(problematic)";
        String msg = "Egress links: " + n + " " + noun;
        return NodeBadge.number(status, n, msg);
    }

    private Highlights fromLinks(Set<Link> links, DeviceId devId) {
        DemoLinkMap linkMap = new DemoLinkMap();
        if (links != null) {
            log.debug("Processing {} links", links.size());
            links.forEach(linkMap::add);
        } else {
            log.debug("No egress links found for device {}", devId);
        }

        Highlights highlights = new Highlights();

        for (DemoLink dlink : linkMap.biLinks()) {
            dlink.makeImportant().setLabel("Yo!");
            highlights.add(dlink.highlight(null).addMod(new Mod("animated")));
        }
        return highlights;
    }

    private void initLinkSet() {
        Set<Link> links = new HashSet<>();
        for (Link link : linkService.getActiveLinks()) {
            links.add(link);
        }
        linkSet = links.toArray(new Link[links.size()]);
        linkIndex = 0;
        log.debug("initialized link set to {}", linkSet.length);
    }

    private void sendAllPortTraffic() {
        log.debug("sendAllPortTraffic");
        sendHighlights(buildHighlights());
    }

    private Highlights buildHighlights() {
        Highlights highlights = new Highlights();
        TrafficLinkMap linkMap = new TrafficLinkMap();
        linkService.getLinks().forEach(linkMap::add);
        hostService.getHosts().forEach(host -> {
            linkMap.add(createEdgeLink(host, true));
            linkMap.add(createEdgeLink(host, false));
        });
        for (TrafficLink link : linkMap.biLinks()) {
            attachPortLoad(link);
            // we only want to report on links deemed to have traffic
            if (link.hasTraffic()) {
                highlights.add(link.highlight(TrafficLink.StatsType.PORT_STATS));
            }
        }
        return highlights;
    }

    private void attachPortLoad(TrafficLink link) {
        // For bi-directional traffic links, use
        // the max link rate of either direction
        // (we choose 'one' since we know that is never null)
        Link one = link.one();
        Load egressSrc = portStatisticsService.load(one.src());
        Load egressDst = portStatisticsService.load(one.dst());
        link.addLoad(maxLoad(egressSrc, egressDst), 0);
    }

    private Load maxLoad(Load a, Load b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.rate() > b.rate() ? a : b;
    }

    private void sendLinkData() {
        DemoLinkMap linkMap = new DemoLinkMap();
        for (Link link : linkSet) {
            linkMap.add(link);
        }
        DemoLink dl = linkMap.add(linkSet[linkIndex]);
        dl.makeImportant().setLabel(Integer.toString(linkIndex));
        log.debug("sending link data (index {})", linkIndex);

        linkIndex += 1;
        if (linkIndex >= linkSet.length) {
            linkIndex = 0;
        }

        Highlights highlights = new Highlights();
        for (DemoLink dlink : linkMap.biLinks()) {
            highlights.add(dlink.highlight(null));
        }

        sendHighlights(highlights);
    }

    private synchronized void scheduleTask() {
        if (demoTask == null) {
            log.debug("Starting up demo task...");
            demoTask = new DisplayUpdateTask();
            timer.schedule(demoTask, UPDATE_PERIOD_MS, UPDATE_PERIOD_MS);
        } else {
            log.debug("(demo task already running");
        }
    }

    private synchronized void cancelTask() {
        if (demoTask != null) {
            demoTask.cancel();
            demoTask = null;
        }
    }

    private class DisplayUpdateTask extends TimerTask {
        @Override
        public void run() {
            try {
                switch (currentMode) {
                    case LINK:
                        sendAllPortTraffic();
                        //sendLinkData();
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                log.warn("Unable to process demo task: {}", e.getMessage());
                log.debug("Oops", e);
            }
        }
    }
}