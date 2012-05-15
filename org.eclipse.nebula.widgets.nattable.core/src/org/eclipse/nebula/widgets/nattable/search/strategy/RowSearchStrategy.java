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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;


public class RowSearchStrategy extends AbstractSearchStrategy {

	private final IConfigRegistry configRegistry;
	private final int[] rowPositions;
	private final String searchDirection;

	public RowSearchStrategy(int[] rowPositions, IConfigRegistry configRegistry) {
		this(rowPositions, configRegistry, ISearchDirection.SEARCH_FORWARD);
	}
	
	public RowSearchStrategy(int[] rowPositions, IConfigRegistry configRegistry, String searchDirection) {
		this.rowPositions = rowPositions;
		this.configRegistry = configRegistry;
		this.searchDirection = searchDirection;
	}
	
	public PositionCoordinate executeSearch(Object valueToMatch) {
		return CellDisplayValueSearchUtil.findCell(getContextLayer(), configRegistry, getRowCellsToSearch(getContextLayer()), valueToMatch, getComparator(), isCaseSensitive());
	}

	protected PositionCoordinate[] getRowCellsToSearch(ILayer contextLayer) {
		List<PositionCoordinate> cellsToSearch = new ArrayList<PositionCoordinate>();
		for (int rowPosition : rowPositions) {
			cellsToSearch.addAll(CellDisplayValueSearchUtil.getCellCoordinates(getContextLayer(), 0, rowPosition, contextLayer.getColumnCount(), 1));
		}
		if (searchDirection.equals(ISearchDirection.SEARCH_BACKWARDS)) {
			Collections.reverse(cellsToSearch);
		}
		return cellsToSearch.toArray(new PositionCoordinate[0]);
	}
}
