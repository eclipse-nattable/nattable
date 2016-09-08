/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Loris Securo <lorissek@gmail.com> - Bug 499701
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.nebula.widgets.nattable.persistence.ColorPersistor;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * This class defines the visual attributes of a Border.
 */
public class BorderStyle {

    private int thickness = 1;
    private Color color = GUIHelper.COLOR_BLACK;
    private LineStyleEnum lineStyle = LineStyleEnum.SOLID;
    private BorderModeEnum borderMode = BorderModeEnum.CENTERED;

    public enum LineStyleEnum {
        SOLID, DASHED, DOTTED, DASHDOT, DASHDOTDOT;

        public static int toSWT(LineStyleEnum line) {
            if (line == null)
                throw new IllegalArgumentException("null"); //$NON-NLS-1$
            if (line.equals(SOLID))
                return SWT.LINE_SOLID;
            else if (line.equals(DASHED))
                return SWT.LINE_DASH;
            else if (line.equals(DOTTED))
                return SWT.LINE_DOT;
            else if (line.equals(DASHDOT))
                return SWT.LINE_DASHDOT;
            else if (line.equals(DASHDOTDOT))
                return SWT.LINE_DASHDOTDOT;
            else
                return SWT.LINE_SOLID;
        }
    }

    /**
     * @since 1.5
     */
    public enum BorderModeEnum {
        CENTERED, INTERNAL, EXTERNAL;
    }

    public BorderStyle() {}

    public BorderStyle(int thickness, Color color, LineStyleEnum lineStyle) {
        this.thickness = thickness;
        this.color = color;
        this.lineStyle = lineStyle;
    }

    /**
     * @since 1.5
     */
    public BorderStyle(int thickness, Color color, LineStyleEnum lineStyle, BorderModeEnum borderMode) {
        this.thickness = thickness;
        this.color = color;
        this.lineStyle = lineStyle;
        this.borderMode = borderMode;
    }

    /**
     * Reconstruct this instance from the persisted String.
     *
     * @see BorderStyle#toString()
     */
    public BorderStyle(String string) {
        String[] tokens = string.split("\\|"); //$NON-NLS-1$

        this.thickness = Integer.parseInt(tokens[0]);
        this.color = ColorPersistor.asColor(tokens[1]);
        this.lineStyle = LineStyleEnum.valueOf(tokens[2]);
        if (tokens.length > 3) {
            this.borderMode = BorderModeEnum.valueOf(tokens[3]);
        }
    }

    public int getThickness() {
        return this.thickness;
    }

    public Color getColor() {
        return this.color;
    }

    public LineStyleEnum getLineStyle() {
        return this.lineStyle;
    }

    /**
     * @since 1.5
     */
    public BorderModeEnum getBorderMode() {
        return this.borderMode;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setLineStyle(LineStyleEnum lineStyle) {
        this.lineStyle = lineStyle;
    }

    /**
     * @since 1.5
     */
    public void setBorderMode(BorderModeEnum borderMode) {
        this.borderMode = borderMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BorderStyle other = (BorderStyle) obj;
        if (this.borderMode != other.borderMode)
            return false;
        if (this.color == null) {
            if (other.color != null)
                return false;
        } else if (!this.color.equals(other.color))
            return false;
        if (this.lineStyle != other.lineStyle)
            return false;
        if (this.thickness != other.thickness)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.borderMode == null) ? 0 : this.borderMode.hashCode());
        result = prime * result + ((this.color == null) ? 0 : this.color.hashCode());
        result = prime * result + ((this.lineStyle == null) ? 0 : this.lineStyle.hashCode());
        result = prime * result + this.thickness;
        return result;
    }

    /**
     * @return a human readable representation of the border style. This is
     *         suitable for constructing an equivalent instance using the
     *         BorderStyle(String) constructor
     */
    @Override
    public String toString() {
        return this.thickness + "|" + //$NON-NLS-1$
                ColorPersistor.asString(this.color) + "|" + //$NON-NLS-1$
                this.lineStyle + "|" + //$NON-NLS-1$
                this.borderMode;
    }
}