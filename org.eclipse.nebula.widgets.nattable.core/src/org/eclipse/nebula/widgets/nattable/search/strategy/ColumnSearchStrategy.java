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

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.SearchDirection;

public class ColumnSearchStrategy extends AbstractSearchStrategy {

    private int[] columnPositions;
    private int startingRowPosition;
    private final IConfigRegistry configRegistry;

    public ColumnSearchStrategy(int[] columnPositions, IConfigRegistry configRegistry) {
        this(columnPositions, 0, configRegistry, SearchDirection.SEARCH_FORWARD);
    }

    /**
     *
     * @param columnPositions
     *            The column positions to search in.
     * @param startingRowPosition
     *            The row position to start.
     * @param configRegistry
     *            The {@link ConfigRegistry}.
     * @param searchDirection
     *            The {@link SearchDirection}.
     * @deprecated Use constructor with {@link SearchDirection} parameter
     */
    @Deprecated
    public ColumnSearchStrategy(int[] columnPositions, int startingRowPosition, IConfigRegistry configRegistry, String searchDirection) {
        this(columnPositions, startingRowPosition, configRegistry, SearchDirection.valueOf(searchDirection));
    }

    /**
     *
     * @param columnPositions
     *            The column positions to search in.
     * @param startingRowPosition
     *            The row position to start.
     * @param configRegistry
     *            The {@link ConfigRegistry}.
     * @param searchDirection
     *            The {@link SearchDirection}.
     * @since 2.0
     */
    public ColumnSearchStrategy(int[] columnPositions, int startingRowPosition, IConfigRegistry configRegistry, SearchDirection searchDirection) {
        this.columnPositions = columnPositions;
        this.startingRowPosition = startingRowPosition;
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
        List<PositionCoordinate> cellsToSearch = new ArrayList<>();
        int rowPosition = this.startingRowPosition;
        // See how many rows we can add, depends on where the search is starting
        // from
        final int rowCount = contextLayer.getRowCount();
        int height;
        if (this.searchDirection.equals(SearchDirection.SEARCH_FORWARD)) {
            height = rowCount - this.startingRowPosition;
        } else {
            height = this.startingRowPosition;
        }
        for (int columnIndex = 0; columnIndex < this.columnPositions.length; columnIndex++) {
            final int startingColumnPosition = this.columnPositions[columnIndex];
            if (this.searchDirection.equals(SearchDirection.SEARCH_BACKWARDS)) {
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
