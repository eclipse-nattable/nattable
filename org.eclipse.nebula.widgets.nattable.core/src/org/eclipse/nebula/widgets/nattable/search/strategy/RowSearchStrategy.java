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
package org.eclipse.nebula.widgets.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;

public class RowSearchStrategy extends AbstractSearchStrategy {

    private int[] rowPositions;
    private int startingColumnPosition;
    private final IConfigRegistry configRegistry;

    public RowSearchStrategy(int[] rowPositions, IConfigRegistry configRegistry) {
        this(rowPositions, 0, configRegistry, ISearchDirection.SEARCH_FORWARD);
    }

    public RowSearchStrategy(int[] rowPositions, int startingColumnPosition, IConfigRegistry configRegistry, String searchDirection) {
        this.rowPositions = rowPositions;
        this.startingColumnPosition = startingColumnPosition;
        this.configRegistry = configRegistry;
        this.searchDirection = searchDirection;
    }

    @Override
    public PositionCoordinate executeSearch(Object valueToMatch) {
        @SuppressWarnings("unchecked")
        Comparator<String> comparator = (Comparator<String>) getComparator();
        return CellDisplayValueSearchUtil.findCell(
                getContextLayer(),
                this.configRegistry,
                getRowCellsToSearch(getContextLayer()),
                valueToMatch,
                comparator,
                isCaseSensitive(),
                isWholeWord(),
                isRegex(),
                isIncludeCollapsed());
    }

    public void setStartingColumnPosition(int startingColumnPosition) {
        this.startingColumnPosition = startingColumnPosition;
    }

    public void setRowPositions(int[] rowPositions) {
        this.rowPositions = rowPositions;
    }

    protected PositionCoordinate[] getRowCellsToSearch(ILayer contextLayer) {
        List<PositionCoordinate> cellsToSearch = new ArrayList<>();
        int columnPosition = this.startingColumnPosition;
        // See how many columns we can add, depends on where the search is
        // starting from
        final int columnCount = contextLayer.getColumnCount();
        int width;
        if (this.searchDirection.equals(ISearchDirection.SEARCH_FORWARD)) {
            width = columnCount - this.startingColumnPosition;
        } else {
            width = this.startingColumnPosition;
        }
        for (int rowIndex = 0; rowIndex < this.rowPositions.length; rowIndex++) {
            final int startingRowPosition = this.rowPositions[rowIndex];
            if (this.searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
                cellsToSearch.addAll(CellDisplayValueSearchUtil.getDescendingCellCoordinatesRowFirst(
                        getContextLayer(),
                        columnPosition,
                        startingRowPosition,
                        width,
                        1));
                columnPosition = columnCount - 1;
            } else {
                cellsToSearch.addAll(CellDisplayValueSearchUtil.getCellCoordinatesRowFirst(
                        getContextLayer(),
                        columnPosition,
                        startingRowPosition,
                        width,
                        1));
                columnPosition = 0;
            }
            width = columnCount;
            // After first row is set, start the next row from the top
        }
        return cellsToSearch.toArray(new PositionCoordinate[0]);
    }
}
