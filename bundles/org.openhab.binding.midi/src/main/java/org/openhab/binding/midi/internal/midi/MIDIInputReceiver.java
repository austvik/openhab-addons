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
package org.openhab.binding.midi.internal.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.midi.internal.handlers.MIDIDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives input from MIDI devices.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MIDIInputReceiver implements Receiver {

    private final Logger logger = LoggerFactory.getLogger(MIDIInputReceiver.class);

    public final String deviceName;
    public final MIDIDeviceHandler handler;

    public MIDIInputReceiver(String deviceName, MIDIDeviceHandler handler) {
        this.deviceName = deviceName;
        this.handler = handler;
    }

    @Override
    public void send(@Nullable MidiMessage msg, long timeStamp) {
        if (msg != null) {
            byte[] messageContent = msg.getMessage();
            if (messageContent != null) {
                String formattedMessage = MIDIHelper.formatMIDIString(messageContent);
                logger.debug("{} received: {}", deviceName, formattedMessage);
                if (msg instanceof ShortMessage shortMessage) {
                    handler.receivedShortMessage(shortMessage, formattedMessage);
                } else if (msg instanceof SysexMessage) {
                    handler.receivedSysexMessage(formattedMessage);
                } else {
                    logger.debug("{} received but ignored: {}, type of message: {}", deviceName, formattedMessage,
                            msg.getClass().getName());
                }
            }
        }
    }

    @Override
    public void close() {
    }
}
