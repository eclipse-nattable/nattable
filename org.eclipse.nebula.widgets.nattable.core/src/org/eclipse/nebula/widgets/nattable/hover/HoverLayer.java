/*******************************************************************************
 * Copyright (c) 2013, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover;

import org.eclipse.nebula.widgets.nattable.hover.command.ClearHoverStylingCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.command.HoverStylingByIndexCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.command.HoverStylingCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.config.BodyHoverStylingBindings;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Point;

/**
 * This layer simply adds the possibility to render cells differently if the
 * mouse cursor is moved over them. This is also called "hover styling".
 * <p>
 * It knows over which cell the mouse cursor is currently set as it is informed
 * about that via IMouseActions that react on mouse move events.
 * <p>
 * The place where this layer is added to the layer stack has impact on the
 * result. If you for example add the SelectionLayer on top of the HoverLayer,
 * selected cells will not change their style while moving the mouse cursor over
 * them. Adding the HoverLayer on top of the SelectionLayer will also change the
 * style of the selected cells.
 * <p>
 * Note: If the HoverLayer should be added to the row or column header region,
 * the corresponding configurations need to be set instead of the default
 * configuration. This is because the row and column headers by default have
 * mouse move listeners registered that collide with the mouse move listener for
 * managing hover behaviour.
 */
public class HoverLayer extends AbstractIndexLayerTransform {

    /**
     * The position of the cell over which the mouse cursor is currently
     * located.
     */
    private Point currentHoveredCellPosition;
    /**
     * If set to <code>true</code> it will fire a {@link RowVisualUpdateEvent}
     * on hover. If also {@link #fireColumnUpdates} it set to <code>true</code>,
     * a {@link VisualRefreshEvent} will be fired on hover.
     */
    private boolean fireRowUpdates = false;
    /**
     * If set to <code>true</code> it will fire a
     * {@link ColumnVisualUpdateEvent} on hover. If also {@link #fireRowUpdates}
     * it set to <code>true</code>, a {@link VisualRefreshEvent} will be fired
     * on hover.
     */
    private boolean fireColumnUpdates = false;

    /**
     * Create a new HoverLayer that uses the default configuration.
     *
     * @param underlyingLayer
     *            The layer on which the HoverLayer should be positioned.
     */
    public HoverLayer(IUniqueIndexLayer underlyingLayer) {
        this(underlyingLayer, true);
    }

    /**
     * @param underlyingLayer
     *            The layer on which the HoverLayer should be positioned.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be used,
     *            <code>false</code> if a different configuration will be set
     *            after creation time.
     */
    public HoverLayer(IUniqueIndexLayer underlyingLayer, boolean useDefaultConfiguration) {
        super(underlyingLayer);

        if (useDefaultConfiguration) {
            addConfiguration(new BodyHoverStylingBindings(this));
        }

        registerCommandHandler(new HoverStylingCommandHandler(this));
        registerCommandHandler(new HoverStylingByIndexCommandHandler(this));
        registerCommandHandler(new ClearHoverStylingCommandHandler(this));
    }

    @Override
    public DisplayMode getDisplayModeByPosition(int columnPosition, int rowPosition) {
        if (isCellPositionHovered(columnPosition, rowPosition)) {
            return DisplayMode.HOVER;
        } else {
            return super.getDisplayModeByPosition(columnPosition, rowPosition);
        }
    }

    /**
     * Check if this HoverLayer knows the current hovered cell and if that cell
     * is located at the given position coordinates.
     *
     * @param cellPosition
     *            The position of the cell that should be checked.
     * @return <code>true</code> if the mouse cursor is currently located over
     *         the cell at the given position, <code>false</code> if not.
     */
    public boolean isCellPositionHovered(Point cellPosition) {
        return isCellPositionHovered(cellPosition.x, cellPosition.y);
    }

    /**
     * Check if this HoverLayer knows the current hovered cell and if that cell
     * is located at the given position coordinates.
     *
     * @param columnPosition
     *            The column position of the cell that should be checked.
     * @param rowPosition
     *            The row position of the cell that should be checked.
     * @return <code>true</code> if the mouse cursor is currently located over
     *         the cell at the given position, <code>false</code> if not.
     */
    public boolean isCellPositionHovered(int columnPosition, int rowPosition) {
        if (this.currentHoveredCellPosition != null) {
            ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
            if (cell != null) {
                return (this.currentHoveredCellPosition.x == cell.getOriginColumnPosition()
                        && this.currentHoveredCellPosition.y == cell.getOriginRowPosition());
            }
        }
        return false;
    }

    /**
     * Check if this HoverLayer knows the current hovered cell and if that cell
     * is at the given row position.
     *
     * @param rowPosition
     *            The row position of the cell that should be checked.
     * @return <code>true</code> if the current hovered cell is in the given row
     *         position, <code>false</code> if not.
     *
     * @since 2.6
     */
    public boolean isRowPositionHovered(int rowPosition) {
        return this.currentHoveredCellPosition != null
                && this.currentHoveredCellPosition.y == rowPosition;
    }

    /**
     * Check if this HoverLayer knows the current hovered cell and if that cell
     * is at the given column position.
     *
     * @param columnPosition
     *            The column position of the cell that should be checked.
     * @return <code>true</code> if the current hovered cell is in the given
     *         column position, <code>false</code> if not.
     *
     * @since 2.6
     */
    public boolean isColumnPositionHovered(int columnPosition) {
        return this.currentHoveredCellPosition != null
                && this.currentHoveredCellPosition.x == columnPosition;
    }

    /**
     * @return The position of the cell that is currently hovered.
     */
    public Point getCurrentHoveredCellPosition() {
        return this.currentHoveredCellPosition;
    }

    /**
     * Set the information about the cell position that is currently hovered and
     * fire an event to update a possible previous hovered cell to remove the
     * hover styling and an event to update the newly hovered cell to apply the
     * hover styling.
     *
     * @param columnPosition
     *            The column position of the cell that is currently hovered.
     * @param rowPosition
     *            The row position of the cell that is currently hovered.
     */
    public void setCurrentHoveredCellPosition(int columnPosition, int rowPosition) {
        setCurrentHoveredCellPosition(new Point(columnPosition, rowPosition));
    }

    /**
     * Set the information about the cell identified by index that is currently
     * hovered and fire an event to update a possible previous hovered cell to
     * remove the hover styling and an event to update the newly hovered cell to
     * apply the hover styling.
     *
     * @param columnIndex
     *            The column index of the cell that is currently hovered.
     * @param rowIndex
     *            The row index of the cell that is currently hovered.
     *
     * @since 2.1
     */
    public void setCurrentHoveredCellByIndex(int columnIndex, int rowIndex) {
        int columnPosition = getColumnPositionByIndex(columnIndex);
        int rowPosition = getRowPositionByIndex(rowIndex);
        setCurrentHoveredCellPosition(new Point(columnPosition, rowPosition));
    }

    /**
     * Set the information about the cell position that is currently hovered and
     * fire an event to update a possible previous hovered cell to remove the
     * hover styling and an event to update the newly hovered cell to apply the
     * hover styling.
     *
     * @param cellPosition
     *            The position of the cell that is currently hovered.
     */
    public void setCurrentHoveredCellPosition(Point cellPosition) {
        if (!isCellPositionHovered(cellPosition)) {
            Point oldHover = this.currentHoveredCellPosition;

            ILayerCell cell = getCellByPosition(cellPosition.x, cellPosition.y);
            if (cell != null) {
                this.currentHoveredCellPosition =
                        new Point(cell.getOriginColumnPosition(), cell.getOriginRowPosition());
            }

            if (oldHover != null) {
                fireUpdateEvent(oldHover.x, oldHover.y);
            }
            fireUpdateEvent(
                    this.currentHoveredCellPosition.x,
                    this.currentHoveredCellPosition.y);
        }
    }

    /**
     * Removes the local stored information about the cell position that is
     * currently hovered and fires an event to update the cell so the hover
     * styling is removed.
     */
    public void clearCurrentHoveredCellPosition() {
        if (this.currentHoveredCellPosition != null) {
            Point oldHover = this.currentHoveredCellPosition;
            this.currentHoveredCellPosition = null;
            fireUpdateEvent(oldHover.x, oldHover.y);
        }
    }

    /**
     * Configure whether visual updates for the whole row should be fired.
     * <p>
     * <b>Note:</b><br>
     * If {@link #fireColumnUpdates} and {@link #fireRowUpdates} are set to
     * <code>true</code> the whole table will be refreshed on hovering a single
     * cell.
     * </p>
     *
     * @param fireRowUpdates
     *            <code>true</code> if visual updates for the whole row should
     *            be fired.
     *
     * @since 2.6
     */
    public void setFireRowUpdates(boolean fireRowUpdates) {
        this.fireRowUpdates = fireRowUpdates;
    }

    /**
     *
     * @return <code>true</code> if visual updates for the whole row are fired.
     * @since 2.6
     */
    public boolean isFireRowUpdates() {
        return this.fireRowUpdates;
    }

    /**
     * Configure whether visual updates for the whole column should be fired.
     * <p>
     * <b>Note:</b><br>
     * If {@link #fireColumnUpdates} and {@link #fireRowUpdates} are set to
     * <code>true</code> the whole table will be refreshed on hovering a single
     * cell.
     * </p>
     *
     * @param fireColumnUpdates
     *            <code>true</code> if visual updates for the whole column
     *            should be fired.
     *
     * @since 2.6
     */
    public void setFireColumnUpdates(boolean fireColumnUpdates) {
        this.fireColumnUpdates = fireColumnUpdates;
    }

    /**
     *
     * @return <code>true</code> if visual updates for the whole column are
     *         fired.
     * @since 2.6
     */
    public boolean isFireColumnUpdates() {
        return this.fireColumnUpdates;
    }

    /**
     * Fire a visual update event according to the configuration of
     * {@link #fireColumnUpdates} and {@link #fireRowUpdates}.
     *
     * @param x
     *            The column position for the visual update event.
     * @param y
     *            The row position of the visual update event.
     */
    private void fireUpdateEvent(int x, int y) {
        if (this.fireRowUpdates && this.fireColumnUpdates) {
            fireLayerEvent(new VisualRefreshEvent(this));
        } else if (this.fireRowUpdates) {
            fireLayerEvent(new RowVisualUpdateEvent(this, y));
        } else if (this.fireColumnUpdates) {
            fireLayerEvent(new ColumnVisualUpdateEvent(this, x));
        } else {
            fireLayerEvent(new CellVisualUpdateEvent(this, x, y));
        }
    }
}
