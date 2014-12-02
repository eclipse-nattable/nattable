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
package org.eclipse.nebula.widgets.nattable.hover;

import org.eclipse.nebula.widgets.nattable.hover.command.ClearHoverStylingCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.command.HoverStylingCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.config.BodyHoverStylingBindings;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualUpdateEvent;
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
 *
 * @author Dirk Fauth
 *
 */
public class HoverLayer extends AbstractIndexLayerTransform {

    /**
     * The position of the cell over which the mouse cursor is currently
     * located.
     */
    private Point currentHoveredCellPosition;

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
    public HoverLayer(IUniqueIndexLayer underlyingLayer,
            boolean useDefaultConfiguration) {
        super(underlyingLayer);

        if (useDefaultConfiguration)
            addConfiguration(new BodyHoverStylingBindings(this));

        registerCommandHandler(new HoverStylingCommandHandler(this));
        registerCommandHandler(new ClearHoverStylingCommandHandler(this));
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
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
        return (this.currentHoveredCellPosition != null
                && this.currentHoveredCellPosition.x == columnPosition && this.currentHoveredCellPosition.y == rowPosition);
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
    public void setCurrentHoveredCellPosition(int columnPosition,
            int rowPosition) {
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

            this.currentHoveredCellPosition = cellPosition;

            if (oldHover != null) {
                fireLayerEvent(new CellVisualUpdateEvent(this, oldHover.x,
                        oldHover.y));
            }
            fireLayerEvent(new CellVisualUpdateEvent(this,
                    this.currentHoveredCellPosition.x,
                    this.currentHoveredCellPosition.y));
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
            fireLayerEvent(new CellVisualUpdateEvent(this, oldHover.x,
                    oldHover.y));
        }
    }
}
