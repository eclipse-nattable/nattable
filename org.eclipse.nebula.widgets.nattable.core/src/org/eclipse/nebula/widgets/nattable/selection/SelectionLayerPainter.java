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
import org.eclipse.swt.graphics.Rectangle;

/**
 * Specialised GridLineCellLayerPainter that renders an additional border around selected cells.
 * By default the additional selection anchor border style is black dotted one pixel sized line.
 * This style can be configured via ConfigRegistry.
 * 
 * @see SelectionStyleLabels#SELECTION_ANCHOR_GRID_LINE_STYLE
 */
public class SelectionLayerPainter extends GridLineCellLayerPainter {

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
	public SelectionLayerPainter(final Color gridColor, boolean clipLeft, boolean clipTop) {
		super(gridColor, clipLeft, clipTop);
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
	public SelectionLayerPainter(boolean clipLeft, boolean clipTop) {
		this(GUIHelper.COLOR_GRAY, clipLeft, clipTop);
	}

	@Override
	public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle pixelRectangle, IConfigRegistry configRegistry) {
		Rectangle positionRectangle = getPositionRectangleFromPixelRectangle(natLayer, pixelRectangle);
		int columnPositionOffset = positionRectangle.x;
		int rowPositionOffset = positionRectangle.y;
		
		super.paintLayer(natLayer, gc, xOffset, yOffset, pixelRectangle, configRegistry);
		
		// Save gc settings
		int originalLineStyle = gc.getLineStyle();
		Color originalForeground = gc.getForeground();
		
		// Apply border settings
		applyBorderStyle(gc, configRegistry);
		
		// Draw horizontal borders
		for (int columnPosition = columnPositionOffset; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++) {
			ILayerCell previousCell = null;
			ILayerCell currentCell = null;
			ILayerCell afterCell = null;
			for (int rowPosition = rowPositionOffset; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++) {
				currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
				afterCell = natLayer.getCellByPosition(columnPosition, rowPosition+1);
				
				if (currentCell != null) {
					Rectangle currentCellBounds = currentCell.getBounds();
					
					if (isSelected(currentCell)) {
						int x0 = currentCellBounds.x - 1;
						int x1 = currentCellBounds.x + currentCellBounds.width - 1;
						
						int y = currentCellBounds.y - 1;
						
						if (previousCell != null) {
							Rectangle previousCellBounds = previousCell.getBounds();
							x0 = Math.max(x0, previousCellBounds.x - 1);
							x1 = Math.min(x1, previousCellBounds.x + previousCellBounds.width - 1);
						}
						
						if (previousCell == null || !isSelected(previousCell))
							gc.drawLine(x0, y, x1, y);
						
						//check after
						if (afterCell == null || !isSelected(afterCell)) {
							Rectangle cellBounds = afterCell != null ? afterCell.getBounds() : currentCell.getBounds();

							y = currentCellBounds.y + currentCellBounds.height - 1;
							
							x0 = Math.max(x0, cellBounds.x - 1);
							x1 = Math.min(x1, cellBounds.x + cellBounds.width - 1);
							
							gc.drawLine(x0, y, x1, y);
						}
					}
					else {
						//check if previous was selected to not override the border again
						//this is necessary because of single cell updates
						if (positionRectangle.width == 2 || positionRectangle.height == 2) {
							if (afterCell != null && isSelected(afterCell)) {
								Rectangle afterCellBounds = afterCell.getBounds();
								
								int x0 = Math.max(afterCellBounds.x - 1, currentCellBounds.x - 1);
								int x1 = Math.min(afterCellBounds.x + afterCellBounds.width - 1, 
										currentCellBounds.x + currentCellBounds.width - 1);
								
								int y = currentCellBounds.y + currentCellBounds.height - 1;
								gc.drawLine(x0, y, x1, y);
							}
						}
					}
				}
				previousCell = currentCell;
			}
		}
		
		// Draw vertical borders
		for (int rowPosition = rowPositionOffset; rowPosition < rowPositionOffset + positionRectangle.height; rowPosition++) {
			ILayerCell previousCell = null;
			ILayerCell currentCell = null;
			ILayerCell afterCell = null;
			for (int columnPosition = columnPositionOffset; columnPosition < columnPositionOffset + positionRectangle.width; columnPosition++) {
				currentCell = natLayer.getCellByPosition(columnPosition, rowPosition);
				afterCell = natLayer.getCellByPosition(columnPosition+1, rowPosition);
				
				if (currentCell != null) {
					Rectangle currentCellBounds = currentCell.getBounds();
					
					if (isSelected(currentCell)) {
						int y0 = currentCellBounds.y - 1;
						int y1 = currentCellBounds.y + currentCellBounds.height - 1;
						
						int x = currentCellBounds.x - 1;
						
						if (previousCell != null) {
							Rectangle previousCellBounds = previousCell.getBounds();
							y0 = Math.max(y0, previousCellBounds.y - 1);
							y1 = Math.min(y1, previousCellBounds.y + previousCellBounds.height - 1);
						}
						
						if (previousCell == null || !isSelected(previousCell))
							gc.drawLine(x, y0, x, y1);
						
						//check after
						if (afterCell == null || !isSelected(afterCell)) {
							Rectangle cellBounds = afterCell != null ? afterCell.getBounds() : currentCell.getBounds();
							
							x = currentCellBounds.x + currentCellBounds.width - 1;
							
							y0 = Math.max(y0, cellBounds.y - 1);
							y1 = Math.min(y1, cellBounds.y + cellBounds.height - 1);
							
							gc.drawLine(x, y0, x, y1);
						}
					}
					else {
						//check if previous was selected to not override the border again
						//this is necessary because of single cell updates
						//check if previous was selected to not override the border again
						//this is necessary because of single cell updates
						if (positionRectangle.width == 2 || positionRectangle.height == 2) {
							if (afterCell != null && isSelected(afterCell)) {
								Rectangle afterCellBounds = afterCell.getBounds();
								
								int y0 = Math.max(afterCellBounds.y - 1, currentCellBounds.y - 1);
								int y1 = Math.min(afterCellBounds.y + afterCellBounds.height - 1, 
										currentCellBounds.y + currentCellBounds.height - 1);
	
								int x = currentCellBounds.x + currentCellBounds.width - 1;
								gc.drawLine(x, y0, x, y1);
							}
						}
					}
				}
				previousCell = currentCell;
			}
		}
		
		// Restore original gc settings
		gc.setLineStyle(originalLineStyle);
		gc.setForeground(originalForeground);
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
