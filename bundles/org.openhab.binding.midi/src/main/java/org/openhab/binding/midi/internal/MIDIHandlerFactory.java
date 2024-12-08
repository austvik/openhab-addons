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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.midi.internal.handlers.MIDIChannelHandler;
import org.openhab.binding.midi.internal.handlers.MIDIDeviceHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MIDIHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.midi", service = ThingHandlerFactory.class)
public class MIDIHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MIDIHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_MIDI_DEVICE,
            THING_TYPE_MIDI_CHANNEL);

    @Activate
    public MIDIHandlerFactory() {
        logger.debug("Activating factory for: {}", SUPPORTED_THING_TYPES_UIDS);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MIDI_DEVICE.equals(thingTypeUID)) {
            return new MIDIDeviceHandler((Bridge) thing);
        }

        if (THING_TYPE_MIDI_CHANNEL.equals(thingTypeUID)) {
            return new MIDIChannelHandler(thing);
        }

        return null;
    }
}
