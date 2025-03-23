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

import java.util.Collection;
import java.util.List;

import javax.sound.midi.ShortMessage;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.midi.internal.MIDIBindingConstants;
import org.openhab.binding.midi.internal.MIDIChannelConfiguration;
import org.openhab.binding.midi.internal.discovery.MIDIControlChangeDiscoveryService;
import org.openhab.core.library.types.DecimalType;
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
 * Class that makes it easier to listen to signals on MIDI channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MIDIChannelHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(MIDIChannelHandler.class);

    private @NonNullByDefault({}) MIDIChannelConfiguration config = null;

    public MIDIChannelHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(MIDIChannelConfiguration.class);
        logger.debug("Listening on MIDI channel {}", config.channel);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID cuid, Command cmnd) {
        logger.debug("Handle command!");
    }

    public void receivedShortMessage(ShortMessage midiMessage, String messageString) {
        int command = midiMessage.getCommand();
        logger.debug("Channel {} received command type {} message {}", config.channel, command, messageString);
        switch (command) {
            case ShortMessage.NOTE_ON ->
                updateState(MIDIBindingConstants.CHANNEL_NOTE_ON, new DecimalType(midiMessage.getData1()));
            case ShortMessage.NOTE_OFF ->
                updateState(MIDIBindingConstants.CHANNEL_NOTE_OFF, new DecimalType(midiMessage.getData1()));
            case ShortMessage.CONTROL_CHANGE -> {
                updateState(MIDIBindingConstants.CHANNEL_CONTROL_CHANGE, new DecimalType(midiMessage.getData1()));

                // Alert (child) channels
                for (Thing t : getThing().getThings()) {
                    if (t.getHandler() instanceof MIDIControlChangeHandler handler) {
                        if (handler.getCCNumber() == midiMessage.getData1()) {
                            handler.receivedShortMessage(midiMessage, messageString);
                        }
                    }
                }
            }
            case ShortMessage.PROGRAM_CHANGE ->
                updateState(MIDIBindingConstants.CHANNEL_PROGRAM_CHANGE, new DecimalType(midiMessage.getData1()));
            default ->
                logger.debug("Channel received update of unhandled command: {}, message: {}", command, messageString);
        }
    }

    public int getChannel() {
        return config.channel;
    }

    public MIDIDeviceHandler getDeviceHandler() {
        return (@NonNull MIDIDeviceHandler) getBridge().getHandler();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(MIDIControlChangeDiscoveryService.class);
    }
}
