/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Thorsten Schlathölter <tschlat@gmx.de> - Bug 467047
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.search.strategy.GridSearchStrategy.GridRectangle;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class CellDisplayValueSearchUtil {

    static List<PositionCoordinate> getCellCoordinates(
            ILayer contextLayer,
            int startingColumnPosition,
            int startingRowPosition,
            int width,
            int height) {
        List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
        for (int columnPosition = 0; columnPosition < width; columnPosition++) {
            for (int rowPosition = 0; rowPosition < height; rowPosition++) {
                PositionCoordinate coordinate = new PositionCoordinate(
                        contextLayer,
                        startingColumnPosition,
                        startingRowPosition++);
                coordinates.add(coordinate);
            }
            startingColumnPosition++;
        }
        return coordinates;
    }

    static List<PositionCoordinate> getDescendingCellCoordinates(
            ILayer contextLayer,
            int startingColumnPosition,
            int startingRowPosition,
            int width,
            int height) {
        List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
        for (int columnPosition = width; columnPosition >= 0 && startingColumnPosition >= 0; columnPosition--) {
            for (int rowPosition = height; rowPosition >= 0 && startingRowPosition >= 0; rowPosition--) {
                PositionCoordinate coordinate = new PositionCoordinate(
                        contextLayer,
                        startingColumnPosition,
                        startingRowPosition--);
                coordinates.add(coordinate);
            }
            startingColumnPosition--;
        }
        return coordinates;
    }

    static List<PositionCoordinate> getCellCoordinatesRowFirst(
            ILayer contextLayer,
            int startingColumnPosition,
            int startingRowPosition,
            int width,
            int height) {
        List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
        for (int rowPosition = 0; rowPosition < height; rowPosition++) {
            for (int columnPosition = 0; columnPosition < width; columnPosition++) {
                PositionCoordinate coordinate = new PositionCoordinate(
                        contextLayer,
                        startingColumnPosition++,
                        startingRowPosition);
                coordinates.add(coordinate);
            }
            startingRowPosition++;
        }
        return coordinates;
    }

    static List<PositionCoordinate> getDescendingCellCoordinatesRowFirst(
            ILayer contextLayer,
            int startingColumnPosition,
            int startingRowPosition,
            int width,
            int height) {
        List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
        for (int rowPosition = height; rowPosition >= 0 && startingRowPosition >= 0; rowPosition--) {
            for (int columnPosition = width; columnPosition >= 0 && startingColumnPosition >= 0; columnPosition--) {
                PositionCoordinate coordinate = new PositionCoordinate(
                        contextLayer,
                        startingColumnPosition--,
                        startingRowPosition);
                coordinates.add(coordinate);
            }
            startingRowPosition--;
        }
        return coordinates;
    }

    /**
     * Finds the first matching cell in a list of cells.
     *
     * @param layer
     * @param configRegistry
     * @param cellsToSearch
     * @param valueToMatch
     * @param comparator
     * @param caseSensitive
     * @param wholeWord
     * @param regex
     * @param includeCollapsed
     *            TODO currently ignored
     * @return
     * @throws PatternSyntaxException
     */
    static PositionCoordinate findCell(
            final ILayer layer,
            final IConfigRegistry configRegistry,
            final PositionCoordinate[] cellsToSearch,
            final Object valueToMatch,
            final Comparator<String> comparator,
            final boolean caseSensitive,
            final boolean wholeWord,
            final boolean regex,
            final boolean includeCollapsed) throws PatternSyntaxException {
        String stringValue = caseSensitive ? valueToMatch.toString() : valueToMatch.toString().toLowerCase();
        Pattern pattern = regex ? Pattern.compile(stringValue) : null;
        for (int cellIndex = 0; cellIndex < cellsToSearch.length; cellIndex++) {
            final PositionCoordinate cellCoordinate = cellsToSearch[cellIndex];
            if (compare(
                    layer,
                    configRegistry,
                    pattern,
                    stringValue,
                    comparator,
                    caseSensitive,
                    wholeWord,
                    regex,
                    cellCoordinate.columnPosition,
                    cellCoordinate.rowPosition)) {
                return cellCoordinate;
            }
        }
        return null;
    }

    /**
     * Finds the first matching cell in a list of grid cell rectangles.
     *
     * @param layer
     * @param configRegistry
     * @param cellRectangles
     * @param valueToMatch
     * @param comparator
     * @param caseSensitive
     * @param wholeWord
     * @param regex
     * @param includeCollapsed
     *            TODO currently ignored
     * @return
     * @throws PatternSyntaxException
     */
    static PositionCoordinate findCell(
            final ILayer layer,
            final IConfigRegistry configRegistry,
            final List<GridRectangle> cellRectangles,
            final Object valueToMatch,
            final Comparator<String> comparator,
            final boolean caseSensitive,
            final boolean wholeWord,
            final boolean regex,
            final boolean columnFirst,
            final boolean includeCollapsed) throws PatternSyntaxException {
        String stringValue = caseSensitive ? valueToMatch.toString() : valueToMatch.toString().toLowerCase();
        Pattern pattern = regex ? Pattern.compile(stringValue) : null;
        for (GridRectangle cellRectangle : cellRectangles) {
            int direction = cellRectangle.firstDim.size() > 0 || cellRectangle.secondDim.size() > 0 ? 1 : -1;
            for (int i = cellRectangle.firstDim.start; Math.abs(cellRectangle.firstDim.end - i) > 0; i += direction) {
                PositionCoordinate result = findCell(
                        layer,
                        configRegistry,
                        i,
                        cellRectangle.secondDim.start,
                        cellRectangle.secondDim.end,
                        direction,
                        pattern,
                        stringValue,
                        comparator,
                        caseSensitive,
                        wholeWord,
                        regex,
                        columnFirst,
                        includeCollapsed);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Finds the first matching cell in a table slice.
     *
     * @param layer
     * @param configRegistry
     * @param firstDimIndex
     * @param secondDimStart
     * @param secondDimEnd
     * @param direction
     * @param pattern
     * @param stringValue
     * @param comparator
     * @param caseSensitive
     * @param wholeWord
     * @param regex
     * @param columnFirst
     * @param includeCollapsed
     * @return
     * @throws PatternSyntaxException
     */
    private static PositionCoordinate findCell(
            ILayer layer,
            IConfigRegistry configRegistry,
            int firstDimIndex,
            int secondDimStart,
            int secondDimEnd,
            int direction,
            Pattern pattern,
            String stringValue,
            Comparator<String> comparator,
            boolean caseSensitive,
            boolean wholeWord,
            boolean regex,
            final boolean columnFirst,
            boolean includeCollapsed) throws PatternSyntaxException {

        int columnPosition;
        int rowPosition;
        if (columnFirst) {
            columnPosition = firstDimIndex;
            rowPosition = secondDimStart;
        } else {
            columnPosition = secondDimStart;
            rowPosition = firstDimIndex;
        }

        for (int i = secondDimStart; direction * (secondDimEnd - i) > 0; i += direction) {
            ILayerCell cellByPosition = layer.getCellByPosition(columnPosition, rowPosition);
            PositionCoordinate searchAnchor = getSearchAnchor(cellByPosition, direction);

            // If we do not hit the searchAnchor with our current position it
            // means that we have hit a spanned cell somewhere else than in the
            // top left (for direction == 1) or bottom right (for direction ==
            // -1). That in turn means that we have already visited that cell.
            // Thus we skip the compare and proceed to the next position.
            if (searchAnchor.columnPosition == columnPosition && searchAnchor.rowPosition == rowPosition) {
                if (compare(
                        layer,
                        configRegistry,
                        pattern,
                        stringValue,
                        comparator,
                        caseSensitive,
                        wholeWord,
                        regex,
                        columnPosition,
                        rowPosition)) {
                    return new PositionCoordinate(layer, columnPosition, rowPosition);
                }
            }

            if (columnFirst) {
                rowPosition += direction;
            } else {
                columnPosition += direction;
            }
        }
        return null;
    }

    /**
     * Get an anchor for the search of the given cell.
     *
     * @param cell
     * @param direction
     * @return
     */
    private static PositionCoordinate getSearchAnchor(ILayerCell cell, int direction) {
        if (direction > 0) {
            // Return the original position of the cell (upper left corner)
            return new PositionCoordinate(cell.getLayer(), cell.getOriginColumnPosition(), cell.getOriginRowPosition());
        }

        // Return the lower right corner as we are approaching bottom up.
        return new PositionCoordinate(
                cell.getLayer(),
                cell.getOriginColumnPosition() + cell.getColumnSpan() - 1,
                cell.getOriginRowPosition() + cell.getRowSpan() - 1);
    }

    private static boolean compare(
            ILayer layer,
            IConfigRegistry configRegistry,
            Pattern pattern,
            String stringValue,
            Comparator<String> comparator,
            boolean caseSensitive,
            boolean wholeWord,
            boolean regex,
            int columnPosition,
            int rowPosition) {

        // Convert cell's data
        final IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                DisplayMode.NORMAL,
                layer.getConfigLabelsByPosition(columnPosition, rowPosition).getLabels());
        Object dataValue = null;
        if (displayConverter != null) {
            ILayerCell cell = layer.getCellByPosition(columnPosition, rowPosition);
            if (cell != null) {
                dataValue = displayConverter.canonicalToDisplayValue(cell, configRegistry, cell.getDataValue());
            }
        }

        // Compare with valueToMatch
        if (dataValue instanceof Comparable<?>) {
            String dataValueString = caseSensitive ? dataValue.toString() : dataValue.toString().toLowerCase();
            if (regex) {
                if (pattern.matcher(dataValueString).matches()) {
                    return true;
                }
            } else if (comparator.compare(stringValue, dataValueString) == 0) {
                return true;
            } else if (!wholeWord && dataValueString.contains(stringValue)) {
                return true;
            }
        }
        return false;
    }
}
