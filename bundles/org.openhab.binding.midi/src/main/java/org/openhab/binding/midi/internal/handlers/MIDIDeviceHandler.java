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
package org.openhab.binding.midi.internal.handlers;

import static org.openhab.binding.midi.internal.MIDIBindingConstants.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.midi.internal.MIDIDeviceConfiguration;
import org.openhab.binding.midi.internal.discovery.MIDIChannelDiscoveryService;
import org.openhab.binding.midi.internal.midi.MidiDeviceException;
import org.openhab.binding.midi.internal.midi.TwoWayMIDIDevice;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MIDIDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MIDIDeviceHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(MIDIDeviceHandler.class);
    private final Set<MIDIEventListener> eventListeners = new HashSet<>(1);

    private @Nullable TwoWayMIDIDevice device;

    public MIDIDeviceHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            String message = command.toString();
            var d = device;
            if (d != null) {
                if (CHANNEL_SEND_SYSEX_MESSAGE.equals(channelUID.getId())) {
                    d.transmitSysex(message);
                } else if (CHANNEL_SEND_CHANNEL_MESSAGE.equals(channelUID.getId())) {
                    d.transmitChannel(message);
                }
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        MIDIDeviceConfiguration config = getConfigAs(MIDIDeviceConfiguration.class);
        scheduler.execute(() -> {
            logger.debug("Connecting to MIDI device '{}'", config.deviceId);
            try {
                device = TwoWayMIDIDevice.connect(config.deviceId, this);
                updateStatus(ThingStatus.ONLINE);
            } catch (MidiDeviceException mde) {
                logger.error("Failed to connect to MIDI device: {}", mde.getMessage());
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    public void receivedSysexMessage(String message) {
        triggerChannel(CHANNEL_RECEIVE_SYSEX_MESSAGE, message);
    }

    public void receivedShortMessage(ShortMessage midiMessage, String messageString) {
        triggerChannel(CHANNEL_RECEIVE_CHANNEL_MESSAGE, messageString);
        int messageChannel = midiMessage.getChannel() + 1;

        // Alert (child) channels
        for (Thing t : getThing().getThings()) {
            if (t.getHandler() instanceof MIDIChannelHandler handler) {
                if (handler.getChannel() == messageChannel) {
                    handler.receivedShortMessage(midiMessage, messageString);
                }
            }
        }

        // Background discovery
        triggerEventListeners(midiMessage, messageString);
    }

    @Override
    public void dispose() {
        TwoWayMIDIDevice md = device;
        if (md != null) {
            md.close();
        }
    }

    public String getDeviceId() {
        TwoWayMIDIDevice md = device;
        if (md != null) {
            return md.getDeviceId();
        }

        return "";
    }

    // Event listening

    public void addEventListener(MIDIEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(MIDIEventListener listener) {
        eventListeners.remove(listener);
    }

    public void triggerEventListeners(MidiMessage message, String messageString) {
        for (MIDIEventListener listener : eventListeners) {
            listener.receivedMessage(message, messageString);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(MIDIChannelDiscoveryService.class);
    }
}
