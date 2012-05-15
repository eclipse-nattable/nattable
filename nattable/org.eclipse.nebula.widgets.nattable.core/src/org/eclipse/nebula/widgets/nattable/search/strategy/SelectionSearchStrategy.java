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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;


public class SelectionSearchStrategy extends AbstractSearchStrategy {

	private final IConfigRegistry configRegistry;
	private final String searchDirection;

	public SelectionSearchStrategy(IConfigRegistry configRegistry) {
		this(configRegistry, ISearchDirection.SEARCH_FORWARD);
	}
	
	public SelectionSearchStrategy(IConfigRegistry configRegistry, String searchDirection) {
		this.configRegistry = configRegistry;
		this.searchDirection = searchDirection;
	}
	
	public PositionCoordinate executeSearch(Object valueToMatch) {
		ILayer contextLayer = getContextLayer();
		if (! (contextLayer instanceof SelectionLayer)) {
			throw new RuntimeException("For the GridSearchStrategy to work it needs the selectionLayer to be passed as the contextLayer."); //$NON-NLS-1$
		}
		SelectionLayer selectionLayer = (SelectionLayer)contextLayer;
		PositionCoordinate coordinate = CellDisplayValueSearchUtil.findCell(selectionLayer, configRegistry, getSelectedCells(selectionLayer), valueToMatch, getComparator(), isCaseSensitive());		
		return coordinate;
	}

	protected PositionCoordinate[] getSelectedCells(SelectionLayer selectionLayer) {
		PositionCoordinate[] selectedCells = null;
		if (searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
			List<PositionCoordinate> coordinates = Arrays.asList(selectionLayer.getSelectedCellPositions());
			Collections.reverse(coordinates);
			selectedCells = coordinates.toArray(new PositionCoordinate[0]);
		} else {
			selectedCells = selectionLayer.getSelectedCellPositions();
		}
		return selectedCells;
	}
}
