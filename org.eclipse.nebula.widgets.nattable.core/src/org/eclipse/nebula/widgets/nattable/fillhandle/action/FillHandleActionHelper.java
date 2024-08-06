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

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Helper class for the fill handle actions.
 *
 * @since 2.5
 */
public final class FillHandleActionHelper {

    private FillHandleActionHelper() {
        // private default constructor for helper class
    }

    /**
     * Check if the menu should be shown for selecting copy or series fill
     * operation.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed.
     * @param clipboard
     *            The internal clipboard that carries the cells for the copy
     *            &amp; paste operation triggered by using the fill handle.
     * @return <code>true</code> if the menu should be shown, <code>false</code>
     *         if not.
     */
    public static boolean showMenu(NatTable natTable, InternalCellClipboard clipboard) {
        if (clipboard == null || clipboard.getCopiedCells() == null) {
            return false;
        }

        Class<?> type = null;
        for (ILayerCell[] cells : clipboard.getCopiedCells()) {
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
     * Create the menu that should be opened on a fill operation.
     *
     * @param natTable
     *            The NatTable instance on which the operation is performed and
     *            the menu should be connected to.
     * @param directionProvider
     *            The {@link Supplier} that provides the The direction in which
     *            the fill handle was dragged.
     * @param resetRunnable
     *            The {@link Runnable} that should be executed when the menu is
     *            hidden.
     * @return The menu that should be opened on a fill operation.
     */
    public static Menu createFillHandleMenu(NatTable natTable, Supplier<MoveDirectionEnum> directionProvider, Consumer<NatTable> resetRunnable) {
        Menu menu = new Menu(natTable);
        MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
        copyItem.setText(Messages.getLocalizedMessage("%FillHandleDragMode.menu.item.copy")); //$NON-NLS-1$
        copyItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(
                        new FillHandlePasteCommand(
                                FillHandleOperation.COPY,
                                directionProvider.get(),
                                natTable.getConfigRegistry()));
            }
        });
        MenuItem seriesItem = new MenuItem(menu, SWT.PUSH);
        seriesItem.setText(Messages.getLocalizedMessage("%FillHandleDragMode.menu.item.series")); //$NON-NLS-1$
        seriesItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(
                        new FillHandlePasteCommand(
                                FillHandleOperation.SERIES,
                                directionProvider.get(),
                                natTable.getConfigRegistry()));
            }
        });

        // add a menu listener to reset the fill state when the menu is
        // closed
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuHidden(MenuEvent e) {
                // perform the reset operation asynchronously because on
                // several OS the hide event is processed BEFORE the
                // selection event
                Display.getDefault().asyncExec(() -> resetRunnable.accept(natTable));
            }
        });

        // add the dispose listener for disposing the menu
        natTable.addDisposeListener(e -> menu.dispose());

        return menu;
    }
}
