/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;

public class ColumnSearchStrategy extends AbstractSearchStrategy {

    private int[] columnPositions;
    private int startingRowPosition;
    private final String searchDirection;
    private final IConfigRegistry configRegistry;

    public ColumnSearchStrategy(int[] columnPositions, IConfigRegistry configRegistry) {
        this(columnPositions, 0, configRegistry, ISearchDirection.SEARCH_FORWARD);
    }

    public ColumnSearchStrategy(int[] columnPositions, int startingRowPosition, IConfigRegistry configRegistry, String searchDirection) {
        this.columnPositions = columnPositions;
        this.startingRowPosition = startingRowPosition;
        this.configRegistry = configRegistry;
        this.searchDirection = searchDirection;
    }

    @Override
    public PositionCoordinate executeSearch(Object valueToMatch) throws PatternSyntaxException {
        @SuppressWarnings("unchecked")
        Comparator<String> comparator = (Comparator<String>) getComparator();
        return CellDisplayValueSearchUtil.findCell(
                getContextLayer(),
                this.configRegistry,
                getColumnCellsToSearch(getContextLayer()),
                valueToMatch,
                comparator,
                isCaseSensitive(),
                isWholeWord(),
                isRegex(),
                isIncludeCollapsed());
    }

    public void setStartingRowPosition(int startingRowPosition) {
        this.startingRowPosition = startingRowPosition;
    }

    public void setColumnPositions(int[] columnPositions) {
        this.columnPositions = columnPositions;
    }

    protected PositionCoordinate[] getColumnCellsToSearch(ILayer contextLayer) {
        List<PositionCoordinate> cellsToSearch = new ArrayList<PositionCoordinate>();
        int rowPosition = this.startingRowPosition;
        // See how many rows we can add, depends on where the search is starting
        // from
        final int rowCount = contextLayer.getRowCount();
        int height;
        if (this.searchDirection.equals(ISearchDirection.SEARCH_FORWARD)) {
            height = rowCount - this.startingRowPosition;
        } else {
            height = this.startingRowPosition;
        }
        for (int columnIndex = 0; columnIndex < this.columnPositions.length; columnIndex++) {
            final int startingColumnPosition = this.columnPositions[columnIndex];
            if (this.searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
                cellsToSearch.addAll(CellDisplayValueSearchUtil.getDescendingCellCoordinates(
                        getContextLayer(),
                        startingColumnPosition,
                        rowPosition,
                        1,
                        height));
                rowPosition = rowCount - 1;
            } else {
                cellsToSearch.addAll(CellDisplayValueSearchUtil.getCellCoordinates(
                        getContextLayer(),
                        startingColumnPosition,
                        rowPosition,
                        1,
                        height));
                rowPosition = 0;
            }
            height = rowCount;
            // After first column is set, start the next column from the top
        }
        return cellsToSearch.toArray(new PositionCoordinate[0]);
    }
}
