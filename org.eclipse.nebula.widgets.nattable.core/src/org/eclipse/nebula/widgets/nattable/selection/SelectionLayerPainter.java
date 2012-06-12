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
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class SelectionLayerPainter extends GridLineCellLayerPainter {

	private int columnPositionOffset;
	
	private int rowPositionOffset;
	
	private Map<Point, ILayerCell> cells;
	
	@Override
	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
		columnPositionOffset = positionRectangle.x;
		rowPositionOffset = positionRectangle.y;
		cells = new HashMap<Point, ILayerCell>();
		
		super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);
		
		// Save gc settings
		int originalLineStyle = gc.getLineStyle();
		Color originalForeground = gc.getForeground();
		
		// Apply border settings
		gc.setLineStyle(SWT.LINE_CUSTOM);
		gc.setLineDash(new int[] { 1, 1 });
		gc.setForeground(GUIHelper.COLOR_BLACK);
		
		// Draw horizontal borders
		boolean selectedMode = false;
		for (int columnPosition = columnPositionOffset; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++) {
			ILayerCell previousCell = null;
			ILayerCell currentCell = null;
			for (int rowPosition = rowPositionOffset; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++) {
				currentCell = cells.get(new Point(columnPosition, rowPosition));
				if (currentCell != null) {
					if (selectedMode != isSelected(currentCell)) {
						selectedMode = !selectedMode;
						
						// Draw minimal shared border between previous and current cell
						Rectangle currentCellBounds = currentCell.getBounds();
						
						int x0 = xOffset + currentCellBounds.x - 1;
						int x1 = xOffset + currentCellBounds.x + currentCellBounds.width - 1;
						
						int y = yOffset + currentCellBounds.y - 1;
						
						if (previousCell != null) {
							Rectangle previousCellBounds = previousCell.getBounds();
							x0 = Math.max(x0, xOffset + previousCellBounds.x - 1);
							x1 = Math.min(x1, xOffset + previousCellBounds.x + previousCellBounds.width - 1);
						}
						
						gc.drawLine(x0, y, x1, y);
					}
				}
				previousCell = currentCell;
			}
			if (selectedMode && currentCell != null) {
				// If last cell is selected, draw its bottom edge
				Rectangle cellBounds = currentCell.getBounds();
				gc.drawLine(
						xOffset + cellBounds.x - 1,
						yOffset + cellBounds.y + cellBounds.height - 1,
						xOffset + cellBounds.x + cellBounds.width - 1,
						yOffset + cellBounds.y + cellBounds.height - 1
				);
			}
			selectedMode = false;
		}
		
		// Draw vertical borders
		for (int rowPosition = rowPositionOffset; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++) {
			ILayerCell previousCell = null;
			ILayerCell currentCell = null;
			for (int columnPosition = columnPositionOffset; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++) {
				currentCell = cells.get(new Point(columnPosition, rowPosition));
				if (currentCell != null) {
					if (selectedMode != isSelected(currentCell)) {
						selectedMode = !selectedMode;
						
						// Draw minimal shared border between previous and current cell
						Rectangle currentCellBounds = currentCell.getBounds();
						
						int x = xOffset + currentCellBounds.x - 1;
						
						int y0 = yOffset + currentCellBounds.y - 1;
						int y1 = yOffset + currentCellBounds.y + currentCellBounds.height - 1;
						
						if (previousCell != null) {
							Rectangle previousCellBounds = previousCell.getBounds();
							y0 = Math.max(y0, yOffset + previousCellBounds.y - 1);
							y1 = Math.min(y1, yOffset + previousCellBounds.y + previousCellBounds.height - 1);
						}
						
						gc.drawLine(x, y0, x, y1);
					}
				}
				previousCell = currentCell;
			}
			if (selectedMode && currentCell != null) {
				// If last cell is selected, draw its right edge
				Rectangle cellBounds = currentCell.getBounds();
				gc.drawLine(
						xOffset + cellBounds.x + cellBounds.width - 1,
						yOffset + cellBounds.y - 1,
						xOffset + cellBounds.x + cellBounds.width - 1,
						yOffset + cellBounds.y + cellBounds.height - 1
				);
			}
			selectedMode = false;
		}
		
		// Restore original gc settings
		gc.setLineStyle(originalLineStyle);
		gc.setForeground(originalForeground);
	}
	
	@Override
	protected void paintCell(ILayerCell cell, GC gc, IConfigRegistry configRegistry) {
		for (int columnPosition = cell.getOriginColumnPosition(); columnPosition < cell.getOriginColumnPosition() + cell.getColumnSpan(); columnPosition++) {
			for (int rowPosition = cell.getOriginRowPosition(); rowPosition < cell.getOriginRowPosition() + cell.getRowSpan(); rowPosition++) {
				cells.put(new Point(columnPosition, rowPosition), cell);
			}
		}
		
		super.paintCell(cell, gc, configRegistry);
	}
	
	private boolean isSelected(ILayerCell cell) {
		return cell.getDisplayMode() == DisplayMode.SELECT;
	}
	
}
