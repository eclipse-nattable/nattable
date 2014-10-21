/*******************************************************************************
 * Copyright (c) 2014, 2015 Dirk Fauth, Edwin Park.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com>   - initial API and implementation
 *     Edwin Park <esp1@cornell.edu>            - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.core.ui.rendering;

/**
 * UI independent implementation for a rgb based color that transports the
 * necessary information to create a color instance for a specific UI toolkit.
 */
public class Color {

    /**
     * the red component, in the range {@code 0-255}
     */
    public final int red;
    /**
     * the green component, in the range {@code 0-255}
     */
    public final int green;
    /**
     * the blue component, in the range {@code 0-255}
     */
    public final int blue;
    /**
     * the opacity component, in the range {@code 0.0-1.0}
     */
    public final double opacity;
    /**
     * The native color object for the UI toolkit in use, that matches this
     * {@link Color} instance. Used to reduce the amount of UI toolkit color
     * objects on rendering.
     */
    private Object nativeColor;

    /**
     * Creates a color with the specified RGB values in the range {@code 0-255},
     * and a opacity of {@code 1.0} (respectively {@code 255}).
     *
     * @param red
     *            the red component, in the range {@code 0-255}
     * @param green
     *            the green component, in the range {@code 0-255}
     * @param blue
     *            the blue component, in the range {@code 0-255}
     */
    public Color(int red, int green, int blue) {
        this(red, green, blue, 1.0);
    }

    /**
     * Creates a color with the specified RGB values in the range {@code 0-255},
     * and a given opacity in the range {@code 0-255}. The opacity value will be
     * calculated to a double value in the range {@code 0.0-1.0}.
     *
     * @param red
     *            the red component, in the range {@code 0-255}
     * @param green
     *            the green component, in the range {@code 0-255}
     * @param blue
     *            the blue component, in the range {@code 0-255}
     * @param opacity
     *            the opacity component, in the range {@code 0-255}
     */
    public Color(int red, int green, int blue, int opacity) {
        this(red, green, blue, Double.valueOf(opacity / 255));
    }

    /**
     * Creates a color with the specified RGB values in the range {@code 0-255},
     * and a given opacity in the range {@code 0.0-1.0}.
     *
     * @param red
     *            the red component, in the range {@code 0-255}
     * @param green
     *            the green component, in the range {@code 0-255}
     * @param blue
     *            the blue component, in the range {@code 0-255}
     * @param opacity
     *            the opacity component, in the range {@code 0.0-1.0}
     */
    public Color(int red, int green, int blue, double opacity) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.opacity = opacity;
    }

    /**
     * @return the native color object for the UI toolkit in use, that matches
     *         this {@link Color} instance.
     */
    public Object getNativeColor() {
        return this.nativeColor;
    }

    /**
     * Set the native color object for the UI toolkit in use, that matches this
     * {@link Color} instance. Will be called by the {@link GraphicsContext}
     * implementation the first time the corresponding native color object is
     * retrieved.
     *
     * @param nativeColor
     *            the native color object for the UI toolkit in use, that
     *            matches this {@link Color} instance.
     */
    public void setNativeColor(Object nativeColor) {
        this.nativeColor = nativeColor;
    }
}
