/*****************************************************************************
 * Copyright (c) 2015, 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.action;

import java.util.Date;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.viewport.action.AutoScrollDragMode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * The {@link IDragMode} that is registered to get triggered for dragging the
 * fill drag handle.
 *
 * @since 1.4
 */
public class FillHandleDragMode extends AutoScrollDragMode {

    protected MouseEvent startEvent;
    protected Point startIndex;
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

            this.startIndex = new Point(
                    this.selectionCell.getColumnIndex(),
                    this.selectionCell.getRowIndex());
        }
    }

    @Override
    protected void performDragAction(
            NatTable natTable,
            int x, int y,
            MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {

        int selectedColumnPosition = natTable.getColumnPositionByX(x);
        int selectedRowPosition = natTable.getRowPositionByY(y);

        int selectedColumnIndex = natTable.getColumnIndexByPosition(selectedColumnPosition);
        int selectedRowIndex = natTable.getRowIndexByPosition(selectedRowPosition);

        if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
            Rectangle actionBounds = null;

            int xStart = this.startIndex.x;
            int yStart = this.startIndex.y;

            Rectangle region = this.selectionLayer.getLastSelectedRegion();

            // only increase range in one direction
            int xDiff = calculateIncreasedPositiveDiff(
                    x,
                    (x < this.startEvent.x) ? this.selectionCell.getBounds().x : this.startEvent.x);
            int yDiff = calculateIncreasedPositiveDiff(
                    y,
                    (y < this.startEvent.y) ? this.selectionCell.getBounds().y : this.startEvent.y);
            if (selectedColumnIndex >= region.x && selectedColumnIndex < (region.x + region.width)) {
                xDiff = 0;
            }
            if (selectedRowIndex >= region.y && selectedRowIndex < (region.y + region.height)) {
                yDiff = 0;
            }

            int width = -1;
            int height = -1;

            // check if only drag operations in one direction are supported
            Direction direction = natTable.getConfigRegistry().getConfigAttribute(
                    FillHandleConfigAttributes.ALLOWED_FILL_DIRECTION,
                    DisplayMode.NORMAL,
                    this.selectionCell.getConfigLabels().getLabels());

            if (direction == null) {
                direction = Direction.BOTH;
            }

            if (direction != Direction.NONE) {
                if (direction == Direction.VERTICAL
                        || (direction == Direction.BOTH && yDiff >= xDiff)) {
                    int diff = calculateIncreasedPositiveDiff(selectedRowIndex, this.startIndex.y);
                    height = Math.max(diff, this.selectionLayer.getSelectedRowCount());
                    width = this.selectionLayer.getSelectedColumnPositions().length;
                    this.direction = MoveDirectionEnum.DOWN;
                    if ((selectedRowIndex - this.startIndex.y) < 0) {
                        yStart = selectedRowIndex;
                        height = diff + this.selectionLayer.getSelectedRowCount() - 1;
                        this.direction = MoveDirectionEnum.UP;
                    }
                } else {
                    int diff = calculateIncreasedPositiveDiff(selectedColumnIndex, this.startIndex.x);
                    height = this.selectionLayer.getSelectedRowCount();
                    width = Math.max(diff, this.selectionLayer.getSelectedColumnPositions().length);
                    this.direction = MoveDirectionEnum.RIGHT;
                    if ((selectedColumnIndex - this.startIndex.x) < 0) {
                        xStart = selectedColumnIndex;
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
        if (this.clipboard == null || this.clipboard.getCopiedCells() == null) {
            return false;
        }

        Class<?> type = null;
        for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
            for (ILayerCell cell : cells) {
                if (cell.getDataValue() == null) {
                    return false;
                } else {
                    if (type == null) {
                        type = cell.getDataValue().getClass();
                        if (!Number.class.isAssignableFrom(type)
                                && !Date.class.isAssignableFrom(type)) {
                            return false;
                        }
                    } else if (type != cell.getDataValue().getClass()) {
                        return false;
                    }
                }
            }
        }
        return true;
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
            this.menu = new Menu(natTable);
            MenuItem copyItem = new MenuItem(this.menu, SWT.PUSH);
            copyItem.setText(Messages.getLocalizedMessage("%FillHandleDragMode.menu.item.copy")); //$NON-NLS-1$
            copyItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    natTable.doCommand(
                            new FillHandlePasteCommand(
                                    FillHandleOperation.COPY,
                                    FillHandleDragMode.this.direction,
                                    natTable.getConfigRegistry()));
                }
            });
            MenuItem seriesItem = new MenuItem(this.menu, SWT.PUSH);
            seriesItem.setText(Messages.getLocalizedMessage("%FillHandleDragMode.menu.item.series")); //$NON-NLS-1$
            seriesItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    natTable.doCommand(
                            new FillHandlePasteCommand(
                                    FillHandleOperation.SERIES,
                                    FillHandleDragMode.this.direction,
                                    natTable.getConfigRegistry()));
                }
            });

            // add a menu listener to reset the fill state when the menu is
            // closed
            this.menu.addMenuListener(new MenuAdapter() {
                @Override
                public void menuHidden(MenuEvent e) {
                    // perform the reset operation asynchronously because on
                    // several OS the hide event is processed BEFORE the
                    // selection event
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            reset(natTable);
                        }
                    });
                }
            });

            // add the dispose listener for disposing the menu
            natTable.addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    FillHandleDragMode.this.menu.dispose();
                }
            });
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
        this.startIndex = null;
        this.direction = null;
        this.selectionLayer.setFillHandleRegion(null);
        this.clipboard.clear();
        natTable.redraw();
    }
}
