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

import javax.sound.midi.ShortMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.midi.internal.MIDIBindingConstants;
import org.openhab.binding.midi.internal.MIDIControlChangeConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that makes it easier to listen to signals on MIDI channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MIDIControlChangeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MIDIControlChangeHandler.class);

    private @NonNullByDefault({}) MIDIControlChangeConfiguration config = null;

    public MIDIControlChangeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(MIDIControlChangeConfiguration.class);
        logger.debug("Listening for MIDI control change message number {}", config.ccNumber);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID cuid, Command cmnd) {
        logger.debug("Handle command!");
    }

    public void receivedShortMessage(ShortMessage midiMessage, String messageString) {
        logger.debug("Control change number {} received value {}, message {}", midiMessage.getData1(),
                midiMessage.getData2(), messageString);

        if (midiMessage.getCommand() != ShortMessage.CONTROL_CHANGE) {
            logger.error("Illegal command type for command change: {}. Message: {}", midiMessage.getCommand(),
                    messageString);
            return;
        }

        if (midiMessage.getData1() != config.ccNumber) {
            logger.error("Control change with number {} sent to handler for number {} in message {}",
                    midiMessage.getData1(), config.ccNumber, messageString);
            return;
        }

        updateState(MIDIBindingConstants.CHANNEL_CC_VALUE, new DecimalType(midiMessage.getData2()));
    }

    public int getCCNumber() {
        return config.ccNumber;
    }
}
