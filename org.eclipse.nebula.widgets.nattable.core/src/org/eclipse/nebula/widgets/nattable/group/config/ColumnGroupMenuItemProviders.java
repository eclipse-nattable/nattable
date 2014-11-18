/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.config;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.command.DisplayColumnGroupRenameDialogCommand;
import org.eclipse.nebula.widgets.nattable.group.command.RemoveColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @deprecated Use {@link MenuItemProviders} or the {@link PopupMenuBuilder}
 *             directly
 */
@Deprecated
public class ColumnGroupMenuItemProviders {

    public static IMenuItemProvider renameColumnGroupMenuItemProvider() {
        return renameColumnGroupMenuItemProvider(Messages
                .getString("ColumnGroups.renameColumnGroup")); //$NON-NLS-1$
    }

    public static IMenuItemProvider renameColumnGroupMenuItemProvider(
            final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable,
                    final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(menuLabel);
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        NatEventData natEventData = MenuItemProviders
                                .getNatEventData(e);
                        int columnPosition = natEventData.getColumnPosition();
                        natTable.doCommand(new DisplayColumnGroupRenameDialogCommand(
                                natTable, columnPosition));
                    }
                });
            }
        };
    }

    public static IMenuItemProvider removeColumnGroupMenuItemProvider() {
        return removeColumnGroupMenuItemProvider(Messages
                .getString("ColumnGroups.removeColumnGroup")); //$NON-NLS-1$
    }

    public static IMenuItemProvider removeColumnGroupMenuItemProvider(
            final String menuLabel) {
        return new IMenuItemProvider() {

            @Override
            public void addMenuItem(final NatTable natTable,
                    final Menu popupMenu) {
                MenuItem columnStyleEditor = new MenuItem(popupMenu, SWT.PUSH);
                columnStyleEditor.setText(menuLabel);
                columnStyleEditor.setEnabled(true);

                columnStyleEditor.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        NatEventData natEventData = MenuItemProviders
                                .getNatEventData(e);
                        int columnPosition = natEventData.getColumnPosition();
                        int columnIndex = natEventData.getNatTable()
                                .getColumnIndexByPosition(columnPosition);
                        natTable.doCommand(new RemoveColumnGroupCommand(
                                columnIndex));
                    }
                });
            }
        };
    }

}
