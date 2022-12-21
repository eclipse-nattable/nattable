/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.SearchDirection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.DataProviderFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GridSearchStrategyTest {

    // Has 10 columns and 5 rows
    private DefaultGridLayer gridLayer;
    private SelectionLayer selectionLayer;
    private ConfigRegistry configRegistry;

    private DefaultGridLayer spanningGridLayer;
    private SelectionLayer spanningSelectionLayer;
    private ConfigRegistry spanningConfigRegistry;

    @BeforeEach
    public void setUp() {
        this.gridLayer = new DefaultGridLayer(getBodyDataProvider(), GridLayerFixture.colHeaderDataProvider);
        this.selectionLayer = this.gridLayer.getBodyLayer().getSelectionLayer();
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(
                        new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration().configureRegistry(this.configRegistry);

        // spanning
        this.spanningGridLayer = new DefaultGridLayer(
                new SpanningDataLayer(getSpanningBodyDataProvider()),
                new DefaultColumnHeaderDataLayer(GridLayerFixture.colHeaderDataProvider),
                new DefaultRowHeaderDataLayer(new DefaultRowHeaderDataProvider(GridLayerFixture.rowHeaderDataProvider)),
                new DataLayer(GridLayerFixture.cornerDataProvider));
        this.spanningSelectionLayer = this.spanningGridLayer.getBodyLayer().getSelectionLayer();
        this.spanningGridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.spanningGridLayer.doCommand(
                new ClientAreaResizeCommand(
                        new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.spanningConfigRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration().configureRegistry(this.spanningConfigRegistry);

    }

    public IDataProvider getBodyDataProvider() {
        return new IDataProvider() {
            final IDataProvider bodyDataProvider = GridLayerFixture.bodyDataProvider;

            @Override
            public int getColumnCount() {
                return this.bodyDataProvider.getColumnCount();
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                Object dataValue = null;
                if (columnIndex == 2 && rowIndex == 2) {
                    dataValue = "body";
                } else if (columnIndex == 4 && rowIndex == 4) {
                    dataValue = "Body";
                } else if (columnIndex == 3 && rowIndex == 3) {
                    dataValue = "Body";
                } else if (columnIndex == 0 && rowIndex == 0) {
                    dataValue = "Body";
                } else {
                    dataValue = this.bodyDataProvider.getDataValue(columnIndex, rowIndex);
                }
                return dataValue;
            }

            @Override
            public int getRowCount() {
                return this.bodyDataProvider.getRowCount();
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                this.bodyDataProvider.setDataValue(columnIndex, rowIndex, newValue);
            }

        };
    }

    public ISpanningDataProvider getSpanningBodyDataProvider() {
        return new ISpanningDataProvider() {
            final IDataProvider bodyDataProvider = new DataProviderFixture(10, 10);

            @Override
            public DataCell getCellByPosition(int columnPosition, int rowPosition) {
                if (columnPosition == 0 && rowPosition <= 2) {
                    return new DataCell(0, 0, 1, 3);
                } else if (columnPosition == 2 && (rowPosition == 1 || rowPosition == 2)) {
                    return new DataCell(2, 1, 1, 2);
                }
                return new DataCell(columnPosition, rowPosition);
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                Object dataValue = null;
                if (columnIndex == 0 && rowIndex <= 2) {
                    dataValue = "body";
                } else if (columnIndex == 2 && (rowIndex == 1 || rowIndex == 2)) {
                    dataValue = "body";
                } else if (columnIndex == 4 && (rowIndex == 1 || rowIndex == 2)) {
                    dataValue = "body";
                } else {
                    dataValue = this.bodyDataProvider.getDataValue(columnIndex, rowIndex);
                }
                return dataValue;
            }

            @Override
            public int getColumnCount() {
                return this.bodyDataProvider.getColumnCount();
            }

            @Override
            public int getRowCount() {
                return this.bodyDataProvider.getRowCount();
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                this.bodyDataProvider.setDataValue(columnIndex, rowIndex, newValue);
            }

        };
    }

    @Test
    public void searchShouldWrapAroundColumn() {
        // Select search starting point in composite coordinates
        this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, false));

        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, false, true);

        // If we don't specify to wrap the search, it will not find it.
        gridStrategy.setContextLayer(this.selectionLayer);
        gridStrategy.setCaseSensitive(true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());

        PositionCoordinate searchResult = gridStrategy.executeSearch("body");
        assertNull(searchResult);

        // Should find it when wrap search is enabled.
        gridStrategy.setWrapSearch(true);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(2, searchResult.columnPosition);
        assertEquals(2, searchResult.rowPosition);
    }

    @Test
    public void searchShouldWrapAroundRow() {
        // Select search starting point in composite coordinates
        this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, false));

        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, false, true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());
        // If we don't specify to wrap the search, it will not find it.
        gridStrategy.setContextLayer(this.selectionLayer);

        PositionCoordinate searchResult = gridStrategy.executeSearch("[1,3]");
        assertNull(searchResult);

        // Should find it when wrap search is enabled.
        gridStrategy.setWrapSearch(true);

        searchResult = gridStrategy.executeSearch("[1,3]");
        assertNotNull(searchResult);
        assertEquals(1, searchResult.columnPosition);
        assertEquals(3, searchResult.rowPosition);
    }

    @Test
    public void searchShouldMoveBackwardsToFindCell() {
        // Select search starting point in composite coordinates
        this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, false));

        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, false, SearchDirection.SEARCH_BACKWARDS, true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());
        gridStrategy.setContextLayer(this.selectionLayer);

        PositionCoordinate searchResult = gridStrategy.executeSearch("[1,3]");
        assertNotNull(searchResult);
        assertEquals(1, searchResult.columnPosition);
        assertEquals(3, searchResult.rowPosition);
    }

    @Test
    public void shouldFindAllCellsWithValue() {
        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, true, SearchDirection.SEARCH_BACKWARDS, true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());
        gridStrategy.setContextLayer(this.selectionLayer);
        gridStrategy.setCaseSensitive(true);
        gridStrategy.setWrapSearch(true);

        PositionCoordinate searchResult = gridStrategy.executeSearch("Body");
        assertEquals(0, searchResult.columnPosition);
        assertEquals(0, searchResult.rowPosition);

        // Simulate selecting the search result
        this.selectionLayer.doCommand(
                new SelectCellCommand(
                        this.selectionLayer,
                        searchResult.columnPosition,
                        searchResult.rowPosition,
                        false,
                        false));

        searchResult = gridStrategy.executeSearch("Body");
        assertEquals(4, searchResult.columnPosition);
        assertEquals(4, searchResult.rowPosition);

        // Simulate selecting the search result
        this.selectionLayer.doCommand(
                new SelectCellCommand(
                        this.selectionLayer,
                        searchResult.columnPosition,
                        searchResult.rowPosition,
                        false,
                        false));

        searchResult = gridStrategy.executeSearch("Body");
        assertEquals(3, searchResult.columnPosition);
        assertEquals(3, searchResult.rowPosition);
    }

    @Test
    public void searchShouldNotFindWithSkipSearchLabel() {
        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, false, true);

        // If we don't specify to wrap the search, it will not find it.
        gridStrategy.setContextLayer(this.selectionLayer);
        gridStrategy.setCaseSensitive(true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());

        PositionCoordinate searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(2, searchResult.columnPosition);
        assertEquals(2, searchResult.rowPosition);

        // clear the selection
        this.selectionLayer.clear();

        // configure skip search label
        ColumnOverrideLabelAccumulator accumulator = new ColumnOverrideLabelAccumulator(this.selectionLayer);
        accumulator.registerColumnOverrides(2, ISearchStrategy.SKIP_SEARCH_RESULT_LABEL);
        this.selectionLayer.setConfigLabelAccumulator(accumulator);

        searchResult = gridStrategy.executeSearch("body");
        assertNull(searchResult);
    }

    @Test
    public void searchShouldHandleSpannedCellsForward() {
        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.spanningConfigRegistry, true, true);

        gridStrategy.setContextLayer(this.spanningSelectionLayer);
        gridStrategy.setCaseSensitive(true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());

        PositionCoordinate searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(0, searchResult.columnPosition);
        assertEquals(0, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(0, 0);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(2, searchResult.columnPosition);
        assertEquals(1, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(2, 1);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(4, searchResult.columnPosition);
        assertEquals(1, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(4, 1);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(4, searchResult.columnPosition);
        assertEquals(2, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(4, 2);

        // because of wrap, start from scratch
        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(0, searchResult.columnPosition);
        assertEquals(0, searchResult.rowPosition);
    }

    @Test
    public void searchShouldHandleSpannedCellsBackwards() {
        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.spanningConfigRegistry, true, SearchDirection.SEARCH_BACKWARDS, true);

        // start after the last expected search result
        this.spanningSelectionLayer.setSelectedCell(5, 5);

        gridStrategy.setContextLayer(this.spanningSelectionLayer);
        gridStrategy.setCaseSensitive(true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());

        PositionCoordinate searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(4, searchResult.columnPosition);
        assertEquals(2, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(4, 2);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(4, searchResult.columnPosition);
        assertEquals(1, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(4, 1);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(2, searchResult.columnPosition);
        assertEquals(1, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(2, 1);

        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(0, searchResult.columnPosition);
        assertEquals(0, searchResult.rowPosition);
        this.spanningSelectionLayer.setSelectedCell(0, 0);

        // because of wrap, start from scratch
        searchResult = gridStrategy.executeSearch("body");
        assertNotNull(searchResult);
        assertEquals(4, searchResult.columnPosition);
        assertEquals(2, searchResult.rowPosition);
    }

}
