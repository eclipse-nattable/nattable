/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.IndexCoordinate;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.swt.widgets.Event;

/**
 * {@link FormulaErrorReporter} implementation that shows formula errors in a
 * tooltip. Extends {@link NatTableContentTooltip} for showing the error.
 *
 * @since 1.4
 */
public class FormulaTooltipErrorReporter extends NatTableContentTooltip implements FormulaErrorReporter {

    protected Map<IndexCoordinate, String> formulaErrors = new HashMap<IndexCoordinate, String>();
    protected IUniqueIndexLayer bodyDataLayer;

    /**
     * Creates a {@link FormulaTooltipErrorReporter} for the given
     * {@link NatTable} instance that shows formula error tooltips for
     * {@link GridRegion#BODY}.
     *
     * @param natTable
     *            The {@link NatTable} to which the tooltip should be bound.
     * @param bodyDataLayer
     *            The {@link DataLayer} of the body region, needed to perform
     *            index transformations.
     */
    public FormulaTooltipErrorReporter(NatTable natTable, IUniqueIndexLayer bodyDataLayer) {
        this(natTable, bodyDataLayer, GridRegion.BODY);
    }

    /**
     * Creates a {@link FormulaTooltipErrorReporter} for the given
     * {@link NatTable} instance that shows formula error tooltips for the given
     * grid regions.
     *
     * <p>
     * This constructor is intended to be used for subclassing, if additionally
     * to formula errors, tooltips should be shown in other grid regions too.
     * Note that in this case {@link FormulaTooltipErrorReporter#getText(Event)}
     * needs to be overriden too.
     * </p>
     *
     * @param natTable
     *            The {@link NatTable} to which the tooltip should be bound.
     * @param bodyDataLayer
     *            The {@link DataLayer} of the body region, needed to perform
     *            index transformations.
     * @param tooltipRegions
     *            The regions for which this {@link FormulaTooltipErrorReporter}
     *            should be activated.
     */
    public FormulaTooltipErrorReporter(NatTable natTable, IUniqueIndexLayer bodyDataLayer, String... tooltipRegions) {
        super(natTable, tooltipRegions);
        this.bodyDataLayer = bodyDataLayer;
    }

    @Override
    protected String getText(Event event) {
        int col = this.natTable.getColumnPositionByX(event.x);
        int row = this.natTable.getRowPositionByY(event.y);

        int colIdx = LayerUtil.convertColumnPosition(this.natTable, col, this.bodyDataLayer);
        int rowIdx = LayerUtil.convertRowPosition(this.natTable, row, this.bodyDataLayer);

        IndexCoordinate coord = new IndexCoordinate(colIdx, rowIdx);
        if (this.formulaErrors.containsKey(coord)) {
            return this.formulaErrors.get(coord);
        }

        return null;
    }

    @Override
    public void addFormulaError(int column, int row, String message) {
        this.formulaErrors.put(new IndexCoordinate(column, row), message);
    }

    @Override
    public void clearFormulaError(int column, int row) {
        this.formulaErrors.remove(new IndexCoordinate(column, row));
    }

    @Override
    public boolean hasFormulaError(int column, int row) {
        return this.formulaErrors.containsKey(new IndexCoordinate(column, row));
    }

    @Override
    public String getFormulaError(int column, int row) {
        return this.formulaErrors.get(new IndexCoordinate(column, row));
    }

}
