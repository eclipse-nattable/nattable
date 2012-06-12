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
package org.eclipse.nebula.widgets.nattable.painter.layer;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class CellLayerPainter implements ILayerPainter {

	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		if (pixelRectangle.width <= 0 || pixelRectangle.height <= 0) {
			return;
		}
		
		Collection<ILayerCell> spannedCells = new HashSet<ILayerCell>();
		
		Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
		
		for (int columnPosition = positionRectangle.x; columnPosition < positionRectangle.x + positionRectangle.width; columnPosition++) {
			for (int rowPosition = positionRectangle.y; rowPosition < positionRectangle.y + positionRectangle.height; rowPosition++) {
				ILayerCell cell = natLayer.getCellByPosition(columnPosition, rowPosition);
				if (cell != null) {
					if (cell.isSpannedCell()) {
						spannedCells.add(cell);
					} else {
						paintCell(cell, gc, configRegistry);
					}
				}
			}
		}
		
		for (ILayerCell cell : spannedCells) {
			paintCell(cell, gc, configRegistry);
		}
	}
	
	public Rectangle adjustCellBounds(int columnPosition, int rowPosition, Rectangle cellBounds) {
		return cellBounds;
	}
	
	protected Rectangle getPositionRectangleFromPixelRectangle(ILayer natLayer, Rectangle pixelRectangle) {
		int columnPositionOffset = natLayer.getColumnPositionByX(pixelRectangle.x);
		int rowPositionOffset = natLayer.getRowPositionByY(pixelRectangle.y);
		int numColumns = natLayer.getColumnPositionByX(Math.min(natLayer.getWidth(), pixelRectangle.x + pixelRectangle.width) - 1) - columnPositionOffset + 1;
		int numRows = natLayer.getRowPositionByY(Math.min(natLayer.getHeight(), pixelRectangle.y + pixelRectangle.height) - 1) - rowPositionOffset + 1;
		
		return new Rectangle(columnPositionOffset, rowPositionOffset, numColumns, numRows);
	}

	protected void paintCell(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		ICellPainter cellPainter = cell.getLayer().getCellPainter(cell.getColumnPosition(), cell.getRowPosition(), cell, configRegistry);
		Rectangle adjustedCellBounds = cell.getLayer().getLayerPainter().adjustCellBounds(cell.getColumnPosition(), cell.getRowPosition(), cell.getBounds());
		if (cellPainter != null) {
			cellPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);
		}
	}

}
