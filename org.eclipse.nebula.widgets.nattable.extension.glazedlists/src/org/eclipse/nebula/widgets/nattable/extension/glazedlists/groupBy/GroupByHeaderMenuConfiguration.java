/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453219
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.UngroupByColumnIndexCommand;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Configuration for adding a context menu to the groupBy header. By adding this
 * configuration a popup menu is openend to perform an ungroup action when
 * performing a right click on a groupBy indicator.
 */
public class GroupByHeaderMenuConfiguration extends AbstractUiBindingConfiguration {

    public static final String UNGROUP_BY_MENU_ITEM_ID = "ungroupByMenuItem"; //$NON-NLS-1$

    /**
     * The {@link GroupByHeaderLayer} to which the menu should be attached.
     */
    private final GroupByHeaderLayer groupByHeaderLayer;

    /**
     * The groupBy header menu.
     */
    protected Menu groupByHeaderMenu;

    /**
     * Creates the groupBy header menu configuration to add a popup menu to the
     * groupBy header.
     *
     * @param natTable
     *            The current NatTable instance.
     * @param groupByHeaderLayer
     *            The {@link GroupByHeaderLayer} to which the menu should be
     *            attached.
     */
    public GroupByHeaderMenuConfiguration(NatTable natTable, GroupByHeaderLayer groupByHeaderLayer) {

        this.groupByHeaderLayer = groupByHeaderLayer;
        this.groupByHeaderMenu = createGroupByHeaderMenu(natTable).build();
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GroupByHeaderLayer.GROUP_BY_REGION, MouseEventMatcher.RIGHT_BUTTON) {
                    @Override
                    public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
                        if (super.matches(natTable, event, regionLabels)) {
                            int groupByColumnIndex =
                                    GroupByHeaderMenuConfiguration.this.groupByHeaderLayer.getGroupByColumnIndexAtXY(event.x, event.y);
                            return groupByColumnIndex >= 0;
                        }
                        return false;
                    }
                }, new PopupMenuAction(this.groupByHeaderMenu));
    }

    /**
     * Creates the {@link PopupMenuBuilder} for the groupBy header menu with the
     * menu items to ungroup.
     *
     * @param natTable
     *            The NatTable where the menu should be attached.
     * @return The {@link PopupMenuBuilder} that is used to build the groupBy
     *         header menu.
     */
    protected PopupMenuBuilder createGroupByHeaderMenu(NatTable natTable) {
        return new PopupMenuBuilder(natTable).withMenuItemProvider(UNGROUP_BY_MENU_ITEM_ID,
                new IMenuItemProvider() {
                    @Override
                    public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                        MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                        menuItem.setText(Messages.getLocalizedMessage("%GroupByHeaderMenuConfiguration.ungroupBy")); //$NON-NLS-1$
                        menuItem.setEnabled(true);

                        menuItem.addSelectionListener(new SelectionAdapter() {
                            @Override
                            public void widgetSelected(SelectionEvent event) {
                                NatEventData natEventData = MenuItemProviders.getNatEventData(event);
                                MouseEvent originalEvent = natEventData.getOriginalEvent();

                                int groupByColumnIndex =
                                        GroupByHeaderMenuConfiguration.this.groupByHeaderLayer.getGroupByColumnIndexAtXY(
                                                originalEvent.x,
                                                originalEvent.y);

                                natTable.doCommand(new UngroupByColumnIndexCommand(groupByColumnIndex));
                            }
                        });
                    }
                });
    }
}
