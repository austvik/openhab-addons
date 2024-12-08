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

import java.util.HexFormat;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper methods for dealing with MIDI.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public final class MIDIHelper {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern TWO_CHARACTERS = Pattern.compile("..");

    private MIDIHelper() {
    }

    public static byte[] parseByteString(String message) {
        String[] parts = WHITESPACE.split(message);
        byte[] bytes = new byte[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                int value = Integer.parseInt(parts[i], 16);
                bytes[i] = (byte) value;
            } catch (NumberFormatException nfex) {
                return new byte[0];
            }
        }

        return bytes;
    }

    public static String formatMIDIString(byte[] message) {
        String hex = HexFormat.of().formatHex(message);
        return TWO_CHARACTERS.matcher(hex).replaceAll("$0 ").trim();
    }
}
