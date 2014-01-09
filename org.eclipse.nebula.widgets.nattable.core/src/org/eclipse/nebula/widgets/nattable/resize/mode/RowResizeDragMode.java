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
package org.eclipse.nebula.widgets.nattable.resize.mode;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Drag mode that will implement the row resizing process.
 */
public class RowResizeDragMode implements IDragMode {
	
	private static final int DEFAULT_ROW_HEIGHT_MINIMUM = 18;
	
	protected int gridRowPositionToResize;
	protected int originalRowHeight;
	protected int startY;
	protected int currentY;
	protected int lastY = -1;
	protected int gridRowStartY;
	
	protected boolean checkMinimumWidth = true;

	protected final IOverlayPainter overlayPainter = new RowResizeOverlayPainter();
	
	@Override
	public void mouseDown(NatTable natTable, MouseEvent event) {
		natTable.forceFocus();
		gridRowPositionToResize = 
		    CellEdgeDetectUtil.getRowPositionToResize(natTable, new Point(event.x, event.y));
		if (gridRowPositionToResize > 0) {
		    gridRowStartY = natTable.getStartYOfRowPosition(gridRowPositionToResize);
		    originalRowHeight = natTable.getRowHeightByPosition(gridRowPositionToResize);
		    startY = event.y;
	        natTable.addOverlayPainter(overlayPainter);
		}
	}
	
	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		if (event.y > natTable.getHeight()) {
			return;
		}
	    currentY = event.y;
        if (checkMinimumWidth && currentY < gridRowStartY + getRowHeightMinimum()) {
            currentY = gridRowStartY + getRowHeightMinimum();
        } else {
	    	int overlayExtent = RowResizeOverlayPainter.ROW_RESIZE_OVERLAY_HEIGHT / 2;
		    
	    	Set<Integer> rowsToRepaint = new HashSet<Integer>();
	    	
	    	rowsToRepaint.add(Integer.valueOf(natTable.getRowPositionByY(currentY - overlayExtent)));
	    	rowsToRepaint.add(Integer.valueOf(natTable.getRowPositionByY(currentY + overlayExtent)));
	    	
	    	if (lastY >= 0) {
		    	rowsToRepaint.add(Integer.valueOf(natTable.getRowPositionByY(lastY - overlayExtent)));
		    	rowsToRepaint.add(Integer.valueOf(natTable.getRowPositionByY(lastY + overlayExtent)));
	    	}
	    	
	    	for (Integer rowToRepaint : rowsToRepaint) {
	    		natTable.repaintRow(rowToRepaint.intValue());
	    	}
	        
	        lastY = currentY;
        }
	}

	@Override
	public void mouseUp(NatTable natTable, MouseEvent event) {
		natTable.removeOverlayPainter(overlayPainter);
		updateRowHeight(natTable, event);
	}

	private void updateRowHeight(ILayer natLayer, MouseEvent e) {
		int dragHeight = e.y - startY;
		int newRowHeight = originalRowHeight + dragHeight;
		if (newRowHeight < getRowHeightMinimum()) newRowHeight = getRowHeightMinimum();
		natLayer.doCommand(new RowResizeCommand(natLayer, gridRowPositionToResize, newRowHeight));
	}
	
	// XXX: should ask the layer for its minimum row height
	public int getRowHeightMinimum() {
	    return DEFAULT_ROW_HEIGHT_MINIMUM;
	}
	
	private class RowResizeOverlayPainter implements IOverlayPainter {
		
		static final int ROW_RESIZE_OVERLAY_HEIGHT = 2;
		
	    @Override
		public void paintOverlay(GC gc, ILayer layer) {
	        Color originalBackgroundColor = gc.getBackground();
	        gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
	        gc.fillRectangle(0, currentY - (ROW_RESIZE_OVERLAY_HEIGHT / 2), layer.getWidth(), ROW_RESIZE_OVERLAY_HEIGHT);
	        gc.setBackground(originalBackgroundColor);
	    }
	}
}
