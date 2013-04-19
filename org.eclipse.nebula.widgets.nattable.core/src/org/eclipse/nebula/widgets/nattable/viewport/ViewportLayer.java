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
package org.eclipse.nebula.widgets.nattable.viewport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PixelCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.selection.ScrollSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.ScrollSelectionCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowCellInViewportCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.event.ScrollEvent;
import org.eclipse.nebula.widgets.nattable.viewport.event.ViewportEventHandler;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Viewport - the visible area of NatTable
 * Places a 'viewport' over the table. Introduces scroll bars over the table and
 * keeps them in sync with the data being displayed. This is typically placed over the
 * {@link SelectionLayer}.
 */
public class ViewportLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	private HorizontalScrollBarHandler hBarListener;
	private VerticalScrollBarHandler vBarListener;
	private final IUniqueIndexLayer scrollableLayer;

	// The viewport origin, in scrollable pixel coordinates.
	private PixelCoordinate origin = new PixelCoordinate(0, 0);
	private PixelCoordinate minimumOrigin = new PixelCoordinate(0, 0);
	private boolean viewportOff = false;
	private PixelCoordinate savedOrigin = new PixelCoordinate(0, 0);

	// Cache
	private int cachedColumnCount = -1;
	private int cachedRowCount = -1;
	private int cachedClientAreaWidth = 0;
	private int cachedClientAreaHeight = 0;
	private int cachedWidth = -1;
	private int cachedHeight = -1;
	
	// Edge hover scrolling
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private Point edgeHoverScrollOffset = new Point(0, 0);
	private ScheduledFuture<?> edgeHoverScrollFuture;

	public ViewportLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
		this.scrollableLayer = underlyingLayer;

		registerCommandHandlers();

		registerEventHandler(new ViewportEventHandler(this));
	}

	@Override
	public void dispose() {
		super.dispose();
		
		if (hBarListener != null) {
			hBarListener.dispose();
		}
		
		if (vBarListener != null) {
			vBarListener.dispose();
		}
		
		scheduler.shutdown();
	}
	
	// Minimum Origin
	
	public PixelCoordinate getMinimumOrigin() {
		return minimumOrigin;
	}
	
	public int getMinimumOriginColumnPosition() {
		return scrollableLayer.getColumnPositionByX(minimumOrigin.getX());
	}
	
	public int getMinimumOriginRowPosition() {
		return scrollableLayer.getRowPositionByY(minimumOrigin.getY());
	}
	
	public void setMinimumOriginX(int newMinimumOriginX) {
		if (newMinimumOriginX != minimumOrigin.getX()) {
			minimumOrigin = new PixelCoordinate(newMinimumOriginX, minimumOrigin.getY());
		}
		
		PixelCoordinate previousOrigin = origin;
		
		if (previousOrigin.getX() == newMinimumOriginX || origin.getX() < newMinimumOriginX) {
			origin = minimumOrigin;
		}
		
		if (origin != previousOrigin) {
			invalidateHorizontalStructure();
		}
		
		recalculateHorizontalScrollBar();
	}
	
	public void setMinimumOriginY(int newMinimumOriginY) {
		if (newMinimumOriginY != minimumOrigin.getY()) {
			minimumOrigin = new PixelCoordinate(minimumOrigin.getX(), newMinimumOriginY);
		}
		
		PixelCoordinate previousOrigin = origin;
		
		if (previousOrigin.getY() == newMinimumOriginY || origin.getY() < newMinimumOriginY) {
			origin = minimumOrigin;
		}
		
		if (origin != previousOrigin) {
			invalidateVerticalStructure();
		}
		
		recalculateVerticalScrollBar();
	}
	
	public void setMinimumOrigin(int newMinimumOriginX, int newMinimumOriginY) {
		setMinimumOriginX(newMinimumOriginX);
		setMinimumOriginY(newMinimumOriginY);
	}

	// Origin
	
	public PixelCoordinate getOrigin() {
		return viewportOff ? minimumOrigin : origin;
	}
	
	private int getOriginColumnPosition() {
		return scrollableLayer.getColumnPositionByX(getOrigin().getX());
	}
	
	private int getOriginRowPosition() {
		return scrollableLayer.getRowPositionByY(getOrigin().getY());
	}
	
	/**
	 * @param x
	 * @return valid x value within bounds: minimum origin x < x < max x (= column 0 x + width)
	 */
	private int boundsCheckOriginX(int x) {
		int min = minimumOrigin.getX();
		if (x <= min) {
			return min;
		}
		int max = Math.max(getUnderlyingLayer().getStartXOfColumnPosition(0) + getUnderlyingLayer().getWidth(), min);
		if (x > max) {
			return max;
		}
		return x;
	}
	
	/**
	 * @param y
	 * @return valid y value within bounds: minimum origin y < y < max y (= row 0 y + height)
	 */
	private int boundsCheckOriginY(int y) {
		int min = minimumOrigin.getY();
		if (y <= min) {
			return min;
		}
		int max = Math.max(getUnderlyingLayer().getStartYOfRowPosition(0) + getUnderlyingLayer().getHeight(), min);
		if (y > max) {
			return max;
		}
		return y;
	}

	public void setOriginX(int newOriginX) {
		newOriginX = boundsCheckOriginX(newOriginX);
		newOriginX = boundsCheckOriginX(adjustOriginX(newOriginX));

		if (newOriginX != origin.getX()) {
			invalidateHorizontalStructure();
			origin = new PixelCoordinate(newOriginX, origin.getY());
			fireScrollEvent();
		}
	}

	public void setOriginY(int newOriginY) {
		newOriginY = boundsCheckOriginY(newOriginY);
		newOriginY = boundsCheckOriginY(adjustOriginY(newOriginY));

		if (newOriginY != origin.getY()) {
			invalidateVerticalStructure();
			origin = new PixelCoordinate(origin.getX(), newOriginY);
			fireScrollEvent();
		}
	}
	
	public void resetOrigin(int newOriginX, int newOriginY) {
		PixelCoordinate previousOrigin = origin;
		
		minimumOrigin = new PixelCoordinate(0, 0);
		origin = new PixelCoordinate(newOriginX, newOriginY);
		
		if (origin.getX() != previousOrigin.getX()) {
			invalidateHorizontalStructure();
		}
		
		if (origin.getY() != previousOrigin.getY()) {
			invalidateVerticalStructure();
		}
	}
	
	// Configuration
	
	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new RecalculateScrollBarsCommandHandler(this));
		registerCommandHandler(new ScrollSelectionCommandHandler(this));
		registerCommandHandler(new ShowCellInViewportCommandHandler(this));
		registerCommandHandler(new ShowColumnInViewportCommandHandler(this));
		registerCommandHandler(new ShowRowInViewportCommandHandler(this));
		registerCommandHandler(new ViewportSelectColumnCommandHandler(this));
		registerCommandHandler(new ViewportSelectRowCommandHandler(this));
		registerCommandHandler(new ViewportDragCommandHandler(this));
	}

	// Horizontal features

	// Columns

	/**
	 * @return <i>visible</i> column count
	 *    Note: This takes care of the frozen columns
	 */
	@Override
	public int getColumnCount() {
		if (viewportOff) {
			return Math.max(scrollableLayer.getColumnCount() - getMinimumOriginColumnPosition(), 0);
		} else {
			if (cachedColumnCount < 0) {
				int availableWidth = getClientAreaWidth();
				if (availableWidth >= 0) {
					
					// lower bound check
					if (origin.getX() < minimumOrigin.getX()) {
						origin = new PixelCoordinate(minimumOrigin.getX(), origin.getY());
					}
	
					recalculateAvailableWidthAndColumnCount();
				}
			}
			
			return cachedColumnCount;
		}
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return scrollableLayer.getColumnPositionByIndex(columnIndex) - getOriginColumnPosition();
	}

	@Override
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		
		int underlyingPosition = getOriginColumnPosition() + localColumnPosition;
		
		if (underlyingPosition < getMinimumOriginColumnPosition()) {
			return -1;
		}
		
		return underlyingPosition;
	}

	@Override
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		if (sourceUnderlyingLayer != getUnderlyingLayer()) {
			return -1;
		}

		return underlyingColumnPosition - getOriginColumnPosition();
	}

	// Width

	/**
	 * @return the width of the total number of visible columns
	 */
	@Override
	public int getWidth() {
		if (viewportOff) {
			return scrollableLayer.getWidth() - scrollableLayer.getStartXOfColumnPosition(getMinimumOriginColumnPosition());
		}
		if (cachedWidth < 0) {
			recalculateAvailableWidthAndColumnCount();
		}
		return cachedWidth;
	}
	
	@Override
	public int getColumnWidthByPosition(int columnPosition) {
		int width = super.getColumnWidthByPosition(columnPosition);
		return width;
	}

	// Column resize

	@Override
	public boolean isColumnPositionResizable(int columnPosition) {
		return getUnderlyingLayer().isColumnPositionResizable(getOriginColumnPosition() + columnPosition);
	}

	// X

	@Override
	public int getColumnPositionByX(int x) {
		return getUnderlyingLayer().getColumnPositionByX(getOrigin().getX() + x) - getOriginColumnPosition();
	}

	@Override
	public int getStartXOfColumnPosition(int columnPosition) {
		return getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition() + columnPosition) - getOrigin().getX();
	}

	// Vertical features

	// Rows

	/**
	 * @return total number of rows visible in the viewport
	 */
	@Override
	public int getRowCount() {
		if (viewportOff) {
			return Math.max(scrollableLayer.getRowCount() - getMinimumOriginRowPosition(), 0);
		} else {
			if (cachedRowCount < 0) {
				int availableHeight = getClientAreaHeight();
				if (availableHeight >= 0) {
					
					// lower bound check
					if (origin.getY() < minimumOrigin.getY()) {
						origin = new PixelCoordinate(origin.getX(), minimumOrigin.getY());
					}
					
					recalculateAvailableHeightAndRowCount();
				}
			}
			
			return cachedRowCount;
		}
	}

	public int getRowPositionByIndex(int rowIndex) {
		return scrollableLayer.getRowPositionByIndex(rowIndex) - getOriginRowPosition();
	}

	@Override
	public int localToUnderlyingRowPosition(int localRowPosition) {
		
		int underlyingPosition = getOriginRowPosition() + localRowPosition;
		
		if (underlyingPosition < getMinimumOriginRowPosition()) {
			return -1;
		}
		
		return underlyingPosition;
	}

	@Override
	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		if (sourceUnderlyingLayer != getUnderlyingLayer()) {
			return -1;
		}

		return underlyingRowPosition - getOriginRowPosition();
	}
	
	// Height

	@Override
	public int getHeight() {
		if (viewportOff) {
			return scrollableLayer.getHeight() - scrollableLayer.getStartYOfRowPosition(getMinimumOriginRowPosition());
		}
		if (cachedHeight < 0) {
			recalculateAvailableHeightAndRowCount();
		}
		return cachedHeight;
	}
	
	@Override
	public int getRowHeightByPosition(int rowPosition) {
		int height = super.getRowHeightByPosition(rowPosition);
		return height;
	}
	
	// Row resize

	// Y

	@Override
	public int getRowPositionByY(int y) {
		return getUnderlyingLayer().getRowPositionByY(getOrigin().getY() + y) - getOriginRowPosition();
	}

	@Override
	public int getStartYOfRowPosition(int rowPosition) {
		return getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition() + rowPosition) - getOrigin().getY();
	}

	// Cell features

	@Override
	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		Rectangle bounds = getUnderlyingLayer().getBoundsByPosition(underlyingColumnPosition, underlyingRowPosition);
		bounds.x -= getOrigin().getX();
		bounds.y -= getOrigin().getY();
		return bounds;
	}

	/**
	 * Clear horizontal caches
	 */
	public void invalidateHorizontalStructure() {
		cachedColumnCount = -1;
		cachedClientAreaWidth = 0;
		cachedWidth = -1;
	}

	/**
	 * Clear vertical caches
	 */
	public void invalidateVerticalStructure() {
		cachedRowCount = -1;
		cachedClientAreaHeight = 0;
		cachedHeight = -1;
	}

	/**
	 * This method will add the indexes of the column which fit in the available
	 * view port width. Every time a column is added, the available width is
	 * reduced by the width of the added column.
	 */
	protected void recalculateAvailableWidthAndColumnCount() {
		int availableWidth = getClientAreaWidth();
		int originColumnPosition = getOriginColumnPosition();
		if (originColumnPosition >= 0) {
			availableWidth += getOrigin().getX() - underlyingLayer.getStartXOfColumnPosition(originColumnPosition);
		}
		ILayer underlyingLayer = getUnderlyingLayer();

		cachedWidth = 0;
		cachedColumnCount = 0;

		for (int columnPosition = originColumnPosition; columnPosition >= 0 && columnPosition < underlyingLayer.getColumnCount() && availableWidth > 0; columnPosition++) {
			int width = underlyingLayer.getColumnWidthByPosition(columnPosition);
			availableWidth -= width;
			cachedWidth += width;
			cachedColumnCount++;
		}

		int checkedOriginX = boundsCheckOriginX(origin.getX());
		if (checkedOriginX != origin.getX()) {
			origin = new PixelCoordinate(checkedOriginX, origin.getY());
		}
	}

	protected void recalculateAvailableHeightAndRowCount() {
		int availableHeight = getClientAreaHeight();
		int originRowPosition = getOriginRowPosition();
		if (originRowPosition >= 0) {
			availableHeight += getOrigin().getY() - underlyingLayer.getStartYOfRowPosition(originRowPosition);
		}
		ILayer underlyingLayer = getUnderlyingLayer();

		cachedHeight = 0;
		cachedRowCount = 0;

		for (int rowPosition = getOriginRowPosition(); rowPosition >= 0 && rowPosition < underlyingLayer.getRowCount() && availableHeight > 0; rowPosition++) {
			int height = underlyingLayer.getRowHeightByPosition(rowPosition);
			availableHeight -= height;
			cachedHeight += height;
			cachedRowCount++;
		}

		int checkedOriginY = boundsCheckOriginY(origin.getY());
		if (checkedOriginY != origin.getY()) {
			origin = new PixelCoordinate(origin.getY(), checkedOriginY);
		}
	}

	/**
	 * Srcolls the table so that the specified cell is visible i.e. in the Viewport
	 * @param scrollableColumnPosition
	 * @param scrollableRowPosition
	 * @param forceEntireCellIntoViewport
	 */
	public void moveCellPositionIntoViewport(int scrollableColumnPosition, int scrollableRowPosition) {
		moveColumnPositionIntoViewport(scrollableColumnPosition);
		moveRowPositionIntoViewport(scrollableRowPosition);
	}

	/**
	 * Scrolls the viewport (if required) so that the specified column is visible.
	 * @param scrollableColumnPosition column position in terms of the Scrollable Layer
	 */
	public void moveColumnPositionIntoViewport(int scrollableColumnPosition) {
		ILayer underlyingLayer = getUnderlyingLayer();
		if (underlyingLayer.getColumnIndexByPosition(scrollableColumnPosition) >= 0) {
			if (scrollableColumnPosition >= getMinimumOriginColumnPosition()) {
				int originColumnPosition = getOriginColumnPosition();

				if (scrollableColumnPosition <= originColumnPosition) {
					// Move left
					setOriginX(scrollableLayer.getStartXOfColumnPosition(scrollableColumnPosition));
				} else {
					int scrollableColumnStartX = underlyingLayer.getStartXOfColumnPosition(scrollableColumnPosition);
					int scrollableColumnEndX = scrollableColumnStartX + underlyingLayer.getColumnWidthByPosition(scrollableColumnPosition);
					int clientAreaWidth = getClientAreaWidth();
					int viewportEndX = underlyingLayer.getStartXOfColumnPosition(getOriginColumnPosition()) + clientAreaWidth;

					if (viewportEndX < scrollableColumnEndX) {
						// Move right
						setOriginX(Math.min(scrollableColumnEndX - clientAreaWidth, scrollableColumnStartX));
					}
				}
				
				// TEE: adjust scrollbar to reflect new position
				adjustHorizontalScrollBar();
			}
		}
	}

	/**
	 * @see #moveColumnPositionIntoViewport(int, boolean)
	 */
	public void moveRowPositionIntoViewport(int scrollableRowPosition) {
		ILayer underlyingLayer = getUnderlyingLayer();
		if (underlyingLayer.getRowIndexByPosition(scrollableRowPosition) >= 0) {
			if (scrollableRowPosition >= getMinimumOriginRowPosition()) {
				int originRowPosition = getOriginRowPosition();

				if (scrollableRowPosition <= originRowPosition) {
					// Move up
					setOriginY(scrollableLayer.getStartYOfRowPosition(scrollableRowPosition));
				} else {
					int scrollableRowStartY = underlyingLayer.getStartYOfRowPosition(scrollableRowPosition);
					int scrollableRowEndY = scrollableRowStartY + underlyingLayer.getRowHeightByPosition(scrollableRowPosition);
					int clientAreaHeight = getClientAreaHeight();
					int viewportEndY = underlyingLayer.getStartYOfRowPosition(getOriginRowPosition()) + clientAreaHeight;

					if (viewportEndY < scrollableRowEndY) {
						// Move down
						setOriginY(Math.min(scrollableRowEndY - clientAreaHeight, scrollableRowStartY));
					}
				}
				
				// TEE: at least adjust scrollbar to reflect new position
				adjustVerticalScrollBar();
			}
		}
	}

	protected void fireScrollEvent() {
		fireLayerEvent(new ScrollEvent(this));
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof ClientAreaResizeCommand && command.convertToTargetLayer(this)) {
			ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;
			
			//remember the difference from client area to body region area
			//needed because the scrollbar will be removed and therefore the client area will become bigger
			int widthDiff = clientAreaResizeCommand.getScrollable().getClientArea().width - clientAreaResizeCommand.getCalcArea().width;
			int heightDiff = clientAreaResizeCommand.getScrollable().getClientArea().height - clientAreaResizeCommand.getCalcArea().height;
			
			ScrollBar hBar = clientAreaResizeCommand.getScrollable().getHorizontalBar();
			ScrollBar vBar = clientAreaResizeCommand.getScrollable().getVerticalBar();

			if (hBarListener == null) {
				hBarListener = new HorizontalScrollBarHandler(this, hBar);
			}
			if (vBarListener == null) {
				vBarListener = new VerticalScrollBarHandler(this, vBar);
			}

			handleGridResize();
			
			//after handling the scrollbars recalculate the area to use for percentage calculation
			Rectangle possibleArea = clientAreaResizeCommand.getScrollable().getClientArea();
			possibleArea.width = possibleArea.width - widthDiff;
			possibleArea.height = possibleArea.height - heightDiff;
			clientAreaResizeCommand.setCalcArea(possibleArea);
		} else if (command instanceof TurnViewportOffCommand) {
			savedOrigin = origin;
			viewportOff = true;
			return true;
		} else if (command instanceof TurnViewportOnCommand) {
			viewportOff = false;
			origin = savedOrigin;
			return true;
		} else if (command instanceof PrintEntireGridCommand) {
			moveCellPositionIntoViewport(0, 0);
		}
		return super.doCommand(command);
	}

	private void recalculateHorizontalScrollBar() {
		if (hBarListener != null) {
			hBarListener.recalculateScrollBarSize();
			
			if (!hBarListener.scrollBar.getEnabled()) {
				setOriginX(0);
			}
		}
	}

	private void recalculateVerticalScrollBar() {
		if (vBarListener != null) {
			vBarListener.recalculateScrollBarSize();
			
			if (!vBarListener.scrollBar.getEnabled()) {
				setOriginY(0);
			}
		}
	}

	public void recalculateScrollBars() {
		recalculateHorizontalScrollBar();
		recalculateVerticalScrollBar();
	}

	protected void handleGridResize() {
		setOriginX(origin.getX());
		recalculateHorizontalScrollBar();
		setOriginY(origin.getY());
		recalculateVerticalScrollBar();
	}

	/**
	 * If the client area size is greater than the content size, move origin to fill as much content as possible.
	 */
	protected int adjustOriginX(int originX) {
		if (getColumnCount() == 0) {
			return 0;
		}

		int availableWidth = getClientAreaWidth() - (scrollableLayer.getWidth() - originX);
		if (availableWidth <= 0) {
			return originX;
		} else {
			return boundsCheckOriginX(originX - availableWidth);
		}
	}

	/**
	 * If the client area size is greater than the content size, move origin to fill as much content as possible.
	 */
	protected int adjustOriginY(int originY) {
		if (getRowCount() == 0) {
			return 0;
		}

		int availableHeight = getClientAreaHeight() - (scrollableLayer.getHeight() - originY);
		if (availableHeight <= 0) {
			return originY;
		} else {
			return boundsCheckOriginY(originY - availableHeight);
		}
	}

	public void scrollVerticallyByAPage(ScrollSelectionCommand scrollSelectionCommand) {
		getUnderlyingLayer().doCommand(scrollVerticallyByAPageCommand(scrollSelectionCommand));
	}

	protected MoveSelectionCommand scrollVerticallyByAPageCommand(ScrollSelectionCommand scrollSelectionCommand) {
		return new MoveSelectionCommand(scrollSelectionCommand.getDirection(),
										getRowCount(),
										scrollSelectionCommand.isShiftMask(),
										scrollSelectionCommand.isControlMask());
	}

	protected boolean isLastColumnCompletelyDisplayed() {
		int lastDisplayableColumnIndex = getUnderlyingLayer().getColumnIndexByPosition(getUnderlyingLayer().getColumnCount() - 1);
		int visibleColumnCount = getColumnCount();
		int lastVisibleColumnIndex = getColumnIndexByPosition(visibleColumnCount - 1);

		return (lastVisibleColumnIndex == lastDisplayableColumnIndex) && (getClientAreaWidth() >= getWidth());
	}

	protected boolean isLastRowCompletelyDisplayed() {
		int lastDisplayableRowIndex = getUnderlyingLayer().getRowIndexByPosition(getUnderlyingLayer().getRowCount() - 1);
		int visibleRowCount = getRowCount();
		int lastVisibleRowIndex = getRowIndexByPosition(visibleRowCount - 1);

		return (lastVisibleRowIndex == lastDisplayableRowIndex) && (getClientAreaHeight() >= getHeight());
	}

	// Event handling

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
			if (structuralChangeEvent.isHorizontalStructureChanged()) {
				invalidateHorizontalStructure();
			}
			if (structuralChangeEvent.isVerticalStructureChanged()) {
				invalidateVerticalStructure();
			}
		}

		if (event instanceof CellSelectionEvent) {
			processSelection((CellSelectionEvent) event);
		} else if (event instanceof ColumnSelectionEvent) {
			processColumnSelection((ColumnSelectionEvent) event);
		} else if (event instanceof RowSelectionEvent) {
			processRowSelection((RowSelectionEvent) event);
		}

		super.handleLayerEvent(event);
	}

	/**
	 * Handle the {@link CellSelectionEvent}
	 * @param selectionEvent
	 */
	private void processSelection(CellSelectionEvent selectionEvent) {
		moveCellPositionIntoViewport(selectionEvent.getColumnPosition(), selectionEvent.getRowPosition());
		adjustHorizontalScrollBar();
		adjustVerticalScrollBar();
	}

	private void processColumnSelection(ColumnSelectionEvent selectionEvent) {
		for (Range columnPositionRange : selectionEvent.getColumnPositionRanges()) {
			moveColumnPositionIntoViewport(columnPositionRange.end - 1);
			adjustHorizontalScrollBar();
		}
	}

	private void processRowSelection(RowSelectionEvent selectionEvent) {
		int rowPositionToMoveIntoViewport = selectionEvent.getRowPositionToMoveIntoViewport();
		if (rowPositionToMoveIntoViewport >= 0) {
			moveRowPositionIntoViewport(rowPositionToMoveIntoViewport);
			adjustVerticalScrollBar();
		}
	}

	private void adjustHorizontalScrollBar() {
		if (hBarListener != null) {
			hBarListener.adjustScrollBar();
		}
	}

	private void adjustVerticalScrollBar() {
		if (vBarListener != null) {
			vBarListener.adjustScrollBar();
		}
	}

	// Accessors

	public int getClientAreaWidth() {
		int clientAreaWidth = getClientAreaProvider().getClientArea().width;
		if (clientAreaWidth != cachedClientAreaWidth) {
			invalidateHorizontalStructure();
			cachedClientAreaWidth = clientAreaWidth;
		}
		return cachedClientAreaWidth;
	}

	public int getClientAreaHeight() {
		int clientAreaHeight = getClientAreaProvider().getClientArea().height;
		if (clientAreaHeight != cachedClientAreaHeight) {
			invalidateVerticalStructure();
			cachedClientAreaHeight = clientAreaHeight;
		}
		return cachedClientAreaHeight;
	}

	public SelectionLayer getSelectionLayer() {
		return (SelectionLayer) getUnderlyingLayer();
	}

	public IUniqueIndexLayer getScrollableLayer() {
		return scrollableLayer;
	}

	@Override
	public String toString() {
		return "Viewport Layer"; //$NON-NLS-1$
	}
	
	// Edge hover scrolling
	
	public void drag(int x, int y) {
		edgeHoverScrollOffset.x = 0;
		edgeHoverScrollOffset.y = 0;
		
		if (x < 0 && y < 0) {
			cancelEdgeHoverScroll();
			return;
		}
		
		Rectangle clientArea = getClientAreaProvider().getClientArea();
		
		int minX = clientArea.x;
		int maxX = clientArea.x + clientArea.width;
		if (x >= minX && x < minX + 10) {
			edgeHoverScrollOffset.x = -1;
		} else if (x > maxX - 10 && x < maxX) {
			edgeHoverScrollOffset.x = 1;
		}
		
		int minY = clientArea.y;
		int maxY = clientArea.y + clientArea.height;
		if (y >= minY && y < minY + 10) {
			edgeHoverScrollOffset.y = -1;
		} else if (y > maxY - 10 && y < maxY) {
			edgeHoverScrollOffset.y = 1;
		}
		
		if (edgeHoverScrollOffset.x != 0 || edgeHoverScrollOffset.y != 0) {
			if (edgeHoverScrollFuture == null || edgeHoverScrollFuture.isDone()) {
				edgeHoverScrollFuture = scheduler.schedule(new MoveViewportRunnable(), 500, TimeUnit.MILLISECONDS);
			}
		} else {
			cancelEdgeHoverScroll();
		}
	}
	
	private void cancelEdgeHoverScroll() {
		edgeHoverScrollOffset.x = 0;
		edgeHoverScrollOffset.y = 0;
		
		if (edgeHoverScrollFuture != null) {
			edgeHoverScrollFuture.cancel(false);
			edgeHoverScrollFuture = null;
		}
	}
	
	class MoveViewportRunnable implements Runnable {
		
		public void run() {
			if (edgeHoverScrollOffset.x != 0 || edgeHoverScrollOffset.y != 0) {
				setOriginX(getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition() + edgeHoverScrollOffset.x));
				setOriginY(getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition() + edgeHoverScrollOffset.y));
				
				edgeHoverScrollFuture = scheduler.schedule(new MoveViewportRunnable(), 100, TimeUnit.MILLISECONDS);
			}
		}
		
	}
	
	@Override
	public ILayerPainter getLayerPainter() {
		return super.getLayerPainter();
	}

}
