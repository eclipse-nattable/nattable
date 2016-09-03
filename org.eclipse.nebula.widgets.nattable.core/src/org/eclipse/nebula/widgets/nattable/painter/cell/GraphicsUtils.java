/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Loris Securo <lorissek@gmail.com> - Bug 499701, 500800
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.BorderModeEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * This class contains utility methods for drawing graphics
 *
 * @see <a href=
 *      "http://java-gui.info/Apress-The.Definitive.Guide.to.SWT.and.JFace/8886final/LiB0095.html">GC
 *      snippets</a>
 */
public class GraphicsUtils {

    /**
     * Draws text vertically (rotates plus or minus 90 degrees). Uses the
     * current font, color, and background.
     * <dl>
     * <dt><b>Styles: </b></dt>
     * <dd>UP, DOWN</dd>
     * </dl>
     *
     * @param string
     *            the text to draw
     * @param x
     *            the x coordinate of the top left corner of the drawing
     *            rectangle
     * @param y
     *            the y coordinate of the top left corner of the drawing
     *            rectangle
     * @param gc
     *            the GC on which to draw the text
     * @param style
     *            the style (SWT.UP or SWT.DOWN)
     *            <p>
     *            Note: Only one of the style UP or DOWN may be specified.
     *            </p>
     */
    public static void drawVerticalText(String string, int x, int y, GC gc,
            int style) {
        drawVerticalText(string, x, y, false, false, true, gc, style);
    }

    /**
     * Draws text vertically (rotates plus or minus 90 degrees). Uses the
     * current font, color, and background.
     * <dl>
     * <dt><b>Styles: </b></dt>
     * <dd>UP, DOWN</dd>
     * </dl>
     *
     * @param string
     *            the text to draw
     * @param x
     *            the x coordinate of the top left corner of the drawing
     *            rectangle
     * @param y
     *            the y coordinate of the top left corner of the drawing
     *            rectangle
     * @param underline
     *            set to <code>true</code> to render the text underlined
     * @param strikethrough
     *            set to <code>true</code> to render the text strikethrough
     * @param paintBackground
     *            set to <code>false</code> to render the background
     *            transparent. Needed for example to render the background with
     *            an image or gradient with another painter so the text drawn
     *            here should have no background.
     * @param gc
     *            the GC on which to draw the text
     * @param style
     *            the style (SWT.UP or SWT.DOWN)
     *            <p>
     *            Note: Only one of the style UP or DOWN may be specified.
     *            </p>
     */
    public static void drawVerticalText(String string, int x, int y,
            boolean underline, boolean strikethrough, boolean paintBackground,
            GC gc, int style) {
        // Get the current display
        Display display = Display.getCurrent();
        if (display == null)
            SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);

        // Determine string's dimensions
        // FontMetrics fm = gc.getFontMetrics();
        Point pt = gc.textExtent(string.trim());

        // Create an image the same size as the string
        Image stringImage = new Image(display, pt.x, pt.y);

        // Create a GC so we can draw the image
        GC stringGc = new GC(stringImage);

        // Set attributes from the original GC to the new GC
        stringGc.setAntialias(gc.getAntialias());
        stringGc.setTextAntialias(gc.getTextAntialias());
        stringGc.setForeground(gc.getForeground());
        stringGc.setBackground(gc.getBackground());
        stringGc.setFont(gc.getFont());

        // Fill the image with the specified background color
        // to avoid white spaces if the text does not fill the
        // whole image (e.g. on new lines)
        stringGc.fillRectangle(0, 0, pt.x, pt.y);

        // Draw the text onto the image
        stringGc.drawText(string, 0, 0);

        // draw underline and/or strikethrough
        if (underline || strikethrough) {
            // check and draw underline and strikethrough separately so it is
            // possible to combine both
            if (underline) {
                // y = start y of text + font height
                // - half of the font descent so the underline is between the
                // baseline and the bottom
                int underlineY = pt.y
                        - (stringGc.getFontMetrics().getDescent() / 2);
                stringGc.drawLine(0, underlineY, pt.x, underlineY);
            }

            if (strikethrough) {
                // y = start y of text + half of font height + ascent so lower
                // case characters are
                // also strikethrough
                int strikeY = (pt.y / 2)
                        + (stringGc.getFontMetrics().getLeading() / 2);
                stringGc.drawLine(0, strikeY, pt.x, strikeY);
            }
        }

        // Draw the image vertically onto the original GC
        drawVerticalImage(stringImage, x, y, paintBackground, gc, style);

        // Dispose the new GC
        stringGc.dispose();

        // Dispose the image
        stringImage.dispose();
    }

    /**
     * Draws an image vertically (rotates plus or minus 90 degrees)
     * <dl>
     * <dt><b>Styles: </b></dt>
     * <dd>UP, DOWN</dd>
     * </dl>
     *
     * @param image
     *            the image to draw
     * @param x
     *            the x coordinate of the top left corner of the drawing
     *            rectangle
     * @param y
     *            the y coordinate of the top left corner of the drawing
     *            rectangle
     * @param gc
     *            the GC on which to draw the image
     * @param style
     *            the style (SWT.UP or SWT.DOWN)
     *            <p>
     *            Note: Only one of the style UP or DOWN may be specified.
     *            </p>
     */
    public static void drawVerticalImage(Image image, int x, int y, GC gc,
            int style) {
        drawVerticalImage(image, x, y, true, gc, style);
    }

    /**
     * Draws an image vertically (rotates plus or minus 90 degrees)
     * <dl>
     * <dt><b>Styles: </b></dt>
     * <dd>UP, DOWN</dd>
     * </dl>
     *
     * @param image
     *            the image to draw
     * @param x
     *            the x coordinate of the top left corner of the drawing
     *            rectangle
     * @param y
     *            the y coordinate of the top left corner of the drawing
     *            rectangle
     * @param paintBackground
     *            set to <code>false</code> to render the background
     *            transparent. Needed for example to render the background with
     *            an image or gradient with another painter so the text drawn
     *            here should have no background.
     * @param gc
     *            the GC on which to draw the image
     * @param style
     *            the style (SWT.UP or SWT.DOWN)
     *            <p>
     *            Note: Only one of the style UP or DOWN may be specified.
     *            </p>
     */
    public static void drawVerticalImage(Image image, int x, int y,
            boolean paintBackground, GC gc, int style) {
        // Get the current display
        Display display = Display.getCurrent();
        if (display == null)
            SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);

        // Use the image's data to create a rotated image's data
        ImageData sd = image.getImageData();
        ImageData dd = new ImageData(sd.height, sd.width, sd.depth, sd.palette);
        dd.transparentPixel = sd.transparentPixel;

        // set the defined backgroundcolor to be transparent
        if (!paintBackground) {
            dd.transparentPixel = sd.palette.getPixel(gc.getBackground()
                    .getRGB());
        }

        // Determine which way to rotate, depending on up or down
        boolean up = (style & SWT.UP) == SWT.UP;

        // Run through the horizontal pixels
        for (int sx = 0; sx < sd.width; sx++) {
            // Run through the vertical pixels
            for (int sy = 0; sy < sd.height; sy++) {
                // Determine where to move pixel to in destination image data
                int dx = up ? sy : sd.height - sy - 1;
                int dy = up ? sd.width - sx - 1 : sx;
                // Swap the x, y source data to y, x in the destination
                dd.setPixel(dx, dy, sd.getPixel(sx, sy));
            }
        }

        // Create the vertical image
        Image vertical = new Image(display, dd);

        // Draw the vertical image onto the original GC
        gc.drawImage(vertical, x, y);

        // Dispose the vertical image
        vertical.dispose();
    }

    /**
     * Creates an image containing the specified text, rotated either plus or
     * minus 90 degrees.
     * <dl>
     * <dt><b>Styles: </b></dt>
     * <dd>UP, DOWN</dd>
     * </dl>
     *
     * @param text
     *            the text to rotate
     * @param font
     *            the font to use
     * @param foreground
     *            the color for the text
     * @param background
     *            the background color
     * @param style
     *            direction to rotate (up or down)
     * @return Image
     *         <p>
     *         Note: Only one of the style UP or DOWN may be specified.
     *         </p>
     */
    public static Image createRotatedText(String text, Font font,
            Color foreground, Color background, int style) {
        // Get the current display
        Display display = Display.getCurrent();
        if (display == null)
            SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);

        // Create a GC to calculate font's dimensions
        GC gc = new GC(display);
        gc.setFont(font);

        // Determine string's dimensions
        // FontMetrics fm = gc.getFontMetrics();
        Point pt = gc.textExtent(text);

        // Dispose that gc
        gc.dispose();

        // Create an image the same size as the string
        Image stringImage = new Image(display, pt.x, pt.y);
        // Create a gc for the image
        gc = new GC(stringImage);
        gc.setFont(font);
        gc.setForeground(foreground);
        gc.setBackground(background);

        // Draw the text onto the image
        gc.drawText(text, 0, 0);

        // Draw the image vertically onto the original GC
        Image image = createRotatedImage(stringImage, style);

        // Dispose the new GC
        gc.dispose();

        // Dispose the horizontal image
        stringImage.dispose();

        // Return the rotated image
        return image;
    }

    /**
     * Creates a rotated image (plus or minus 90 degrees)
     * <dl>
     * <dt><b>Styles: </b></dt>
     * <dd>UP, DOWN</dd>
     * </dl>
     *
     * @param image
     *            the image to rotate
     * @param style
     *            direction to rotate (up or down)
     * @return Image
     *         <p>
     *         Note: Only one of the style UP or DOWN may be specified.
     *         </p>
     */
    public static Image createRotatedImage(Image image, int style) {
        // Get the current display
        Display display = Display.getCurrent();
        if (display == null)
            SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);

        // Use the image's data to create a rotated image's data
        ImageData sd = image.getImageData();
        ImageData dd = new ImageData(sd.height, sd.width, sd.depth, sd.palette);

        // Determine which way to rotate, depending on up or down
        boolean up = (style & SWT.UP) == SWT.UP;

        // Run through the horizontal pixels
        for (int sx = 0; sx < sd.width; sx++) {
            // Run through the vertical pixels
            for (int sy = 0; sy < sd.height; sy++) {
                // Determine where to move pixel to in destination image data
                int dx = up ? sy : sd.height - sy - 1;
                int dy = up ? sd.width - sx - 1 : sx;

                // Swap the x, y source data to y, x in the destination
                dd.setPixel(dx, dy, sd.getPixel(sx, sy));
            }
        }

        // Create the vertical image
        return new Image(display, dd);
    }

    /**
     * Draws a horizontal line starting at (x, y) and having the given width.
     * <p>
     * Unlike {@link GC#drawLine}, this method guarantees that the line will
     * always start at the given coordinates and will always have the given
     * width.
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param width
     *            the width of the line to draw
     *
     * @see #drawLineHorizontalBorderTop(GC, int, int, int)
     * @see #drawLineHorizontalBorderBottom(GC, int, int, int)
     * @see #drawLineHorizontal(GC, int, int, int, boolean, boolean)
     * @since 1.5
     */
    public static void drawLineHorizontal(GC gc, int x, int y, int width) {

        if (width == 0) {
            return;
        }

        int x2;

        // create coordinate for destination point
        if (width > 0) {
            x2 = x + width - 1;
        } else {
            x2 = x + width + 1;
        }

        // fix position and length
        if (x2 < x) {
            x++;
        } else {
            int lineWidth = gc.getLineWidth();
            if (lineWidth != 1) {
                x2++;
            }
        }

        gc.drawLine(x, y, x2, y);
    }

    /**
     * The difference between this method and
     * {@link #drawLineHorizontal(GC, int, int, int)} is that the line could be
     * extended to draw left and/or right corners (e.g. the corners of a
     * rectangle).
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param width
     *            the width of the line to draw
     * @param drawLeftCorner
     *            to draw the left corner
     * @param drawRightCorner
     *            to draw the right corner
     *
     * @see #drawLineHorizontal(GC, int, int, int)
     * @since 1.5
     */
    public static void drawLineHorizontal(GC gc, int x, int y, int width, boolean drawLeftCorner, boolean drawRightCorner) {

        if (width == 0) {
            return;
        }

        // adjust the position and length in order to create the corners
        int lineWidth = gc.getLineWidth();
        if (width > 0) {
            if (drawLeftCorner) {
                x = x - (lineWidth / 2);
                width = width + (lineWidth / 2);
            }
            if (drawRightCorner) {
                width = width + ((lineWidth - 1) / 2);
            }
        } else {
            if (drawRightCorner) {
                x = x + ((lineWidth - 1) / 2);
                width = width - ((lineWidth - 1) / 2);
            }
            if (drawLeftCorner) {
                width = width - (lineWidth / 2);
            }
        }

        drawLineHorizontal(gc, x, y, width);
    }

    /**
     * Draws a vertical line starting at (x, y) and having the given height.
     * <p>
     * Unlike {@link GC#drawLine}, this method guarantees that the line will
     * always start at the given coordinates and will always have the given
     * height.
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param height
     *            the height of the line to draw
     *
     * @see #drawLineVerticalBorderLeft(GC, int, int, int)
     * @see #drawLineVerticalBorderRight(GC, int, int, int)
     * @see #drawLineVertical(GC, int, int, int, boolean, boolean)
     * @since 1.5
     */
    public static void drawLineVertical(GC gc, int x, int y, int height) {

        if (height == 0) {
            return;
        }

        int y2;

        // create coordinate for destination point
        if (height > 0) {
            y2 = y + height - 1;
        } else {
            y2 = y + height + 1;
        }

        // fix position and length
        if (y2 < y) {
            y++;
        } else {
            int lineWidth = gc.getLineWidth();
            if (lineWidth != 1) {
                y2++;
            }
        }

        gc.drawLine(x, y, x, y2);
    }

    /**
     * The difference between this method and
     * {@link #drawLineVertical(GC, int, int, int)} is that the line could be
     * extended to draw top and/or bottom corners (e.g. the corners of a
     * rectangle).
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param height
     *            the height of the line to draw
     * @param drawTopCorner
     *            to draw the top corner
     * @param drawBottomCorner
     *            to draw the bottom corner
     *
     * @see #drawLineVertical(GC, int, int, int)
     * @since 1.5
     */
    public static void drawLineVertical(GC gc, int x, int y, int height, boolean drawTopCorner, boolean drawBottomCorner) {

        if (height == 0) {
            return;
        }

        // adjust the position and length in order to create the corners
        int lineWidth = gc.getLineWidth();
        if (height > 0) {
            if (drawTopCorner) {
                y = y - (lineWidth / 2);
                height = height + (lineWidth / 2);
            }
            if (drawBottomCorner) {
                height = height + ((lineWidth - 1) / 2);
            }
        } else {
            if (drawBottomCorner) {
                y = y + ((lineWidth - 1) / 2);
                height = height - ((lineWidth - 1) / 2);
            }
            if (drawTopCorner) {
                height = height - (lineWidth / 2);
            }
        }

        drawLineVertical(gc, x, y, height);
    }

    /**
     * Draws a horizontal line starting at (x, y) and having the given width.
     * The increased thickness resulting from {@link GC#getLineWidth} will be
     * strictly drawn below the line.
     * <p>
     * Unlike {@link GC#drawLine}, this method guarantees that the line will
     * always start at the given coordinates and will always have the given
     * width.
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param width
     *            the width of the line to draw
     *
     * @see #drawLineHorizontal(GC, int, int, int)
     * @see #drawLineHorizontalBorderTop(GC, int, int, int)
     * @see #drawLineHorizontalBorderBottom(GC, int, int, int, boolean, boolean)
     * @since 1.5
     */
    public static void drawLineHorizontalBorderBottom(GC gc, int x, int y, int width) {

        if (width == 0) {
            return;
        }

        // adjust the line to make it have the border at bottom
        int lineWidth = gc.getLineWidth();
        y = y + (lineWidth / 2);

        drawLineHorizontal(gc, x, y, width);
    }

    /**
     * The difference between this method and
     * {@link #drawLineHorizontalBorderBottom(GC, int, int, int)} is that the
     * line could be extended to draw left and/or right corners (e.g. the
     * corners of a rectangle).
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param width
     *            the width of the line to draw
     * @param drawLeftCorner
     *            to draw the left corner
     * @param drawRightCorner
     *            to draw the right corner
     *
     * @see #drawLineHorizontalBorderBottom(GC, int, int, int)
     * @since 1.5
     */
    public static void drawLineHorizontalBorderBottom(GC gc, int x, int y, int width, boolean drawLeftCorner, boolean drawRightCorner) {

        if (width == 0) {
            return;
        }

        // adjust the position and length in order to create the corners
        int lineWidth = gc.getLineWidth();
        if (width > 0) {
            if (drawLeftCorner) {
                x = x - (lineWidth - 1);
                width = width + (lineWidth - 1);
            }
            if (drawRightCorner) {
                width = width + (lineWidth - 1);
            }
        } else {
            if (drawRightCorner) {
                x = x + (lineWidth - 1);
                width = width - (lineWidth - 1);
            }
            if (drawLeftCorner) {
                width = width - (lineWidth - 1);
            }
        }

        drawLineHorizontalBorderBottom(gc, x, y, width);
    }

    /**
     * Draws a horizontal line starting at (x, y) and having the given width.
     * The increased thickness resulting from {@link GC#getLineWidth} will be
     * strictly drawn above the line.
     * <p>
     * Unlike {@link GC#drawLine}, this method guarantees that the line will
     * always start at the given coordinates and will always have the given
     * width.
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param width
     *            the width of the line to draw
     *
     * @see #drawLineHorizontal(GC, int, int, int)
     * @see #drawLineHorizontalBorderBottom(GC, int, int, int)
     * @see #drawLineHorizontalBorderTop(GC, int, int, int, boolean, boolean)
     * @since 1.5
     */
    public static void drawLineHorizontalBorderTop(GC gc, int x, int y, int width) {

        if (width == 0) {
            return;
        }

        // adjust the line to make it have the border at top
        int lineWidth = gc.getLineWidth();
        y = y - ((lineWidth - 1) / 2);

        drawLineHorizontal(gc, x, y, width);
    }

    /**
     * The difference between this method and
     * {@link #drawLineHorizontalBorderTop(GC, int, int, int)} is that the line
     * could be extended to draw left and/or right corners (e.g. the corners of
     * a rectangle).
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param width
     *            the width of the line to draw
     * @param drawLeftCorner
     *            to draw the left corner
     * @param drawRightCorner
     *            to draw the right corner
     *
     * @see #drawLineHorizontalBorderTop(GC, int, int, int)
     * @since 1.5
     */
    public static void drawLineHorizontalBorderTop(GC gc, int x, int y, int width, boolean drawLeftCorner, boolean drawRightCorner) {

        if (width == 0) {
            return;
        }

        // adjust the position and length in order to create the corners
        int lineWidth = gc.getLineWidth();
        if (width > 0) {
            if (drawLeftCorner) {
                x = x - (lineWidth - 1);
                width = width + (lineWidth - 1);
            }
            if (drawRightCorner) {
                width = width + (lineWidth - 1);
            }
        } else {
            if (drawRightCorner) {
                x = x + (lineWidth - 1);
                width = width - (lineWidth - 1);
            }
            if (drawLeftCorner) {
                width = width - (lineWidth - 1);
            }
        }

        drawLineHorizontalBorderTop(gc, x, y, width);
    }

    /**
     * Draws a vertical line starting at (x, y) and having the given height. The
     * increased thickness resulting from {@link GC#getLineWidth} will be
     * strictly drawn to the right of the line.
     * <p>
     * Unlike {@link GC#drawLine}, this method guarantees that the line will
     * always start at the given coordinates and will always have the given
     * height.
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param height
     *            the height of the line to draw
     *
     * @see #drawLineVertical(GC, int, int, int)
     * @see #drawLineVerticalBorderLeft(GC, int, int, int)
     * @see #drawLineVerticalBorderRight(GC, int, int, int, boolean, boolean)
     * @since 1.5
     */
    public static void drawLineVerticalBorderRight(GC gc, int x, int y, int height) {

        if (height == 0) {
            return;
        }

        // adjust the line to make it have the border at right
        int lineWidth = gc.getLineWidth();
        x = x + (lineWidth / 2);

        drawLineVertical(gc, x, y, height);
    }

    /**
     * The difference between this method and
     * {@link #drawLineVerticalBorderRight(GC, int, int, int)} is that the
     * line could be extended to draw top and/or bottom corners (e.g. the
     * corners of a rectangle).
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param height
     *            the height of the line to draw
     * @param drawTopCorner
     *            to draw the top corner
     * @param drawBottomCorner
     *            to draw the bottom corner
     *
     * @see #drawLineVerticalBorderRight(GC, int, int, int)
     * @since 1.5
     */
    public static void drawLineVerticalBorderRight(GC gc, int x, int y, int height, boolean drawTopCorner, boolean drawBottomCorner) {

        if (height == 0) {
            return;
        }

        // adjust the position and length in order to create the corners
        int lineWidth = gc.getLineWidth();
        if (height > 0) {
            if (drawTopCorner) {
                y = y - (lineWidth - 1);
                height = height + (lineWidth - 1);
            }
            if (drawBottomCorner) {
                height = height + (lineWidth - 1);
            }
        } else {
            if (drawBottomCorner) {
                y = y + (lineWidth - 1);
                height = height - (lineWidth - 1);
            }
            if (drawTopCorner) {
                height = height - (lineWidth - 1);
            }
        }

        drawLineVerticalBorderRight(gc, x, y, height);
    }

    /**
     * Draws a vertical line starting at (x, y) and having the given height. The
     * increased thickness resulting from {@link GC#getLineWidth} will be
     * strictly drawn to the left of the line.
     * <p>
     * Unlike {@link GC#drawLine}, this method guarantees that the line will
     * always start at the given coordinates and will always have the given
     * height.
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param height
     *            the height of the line to draw
     *
     * @see #drawLineVertical(GC, int, int, int)
     * @see #drawLineVerticalBorderRight(GC, int, int, int)
     * @see #drawLineVerticalBorderLeft(GC, int, int, int, boolean, boolean)
     * @since 1.5
     */
    public static void drawLineVerticalBorderLeft(GC gc, int x, int y, int height) {

        if (height == 0) {
            return;
        }

        // adjust the line to make it have the border at left
        int lineWidth = gc.getLineWidth();
        x = x - ((lineWidth - 1) / 2);

        drawLineVertical(gc, x, y, height);
    }

    /**
     * The difference between this method and
     * {@link #drawLineVerticalBorderLeft(GC, int, int, int)} is that the line
     * could be extended to draw top and/or bottom corners (e.g. the corners of
     * a rectangle).
     *
     * @param gc
     *            the GC to use to draw
     * @param x
     *            the starting point's x coordinate
     * @param y
     *            the starting point's y coordinate
     * @param height
     *            the height of the line to draw
     * @param drawTopCorner
     *            to draw the top corner
     * @param drawBottomCorner
     *            to draw the bottom corner
     *
     * @see #drawLineVerticalBorderLeft(GC, int, int, int)
     * @since 1.5
     */
    public static void drawLineVerticalBorderLeft(GC gc, int x, int y, int height, boolean drawTopCorner, boolean drawBottomCorner) {

        if (height == 0) {
            return;
        }

        // adjust the position and length in order to create the corners
        int lineWidth = gc.getLineWidth();
        if (height > 0) {
            if (drawTopCorner) {
                y = y - (lineWidth - 1);
                height = height + (lineWidth - 1);
            }
            if (drawBottomCorner) {
                height = height + (lineWidth - 1);
            }
        } else {
            if (drawBottomCorner) {
                y = y + (lineWidth - 1);
                height = height - (lineWidth - 1);
            }
            if (drawTopCorner) {
                height = height - (lineWidth - 1);
            }
        }

        drawLineVerticalBorderLeft(gc, x, y, height);
    }

    /**
     * Draws a filled rectangle.
     * <p>
     * Unlike {@link GC#fillRectangle(Rectangle)}, this method guarantees that
     * the rectangle will always start at the given coordinates even in case of
     * negative width/height.
     *
     * @param gc
     *            the GC to use to draw
     * @param rect
     *            the rectangle to draw
     * @since 1.5
     */
    public static void fillRectangle(GC gc, Rectangle rect) {

        Rectangle rectangle = new Rectangle(rect.x, rect.y, rect.width, rect.height);

        // even with negative lengths, the rectangle should start from (x, y)
        if (rectangle.width < 0) {
            rectangle.x++;
        }
        if (rectangle.height < 0) {
            rectangle.y++;
        }

        gc.fillRectangle(rectangle);
    }

    /**
     * Draws a rectangle with the given border style.
     * <p>
     * Unlike {@link GC#drawRectangle(Rectangle)}:
     * <ul>
     * <li>the width and height of the resulting rectangle will be always
     * exactly {@code rect.width} and {@code rect.height}
     * <li>the rectangle will always start at the given coordinates even in case
     * of negative width/height
     * </ul>
     *
     * @param gc
     *            the GC to use to draw
     * @param rect
     *            the rectangle to draw
     * @param borderStyle
     *            the border style of the rectangle
     * @since 1.5
     */
    public static void drawRectangle(GC gc, Rectangle rect, BorderStyle borderStyle) {

        int originalLineStyle = gc.getLineStyle();
        int originalLineWidth = gc.getLineWidth();
        Color originalForeground = gc.getForeground();

        gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
        gc.setLineWidth(borderStyle.getThickness());
        gc.setForeground(borderStyle.getColor());

        switch (borderStyle.getBorderMode()) {
            case CENTERED:
                drawRectangle(gc, rect);
                break;
            case INTERNAL:
                drawRectangleBorderInternal(gc, rect);
                break;
            case EXTERNAL:
                drawRectangleBorderExternal(gc, rect);
                break;
        }

        gc.setLineStyle(originalLineStyle);
        gc.setLineWidth(originalLineWidth);
        gc.setForeground(originalForeground);
    }

    /**
     * Draws a rectangle.
     * <p>
     * Unlike {@link GC#drawRectangle(Rectangle)}:
     * <ul>
     * <li>the width and height of the resulting rectangle will be always
     * exactly {@code rect.width} and {@code rect.height}
     * <li>the rectangle will always start at the given coordinates even in case
     * of negative width/height
     * </ul>
     *
     * @param gc
     *            the GC to use to draw
     * @param rect
     *            the rectangle to draw
     *
     * @see #drawRectangleBorderInternal
     * @see #drawRectangleBorderExternal
     * @since 1.5
     */
    public static void drawRectangle(GC gc, Rectangle rect) {

        if (rect.width == 0 || rect.height == 0) {
            return;
        }

        Rectangle rectangle = new Rectangle(rect.x, rect.y, rect.width, rect.height);

        int absWidth = Math.abs(rectangle.width);
        int absHeight = Math.abs(rectangle.height);

        // it's a point but it can grow with the border width
        if (absWidth == 1 && absHeight == 1) {

            int lineWidth = gc.getLineWidth();

            rectangle.x = rectangle.x - (lineWidth / 2);
            rectangle.y = rectangle.y - (lineWidth / 2);
            rectangle.width = rectangle.width + lineWidth - 1;
            rectangle.height = rectangle.height + lineWidth - 1;

            Color originalBackground = gc.getBackground();
            gc.setBackground(gc.getForeground());
            fillRectangle(gc, rectangle);
            gc.setBackground(originalBackground);

            return;
        }

        // it's a line
        if (absWidth == 1) {
            drawLineVertical(gc, rectangle.x, rectangle.y, rectangle.height);
            return;
        }
        if (absHeight == 1) {
            drawLineHorizontal(gc, rectangle.x, rectangle.y, rectangle.width);
            return;
        }

        // even with negative lengths, the rectangle should start from (x, y)
        if (rectangle.width < 0) {
            rectangle.x++;
        }
        if (rectangle.height < 0) {
            rectangle.y++;
        }

        // fix the lengths
        rectangle.width--;
        rectangle.height--;

        gc.drawRectangle(rectangle);
    }

    /**
     * Draws a rectangle. The increased border thickness resulting from
     * {@link GC#getLineWidth} will be strictly drawn inside of the rectangle.
     * <p>
     * Unlike {@link GC#drawRectangle(Rectangle)}:
     * <ul>
     * <li>the width and height of the resulting rectangle will be always
     * exactly {@code rect.width} and {@code rect.height}
     * <li>the rectangle will always start at the given coordinates even in case
     * of negative width/height
     * </ul>
     *
     * @param gc
     *            the GC to use to draw
     * @param rect
     *            the rectangle to draw
     * @see #drawRectangle(GC, Rectangle)
     * @see #drawRectangleBorderExternal
     * @since 1.5
     */
    public static void drawRectangleBorderInternal(GC gc, Rectangle rect) {

        if (rect.width == 0 || rect.height == 0) {
            return;
        }

        Rectangle rectangle = new Rectangle(rect.x, rect.y, rect.width, rect.height);

        int lineWidth = gc.getLineWidth();

        // check if it's a line
        int absWidth = Math.abs(rectangle.width);
        if (absWidth == 1) {
            // we're supposed to draw inside the rectangle and since it's a line
            // we don't want it to grow outside, so we force the line width at 1
            if (lineWidth != 1) {
                gc.setLineWidth(1);
            }
            drawLineVertical(gc, rectangle.x, rectangle.y, rectangle.height);
            if (lineWidth != 1) {
                gc.setLineWidth(lineWidth);
            }
            return;
        }
        int absHeight = Math.abs(rectangle.height);
        if (absHeight == 1) {
            // we're supposed to draw inside the rectangle and since it's a line
            // we don't want it to grow outside, so we force the line width at 1
            if (lineWidth != 1) {
                gc.setLineWidth(1);
            }
            drawLineHorizontal(gc, rectangle.x, rectangle.y, rectangle.width);
            if (lineWidth != 1) {
                gc.setLineWidth(lineWidth);
            }
            return;
        }

        // if the border is big enough to cover the whole rectangle interior we
        // simply draw a fill rectangle
        int minSide = Math.min(absWidth, absHeight);
        if (minSide <= (lineWidth * 2)) {
            Color originalBackground = gc.getBackground();
            gc.setBackground(gc.getForeground());
            fillRectangle(gc, rectangle);
            gc.setBackground(originalBackground);
            return;
        }

        // adjust the rectangle to make it internal
        if (rectangle.width > 0) {
            rectangle.x = rectangle.x + (lineWidth / 2);
            rectangle.width = rectangle.width - (lineWidth - 1);
        } else {
            rectangle.x = rectangle.x - ((lineWidth - 1) / 2);
            rectangle.width = rectangle.width + (lineWidth - 1);
        }

        if (rectangle.height > 0) {
            rectangle.y = rectangle.y + (lineWidth / 2);
            rectangle.height = rectangle.height - (lineWidth - 1);
        } else {
            rectangle.y = rectangle.y - ((lineWidth - 1) / 2);
            rectangle.height = rectangle.height + (lineWidth - 1);
        }

        drawRectangle(gc, rectangle);
    }

    /**
     * Draws a rectangle. The increased border thickness resulting from
     * {@link GC#getLineWidth} will be strictly drawn outside of the rectangle.
     * <p>
     * Unlike {@link GC#drawRectangle(Rectangle)}:
     * <ul>
     * <li>the width and height of the resulting rectangle will be always
     * exactly {@code rect.width} and {@code rect.height}
     * <li>the rectangle will always start at the given coordinates even in case
     * of negative width/height
     * </ul>
     *
     * @param gc
     *            the GC to use to draw
     * @param rect
     *            the rectangle to draw
     *
     * @see #drawRectangle(GC, Rectangle)
     * @see #drawRectangleBorderInternal
     * @since 1.5
     */
    public static void drawRectangleBorderExternal(GC gc, Rectangle rect) {

        if (rect.width == 0 || rect.height == 0) {
            return;
        }

        Rectangle rectangle = new Rectangle(rect.x, rect.y, rect.width, rect.height);

        int lineWidth = gc.getLineWidth();

        // adjust the rectangle to make it external
        if (rectangle.width > 0) {
            rectangle.x = rectangle.x - ((lineWidth - 1) / 2);
            rectangle.width = rectangle.width + (lineWidth - 1);
        } else {
            rectangle.x = rectangle.x + (lineWidth / 2);
            rectangle.width = rectangle.width - (lineWidth - 1);
        }

        if (rectangle.height > 0) {
            rectangle.y = rectangle.y - ((lineWidth - 1) / 2);
            rectangle.height = rectangle.height + (lineWidth - 1);
        } else {
            rectangle.y = rectangle.y + (lineWidth / 2);
            rectangle.height = rectangle.height - (lineWidth - 1);
        }

        drawRectangle(gc, rectangle);
    }

    /**
     * Returns the external bounds of the rectangle that would be obtained by
     * painting <code>rectangle</code> with <code>borderStyle</code> using one
     * of the <code>GraphicsUtils.drawRectangle...</code> methods.
     *
     * @param rectangle
     *            the rectangle to consider
     * @param borderStyle
     *            the border style to consider
     * @return the external bounds of what would be the resulting rectangle
     *
     * @since 1.5
     */
    public static Rectangle getResultingExternalBounds(Rectangle rectangle, BorderStyle borderStyle) {
        if (rectangle == null || borderStyle == null) {
            return null;
        }

        Rectangle resultingRectangle = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        int borderWidth = borderStyle.getThickness();
        BorderModeEnum borderMode = borderStyle.getBorderMode();

        switch (borderMode) {
            case CENTERED:
                if (resultingRectangle.width > 0) {
                    resultingRectangle.x = resultingRectangle.x - (borderWidth / 2);
                    resultingRectangle.width = resultingRectangle.width + (borderWidth - 1);
                } else {
                    resultingRectangle.x = resultingRectangle.x + ((borderWidth - 1) / 2);
                    resultingRectangle.width = resultingRectangle.width - (borderWidth - 1);
                }

                if (resultingRectangle.height > 0) {
                    resultingRectangle.y = resultingRectangle.y - (borderWidth / 2);
                    resultingRectangle.height = resultingRectangle.height + (borderWidth - 1);
                } else {
                    resultingRectangle.y = resultingRectangle.y + ((borderWidth - 1) / 2);
                    resultingRectangle.height = resultingRectangle.height - (borderWidth - 1);
                }
                break;
            case EXTERNAL:
                if (resultingRectangle.width > 0) {
                    resultingRectangle.x = resultingRectangle.x - (borderWidth - 1);
                    resultingRectangle.width = resultingRectangle.width + ((borderWidth - 1) * 2);
                } else {
                    resultingRectangle.x = resultingRectangle.x + (borderWidth - 1);
                    resultingRectangle.width = resultingRectangle.width - ((borderWidth - 1) * 2);
                }

                if (resultingRectangle.height > 0) {
                    resultingRectangle.y = resultingRectangle.y - (borderWidth - 1);
                    resultingRectangle.height = resultingRectangle.height + ((borderWidth - 1) * 2);
                } else {
                    resultingRectangle.y = resultingRectangle.y + (borderWidth - 1);
                    resultingRectangle.height = resultingRectangle.height - ((borderWidth - 1) * 2);
                }
                break;
            case INTERNAL:
                // the external bounds remain the same
                break;
        }

        return resultingRectangle;
    }
}