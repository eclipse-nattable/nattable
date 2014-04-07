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


import java.util.regex.PatternSyntaxException;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
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
	
	String searchText;
	boolean isForward;
	boolean isWrapSearch;
	boolean isCaseSensitive;
	boolean isWholeWord;
	boolean isIncremental;
	boolean isRegex;
	boolean isIncludeCollapsed;
	boolean isColumnFirst;
	PositionCoordinate expected;

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
		selectCell(3, 3);
	}

	private boolean selectCell(int columnPosition, int rowPosition) {
		return gridLayer.doCommand(new SelectCellCommand(gridLayer, columnPosition, rowPosition, false, false));
	}
	
	private void doTest() throws PatternSyntaxException {
		// Register call back
		final ILayerListener listener = new ILayerListener() {
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof SearchEvent) {
					// Check event, coordinate should be in composite layer coordinates
					SearchEvent searchEvent = (SearchEvent)event;
					if (expected != null) {
						Assert.assertEquals(expected.columnPosition, searchEvent.getCellCoordinate().getColumnPosition());
						Assert.assertEquals(expected.rowPosition, searchEvent.getCellCoordinate().getRowPosition());
					} else {
						Assert.assertNull(searchEvent.getCellCoordinate());
					}
				}
			}
		};
		gridLayer.addLayerListener(listener);
		try {
			SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
			final GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(
					configRegistry, isWrapSearch, isColumnFirst);
			final SearchCommand searchCommand = new SearchCommand(
					searchText, selectionLayer, gridSearchStrategy,
					isForward ? ISearchDirection.SEARCH_FORWARD : ISearchDirection.SEARCH_BACKWARDS,
					isWrapSearch, isCaseSensitive,
					isWholeWord, isIncremental, isRegex,
					isIncludeCollapsed, new CellValueAsStringComparator<Comparable<String>>());
			commandHandler.doCommand(selectionLayer, searchCommand);

			final PositionCoordinate searchResultCellCoordinate = commandHandler.getSearchResultCellCoordinate();
			if (expected != null) {
				Assert.assertEquals(expected.columnPosition, searchResultCellCoordinate.columnPosition);
				Assert.assertEquals(expected.rowPosition, searchResultCellCoordinate.rowPosition);
			} else {
				Assert.assertNull(searchResultCellCoordinate);
			}
		} finally {
			gridLayer.removeLayerListener(listener);
		}
	}

	@Test
	public void shouldFindTextInGrid() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;

		searchText = "[2,4]";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		
		isForward = false;
		
		searchText = "[2,3]";
		expected = new PositionCoordinate(null, 2, 3);
		doTest();
		
		searchText = "[2,4]";
		expected = null;
		doTest();
	}

	@Test
	public void shouldFindTextInGridIncrementally() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = true;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;

		searchText = "[";
		expected = new PositionCoordinate(null, 2, 2);
		doTest();
		searchText = "[2";
		expected = new PositionCoordinate(null, 2, 2);
		doTest();
		searchText = "[2,";
		expected = new PositionCoordinate(null, 2, 2);
		doTest();
		searchText = "[2,4";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		
		isForward = false;
		
		searchText = "[";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		searchText = "[2";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		searchText = "[2,";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		searchText = "[2,2";
		expected = new PositionCoordinate(null, 2, 2);
		doTest();
	}

	@Test
	public void shouldFindTextInGridNonIncrementally() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;

		searchText = "[";
		expected = new PositionCoordinate(null, 2, 3);
		doTest();
		searchText = "[2";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		searchText = "[2,4";
		expected = null;
		doTest();
	}

	@Test
	public void shouldFindTextInGridAfterWrapping() {
		isForward = true;
		isWrapSearch = true;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;
		
		searchText = "[2,2]";
		expected = new PositionCoordinate(null, 2, 2);
		doTest();
		
		isForward = false;
		
		isWrapSearch = false;
		searchText = "[2,4]";
		expected = null;
		doTest();
		
		isWrapSearch = true;
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
		
		selectCell(0, 0);
		
		final int columnCount = gridLayer.getBodyLayer().getColumnCount();
		final int rowCount = gridLayer.getBodyLayer().getRowCount();
		searchText = "[" + String.valueOf(columnCount - 1) + ",";
		expected = new PositionCoordinate(null, columnCount - 1, rowCount - 1);
		doTest();
		
		isForward = true;
		searchText = "[0,";
		expected = new PositionCoordinate(null, 0, 0);
		doTest();
	}

	@Test
	public void shouldNotFindTextInGridWithoutWrapping() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;
		
		searchText = "[2,2]";
		expected = null;
		doTest();

		selectCell(0, 0);
		
		isForward = false;
		isWrapSearch = false;
		final int columnCount = gridLayer.getBodyLayer().getColumnCount();
		final int rowCount = gridLayer.getBodyLayer().getRowCount();
		searchText = "[" + String.valueOf(columnCount - 1) + ",";
		expected = null;
		doTest();
		
		selectCell(columnCount - 1, rowCount - 1);
		
		isForward = true;
		isWrapSearch = false;
		searchText = "[0,";
		expected = null;
		doTest();
	}

	@Test
	public void shouldFindRegexInGrid() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = true;
		isIncludeCollapsed = false;
		isColumnFirst = true;
		
		searchText = ".2.4.";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
	}

	@Test
	public void shouldFindRegexInColumnFirst() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = true;
		isIncludeCollapsed = false;
		isColumnFirst = true;
		
		searchText = ".[23].[23].";
		expected = new PositionCoordinate(null, 2, 3);
		doTest();	
	}

	@Test
	public void shouldFindRegexInRowFirst() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = false;
		isIncremental = false;
		isRegex = true;
		isIncludeCollapsed = false;
		isColumnFirst = false;
		
		searchText = ".[23].[23].";
		expected = new PositionCoordinate(null, 3, 2);
		doTest();	
	}

	@Test
	public void shouldNotFindInGridForBadRegex() {
		try {
			isForward = true;
			isWrapSearch = false;
			isCaseSensitive = false;
			isWholeWord = false;
			isIncremental = false;
			isRegex = true;
			isIncludeCollapsed = false;
			isColumnFirst = true;
			
			searchText = "[2";
			expected = null;
			doTest();
			Assert.fail("Invalid regex didn't throw as expected");
		} catch (PatternSyntaxException e) {
		}
	}

	@Test
	public void shouldFindWholeWordInGrid() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = true;
		isIncremental = false;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;
		
		searchText = "[2,4]";
		expected = new PositionCoordinate(null, 2, 4);
		doTest();
	}

	@Test
	public void shouldNotFindWholeWordInGrid() {
		isForward = true;
		isWrapSearch = false;
		isCaseSensitive = false;
		isWholeWord = true;
		isIncremental = false;
		isRegex = false;
		isIncludeCollapsed = false;
		isColumnFirst = true;
		
		searchText = "[2,4";
		expected = null;
		doTest();
	}
}
