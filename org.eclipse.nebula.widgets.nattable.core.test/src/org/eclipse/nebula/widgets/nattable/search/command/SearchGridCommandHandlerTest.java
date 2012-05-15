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
package org.eclipse.nebula.widgets.nattable.search.command;


import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.search.command.SearchCommand;
import org.eclipse.nebula.widgets.nattable.search.command.SearchGridCellsCommandHandler;
import org.eclipse.nebula.widgets.nattable.search.event.SearchEvent;
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

public class SearchGridCommandHandlerTest {
	
	private SearchGridCellsCommandHandler commandHandler;
	// Has 10 columns and 5 rows
	private GridLayerFixture gridLayer;
	private ConfigRegistry configRegistry;
	
	@Before
	public void setUp() {
		gridLayer = new GridLayerFixture();
		gridLayer.setClientAreaProvider(new IClientAreaProvider() {

			public Rectangle getClientArea() {
				return new Rectangle(0,0,1050,250);
			}
			
		});
		gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
		
		
		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
		
		commandHandler = new SearchGridCellsCommandHandler(gridLayer.getBodyLayer().getSelectionLayer());
	}
	
	@Test
	public void shouldFindTextInGrid() {
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 3, 3, false, false));
		// Register call back
		gridLayer.addLayerListener(new ILayerListener() {
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof SearchEvent) {
					// Check event, coordinate should be in composite layer coordinates
					SearchEvent searchEvent = (SearchEvent)event;
					Assert.assertEquals(2, searchEvent.getCellCoordinate().getColumnPosition());
					Assert.assertEquals(4, searchEvent.getCellCoordinate().getRowPosition());
				}
			}
		});
		
		SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		commandHandler.doCommand(selectionLayer, new SearchCommand("[2,4]", selectionLayer, new GridSearchStrategy(configRegistry, false), ISearchDirection.SEARCH_FORWARD, false, false, new CellValueAsStringComparator<Comparable<String>>()));
		
		final PositionCoordinate searchResultCellCoordinate = commandHandler.getSearchResultCellCoordinate();
		Assert.assertEquals(2, searchResultCellCoordinate.columnPosition);
		Assert.assertEquals(4, searchResultCellCoordinate.rowPosition);
	}
	
	@Test
	public void shouldFindTextInGridAfterWrapping() {
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 3, 3, false, false));
		
		// Register Call back
		gridLayer.addLayerListener(new ILayerListener() {
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof SearchEvent) {
					// Check event, coordinate should be in composite layer coordinates
					SearchEvent searchEvent = (SearchEvent)event;
					Assert.assertEquals(2, searchEvent.getCellCoordinate().getColumnPosition());
					Assert.assertEquals(2, searchEvent.getCellCoordinate().getRowPosition());
				}
			}
		});
		SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
		commandHandler.doCommand(selectionLayer, new SearchCommand("[2,2]", selectionLayer, new GridSearchStrategy(configRegistry, true), ISearchDirection.SEARCH_FORWARD, true, false, new CellValueAsStringComparator<Comparable<String>>()));
		
		final PositionCoordinate searchResultCellCoordinate = commandHandler.getSearchResultCellCoordinate();
		Assert.assertEquals(2, searchResultCellCoordinate.columnPosition);
		Assert.assertEquals(2, searchResultCellCoordinate.rowPosition);
	}	
}
