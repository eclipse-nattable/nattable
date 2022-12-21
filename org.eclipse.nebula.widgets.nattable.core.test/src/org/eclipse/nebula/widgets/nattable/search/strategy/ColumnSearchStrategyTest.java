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

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnSearchStrategyTest {

    private DefaultGridLayer gridLayer;
    private ILayer layer;
    private ConfigRegistry configRegistry;

    @BeforeEach
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.layer = this.gridLayer.getBodyLayer().getSelectionLayer();
        this.configRegistry = new ConfigRegistry();
        this.configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new DefaultDisplayConverter());
    }

    @Test
    public void shouldAccessCellsInSelectedColumn() {
        // Choose three columns for searching
        ColumnSearchStrategy columnSearchStrategy = new ColumnSearchStrategy(new int[] { 2, 5, 8 }, this.configRegistry);
        columnSearchStrategy.setComparator(new CellValueAsStringComparator<>());
        PositionCoordinate[] cellsToSearch = columnSearchStrategy.getColumnCellsToSearch(this.layer);

        PositionCoordinate cell = cellsToSearch[0];
        assertEquals(2, cell.columnPosition);
        cell = cellsToSearch[5];
        assertEquals(5, cell.columnPosition);
        cell = cellsToSearch[10];
        assertEquals(8, cell.columnPosition);

        assertEquals(15, cellsToSearch.length);

        assertEquals(0, this.gridLayer.getBodyLayer().getSelectionLayer().getSelectedCells().size());
    }

    @Test
    public void shouldSearchAllBodyCellsForColumnInSelection() {
        ColumnSearchStrategy columnSearchStrategy = new ColumnSearchStrategy(new int[] { 2, 5, 8 }, this.configRegistry);
        columnSearchStrategy.setComparator(new CellValueAsStringComparator<>());
        columnSearchStrategy.setContextLayer(this.layer);

        PositionCoordinate cell = columnSearchStrategy.executeSearch("[2,2]");
        assertEquals(2, cell.getColumnPosition());
        assertEquals(2, cell.getRowPosition());
    }
}
