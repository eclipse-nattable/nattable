/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 462143
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PixelCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.print.command.PrintEntireGridCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;
import org.eclipse.nebula.widgets.nattable.selection.ScrollSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

/**
 * Viewport - the visible area of NatTable Places a 'viewport' over the table.
 * Introduces scroll bars over the table and keeps them in sync with the data
 * being displayed. This is typically placed over the {@link SelectionLayer}.
 */
public class ViewportLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private static final int EDGE_HOVER_REGION_SIZE = 12;

    private HorizontalScrollBarHandler hBarListener;
    private VerticalScrollBarHandler vBarListener;
    private final IUniqueIndexLayer scrollableLayer;

    private IScroller<?> horizontalScroller;
    private IScroller<?> verticalScroller;

    private boolean horizontalScrollbarEnabled = true;
    private boolean verticalScrollbarEnabled = true;

    // The viewport origin, in scrollable pixel coordinates.
    private PixelCoordinate origin = new PixelCoordinate(0, 0);
    private PixelCoordinate minimumOrigin = new PixelCoordinate(0, 0);
    private int minimumOriginColumnPosition = 0;
    private int minimumOriginRowPosition = 0;
    private boolean viewportOff = false;
    private PixelCoordinate savedOrigin = new PixelCoordinate(0, 0);

    // split viewport support
    /**
     * Only used for split viewport support to configure the maximum column
     * position this viewport instance should handle. If set to a positive
     * value, column positions to the right will not be handled.
     */
    private int maxColumnPosition = -1;
    /**
     * Only used for split viewport support to configure the minimum column
     * position this viewport instance should handle. If set to a positive
     * value, column positions to the left will not be handled.
     */
    private int minColumnPosition = -1;
    /**
     * Only used for split viewport support to configure the maximum row
     * position this viewport instance should handle. If set to a positive
     * value, row positions to the bottom will not be handled.
     */
    private int maxRowPosition = -1;
    /**
     * Only used for split viewport support to configure the minimum row
     * position this viewport instance should handle. If set to a positive
     * value, row positions to the top will not be handled.
     */
    private int minRowPosition = -1;

    // Cache
    private int cachedColumnCount = -1;
    private int cachedRowCount = -1;
    private int cachedClientAreaWidth = 0;
    private int cachedClientAreaHeight = 0;
    private int cachedWidth = -1;
    private int cachedHeight = -1;

    // Edge hover scrolling

    private MoveViewportRunnable edgeHoverRunnable;

    private KeepRowInsideViewportEventHandler resizeEventHandler;

    public ViewportLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
        this.scrollableLayer = underlyingLayer;

        registerCommandHandlers();

        registerEventHandler(new ViewportEventHandler(this));
    }

    @Override
    public void dispose() {
        super.dispose();

        if (this.hBarListener != null) {
            this.hBarListener.dispose();
            this.hBarListener = null;
        }

        if (this.vBarListener != null) {
            this.vBarListener.dispose();
            this.vBarListener = null;
        }

        cancelEdgeHoverScroll();
    }

    /**
     * Set a different horizontal scroller than the default one.
     *
     * @param scroller
     *            The scroller that should be used for horizontal scrolling.
     */
    public void setHorizontalScroller(IScroller<?> scroller) {
        this.horizontalScroller = scroller;
        // ensure to dispose and remove the already registered listener
        if (this.hBarListener != null) {
            this.hBarListener.dispose();
            this.hBarListener = null;
        }
    }

    /**
     * Set a different vertical scroller than the default one.
     *
     * @param scroller
     *            The scroller that should be used for vertical scrolling.
     */
    public void setVerticalScroller(IScroller<?> scroller) {
        this.verticalScroller = scroller;
        // ensure to dispose and remove the already registered listener
        if (this.vBarListener != null) {
            this.vBarListener.dispose();
            this.vBarListener = null;
        }
    }

    public int getMaxWidth() {
        if (getMaxColumnPosition() < 0) {
            return -1;
        } else {
            int maxWidth = 0;
            for (int i = 0; i < getMaxColumnPosition(); i++) {
                maxWidth += this.scrollableLayer.getColumnWidthByPosition(i);
            }
            return maxWidth;
        }
    }

    public int getMinVerticalStart() {
        if (getMinColumnPosition() < 0) {
            return -1;
        } else {
            int minStart = 0;
            for (int i = 0; i < getMinColumnPosition(); i++) {
                minStart += this.scrollableLayer.getColumnWidthByPosition(i);
            }
            return minStart;
        }
    }

    public int getMaxHeight() {
        if (getMaxRowPosition() < 0) {
            return -1;
        } else {
            int maxHeight = 0;
            for (int i = 0; i < getMaxRowPosition(); i++) {
                maxHeight += getRowHeightByPosition(i);
            }
            return maxHeight;
        }
    }

    public int getMinHorizontalStart() {
        if (getMinRowPosition() < 0) {
            return -1;
        } else {
            int minStart = 0;
            for (int i = 0; i < getMinRowPosition(); i++) {
                minStart += getRowHeightByPosition(i);
            }
            return minStart;
        }
    }

    // Minimum Origin

    /**
     * @return The minimum origin pixel position.
     */
    public PixelCoordinate getMinimumOrigin() {
        return this.minimumOrigin;
    }

    /**
     * @return The minimum origin column position
     */
    public int getMinimumOriginColumnPosition() {
        return this.minimumOriginColumnPosition;
    }

    /**
     * @return The minimum origin row position
     */
    public int getMinimumOriginRowPosition() {
        return this.minimumOriginRowPosition;
    }

    /**
     * Set the minimum origin X pixel position.
     *
     * @param newMinimumOriginX
     *            The new minimum origin x.
     */
    public void setMinimumOriginX(int newMinimumOriginX) {
        if (newMinimumOriginX >= 0) {

            int minStart = getMinVerticalStart();
            if (newMinimumOriginX < minStart) {
                newMinimumOriginX = minStart;
            }

            PixelCoordinate previousMinimumOrigin = this.minimumOrigin;

            if (newMinimumOriginX != this.minimumOrigin.getX()) {
                this.minimumOrigin = new PixelCoordinate(newMinimumOriginX, this.minimumOrigin.getY());
                this.minimumOriginColumnPosition = this.scrollableLayer.getColumnPositionByX(this.minimumOrigin.getX());
            }

            int delta = this.minimumOrigin.getX() - previousMinimumOrigin.getX();
            setOriginX(this.origin.getX() + delta);

            recalculateHorizontalScrollBar();
        }
    }

    /**
     * Set the minimum origin Y pixel position.
     *
     * @param newMinimumOriginY
     *            The new minimum origin y.
     */
    public void setMinimumOriginY(int newMinimumOriginY) {
        if (newMinimumOriginY >= 0) {

            int minStart = getMinHorizontalStart();
            if (newMinimumOriginY < minStart) {
                newMinimumOriginY = minStart;
            }

            PixelCoordinate previousMinimumOrigin = this.minimumOrigin;

            if (newMinimumOriginY != this.minimumOrigin.getY()) {
                this.minimumOrigin = new PixelCoordinate(this.minimumOrigin.getX(), newMinimumOriginY);
                this.minimumOriginRowPosition = this.scrollableLayer.getRowPositionByY(this.minimumOrigin.getY());
            }

            int delta = this.minimumOrigin.getY() - previousMinimumOrigin.getY();
            setOriginY(this.origin.getY() + delta);

            recalculateVerticalScrollBar();
        }
    }

    /**
     * Set the minimum origin pixel position to the given values.
     *
     * @param newMinimumOriginX
     *            The new minimum origin x.
     * @param newMinimumOriginY
     *            The new minimum origin y.
     */
    public void setMinimumOrigin(int newMinimumOriginX, int newMinimumOriginY) {
        setMinimumOriginX(newMinimumOriginX);
        setMinimumOriginY(newMinimumOriginY);
    }

    // Origin

    /**
     * @return The origin pixel position
     */
    public PixelCoordinate getOrigin() {
        return this.viewportOff ? this.minimumOrigin : this.origin;
    }

    /**
     * @return The origin column position
     */
    private int getOriginColumnPosition() {
        return this.scrollableLayer.getColumnPositionByX(getOrigin().getX());
    }

    /**
     * @return The origin row position
     */
    private int getOriginRowPosition() {
        return this.scrollableLayer.getRowPositionByY(getOrigin().getY());
    }

    /**
     * Range checking for origin X pixel position.
     *
     * @param x
     *            The x value to check.
     * @return A valid x value within bounds: minimum origin x < x < max x (=
     *         column 0 x + width)
     */
    private int boundsCheckOriginX(int x) {
        int min = this.minimumOrigin.getX();
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
     * Range checking for origin Y pixel position.
     *
     * @param y
     *            The y value to check.
     * @return A valid y value within bounds: minimum origin y < y < max y (=
     *         row 0 y + height)
     */
    private int boundsCheckOriginY(int y) {
        int min = this.minimumOrigin.getY();
        if (y <= min) {
            return min;
        }
        int max = Math.max(getUnderlyingLayer().getStartYOfRowPosition(0) + getUnderlyingLayer().getHeight(), min);
        if (y > max) {
            return max;
        }
        return y;
    }

    /**
     * Set the origin X pixel position.
     *
     * @param newOriginX
     *            The new origin x value.
     */
    public void setOriginX(int newOriginX) {
        newOriginX = boundsCheckOriginX(newOriginX);
        newOriginX = boundsCheckOriginX(adjustOriginX(newOriginX));

        if (newOriginX != this.origin.getX()) {
            invalidateHorizontalStructure();
            this.origin = new PixelCoordinate(newOriginX, this.origin.getY());
            fireScrollEvent();
        }
    }

    /**
     * Set the origin Y pixel position.
     *
     * @param newOriginY
     *            The new origin y value.
     */
    public void setOriginY(int newOriginY) {
        newOriginY = boundsCheckOriginY(newOriginY);
        newOriginY = boundsCheckOriginY(adjustOriginY(newOriginY));

        if (newOriginY != this.origin.getY()) {
            invalidateVerticalStructure();
            this.origin = new PixelCoordinate(this.origin.getX(), newOriginY);
            fireScrollEvent();
        }
    }

    /**
     * Reset the origin pixel position to the given values.
     *
     * @param newOriginX
     *            The new origin x value.
     * @param newOriginY
     *            The new origin y value.
     */
    public void resetOrigin(int newOriginX, int newOriginY) {
        PixelCoordinate previousOrigin = this.origin;

        this.minimumOrigin = new PixelCoordinate(0, 0);
        this.minimumOriginColumnPosition = 0;
        this.minimumOriginRowPosition = 0;
        this.origin = new PixelCoordinate(newOriginX, newOriginY);

        if (this.origin.getX() != previousOrigin.getX()) {
            invalidateHorizontalStructure();
        }

        if (this.origin.getY() != previousOrigin.getY()) {
            invalidateVerticalStructure();
        }
    }

    // Split viewport support

    /**
     * @return The maximum column position of a split viewport or -1 in case
     *         there are no multiple viewports configured.
     */
    public int getMaxColumnPosition() {
        return this.maxColumnPosition;
    }

    /**
     * @param maxColumnPosition
     *            The right most column position in case split viewports need to
     *            be configured.
     */
    public void setMaxColumnPosition(int maxColumnPosition) {
        this.maxColumnPosition = maxColumnPosition;
    }

    /**
     * @return The minimum column position of a split viewport or -1 in case
     *         there are no multiple viewports configured.
     */
    public int getMinColumnPosition() {
        return this.minColumnPosition;
    }

    /**
     * Sets the minimum column position for a split viewport and directly sets
     * the minimum origin x value dependent on the configuration.
     *
     * @param minColumnPosition
     *            The left most column position in case split viewport need to
     *            be configured.
     */
    public void setMinColumnPosition(int minColumnPosition) {
        this.minColumnPosition = minColumnPosition;
        // set the minimum origin x dependent to the min column position
        int newMinOriginX = this.scrollableLayer.getStartXOfColumnPosition(this.minColumnPosition);
        setMinimumOriginX(newMinOriginX);
    }

    /**
     * @return The maximum row position of a split viewport or -1 in case there
     *         are no multiple viewports configured.
     */
    public int getMaxRowPosition() {
        return this.maxRowPosition;
    }

    /**
     * @param maxRowPosition
     *            The right most row position in case split viewports need to be
     *            configured.
     */
    public void setMaxRowPosition(int maxRowPosition) {
        this.maxRowPosition = maxRowPosition;
    }

    /**
     * @return The minimum row position of a split viewport or -1 in case there
     *         are no multiple viewports configured.
     */
    public int getMinRowPosition() {
        return this.minRowPosition;
    }

    /**
     * Sets the minimum row position for a split viewport and directly sets the
     * minimum origin y value dependent on the configuration.
     *
     * @param minRowPosition
     *            The left most row position in case split viewport need to be
     *            configured.
     */
    public void setMinRowPosition(int minRowPosition) {
        this.minRowPosition = minRowPosition;
        // set the minimum origin y dependent to the min row position
        int newMinOriginY = this.scrollableLayer.getStartYOfRowPosition(this.minRowPosition);
        setMinimumOriginY(newMinOriginY);
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
     * @return <i>visible</i> column count Note: This takes care of the frozen
     *         columns
     */
    @Override
    public int getColumnCount() {
        if (this.viewportOff) {
            // in case of split viewports we only return the number of columns
            // in the split
            if (getMaxColumnPosition() >= 0) {
                return getMaxColumnPosition();
            } else if (getMinColumnPosition() >= 0) {
                return Math.max(this.scrollableLayer.getColumnCount() - getMinColumnPosition(), 0);
            }

            return Math.max(this.scrollableLayer.getColumnCount() - getMinimumOriginColumnPosition(), 0);
        } else {
            if (this.cachedColumnCount < 0) {
                int availableWidth = getClientAreaWidth();
                if (availableWidth >= 0) {
                    // lower bound check
                    if (this.origin.getX() < this.minimumOrigin.getX()) {
                        this.origin = new PixelCoordinate(this.minimumOrigin.getX(), this.origin.getY());
                    }

                    recalculateAvailableWidthAndColumnCount();
                }
            }

            return this.cachedColumnCount;
        }
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.scrollableLayer.getColumnPositionByIndex(columnIndex) - getOriginColumnPosition();
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
        if (this.viewportOff) {
            int width = this.scrollableLayer.getWidth() - this.scrollableLayer.getStartXOfColumnPosition(getMinimumOriginColumnPosition());

            if (getMaxColumnPosition() >= 0) {
                int maxWidth = getMaxWidth();
                if (maxWidth < width) {
                    return maxWidth;
                }
            } else {
                return width;
            }
        }
        if (this.cachedWidth < 0) {
            recalculateAvailableWidthAndColumnCount();
        }
        return this.cachedWidth;
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
        if (this.viewportOff) {
            // in case of split viewports we only return the number of rows in
            // the split
            if (getMaxRowPosition() >= 0) {
                return getMaxRowPosition();
            } else if (getMinRowPosition() >= 0) {
                return Math.max(this.scrollableLayer.getRowCount() - getMinRowPosition(), 0);
            }

            return Math.max(this.scrollableLayer.getRowCount() - getMinimumOriginRowPosition(), 0);
        } else {
            if (this.cachedRowCount < 0) {
                int availableHeight = getClientAreaHeight();
                if (availableHeight >= 0) {

                    // lower bound check
                    if (this.origin.getY() < this.minimumOrigin.getY()) {
                        this.origin = new PixelCoordinate(this.origin.getX(), this.minimumOrigin.getY());
                    }

                    recalculateAvailableHeightAndRowCount();
                }
            }

            return this.cachedRowCount;
        }
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.scrollableLayer.getRowPositionByIndex(rowIndex) - getOriginRowPosition();
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
        if (this.viewportOff) {
            int height = this.scrollableLayer.getHeight() - this.scrollableLayer.getStartYOfRowPosition(getMinimumOriginRowPosition());
            if (getMaxRowPosition() >= 0) {
                int maxHeight = getMaxHeight();
                if (maxHeight < height) {
                    return maxHeight;
                }
            } else {
                return height;
            }
        }
        if (this.cachedHeight < 0) {
            recalculateAvailableHeightAndRowCount();
        }
        return this.cachedHeight;
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
        this.cachedColumnCount = -1;
        this.cachedClientAreaWidth = 0;
        this.cachedWidth = -1;
    }

    /**
     * Clear vertical caches
     */
    public void invalidateVerticalStructure() {
        this.cachedRowCount = -1;
        this.cachedClientAreaHeight = 0;
        this.cachedHeight = -1;
    }

    /**
     * Recalculate horizontal dimension properties.
     */
    protected void recalculateAvailableWidthAndColumnCount() {
        int clientAreaWidth = getMaxColumnPosition() >= 0 ? Math.min(getMaxWidth(), getClientAreaWidth()) : getClientAreaWidth();
        int availableWidth = clientAreaWidth;
        int originColumnPosition = getOriginColumnPosition();
        if (originColumnPosition >= 0) {
            availableWidth += getOrigin().getX() - getUnderlyingLayer().getStartXOfColumnPosition(originColumnPosition);
        }

        int maxColumnCount = getMaxColumnPosition() < 0 ? getUnderlyingLayer().getColumnCount() : getMaxColumnPosition();

        this.cachedWidth = 0;
        this.cachedColumnCount = 0;

        for (int columnPosition = originColumnPosition; columnPosition >= 0
                && columnPosition < maxColumnCount && availableWidth > 0; columnPosition++) {

            int width = getUnderlyingLayer().getColumnWidthByPosition(columnPosition);
            availableWidth -= width;
            this.cachedWidth += width;
            this.cachedColumnCount++;
        }

        if (this.cachedColumnCount == maxColumnCount
                && this.cachedWidth != getUnderlyingLayer().getWidth()) {
            this.cachedWidth = getUnderlyingLayer().getWidth();
        }

        if (this.cachedWidth > clientAreaWidth) {
            this.cachedWidth = clientAreaWidth;
        }

        int checkedOriginX = boundsCheckOriginX(this.origin.getX());
        if (checkedOriginX != this.origin.getX()) {
            this.origin = new PixelCoordinate(checkedOriginX, this.origin.getY());
        }
    }

    /**
     * Recalculate vertical dimension properties.
     */
    protected void recalculateAvailableHeightAndRowCount() {
        int clientAreaHeight = getMaxRowPosition() >= 0 ? Math.min(getMaxHeight(), getClientAreaHeight()) : getClientAreaHeight();
        int availableHeight = clientAreaHeight;
        int originRowPosition = getOriginRowPosition();
        if (originRowPosition >= 0) {
            availableHeight += getOrigin().getY() - getUnderlyingLayer().getStartYOfRowPosition(originRowPosition);
        }

        int maxRowCount = getMaxRowPosition() < 0 ? getUnderlyingLayer().getRowCount() : getMaxRowPosition();

        this.cachedHeight = 0;
        this.cachedRowCount = 0;

        for (int rowPosition = originRowPosition; rowPosition >= 0
                && rowPosition < maxRowCount && availableHeight > 0; rowPosition++) {
            int height = getUnderlyingLayer().getRowHeightByPosition(rowPosition);
            availableHeight -= height;
            this.cachedHeight += height;
            this.cachedRowCount++;
        }

        if (this.cachedRowCount == maxRowCount
                && this.cachedHeight != getUnderlyingLayer().getHeight()) {
            this.cachedHeight = getUnderlyingLayer().getHeight();
        }

        if (this.cachedHeight > clientAreaHeight)
            this.cachedHeight = clientAreaHeight;

        int checkedOriginY = boundsCheckOriginY(this.origin.getY());
        if (checkedOriginY != this.origin.getY()) {
            this.origin = new PixelCoordinate(this.origin.getX(), checkedOriginY);
        }
    }

    /**
     * Scrolls the table so that the specified cell is visible i.e. in the
     * Viewport
     *
     * @param scrollableColumnPosition
     *            The column position to scroll to.
     * @param scrollableRowPosition
     *            The row position to scroll to.
     */
    public void moveCellPositionIntoViewport(int scrollableColumnPosition, int scrollableRowPosition) {
        moveColumnPositionIntoViewport(scrollableColumnPosition);
        moveRowPositionIntoViewport(scrollableRowPosition);
    }

    /**
     * Scrolls the viewport (if required) so that the specified column is
     * visible.
     *
     * @param scrollableColumnPosition
     *            column position in terms of the Scrollable Layer
     */
    public void moveColumnPositionIntoViewport(int scrollableColumnPosition) {
        ILayer underlyingLayer = getUnderlyingLayer();
        int maxWidth = getMaxWidth();
        if (underlyingLayer.getColumnIndexByPosition(scrollableColumnPosition) >= 0
                && (maxWidth < 0 || (maxWidth >= 0
                        && underlyingLayer.getStartXOfColumnPosition(scrollableColumnPosition) < maxWidth))) {
            if (scrollableColumnPosition >= getMinimumOriginColumnPosition()) {
                int originColumnPosition = getOriginColumnPosition();

                if (scrollableColumnPosition <= originColumnPosition) {
                    // Move left
                    setOriginX(this.scrollableLayer.getStartXOfColumnPosition(scrollableColumnPosition));
                } else {
                    int scrollableColumnStartX = underlyingLayer.getStartXOfColumnPosition(scrollableColumnPosition);
                    int scrollableColumnEndX = scrollableColumnStartX + underlyingLayer.getColumnWidthByPosition(scrollableColumnPosition);
                    int clientAreaWidth = getClientAreaWidth();
                    int viewportEndX = getOrigin().getX() + clientAreaWidth;

                    int maxX = maxWidth >= 0 ? Math.min(maxWidth, scrollableColumnEndX) : scrollableColumnEndX;

                    if (viewportEndX < maxX) {
                        // Move right
                        setOriginX(Math.min(maxX - clientAreaWidth, maxX));
                    }
                }

                // TEE: adjust scrollbar to reflect new position
                adjustHorizontalScrollBar();
            }
        }
    }

    /**
     * @param scrollableRowPosition
     *            The row position to scroll to.
     * @see #moveColumnPositionIntoViewport(int)
     */
    public void moveRowPositionIntoViewport(int scrollableRowPosition) {
        ILayer underlyingLayer = getUnderlyingLayer();
        int maxHeight = getMaxHeight();
        if (underlyingLayer.getRowIndexByPosition(scrollableRowPosition) >= 0
                && (maxHeight < 0 || (maxHeight >= 0
                        && underlyingLayer.getStartYOfRowPosition(scrollableRowPosition) < maxHeight))) {
            if (scrollableRowPosition >= getMinimumOriginRowPosition()) {
                int originRowPosition = getOriginRowPosition();

                if (scrollableRowPosition <= originRowPosition) {
                    // Move up
                    setOriginY(this.scrollableLayer.getStartYOfRowPosition(scrollableRowPosition));
                } else {
                    int scrollableRowStartY = underlyingLayer.getStartYOfRowPosition(scrollableRowPosition);
                    int scrollableRowEndY = scrollableRowStartY + underlyingLayer.getRowHeightByPosition(scrollableRowPosition);
                    int clientAreaHeight = getClientAreaHeight();
                    int viewportEndY = getOrigin().getY() + clientAreaHeight;

                    int maxY = maxHeight >= 0 ? Math.min(maxHeight, scrollableRowEndY) : scrollableRowEndY;

                    if (viewportEndY < maxY) {
                        // Move down
                        setOriginY(Math.min(maxY - clientAreaHeight, maxY));
                    }
                }

                // TEE: at least adjust scrollbar to reflect new position
                adjustVerticalScrollBar();

                // add a listener that is ensuring to keep the selection in the
                // viewport for 100ms
                // this is necessary for keeping the cell in the viewport if
                // automatically resize events are generated (see Bug 411670)
                if (this.resizeEventHandler == null) {
                    this.resizeEventHandler = new KeepRowInsideViewportEventHandler(scrollableRowPosition);
                    registerEventHandler(this.resizeEventHandler);
                    Display.getCurrent().timerExec(100, new Runnable() {
                        @Override
                        public void run() {
                            // check if no resize events occurred and unregister
                            // in that case
                            if (!ViewportLayer.this.resizeEventHandler.handled) {
                                unregisterEventHandler(ViewportLayer.this.resizeEventHandler);
                                ViewportLayer.this.resizeEventHandler = null;
                            } else {
                                // reset the handled flag and wait for another
                                // 100ms
                                ViewportLayer.this.resizeEventHandler.handled = false;
                                Display.getCurrent().timerExec(100, this);
                            }
                        }
                    });
                }
            }
        }
    }

    protected void fireScrollEvent() {
        fireLayerEvent(new ScrollEvent(this));
    }

    boolean processingClientAreaResizeCommand = false;

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ClientAreaResizeCommand
                && command.convertToTargetLayer(this)) {
            if (this.processingClientAreaResizeCommand) {
                return false;
            }

            this.processingClientAreaResizeCommand = true;

            ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;

            // remember the difference from client area to body region area
            // needed because the scrollbar will be removed and therefore the
            // client area will become bigger
            Scrollable scrollable = clientAreaResizeCommand.getScrollable();
            Rectangle clientArea = scrollable.getClientArea();
            Rectangle calcArea = clientAreaResizeCommand.getCalcArea();
            int widthDiff = clientArea.width - calcArea.width;
            int heightDiff = clientArea.height - calcArea.height;

            if (this.hBarListener == null && this.horizontalScrollbarEnabled) {
                ScrollBar hBar = scrollable.getHorizontalBar();

                if (this.horizontalScroller != null) {
                    hBar.setEnabled(false);
                    hBar.setVisible(false);
                } else {
                    this.horizontalScroller = new ScrollBarScroller(hBar);
                }

                this.hBarListener = new HorizontalScrollBarHandler(this, this.horizontalScroller);

                if (scrollable instanceof NatTable) {
                    this.hBarListener.setTable((NatTable) scrollable);
                }
            }

            if (this.vBarListener == null && this.verticalScrollbarEnabled) {
                ScrollBar vBar = scrollable.getVerticalBar();

                if (this.verticalScroller != null) {
                    vBar.setEnabled(false);
                    vBar.setVisible(false);
                } else {
                    this.verticalScroller = new ScrollBarScroller(vBar);
                }

                this.vBarListener = new VerticalScrollBarHandler(this, this.verticalScroller);

                if (scrollable instanceof NatTable) {
                    this.vBarListener.setTable((NatTable) scrollable);
                }
            }

            handleGridResize();

            // after handling the scrollbars recalculate the area to use for
            // percentage calculation
            Rectangle possibleArea = scrollable.getClientArea();
            possibleArea.width = possibleArea.width - widthDiff;
            possibleArea.height = possibleArea.height - heightDiff;
            clientAreaResizeCommand.setCalcArea(possibleArea);

            // we don't return true here because the ClientAreaResizeCommand
            // needs to be handled by the DataLayer in case percentage sizing is
            // enabled. if we would return true, the DataLayer wouldn't be able
            // to calculate the column/row sizes regarding the client area
            boolean result = super.doCommand(command);

            // we need to first give underlying layers the chance to process the
            // command and afterwards set the processing flag to false
            // this way we avoid processing the resize multiple times because of
            // re-calculation in conjunction with scrollbar visibility state
            // changes
            this.processingClientAreaResizeCommand = false;

            return result;
        } else if (command instanceof TurnViewportOffCommand) {
            this.savedOrigin = this.origin;
            this.viewportOff = true;
            return true;
        } else if (command instanceof TurnViewportOnCommand) {
            this.viewportOff = false;
            this.origin = this.savedOrigin;
            // only necessary in case of split viewports and auto resizing, but
            // shouldn't hurt in other cases
            recalculateScrollBars();
            return true;
        } else if (command instanceof PrintEntireGridCommand) {
            moveCellPositionIntoViewport(0, 0);
        }
        return super.doCommand(command);
    }

    /**
     * Recalculate horizontal scrollbar characteristics.
     */
    private void recalculateHorizontalScrollBar() {
        if (this.hBarListener != null) {
            this.hBarListener.recalculateScrollBarSize();

            if (!this.hBarListener.scroller.isDisposed()
                    && !this.hBarListener.scroller.getEnabled()) {
                setOriginX(this.minimumOrigin.getX());
            } else {
                setOriginX(this.origin.getX());
            }
        }
    }

    /**
     * Recalculate vertical scrollbar characteristics;
     */
    private void recalculateVerticalScrollBar() {
        if (this.vBarListener != null) {
            this.vBarListener.recalculateScrollBarSize();

            if (!this.vBarListener.scroller.isDisposed()
                    && !this.vBarListener.scroller.getEnabled()) {
                setOriginY(this.minimumOrigin.getY());
            } else {
                setOriginY(this.origin.getY());
            }
        }
    }

    /**
     * Recalculate scrollbar characteristics.
     */
    public void recalculateScrollBars() {
        recalculateHorizontalScrollBar();
        recalculateVerticalScrollBar();
    }

    /**
     * Recalculate viewport characteristics when the grid has been resized.
     */
    protected void handleGridResize() {
        setOriginX(this.origin.getX());
        recalculateHorizontalScrollBar();
        setOriginY(this.origin.getY());
        recalculateVerticalScrollBar();
    }

    /**
     * If the client area size is greater than the content size, move origin to
     * fill as much content as possible.
     *
     * @param originX
     *            The origin x value to adjust if necessary.
     */
    protected int adjustOriginX(int originX) {
        if (getColumnCount() == 0) {
            return 0;
        }

        int availableWidth = getClientAreaWidth()
                - (this.scrollableLayer.getWidth() - originX);
        if (availableWidth <= 0) {
            // in case there is a maximum number of columns configured for
            // multiple viewports we need to ensure that there is no gap
            int clientAreaWidth = getClientAreaWidth();

            if (getMaxColumnPosition() >= 0 && clientAreaWidth >= getWidth()) {
                int visibleWidth = calculateVisibleWidth(originX);
                if (visibleWidth < clientAreaWidth) {
                    originX -= clientAreaWidth - visibleWidth;
                }
            }

            return originX;
        } else {
            return boundsCheckOriginX(originX - availableWidth);
        }
    }

    /**
     * This method will be called in case of split viewports. It is used to
     * calculate the width of the visible columns, taking into account the
     * origin and a possible not completely rendered column. The result will be
     * interpreted by adjusting the originX in case there is less visible
     * rendering for the set origin compared to the client area width. In this
     * case the originX needs to be adjusted to fill a gap that would exist
     * otherwise.
     *
     * @param originX
     *            The originX that is currently set.
     * @return The width of the visible columns for the current set origin.
     */
    private int calculateVisibleWidth(int originX) {
        int partialVisibleColumnWidth = getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition() + 1) - originX;
        int visibleWidth = partialVisibleColumnWidth;
        for (int i = getOriginColumnPosition() + 1; i < getMaxColumnPosition(); i++) {
            visibleWidth += getUnderlyingLayer().getColumnWidthByPosition(i);
        }
        return visibleWidth;
    }

    /**
     * If the client area size is greater than the content size, move origin to
     * fill as much content as possible.
     *
     * @param originY
     *            The origin y value to adjust if necessary.
     */
    protected int adjustOriginY(int originY) {
        if (getRowCount() == 0) {
            return 0;
        }

        int availableHeight = getClientAreaHeight() - (this.scrollableLayer.getHeight() - originY);

        if (availableHeight <= 0) {
            // in case there is a maximum number of rows configured for multiple
            // viewports
            // we need to ensure that there is no gap
            int clientAreaHeight = getClientAreaHeight();
            if (getMaxRowPosition() >= 0 && clientAreaHeight >= getHeight()) {
                int visibleHeight = calculateVisibleHeight(originY);
                if (visibleHeight < clientAreaHeight) {
                    originY -= clientAreaHeight - visibleHeight;
                }
            }

            return originY;
        } else {
            return boundsCheckOriginY(originY - availableHeight);
        }
    }

    /**
     * This method will be called in case of split viewports. It is used to
     * calculate the height of the visible rows, taking into account the origin
     * and a possible not completely rendered row. The result will be
     * interpreted by adjusting the originY in case there is less visible
     * rendering for the set origin compared to the client area height. In this
     * case the originY needs to be adjusted to fill a gap that would exist
     * otherwise.
     *
     * @param originY
     *            The originY that is currently set.
     * @return The height of the visible rows for the current set origin.
     */
    private int calculateVisibleHeight(int originY) {
        int partialVisibleRowHeight = getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition() + 1) - originY;
        int visibleHeight = partialVisibleRowHeight;
        for (int i = getOriginRowPosition() + 1; i < getMaxRowPosition(); i++) {
            visibleHeight += getUnderlyingLayer().getRowHeightByPosition(i);
        }
        return visibleHeight;
    }

    /**
     * Scrolls the viewport vertically by a page. This is done by creating a
     * MoveSelectionCommand to move the selection, which will then trigger an
     * update of the viewport.
     *
     * @param scrollSelectionCommand
     *            The {@link ScrollSelectionCommand} that is transfered to a
     *            {@link MoveSelectionCommand}
     */
    public void scrollVerticallyByAPage(ScrollSelectionCommand scrollSelectionCommand) {
        getUnderlyingLayer().doCommand(scrollVerticallyByAPageCommand(scrollSelectionCommand));
    }

    protected MoveSelectionCommand scrollVerticallyByAPageCommand(ScrollSelectionCommand scrollSelectionCommand) {
        return new MoveSelectionCommand(
                scrollSelectionCommand.getDirection(),
                getRowCount(),
                scrollSelectionCommand.isShiftMask(),
                scrollSelectionCommand.isControlMask());
    }

    /**
     * @return <code>true</code> if last column is completely displayed,
     *         <code>false</code> otherwise
     */
    protected boolean isLastColumnCompletelyDisplayed() {
        int lastDisplayableColumnIndex = getUnderlyingLayer().getColumnIndexByPosition(getUnderlyingLayer().getColumnCount() - 1);
        int visibleColumnCount = getColumnCount();
        int lastVisibleColumnIndex = getColumnIndexByPosition(visibleColumnCount - 1);

        return (lastVisibleColumnIndex == lastDisplayableColumnIndex)
                && (getClientAreaWidth() >= getWidth());
    }

    /**
     * @return <code>true</code> if last row is completely displayed,
     *         <code>false</code> otherwise
     */
    protected boolean isLastRowCompletelyDisplayed() {
        int lastDisplayableRowIndex = getUnderlyingLayer().getRowIndexByPosition(getUnderlyingLayer().getRowCount() - 1);
        int visibleRowCount = getRowCount();
        int lastVisibleRowIndex = getRowIndexByPosition(visibleRowCount - 1);

        return (lastVisibleRowIndex == lastDisplayableRowIndex)
                && (getClientAreaHeight() >= getHeight());
    }

    // Event handling

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                invalidateHorizontalStructure();

                // saved origin correction for multi viewports
                if (this.viewportOff
                        && (getMaxColumnPosition() >= 0 || getMinColumnPosition() >= 0)
                        && event instanceof ColumnResizeEvent) {
                    correctSavedOriginX();
                }
            }
            if (structuralChangeEvent.isVerticalStructureChanged()) {
                invalidateVerticalStructure();

                // saved origin correction for multi viewports
                if (this.viewportOff
                        && (getMaxRowPosition() >= 0 || getMinRowPosition() >= 0)
                        && event instanceof RowResizeEvent) {
                    correctSavedOriginY();
                }
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
     * This method gets called in case of automatic column resize is performed
     * when split viewports are active.
     * <p>
     * Automatic resize commands will first turn the viewport off, then perform
     * the resizing and then turn the viewport on again. Turning the viewport
     * off and on again causes reapplying the origin, which has impact on split
     * viewport minimum/maximum origins.
     */
    private void correctSavedOriginX() {
        int newOriginX = this.savedOrigin.getX();

        int columnPosition = 0;
        if (getMinColumnPosition() >= 0) {
            int possibleWidth = 0;
            for (int col = columnPosition; col < getMinColumnPosition(); col++) {
                possibleWidth += this.scrollableLayer.getColumnWidthByPosition(col);
            }
            if (possibleWidth != this.minimumOrigin.getX()) {
                int delta = this.minimumOrigin.getX() - possibleWidth;
                newOriginX = newOriginX - delta;
                // as the width of the other split viewport has changed, we need
                // to update the minimum width too
                this.minimumOrigin = new PixelCoordinate(this.minimumOrigin.getX()
                        - delta, this.minimumOrigin.getY());
            }
        } else {
            int originX = this.savedOrigin.getX();
            int visibleWidth = calculateVisibleWidth(originX);
            int clientAreaWidth = getClientAreaWidth();
            if (visibleWidth < clientAreaWidth) {
                int possibleWidth = 0;
                int columnCount = getMaxColumnPosition() >= 0 ? getMaxColumnPosition() : this.scrollableLayer.getColumnCount();
                for (int col = columnPosition; col < columnCount; col++) {
                    possibleWidth += this.scrollableLayer.getColumnWidthByPosition(col);
                }
                if (possibleWidth >= clientAreaWidth) {
                    newOriginX = this.scrollableLayer.getStartXOfColumnPosition(columnPosition);
                } else {
                    newOriginX = this.scrollableLayer.getWidth() - clientAreaWidth;
                }
                newOriginX = Math.max(0, newOriginX);
            }
        }
        this.savedOrigin = new PixelCoordinate(newOriginX, this.savedOrigin.getY());
    }

    /**
     * This method gets called in case of automatic row resize is performed when
     * split viewports are active.
     * <p>
     * Automatic resize commands will first turn the viewport off, then perform
     * the resizing and then turn the viewport on again. Turning the viewport
     * off and on again causes reapplying the origin, which has impact on split
     * viewport minimum/maximum origins.
     * </p>
     */
    private void correctSavedOriginY() {
        int newOriginY = this.savedOrigin.getY();

        int rowPosition = 0;
        if (getMinRowPosition() >= 0) {
            int possibleHeight = 0;
            for (int row = rowPosition; row < getMinRowPosition(); row++) {
                possibleHeight += this.scrollableLayer.getRowHeightByPosition(row);
            }
            if (possibleHeight != this.minimumOrigin.getY()) {
                int delta = this.minimumOrigin.getY() - possibleHeight;
                newOriginY = newOriginY - delta;
                // as the height of the other split viewport has changed, we
                // need to update the minimum height too
                this.minimumOrigin = new PixelCoordinate(this.minimumOrigin.getX(), this.minimumOrigin.getY() - delta);
            }
        } else {
            int originY = this.savedOrigin.getY();
            int visibleHeight = calculateVisibleHeight(originY);
            int clientAreaHeight = getClientAreaHeight();
            if (visibleHeight < clientAreaHeight) {
                int possibleHeight = 0;
                int rowCount = getMaxRowPosition() >= 0 ? getMaxRowPosition() : this.scrollableLayer.getRowCount();
                for (int row = rowPosition; row < rowCount; row++) {
                    possibleHeight += this.scrollableLayer.getRowHeightByPosition(row);
                }
                if (possibleHeight >= clientAreaHeight) {
                    newOriginY = this.scrollableLayer.getStartYOfRowPosition(rowPosition);
                } else {
                    newOriginY = this.scrollableLayer.getHeight() - clientAreaHeight;
                }
                newOriginY = Math.max(0, newOriginY);
            }
        }
        this.savedOrigin = new PixelCoordinate(this.savedOrigin.getX(), newOriginY);
    }

    /**
     * Handle {@link CellSelectionEvent}
     *
     * @param selectionEvent
     *            The event to handle
     */
    private void processSelection(CellSelectionEvent selectionEvent) {
        moveCellPositionIntoViewport(
                selectionEvent.getColumnPosition(),
                selectionEvent.getRowPosition());
        adjustHorizontalScrollBar();
        adjustVerticalScrollBar();
    }

    /**
     * Handle {@link ColumnSelectionEvent}
     *
     * @param selectionEvent
     *            The event to handle
     */
    private void processColumnSelection(ColumnSelectionEvent selectionEvent) {
        for (Range columnPositionRange : selectionEvent.getColumnPositionRanges()) {
            moveColumnPositionIntoViewport(columnPositionRange.end - 1);
            adjustHorizontalScrollBar();
        }
    }

    /**
     * Handle {@link RowSelectionEvent}
     *
     * @param selectionEvent
     *            The event to handle
     */
    private void processRowSelection(RowSelectionEvent selectionEvent) {
        int rowPositionToMoveIntoViewport = selectionEvent.getRowPositionToMoveIntoViewport();
        if (rowPositionToMoveIntoViewport >= 0) {
            moveRowPositionIntoViewport(rowPositionToMoveIntoViewport);
            adjustVerticalScrollBar();
        }
    }

    /**
     * Adjusts horizontal scrollbar to sync with current state of viewport.
     */
    private void adjustHorizontalScrollBar() {
        if (this.hBarListener != null) {
            this.hBarListener.adjustScrollBar();
        }
    }

    /**
     * Adjusts vertical scrollbar to sync with current state of viewport.
     */
    private void adjustVerticalScrollBar() {
        if (this.vBarListener != null) {
            this.vBarListener.adjustScrollBar();
        }
    }

    // Accessors

    /**
     * @return The width of the visible client area. Will recalculate horizontal
     *         dimension information if the width has changed.
     */
    public int getClientAreaWidth() {
        int clientAreaWidth = getClientAreaProvider().getClientArea().width;
        if (clientAreaWidth != this.cachedClientAreaWidth) {
            invalidateHorizontalStructure();
            this.cachedClientAreaWidth = clientAreaWidth;
        }
        return this.cachedClientAreaWidth;
    }

    /**
     * @return The height of the visible client area. Will recalculate vertical
     *         dimension information if the height has changed.
     */
    public int getClientAreaHeight() {
        int clientAreaHeight = getClientAreaProvider().getClientArea().height;
        if (clientAreaHeight != this.cachedClientAreaHeight) {
            invalidateVerticalStructure();
            this.cachedClientAreaHeight = clientAreaHeight;
        }
        return this.cachedClientAreaHeight;
    }

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

    // Edge hover scrolling

    /**
     * Used for edge hover scrolling. Called from the
     * ViewportDragCommandHandler.
     *
     * @param x
     *            The x coordinate
     * @param y
     *            The y coordinate
     */
    public void drag(int x, int y) {
        if (x < 0 && y < 0) {
            cancelEdgeHoverScroll();
            return;
        }

        MoveViewportRunnable move = this.edgeHoverRunnable;
        if (move == null) {
            move = new MoveViewportRunnable();
        }

        Rectangle clientArea = getClientAreaProvider().getClientArea();
        {
            int change = 0;
            int minX = clientArea.x;
            int maxX = clientArea.x + clientArea.width;
            if (x >= minX && x < minX + EDGE_HOVER_REGION_SIZE) {
                change = -1;
            } else if (x >= maxX - EDGE_HOVER_REGION_SIZE && x < maxX) {
                change = 1;
            }
            move.x = change;
        }
        {
            int change = 0;
            int minY = clientArea.y;
            int maxY = clientArea.y + clientArea.height;
            if (y >= minY && y < minY + EDGE_HOVER_REGION_SIZE) {
                change = -1;
            } else if (y >= maxY - EDGE_HOVER_REGION_SIZE && y < maxY) {
                change = 1;
            }
            move.y = change;
        }

        if (move.x != 0 || move.y != 0) {
            move.schedule();
        } else {
            cancelEdgeHoverScroll();
        }
    }

    /**
     * Used to scroll in the given direction on drag operations outside the
     * visible region. Does not start a background thread for automatic
     * scrolling.
     *
     * @param horizontal
     *            The horizontal movement for the scroll operation
     *            <code>MoveDirectionEnum.LEFT</code>,
     *            <code>MoveDirectionEnum.RIGHT</code>,
     *            <code>MoveDirectionEnum.NONE</code>
     * @param vertical
     *            The vertical movement for the scroll operation
     *            <code>MoveDirectionEnum.UP</code>,
     *            <code>MoveDirectionEnum.DOWN</code>,
     *            <code>MoveDirectionEnum.NONE</code>
     * @since 1.3
     */
    public void drag(MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {
        if ((horizontal == null && vertical == null)
                || MoveDirectionEnum.NONE.equals(horizontal) && MoveDirectionEnum.NONE.equals(vertical)) {
            return;
        }

        int x = 0;
        int y = 0;

        switch (horizontal) {
            case LEFT:
                x = -1;
                break;
            case RIGHT:
                x = 1;
                break;
            case NONE:
                x = 0;
        }

        switch (vertical) {
            case UP:
                y = -1;
                break;
            case DOWN:
                y = 1;
                break;
            case NONE:
                y = 0;
        }

        if (x != 0) {
            setOriginX(getUnderlyingLayer().getStartXOfColumnPosition(
                    getOriginColumnPosition() + x));
        }
        if (y != 0) {
            setOriginY(getUnderlyingLayer().getStartYOfRowPosition(
                    getOriginRowPosition() + y));
        }
    }

    /**
     * Cancels an edge hover scroll.
     */
    private void cancelEdgeHoverScroll() {
        this.edgeHoverRunnable = null;
    }

    /**
     * Enable/disable the horizontal scrollbar in this ViewportLayer.
     * <p>
     * Note: Setting the value to <code>false</code> will avoid registering a
     * HorizontalScrollBarHandler, which means that there are no actions
     * performed on the horizontal scrollbar in any case. If a horizontal
     * scrollbar is rendered, it will be shown disabled. The rendering of
     * scrollbar is typically configured via style bit in the NatTable control.
     * So if there is a disabled scrollbar rendered check the style bits of the
     * NatTable, and try to remove SWT.H_SCROLL which is set in the default
     * style options.
     * </p>
     *
     * @param enabled
     *            <code>false</code> to disable the horizontal scrollbar,
     *            <code>true</code> to enable it.
     */
    public void setHorizontalScrollbarEnabled(boolean enabled) {
        this.horizontalScrollbarEnabled = enabled;
    }

    /**
     * Enable/disable the vertical scrollbar in this ViewportLayer.
     * <p>
     * Note: Setting the value to <code>false</code> will avoid registering a
     * VerticalScrollBarHandler which means that there are no actions performed
     * on the vertical scrollbar in any case. If a vertical scrollbar is
     * rendered, it will be shown disabled. The rendering of scrollbar is
     * typically configured via style bit in the NatTable control. So if there
     * is a disabled scrollbar rendered check the style bits of the NatTable,
     * and try to remove SWT.V_SCROLL which is set in the default style options.
     * </p>
     *
     * @param enabled
     *            <code>false</code> to disable the vertical scrollbar,
     *            <code>true</code> to enable it.
     */
    public void setVerticalScrollbarEnabled(boolean enabled) {
        this.verticalScrollbarEnabled = enabled;
    }

    /**
     * @return <code>true</code> because the {@link ViewportLayer} is intended
     *         to be a dynamic size layer.
     * @since 1.4
     */
    @Override
    public boolean isDynamicSizeLayer() {
        return true;
    }

    /**
     * Runnable that incrementally scrolls the viewport when drag hovering over
     * an edge.
     */
    class MoveViewportRunnable implements Runnable {

        private int x;
        private int y;

        private final Display display = Display.getCurrent();

        public MoveViewportRunnable() {}

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

            if (this.x != 0) {
                setOriginX(getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition() + this.x));
            }
            if (this.y != 0) {
                setOriginY(getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition() + this.y));
            }

            this.display.timerExec(100, this);
        }

    }

    /**
     * Event handler that ensures to keep a row inside the viewport. Necessary
     * for dynamic row height calculations that occur after a row got moved into
     * the viewport and is therefore moved out of it afterwards.
     */
    class KeepRowInsideViewportEventHandler implements ILayerEventHandler<RowResizeEvent> {

        private final int rowPosition;

        /**
         * Flag to indicate that a {@link RowResizeEvent} was handled.
         */
        public boolean handled = false;

        public KeepRowInsideViewportEventHandler(int rowPosition) {
            this.rowPosition = rowPosition;
        }

        @Override
        public void handleLayerEvent(RowResizeEvent event) {
            moveRowPositionIntoViewport(this.rowPosition);
            this.handled = true;
        }

        @Override
        public Class<RowResizeEvent> getLayerEventClass() {
            return RowResizeEvent.class;
        }
    }

}
