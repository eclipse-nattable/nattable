/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Stephan Wahlbrink - dim-based implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.viewport;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;
import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.VERTICAL;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Orientation;
import org.eclipse.nebula.widgets.nattable.coordinate.PixelCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.TransformIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
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
import org.eclipse.nebula.widgets.nattable.swt.SWTUtil;
import org.eclipse.nebula.widgets.nattable.viewport.command.RecalculateScrollBarsCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowCellInViewportCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.event.ScrollEvent;


/**
 * Viewport - the visible area of NatTable
 * 
 * Places a 'viewport' over the table. Introduces scroll bars over the table and
 * keeps them in sync with the data being displayed. This is typically placed over the
 * {@link SelectionLayer}.
 */
public class ViewportLayer extends TransformIndexLayer {
	
	static final int EDGE_HOVER_REGION_SIZE = 16;
	
	static final int PAGE_INTERSECTION_SIZE = EDGE_HOVER_REGION_SIZE;
	
	
	private final IUniqueIndexLayer scrollableLayer;
	
	// The viewport current origin
	private boolean viewportOff = false;
	private final int[] savedOriginPixel = new int[2];
	
	// Edge hover scrolling
	private MoveViewportRunnable edgeHoverRunnable;
	
	
	/**
	 * Creates a new viewport layer.
	 * 
	 * @param underlyingLayer the underlying scrollable layer
	 */
	public ViewportLayer(/*@NonNull*/ final IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
		this.scrollableLayer = underlyingLayer;
		
		registerCommandHandlers();
		
		updateDims();
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		
		for (final Orientation orientation : Orientation.values()) {
			disposeDim(orientation);
		}
		
		cancelEdgeHoverScroll();
	}
	
	
	public boolean isViewportOff() {
		return this.viewportOff;
	}
	
	
	@Override
	protected void updateDims() {
		final IUniqueIndexLayer scrollable = getScrollableLayer();
		if (scrollable == null) {
			return;
		}
		for (final Orientation orientation : Orientation.values()) {
			disposeDim(orientation);
			setDim(new ViewportDim(this, scrollable.getDim(orientation)));
		}
	}
	
	protected void disposeDim(final Orientation orientation) {
		final ViewportDim dim = get(orientation);
		if (dim != null) {
			dim.dispose();
		}
	}
	
	@Override
	public IViewportDim getDim(final Orientation orientation) {
		return (IViewportDim) super.getDim(orientation);
	}
	
	final ViewportDim get(final Orientation orientation) {
		return (ViewportDim) super.getDim(orientation);
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
	
	
	// Cell features
	
	@Override
	public Rectangle getBoundsByPosition(final int columnPosition, final int rowPosition) {
		final int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		final int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		final Rectangle bounds = getUnderlyingLayer().getBoundsByPosition(underlyingColumnPosition, underlyingRowPosition);
		
		bounds.x -= get(HORIZONTAL).getOriginPixel();
		bounds.y -= get(VERTICAL).getOriginPixel();
		
		return bounds;
	}
	
	protected void fireScrollEvent() {
		fireLayerEvent(new ScrollEvent(this));
	}

	@Override
	public boolean doCommand(final ILayerCommand command) {
		if (command instanceof ClientAreaResizeCommand && command.convertToTargetLayer(this)) {
			final ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;
			
			//remember the difference from client area to body region area
			//needed because the scrollbar will be removed and therefore the client area will become bigger
			final int widthDiff = clientAreaResizeCommand.getScrollable().getClientArea().width - clientAreaResizeCommand.getCalcArea().width;
			final int heightDiff = clientAreaResizeCommand.getScrollable().getClientArea().height - clientAreaResizeCommand.getCalcArea().height;
			
			get(HORIZONTAL).checkScrollBar(clientAreaResizeCommand.getScrollable());
			get(VERTICAL).checkScrollBar(clientAreaResizeCommand.getScrollable());
			
			get(HORIZONTAL).handleResize();
			get(VERTICAL).handleResize();
			
			//after handling the scrollbars recalculate the area to use for percentage calculation
			final Rectangle possibleArea = clientAreaResizeCommand.getScrollable().getClientArea();
			possibleArea.width -= widthDiff;
			possibleArea.height -= heightDiff;
			clientAreaResizeCommand.setCalcArea(possibleArea);
			
			return true;
		} else if (command instanceof TurnViewportOffCommand) {
			if (!isViewportOff()) {
				for (final Orientation orientation : Orientation.values()) {
					this.savedOriginPixel[orientation.ordinal()] = get(orientation).getOriginPixel();
				}
				this.viewportOff = true;
				fireScrollEvent();
			}
			return true;
		} else if (command instanceof TurnViewportOnCommand) {
			if (isViewportOff()) {
				this.viewportOff = false;
				for (final Orientation orientation : Orientation.values()) {
					get(orientation).doSetOriginPixel(this.savedOriginPixel[orientation.ordinal()]);
				}
				fireScrollEvent();
			}
			return true;
		} else if (command instanceof PrintEntireGridCommand) {
			get(HORIZONTAL).movePositionIntoViewport(0);
			get(VERTICAL).movePositionIntoViewport(0);
		}
		return super.doCommand(command);
	}

	/**
	 * Recalculate scrollbar characteristics.
	 */
	public void recalculateScrollBars() {
		get(HORIZONTAL).handleResize();
		get(VERTICAL).handleResize();
	}

	// Event handling

	@Override
	public void handleLayerEvent(final ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			final IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
			if (structuralChangeEvent.isHorizontalStructureChanged()) {
				get(HORIZONTAL).handleStructuralChange(structuralChangeEvent.getColumnDiffs());
			}
			if (structuralChangeEvent.isVerticalStructureChanged()) {
				get(VERTICAL).handleStructuralChange(structuralChangeEvent.getRowDiffs());
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
	 * Handle {@link CellSelectionEvent}
	 * @param selectionEvent
	 */
	private void processSelection(final CellSelectionEvent selectionEvent) {
		get(HORIZONTAL).movePositionIntoViewport(selectionEvent.getColumnPosition());
		get(VERTICAL).movePositionIntoViewport(selectionEvent.getRowPosition());
	}
	
	/**
	 * Handle {@link ColumnSelectionEvent}
	 * @param selectionEvent
	 */
	private void processColumnSelection(final ColumnSelectionEvent selectionEvent) {
		for (final Range columnPositionRange : selectionEvent.getColumnPositionRanges()) {
			get(HORIZONTAL).movePositionIntoViewport(columnPositionRange.end - 1);
		}
	}
	
	/**
	 * Handle {@link RowSelectionEvent}
	 * @param selectionEvent
	 */
	private void processRowSelection(final RowSelectionEvent selectionEvent) {
		final int explicitePosition = selectionEvent.getRowPositionToMoveIntoViewport();
		if (explicitePosition >= 0) {
			get(VERTICAL).movePositionIntoViewport(explicitePosition);
			return;
		}
	}
	
	/**
	 * Scrolls the viewport vertically by a page. This is done by creating a MoveSelectionCommand to move the selection, which will
	 * then trigger an update of the viewport.
	 * @param scrollSelectionCommand
	 */
	public void scrollVerticallyByAPage(final ScrollSelectionCommand scrollSelectionCommand) {
		getUnderlyingLayer().doCommand(scrollVerticallyByAPageCommand(scrollSelectionCommand));
	}
	
	protected MoveSelectionCommand scrollVerticallyByAPageCommand(final ScrollSelectionCommand scrollSelectionCommand) {
		return new MoveSelectionCommand(scrollSelectionCommand.getDirection(),
										getRowCount(),
										scrollSelectionCommand.isShiftMask(),
										scrollSelectionCommand.isControlMask());
	}
	
	
	// Accessors
	
	/**
	 * @return The scrollable layer underlying the viewport.
	 */
	public IUniqueIndexLayer getScrollableLayer() {
		return this.scrollableLayer;
	}
	
	@Override
	public String toString() {
		return "Viewport Layer"; //$NON-NLS-1$
	}
	
	
	// Compatibility
	
	/**
	 * @return The minimum origin pixel position.
	 * 
	 * @deprecated use {@link IViewportDim#getOriginPixel()}
	 */
	@Deprecated
	public PixelCoordinate getMinimumOrigin() {
		return new PixelCoordinate(get(HORIZONTAL).getOriginPixel(), get(VERTICAL).getOriginPixel());
	}
	
	/**
	 * @return The minimum origin column position
	 * 
	 * @deprecated use {@link IViewportDim#getMinimumOriginPosition()}
	 */
	@Deprecated
	public int getMinimumOriginColumnPosition() {
		return get(HORIZONTAL).getMinimumOriginPosition();
	}
	
	/**
	 * @return The minimum origin row position
	 * 
	 * @deprecated use {@link IViewportDim#getMinimumOriginPosition()}
	 */
	@Deprecated
	public int getMinimumOriginRowPosition() {
		return get(VERTICAL).getMinimumOriginPosition();
	}
	
	/**
	 * @return The origin pixel position
	 * 
	 * @deprecated use {@link IViewportDim#getOriginPixel()}
	 */
	@Deprecated
	public PixelCoordinate getOrigin() {
		return new PixelCoordinate(get(HORIZONTAL).getOriginPixel(), get(VERTICAL).getOriginPixel());
	}
	
	/**
	 * Set the origin X pixel position.
	 * @param newOriginX
	 * 
	 * @deprecated use {@link IViewportDim#setOriginPixel(int)}
	 */
	@Deprecated
	public void setOriginX(final int newOriginX) {
		get(HORIZONTAL).setOriginPixel(newOriginX);
	}
	
	/**
	 * Set the origin Y pixel position.
	 * @param newOriginY
	 * 
	 * @deprecated use {@link IViewportDim#setOriginPixel(int)}
	 */
	@Deprecated
	public void setOriginY(final int newOriginY) {
		get(VERTICAL).setOriginPixel(newOriginY);
	}
	
	/**
	 * Srcolls the table so that the specified cell is visible i.e. in the Viewport
	 * @param scrollableColumnPosition
	 * @param scrollableRowPosition
	 * @param forceEntireCellIntoViewport
	 */
	public void moveCellPositionIntoViewport(final int scrollableColumnPosition, final int scrollableRowPosition) {
		moveColumnPositionIntoViewport(scrollableColumnPosition);
		moveRowPositionIntoViewport(scrollableRowPosition);
	}

	/**
	 * Scrolls the viewport (if required) so that the specified column is visible.
	 * @param scrollableColumnPosition column position in terms of the Scrollable Layer
	 * 
	 * @deprecated use {@link IViewportDim#movePositionIntoViewport(int)}
	 */
	@Deprecated
	public void moveColumnPositionIntoViewport(final int scrollableColumnPosition) {
		get(HORIZONTAL).movePositionIntoViewport(scrollableColumnPosition);
	}
	
	/**
	 * @see #moveColumnPositionIntoViewport(int, boolean)
	 * 
	 * @deprecated use {@link IViewportDim#movePositionIntoViewport(int)}
	 */
	@Deprecated
	public void moveRowPositionIntoViewport(final int scrollableRowPosition) {
		get(VERTICAL).movePositionIntoViewport(scrollableRowPosition);
	}
	
	/**
	 * @return The width of the visible client area. Will recalculate horizontal dimension information if the width has changed.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public int getClientAreaWidth() {
		return get(HORIZONTAL).getClientAreaSize();
	}
	
	/**
	 * @return The height of the visible client area. Will recalculate vertical dimension information if the height has changed.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public int getClientAreaHeight() {
		return get(VERTICAL).getClientAreaSize();
	}
	
	
	// Edge hover scrolling
	
	/**
	 * Used for edge hover scrolling. Called from the ViewportDragCommandHandler.
	 * @param x
	 * @param y
	 */
	public void drag(final int x, final int y) {
		if (x < 0 && y < 0) {
			cancelEdgeHoverScroll();
			return;
		}
		
		MoveViewportRunnable move = this.edgeHoverRunnable;
		if (move == null) {
			move = new MoveViewportRunnable();
		}
		
		move.fast = true;
		boolean requireSchedule = false;
		
		final Rectangle clientArea = getClientAreaProvider().getClientArea();
		for (final Orientation orientation : Orientation.values()) {
			final Range range = SWTUtil.getRange(clientArea, orientation);
			final long pixel = (orientation == HORIZONTAL) ? x : y;
			int change = 0;
			if (pixel >= range.start && pixel < range.start + EDGE_HOVER_REGION_SIZE) {
				change = -1;
				if (pixel >= range.start + EDGE_HOVER_REGION_SIZE/2) {
					move.fast = false;
				}
			} else if (pixel >= range.end - EDGE_HOVER_REGION_SIZE && pixel < range.end) {
				change = 1;
				if (pixel < range.end - EDGE_HOVER_REGION_SIZE/2) {
					move.fast = false;
				}
			}
			move.change[orientation.ordinal()] = change;
			requireSchedule |= (change != 0);
		}
		
		if (requireSchedule) {
			move.schedule();
		} else {
			cancelEdgeHoverScroll();
		}
	}
	
	/**
	 * Cancels an edge hover scroll.
	 */
	private void cancelEdgeHoverScroll() {
		this.edgeHoverRunnable = null;
	}
	
	/**
	 * Runnable that incrementally scrolls the viewport when drag hovering over an edge.
	 */
	class MoveViewportRunnable implements Runnable {
		
		
		private final int[] change = new int[2];
		private boolean fast;
		
		private final Display display = Display.getCurrent();
		
		
		public MoveViewportRunnable() {
		}
		
		
		public void schedule() {
			if (ViewportLayer.this.edgeHoverRunnable != this) {
				ViewportLayer.this.edgeHoverRunnable = this;
				this.display.timerExec(500, this);
			}
		}
		
		@Override
		public void run() {
			if (ViewportLayer.this.edgeHoverRunnable != this) {
				return;
			}
			
			for (final Orientation orientation : Orientation.values()) {
				switch (this.change[orientation.ordinal()]) {
				case -1:
					get(orientation).scrollBackwardByPosition();
					break;
				case 1:
					get(orientation).scrollForwardByPosition();
					break;
				}
			}
			
			this.display.timerExec(this.fast ? 100 : 500, this);
		}
		
	}
	
}
