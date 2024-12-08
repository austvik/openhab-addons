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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.midi.internal.MIDIDeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MIDI device that can both transmit and receive MIDI messages.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class TwoWayMIDIDevice {

    private final Logger logger = LoggerFactory.getLogger(TwoWayMIDIDevice.class);

    private final String deviceId;
    private final @Nullable MidiDevice receiveDevice;
    private final @Nullable MidiDevice transmitDevice;

    private TwoWayMIDIDevice(String deviceId, @Nullable MidiDevice receiveDevice, @Nullable MidiDevice transmitDevice) {
        this.deviceId = deviceId;
        this.receiveDevice = receiveDevice;
        this.transmitDevice = transmitDevice;
    }

    public static TwoWayMIDIDevice connect(String deviceId, MIDIDeviceHandler callback) throws MidiDeviceException {
        MidiDevice receiveDevice = null;
        MidiDevice transmitDevice = null;

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (deviceId.equals(info.getName())) {
                try {
                    MidiDevice device = MidiSystem.getMidiDevice(info);
                    if (device.getMaxTransmitters() != 0) {
                        var t = device.getTransmitter();
                        t.setReceiver(new MIDIInputReceiver(deviceId, callback));
                        device.open();
                        receiveDevice = device;
                    }

                    if (device.getMaxReceivers() != 0) {
                        device.open();
                        transmitDevice = device;
                    }
                } catch (MidiUnavailableException ex) {
                    throw new MidiDeviceException("Unable to open MIDI device '{}'", deviceId, ex);
                }
            }
        }

        if (receiveDevice == null || transmitDevice == null) {
            throw new MidiDeviceException("Unable to open MIDI device '{}'", deviceId);
        }

        return new TwoWayMIDIDevice(deviceId, receiveDevice, transmitDevice);
    }

    public void transmitSysex(String message) {
        byte[] bytes = MIDIHelper.parseByteString(message);
        if (bytes.length > 0) {
            try {
                MidiMessage sysEx = new SysexMessage(bytes, bytes.length);
                transmit(sysEx, message);
            } catch (InvalidMidiDataException imde) {
                logger.error("Invalid MIDI data: '{}'", message, imde);
            }
        } else {
            logger.error("Failed to parse MIDI message '{}'", message);
        }
    }

    public void transmitChannel(String message) {
        byte[] bytes = MIDIHelper.parseByteString(message);
        if (bytes.length < 3 || bytes.length > 4) {
            try {
                MidiMessage midiMessage;
                if (bytes.length == 3) {
                    midiMessage = new ShortMessage(bytes[0], bytes[1], bytes[2]);
                } else {
                    midiMessage = new ShortMessage(bytes[0], bytes[1], bytes[2], bytes[3]);
                }

                transmit(midiMessage, message);
            } catch (InvalidMidiDataException imde) {
                logger.error("Invalid MIDI data: '{}'", message, imde);
            }
        } else {
            logger.error("Failed to parse MIDI message '{}', needs to be 3 or 4 bytes long", message);
        }
    }

    private void transmit(MidiMessage midiMessage, String message) {
        try {
            MidiDevice td = transmitDevice;
            if (td != null) {
                td.getReceiver().send(midiMessage, -1);
                logger.debug("Sent message {} to {}", message, deviceId);
            } else {
                logger.error("MIDI device '{}' has no receiver to send message {} to", deviceId, message);
            }
        } catch (MidiUnavailableException muex) {
            logger.error("MIDI unavailavble, failed to send message '{}'", message, muex);
        }
    }

    public void close() {
        MidiDevice rd = receiveDevice;
        if (rd != null && rd.isOpen()) {
            rd.close();
        }

        MidiDevice td = transmitDevice;
        if (td != null && td.isOpen()) {
            td.close();
        }
    }
}
