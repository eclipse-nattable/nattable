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
package org.eclipse.nebula.widgets.nattable.painter.layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;


public class CellLayerPainter implements ILayerPainter {
	
	private ILayer natLayer;
	private Map<Integer, Integer> horizontalPositionToPixelMap;
	private Map<Integer, Integer> verticalPositionToPixelMap;
	
	
	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		if (pixelRectangle.width <= 0 || pixelRectangle.height <= 0) {
			return;
		}
		
		this.natLayer = natLayer;
		Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
		
		calculateDimensionInfo(positionRectangle);
		
		Collection<ILayerCell> spannedCells = new HashSet<ILayerCell>();
		
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
	
	private void calculateDimensionInfo(Rectangle positionRectangle) {
		{	horizontalPositionToPixelMap = new HashMap<Integer, Integer>();
			final int startPosition = positionRectangle.x;
			final int endPosition = startPosition + positionRectangle.width;
			int start2 = (startPosition > 0) ?
					natLayer.getStartXOfColumnPosition(startPosition - 1)
							+ natLayer.getColumnWidthByPosition(startPosition - 1) :
					Integer.MIN_VALUE;
			for (int position = startPosition; position < endPosition; position++) {
				int start1 = natLayer.getStartXOfColumnPosition(position);
				horizontalPositionToPixelMap.put(position, Math.max(start1, start2));
				start2 = start1 + natLayer.getColumnWidthByPosition(position);
			}
			if (endPosition < natLayer.getColumnCount()) {
				int start1 = natLayer.getStartXOfColumnPosition(endPosition);
				horizontalPositionToPixelMap.put(endPosition, Math.max(start1, start2));
			}
		}
		{	verticalPositionToPixelMap = new HashMap<Integer, Integer>();
			final int startPosition = positionRectangle.y;
			final int endPosition = startPosition + positionRectangle.height;
			int start2 = (startPosition > 0) ?
					natLayer.getStartYOfRowPosition(startPosition - 1)
							+ natLayer.getRowHeightByPosition(startPosition - 1) :
					Integer.MIN_VALUE;
			for (int position = startPosition; position < endPosition; position++) {
				int start1 = natLayer.getStartYOfRowPosition(position);
				verticalPositionToPixelMap.put(position, Math.max(start1, start2));
				start2 = start1 + natLayer.getRowHeightByPosition(position);
			}
			if (endPosition < natLayer.getRowCount()) {
				int start1 = natLayer.getStartYOfRowPosition(endPosition);
				verticalPositionToPixelMap.put(endPosition, Math.max(start1, start2));
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
			
			int endX = getStartXOfColumnPosition(cell.getOriginColumnPosition() + cell.getColumnSpan());
			int endY = getStartYOfRowPosition(cell.getOriginRowPosition() + cell.getRowSpan());
			
			Rectangle clipBounds = new Rectangle(startX, startY, endX - startX, endY - startY);
			Rectangle adjustedClipBounds = layer.getLayerPainter().adjustCellBounds(columnPosition, rowPosition, clipBounds);
			gc.setClipping(adjustedClipBounds);
			
			cellPainter.paintCell(cell, gc, adjustedCellBounds, configRegistry);
			
			gc.setClipping(originalClipping);
		}
	}
	
	private int getStartXOfColumnPosition(final int columnPosition) {
		if (columnPosition < natLayer.getColumnCount()) {
			Integer start = horizontalPositionToPixelMap.get(columnPosition);
			if (start == null) {
				start = Integer.valueOf(natLayer.getStartXOfColumnPosition(columnPosition));
				if (columnPosition > 0) {
					int start2 = natLayer.getStartXOfColumnPosition(columnPosition - 1)
							+ natLayer.getColumnWidthByPosition(columnPosition - 1);
					if (start2 > start.intValue()) {
						start = Integer.valueOf(start2);
					}
				}
				horizontalPositionToPixelMap.put(columnPosition, start);
			}
			return start.intValue();
		} else {
			return natLayer.getWidth();
		}
	}
	
	private int getStartYOfRowPosition(final int rowPosition) {
		if (rowPosition < natLayer.getRowCount()) {
			Integer start = verticalPositionToPixelMap.get(rowPosition);
			if (start == null) {
				start = Integer.valueOf(natLayer.getStartYOfRowPosition(rowPosition));
				if (rowPosition > 0) {
					int start2 = natLayer.getStartYOfRowPosition(rowPosition - 1)
							+ natLayer.getRowHeightByPosition(rowPosition - 1);
					if (start2 > start.intValue()) {
						start = Integer.valueOf(start2);
					}
				}
				verticalPositionToPixelMap.put(rowPosition, start);
			}
			return start.intValue();
		} else {
			return natLayer.getHeight();
		}
	}
	
}
