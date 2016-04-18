package org.tester.app.sample;

import org.onosproject.net.Link;
import org.onosproject.net.LinkKey;
import org.onosproject.ui.topo.BiLinkMap;

/**
 * Collection of {@link TrafficLink}s.
 */
public class TrafficLinkMap extends BiLinkMap<TrafficLink> {

    @Override
    public TrafficLink create(LinkKey key, Link link) {
        return new TrafficLink(key, link);
    }
}

