/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.search.event.SearchEvent;
import org.eclipse.nebula.widgets.nattable.search.strategy.AbstractSearchStrategy;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;

public class SearchGridCellsCommandHandler implements ILayerCommandHandler<SearchCommand> {

    private final SelectionLayer selectionLayer;
    private PositionCoordinate searchResultCellCoordinate;

    public SearchGridCellsCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public Class<SearchCommand> getCommandClass() {
        return SearchCommand.class;
    };

    @Override
    public boolean doCommand(ILayer targetLayer, SearchCommand searchCommand) throws PatternSyntaxException {
        searchCommand.convertToTargetLayer(targetLayer);

        final ILayerListener searchEventListener = searchCommand.getSearchEventListener();
        if (searchEventListener != null) {
            this.selectionLayer.addLayerListener(searchEventListener);
        }
        try {
            PositionCoordinate anchor = this.selectionLayer.getSelectionAnchor();
            if (anchor.columnPosition < 0 || anchor.rowPosition < 0) {
                anchor = new PositionCoordinate(this.selectionLayer, 0, 0);
            }
            Object dataValueToFind = null;
            if ((dataValueToFind = searchCommand.getSearchText()) == null) {
                dataValueToFind = this.selectionLayer.getDataValueByPosition(anchor.columnPosition, anchor.rowPosition);
            }

            boolean performActionOnResult = true;
            if (searchCommand.getSearchStrategy() instanceof AbstractSearchStrategy) {
                AbstractSearchStrategy searchStrategy = (AbstractSearchStrategy) searchCommand.getSearchStrategy();
                searchStrategy.setContextLayer(targetLayer);
                searchStrategy.setCaseSensitive(searchCommand.isCaseSensitive());
                searchStrategy.setWrapSearch(searchCommand.isWrapSearch());
                searchStrategy.setWholeWord(searchCommand.isWholeWord());
                searchStrategy.setIncremental(searchCommand.isIncremental());
                searchStrategy.setRegex(searchCommand.isRegex());
                searchStrategy.setIncludeCollapsed(searchCommand.isIncludeCollapsed());
                searchStrategy.setSearchDirection(searchCommand.getSearchDirection());
                searchStrategy.setComparator(searchCommand.getComparator());
                performActionOnResult = !searchStrategy.processResultInternally();
            }

            this.searchResultCellCoordinate = searchCommand.getSearchStrategy().executeSearch(dataValueToFind);

            this.selectionLayer.fireLayerEvent(new SearchEvent(this.searchResultCellCoordinate));

            if (performActionOnResult && this.searchResultCellCoordinate != null) {
                final SelectCellCommand command = new SelectCellCommand(
                        this.selectionLayer,
                        this.searchResultCellCoordinate.columnPosition,
                        this.searchResultCellCoordinate.rowPosition,
                        false,
                        false);
                command.setForcingEntireCellIntoViewport(true);
                this.selectionLayer.doCommand(command);
            }

        } finally {
            if (searchEventListener != null) {
                this.selectionLayer.removeLayerListener(searchEventListener);
            }
        }

        return true;
    }

    public PositionCoordinate getSearchResultCellCoordinate() {
        return this.searchResultCellCoordinate;
    }
}
