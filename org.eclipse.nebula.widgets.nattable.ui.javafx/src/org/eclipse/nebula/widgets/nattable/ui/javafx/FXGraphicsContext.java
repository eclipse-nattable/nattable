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
package org.eclipse.nebula.widgets.nattable.ui.javafx;

import java.io.IOException;
import java.io.InputStream;

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;

import org.eclipse.nebula.widgets.nattable.core.geometry.PixelRectangle;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Color;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Font;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Font.FontStyle;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.GraphicsContext;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Image;
import org.eclipse.nebula.widgets.nattable.core.ui.rendering.Transform;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

@SuppressWarnings("restriction")
public class FXGraphicsContext implements GraphicsContext {

    /**
     * The JavaFX {@link javafx.scene.canvas.GraphicsContext} which is proxied
     * by this implementation.
     */
    private javafx.scene.canvas.GraphicsContext gc;

    private boolean activeClipping;

    /**
     *
     * @param gc
     *            The JavaFX {@link javafx.scene.canvas.GraphicsContext} that
     *            should be proxied by this implementation.
     */
    public FXGraphicsContext(javafx.scene.canvas.GraphicsContext gc) {
        this.gc = gc;
    }

    @Override
    public void initStyle(IStyle style) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawText(String text, double x, double y) {
        this.gc.strokeText(text, x, y);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        this.gc.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void drawRectangle(PixelRectangle rect) {
        this.gc.strokeRect(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public void fillRectangle(PixelRectangle rect) {
        this.gc.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public double calculateTextWidth(String text) {
        // as there is no other way to get font metrics we need to use internal
        // API here
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.gc.getFont());
        return metrics.computeStringWidth(text);
    }

    @Override
    public double getFontHeight() {
        // as there is no other way to get font metrics we need to use internal
        // API here
        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(this.gc.getFont());
        return metrics.getLineHeight();
    }

    @Override
    public void pushState() {
        this.gc.save();
    }

    @Override
    public void popState() {
        this.gc.restore();
    }

    @Override
    public PixelRectangle getClipping() {
        return getPixelRectangle(this.gc.getCanvas().getBoundsInLocal());
    }

    @Override
    public void setClipping(PixelRectangle clipBounds) {
        resetClip();

        if (clipBounds != null) {
            this.activeClipping = true;

            this.gc.save();
            this.gc.beginPath();

            // if this isn't working well, we need to check the following link
            // again
            // http://git.eclipse.org/c/efxclipse/org.eclipse.efxclipse.git/tree/experimental/swt/org.eclipse.fx.runtime.swt/src/org/eclipse/swt/internal/CanvasGC.java
            this.gc.rect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

            this.gc.clip();
            this.gc.closePath();
        }
    }

    private void resetClip() {
        if (this.activeClipping) {
            // Read state of other values
            double globalAlpha = this.gc.getGlobalAlpha();
            BlendMode blendop = this.gc.getGlobalBlendMode();
            Affine transform = this.gc.getTransform();
            Paint fill = this.gc.getFill();
            Paint stroke = this.gc.getStroke();
            double linewidth = this.gc.getLineWidth();
            StrokeLineCap linecap = this.gc.getLineCap();
            StrokeLineJoin linejoin = this.gc.getLineJoin();
            double miterlimit = this.gc.getMiterLimit();
            javafx.scene.text.Font font = this.gc.getFont();
            TextAlignment textalign = this.gc.getTextAlign();
            VPos textbaseline = this.gc.getTextBaseline();
            FillRule fillRule = this.gc.getFillRule();

            // pop the last state from the stack because the clipping is a
            // common rendering attribute
            this.gc.restore();

            this.gc.setGlobalAlpha(globalAlpha);
            this.gc.setGlobalBlendMode(blendop);
            this.gc.setTransform(transform);
            this.gc.setFill(fill);
            this.gc.setStroke(stroke);
            this.gc.setLineWidth(linewidth);
            this.gc.setLineCap(linecap);
            this.gc.setLineJoin(linejoin);
            this.gc.setMiterLimit(miterlimit);
            this.gc.setFont(font);
            this.gc.setTextAlign(textalign);
            this.gc.setTextBaseline(textbaseline);
            this.gc.setFillRule(fillRule);

            this.activeClipping = false;
        }
    }

    private PixelRectangle getPixelRectangle(Bounds rectangle) {
        return new PixelRectangle(rectangle.getMinX(), rectangle.getMinY(), rectangle.getWidth(), rectangle.getHeight());
    }

    @Override
    public void setForeground(Color foregroundColor) {
        this.gc.setStroke(getUiColor(foregroundColor));
    }

    @Override
    public void setBackground(Color backgroundColor) {
        this.gc.setFill(getUiColor(backgroundColor));
    }

    @Override
    public javafx.scene.paint.Color getUiColor(Color color) {
        if (color.getNativeColor() == null) {
            color.setNativeColor(javafx.scene.paint.Color.rgb(color.red, color.green, color.blue, color.opacity));
        }
        if (!(color.getNativeColor() instanceof javafx.scene.paint.Color)) {
            throw new IllegalStateException("native color instance in color wrapper is invalid");
        }
        return (javafx.scene.paint.Color) color.getNativeColor();
    }

    @Override
    public void setFont(Font font) {
        this.gc.setFont(getUiFont(font));
    }

    @Override
    public javafx.scene.text.Font getUiFont(Font font) {
        if (font.getNativeFont() == null) {
            // if all attributes are not set, use the default font
            if (font.name == null && font.height == -1 && font.style == null) {
                font.setNativeFont(javafx.scene.text.Font.getDefault());
            }
            else {
                if (font.name != null && font.height == -1 && font.style == null) {
                    font.setNativeFont(javafx.scene.text.Font.font(font.name));
                }
                else if (font.name == null && font.height >= 0 && font.style == null) {
                    font.setNativeFont(javafx.scene.text.Font.font(font.height));
                }
                else if (font.name != null && font.height >= 0 && font.style == null) {
                    font.setNativeFont(javafx.scene.text.Font.font(font.name, font.height));
                }
                else if (font.style != null) {
                    javafx.scene.text.Font defaultFont = javafx.scene.text.Font.getDefault();
                    String fontName = (font.name == null) ? defaultFont.getName() : font.name;
                    double height = (font.height == -1) ? defaultFont.getSize() : font.height;

                    FontPosture posture = null;
                    FontWeight weight = null;
                    for (FontStyle style : font.style) {
                        switch (style) {
                            case ITALIC:
                                posture = FontPosture.ITALIC;
                                break;
                            case REGULAR:
                                posture = FontPosture.REGULAR;
                                break;
                            case BLACK:
                                weight = FontWeight.BLACK;
                                break;
                            case BOLD:
                                weight = FontWeight.BOLD;
                                break;
                            case EXTRA_BOLD:
                                weight = FontWeight.EXTRA_BOLD;
                                break;
                            case EXTRA_LIGHT:
                                weight = FontWeight.EXTRA_LIGHT;
                                break;
                            case LIGHT:
                                weight = FontWeight.LIGHT;
                                break;
                            case MEDIUM:
                                weight = FontWeight.MEDIUM;
                                break;
                            case NORMAL:
                                weight = FontWeight.NORMAL;
                                break;
                            case SEMI_BOLD:
                                weight = FontWeight.SEMI_BOLD;
                                break;
                            case THIN:
                                weight = FontWeight.THIN;
                                break;
                        }
                    }

                    if (posture != null && weight != null) {
                        font.setNativeFont(javafx.scene.text.Font.font(fontName, weight, posture, height));
                    }
                    else if (posture != null && weight == null) {
                        font.setNativeFont(javafx.scene.text.Font.font(fontName, posture, height));
                    }
                    else if (posture == null && weight != null) {
                        font.setNativeFont(javafx.scene.text.Font.font(fontName, weight, height));
                    }
                }
            }
        }
        if (!(font.getNativeFont() instanceof javafx.scene.text.Font)) {
            throw new IllegalStateException("native font instance in font wrapper is invalid");
        }
        return (javafx.scene.text.Font) font.getNativeFont();
    }

    @Override
    public void drawImage(Image image, double x, double y) {
        this.gc.drawImage(getUiImage(image), x, y);
    }

    @Override
    public javafx.scene.image.Image getUiImage(Image image) {
        if (image.getNativeImage() == null) {
            // TODO check scaling
            try (InputStream in = image.url.openStream()) {
                image.setNativeImage(new javafx.scene.image.Image(in));
            } catch (IOException e) {
                // TODO logging
            }
        }
        if (!(image.getNativeImage() instanceof javafx.scene.image.Image)) {
            throw new IllegalStateException("native image instance in image wrapper is invalid");
        }
        return (javafx.scene.image.Image) image.getNativeImage();
    }

    @Override
    public void setTransform(Transform transform) {
        if (transform == null) {
            // TODO reset transformation - need to be tested
            this.gc.rotate(0);
            this.gc.scale(0, 0);
            this.gc.translate(0, 0);
        }
        else {
            if (transform.hasRotation()) {
                this.gc.rotate(transform.getAngle());
            }

            if (transform.hasScaling()) {
                this.gc.scale(transform.getScaleX(), transform.getScaleY());
            }

            if (transform.hasTranslation()) {
                this.gc.translate(transform.getOffsetX(), transform.getOffsetY());
            }
        }
    }

}
