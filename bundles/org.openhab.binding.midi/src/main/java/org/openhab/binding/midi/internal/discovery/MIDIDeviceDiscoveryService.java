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
package org.openhab.binding.midi.internal.discovery;

import static org.openhab.binding.midi.internal.MIDIBindingConstants.THING_TYPE_MIDI_DEVICE;

import java.util.Set;
import java.util.regex.Pattern;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers MIDI devices.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.midi")
public class MIDIDeviceDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(MIDIDeviceDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_MIDI_DEVICE);
    private static final Pattern INVALID_THING_UID_CHARSET = Pattern.compile("[\\W]");

    public MIDIDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES, 5, true);
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start MIDI device background discovery");
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop MIDI device background discovery");
    }

    @Override
    public void startScan() {
        logger.debug("Scanning for MIDI devices");
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            MidiDevice device;
            try {
                device = MidiSystem.getMidiDevice(info);

                if (device.getMaxReceivers() == 0 && device.getMaxTransmitters() == 0) {
                    // Only consider devices we can transmit to or receive from (-1 is unlimited)
                    continue;
                }

                String safeName = makeNameUIDSafe(info.getName());
                logger.debug(
                        "Discovered device: {} Full name: {}, Vendor: {}, Version: {}, Description: {}, Max receivers: {}, Max transmitters: {}, Is open: {}",
                        safeName, info.getName(), info.getVendor(), info.getVersion(), info.getDescription(),
                        device.getMaxReceivers(), device.getMaxTransmitters(), device.isOpen());

                try {
                    ThingUID thingId = new ThingUID(THING_TYPE_MIDI_DEVICE, safeName);
                    DiscoveryResult result = DiscoveryResultBuilder.create(thingId)
                            .withProperty("deviceId", info.getName()).withProperty("vendor", info.getVendor())
                            .withProperty("version", info.getVersion())
                            .withProperty("description", info.getDescription())
                            .withProperty("maxReceivers", generateNumberProperty(device.getMaxReceivers()))
                            .withProperty("maxTransmitters", generateNumberProperty(device.getMaxTransmitters()))
                            .withRepresentationProperty("deviceId").build();
                    thingDiscovered(result);
                } catch (IllegalArgumentException iaex) {
                    logger.error("Illegal MIDI device name: {}", info.getName(), iaex);
                }
            } catch (MidiUnavailableException ex) {
                logger.error("MIDI device is unavailable for {}", info.getName(), ex);
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    private static String makeNameUIDSafe(String midiName) {
        return INVALID_THING_UID_CHARSET.matcher(midiName).replaceAll("-");
    }

    private static String generateNumberProperty(int num) {
        if (num == -1) {
            return "unlimited";
        }

        return Integer.toString(num);
    }
}
