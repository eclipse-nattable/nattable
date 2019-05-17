/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.painter;

import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialization of the {@link GridLineCellLayerPainter} to support rendering
 * of huge row groups in the performance {@link RowGroupHeaderLayer}.
 *
 * @since 1.6
 */
public class RowGroupHeaderGridLineCellLayerPainter extends GridLineCellLayerPainter {

    protected RowGroupHeaderLayer rowGroupHeaderLayer;

    /**
     * Create a RowGroupHeaderGridLineCellLayerPainter that renders gray grid
     * lines and uses the default clipping behaviour.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} used to check if huge spanning
     *            row groups should be rendered differently.
     */
    public RowGroupHeaderGridLineCellLayerPainter(RowGroupHeaderLayer rowGroupHeaderLayer) {
        this(rowGroupHeaderLayer, GUIHelper.COLOR_GRAY);
    }

    /**
     * Create a RowGroupHeaderGridLineCellLayerPainter that renders grid lines
     * in the specified color and uses the default clipping behaviour.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} used to check if huge spanning
     *            row groups should be rendered differently.
     * @param gridColor
     *            The color that should be used to render the grid lines.
     */
    public RowGroupHeaderGridLineCellLayerPainter(RowGroupHeaderLayer rowGroupHeaderLayer, Color gridColor) {
        super(gridColor);
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    /**
     * Create a RowGroupHeaderGridLineCellLayerPainter that renders gray grid
     * lines and uses the specified clipping behaviour.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} used to check if huge spanning
     *            row groups should be rendered differently.
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
    public RowGroupHeaderGridLineCellLayerPainter(RowGroupHeaderLayer rowGroupHeaderLayer, boolean clipLeft, boolean clipTop) {
        this(rowGroupHeaderLayer, GUIHelper.COLOR_GRAY, clipLeft, clipTop);
    }

    /**
     * Create a RowGroupHeaderGridLineCellLayerPainter that renders grid lines
     * in the specified color and uses the specified clipping behaviour.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} used to check if huge spanning
     *            row groups should be rendered differently.
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
    public RowGroupHeaderGridLineCellLayerPainter(RowGroupHeaderLayer rowGroupHeaderLayer, Color gridColor, boolean clipLeft, boolean clipTop) {
        super(gridColor, clipLeft, clipTop);
        this.rowGroupHeaderLayer = rowGroupHeaderLayer;
    }

    @Override
    public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle bounds) {
        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(columnPosition, rowPosition);
        if (this.rowGroupHeaderLayer.isShowAlwaysGroupNames()
                && cell.getOriginColumnPosition() < this.rowGroupHeaderLayer.getLevelCount()
                && this.rowGroupHeaderLayer.isPartOfAGroup(columnPosition)
                && bounds.y < 0) {
            bounds.height += bounds.y;
            bounds.y = 0;
        }
        return super.adjustCellBounds(columnPosition, rowPosition, bounds);
    }

}
