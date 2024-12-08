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
package org.openhab.binding.midi.internal;

import static org.openhab.binding.midi.internal.MIDIBindingConstants.*;

import javax.sound.midi.ShortMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.midi.internal.midi.MidiDeviceException;
import org.openhab.binding.midi.internal.midi.TwoWayMIDIDevice;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
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
public class MIDIDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MIDIDeviceHandler.class);

    private @Nullable TwoWayMIDIDevice device;

    public MIDIDeviceHandler(Thing thing) {
        super(thing);
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
        MIDIConfiguration config = getConfigAs(MIDIConfiguration.class);
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
        logger.debug("Channel {}, CC: {}, PC: {}, Note ON: {}, Note Off: {}, Data: {}", midiMessage.getChannel(),
                midiMessage.getCommand() == ShortMessage.CONTROL_CHANGE,
                midiMessage.getCommand() == ShortMessage.PROGRAM_CHANGE,
                midiMessage.getCommand() == ShortMessage.NOTE_ON, midiMessage.getCommand() == ShortMessage.NOTE_OFF,
                midiMessage.getData1());
    }

    @Override
    public void dispose() {
        TwoWayMIDIDevice md = device;
        if (md != null) {
            md.close();
        }
    }
}
