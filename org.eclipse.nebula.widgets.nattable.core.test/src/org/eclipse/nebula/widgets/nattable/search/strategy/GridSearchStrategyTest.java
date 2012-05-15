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
import org.eclipse.nebula.widgets.nattable.search.strategy.GridSearchStrategy;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
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

public class GridSearchStrategyTest {
	
	// Has 10 columns and 5 rows
	private DefaultGridLayer gridLayer;
	private ConfigRegistry configRegistry;
	
	@Before
	public void setUp() {
		gridLayer = new DefaultGridLayer(getBodyDataProvider(), GridLayerFixture.colHeaderDataProvider);
		gridLayer.setClientAreaProvider(new IClientAreaProvider() {

			public Rectangle getClientArea() {
				return new Rectangle(0,0,1050,250);
			}
			
		});
		gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
		
		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
	}
	
	public IDataProvider getBodyDataProvider() {
		return new IDataProvider() {
			final IDataProvider bodyDataProvider = GridLayerFixture.bodyDataProvider;
			public int getColumnCount() {
				return bodyDataProvider.getColumnCount();
			}

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
				}  else {
					dataValue= bodyDataProvider.getDataValue(columnIndex, rowIndex);
				}
				return dataValue;
			}

			public int getRowCount() {
				return bodyDataProvider.getRowCount();
			}

			public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
				bodyDataProvider.setDataValue(columnIndex, rowIndex, newValue);
			}
			
		};
	}
	
	@Test
	public void shouldAccessCorrectNumberOfRemainingColumns() {
		final SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		GridSearchStrategy gridStrategy = new GridSearchStrategy(configRegistry, false);
		int columnsArray[] = gridStrategy.getColumnsToSearchArray(selectionLayer.getColumnCount(), 5);
		Assert.assertEquals(5, columnsArray[0]);
		Assert.assertEquals(6, columnsArray[1]);
		Assert.assertEquals(7, columnsArray[2]);
		Assert.assertEquals(8, columnsArray[3]);
		Assert.assertEquals(9, columnsArray[4]);
		
		Assert.assertEquals(5, columnsArray.length);
	}
	
	@Test
	public void searchShouldWrapAroundColumn() {
		// Select search starting point in composite coordinates
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 3, 4, false, false));
		
		GridSearchStrategy gridStrategy = new GridSearchStrategy(configRegistry, false);
		
		// If we don't specify to wrap the search, it will not find it.
		final SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		gridStrategy.setContextLayer(selectionLayer);
		gridStrategy.setCaseSensitive(true);
		gridStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		Assert.assertNull(gridStrategy.executeSearch("body"));
		
		gridStrategy.setWrapSearch(true);
		// Should find it when wrap search is enabled.
		Assert.assertNotNull(gridStrategy.executeSearch("Body"));
	}
	
	@Test
	public void searchShouldWrapAroundRow() {
		// Select search starting point in composite coordinates
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 3, 4, false, false));
		
		GridSearchStrategy gridStrategy = new GridSearchStrategy(configRegistry, false);
		gridStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		// If we don't specify to wrap the search, it will not find it.
		final SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		gridStrategy.setContextLayer(selectionLayer);
		Assert.assertNull(gridStrategy.executeSearch("[1,3]"));
		
		gridStrategy.setWrapSearch(true);
		
		// Should find it when wrap search is enabled.
		Assert.assertNotNull(gridStrategy.executeSearch("[1,3]"));
	}
	
	@Test
	public void searchShouldMoveBackwardsToFindCell() {
		// Select search starting point in composite coordinates
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 3, 4, false, false));
		
		GridSearchStrategy gridStrategy = new GridSearchStrategy(configRegistry, false, ISearchDirection.SEARCH_BACKWARDS);
		gridStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		final SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		gridStrategy.setContextLayer(selectionLayer);
		
		Assert.assertNotNull(gridStrategy.executeSearch("[1,3]"));
	}
	
	@Test
	public void shouldFindAllCellsWithValue() {
		GridSearchStrategy gridStrategy = new GridSearchStrategy(configRegistry, true, ISearchDirection.SEARCH_BACKWARDS);
		gridStrategy.setComparator(new CellValueAsStringComparator<Comparable<String>>());
		final SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		gridStrategy.setContextLayer(selectionLayer);
		gridStrategy.setCaseSensitive(true);
		
		PositionCoordinate searchResult = gridStrategy.executeSearch("Body");
		Assert.assertEquals(0, searchResult.columnPosition);
		Assert.assertEquals(0, searchResult.rowPosition);
		
		gridStrategy.setWrapSearch(true);
		// Simulate selecting the search result
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, searchResult.columnPosition, searchResult.rowPosition, false, false));
		searchResult = gridStrategy.executeSearch("Body");
		//System.out.println(searchResult);
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, searchResult.columnPosition, searchResult.rowPosition, false, false));
		searchResult = gridStrategy.executeSearch("Body");
		//System.out.println(searchResult);
		Assert.assertEquals(3, searchResult.columnPosition);
		Assert.assertEquals(3, searchResult.rowPosition);
	}
}
