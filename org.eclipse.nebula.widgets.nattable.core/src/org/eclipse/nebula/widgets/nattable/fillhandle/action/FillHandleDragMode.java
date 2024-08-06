/*****************************************************************************
 * Copyright (c) 2015, 2024 CEA LIST and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.viewport.action.AutoScrollDragMode;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

/**
 * The {@link IDragMode} that is registered to get triggered for dragging the
 * fill drag handle.
 *
 * @since 1.4
 */
public class FillHandleDragMode extends AutoScrollDragMode {

    protected MouseEvent startEvent;
    /**
     * @since 1.6
     */
    protected Point startPosition;
    protected MoveDirectionEnum direction;

    protected SelectionLayer selectionLayer;

    protected ILayerCell selectionCell;

    protected InternalCellClipboard clipboard;

    protected Menu menu;

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the fill handle
     *            region and perform the update command.
     * @param clipboard
     *            The internal clipboard that carries the cells for the copy
     *            &amp; paste operation triggered by using the fill handle.
     */
    public FillHandleDragMode(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        super(true, true);

        if (selectionLayer == null) {
            throw new IllegalArgumentException("SelectionLayer can not be null"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.clipboard = clipboard;
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        PositionCoordinate[] selectedCellPositions = this.selectionLayer.getSelectedCellPositions();
        if (selectedCellPositions.length > 0) {
            this.startEvent = event;

            this.selectionCell = this.selectionLayer.getCellByPosition(
                    selectedCellPositions[0].columnPosition,
                    selectedCellPositions[0].rowPosition);

            this.startPosition = new Point(
                    this.selectionCell.getColumnPosition(),
                    this.selectionCell.getRowPosition());
        }
    }

    @Override
    protected void performDragAction(
            NatTable natTable,
            int x, int y,
            MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {

        int natTableColumnPosition = natTable.getColumnPositionByX(x);
        int natTableRowPosition = natTable.getRowPositionByY(y);

        int selectedColumnPosition = LayerUtil.convertColumnPosition(natTable, natTableColumnPosition, this.selectionLayer);
        int selectedRowPosition = LayerUtil.convertRowPosition(natTable, natTableRowPosition, this.selectionLayer);

        if (natTableColumnPosition > -1 && natTableRowPosition > -1) {
            Rectangle actionBounds = null;

            int xStart = this.startPosition.x;
            int yStart = this.startPosition.y;

            Rectangle region = this.selectionLayer.getLastSelectedRegion();

            // only increase range in one direction
            int xDiff = calculateIncreasedPositiveDiff(
                    x,
                    (x < this.startEvent.x) ? this.selectionCell.getBounds().x : this.startEvent.x);
            int yDiff = calculateIncreasedPositiveDiff(
                    y,
                    (y < this.startEvent.y) ? this.selectionCell.getBounds().y : this.startEvent.y);
            if (selectedColumnPosition >= region.x && selectedColumnPosition < (region.x + region.width)) {
                xDiff = 0;
            }
            if (selectedRowPosition >= region.y && selectedRowPosition < (region.y + region.height)) {
                yDiff = 0;
            }

            int width = -1;
            int height = -1;

            // check if only drag operations in one direction are supported
            Direction direction = natTable.getConfigRegistry().getConfigAttribute(
                    FillHandleConfigAttributes.ALLOWED_FILL_DIRECTION,
                    DisplayMode.NORMAL,
                    this.selectionCell.getConfigLabels());

            if (direction == null) {
                direction = Direction.BOTH;
            }

            if (direction != Direction.NONE) {
                if (direction == Direction.VERTICAL
                        || (direction == Direction.BOTH && yDiff >= xDiff)) {
                    int diff = calculateIncreasedPositiveDiff(selectedRowPosition, this.startPosition.y);
                    height = Math.max(diff, this.selectionLayer.getSelectedRowCount());
                    width = this.selectionLayer.getSelectedColumnPositions().length;
                    this.direction = MoveDirectionEnum.DOWN;
                    if ((selectedRowPosition - this.startPosition.y) < 0) {
                        yStart = selectedRowPosition;
                        height = diff + this.selectionLayer.getSelectedRowCount() - 1;
                        this.direction = MoveDirectionEnum.UP;
                    }
                } else {
                    int diff = calculateIncreasedPositiveDiff(selectedColumnPosition, this.startPosition.x);
                    height = this.selectionLayer.getSelectedRowCount();
                    width = Math.max(diff, this.selectionLayer.getSelectedColumnPositions().length);
                    this.direction = MoveDirectionEnum.RIGHT;
                    if ((selectedColumnPosition - this.startPosition.x) < 0) {
                        xStart = selectedColumnPosition;
                        width = diff + this.selectionLayer.getSelectedColumnPositions().length - 1;
                        this.direction = MoveDirectionEnum.LEFT;
                    }
                }

                actionBounds = new Rectangle(
                        xStart,
                        yStart,
                        width,
                        height);

                this.selectionLayer.setFillHandleRegion(actionBounds);
                natTable.redraw();
            }
        }
    }

    /**
     * Calculates the difference between the two given values and increases the
     * value by one.
     *
     * @param selectedIndex
     *            The first value
     * @param relativeIndex
     *            The second value
     * @return The difference between the two given values increased by one.
     */
    protected int calculateIncreasedPositiveDiff(int selectedIndex, int relativeIndex) {
        int diff = selectedIndex - relativeIndex;
        if (diff < 0) {
            diff *= -1;
        }
        diff++;
        return diff;
    }

    @Override
    public void mouseUp(final NatTable natTable, MouseEvent event) {
        // Cancel any active viewport drag
        super.mouseUp(natTable, event);

        if (natTable.doCommand(
                new CopyDataToClipboardCommand(
                        "\t", //$NON-NLS-1$
                        System.getProperty("line.separator"), //$NON-NLS-1$
                        natTable.getConfigRegistry()))) {

            if (this.clipboard != null) {
                if (showMenu(natTable)) {
                    openMenu(natTable);
                } else {
                    natTable.doCommand(
                            new FillHandlePasteCommand(
                                    FillHandleOperation.COPY,
                                    this.direction,
                                    natTable.getConfigRegistry()));
                    reset(natTable);
                }
            } else {
                natTable.doCommand(
                        new FillHandlePasteCommand(
                                FillHandleOperation.COPY,
                                this.direction,
                                natTable.getConfigRegistry()));
                reset(natTable);
            }
        } else {
            reset(natTable);
        }
    }

    /**
     * Check if the menu should be shown for selecting copy or series fill
     * operation.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed.
     * @return <code>true</code> if the menu should be shown, <code>false</code>
     *         if not.
     */
    protected boolean showMenu(final NatTable natTable) {
        return FillHandleActionHelper.showMenu(natTable, this.clipboard);
    }

    /**
     * Opens a menu that enables a user to select whether values should simply
     * be copied or if a series should be filled.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed.
     */
    protected void openMenu(final NatTable natTable) {
        // lazily create the menu
        if (this.menu == null || this.menu.isDisposed()) {
            this.menu = FillHandleActionHelper.createFillHandleMenu(
                    natTable,
                    () -> FillHandleDragMode.this.direction,
                    (n) -> reset(n));
        }

        this.menu.setVisible(true);
    }

    /**
     * Reset the {@link FillHandleDragMode} states, the fill handle region in
     * the {@link SelectionLayer} and redraw the given NatTable.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed.
     */
    protected void reset(NatTable natTable) {
        this.selectionCell = null;
        this.startEvent = null;
        this.startPosition = null;
        this.direction = null;
        this.selectionLayer.setFillHandleRegion(null);
        this.clipboard.clear();
        if (!natTable.isDisposed()) {
            natTable.redraw();
        }
    }
}
