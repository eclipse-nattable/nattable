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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class CellDisplayValueSearchUtil {
	
	static List<PositionCoordinate> getCellCoordinates(ILayer contextLayer, int startingColumnPosition, int startingRowPosition, int width, int height) {
		List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
		for (int columnPosition = 0; columnPosition < width; columnPosition++) {
			for (int rowPosition = 0; rowPosition < height; rowPosition++) {
				PositionCoordinate coordinate = new PositionCoordinate(contextLayer, startingColumnPosition, startingRowPosition++);
				coordinates.add(coordinate);
			}
			startingColumnPosition++;
		}
		return coordinates;
	}
	
	static List<PositionCoordinate> getDescendingCellCoordinates(ILayer contextLayer, int startingColumnPosition, int startingRowPosition, int width, int height) {
		List<PositionCoordinate> coordinates = new ArrayList<PositionCoordinate>();
		for (int columnPosition = width; columnPosition >= 0 && startingColumnPosition >= 0; columnPosition--) {
			for (int rowPosition = height; rowPosition >= 0 && startingRowPosition >= 0; rowPosition--) {
				PositionCoordinate coordinate = new PositionCoordinate(contextLayer, startingColumnPosition, startingRowPosition--);
				coordinates.add(coordinate);
			}
			startingColumnPosition--;
		}
		return coordinates;
	}
	
	
	@SuppressWarnings("unchecked")
	static PositionCoordinate findCell(final ILayer layer, final IConfigRegistry configRegistry, final PositionCoordinate[] cellsToSearch, final Object valueToMatch, final Comparator comparator, final boolean caseSensitive) {	
		final List<PositionCoordinate> cellCoordinates = Arrays.asList(cellsToSearch);		
		// Find cell
		PositionCoordinate targetCoordinate = null;
		
		String stringValue = caseSensitive ? valueToMatch.toString() : valueToMatch.toString().toLowerCase();
		for (int cellIndex = 0; cellIndex < cellCoordinates.size(); cellIndex++) {
			final PositionCoordinate cellCoordinate = cellCoordinates.get(cellIndex);
			final int columnPosition = cellCoordinate.columnPosition;
			final int rowPosition = cellCoordinate.rowPosition;
			
			// Convert cell's data
			final IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.NORMAL, layer.getConfigLabelsByPosition(columnPosition, rowPosition).getLabels());
			Object dataValue = null;
			if (displayConverter != null) {
				ILayerCell cell = layer.getCellByPosition(columnPosition, rowPosition);
				if (cell != null) {
					dataValue = displayConverter.canonicalToDisplayValue(cell, configRegistry, cell.getDataValue());
				}
			}
			
			// Compare with valueToMatch
			if (dataValue instanceof Comparable<?>) {
				String dataValueString = caseSensitive ? dataValue.toString() : dataValue.toString().toLowerCase();
				if (comparator.compare(stringValue, dataValueString) == 0 || dataValueString.contains(stringValue)) {
					targetCoordinate = cellCoordinate;
					break;
				}
			}
		}
		
		return targetCoordinate;
	}
}
