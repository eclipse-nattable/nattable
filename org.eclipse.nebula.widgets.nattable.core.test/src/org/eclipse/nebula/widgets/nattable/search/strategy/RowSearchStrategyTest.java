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

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class RowSearchStrategyTest {

    private DefaultGridLayer gridLayer;
    private ILayer layer;
    private ConfigRegistry configRegistry;

    @Before
    public void setUp() {
        this.gridLayer = new GridLayerFixture();
        this.layer = this.gridLayer.getBodyLayer().getSelectionLayer();

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration().configureRegistry(this.configRegistry);
    }

    @Test
    public void shouldAccessCellInSelectedRow() {
        // Select three rows for searching
        RowSearchStrategy rowStrategy = new RowSearchStrategy(new int[] { 0, 2, 4 }, this.configRegistry);
        PositionCoordinate[] cellsToSearch = rowStrategy.getRowCellsToSearch(this.layer);

        PositionCoordinate cell = cellsToSearch[0];
        assertEquals(0, cell.getRowPosition());

        cell = cellsToSearch[10];
        assertEquals(2, cell.getRowPosition());

        cell = cellsToSearch[20];
        assertEquals(4, cell.getRowPosition());
        assertEquals(30, cellsToSearch.length);

        // the SelectionLayer is not set as context layer, therefore nothing
        // should happen
        assertEquals(0, this.gridLayer.getBodyLayer().getSelectionLayer().getSelectedCells().size());
    }

    @Test
    public void shouldSearchAllBodyCellsForRowInSelection() {
        RowSearchStrategy rowStrategy = new RowSearchStrategy(new int[] { 2, 0, 4 }, this.configRegistry);
        rowStrategy.setComparator(new CellValueAsStringComparator<>());
        rowStrategy.setContextLayer(this.layer);

        PositionCoordinate cell = rowStrategy.executeSearch("[2,2]");
        assertEquals(2, cell.getColumnPosition());
        assertEquals(2, cell.getRowPosition());
    }
}
