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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
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
	
	public Class<SearchCommand> getCommandClass() {
		return SearchCommand.class;
	};
	
	public boolean doCommand(ILayer targetLayer, SearchCommand searchCommand) {
		searchCommand.convertToTargetLayer(targetLayer);
		
		AbstractSearchStrategy searchStrategy = (AbstractSearchStrategy) searchCommand.getSearchStrategy();
		if (searchCommand.getSearchEventListener() != null) {
			selectionLayer.addLayerListener(searchCommand.getSearchEventListener());
		}
		PositionCoordinate anchor = selectionLayer.getSelectionAnchor();
		if (anchor.columnPosition < 0 || anchor.rowPosition < 0) {
			anchor = new PositionCoordinate(selectionLayer, 0, 0);
		}
		searchStrategy.setContextLayer(targetLayer);
		Object dataValueToFind = null;
		if ((dataValueToFind = searchCommand.getSearchText()) == null) {
			dataValueToFind = selectionLayer.getDataValueByPosition(anchor.columnPosition, anchor.rowPosition);
		}
		
		searchStrategy.setCaseSensitive(searchCommand.isCaseSensitive());
		searchStrategy.setWrapSearch(searchCommand.isWrapSearch());
		searchStrategy.setSearchDirection(searchCommand.getSearchDirection());
		searchStrategy.setComparator(searchCommand.getComparator());
		searchResultCellCoordinate = searchStrategy.executeSearch(dataValueToFind);
		
		selectionLayer.fireLayerEvent(new SearchEvent(searchResultCellCoordinate));
		if (searchResultCellCoordinate != null) {
			final SelectCellCommand command = new SelectCellCommand(selectionLayer, searchResultCellCoordinate.columnPosition, searchResultCellCoordinate.rowPosition, false, false);
			command.setForcingEntireCellIntoViewport(true);
			selectionLayer.doCommand(command);
		}
		
		return true;
	}
	
	public PositionCoordinate getSearchResultCellCoordinate() {
		return searchResultCellCoordinate;
	}
}
