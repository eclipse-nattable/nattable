/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tooltip;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.CellDisplayConversionUtils;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.PasswordTextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

/**
 * {@link ToolTip} implementation for the {@link NatTable} which will show the
 * display value of the cell of which the tooltip is requested.
 * <p>
 * It is possible to configure for which regions the tooltips should be
 * activated. If none are configured, the tooltips are active for every region
 * of the {@link NatTable}.
 *
 * @author Dirk Fauth
 * @version 1.0.0
 */
public class NatTableContentTooltip extends DefaultToolTip {

    /**
     * The {@link NatTable} instance for which this {@link ToolTip} is used.
     */
    protected NatTable natTable;
    /**
     * The regions of the {@link NatTable} for which this {@link ToolTip} is
     * active.
     */
    protected String[] tooltipRegions;

    /**
     * Creates a new {@link ToolTip} object, attaches it to the given
     * {@link NatTable} instance and configures and activates it.
     *
     * @param natTable
     *            The {@link NatTable} instance for which this {@link ToolTip}
     *            is used.
     * @param tooltipRegions
     *            The regions of the {@link NatTable} for which this
     *            {@link ToolTip} is active. If none are given, the tooltip will
     *            be active for all regions.
     */
    public NatTableContentTooltip(NatTable natTable, String... tooltipRegions) {
        super(natTable, ToolTip.NO_RECREATE, false);
        setPopupDelay(500);
        setShift(new Point(10, 10));
        activate();
        this.natTable = natTable;
        this.tooltipRegions = tooltipRegions;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Implementation here means the tooltip is not redrawn unless mouse hover
     * moves outside of the current cell (the combination of ToolTip.NO_RECREATE
     * style and override of this method).
     */
    @Override
    protected Object getToolTipArea(Event event) {
        int col = this.natTable.getColumnPositionByX(event.x);
        int row = this.natTable.getRowPositionByY(event.y);

        return new Point(col, row);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Evaluates the cell for which the tooltip should be rendered and checks
     * the display value. If the display value is empty <code>null</code> will
     * be returned which will result in not showing a tooltip.
     */
    @Override
    protected String getText(Event event) {
        int col = this.natTable.getColumnPositionByX(event.x);
        int row = this.natTable.getRowPositionByY(event.y);

        ILayerCell cell = this.natTable.getCellByPosition(col, row);
        if (cell != null) {
            // if the registered cell painter is the PasswordCellPainter, there
            // will be no tooltip
            ICellPainter painter = this.natTable.getConfigRegistry()
                    .getConfigAttribute(CellConfigAttributes.CELL_PAINTER,
                            DisplayMode.NORMAL,
                            cell.getConfigLabels().getLabels());
            if (isVisibleContentPainter(painter)) {
                String tooltipValue = CellDisplayConversionUtils
                        .convertDataType(cell, this.natTable.getConfigRegistry());

                if (tooltipValue.length() > 0) {
                    return tooltipValue;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the given {@link ICellPainter} is showing the content directly
     * or if it is anonymized by using the {@link PasswordTextPainter}
     *
     * @param painter
     *            The {@link ICellPainter} to check.
     * @return <code>true</code> if the painter is not a
     *         {@link PasswordTextPainter}
     */
    protected boolean isVisibleContentPainter(ICellPainter painter) {
        if (painter instanceof PasswordTextPainter) {
            return false;
        } else if (painter instanceof CellPainterWrapper) {
            return isVisibleContentPainter(((CellPainterWrapper) painter)
                    .getWrappedPainter());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Will only display a tooltip if the value of the cell for which the
     * tooltip should be rendered is not empty.
     * <p>
     * If there are regions configured for which the tooltip should be visible,
     * it is also checked if the the region for which the tooltip should be
     * rendered is in one of the configured tooltip regions.
     */
    @Override
    protected boolean shouldCreateToolTip(Event event) {
        // check the region?
        boolean regionCheckPassed = false;
        if (this.tooltipRegions.length > 0) {
            LabelStack regionLabels = this.natTable.getRegionLabelsByXY(event.x,
                    event.y);
            if (regionLabels != null) {
                for (String label : this.tooltipRegions) {
                    if (regionLabels.hasLabel(label)) {
                        regionCheckPassed = true;
                        break;
                    }
                }
            }
        } else {
            regionCheckPassed = true;
        }

        if (regionCheckPassed && getText(event) != null) {
            return super.shouldCreateToolTip(event);
        }

        return false;
    }

}
