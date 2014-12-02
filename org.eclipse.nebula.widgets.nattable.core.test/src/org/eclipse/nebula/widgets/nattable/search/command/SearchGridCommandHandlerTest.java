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
        this.gridLayer = new GridLayerFixture();
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

        this.commandHandler = new SearchGridCellsCommandHandler(this.gridLayer
                .getBodyLayer().getSelectionLayer());
        selectCell(3, 3);
    }

    private boolean selectCell(int columnPosition, int rowPosition) {
        return this.gridLayer.doCommand(new SelectCellCommand(this.gridLayer,
                columnPosition, rowPosition, false, false));
    }

    private void doTest() throws PatternSyntaxException {
        // Register call back
        final ILayerListener listener = new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof SearchEvent) {
                    // Check event, coordinate should be in composite layer
                    // coordinates
                    SearchEvent searchEvent = (SearchEvent) event;
                    if (SearchGridCommandHandlerTest.this.expected != null) {
                        Assert.assertEquals(SearchGridCommandHandlerTest.this.expected.columnPosition,
                                searchEvent.getCellCoordinate()
                                        .getColumnPosition());
                        Assert.assertEquals(SearchGridCommandHandlerTest.this.expected.rowPosition, searchEvent
                                .getCellCoordinate().getRowPosition());
                    } else {
                        Assert.assertNull(searchEvent.getCellCoordinate());
                    }
                }
            }
        };
        this.gridLayer.addLayerListener(listener);
        try {
            SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                    .getSelectionLayer();
            final GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(
                    this.configRegistry, this.isWrapSearch, this.isColumnFirst);
            final SearchCommand searchCommand = new SearchCommand(this.searchText,
                    selectionLayer, gridSearchStrategy,
                    this.isForward ? ISearchDirection.SEARCH_FORWARD
                            : ISearchDirection.SEARCH_BACKWARDS, this.isWrapSearch,
                    this.isCaseSensitive, this.isWholeWord, this.isIncremental, this.isRegex,
                    this.isIncludeCollapsed,
                    new CellValueAsStringComparator<Comparable<String>>());
            this.commandHandler.doCommand(selectionLayer, searchCommand);

            final PositionCoordinate searchResultCellCoordinate = this.commandHandler
                    .getSearchResultCellCoordinate();
            if (this.expected != null) {
                Assert.assertEquals(this.expected.columnPosition,
                        searchResultCellCoordinate.columnPosition);
                Assert.assertEquals(this.expected.rowPosition,
                        searchResultCellCoordinate.rowPosition);
            } else {
                Assert.assertNull(searchResultCellCoordinate);
            }
        } finally {
            this.gridLayer.removeLayerListener(listener);
        }
    }

    @Test
    public void shouldFindTextInGrid() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[2,4]";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();

        this.isForward = false;

        this.searchText = "[2,3]";
        this.expected = new PositionCoordinate(null, 2, 3);
        doTest();

        this.searchText = "[2,4]";
        this.expected = null;
        doTest();
    }

    @Test
    public void shouldFindTextInGridIncrementally() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = true;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[";
        this.expected = new PositionCoordinate(null, 2, 2);
        doTest();
        this.searchText = "[2";
        this.expected = new PositionCoordinate(null, 2, 2);
        doTest();
        this.searchText = "[2,";
        this.expected = new PositionCoordinate(null, 2, 2);
        doTest();
        this.searchText = "[2,4";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();

        this.isForward = false;

        this.searchText = "[";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();
        this.searchText = "[2";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();
        this.searchText = "[2,";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();
        this.searchText = "[2,2";
        this.expected = new PositionCoordinate(null, 2, 2);
        doTest();
    }

    @Test
    public void shouldFindTextInGridNonIncrementally() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[";
        this.expected = new PositionCoordinate(null, 2, 3);
        doTest();
        this.searchText = "[2";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();
        this.searchText = "[2,4";
        this.expected = null;
        doTest();
    }

    @Test
    public void shouldFindTextInGridAfterWrapping() {
        this.isForward = true;
        this.isWrapSearch = true;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[2,2]";
        this.expected = new PositionCoordinate(null, 2, 2);
        doTest();

        this.isForward = false;

        this.isWrapSearch = false;
        this.searchText = "[2,4]";
        this.expected = null;
        doTest();

        this.isWrapSearch = true;
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();

        selectCell(0, 0);

        final int columnCount = this.gridLayer.getBodyLayer().getColumnCount();
        final int rowCount = this.gridLayer.getBodyLayer().getRowCount();
        this.searchText = "[" + String.valueOf(columnCount - 1) + ",";
        this.expected = new PositionCoordinate(null, columnCount - 1, rowCount - 1);
        doTest();

        this.isForward = true;
        this.searchText = "[0,";
        this.expected = new PositionCoordinate(null, 0, 0);
        doTest();
    }

    @Test
    public void shouldNotFindTextInGridWithoutWrapping() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[2,2]";
        this.expected = null;
        doTest();

        selectCell(0, 0);

        this.isForward = false;
        this.isWrapSearch = false;
        final int columnCount = this.gridLayer.getBodyLayer().getColumnCount();
        final int rowCount = this.gridLayer.getBodyLayer().getRowCount();
        this.searchText = "[" + String.valueOf(columnCount - 1) + ",";
        this.expected = null;
        doTest();

        selectCell(columnCount - 1, rowCount - 1);

        this.isForward = true;
        this.isWrapSearch = false;
        this.searchText = "[0,";
        this.expected = null;
        doTest();
    }

    @Test
    public void shouldFindRegexInGrid() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = true;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = ".2.4.";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();
    }

    @Test
    public void shouldFindRegexInColumnFirst() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = true;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = ".[23].[23].";
        this.expected = new PositionCoordinate(null, 2, 3);
        doTest();
    }

    @Test
    public void shouldFindRegexInRowFirst() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = false;
        this.isIncremental = false;
        this.isRegex = true;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = false;

        this.searchText = ".[23].[23].";
        this.expected = new PositionCoordinate(null, 3, 2);
        doTest();
    }

    @Test
    public void shouldNotFindInGridForBadRegex() {
        try {
            this.isForward = true;
            this.isWrapSearch = false;
            this.isCaseSensitive = false;
            this.isWholeWord = false;
            this.isIncremental = false;
            this.isRegex = true;
            this.isIncludeCollapsed = false;
            this.isColumnFirst = true;

            this.searchText = "[2";
            this.expected = null;
            doTest();
            Assert.fail("Invalid regex didn't throw as expected");
        } catch (PatternSyntaxException e) {}
    }

    @Test
    public void shouldFindWholeWordInGrid() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = true;
        this.isIncremental = false;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[2,4]";
        this.expected = new PositionCoordinate(null, 2, 4);
        doTest();
    }

    @Test
    public void shouldNotFindWholeWordInGrid() {
        this.isForward = true;
        this.isWrapSearch = false;
        this.isCaseSensitive = false;
        this.isWholeWord = true;
        this.isIncremental = false;
        this.isRegex = false;
        this.isIncludeCollapsed = false;
        this.isColumnFirst = true;

        this.searchText = "[2,4";
        this.expected = null;
        doTest();
    }
}
