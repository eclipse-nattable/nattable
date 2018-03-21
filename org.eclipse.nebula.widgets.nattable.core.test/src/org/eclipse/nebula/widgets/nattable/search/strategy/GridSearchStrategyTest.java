/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class GridSearchStrategyTest {

    // Has 10 columns and 5 rows
    private DefaultGridLayer gridLayer;
    private SelectionLayer selectionLayer;
    private ConfigRegistry configRegistry;

    @Before
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

        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, false, ISearchDirection.SEARCH_BACKWARDS, true);
        gridStrategy.setComparator(new CellValueAsStringComparator<>());
        gridStrategy.setContextLayer(this.selectionLayer);

        PositionCoordinate searchResult = gridStrategy.executeSearch("[1,3]");
        assertNotNull(searchResult);
        assertEquals(1, searchResult.columnPosition);
        assertEquals(3, searchResult.rowPosition);
    }

    @Test
    public void shouldFindAllCellsWithValue() {
        GridSearchStrategy gridStrategy = new GridSearchStrategy(this.configRegistry, true, ISearchDirection.SEARCH_BACKWARDS, true);
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

}
