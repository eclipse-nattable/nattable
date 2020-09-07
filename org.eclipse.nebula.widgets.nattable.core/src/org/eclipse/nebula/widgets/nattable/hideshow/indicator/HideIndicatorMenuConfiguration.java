/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.hideshow.indicator;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.action.RowResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

/**
 * Configuration that is used to register ui bindings for opening menus on right
 * click on the cell edge of hidden columns and hidden rows.
 *
 * @since 1.6
 */
public class HideIndicatorMenuConfiguration extends AbstractUiBindingConfiguration {

    /**
     * The layer in the column header that should be used to determine the
     * height of the hidden column indicator. Should be the top most layer in
     * the column header region, e.g. the FilterRowHeaderComposite in case
     * filtering is included. Can be <code>null</code> which leads to label
     * inspection of the table row the mouse cursor moves over.
     */
    protected ILayer columnHeaderLayer;
    /**
     * The layer in the row header that should be used to determine the width of
     * the hidden row indicator. Should be the top most layer in the row header
     * region. Can be <code>null</code> which leads to label inspection of the
     * table column the mouse cursor moves over.
     */
    protected ILayer rowHeaderLayer;
    /**
     * The menu for the hidden column indicator.
     */
    protected Menu columnHideIndicatorMenu;
    /**
     * The menu for the hidden row indicator.
     */
    protected Menu rowHideIndicatorMenu;

    /**
     * Creates a menu configuration that attaches menus to the hidden row
     * indicator and the hidden column indicator.
     *
     * @param natTable
     *            The NatTable instance to which the context menus should be
     *            added to.
     * @param columnHeaderLayer
     *            The layer in the column header that should be used to
     *            determine the height of the hidden column indicator. Should be
     *            the top most layer in the column header region, e.g. the
     *            FilterRowHeaderComposite in case filtering is included. Can be
     *            <code>null</code> which leads to label inspection of the table
     *            row the mouse cursor moves over.
     * @param rowHeaderLayer
     *            The layer in the row header that should be used to determine
     *            the width of the hidden row indicator. Should be the top most
     *            layer in the row header region. Can be <code>null</code> which
     *            leads to label inspection of the table column the mouse cursor
     *            moves over.
     */
    public HideIndicatorMenuConfiguration(NatTable natTable, ILayer columnHeaderLayer, ILayer rowHeaderLayer) {
        this.columnHeaderLayer = columnHeaderLayer;
        this.rowHeaderLayer = rowHeaderLayer;
        this.columnHideIndicatorMenu = createColumnHeaderMenu(natTable).build();
        this.rowHideIndicatorMenu = createRowHeaderMenu(natTable).build();
    }

    /**
     * Creates the {@link PopupMenuBuilder} for the column hide indicator menu
     * with the menu items that should be added to the menu.
     *
     * @param natTable
     *            The NatTable where the menu should be attached.
     * @return The {@link PopupMenuBuilder} that is used to build the column
     *         hide indicator menu.
     */
    protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
        return new PopupMenuBuilder(natTable)
                .withMenuItemProvider(MenuItemProviders.showColumnMenuItemProvider(true, "%MenuItemProviders.showColumn", null)) //$NON-NLS-1$
                .withMenuItemProvider(MenuItemProviders.showAllColumnsMenuItemProvider("%MenuItemProviders.showAllColumns", null)); //$NON-NLS-1$
    }

    /**
     * Creates the {@link PopupMenuBuilder} for the row hide indicator menu with
     * the menu items that should be added to the menu.
     *
     * @param natTable
     *            The NatTable where the menu should be attached.
     * @return The {@link PopupMenuBuilder} that is used to build the row hide
     *         indicator menu.
     */
    protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
        return new PopupMenuBuilder(natTable)
                .withMenuItemProvider(MenuItemProviders.showRowMenuItemProvider(true, "%MenuItemProviders.showRow", null)) //$NON-NLS-1$
                .withMenuItemProvider(MenuItemProviders.showAllRowsMenuItemProvider("%MenuItemProviders.showAllRows", null)); //$NON-NLS-1$
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        if (this.columnHideIndicatorMenu != null) {
            // Mouse move - Show resize cursor
            uiBindingRegistry.registerFirstMouseMoveBinding(
                    new ColumnHideIndicatorEventMatcher(
                            SWT.NONE,
                            GridRegion.COLUMN_HEADER,
                            0,
                            this.columnHeaderLayer),
                    new ColumnResizeCursorAction());

            uiBindingRegistry.registerFirstMouseDownBinding(
                    new ColumnHideIndicatorEventMatcher(
                            SWT.NONE,
                            GridRegion.COLUMN_HEADER,
                            MouseEventMatcher.RIGHT_BUTTON,
                            this.columnHeaderLayer),
                    new PopupMenuAction(this.columnHideIndicatorMenu));
        }

        if (this.rowHideIndicatorMenu != null) {
            // Mouse move - Show resize cursor
            uiBindingRegistry.registerFirstMouseMoveBinding(
                    new RowHideIndicatorEventMatcher(
                            SWT.NONE,
                            GridRegion.ROW_HEADER,
                            0,
                            this.rowHeaderLayer),
                    new RowResizeCursorAction());

            uiBindingRegistry.registerFirstMouseDownBinding(
                    new RowHideIndicatorEventMatcher(
                            SWT.NONE,
                            GridRegion.ROW_HEADER,
                            MouseEventMatcher.RIGHT_BUTTON,
                            this.rowHeaderLayer),
                    new PopupMenuAction(this.rowHideIndicatorMenu));
        }

        if (this.columnHideIndicatorMenu != null
                || this.rowHideIndicatorMenu != null) {
            uiBindingRegistry.registerMouseMoveBinding(
                    new MouseEventMatcher(),
                    new ClearCursorAction());
        }
    }

}
