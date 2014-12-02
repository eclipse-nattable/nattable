/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.search.strategy.SelectionSearchStrategy;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectionSearchStrategyTest {

    private static final String CELL_VALUE = "EVEN_BODY_CELL";

    private DefaultGridLayer gridLayer;
    private ConfigRegistry configRegistry;
    private IDataProvider bodyDataProvider;

    @Before
    public void setUp() {
        this.bodyDataProvider = new IDataProvider() {
            @Override
            public int getColumnCount() {
                return GridLayerFixture.bodyDataProvider.getColumnCount();
            }

            @Override
            public int getRowCount() {
                return GridLayerFixture.bodyDataProvider.getRowCount();
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                if (columnIndex == 0 || columnIndex == 9) {
                    return CELL_VALUE;
                }
                return GridLayerFixture.bodyDataProvider.getDataValue(
                        columnIndex, rowIndex);
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex,
                    Object newValue) {
                throw new UnsupportedOperationException();
            }

        };

        this.gridLayer = new DefaultGridLayer(this.bodyDataProvider,
                GridLayerFixture.colHeaderDataProvider,
                GridLayerFixture.rowHeaderDataProvider,
                GridLayerFixture.cornerDataProvider);
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display
                .getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration()
                .configureRegistry(this.configRegistry);
    }

    @Test
    public void shouldAccessWhatIsInSelection() {
        // Select range of cells
        int startColumnPosition = 2;
        int startRowPosition = 1;
        final int lastColumn = 7;
        final int lastRow = 5;

        for (int columnPosition = startColumnPosition; columnPosition < lastColumn; columnPosition++) {
            for (int rowPosition = startRowPosition; rowPosition < lastRow; rowPosition++) {
                this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer,
                        columnPosition, rowPosition, false, true));
            }
        }

        // We should get 20 Cells from the body
        SelectionSearchStrategy selectionStrategy = new SelectionSearchStrategy(
                this.configRegistry);
        PositionCoordinate[] cellsToSearch = selectionStrategy
                .getSelectedCells(this.gridLayer.getBodyLayer().getSelectionLayer());
        Assert.assertEquals(20, cellsToSearch.length);
    }

    @Test
    public void shouldOnlySearchWhatIsSelected() {
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 1, 4, false, true));
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 2, 2, false, true));
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, true));
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 5, 4, false, true));
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 6, 2, false, true));

        Assert.assertEquals(5, this.gridLayer.getBodyLayer().getSelectionLayer()
                .getSelectedCellPositions().length);

        final SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                .getSelectionLayer();
        SelectionSearchStrategy selectionStrategy = new SelectionSearchStrategy(
                this.configRegistry);
        selectionStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        selectionStrategy.setContextLayer(selectionLayer);
        Assert.assertNull(selectionStrategy.executeSearch("[0,1]"));
        Assert.assertNotNull(selectionStrategy.executeSearch(CELL_VALUE));
        Assert.assertNotNull(selectionStrategy.executeSearch(CELL_VALUE));
        Assert.assertNull(selectionStrategy.executeSearch("[5,0]"));
    }

    @Test
    public void shouldSearchSelectionBackwards() {
        // Select entire grid
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 1, 1, false, false));
        this.gridLayer.doCommand(new SelectAllCommand());

        // Should find the first cell in grid
        final SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                .getSelectionLayer();
        SelectionSearchStrategy selectionStrategy = new SelectionSearchStrategy(
                this.configRegistry);
        selectionStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        selectionStrategy.setContextLayer(selectionLayer);

        PositionCoordinate positionCoordinate = selectionStrategy
                .executeSearch(CELL_VALUE);
        Assert.assertEquals(0, positionCoordinate.columnPosition);
        Assert.assertEquals(0, positionCoordinate.rowPosition);

        // Should find last cell
        selectionStrategy = new SelectionSearchStrategy(this.configRegistry,
                ISearchDirection.SEARCH_BACKWARDS, true);
        selectionStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        selectionStrategy.setContextLayer(selectionLayer);

        positionCoordinate = selectionStrategy.executeSearch(CELL_VALUE);
        Assert.assertEquals(9, positionCoordinate.columnPosition);
        Assert.assertEquals(4, positionCoordinate.rowPosition);
    }
}
