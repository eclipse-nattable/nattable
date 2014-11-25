/*******************************************************************************
 * Copyright (c) 2013, 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453219
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

/**
 * Abstract implementation for adding header menus to a NatTable. There will be
 * header menus attached to the column header, the row header and the corner
 * region. By default empty menus will be attached, which will result in not
 * showing a menu. On creating a specialized header menu configuration you can
 * choose for which header region you want to add a menu.
 */
public class AbstractHeaderMenuConfiguration extends AbstractUiBindingConfiguration {

    /**
     * The column header menu.
     */
    protected Menu colHeaderMenu;
    /**
     * The row header menu
     */
    protected Menu rowHeaderMenu;
    /**
     * The corner region menu
     */
    protected Menu cornerMenu;

    /**
     * Creates a header menu configuration that attaches menus to the row
     * header, the column header and the corner region.
     */
    public AbstractHeaderMenuConfiguration(NatTable natTable) {
        this.colHeaderMenu = createColumnHeaderMenu(natTable).build();
        this.rowHeaderMenu = createRowHeaderMenu(natTable).build();
        this.cornerMenu = createCornerMenu(natTable).build();
    }

    /**
     * Creates the {@link PopupMenuBuilder} for the column header menu with the
     * menu items that should be added to the menu.
     *
     * @param natTable
     *            The NatTable where the menu should be attached.
     * @return The {@link PopupMenuBuilder} that is used to build the column
     *         header menu.
     */
    protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
        return new PopupMenuBuilder(natTable);
    }

    /**
     * Creates the {@link PopupMenuBuilder} for the row header menu with the
     * menu items that should be added to the menu.
     *
     * @param natTable
     *            The NatTable where the menu should be attached.
     * @return The {@link PopupMenuBuilder} that is used to build the row header
     *         menu.
     */
    protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
        return new PopupMenuBuilder(natTable);
    }

    /**
     * Creates the {@link PopupMenuBuilder} for the corner menu with the menu
     * items that should be added to the menu.
     *
     * @param natTable
     *            The NatTable where the menu should be attached.
     * @return The {@link PopupMenuBuilder} that is used to build the corner
     *         menu.
     */
    protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
        return new PopupMenuBuilder(natTable);
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        if (this.colHeaderMenu != null) {
            uiBindingRegistry.registerMouseDownBinding(
                    new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, MouseEventMatcher.RIGHT_BUTTON),
                    new PopupMenuAction(this.colHeaderMenu));
        }

        if (this.rowHeaderMenu != null) {
            uiBindingRegistry.registerMouseDownBinding(
                    new MouseEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, MouseEventMatcher.RIGHT_BUTTON),
                    new PopupMenuAction(this.rowHeaderMenu));
        }

        if (this.cornerMenu != null) {
            uiBindingRegistry.registerMouseDownBinding(
                    new MouseEventMatcher(SWT.NONE, GridRegion.CORNER, MouseEventMatcher.RIGHT_BUTTON),
                    new PopupMenuAction(this.cornerMenu));
        }
    }

}
