/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 451217
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze;

import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeCommandHandler;
import org.eclipse.nebula.widgets.nattable.freeze.config.DefaultFreezeGridBindings;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentIndexLayer;
import org.eclipse.nebula.widgets.nattable.group.command.ViewportSelectColumnGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.CompositeFreezeLayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommandHandler;
import org.eclipse.swt.graphics.Point;

public class CompositeFreezeLayer extends CompositeLayer implements IUniqueIndexLayer {

    private final FreezeLayer freezeLayer;
    private final ViewportLayer viewportLayer;
    private final SelectionLayer selectionLayer;

    public CompositeFreezeLayer(FreezeLayer freezeLayer,
            ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
        this(freezeLayer, viewportLayer, selectionLayer, true);
    }

    public CompositeFreezeLayer(FreezeLayer freezeLayer,
            ViewportLayer viewportLayer, SelectionLayer selectionLayer,
            boolean useDefaultConfiguration) {
        super(2, 2);
        this.freezeLayer = freezeLayer;
        this.viewportLayer = viewportLayer;
        this.selectionLayer = selectionLayer;

        setChildLayer("FROZEN_REGION", freezeLayer, 0, 0); //$NON-NLS-1$
        setChildLayer("FROZEN_ROW_REGION", //$NON-NLS-1$
                new DimensionallyDependentIndexLayer(
                        viewportLayer.getScrollableLayer(), viewportLayer, freezeLayer),
                1, 0);
        setChildLayer("FROZEN_COLUMN_REGION", //$NON-NLS-1$
                new DimensionallyDependentIndexLayer(
                        viewportLayer.getScrollableLayer(), freezeLayer, viewportLayer),
                0, 1);
        setChildLayer("NONFROZEN_REGION", viewportLayer, 1, 1); //$NON-NLS-1$

        this.layerPainter = new CompositeFreezeLayerPainter(this);

        registerCommandHandlers();

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultFreezeGridBindings());
        }
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        // Bug 451217
        // if structural change events are fired that carry no explicit diff
        // information it is likely that the event handlers in the underlying
        // layers are not executed. The following code is intended to "repair"
        // possible inconsistent freeze-viewport states
        if (event instanceof RowStructuralChangeEvent
                && (((RowStructuralChangeEvent) event).getRowDiffs() == null
                        || ((RowStructuralChangeEvent) event).getRowDiffs().isEmpty())) {
            if (this.viewportLayer.getMinimumOriginRowPosition() < this.freezeLayer.getRowCount()) {
                this.viewportLayer.setMinimumOriginY(this.freezeLayer.getHeight());
            }
        }
        if (event instanceof ColumnStructuralChangeEvent
                && (((ColumnStructuralChangeEvent) event).getColumnDiffs() == null
                        || ((ColumnStructuralChangeEvent) event).getColumnDiffs().isEmpty())) {
            if (this.viewportLayer.getMinimumOriginColumnPosition() < this.freezeLayer.getColumnCount()) {
                this.viewportLayer.setMinimumOriginX(this.freezeLayer.getWidth());
            }
        }
        // Bug 470061
        // in case the all columns are frozen, we also need to "repair"
        // the inconsistent freeze-viewport states, as the viewport is not
        // able to update itself since it doesn't handle the structural change
        // event
        else if (event instanceof ColumnResizeEvent
                && this.freezeLayer.getColumnCount() == this.selectionLayer.getColumnCount()
                && this.viewportLayer.getMinimumOriginColumnPosition() < this.freezeLayer.getColumnCount()) {
            this.viewportLayer.setMinimumOriginX(this.freezeLayer.getWidth());
        }

        super.handleLayerEvent(event);
    }

    public boolean isFrozen() {
        return this.freezeLayer.isFrozen();
    }

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new FreezeCommandHandler(this.freezeLayer, this.viewportLayer, this.selectionLayer));

        final DimensionallyDependentIndexLayer frozenRowLayer =
                (DimensionallyDependentIndexLayer) getChildLayerByLayoutCoordinate(1, 0);
        frozenRowLayer.registerCommandHandler(new ViewportSelectRowCommandHandler(frozenRowLayer));

        final DimensionallyDependentIndexLayer frozenColumnLayer =
                (DimensionallyDependentIndexLayer) getChildLayerByLayoutCoordinate(0, 1);
        frozenColumnLayer.registerCommandHandler(new ViewportSelectColumnCommandHandler(frozenColumnLayer));
        frozenColumnLayer.registerCommandHandler(new ViewportSelectColumnGroupCommandHandler(frozenColumnLayer));
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        // if this layer should handle a ClientAreaResizeCommand we have to
        // ensure that it is only called on the ViewportLayer, as otherwise
        // an undefined behaviour could occur because the ViewportLayer
        // isn't informed about potential refreshes
        if (command instanceof ClientAreaResizeCommand) {
            this.viewportLayer.doCommand(command);
        }
        return super.doCommand(command);
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        int columnPosition = this.freezeLayer.getColumnPositionByIndex(columnIndex);
        if (columnPosition >= 0) {
            return columnPosition;
        }
        return this.freezeLayer.getColumnCount()
                + this.viewportLayer.getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        int rowPosition = this.freezeLayer.getRowPositionByIndex(rowIndex);
        if (rowPosition >= 0) {
            return rowPosition;
        }
        return this.freezeLayer.getRowCount()
                + this.viewportLayer.getRowPositionByIndex(rowIndex);
    }

    @Override
    protected int getLayoutXByColumnPosition(int compositeColumnPosition) {
        if (compositeColumnPosition < 0 || compositeColumnPosition >= getColumnCount()) {
            return 1;
        }
        return super.getLayoutXByColumnPosition(compositeColumnPosition);
    }

    @Override
    protected int getLayoutYByRowPosition(int compositeRowPosition) {
        if (compositeRowPosition < 0 || compositeRowPosition >= getRowCount()) {
            return 1;
        }
        return super.getLayoutYByRowPosition(compositeRowPosition);
    }

    @Override
    protected Point getLayoutXYByPosition(int compositeColumnPosition, int compositeRowPosition) {
        int layoutX = getLayoutXByColumnPosition(compositeColumnPosition);
        int layoutY = getLayoutYByRowPosition(compositeRowPosition);

        if (layoutX < 0 || layoutY < 0) {
            return null;
        }

        return new Point(layoutX, layoutY);
    }

    /**
     * This method is used to determine the bounds of a cell with column span in
     * case of an active freeze. This is needed because column positions can be
     * ambiguous if the start column of a spanned cell is moved below the frozen
     * area.
     *
     * @param columnPosition
     *            The column position that was used to retrieve the cell. Needed
     *            to identify the origin layout in order to know if the start
     *            needs to be searched in the frozen or the scrollable area.
     * @param startColumn
     *            The start column position of the spanned cell.
     * @param endColumn
     *            The end column position of the spanned cell.
     * @return int array that contains the start x position of a cell in the
     *         first element, and the width of the cell in the second element.
     * @since 1.6
     */
    public int[] getColumnBounds(int columnPosition, int startColumn, int endColumn) {
        int columnPositionLayout = getLayoutXByColumnPosition(columnPosition);
        int startColumnLayout = getLayoutXByColumnPosition(startColumn);
        int endColumnLayout = getLayoutXByColumnPosition(endColumn);

        int start = startColumn;
        int end = isFrozen() && endColumnLayout == 1 ? endColumn - this.viewportLayer.getScrollableLayer().getColumnPositionByX(this.viewportLayer.getOrigin().getX()) : endColumn;
        ILayer startLayer = null;

        int startX = 0;
        int width = 0;
        if (columnPositionLayout == endColumnLayout) {
            // if a column in the same layout was requested where the end
            // position is, we use the same layout for calculating start/end

            if (endColumnLayout == 0 || startColumn == columnPosition) {
                startX = getStartXOfColumnPosition(startColumn);
                int column = startColumn;
                for (; column <= endColumn; column++) {
                    width += getColumnWidthByPosition(column);
                }
            } else {
                startLayer = getChildLayerByLayoutCoordinate(endColumnLayout, 1);
                start = start - this.viewportLayer.getMinimumOriginColumnPosition();
                end = endColumn - this.viewportLayer.getMinimumOriginColumnPosition();
                startX = this.freezeLayer.getWidth() + startLayer.getStartXOfColumnPosition(start);
                int column = start;
                for (; column <= end; column++) {
                    width += this.viewportLayer.getColumnWidthByPosition(column);
                }
            }
        } else {
            startLayer = getChildLayerByLayoutCoordinate(startColumnLayout, 1);
            startX = this.freezeLayer.getStartXOfColumnPosition(start);
            int freezeWidth = 0;
            int column = start;
            for (; column < this.freezeLayer.getColumnCount(); column++) {
                freezeWidth += this.freezeLayer.getColumnWidthByPosition(column);
            }

            int endX = this.freezeLayer.getWidth() + this.viewportLayer.getStartXOfColumnPosition(end) + this.viewportLayer.getColumnWidthByPosition(end);
            width = Math.max(freezeWidth, endX - startX);
        }

        return new int[] { startX, width };
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        PositionCoordinate coord = this.freezeLayer.getTopLeftPosition();
        properties.setProperty(
                prefix + FreezeLayer.PERSISTENCE_TOP_LEFT_POSITION,
                coord.columnPosition + IPersistable.VALUE_SEPARATOR + coord.rowPosition);

        coord = this.freezeLayer.getBottomRightPosition();
        properties.setProperty(
                prefix + FreezeLayer.PERSISTENCE_BOTTOM_RIGHT_POSITION,
                coord.columnPosition + IPersistable.VALUE_SEPARATOR + coord.rowPosition);

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        String property = properties.getProperty(
                prefix + FreezeLayer.PERSISTENCE_TOP_LEFT_POSITION);
        PositionCoordinate topLeftPosition = null;
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property,
                    IPersistable.VALUE_SEPARATOR);
            String columnPosition = tok.nextToken();
            String rowPosition = tok.nextToken();
            topLeftPosition = new PositionCoordinate(
                    this.freezeLayer,
                    Integer.valueOf(columnPosition),
                    Integer.valueOf(rowPosition));
        }

        property = properties.getProperty(
                prefix + FreezeLayer.PERSISTENCE_BOTTOM_RIGHT_POSITION);
        PositionCoordinate bottomRightPosition = null;
        if (property != null) {
            StringTokenizer tok =
                    new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            String columnPosition = tok.nextToken();
            String rowPosition = tok.nextToken();
            bottomRightPosition = new PositionCoordinate(
                    this.freezeLayer,
                    Integer.valueOf(columnPosition),
                    Integer.valueOf(rowPosition));
        }

        // only restore a freeze state if there is one persisted
        if (topLeftPosition != null && bottomRightPosition != null) {
            if (topLeftPosition.columnPosition == -1
                    && topLeftPosition.rowPosition == -1
                    && bottomRightPosition.columnPosition == -1
                    && bottomRightPosition.rowPosition == -1) {
                FreezeHelper.unfreeze(this.freezeLayer, this.viewportLayer);
            } else {
                FreezeHelper.freeze(this.freezeLayer, this.viewportLayer,
                        topLeftPosition, bottomRightPosition);
            }
        }

        super.loadState(prefix, properties);
    }

}
