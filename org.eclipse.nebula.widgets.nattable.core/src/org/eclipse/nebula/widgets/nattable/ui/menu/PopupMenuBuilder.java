/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 451490, 453219
 *     Roman Flueckiger <roman.flueckiger@mac.com> - Bug 451490
 *     Thanh Liem PHAN (ALL4TEC) <thanhliem.phan@all4tec.net> - Bug 509361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.command.ExportTableCommandHandler;
import org.eclipse.nebula.widgets.nattable.export.image.ImageExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This class is used to create a context menu.
 */
public class PopupMenuBuilder {

    public static final String HIDE_COLUMN_MENU_ITEM_ID = "hideColumnMenuItem"; //$NON-NLS-1$
    public static final String SHOW_ALL_COLUMNS_MENU_ITEM_ID = "showAllColumnsMenuItem"; //$NON-NLS-1$
    public static final String HIDE_ROW_MENU_ITEM_ID = "hideRowMenuItem"; //$NON-NLS-1$
    public static final String SHOW_ALL_ROWS_MENU_ITEM_ID = "showAllRowsMenuItem"; //$NON-NLS-1$
    public static final String AUTO_RESIZE_COLUMN_MENU_ITEM_ID = "autoResizeColumnMenuItem"; //$NON-NLS-1$
    public static final String AUTO_RESIZE_ROW_MENU_ITEM_ID = "autoResizeRowMenuItem"; //$NON-NLS-1$
    public static final String AUTO_RESIZE_ALL_SELECTED_COLUMN_MENU_ITEM_ID = "autoResizeAllSelectedColumnMenuItem"; //$NON-NLS-1$
    public static final String COLUMN_CHOOSER_MENU_ITEM_ID = "columnChooserMenuItem"; //$NON-NLS-1$
    public static final String COLUMN_STYLE_EDITOR_MENU_ITEM_ID = "columnStyleEditorMenuItem"; //$NON-NLS-1$
    public static final String COLUMN_RENAME_MENU_ITEM_ID = "columnRenameMenuItem"; //$NON-NLS-1$
    public static final String CREATE_COLUMN_GROUP_MENU_ITEM_ID = "createColumnGroupMenuItem"; //$NON-NLS-1$
    public static final String RENAME_COLUMN_GROUP_MENU_ITEM_ID = "renameColumnGroupMenuItem"; //$NON-NLS-1$
    public static final String REMOVE_COLUMN_GROUP_MENU_ITEM_ID = "removeColumnGroupMenuItem"; //$NON-NLS-1$
    public static final String UNGROUP_COLUMNS_MENU_ITEM_ID = "ungroupColumnsMenuItem"; //$NON-NLS-1$
    public static final String INSPECT_LABEL_MENU_ITEM_ID = "inspectLabelMenuItem"; //$NON-NLS-1$
    public static final String CATEGORIES_BASED_COLUMN_CHOOSER_MENU_ITEM_ID = "categoriesBasedColumnChooserMenuItem"; //$NON-NLS-1$
    public static final String CLEAR_ALL_FILTERS_MENU_ITEM_ID = "clearAllFiltersMenuItem"; //$NON-NLS-1$
    public static final String TOGGLE_FILTER_ROW_MENU_ITEM_ID = "toggleFilterRowMenuItem"; //$NON-NLS-1$
    public static final String STATE_MANAGER_MENU_ITEM_ID = "stateManagerMenuItem"; //$NON-NLS-1$
    public static final String SEPARATOR_MENU_ITEM_ID = "separatorMenuItem"; //$NON-NLS-1$
    /**
     * @since 1.5
     */
    public static final String EXPORT_IMAGE_MENU_ITEM_ID = "exportImageMenuItem"; //$NON-NLS-1$

    /**
     * The active NatTable instance the context menu should be added to. Needed
     * in advance to be able to add custom menu items that need the NatTable
     * instance.
     */
    protected NatTable natTable;

    /**
     * The {@link Menu} that is created with this popup menu builder.
     */
    protected Menu popupMenu;

    /**
     * The {@link MenuManager} that is used by this popup menu builder. Can be
     * <code>null</code> if plain SWT menu mechanisms are used.
     */
    protected MenuManager menuManager;

    /**
     * Collection of all registered visibility state checkers for configured
     * id's.
     */
    protected final MenuItemStateMap visibility = new MenuItemStateMap();

    /**
     * Collection of all registered enablement state checkers for configured
     * id's.
     */
    protected final MenuItemStateMap enablement = new MenuItemStateMap();

    /**
     * Creates {@link PopupMenuBuilder} that builds up a new {@link Menu} that
     * is only configurable with this instance of {@link PopupMenuBuilder}. Uses
     * a {@link MenuManager} internally to be able to configure visibility and
     * enabled states.
     *
     * @param parent
     *            The active NatTable instance the context menu should be added
     *            to.
     */
    public PopupMenuBuilder(NatTable parent) {
        this(parent, new MenuManager());
    }

    /**
     * Creates a {@link PopupMenuBuilder} that builds up a new {@link Menu}
     * using the given {@link MenuManager}.
     *
     * @param parent
     *            The active NatTable instance the context menu should be added
     *            to.
     * @param manager
     *            The {@link MenuManager} that should be used to create the
     *            {@link Menu}.
     */
    public PopupMenuBuilder(NatTable parent, MenuManager manager) {
        this.natTable = parent;
        this.menuManager = manager;
        this.popupMenu = manager.createContextMenu(this.natTable);
    }

    /**
     * Creates a popup menu builder based on the given menu. Using this enables
     * the possibility to use configured context menus from plugin.xml and
     * adding NatTable commands programmatically.
     * <p>
     * As an example you might want to create a PopupMenuBuilder by using a
     * configured menu with the id
     * <i>org.eclipse.nebula.widgets.nattable.example.contextmenu</i>
     * <p>
     *
     * <pre>
     * ISelectionProvider isp =
     *         new RowSelectionProvider&lt;?&gt;(selectionLayer, bodyDataProvider, false);
     * MenuManager menuManager = new MenuManager();
     * menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
     * getSite().registerContextMenu(
     *         &quot;org.eclipse.nebula.widgets.nattable.example.contextmenu&quot;, menuManager, isp);
     * PopupMenuBuilder popupMenu =
     *         new PopupMenuBuilder(menuManager.createContextMenu(natTable));
     * </pre>
     * <p>
     * For usage with Eclipse 4 you can use the <code>EMenuService</code> to
     * register the menu to a NatTable instance. Afterwards get the menu and
     * remove it from the NatTable to avoid the SWT control menu. The generated
     * menu carries a {@link MenuManager} in the {@link Menu#getData()} which
     * will be used within this {@link PopupMenuBuilder}.
     * </p>
     *
     * <pre>
     * menuService.registerContextMenu(natTable, menuId);
     * Menu swtMenu = natTable.getMenu();
     * natTable.setMenu(null);
     * </pre>
     *
     * @param natTable
     *            The active NatTable instance which might be needed for
     *            creation of menu items that need the NatTable instance to
     *            work.
     * @param menu
     *            The registered context menu.
     */
    public PopupMenuBuilder(NatTable natTable, Menu menu) {
        this.natTable = natTable;
        this.popupMenu = menu;

        // if the menu is build up using a MenuManager, remember that for
        // further use
        Object mgr = menu.getData("org.eclipse.jface.action.MenuManager.managerKey"); //$NON-NLS-1$
        if (mgr != null && mgr instanceof MenuManager) {
            this.menuManager = (MenuManager) mgr;
        }
    }

    /**
     * Adds the menu item provided by the given {@link IMenuItemProvider} to the
     * popup menu. You can use this to add your own item to the popup menu.
     * <p>
     * Items added by this method can not be identified by id, so adding visible
     * or enabled state checkers is not possible for these providers.
     * </p>
     *
     * @param menuItemProvider
     *            The {@link IMenuItemProvider} that provides the menu item that
     *            should be added to the popup menu.
     * @return The current {@link PopupMenuBuilder} with the added item.
     */
    public PopupMenuBuilder withMenuItemProvider(IMenuItemProvider menuItemProvider) {
        if (this.menuManager == null) {
            menuItemProvider.addMenuItem(this.natTable, this.popupMenu);
        } else {
            this.menuManager.add(new PopupContributionItem(menuItemProvider));
        }
        return this;
    }

    /**
     * Adds the menu item provided by the given {@link IMenuItemProvider} to the
     * popup menu. You can use this to add your own item to the popup menu.
     * <p>
     * As items added by this method can be identified via the given id it is
     * possible to register visible or enabled state checkers for these
     * providers.
     * </p>
     *
     * @param id
     *            The id under which the given {@link IMenuItemProvider} should
     *            be identifiable.
     * @param menuItemProvider
     *            The {@link IMenuItemProvider} that provides the menu item that
     *            should be added to the popup menu.
     * @return The current {@link PopupMenuBuilder} with the added item.
     */
    public PopupMenuBuilder withMenuItemProvider(String id, IMenuItemProvider menuItemProvider) {
        if (this.menuManager == null) {
            menuItemProvider.addMenuItem(this.natTable, this.popupMenu);
        } else {
            this.menuManager.add(new PopupContributionItem(id, menuItemProvider));
        }
        return this;
    }

    /**
     * Adds the menu item(s) provided by the given {@link ContributionItem} to
     * the popup menu. You can use this to add your own item to the popup menu.
     * <p>
     * This method is only working if the {@link PopupMenuBuilder} is using a
     * {@link MenuManager}.
     * </p>
     * <p>
     * Using this adds support for visibility and enabled states of menu items.
     * </p>
     *
     * @param contributionItem
     *            The {@link ContributionItem} that is used to add a menu item
     *            to the menu.
     * @return The current {@link PopupMenuBuilder} with the added item.
     * @throws IllegalStateException
     *             if this {@link PopupMenuBuilder} does not use a
     *             {@link MenuManager}
     */
    public PopupMenuBuilder withContributionItem(ContributionItem contributionItem) {
        if (this.menuManager == null) {
            throw new IllegalStateException("This PopupMenuBuilder is not created using a MenuManager, " //$NON-NLS-1$
                    + "therefore ContributionItems can not be added"); //$NON-NLS-1$
        } else {
            this.menuManager.add(contributionItem);
        }
        return this;
    }

    /**
     * Adds the menu item for hiding a column to the popup menu. Uses the
     * default text localized in NatTable core resource bundles.
     *
     * @return The {@link PopupMenuBuilder} with the hide column menu item
     *         added.
     * @see MenuItemProviders#hideColumnMenuItemProvider()
     */
    public PopupMenuBuilder withHideColumnMenuItem() {
        return withMenuItemProvider(
                HIDE_COLUMN_MENU_ITEM_ID,
                MenuItemProviders.hideColumnMenuItemProvider());
    }

    /**
     * Adds the menu item for hiding a column to the popup menu. Uses the given
     * String as label for the menu item.
     *
     * @param menuLabel
     *            The label to use for showing the item in the popup menu.
     * @return The {@link PopupMenuBuilder} with the hide column menu item
     *         added.
     * @see MenuItemProviders#hideColumnMenuItemProvider(String)
     */
    public PopupMenuBuilder withHideColumnMenuItem(String menuLabel) {
        return withMenuItemProvider(
                HIDE_COLUMN_MENU_ITEM_ID,
                MenuItemProviders.hideColumnMenuItemProvider(menuLabel));
    }

    /**
     * Adds the menu item for showing all columns to the popup menu. Uses the
     * default text localized in NatTable core resource bundles.
     *
     * @return The {@link PopupMenuBuilder} with the show all columns menu item
     *         added.
     * @see MenuItemProviders#showAllColumnsMenuItemProvider()
     */
    public PopupMenuBuilder withShowAllColumnsMenuItem() {
        return withMenuItemProvider(
                SHOW_ALL_COLUMNS_MENU_ITEM_ID,
                MenuItemProviders.showAllColumnsMenuItemProvider());
    }

    /**
     * Adds the menu item for showing all columns to the popup menu. Uses the
     * given String as label for the menu item.
     *
     * @param menuLabel
     *            The label to use for showing the item in the popup menu.
     * @return The {@link PopupMenuBuilder} with the show all columns menu item
     *         added.
     * @see MenuItemProviders#showAllColumnsMenuItemProvider(String)
     */
    public PopupMenuBuilder withShowAllColumnsMenuItem(String menuLabel) {
        return withMenuItemProvider(
                SHOW_ALL_COLUMNS_MENU_ITEM_ID,
                MenuItemProviders.showAllColumnsMenuItemProvider(menuLabel));
    }

    /**
     * Add the menu item for exporting to image to the popup menu. Use the
     * default text localized in NatTable core resource bundles.
     *
     * <p>
     * <b>IMPORTANT:</b> the {@link ImageExporter} needs to be configured for
     * the configuration attribute {@link ExportConfigAttributes#TABLE_EXPORTER}
     * to really export to an image. Also the {@link ExportTableCommandHandler}
     * needs to be registered on an {@link ILayer} in the layer stack, e.g. the
     * GridLayer.
     * </p>
     *
     * @return The {@link PopupMenuBuilder} with the export to image menu item
     *         added.
     * @see MenuItemProviders#exportToImageMenuItemProvider()
     * @since 1.5
     */
    public PopupMenuBuilder withExportToImageMenuItem() {
        return withMenuItemProvider(
                EXPORT_IMAGE_MENU_ITEM_ID,
                MenuItemProviders.exportToImageMenuItemProvider());
    }

    /**
     * Add the menu item for exporting to image to the popup menu. Use the given
     * String as label for the menu item.
     *
     * <p>
     * <b>IMPORTANT:</b> the {@link ImageExporter} needs to be configured for
     * the configuration attribute {@link ExportConfigAttributes#TABLE_EXPORTER}
     * to really export to an image. Also the {@link ExportTableCommandHandler}
     * needs to be registered on an {@link ILayer} in the layer stack, e.g. the
     * GridLayer.
     * </p>
     *
     * @param menuLabel
     *            The label to use for exporting to image in the popup menu.
     * @return The {@link PopupMenuBuilder} with the exporting to image menu
     *         item added.
     * @see MenuItemProviders#exportToImageMenuItemProvider(String)
     * @since 1.5
     */
    public PopupMenuBuilder withExportToImageMenuItem(String menuLabel) {
        return withMenuItemProvider(
                EXPORT_IMAGE_MENU_ITEM_ID,
                MenuItemProviders.exportToImageMenuItemProvider(menuLabel));
    }

    /**
     * Adds the menu item for hiding a row to the popup menu. Uses the default
     * text localized in NatTable core resource bundles.
     *
     * @return The {@link PopupMenuBuilder} with the hide row menu item added.
     * @see MenuItemProviders#hideRowMenuItemProvider()
     */
    public PopupMenuBuilder withHideRowMenuItem() {
        return withMenuItemProvider(
                HIDE_ROW_MENU_ITEM_ID,
                MenuItemProviders.hideRowMenuItemProvider());
    }

    /**
     * Adds the menu item for hiding a row to the popup menu. Uses the given
     * String as label for the menu item.
     *
     * @param menuLabel
     *            The label to use for showing the item in the popup menu.
     * @return The {@link PopupMenuBuilder} with the hide row menu item added.
     * @see MenuItemProviders#hideRowMenuItemProvider(String)
     */
    public PopupMenuBuilder withHideRowMenuItem(String menuLabel) {
        return withMenuItemProvider(
                HIDE_ROW_MENU_ITEM_ID,
                MenuItemProviders.hideRowMenuItemProvider(menuLabel));
    }

    /**
     * Adds the menu item for showing all rows to the popup menu. Uses the
     * default text localized in NatTable core resource bundles.
     *
     * @return The {@link PopupMenuBuilder} with the show all rows menu item
     *         added.
     * @see MenuItemProviders#showAllRowsMenuItemProvider()
     */
    public PopupMenuBuilder withShowAllRowsMenuItem() {
        return withMenuItemProvider(
                SHOW_ALL_ROWS_MENU_ITEM_ID,
                MenuItemProviders.showAllRowsMenuItemProvider());
    }

    /**
     * Adds the menu item for showing all rows to the popup menu. Uses the given
     * String as label for the menu item.
     *
     * @param menuLabel
     *            The label to use for showing the item in the popup menu.
     * @return The {@link PopupMenuBuilder} with the show all rows menu item
     *         added.
     * @see MenuItemProviders#showAllRowsMenuItemProvider(String)
     */
    public PopupMenuBuilder withShowAllRowsMenuItem(String menuLabel) {
        return withMenuItemProvider(
                SHOW_ALL_ROWS_MENU_ITEM_ID,
                MenuItemProviders.showAllRowsMenuItemProvider(menuLabel));
    }

    /**
     * Adds the menu item for auto resizing selected columns to the popup menu.
     * Uses the default text localized in NatTable core resource bundles.
     *
     * @return The {@link PopupMenuBuilder} with the resize selected columns
     *         menu item added.
     * @see MenuItemProviders#autoResizeColumnMenuItemProvider()
     */
    public PopupMenuBuilder withAutoResizeSelectedColumnsMenuItem() {
        return withMenuItemProvider(
                AUTO_RESIZE_COLUMN_MENU_ITEM_ID,
                MenuItemProviders.autoResizeColumnMenuItemProvider());
    }

    /**
     * Adds the menu item for auto resizing selected columns to the popup menu.
     * Uses the given String as label for the menu item.
     *
     * @return The {@link PopupMenuBuilder} with the resize selected columns
     *         menu item added.
     * @see MenuItemProviders#autoResizeColumnMenuItemProvider(String)
     */
    public PopupMenuBuilder withAutoResizeSelectedColumnsMenuItem(String menuLabel) {
        return withMenuItemProvider(
                AUTO_RESIZE_COLUMN_MENU_ITEM_ID,
                MenuItemProviders.autoResizeColumnMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withAutoResizeSelectedRowsMenuItem() {
        return withMenuItemProvider(
                AUTO_RESIZE_ROW_MENU_ITEM_ID,
                MenuItemProviders.autoResizeRowMenuItemProvider());
    }

    public PopupMenuBuilder withAutoResizeSelectedRowsMenuItem(String menuLabel) {
        return withMenuItemProvider(
                AUTO_RESIZE_ROW_MENU_ITEM_ID,
                MenuItemProviders.autoResizeRowMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withColumnChooserMenuItem() {
        return withMenuItemProvider(
                COLUMN_CHOOSER_MENU_ITEM_ID,
                MenuItemProviders.columnChooserMenuItemProvider());
    }

    public PopupMenuBuilder withColumnChooserMenuItem(String menuLabel) {
        return withMenuItemProvider(
                COLUMN_CHOOSER_MENU_ITEM_ID,
                MenuItemProviders.columnChooserMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withColumnStyleEditor() {
        return withMenuItemProvider(
                COLUMN_STYLE_EDITOR_MENU_ITEM_ID,
                MenuItemProviders.columnStyleEditorMenuItemProvider());
    }

    public PopupMenuBuilder withColumnStyleEditor(String menuLabel) {
        return withMenuItemProvider(
                COLUMN_STYLE_EDITOR_MENU_ITEM_ID,
                MenuItemProviders.columnStyleEditorMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withColumnRenameDialog() {
        return withMenuItemProvider(
                COLUMN_RENAME_MENU_ITEM_ID,
                MenuItemProviders.renameColumnMenuItemProvider());
    }

    public PopupMenuBuilder withColumnRenameDialog(String menuLabel) {
        return withMenuItemProvider(
                COLUMN_RENAME_MENU_ITEM_ID,
                MenuItemProviders.renameColumnMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withCreateColumnGroupsMenuItem() {
        return withMenuItemProvider(
                CREATE_COLUMN_GROUP_MENU_ITEM_ID,
                MenuItemProviders.createColumnGroupMenuItemProvider());
    }

    public PopupMenuBuilder withCreateColumnGroupsMenuItem(String menuLabel) {
        return withMenuItemProvider(
                CREATE_COLUMN_GROUP_MENU_ITEM_ID,
                MenuItemProviders.createColumnGroupMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withUngroupColumnsMenuItem() {
        return withMenuItemProvider(
                UNGROUP_COLUMNS_MENU_ITEM_ID,
                MenuItemProviders.ungroupColumnsMenuItemProvider());
    }

    public PopupMenuBuilder withUngroupColumnsMenuItem(String menuLabel) {
        return withMenuItemProvider(
                UNGROUP_COLUMNS_MENU_ITEM_ID,
                MenuItemProviders.ungroupColumnsMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withRenameColumnGroupMenuItem() {
        return withMenuItemProvider(
                RENAME_COLUMN_GROUP_MENU_ITEM_ID,
                MenuItemProviders.renameColumnGroupMenuItemProvider());
    }

    public PopupMenuBuilder withRenameColumnGroupMenuItem(String menuLabel) {
        return withMenuItemProvider(
                RENAME_COLUMN_GROUP_MENU_ITEM_ID,
                MenuItemProviders.renameColumnGroupMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withRemoveColumnGroupMenuItem() {
        return withMenuItemProvider(
                REMOVE_COLUMN_GROUP_MENU_ITEM_ID,
                MenuItemProviders.removeColumnGroupMenuItemProvider());
    }

    public PopupMenuBuilder withRemoveColumnGroupMenuItem(String menuLabel) {
        return withMenuItemProvider(
                REMOVE_COLUMN_GROUP_MENU_ITEM_ID,
                MenuItemProviders.removeColumnGroupMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withInspectLabelsMenuItem() {
        return withMenuItemProvider(
                INSPECT_LABEL_MENU_ITEM_ID,
                MenuItemProviders.inspectLabelsMenuItemProvider());
    }

    public PopupMenuBuilder withCategoriesBasedColumnChooser() {
        return withMenuItemProvider(
                CATEGORIES_BASED_COLUMN_CHOOSER_MENU_ITEM_ID,
                MenuItemProviders.categoriesBasedColumnChooserMenuItemProvider());
    }

    public PopupMenuBuilder withCategoriesBasedColumnChooser(String menuLabel) {
        return withMenuItemProvider(
                CATEGORIES_BASED_COLUMN_CHOOSER_MENU_ITEM_ID,
                MenuItemProviders.categoriesBasedColumnChooserMenuItemProvider(menuLabel));
    }

    public PopupMenuBuilder withClearAllFilters() {
        return withMenuItemProvider(
                CLEAR_ALL_FILTERS_MENU_ITEM_ID,
                MenuItemProviders.clearAllFiltersMenuItemProvider());
    }

    public PopupMenuBuilder withClearAllFilters(String menuLabel) {
        return withMenuItemProvider(
                CLEAR_ALL_FILTERS_MENU_ITEM_ID,
                MenuItemProviders.clearAllFiltersMenuItemProvider(menuLabel));
    }

    /**
     * Adds a menu item for toggling the visibility of the filter row. Uses the
     * default text localized in NatTable core resource bundles.
     *
     * @return The {@link PopupMenuBuilder} with the toggle filter row menu item
     *         added.
     * @see MenuItemProviders#clearToggleFilterRowMenuItemProvider()
     */
    public PopupMenuBuilder withToggleFilterRow() {
        return withMenuItemProvider(
                TOGGLE_FILTER_ROW_MENU_ITEM_ID,
                MenuItemProviders.clearToggleFilterRowMenuItemProvider());
    }

    /**
     * Adds a menu item for toggling the visibility of the filter row. Uses the
     * given String as label for the menu item.
     *
     * @param menuLabel
     *            The label to use for showing the item in the popup menu.
     * @return The {@link PopupMenuBuilder} with the toggle filter row menu item
     *         added.
     * @see MenuItemProviders#clearToggleFilterRowMenuItemProvider(String)
     */
    public PopupMenuBuilder withToggleFilterRow(String menuLabel) {
        return withMenuItemProvider(
                TOGGLE_FILTER_ROW_MENU_ITEM_ID,
                MenuItemProviders.clearToggleFilterRowMenuItemProvider(menuLabel));
    }

    /**
     * Adds the menu item for opening the view management dialog to the popup
     * menu. Uses the default text localized in NatTable core resource bundles.
     * Uses the given String as label for the menu item.
     *
     * @return The {@link PopupMenuBuilder} with the menu item added for showing
     *         the view management dialog for managing NatTable states.
     * @see MenuItemProviders#stateManagerMenuItemProvider()
     */
    public PopupMenuBuilder withStateManagerMenuItemProvider() {
        return withMenuItemProvider(
                STATE_MANAGER_MENU_ITEM_ID,
                MenuItemProviders.stateManagerMenuItemProvider());
    }

    /**
     * Adds the menu item for opening the view management dialog to the popup
     * menu.
     *
     * @param menuLabel
     *            The label to use for showing the item in the popup menu.
     * @return The {@link PopupMenuBuilder} with the menu item added for showing
     *         the view management dialog for managing NatTable states.
     * @see MenuItemProviders#stateManagerMenuItemProvider(String)
     */
    public PopupMenuBuilder withStateManagerMenuItemProvider(String menuLabel) {
        return withMenuItemProvider(
                STATE_MANAGER_MENU_ITEM_ID,
                MenuItemProviders.stateManagerMenuItemProvider(menuLabel));
    }

    /**
     * Adds a separator to the popup menu.
     *
     * @return The {@link PopupMenuBuilder} with an added separator.
     * @see MenuItemProviders#separatorMenuItemProvider()
     */
    public PopupMenuBuilder withSeparator() {
        int count = 0;
        if (this.menuManager != null) {
            for (IContributionItem item : this.menuManager.getItems()) {
                if (item.getId() != null && item.getId().startsWith(SEPARATOR_MENU_ITEM_ID)) {
                    count++;
                }
            }
        }
        return withSeparator(SEPARATOR_MENU_ITEM_ID + "." + count); //$NON-NLS-1$
    }

    /**
     * Adds a separator to the popup menu with the given id.
     *
     * @param id
     *            The id to identify the separator. Necessary if there should be
     *            visibility constraints for specific separators.
     * @return The {@link PopupMenuBuilder} with an added separator.
     * @see MenuItemProviders#separatorMenuItemProvider()
     */
    public PopupMenuBuilder withSeparator(String id) {
        return withMenuItemProvider(id, MenuItemProviders.separatorMenuItemProvider());
    }

    /**
     * Builds and returns the created {@link Menu}.
     * <p>
     * <b>Note:</b> Calling this method will also add a {@link DisposeListener}
     * to the NatTable instance to ensure the created {@link Menu} is disposed
     * when the NatTable itself gets disposed.
     * </p>
     *
     * @return The {@link Menu} that is created by this builder.
     */
    public Menu build() {

        this.natTable.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (PopupMenuBuilder.this.popupMenu != null
                        && !PopupMenuBuilder.this.popupMenu.isDisposed())
                    PopupMenuBuilder.this.popupMenu.dispose();
            }
        });

        return this.popupMenu;
    }

    /**
     * Associate a visibility {@link IMenuItemState} with the menu item
     * identified by the given id.
     * <p>
     * The visibility state is handled by the internal {@link MenuManager}. If
     * no {@link MenuManager} is used, this method will have not effect.
     * </p>
     * <p>
     * For the item to be visible, all associated {@link IMenuItemState} must be
     * active OR no {@link IMenuItemState} must be associated with the item.
     * </p>
     *
     * @param id
     *            the registered {@link IMenuItemState} will affect the menu
     *            item identified by the given id.
     * @param state
     *            the {@link IMenuItemState} to queried for the visibility state
     *            of the menu item with the given id.
     * @return This {@link PopupMenuBuilder} with the visible state checker for
     *         the given id.
     */
    public PopupMenuBuilder withVisibleState(String id, IMenuItemState state) {
        this.visibility.addMenuItemState(id, state);
        return this;
    }

    /**
     * Associate a enabled {@link IMenuItemState} with the menu item identified
     * by the given id.
     * <p>
     * The enabled state is handled by the internal {@link MenuManager}. If no
     * {@link MenuManager} is used, this method will have not effect.
     * </p>
     * <p>
     * For the item to be enabled, all associated {@link IMenuItemState} must be
     * active OR no {@link IMenuItemState} must be associated with the item.
     * </p>
     *
     * @param id
     *            the registered {@link IMenuItemState} will affect the menu
     *            item identified by the given id.
     * @param state
     *            the {@link IMenuItemState} to queried for the enabled state of
     *            the menu item with the given id.
     * @return This {@link PopupMenuBuilder} with the enabled state checker for
     *         the given id.
     */
    public PopupMenuBuilder withEnabledState(String id, IMenuItemState state) {
        this.enablement.addMenuItemState(id, state);
        return this;
    }

    /**
     * Wrapper class to build up a {@link ContributionItem} based on a given
     * {@link IMenuItemProvider}. If an id is set it is possible to register
     * state checkers for enabled and visible state.
     */
    protected class PopupContributionItem extends ContributionItem {

        private IMenuItemProvider provider;

        public PopupContributionItem(IMenuItemProvider provider) {
            this(null, provider);
        }

        public PopupContributionItem(String id, IMenuItemProvider provider) {
            super(id);
            this.provider = provider;
        }

        @Override
        public void fill(Menu menu, int index) {
            List<MenuItem> beforeItems = Arrays.asList(menu.getItems());
            this.provider.addMenuItem(PopupMenuBuilder.this.natTable, menu);
            MenuItem[] afterItems = menu.getItems();

            for (MenuItem item : afterItems) {
                if (!beforeItems.contains(item)) {
                    // isEnabled() seems to be not called by the framework on
                    // opening a menu therefore we set it ourself. For this we
                    // also need to ensure isDynamic() returns true for
                    // re-rendering.
                    item.setEnabled(isEnabled());
                }
            }
        }

        @Override
        public boolean isDynamic() {
            return (getId() != null);
        }

        @Override
        public boolean isEnabled() {
            if (getId() != null) {
                Object eventData = PopupMenuBuilder.this.popupMenu.getData(MenuItemProviders.NAT_EVENT_DATA_KEY);
                if (eventData != null && eventData instanceof NatEventData) {
                    return PopupMenuBuilder.this.enablement.isActive(getId(), (NatEventData) eventData);
                }
            }
            return true;
        }

        @Override
        public boolean isVisible() {
            if (getId() != null) {
                Object eventData = PopupMenuBuilder.this.popupMenu.getData(MenuItemProviders.NAT_EVENT_DATA_KEY);
                if (eventData != null && eventData instanceof NatEventData) {
                    return PopupMenuBuilder.this.visibility.isActive(getId(), (NatEventData) eventData);
                }
            }
            return true;
        }

    }
}
