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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;


public class MultiColumnReorderCommand implements ILayerCommand {

	private List<ColumnPositionCoordinate> fromColumnPositionCoordinates;
	private ColumnPositionCoordinate toColumnPositionCoordinate;
	private boolean reorderToLeftEdge;

	public MultiColumnReorderCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPosition) {
		this(layer, fromColumnPositions, toColumnPosition < layer.getColumnCount() ? toColumnPosition : toColumnPosition - 1, toColumnPosition < layer.getColumnCount());
	}
	
	public MultiColumnReorderCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPosition, boolean reorderToLeftEdge) {
		fromColumnPositionCoordinates = new ArrayList<ColumnPositionCoordinate>();
		for (Integer fromColumnPosition : fromColumnPositions) {
			fromColumnPositionCoordinates.add(new ColumnPositionCoordinate(layer, fromColumnPosition.intValue()));
		}
		
		toColumnPositionCoordinate = new ColumnPositionCoordinate(layer, toColumnPosition);
		
		this.reorderToLeftEdge = reorderToLeftEdge;
	}

	protected MultiColumnReorderCommand(MultiColumnReorderCommand command) {
		this.fromColumnPositionCoordinates = new ArrayList<ColumnPositionCoordinate>(command.fromColumnPositionCoordinates);
		this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
		this.reorderToLeftEdge = command.reorderToLeftEdge;
	}

	public List<Integer> getFromColumnPositions() {
		List<Integer> fromColumnPositions = new ArrayList<Integer>();
		for (ColumnPositionCoordinate fromColumnPositionCoordinate : fromColumnPositionCoordinates) {
			fromColumnPositions.add(Integer.valueOf(fromColumnPositionCoordinate.getColumnPosition()));
		}
		return fromColumnPositions;
	}

	public int getToColumnPosition() {
		return toColumnPositionCoordinate.getColumnPosition();
	}
	
	public boolean isReorderToLeftEdge() {
		return reorderToLeftEdge;
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		List<ColumnPositionCoordinate> convertedFromColumnPositionCoordinates = new ArrayList<ColumnPositionCoordinate>();
		
		for (ColumnPositionCoordinate fromColumnPositionCoordinate : fromColumnPositionCoordinates) {
			ColumnPositionCoordinate convertedFromColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(fromColumnPositionCoordinate, targetLayer);
			if (convertedFromColumnPositionCoordinate != null) {
				convertedFromColumnPositionCoordinates.add(convertedFromColumnPositionCoordinate);
			}
		}
		
		fromColumnPositionCoordinates = convertedFromColumnPositionCoordinates;
		
		toColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(toColumnPositionCoordinate, targetLayer);
		
		return fromColumnPositionCoordinates.size() > 0 && toColumnPositionCoordinate != null;
	}

	public MultiColumnReorderCommand cloneCommand() {
		return new MultiColumnReorderCommand(this);
	}

}
