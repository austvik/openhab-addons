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

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MIDI_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_SEND_SYSEX_MESSAGE = "sendSysexMessage";
    public static final String CHANNEL_SEND_CHANNEL_MESSAGE = "sendChannelMessage";
    public static final String CHANNEL_RECEIVE_SYSEX_MESSAGE = "receiveSysexMessage";
    public static final String CHANNEL_RECEIVE_CHANNEL_MESSAGE = "receiveChannelMessage";
}
