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

import javax.sound.midi.MidiMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listen to midi events.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public interface MIDIEventListener {
    /**
     * Called when you receive a MIDI message.
     *
     * @param message The message object
     * @param messageString The parsed message string
     */
    public void receivedMessage(MidiMessage message, String messageString);
}
