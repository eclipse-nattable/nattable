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
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Drag mode that will implement the column resizing process.
 */
public class ColumnResizeDragMode implements IDragMode {

	private static final int DEFAULT_COLUMN_WIDTH_MINIMUM = 25;

	protected int columnPositionToResize;
	protected int originalColumnWidth;
	protected int startX;
	protected int currentX;
	protected int lastX = -1;
	protected int gridColumnStartX;
	
	protected boolean checkMinimumWidth = true;

	protected final IOverlayPainter overlayPainter = new ColumnResizeOverlayPainter();

	@Override
	public void mouseDown(NatTable natTable, MouseEvent event) {
		natTable.forceFocus();
		columnPositionToResize =
		    CellEdgeDetectUtil.getColumnPositionToResize(natTable, new Point(event.x, event.y));
		if (columnPositionToResize >= 0) {
		    gridColumnStartX = natTable.getStartXOfColumnPosition(columnPositionToResize);
		    originalColumnWidth = natTable.getColumnWidthByPosition(columnPositionToResize);
		    startX = event.x;
		    natTable.addOverlayPainter(overlayPainter);
		}
	}

	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		if (event.x > natTable.getWidth()) {
			return;
		}
	    this.currentX = event.x;
	    if (checkMinimumWidth && currentX < gridColumnStartX + getColumnWidthMinimum()) {
	        currentX = gridColumnStartX + getColumnWidthMinimum();
	    } else {
	    	int overlayExtent = ColumnResizeOverlayPainter.COLUMN_RESIZE_OVERLAY_WIDTH / 2;

	    	Set<Integer> columnsToRepaint = new HashSet<Integer>();

	    	columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(currentX - overlayExtent)));
	    	columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(currentX + overlayExtent)));

	    	if (lastX >= 0) {
	    		columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(lastX - overlayExtent)));
	    		columnsToRepaint.add(Integer.valueOf(natTable.getColumnPositionByX(lastX + overlayExtent)));
	    	}

	    	for (Integer columnToRepaint : columnsToRepaint) {
	    		natTable.repaintColumn(columnToRepaint.intValue());
	    	}

	        lastX = currentX;
	    }
	}

	@Override
	public void mouseUp(NatTable natTable, MouseEvent event) {
	    natTable.removeOverlayPainter(overlayPainter);
		updateColumnWidth(natTable, event);
	}

	private void updateColumnWidth(ILayer natLayer, MouseEvent e) {
	    int dragWidth = e.x - startX;
        int newColumnWidth = originalColumnWidth + dragWidth;
        if (newColumnWidth < getColumnWidthMinimum()) newColumnWidth = getColumnWidthMinimum();
		natLayer.doCommand(new ColumnResizeCommand(natLayer, columnPositionToResize, newColumnWidth));
	}

	// XXX: This method must ask the layer what it's minimum width is!
	private int getColumnWidthMinimum() {
	    return DEFAULT_COLUMN_WIDTH_MINIMUM;
	}

	private class ColumnResizeOverlayPainter implements IOverlayPainter {

		static final int COLUMN_RESIZE_OVERLAY_WIDTH = 2;

	    @Override
		public void paintOverlay(GC gc, ILayer layer) {
	        Color originalBackgroundColor = gc.getBackground();
	        gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
	        gc.fillRectangle(currentX - (COLUMN_RESIZE_OVERLAY_WIDTH / 2), 0, COLUMN_RESIZE_OVERLAY_WIDTH, layer.getHeight());
	        gc.setBackground(originalBackgroundColor);
	    }
	}
}
