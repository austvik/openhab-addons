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

package org.openhab.binding.nanoleaf.internal.layout;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Information about the different Nanoleaf shapes.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public enum ShapeType {
    UNKNOWN("Unknown", -1, 0, 0, DrawingAlgorithm.NONE),
    TRIANGLE("Triangle", 0, 150, 3, DrawingAlgorithm.TRIANGLE),
    RHYTHM("Rhythm", 1, 0, 1, DrawingAlgorithm.NONE),
    SQUARE("Square", 2, 100, 0, DrawingAlgorithm.SQUARE),
    CONTROL_SQUARE_MASTER("Control Square Master", 3, 100, 0, DrawingAlgorithm.SQUARE),
    CONTROL_SQUARE_PASSIVE("Control Square Passive", 4, 100, 0, DrawingAlgorithm.NONE),
    SHAPES_HEXAGON("Hexagon (Shapes)", 7, 67, 6, DrawingAlgorithm.HEXAGON),
    SHAPES_TRIANGLE("Triangle (Shapes)", 8, 134, 3, DrawingAlgorithm.TRIANGLE),
    SHAPES_MINI_TRIANGLE("Mini Triangle (Shapes)", 9, 67, 3, DrawingAlgorithm.TRIANGLE),
    SHAPES_CONTROLLER("Controller (Shapes)", 12, 0, 0, DrawingAlgorithm.NONE),
    ELEMENTS_HEXAGON("Elements Hexagon", 14, 134, 6, DrawingAlgorithm.HEXAGON),
    ELEMENTS_HEXAGON_CORNER("Elements Hexagon - Corner", 15, 33.5 / 58, 6, DrawingAlgorithm.CORNER),
    LINES_CONNECTOR("Lines Connector", 16, 11, 1, DrawingAlgorithm.LINE),
    LIGHT_LINES("Light Lines", 17, 154, 1, DrawingAlgorithm.LINE),
    LINES_LINES_SINGLE("Light Lines - Single Sone", 18, 77, 1, DrawingAlgorithm.LINE),
    CONTROLLER_CAP("Controller Cap", 19, 11, 0, DrawingAlgorithm.NONE),
    POWER_CONNECTOR("Power Connector", 20, 11, 0, DrawingAlgorithm.NONE);

    private final String name;
    private final int id;
    private final double sideLength;
    private final int numSides;
    private final DrawingAlgorithm drawingAlgorithm;

    ShapeType(String name, int id, double sideLenght, int numSides, DrawingAlgorithm drawingAlgorithm) {
        this.name = name;
        this.id = id;
        this.sideLength = sideLenght;
        this.numSides = numSides;
        this.drawingAlgorithm = drawingAlgorithm;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public double getSideLength() {
        return sideLength;
    }

    public int getNumSides() {
        return numSides;
    }

    public DrawingAlgorithm getDrawingAlgorithm() {
        return drawingAlgorithm;
    }

    public static ShapeType valueOf(int id) {
        for (ShapeType shapeType : values()) {
            if (shapeType.getId() == id) {
                return shapeType;
            }
        }

        return ShapeType.UNKNOWN;
    }
}
