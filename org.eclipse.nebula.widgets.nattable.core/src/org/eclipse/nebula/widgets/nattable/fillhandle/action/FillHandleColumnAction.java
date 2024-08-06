/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

/**
 * {@link IMouseAction} that can be registered on double clicks on the fill
 * handle to trigger an automatic fill operation for the whole column.
 *
 * @since 2.5
 */
public class FillHandleColumnAction implements IMouseAction {

    protected SelectionLayer selectionLayer;
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
    public FillHandleColumnAction(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("SelectionLayer can not be null"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.clipboard = clipboard;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        if (this.selectionLayer.hasColumnSelection()) {

            if (natTable.doCommand(
                    new CopyDataToClipboardCommand(
                            "\t", //$NON-NLS-1$
                            System.getProperty("line.separator"), //$NON-NLS-1$
                            natTable.getConfigRegistry()))) {

                // set the fill handle region to be the whole column
                int startRow = this.selectionLayer.getSelectedRowPositions().iterator().next().start;
                Rectangle region = new Rectangle(
                        this.selectionLayer.getSelectedColumnPositions()[0],
                        startRow,
                        this.selectionLayer.getSelectedColumnPositions().length,
                        this.selectionLayer.getRowCount() - startRow);
                this.selectionLayer.setFillHandleRegion(region);

                if (this.clipboard != null) {
                    if (showMenu(natTable)) {
                        openMenu(natTable);
                    } else {
                        natTable.doCommand(
                                new FillHandlePasteCommand(
                                        FillHandleOperation.COPY,
                                        MoveDirectionEnum.DOWN,
                                        natTable.getConfigRegistry()));
                        reset(natTable);
                    }
                } else {
                    natTable.doCommand(
                            new FillHandlePasteCommand(
                                    FillHandleOperation.COPY,
                                    MoveDirectionEnum.DOWN,
                                    natTable.getConfigRegistry()));
                    reset(natTable);
                }
            } else {
                reset(natTable);
            }
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
                    () -> MoveDirectionEnum.DOWN,
                    (n) -> reset(n));
        }

        this.menu.setVisible(true);
    }

    /**
     * Reset the fill handle region in the {@link SelectionLayer}, clear the
     * clipboard and redraw the given NatTable.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed.
     */
    protected void reset(NatTable natTable) {
        this.selectionLayer.setFillHandleRegion(null);
        this.clipboard.clear();
        if (!natTable.isDisposed()) {
            natTable.redraw();
        }
    }

}
