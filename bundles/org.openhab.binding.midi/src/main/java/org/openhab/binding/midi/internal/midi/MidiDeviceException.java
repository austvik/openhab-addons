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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception when dealing with MIDI devices.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class MidiDeviceException extends Exception {

    private static final long serialVersionUID = -3392196492371865023L;

    public MidiDeviceException(String message, String deviceId) {
        super(message.formatted(deviceId));
    }

    public MidiDeviceException(String message, String deviceId, Throwable cause) {
        super(message.formatted(deviceId), cause);
    }
}
