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
package org.eclipse.nebula.widgets.nattable.ui.swt;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.core.geometry.PixelRectangle;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Color;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Font;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Font.FontStyle;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.GraphicsContext;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Image;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Transform;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Proxy implementation of {@link GraphicsContext} to provide the drawing
 * capabilities by SWT {@link GC} so the NatTable can be painted using this.
 */
public class SWTGraphicsContext implements GraphicsContext {

    private static final String KEY_PREFIX = SWTGraphicsContext.class.getCanonicalName() + "."; //$NON-NLS-1$

    enum GraphicsProperties {
        FOREGROUND_COLOR,
        BACKGROUND_COLOR,
        ALPHA,
        CLIP_BOUNDS,
        TRANSFORM,
        LINE_STYLE,
        LINE_DASH,
        LINE_WIDTH
    }

    private Stack<Map<GraphicsProperties, Object>> stateStack = new Stack<Map<GraphicsProperties, Object>>();

    /**
     * The SWT {@link GC} which is proxied by this implementation.
     */
    private GC gc;

    /**
     *
     * @param gc
     *            The SWT {@link GC} that should be proxied by this
     *            implementation.
     */
    public SWTGraphicsContext(GC gc) {
        this.gc = gc;
    }

    @Override
    public void initStyle(IStyle style) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawText(String text, double x, double y) {
        this.gc.drawText(text, (int) x, (int) y, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        this.gc.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    @Override
    public void drawRectangle(PixelRectangle rect) {
        this.gc.drawRectangle((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);
    }

    @Override
    public void fillRectangle(PixelRectangle rect) {
        this.gc.fillRectangle((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);
    }

    @Override
    public double calculateTextWidth(String text) {
        return this.gc.textExtent(text).x;
    }

    @Override
    public double getFontHeight() {
        return this.gc.getFontMetrics().getHeight();
    }

    @Override
    public void pushState() {
        Map<GraphicsProperties, Object> props = new HashMap<GraphicsProperties, Object>();
        props.put(GraphicsProperties.FOREGROUND_COLOR, this.gc.getForeground());
        props.put(GraphicsProperties.BACKGROUND_COLOR, this.gc.getBackground());
        props.put(GraphicsProperties.ALPHA, this.gc.getAlpha());
        props.put(GraphicsProperties.CLIP_BOUNDS, this.gc.getClipping());

        // TODO remember the transform wrapper if any is active
        // props.put(GraphicsProperties.TRANSFORM, null);

        this.stateStack.push(props);
    }

    @Override
    public void popState() {
        Map<GraphicsProperties, Object> props = this.stateStack.pop();
        this.gc.setForeground((org.eclipse.swt.graphics.Color) props.get(GraphicsProperties.FOREGROUND_COLOR));
        this.gc.setBackground((org.eclipse.swt.graphics.Color) props.get(GraphicsProperties.BACKGROUND_COLOR));
        this.gc.setAlpha((Integer) props.get(GraphicsProperties.ALPHA));
        this.gc.setClipping((Rectangle) props.get(GraphicsProperties.CLIP_BOUNDS));
    }

    @Override
    public PixelRectangle getClipping() {
        return getPixelRectangle(this.gc.getClipping());
    }

    @Override
    public void setClipping(PixelRectangle clipBounds) {
        this.gc.setClipping(getUiRectangle(clipBounds));
    }

    @Override
    public void setForeground(Color foregroundColor) {
        this.gc.setForeground(getUiColor(foregroundColor));
        // TODO set alpha
    }

    @Override
    public void setBackground(Color backgroundColor) {
        this.gc.setBackground(getUiColor(backgroundColor));
        // TODO set alpha
    }

    @Override
    public org.eclipse.swt.graphics.Color getUiColor(Color color) {
        if (color.getNativeColor() == null) {
            String key = KEY_PREFIX + "_COLOR_" + color.red + "_" + color.green + "_" + color.blue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (!JFaceResources.getColorRegistry().hasValueFor(key)) {
                JFaceResources.getColorRegistry().put(key, new RGB(color.red, color.green, color.blue));
            }
            color.setNativeColor(JFaceResources.getColorRegistry().get(key));
        }
        if (color.getNativeColor() == null || !(color.getNativeColor() instanceof org.eclipse.swt.graphics.Color)) {
            throw new IllegalStateException("native color instance in color wrapper is invalid");
        }
        return (org.eclipse.swt.graphics.Color) color.getNativeColor();
    }

    @Override
    public void setFont(Font font) {
        this.gc.setFont(getUiFont(font));
    }

    @Override
    public org.eclipse.swt.graphics.Font getUiFont(Font font) {
        if (font.getNativeFont() == null) {
            // if all attributes are not set, use the default font
            if (font.name == null && font.height == -1 && font.style == null) {
                font.setNativeFont(Display.getDefault().getSystemFont());
            }
            else {
                FontData defaultFontData = Display.getDefault().getSystemFont().getFontData()[0];
                String fontName = (font.name == null) ? defaultFontData.getName() : font.name;
                int height = (font.height == -1) ? defaultFontData.getHeight() : Double.valueOf(font.height).intValue();
                int fontStyle = SWT.NORMAL;

                // calculate font style
                if (font.style != null) {
                    for (FontStyle style : font.style) {
                        if (FontStyle.ITALIC.equals(style)) {
                            fontStyle = fontStyle | SWT.ITALIC;
                        }
                        else if (FontStyle.BOLD.equals(style)
                                || FontStyle.EXTRA_BOLD.equals(style)
                                || FontStyle.SEMI_BOLD.equals(style)) {
                            fontStyle = fontStyle | SWT.BOLD;
                        }
                    }
                }

                FontData data = new FontData(fontName, height, fontStyle);
                String key = data.toString();
                if (!JFaceResources.getFontRegistry().hasValueFor(key)) {
                    JFaceResources.getFontRegistry().put(key, new FontData[] { data });
                }
                font.setNativeFont(JFaceResources.getFontRegistry().get(key));
            }
        }
        if (!(font.getNativeFont() instanceof org.eclipse.swt.graphics.Font)) {
            throw new IllegalStateException("native font instance in font wrapper is invalid");
        }
        return (org.eclipse.swt.graphics.Font) font.getNativeFont();
    }

    @Override
    public void drawImage(Image image, double x, double y) {
        this.gc.drawImage(getUiImage(image), Double.valueOf(x).intValue(), Double.valueOf(y).intValue());
    }

    @Override
    public org.eclipse.swt.graphics.Image getUiImage(Image image) {
        if (image.getNativeImage() == null) {
            // key = filename
            String key = image.url.toString();
            key = key.substring(key.lastIndexOf('/') + 1, key.lastIndexOf('.'));

            ImageDescriptor descriptor = JFaceResources.getImageRegistry().getDescriptor(key);
            if (descriptor == null) {
                URL url = image.url;
                // TODO check if scaling is necessary
                // if (GUIHelper.needScaling())

                descriptor = ImageDescriptor.createFromURL(url);

                // TODO or upscale

                JFaceResources.getImageRegistry().put(key, ImageDescriptor.createFromURL(url));
            }
            image.setNativeImage(JFaceResources.getImage(key));
        }
        if (image.getNativeImage() == null || !(image.getNativeImage() instanceof org.eclipse.swt.graphics.Image)) {
            throw new IllegalStateException("native image instance in image wrapper is invalid");
        }
        return (org.eclipse.swt.graphics.Image) image.getNativeImage();
    }

    private Rectangle getUiRectangle(PixelRectangle rectangle) {
        return new Rectangle(
                (int) rectangle.x,
                (int) rectangle.y,
                (int) rectangle.width,
                (int) rectangle.height);
    }

    private PixelRectangle getPixelRectangle(Rectangle rectangle) {
        return new PixelRectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void setTransform(Transform transform) {
        if (transform == null) {
            this.gc.setTransform(null);
        }
        else {
            org.eclipse.swt.graphics.Transform uiTransform =
                    new org.eclipse.swt.graphics.Transform(this.gc.getDevice());

            if (transform.hasRotation()) {
                uiTransform.rotate(transform.getAngle());
            }

            if (transform.hasScaling()) {
                uiTransform.scale(transform.getScaleX(), transform.getScaleY());
            }

            if (transform.hasTranslation()) {
                uiTransform.translate(transform.getOffsetX(), transform.getOffsetY());
            }

            this.gc.setTransform(uiTransform);
        }
    }

}
