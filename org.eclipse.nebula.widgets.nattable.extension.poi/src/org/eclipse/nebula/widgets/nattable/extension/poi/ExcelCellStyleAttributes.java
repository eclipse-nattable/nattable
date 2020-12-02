/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.poi;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;

public class ExcelCellStyleAttributes {

    private final Color fg;
    private final Color bg;
    private final FontData fontData;
    private final String dataFormat;
    private final int hAlign;
    private final int vAlign;
    private final boolean vertical;
    private final boolean wrap;
    private final boolean border;

    public ExcelCellStyleAttributes(Color fg, Color bg, FontData fontData,
            String dataFormat, int hAlign, int vAlign, boolean vertical) {
        this(fg, bg, fontData, dataFormat, hAlign, vAlign, vertical, false, false);
    }

    /**
     *
     * @param fg
     *            The foreground color.
     * @param bg
     *            The background color.
     * @param fontData
     *            The font data.
     * @param dataFormat
     *            The data format.
     * @param hAlign
     *            The horizontal alignment.
     * @param vAlign
     *            The vertical alignment.
     * @param vertical
     *            Flag to indicate that text is rendered vertically.
     * @param wrap
     *            Flag to indicate that word wrapping is enabled.
     * @param border
     *            Flag to indicate that borders should be applied.
     *
     * @since 1.5
     */
    public ExcelCellStyleAttributes(Color fg, Color bg, FontData fontData,
            String dataFormat, int hAlign, int vAlign, boolean vertical, boolean wrap, boolean border) {
        this.fg = fg;
        this.bg = bg;
        this.fontData = fontData;
        this.dataFormat = dataFormat;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.vertical = vertical;
        this.wrap = wrap;
        this.border = border;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.bg == null) ? 0 : this.bg.hashCode());
        result = prime * result + ((this.dataFormat == null) ? 0 : this.dataFormat.hashCode());
        result = prime * result + ((this.fg == null) ? 0 : this.fg.hashCode());
        result = prime * result + ((this.fontData == null) ? 0 : this.fontData.hashCode());
        result = prime * result + this.hAlign;
        result = prime * result + this.vAlign;
        result = prime * result + (this.vertical ? 1231 : 1237);
        result = prime * result + (this.wrap ? 1231 : 1237);
        result = prime * result + (this.border ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExcelCellStyleAttributes other = (ExcelCellStyleAttributes) obj;
        if (this.bg == null) {
            if (other.bg != null)
                return false;
        } else if (!this.bg.equals(other.bg))
            return false;
        if (this.dataFormat == null) {
            if (other.dataFormat != null)
                return false;
        } else if (!this.dataFormat.equals(other.dataFormat))
            return false;
        if (this.fg == null) {
            if (other.fg != null)
                return false;
        } else if (!this.fg.equals(other.fg))
            return false;
        if (this.fontData == null) {
            if (other.fontData != null)
                return false;
        } else if (!this.fontData.equals(other.fontData))
            return false;
        if (this.hAlign != other.hAlign)
            return false;
        if (this.vAlign != other.vAlign)
            return false;
        if (this.vertical != other.vertical)
            return false;
        if (this.wrap != other.wrap)
            return false;
        if (this.border != other.border)
            return false;
        return true;
    }

}
