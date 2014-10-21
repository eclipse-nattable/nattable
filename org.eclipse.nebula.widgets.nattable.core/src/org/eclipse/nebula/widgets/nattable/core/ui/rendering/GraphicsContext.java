/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth, Edwin Park.
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

import org.eclipse.nebula.widgets.nattable.core.geometry.PixelRectangle;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

/**
 * Interface for implementing a proxy to a UI toolkit dependent graphics context
 * implementation. The graphics context is at the end the on who paints lines
 * shapes etc. to the canvas to create the NatTable.
 *
 * e.g. org.eclipse.swt.graphics.GC
 *
 */
public interface GraphicsContext {

    /**
     * Takes the styling attributes of an {@link IStyle} and applies them to the
     * UI toolkit graphics context.
     * <p>
     * It is a good practice to store the current state of the graphics context
     * prior to calling this method. Use {@link GraphicsContext#pushState()} for
     * this.
     * </p>
     * <p>
     * After the drawing operations are done it is also good practice to reset
     * the applied settings using {@link GraphicsContext#popState()}.
     * </p>
     *
     * @param style
     *            {@link IStyle} instance containing the style information that
     *            should be used in the following painting operations.
     */
    void initStyle(IStyle style);

    /**
     * Draws the specified text at the position specified by x and y
     * coordinates. Ensure that drawing the text doesn't draw a background and
     * line delimiters are handled correctly.
     *
     * @param text
     *            The text to draw.
     * @param x
     *            the x coordinate of the top left corner of the rectangular
     *            area where the text is to be drawn
     * @param y
     *            the y coordinate of the top left corner of the rectangular
     *            area where the text is to be drawn
     */
    void drawText(String text, double x, double y);

    /**
     * Draws a line, using the foreground color, between the points (
     * <code>x1</code>, <code>y1</code>) and (<code>x2</code>, <code>y2</code>).
     *
     * @param x1
     *            the first point's x coordinate
     * @param y1
     *            the first point's y coordinate
     * @param x2
     *            the second point's x coordinate
     * @param y2
     *            the second point's y coordinate
     */
    void drawLine(double x1, double y1, double x2, double y2);

    /**
     * Draws a rectangle with the specified bounds using the foreground color.
     *
     * @param rect
     *            The rectangle's pixel bounds.
     */
    void drawRectangle(PixelRectangle rect);

    /**
     * Fills the specified rectangular pixel area with the background color.
     *
     * @param rect
     *            The rectangle's pixel bounds.
     */
    void fillRectangle(PixelRectangle rect);

    /**
     * Returns the calculated pixel width of the given string if drawn in the
     * current font in the receiver. Tab expansion and carriage return
     * processing are performed.
     * <p>
     *
     * @param text
     *            the text to measure
     * @return the width in pixels
     */
    double calculateTextWidth(String text);

    /**
     * Returns the height of the font currently being used by the receiver,
     * measured in pixels. A font's height is the sum of its ascent, descent and
     * leading area.
     *
     * @return The height of the font currently being used.
     */
    double getFontHeight();

    // State

    /**
     * Saves the current graphics context property state and pushes it onto a
     * stack.
     */
    void pushState();

    /**
     * Pops the last saved graphics context state and restores all properties
     * back to what they were when that state was saved.
     */
    void popState();

    /**
     * Returns the bounding rectangle of the receiver's clipping region. If no
     * clipping region is set, the return value will be a rectangle which covers
     * the entire bounds of the object the receiver is drawing on.
     *
     * @return The bounding rectangle of the clipping region.
     */
    PixelRectangle getClipping();

    /**
     * Sets the area of the receiver which can be changed by drawing operations
     * to the rectangular area specified by the argument. Specifying null for
     * the rectangle reverts the receiver's clipping area to its original value.
     *
     * @param clipBounds
     *            The clipping rectangle or <code>null</code>.
     */
    void setClipping(PixelRectangle clipBounds);

    /**
     * Set the foreground color.
     *
     * @param foregroundColor
     *            The new foreground color to use.
     */
    void setForeground(Color foregroundColor);

    /**
     * Set the background color.
     *
     * @param backgroundColor
     *            The new background color to use.
     */
    void setBackground(Color backgroundColor);

    /**
     * Transforms the given NatTable {@link Color} to the UI toolkit dependent
     * color implementation instance. Used internally to operate with UI toolkit
     * dependent color instances.
     * <p>
     * For optimization, this method should also set the UI toolkit dependent
     * color instance as native value to the {@link Color} instance. This way
     * resource allocation and the number of created objects is reduced.
     * </p>
     * <p>
     * Note: On overriding you can return the specific color implementation of
     * the UI toolkit to avoid later casting operations.
     * </p>
     *
     * @param color
     *            The NatTable {@link Color} that transports the necessary
     *            information to build up a UI dependent color object.
     * @return A UI toolkit color instance.
     */
    Object getUiColor(Color color);

    // TODO
    // void setLineStyle(int style);
    //
    // void setLineDash(int dash);
    //
    // void setLineWidth(int width);
    //
    // void setAntialias(int antialias);
    //
    // void setTextAntialias(int antialias);

    /**
     * Set the font.
     *
     * @param font
     *            The new font to use.
     */
    void setFont(Font font);

    /**
     * Transforms the given NatTable {@link Font} to the UI toolkit dependent
     * font implementation instance. Used internally to operate with UI toolkit
     * dependent font instances.
     * <p>
     * For optimization, this method should also set the UI toolkit dependent
     * font instance as native value to the {@link Font} instance. This way
     * resource allocation and the number of created objects is reduced.
     * </p>
     * <p>
     * Note: On overriding you can return the specific font implementation of
     * the UI toolkit to avoid later casting operations.
     * </p>
     *
     * @param font
     *            The NatTable {@link Font} that transports the necessary
     *            information to build up a UI dependent font object.
     * @return A UI toolkit font instance.
     */
    Object getUiFont(Font font);

    /**
     * Draws the given image at the specified coordinates.
     *
     * @param image
     *            the image to draw
     * @param x
     *            the x coordinate of where to draw
     * @param y
     *            the y coordinate of where to draw
     */
    void drawImage(Image image, double x, double y);

    /**
     * Transforms the given NatTable {@link Image} to the UI toolkit dependent
     * image implementation instance. Used internally to operate with UI toolkit
     * dependent image instances.
     * <p>
     * For optimization, this method should also set the UI toolkit dependent
     * image instance as native value to the {@link Image} instance. This way
     * resource allocation and the number of created objects is reduced.
     * </p>
     * <p>
     * Note: On overriding you can return the specific image implementation of
     * the UI toolkit to avoid later casting operations.
     * </p>
     *
     * @param image
     *            The NatTable {@link Image} that transports the necessary
     *            information to build up a UI dependent image object.
     * @return A UI toolkit image instance.
     */
    Object getUiImage(Image image);

    /**
     * Sets the current transform.
     *
     * @param transform
     *            The transformation that should be applied or <code>null</code>
     *            to reset an applied transformation.
     */
    void setTransform(Transform transform);
}
