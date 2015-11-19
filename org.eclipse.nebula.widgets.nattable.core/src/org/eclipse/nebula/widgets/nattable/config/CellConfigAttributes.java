/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface CellConfigAttributes {

    /**
     * Attribute for configuring the ICellPainter that should be used to render
     * a cell.
     */
    ConfigAttribute<ICellPainter> CELL_PAINTER = new ConfigAttribute<ICellPainter>();

    /**
     * Attribute for configuring the IStyle that should be used to render a
     * cell.
     */
    ConfigAttribute<IStyle> CELL_STYLE = new ConfigAttribute<IStyle>();

    /**
     * Attribute for configuring the IDisplayConverter that should be used to
     * convert the data in a cell for rendering.
     */
    ConfigAttribute<IDisplayConverter> DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();

    /**
     * Attribute for configuring the Color that should be used to render the
     * grid lines. Will be interpreted by the GridLineCellLayerPainter.
     */
    ConfigAttribute<Color> GRID_LINE_COLOR = new ConfigAttribute<Color>();

    /**
     * Attribute for configuring whether grid lines should be rendered or not.
     * Will be interpreted by the GridLineCellLayerPainter.
     */
    ConfigAttribute<Boolean> RENDER_GRID_LINES = new ConfigAttribute<Boolean>();

    /**
     * Attribute for configuring the width of the grid lines. Is for example
     * used on printing to ensure the grid lines are always printed.
     *
     * @since 1.4
     */
    ConfigAttribute<Integer> GRID_LINE_WIDTH = new ConfigAttribute<Integer>();
}
