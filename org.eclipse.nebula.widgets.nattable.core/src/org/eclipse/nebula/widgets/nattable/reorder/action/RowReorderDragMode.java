/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.IOverlayPainter;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.ClearAllSelectionsCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommand;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the row header.
 * It does the following when invoked:
 * <ol>
 *    <li>Fires a row reorder command, to move columns</li>
 *    <li>Overlays a black line indicating the new row position</li>
 * </ol>
 */
public class RowReorderDragMode implements IDragMode {
	
	protected NatTable natTable;
	protected MouseEvent initialEvent;
	protected MouseEvent currentEvent;
	protected int dragFromGridRowPosition;
	
	protected RowReorderOverlayPainter targetOverlayPainter = new RowReorderOverlayPainter();
	
	@Override
	public void mouseDown(NatTable natTable, MouseEvent event) {
		this.natTable = natTable;
		initialEvent = event;
		currentEvent = initialEvent;
		dragFromGridRowPosition = getDragFromGridRowPosition();
		
        natTable.addOverlayPainter(targetOverlayPainter);
        
        natTable.doCommand(new ClearAllSelectionsCommand());
        
        fireMoveStartCommand(natTable, dragFromGridRowPosition);
	}
	
	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		currentEvent = event;
		
		natTable.doCommand(new ViewportDragCommand(-1, event.y));
		
		natTable.redraw();
	}

	@Override
	public void mouseUp(NatTable natTable, MouseEvent event) {
		natTable.removeOverlayPainter(targetOverlayPainter);
		
		int dragToGridRowPosition = getDragToGridRowPosition(getMoveDirection(event.y), natTable.getRowPositionByY(event.y));
		
		if (!isValidTargetRowPosition(natTable, dragFromGridRowPosition, dragToGridRowPosition)) {
			dragToGridRowPosition = -1;
		}
		
		fireMoveEndCommand(natTable, dragToGridRowPosition);
		
		natTable.doCommand(new ViewportDragCommand(-1, -1));  // Cancel any active viewport drag
		
		natTable.redraw();
	}
	
	/**
	 * @return The row position of the row that is dragged
	 */
	private int getDragFromGridRowPosition() {
		return natTable.getRowPositionByY(initialEvent.y);
	}
	
	/**
	 * @param moveDirection The direction to indicate whether the drop was before
	 * 			or after the given row position
	 * @param gridRowPosition The row position at which the drop was performed
	 * @return The row position where the dragged row should be dropped
	 */
	private int getDragToGridRowPosition(CellEdgeEnum moveDirection, int gridRowPosition) {
		int dragToGridRowPosition = -1;
		
		if (moveDirection != null) {
			switch (moveDirection) {
				case TOP:
					dragToGridRowPosition = gridRowPosition;
					break;
				case BOTTOM:
					dragToGridRowPosition = gridRowPosition + 1;
					break;
			}
		}
		
		return dragToGridRowPosition;
	}
	
	/**
	 * @param y The y coordinate of the drop location
	 * @return The direction whether the drop should be performed before the the cell at
	 * 			drop position or after
	 */
	private CellEdgeEnum getMoveDirection(int y) {
	    ILayerCell cell = getRowCell(y);
	    if (cell != null) {
			Rectangle selectedRowHeaderRect = cell.getBounds();
			return CellEdgeDetectUtil.getVerticalCellEdge(selectedRowHeaderRect, new Point(initialEvent.x, y));
	    }
		
		return null;
	}
	
	/**
	 * @param y The y coordinate of the drop location
	 * @return The {@link ILayerCell} at the drop location
	 */
	private ILayerCell getRowCell(int y) {
		int gridColumnPosition = natTable.getColumnPositionByX(initialEvent.x);
	    int gridRowPosition = natTable.getRowPositionByY(y);
	    return natTable.getCellByPosition(gridColumnPosition, gridRowPosition);
	}

	/**
	 * 
	 * @param natLayer The layer the positions are related to
	 * @param dragFromGridRowPosition The row position of the row that is dragged
	 * @param dragToGridRowPosition The row position where the row is dropped
	 * @return <code>true</code> if the drop position is valid, <code>false</code> if not
	 */
	protected boolean isValidTargetRowPosition(ILayer natLayer, int dragFromGridRowPosition, int dragToGridRowPosition) {
		return dragFromGridRowPosition >= 0 && dragToGridRowPosition >= 0;
	}

	/**
	 * Executes the command to indicate row reorder starting.
	 * @param natTable The NatTable instance on which the command should be executed
	 * @param dragFromGridRowPosition The row position of the row that is dragged
	 */
	protected void fireMoveStartCommand(NatTable natTable, int dragFromGridRowPosition) {
		natTable.doCommand(new RowReorderStartCommand(natTable, dragFromGridRowPosition));
	}
	
	/**
	 * Executes the command to indicate row reorder ending.
	 * @param natTable The NatTable instance on which the command should be executed
	 * @param dragToGridRowPosition The position of the row to which the dragged row should be dropped
	 */
	protected void fireMoveEndCommand(NatTable natTable, int dragToGridRowPosition) {
		natTable.doCommand(new RowReorderEndCommand(natTable, dragToGridRowPosition));
	}

	/**
	 * The overlay painter for showing the drag operation.
	 */
	private class RowReorderOverlayPainter implements IOverlayPainter {

		@Override
		public void paintOverlay(GC gc, ILayer layer) {
			int dragFromGridRowPosition = getDragFromGridRowPosition();
			
			if (currentEvent.y > natTable.getHeight()) {
				return;
			}
			
			CellEdgeEnum moveDirection = getMoveDirection(currentEvent.y);
			int dragToGridRowPosition = getDragToGridRowPosition(moveDirection, natTable.getRowPositionByY(currentEvent.y));
			
			if (isValidTargetRowPosition(natTable, dragFromGridRowPosition, dragToGridRowPosition)) {
				int dragToRowHandleY = -1;
				
				if (moveDirection != null) {
					Rectangle selectedRowHeaderRect = getRowCell(currentEvent.y).getBounds();
					
					switch (moveDirection) {
						case TOP:
							dragToRowHandleY = selectedRowHeaderRect.y;
							break;
						case BOTTOM:
							dragToRowHandleY = selectedRowHeaderRect.y + selectedRowHeaderRect.height;
							break;
					}
				}
				
				if (dragToRowHandleY > 0) {
					Color orgBgColor = gc.getBackground();
					gc.setBackground(GUIHelper.COLOR_DARK_GRAY);
					
					gc.fillRectangle(0, dragToRowHandleY - 1, layer.getWidth(), 2);
					
					gc.setBackground(orgBgColor);
				}
			}
		}

	}
	
}
