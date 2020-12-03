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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 458537
 *     Thorsten Schlathölter <tschlat@gmx.de> - Bug 467047
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.SearchDirection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class GridSearchStrategy extends AbstractSearchStrategy {

    private final IConfigRegistry configRegistry;

    public GridSearchStrategy(IConfigRegistry configRegistry, boolean wrapSearch, boolean columnFirst) {
        this(configRegistry, wrapSearch, SearchDirection.SEARCH_FORWARD, columnFirst);
    }

    /**
     *
     * @param configRegistry
     *            The {@link ConfigRegistry}.
     * @param wrapSearch
     *            Flag to configure if the search should wrap.
     * @param searchDirection
     *            The {@link SearchDirection}.
     * @param columnFirst
     *            Flag to configure if the search should be by column.
     * @deprecated Use constructor with {@link SearchDirection} parameter
     */
    @Deprecated
    public GridSearchStrategy(IConfigRegistry configRegistry, boolean wrapSearch, String searchDirection, boolean columnFirst) {
        this(configRegistry, wrapSearch, SearchDirection.valueOf(searchDirection), columnFirst);
    }

    /**
     *
     * @param configRegistry
     *            The {@link ConfigRegistry}.
     * @param wrapSearch
     *            Flag to configure if the search should wrap.
     * @param searchDirection
     *            The {@link SearchDirection}.
     * @param columnFirst
     *            Flag to configure if the search should be by column.
     * @since 2.0
     */
    public GridSearchStrategy(IConfigRegistry configRegistry, boolean wrapSearch, SearchDirection searchDirection, boolean columnFirst) {
        this.configRegistry = configRegistry;
        this.wrapSearch = wrapSearch;
        this.searchDirection = searchDirection;
        this.columnFirst = columnFirst;
    }

    public static class GridRectangle {
        Range firstDim;
        Range secondDim;
    }

    @Override
    public PositionCoordinate executeSearch(Object valueToMatch) {

        ILayer contextLayer = getContextLayer();
        if (!(contextLayer instanceof SelectionLayer)) {
            throw new IllegalStateException("For the GridSearchStrategy to work it needs the selectionLayer to be passed as the contextLayer."); //$NON-NLS-1$
        }
        SelectionLayer selectionLayer = (SelectionLayer) contextLayer;
        PositionCoordinate selectionAnchor = selectionLayer.getSelectionAnchor();

        // Pick start and end values depending on the direction of the search.
        int direction = this.searchDirection.equals(SearchDirection.SEARCH_FORWARD) ? 1 : -1;

        boolean hadSelectionAnchor = selectionAnchor.columnPosition >= 0 && selectionAnchor.rowPosition >= 0;
        if (!hadSelectionAnchor) {
            selectionAnchor.columnPosition = 0;
            selectionAnchor.rowPosition = 0;
        }

        // Pick a first and second dimension based on whether it's a column or
        // row-first search.
        int firstDimPosition;
        int firstDimCount;
        int secondDimPosition;
        int secondDimCount;

        if (this.columnFirst) {
            firstDimPosition = selectionAnchor.columnPosition;
            firstDimCount = selectionLayer.getColumnCount();
            secondDimPosition = selectionAnchor.rowPosition;
            secondDimCount = selectionLayer.getRowCount();
        } else {
            firstDimPosition = selectionAnchor.rowPosition;
            firstDimCount = selectionLayer.getRowCount();
            secondDimPosition = selectionAnchor.columnPosition;
            secondDimCount = selectionLayer.getColumnCount();
        }

        int firstDimStart;
        int firstDimEnd;
        int secondDimStart;
        int secondDimEnd;
        if (direction == 1) {
            firstDimStart = 0;
            firstDimEnd = firstDimCount;
            secondDimStart = 0;
            secondDimEnd = secondDimCount;
        } else {
            firstDimStart = firstDimCount - 1;
            firstDimEnd = -1;
            secondDimStart = secondDimCount - 1;
            secondDimEnd = -1;
        }

        // Move to the next cell if a selection was active and it's not
        // an incremental search.
        final boolean startWithNextCell = hadSelectionAnchor && !isIncremental();
        if (startWithNextCell) {
            if (secondDimPosition + direction != secondDimEnd) {
                // Increment the second dimension
                secondDimPosition += direction;
            } else {
                // Wrap the second dimension
                secondDimPosition = secondDimStart;
                if (firstDimPosition + direction != firstDimEnd) {
                    // Increment the first dimension
                    firstDimPosition += direction;
                } else if (this.wrapSearch) {
                    // Wrap the first dimension
                    firstDimPosition = firstDimStart;
                } else {
                    // Fail outright because there's nothing to search
                    return null;
                }
            }
        }

        // Get a sequence of ranges for searching.
        List<GridRectangle> gridRanges = getRanges(
                firstDimPosition,
                secondDimPosition,
                direction,
                firstDimStart,
                firstDimEnd,
                secondDimStart,
                secondDimEnd);

        // Perform the search.
        @SuppressWarnings("unchecked")
        Comparator<String> comparator = (Comparator<String>) getComparator();
        return CellDisplayValueSearchUtil.findCell(
                getContextLayer(),
                this.configRegistry,
                gridRanges,
                valueToMatch,
                comparator,
                isCaseSensitive(),
                isWholeWord(),
                isRegex(),
                isColumnFirst(),
                isIncludeCollapsed());
    }

    /**
     * Divides the grid search into multiple, sequential ranges, with single
     * slices at the starting point and multiple slices elsewhere.
     *
     * @param firstDimPosition
     * @param secondDimPosition
     * @param direction
     * @param firstDimStart
     * @param firstDimEnd
     * @param secondDimStart
     * @param secondDimEnd
     * @return
     */
    private List<GridRectangle> getRanges(
            int firstDimPosition, int secondDimPosition, int direction,
            int firstDimStart, int firstDimEnd, int secondDimStart, int secondDimEnd) {

        List<GridRectangle> gridRanges = new ArrayList<>();
        GridRectangle gridRange;

        // One first-dimension slice starting at the second
        // dimension selection.
        gridRange = new GridRectangle();
        gridRange.firstDim = new Range(firstDimPosition, firstDimPosition + direction);
        gridRange.secondDim = new Range(secondDimPosition, secondDimEnd);
        gridRanges.add(gridRange);

        if (firstDimStart == 0 && firstDimEnd == 0) {
            // Bug 458537 - this might happen for a grid without content rows
            return gridRanges;
        }

        // One or more first-dimension slices to the wrapping boundary.
        gridRange = new GridRectangle();
        gridRange.firstDim = new Range(firstDimPosition + direction, firstDimEnd);
        gridRange.secondDim = new Range(secondDimStart, secondDimEnd);
        gridRanges.add(gridRange);

        // We're done if wrapping is not enabled or if we've already covered the
        // whole table.
        if (!this.wrapSearch
                || firstDimPosition == firstDimStart && secondDimPosition == secondDimStart) {
            return gridRanges;
        }

        // One or more first-dimension slices after wrapping, up to the
        // first-dimension slice with the starting point.
        gridRange = new GridRectangle();
        gridRange.firstDim = new Range(firstDimStart, firstDimPosition);
        gridRange.secondDim = new Range(secondDimStart, secondDimEnd);
        gridRanges.add(gridRange);

        // One first-dimension slice ending at the second-dimension selection.
        gridRange = new GridRectangle();
        gridRange.firstDim = new Range(firstDimPosition, firstDimPosition + direction);
        gridRange.secondDim = new Range(secondDimStart, secondDimPosition);
        gridRanges.add(gridRange);

        return gridRanges;
    }

}
