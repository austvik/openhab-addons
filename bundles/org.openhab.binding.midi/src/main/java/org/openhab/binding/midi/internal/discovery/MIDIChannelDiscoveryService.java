/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.midi.internal.discovery;

import static org.openhab.binding.midi.internal.MIDIBindingConstants.BACKGROUND_DISCOVERY;
import static org.openhab.binding.midi.internal.MIDIBindingConstants.SEARCH_TIME;
import static org.openhab.binding.midi.internal.MIDIBindingConstants.THING_TYPE_MIDI_CHANNEL;

import java.util.Set;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.midi.internal.handlers.MIDIDeviceHandler;
import org.openhab.binding.midi.internal.handlers.MIDIEventListener;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BridgeHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers MIDI channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MIDIChannelDiscoveryService.class)
@NonNullByDefault
public class MIDIChannelDiscoveryService extends AbstractThingHandlerDiscoveryService<MIDIDeviceHandler>
        implements MIDIEventListener {

    private final Logger logger = LoggerFactory.getLogger(MIDIChannelDiscoveryService.class);

    public MIDIChannelDiscoveryService() {
        super(MIDIDeviceHandler.class, Set.of(THING_TYPE_MIDI_CHANNEL), (int) SEARCH_TIME.getSeconds(),
                BACKGROUND_DISCOVERY);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start AirGradient background discovery");
        getDeviceHandler().addEventListener(this);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stopping AirGradient background discovery");
        getDeviceHandler().addEventListener(this);
    }

    @Override
    protected void startScan() {
        logger.debug("Not able to scan on demand. Send MIDI messages to the device and channels will be discovered.");
    }

    private MIDIDeviceHandler getDeviceHandler() {
        return (@NonNull MIDIDeviceHandler) getThingHandler();
    }

    @Override
    public void receivedMessage(MidiMessage message, String messageString) {
        if (message instanceof ShortMessage msg) {
            int channel = msg.getChannel() + 1;

            BridgeHandler bridge = getDeviceHandler().getThing().getHandler();
            if (bridge == null) {
                logger.debug("Missing bridge, can't discover sensors for unknown bridge.");
                return;
            }

            ThingUID bridgeUid = bridge.getThing().getUID();
            ThingUID thingId = new ThingUID(THING_TYPE_MIDI_CHANNEL, bridgeUid, "channel-" + channel);

            DiscoveryResult result = DiscoveryResultBuilder.create(thingId).withProperty("channel", channel)
                    .withBridge(bridgeUid).withLabel(getDeviceHandler().getDeviceId() + " Channel " + channel)
                    .withRepresentationProperty("channel").build();

            thingDiscovered(result);
        }
    }
}
