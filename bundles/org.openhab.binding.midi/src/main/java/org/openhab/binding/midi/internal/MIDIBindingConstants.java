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

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MIDIBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MIDIBindingConstants {

    private static final String BINDING_ID = "midi";

    // Discovery
    public static final Duration SEARCH_TIME = Duration.ofSeconds(15);
    public static final boolean BACKGROUND_DISCOVERY = true;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MIDI_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_MIDI_CHANNEL = new ThingTypeUID(BINDING_ID, "channel");
    public static final ThingTypeUID THING_TYPE_MIDI_CONTROL_CHANGE = new ThingTypeUID(BINDING_ID, "control-change");

    // List of all Channel ids

    // Device
    public static final String CHANNEL_SEND_SYSEX_MESSAGE = "sendSysexMessage";
    public static final String CHANNEL_SEND_CHANNEL_MESSAGE = "sendChannelMessage";
    public static final String CHANNEL_RECEIVE_SYSEX_MESSAGE = "receiveSysexMessage";
    public static final String CHANNEL_RECEIVE_CHANNEL_MESSAGE = "receiveChannelMessage";

    // Channel
    public static final String CHANNEL_NOTE_ON = "noteOn";
    public static final String CHANNEL_NOTE_OFF = "noteOff";
    public static final String CHANNEL_CONTROL_CHANGE = "controlChange";
    public static final String CHANNEL_PROGRAM_CHANGE = "programChange";

    // Control Change
    public static final String CHANNEL_CC_VALUE = "value";
}
