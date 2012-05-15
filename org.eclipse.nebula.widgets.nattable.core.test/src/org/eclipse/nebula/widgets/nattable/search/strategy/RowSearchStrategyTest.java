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
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.strategy.RowSearchStrategy;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowSearchStrategyTest {
	
	private ILayer layer;
	private ConfigRegistry configRegistry;
	
	@Before
	public void setUp() {
		DefaultGridLayer gridLayer = new GridLayerFixture();
		layer = gridLayer.getBodyLayer().getSelectionLayer();
		
		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
	}
	
	@Test
	public void shouldAccessCellInSelectedRow() {
		// Select three rows for searching
		RowSearchStrategy rowStrategy = new RowSearchStrategy(new int[]{0,2,4}, configRegistry);
		PositionCoordinate[] cellsToSearch = rowStrategy.getRowCellsToSearch(layer);
		PositionCoordinate cell = cellsToSearch[0];
		Assert.assertEquals(0, cell.getRowPosition());
		cell = cellsToSearch[10];
		Assert.assertEquals(2, cell.getRowPosition());
		cell = cellsToSearch[20];
		Assert.assertEquals(4, cell.getRowPosition());
		Assert.assertEquals(30, cellsToSearch.length);
	}
	@Test
	public void shouldSearchAllBodyCellsForRowInSelection() {
		RowSearchStrategy rowStrategy = new RowSearchStrategy(new int[]{2,0,4}, configRegistry);
		rowStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		rowStrategy.setContextLayer(layer);
		PositionCoordinate cell = rowStrategy.executeSearch("[2,2]");
		Assert.assertEquals(2, cell.getColumnPosition());
		Assert.assertEquals(2, cell.getRowPosition());
	}
}
