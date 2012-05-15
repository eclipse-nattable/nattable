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


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.strategy.ColumnSearchStrategy;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnSearchStrategyTest {

	private ILayer layer;
	private ConfigRegistry configRegistry;

	@Before
	public void setUp() {
		DefaultGridLayer gridLayer = new GridLayerFixture();
		layer = gridLayer.getBodyLayer().getSelectionLayer();
		configRegistry = new ConfigRegistry();
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
	}

	@Test
	public void shouldAccessCellsInSelectedColumn() {
		// Choose three columns for searching
		ColumnSearchStrategy columnSearchStrategy = new ColumnSearchStrategy(new int[]{2,5,8}, configRegistry);
		columnSearchStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		PositionCoordinate[] cellsToSearch = columnSearchStrategy.getColumnCellsToSearch(layer);

		PositionCoordinate cell = cellsToSearch[0];
		Assert.assertEquals(2, cell.columnPosition);
		cell = cellsToSearch[5];
		Assert.assertEquals(5, cell.columnPosition);
		cell = cellsToSearch[10];
		Assert.assertEquals(8, cell.columnPosition);

		Assert.assertEquals(15, cellsToSearch.length);
	}

	@Test
	public void shouldSearchAllBodyCellsForColumnInSelection() {
		ColumnSearchStrategy columnSearchStrategy = new ColumnSearchStrategy(new int[]{2,5,8}, configRegistry);
		columnSearchStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		columnSearchStrategy.setContextLayer(layer);
		PositionCoordinate cell = columnSearchStrategy.executeSearch("[2,2]");
		Assert.assertEquals(2, cell.getColumnPosition());
		Assert.assertEquals(2, cell.getRowPosition());
	}
}
