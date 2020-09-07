/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.painter;

import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialization of the {@link GridLineCellLayerPainter} to support rendering
 * of huge column groups in the performance {@link ColumnGroupHeaderLayer}.
 *
 * @since 1.6
 */
public class ColumnGroupHeaderGridLineCellLayerPainter extends GridLineCellLayerPainter {

    protected ColumnGroupHeaderLayer columnGroupHeaderLayer;

    /**
     * Create a ColumnGroupHeaderGridLineCellLayerPainter that renders gray grid
     * lines and uses the default clipping behaviour.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} used to check if huge
     *            spanning column groups should be rendered differently.
     */
    public ColumnGroupHeaderGridLineCellLayerPainter(ColumnGroupHeaderLayer columnGroupHeaderLayer) {
        this(columnGroupHeaderLayer, GUIHelper.COLOR_GRAY);
    }

    /**
     * Create a ColumnGroupHeaderGridLineCellLayerPainter that renders grid
     * lines in the specified color and uses the default clipping behaviour.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} used to check if huge
     *            spanning column groups should be rendered differently.
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public ColumnGroupHeaderGridLineCellLayerPainter(ColumnGroupHeaderLayer columnGroupHeaderLayer, Color gridColor) {
        super(gridColor);
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    /**
     * Create a ColumnGroupHeaderGridLineCellLayerPainter that renders gray grid
     * lines and uses the specified clipping behaviour.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} used to check if huge
     *            spanning column groups should be rendered differently.
     * @param clipLeft
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the left cell will be clipped, if set to
     *            <code>false</code> the right cell will be clipped. The default
     *            value is <code>false</code>.
     * @param clipTop
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the top cell will be clipped, if set to
     *            <code>false</code> the bottom cell will be clipped. The
     *            default value is <code>false</code>.
     */
    public ColumnGroupHeaderGridLineCellLayerPainter(ColumnGroupHeaderLayer columnGroupHeaderLayer, boolean clipLeft, boolean clipTop) {
        this(columnGroupHeaderLayer, GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    /**
     * Create a ColumnGroupHeaderGridLineCellLayerPainter that renders grid
     * lines in the specified color and uses the specified clipping behaviour.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} used to check if huge
     *            spanning column groups should be rendered differently.
     * @param gridColor
     *            The color that should be used to render the grid lines.
     * @param clipLeft
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the left cell will be clipped, if set to
     *            <code>false</code> the right cell will be clipped. The default
     *            value is <code>false</code>.
     * @param clipTop
     *            Configure the rendering behaviour when cells overlap. If set
     *            to <code>true</code> the top cell will be clipped, if set to
     *            <code>false</code> the bottom cell will be clipped. The
     *            default value is <code>false</code>.
     */
    public ColumnGroupHeaderGridLineCellLayerPainter(ColumnGroupHeaderLayer columnGroupHeaderLayer, Color gridColor, boolean clipLeft, boolean clipTop) {
        super(gridColor, clipLeft, clipTop);
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
    }

    @Override
    public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle bounds) {
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(columnPosition, rowPosition);
        if (this.columnGroupHeaderLayer.isShowAlwaysGroupNames()
                && cell.getOriginRowPosition() < this.columnGroupHeaderLayer.getLevelCount()
                && this.columnGroupHeaderLayer.isPartOfAGroup(columnPosition)
                && bounds.x < 0) {
            bounds.width += bounds.x;
            bounds.x = 0;
        }
        return super.adjustCellBounds(columnPosition, rowPosition, bounds);
    }

}
