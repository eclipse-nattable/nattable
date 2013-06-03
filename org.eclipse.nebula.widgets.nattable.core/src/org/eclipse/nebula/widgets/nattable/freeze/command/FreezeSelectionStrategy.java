/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezeSelectionStrategy implements IFreezeCoordinatesProvider {

	private final FreezeLayer freezeLayer;

	private final ViewportLayer viewportLayer;
	
	private final SelectionLayer selectionLayer;

	public FreezeSelectionStrategy(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
		this.freezeLayer = freezeLayer;
		this.viewportLayer = viewportLayer;
		this.selectionLayer = selectionLayer;
	}

	public PositionCoordinate getTopLeftPosition() {
		PositionCoordinate lastSelectedCellPosition = selectionLayer.getLastSelectedCellPosition();
		if (lastSelectedCellPosition == null) {
			return null;
		}
			
		int columnPosition = viewportLayer.getColumnPositionByX(viewportLayer.getOrigin().getX());
		if (columnPosition >= lastSelectedCellPosition.columnPosition) {
			columnPosition = lastSelectedCellPosition.columnPosition - 1;
		}
		
		int rowPosition = viewportLayer.getRowPositionByY(viewportLayer.getOrigin().getY());
		if (rowPosition >= lastSelectedCellPosition.rowPosition) {
			rowPosition = lastSelectedCellPosition.rowPosition - 1;
		}
		
		return new PositionCoordinate(freezeLayer, columnPosition, rowPosition);
	}
	
	public PositionCoordinate getBottomRightPosition() {
		PositionCoordinate selectionAnchor = selectionLayer.getSelectionAnchor();
		if (selectionAnchor == null) {
			return null;
		}
		return new PositionCoordinate(freezeLayer, selectionAnchor.columnPosition - 1, selectionAnchor.rowPosition - 1);
	}

}
