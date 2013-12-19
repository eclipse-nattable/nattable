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

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.layer.GridLineCellLayerPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialized GridLineCellLayerPainter that renders an additional border around selected cells.
 * By default the additional selection anchor border style is black dotted one pixel sized line.
 * This style can be configured via ConfigRegistry.
 * 
 * @see SelectionStyleLabels#SELECTION_ANCHOR_GRID_LINE_STYLE
 */
public class SelectionLayerPainter extends GridLineCellLayerPainter {

	private int columnPositionOffset;
	
	private int rowPositionOffset;
	
	private Map<Point, ILayerCell> cells;
	/**
	 * Create a SelectionLayerPainter that renders grid lines in the specified color 
	 * and uses the default clipping behaviour.
	 * @param gridColor The color that should be used to render the grid lines.
	 */
	public SelectionLayerPainter(final Color gridColor) {
		super(gridColor);
	}
	
	/**
	 * Create a SelectionLayerPainter that renders gray grid lines and uses the default 
	 * clipping behaviour.
	 */
	public SelectionLayerPainter() {
		super();
	}
	
	/**
	 * Create a SelectionLayerPainter that renders grid lines in the specified color 
	 * and uses the specified clipping behaviour.
	 * @param gridColor The color that should be used to render the grid lines.
	 * @param clipLeft Configure the rendering behaviour when cells overlap.
	 * 			If set to <code>true</code> the left cell will be clipped, 
	 * 			if set to <code>false</code> the right cell will be clipped.
	 * 			The default value is <code>false</code>.
	 * @param clipTop Configure the rendering behaviour when cells overlap.
	 * 			If set to <code>true</code> the top cell will be clipped, 
	 * 			if set to <code>false</code> the bottom cell will be clipped.
	 * 			The default value is <code>false</code>.
	 */
	public SelectionLayerPainter(final Color gridColor, boolean clipLeft, boolean clipRight) {
		super(gridColor, clipLeft, clipRight);
	}
	
	/**
	 * Create a SelectionLayerPainter that renders gray grid lines and uses the specified 
	 * clipping behaviour.
	 * @param clipLeft Configure the rendering behaviour when cells overlap.
	 * 			If set to <code>true</code> the left cell will be clipped, 
	 * 			if set to <code>false</code> the right cell will be clipped.
	 * 			The default value is <code>false</code>.
	 * @param clipTop Configure the rendering behaviour when cells overlap.
	 * 			If set to <code>true</code> the top cell will be clipped, 
	 * 			if set to <code>false</code> the bottom cell will be clipped.
	 * 			The default value is <code>false</code>.
	 */
	public SelectionLayerPainter(boolean clipLeft, boolean clipRight) {
		this(GUIHelper.COLOR_GRAY, clipLeft, clipRight);
	}

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
		applyBorderStyle(gc, configRegistry);
		
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
	
	private void applyBorderStyle(GC gc, IConfigRegistry configRegistry) {
		//Note: If there is no style configured for the SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE
		//		label, the style configured for DisplayMode.SELECT will be retrieved by this call.
		//		Ensure that the selection style configuration does not contain a border style configuration
		//		to avoid strange rendering behaviour. By default there is no border configuration added,
		//		so there shouldn't be issues with backwards compatibility. And if there are some, they can
		//		be solved easily by adding the necessary border style configuration.
		IStyle cellStyle = configRegistry.getConfigAttribute(
				CellConfigAttributes.CELL_STYLE, 
				DisplayMode.SELECT, 
				SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE);
		BorderStyle borderStyle = cellStyle != null ? cellStyle.getAttributeValue(CellStyleAttributes.BORDER_STYLE) : null;
		
		//if there is no border style configured, use the default one for backwards compatibility
		if (borderStyle == null) {
			gc.setLineStyle(SWT.LINE_CUSTOM);
			gc.setLineDash(new int[] { 1, 1 });
			gc.setForeground(GUIHelper.COLOR_BLACK);
		}
		else {
			gc.setLineStyle(LineStyleEnum.toSWT(borderStyle.getLineStyle()));
			gc.setLineWidth(borderStyle.getThickness());
			gc.setForeground(borderStyle.getColor());
		}
	}

}
