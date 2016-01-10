/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillDirection;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * The {@link IDragMode} that is registered to get triggered for dragging the
 * fill drag handle.
 *
 * @since 1.4
 */
public class FillHandleDragMode implements IDragMode {

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
     *            The internal clipboard that carries the cells for the copy &
     *            paste operation triggered by using the fill handle.
     */
    public FillHandleDragMode(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
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
    public void mouseMove(NatTable natTable, MouseEvent event) {
        Rectangle clientArea = natTable.getClientAreaProvider().getClientArea();

        int x = event.x;
        int y = event.y;

        MoveDirectionEnum horizontal = MoveDirectionEnum.NONE;
        if (event.x < 0) {
            horizontal = MoveDirectionEnum.LEFT;
            x = 0;
        } else if (event.x > clientArea.width) {
            horizontal = MoveDirectionEnum.RIGHT;
            x = clientArea.width;
        }

        MoveDirectionEnum vertical = MoveDirectionEnum.NONE;
        if (event.y < 0) {
            vertical = MoveDirectionEnum.UP;
            y = 0;
        } else if (event.y > clientArea.height) {
            vertical = MoveDirectionEnum.DOWN;
            y = clientArea.height;
        }

        if (natTable.doCommand(new ViewportDragCommand(horizontal, vertical))) {

            int selectedColumnPosition = natTable.getColumnPositionByX(x);
            int selectedRowPosition = natTable.getRowPositionByY(y);

            int selectedColumnIndex = natTable.getColumnIndexByPosition(selectedColumnPosition);
            int selectedRowIndex = natTable.getRowIndexByPosition(selectedRowPosition);

            if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
                Rectangle actionBounds = null;

                int xStart = this.startIndex.x;
                int yStart = this.startIndex.y;

                // only increase range in one direction
                int xDiff = selectedColumnIndex - this.startIndex.x;
                if (xDiff < 0) {
                    xDiff *= -1;
                }
                xDiff++;

                int yDiff = selectedRowIndex - this.startIndex.y;
                if (yDiff < 0) {
                    yDiff *= -1;
                }
                yDiff++;

                int width = -1;
                int height = -1;

                // check if only drag operations in one direction are supported
                FillDirection direction = natTable.getConfigRegistry().getConfigAttribute(
                        FillHandleConfigAttributes.ALLOWED_FILL_DIRECTION,
                        DisplayMode.NORMAL,
                        this.selectionCell.getConfigLabels().getLabels());

                if (direction == null) {
                    direction = FillDirection.BOTH;
                }

                if (direction == FillDirection.VERTICAL
                        || (direction == FillDirection.BOTH && yDiff >= xDiff)) {
                    height = Math.max(yDiff, this.selectionLayer.getSelectedRowCount());
                    width = this.selectionLayer.getSelectedColumnPositions().length;
                    this.direction = MoveDirectionEnum.DOWN;
                    if ((selectedRowIndex - this.startIndex.y) < 0) {
                        yStart = selectedRowIndex;
                        height = yDiff + this.selectionLayer.getSelectedRowCount() - 1;
                        this.direction = MoveDirectionEnum.UP;
                    }
                } else {
                    height = this.selectionLayer.getSelectedRowCount();
                    width = Math.max(xDiff, this.selectionLayer.getSelectedColumnPositions().length);
                    this.direction = MoveDirectionEnum.RIGHT;
                    if ((selectedColumnIndex - this.startIndex.x) < 0) {
                        xStart = selectedColumnIndex;
                        width = xDiff + this.selectionLayer.getSelectedColumnPositions().length - 1;
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

    @Override
    public void mouseUp(final NatTable natTable, MouseEvent event) {
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
                        if (!type.isAssignableFrom(Number.class)
                                && !type.isAssignableFrom(Date.class)) {
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
                    reset(natTable);
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
                    reset(natTable);
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
