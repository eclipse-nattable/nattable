/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com>   - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.core.ui.rendering;

/**
 * UI independent implementation for a font that transports the necessary
 * information to create a font instance for a specific UI toolkit.
 */
public class Font {

    /**
     * Note that not every UI toolkit supports every {@link FontStyle}. For SWT
     * for example, only NORMAL, BOLD and ITALIC are supported. The others will
     * be translated to the closest matching ones.
     */
    public enum FontStyle {
        /**
         * represents the regular font posture
         */
        REGULAR,
        /**
         * represents the italic font posture
         */
        ITALIC,
        /**
         * represents the black font weight
         */
        BLACK,
        /**
         * represents the bold font weight
         */
        BOLD,
        /**
         * represents the extra bold font weight
         */
        EXTRA_BOLD,
        /**
         * represents the extra light font weight
         */
        EXTRA_LIGHT,
        /**
         * represents the light font weight
         */
        LIGHT,
        /**
         * represents the medium font weight
         */
        MEDIUM,
        /**
         * represents the normal font weight
         */
        NORMAL,
        /**
         * represents the semi bold font weight
         */
        SEMI_BOLD,
        /**
         * represents the thin font weight
         */
        THIN
    }

    /**
     * the name of the font family
     */
    public final String name;
    /**
     * the font height
     */
    public final double height;
    /**
     * the font styles
     */
    public final FontStyle[] style;
    /**
     * The native font object for the UI toolkit in use, that matches this
     * {@link Font} instance. Used to reduce the amount of UI toolkit font
     * objects on rendering.
     */
    private Object nativeFont;

    /**
     * Create a {@link Font} instance that will use the default font of the UI
     * toolkit (typically the system default font).
     */
    public Font() {
        this(null, -1, null);
    }

    /**
     * Create a {@link Font} instance that will search for a font with the given
     * font family name and the default font size (typically the system default
     * font size).
     *
     * @param name
     *            the name of the font family
     */
    public Font(String name) {
        this(name, -1, null);
    }

    /**
     * Create a {@link Font} instance that will use the default font of the UI
     * toolkit (typically the system default font) and the given font height.
     *
     * @param height
     *            the font height
     */
    public Font(double height) {
        this(null, height, null);
    }

    /**
     * Create a {@link Font} instance that will search for a font with the given
     * font family name and the given font size.
     *
     * @param name
     *            the name of the font family
     * @param height
     *            the font height
     */
    public Font(String name, double height) {
        this(name, height, null);
    }

    /**
     * Create a {@link Font} instance that will search for a font with the given
     * font family name, the given font size and the font styles for font weight
     * and font posture.
     *
     * @param name
     *            the name of the font family
     * @param height
     *            the font height
     * @param style
     *            array of {@link FontStyle} values for font weight and font
     *            posture.
     */
    public Font(String name, double height, FontStyle[] style) {
        this.name = name;
        this.height = height;
        this.style = style;
    }

    /**
     * @return the native font object for the UI toolkit in use, that matches
     *         this {@link Font} instance.
     */
    public Object getNativeFont() {
        return this.nativeFont;
    }

    /**
     * Set the native font object for the UI toolkit in use, that matches this
     * {@link Font} instance. Will be called by the {@link GraphicsContext}
     * implementation the first time the corresponding native font object is
     * retrieved.
     *
     * @param nativeFont
     *            the native font object for the UI toolkit in use, that matches
     *            this {@link Font} instance.
     */
    public void setNativeFont(Object nativeFont) {
        this.nativeFont = nativeFont;
    }
}
