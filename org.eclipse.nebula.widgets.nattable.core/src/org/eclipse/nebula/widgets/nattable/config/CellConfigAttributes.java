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
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;

/**
 * Interface that specifies configuration attributes for cell rendering.
 */
public final class CellConfigAttributes {

    private CellConfigAttributes() {
        // private default constructor for constants class
    }

    /**
     * Attribute for configuring the ICellPainter that should be used to render
     * a cell.
     */
    public static final ConfigAttribute<ICellPainter> CELL_PAINTER = new ConfigAttribute<>();

    /**
     * Attribute for configuring the IStyle that should be used to render a
     * cell.
     */
    public static final ConfigAttribute<IStyle> CELL_STYLE = new ConfigAttribute<>();

    /**
     * Attribute for configuring the IDisplayConverter that should be used to
     * convert the data in a cell for rendering.
     */
    public static final ConfigAttribute<IDisplayConverter> DISPLAY_CONVERTER = new ConfigAttribute<>();

    /**
     * Attribute for configuring the Color that should be used to render the
     * grid lines. Will be interpreted by the GridLineCellLayerPainter.
     */
    public static final ConfigAttribute<Color> GRID_LINE_COLOR = new ConfigAttribute<>();

    /**
     * Attribute for configuring whether grid lines should be rendered or not.
     * Will be interpreted by the GridLineCellLayerPainter.
     */
    public static final ConfigAttribute<Boolean> RENDER_GRID_LINES = new ConfigAttribute<>();

    /**
     * Attribute for configuring the width of the grid lines. Is for example
     * used on printing to ensure the grid lines are always printed.
     *
     * @since 1.4
     */
    public static final ConfigAttribute<Integer> GRID_LINE_WIDTH = new ConfigAttribute<>();
}
