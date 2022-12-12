/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains well known information about different Nanoleaf models.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public enum NanoleafControllerModel {
    PANELS("NL22", "Triangles 1st Generation", false, false, "1.5.0"),
    CANVAS("NL29", "Canvas", true, true, "1.1.0"),
    SHAPES_HEXAGONS("NL42", "Shapes Hexagon", true, false, "1.1.0"),
    SMART_BULB("NL45", "Smart Bulb", false, false, "1.1.0"),
    SHAPES_TRIANGLES("NL47", "Shapes Triangles", true, false, "1.1.0"),
    SHAPES_MINI_TRIANGLES("NL48", "Shapes Mini Triangles", true, false, "1.1.0"),
    ELEMENTS_HEXAGONS("NL52", "Elements Hexagons", true, false, "1.1.0"),
    LIGHTSTRIP("NL55", "Lightstrip", false, false, "1.1.0"),
    LINES("NL59", "Lines", false, false, "1.1.0"),
    UNKNOWN("NL??", "Unknown", true, false, "1.1.0");

    private final static Logger logger = LoggerFactory.getLogger(NanoleafControllerModel.class);

    private final String modelNr;
    private final String name;
    private final boolean hasControllerTouchSupport;
    private final boolean hasPanelTouchSupport;
    private final String minApiVersion;

    private NanoleafControllerModel(String modelNr, String name, boolean hasControllerTouchSupport,
            boolean hasPanelTouchSupport, String minApiVersion) {
        this.modelNr = modelNr;
        this.name = name;
        this.hasControllerTouchSupport = hasControllerTouchSupport;
        this.hasPanelTouchSupport = hasPanelTouchSupport;
        this.minApiVersion = minApiVersion;
    }

    public String getModelNr() {
        return modelNr;
    }

    public String getName() {
        return name;
    }

    public boolean hasControllerTouchSupport() {
        return hasControllerTouchSupport;
    }

    public boolean hasPanelTouchSupport() {
        return hasPanelTouchSupport;
    }

    public String getMinApiVersion() {
        return minApiVersion;
    }

    public static NanoleafControllerModel getForModel(@Nullable String modelNr) {
        if (modelNr == null) {
            return NanoleafControllerModel.UNKNOWN;
        }

        for (NanoleafControllerModel controllerModel : values()) {
            if (controllerModel.getModelNr().equals(modelNr)) {
                return controllerModel;
            }
        }

        logger.warn(
                "You have a Nanoleaf model the developers of this binding havent herad about: {}. Please tell them about it.",
                modelNr);
        return NanoleafControllerModel.UNKNOWN;
    }
}
