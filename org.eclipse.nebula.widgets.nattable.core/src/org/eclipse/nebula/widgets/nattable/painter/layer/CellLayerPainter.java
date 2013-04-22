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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class CellLayerPainter implements ILayerPainter {
	
	private ILayer natLayer;
	private Map<Integer, Integer> horizontalPositionToPixelMap;
	private Map<Integer, Integer> verticalPositionToPixelMap;
	
	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		if (pixelRectangle.width <= 0 || pixelRectangle.height <= 0) {
			return;
		}
		
		calculateDimensionInfo(natLayer);
		
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
	
	private void calculateDimensionInfo(ILayer natLayer) {
		this.natLayer = natLayer;
		horizontalPositionToPixelMap = new HashMap<Integer, Integer>();
		verticalPositionToPixelMap = new HashMap<Integer, Integer>();
		
		for (int columnPosition = 0; columnPosition < natLayer.getColumnCount(); columnPosition++) {
			for (int rowPosition = 0; rowPosition < natLayer.getRowCount(); rowPosition++) {
				int x = natLayer.getStartXOfColumnPosition(columnPosition);
				Integer extantX = horizontalPositionToPixelMap.get(columnPosition);
				int width = natLayer.getColumnWidthByPosition(columnPosition);
				horizontalPositionToPixelMap.put(columnPosition, extantX != null ? Math.max(x, extantX) : x);
				horizontalPositionToPixelMap.put(columnPosition + 1, x + width);
				
				int y = natLayer.getStartYOfRowPosition(rowPosition);
				Integer extantY = verticalPositionToPixelMap.get(rowPosition);
				int height = natLayer.getRowHeightByPosition(rowPosition);
				verticalPositionToPixelMap.put(rowPosition, extantY != null ? Math.max(y, extantY) : y);
				verticalPositionToPixelMap.put(rowPosition + 1, y + height);
			}
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
		ILayer layer = cell.getLayer();
		int columnPosition = cell.getColumnPosition();
		int rowPosition = cell.getRowPosition();
		ICellPainter cellPainter = layer.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
		Rectangle adjustedCellBounds = layer.getLayerPainter().adjustCellBounds(columnPosition, rowPosition, cell.getBounds());
		if (cellPainter != null) {
			Rectangle originalClipping = gc.getClipping();
			int startX = getStartXOfColumnPosition(columnPosition);
			int startY = getStartYOfRowPosition(rowPosition);
			int endX = getStartXOfColumnPosition(columnPosition + cell.getColumnSpan());
			int endY = getStartYOfRowPosition(rowPosition + cell.getRowSpan());
			Rectangle clipBounds = new Rectangle(startX, startY, endX - startX, endY - startY);
			System.out.println(clipBounds);
			Rectangle adjustedClipBounds = layer.getLayerPainter().adjustCellBounds(columnPosition, rowPosition, clipBounds);
			gc.setClipping(adjustedClipBounds);
			
			cellPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);
			
			gc.setClipping(originalClipping);
		}
	}
	
	int getStartXOfColumnPosition(int columnPosition) {
		if (columnPosition < natLayer.getColumnCount()) {
			return horizontalPositionToPixelMap.get(columnPosition);
		} else {
			return natLayer.getWidth();
		}
	}
	
	int getStartYOfRowPosition(int rowPosition) {
		if (rowPosition < natLayer.getRowCount()) {
			return verticalPositionToPixelMap.get(rowPosition);
		} else {
			return natLayer.getHeight();
		}
	}
	
}
