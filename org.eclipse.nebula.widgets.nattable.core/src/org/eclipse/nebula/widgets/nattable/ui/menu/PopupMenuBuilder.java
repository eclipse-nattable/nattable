/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.widgets.Menu;

/**
 * This class is used to create a context menu.
 */
public class PopupMenuBuilder {

	/**
	 * The active NatTable instance the context menu should be added to.
	 * Needed in advance to be able to add custom menu items that need the
	 * NatTable instance.
	 */
	protected NatTable natTable;
	
	/**
	 * The {@link Menu} that is created with this popup menu builder.
	 */
	protected Menu popupMenu;

	/**
	 * Creates a new {@link Menu} that is only configurable with this instance of
	 * {@link PopupMenuBuilder}.
	 * @param parent The active NatTable instance the context menu should be added to.
	 */
	public PopupMenuBuilder(NatTable parent) {
		this.natTable = parent;
		this.popupMenu = new Menu(parent.getShell());
	}

	/**
	 * Creates a popup menu builder based on the given menu. 
	 * Using this enables the possibility to use configured context menus from plugin.xml
	 * and adding NatTable commands programatically.
	 * <p>
	 * As an example you might want to create a PopupMenuBuilder by using a configured menu
	 * with the id <i>org.eclipse.nebula.widgets.nattable.example.contextmenu</i>
	 * <p>
	 * <pre>
	 * ISelectionProvider isp = new RowSelectionProvider&lt;?&gt;(selectionLayer, bodyDataProvider, false);
	 * MenuManager menuManager = new MenuManager();
	 * menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	 * getSite().registerContextMenu("org.eclipse.nebula.widgets.nattable.example.contextmenu", menuManager, isp);
	 * PopupMenuBuilder popupMenu = new PopupMenuBuilder(menuManager.createContextMenu(natTable));
	 * </pre>
	 * 
	 * @param natTable The active NatTable instance which might be needed for creation of 
	 * 			menu items that need the NatTable instance to work.
	 * @param menu The registered context menu.
	 */
	public PopupMenuBuilder(NatTable natTable, Menu menu) {
		this.natTable = natTable;
		this.popupMenu = menu;
	}

	/**
	 * Adds the menu item provided by the given {@link IMenuItemProvider} to the popup menu.
	 * You can use this to add your own item to the popup menu.
	 * @param menuItemProvider The {@link IMenuItemProvider} that provides the menu item
	 * 			that should be added to the popup menu.
	 */
	public PopupMenuBuilder withMenuItemProvider(IMenuItemProvider menuItemProvider){
		menuItemProvider.addMenuItem(natTable, popupMenu);
		return this;
	}

	/**
	 * Adds the menu item for hiding a column to the popup menu. Uses the default text
	 * localized in NatTable core resource bundles.
	 * @return The {@link PopupMenuBuilder} with the hide column menu item added.
	 * @see MenuItemProviders#hideColumnMenuItemProvider()
	 */
	public PopupMenuBuilder withHideColumnMenuItem() {
		return withMenuItemProvider(MenuItemProviders.hideColumnMenuItemProvider());
	}

	/**
	 * Adds the menu item for hiding a column to the popup menu. Uses the given String
	 * as label for the menu item.
	 * @param menuLabel The label to use for showing the item in the popup menu.
	 * @return The {@link PopupMenuBuilder} with the hide column menu item added.
	 * @see MenuItemProviders#hideColumnMenuItemProvider(String)
	 */
	public PopupMenuBuilder withHideColumnMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.hideColumnMenuItemProvider(menuLabel));
	}

	/**
	 * Adds the menu item for showing all columns to the popup menu. Uses the default text
	 * localized in NatTable core resource bundles.
	 * @return The {@link PopupMenuBuilder} with the show all columns menu item added.
	 * @see MenuItemProviders#showAllColumnsMenuItemProvider()
	 */
	public PopupMenuBuilder withShowAllColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.showAllColumnsMenuItemProvider());
	}

	/**
	 * Adds the menu item for showing all columns to the popup menu. Uses the given String
	 * as label for the menu item.
	 * @param menuLabel The label to use for showing the item in the popup menu.
	 * @return The {@link PopupMenuBuilder} with the show all columns menu item added.
	 * @see MenuItemProviders#showAllColumnsMenuItemProvider(String)
	 */
	public PopupMenuBuilder withShowAllColumnsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.showAllColumnsMenuItemProvider(menuLabel));
	}

	/**
	 * Adds the menu item for hiding a row to the popup menu. Uses the default text
	 * localized in NatTable core resource bundles.
	 * @return The {@link PopupMenuBuilder} with the hide row menu item added.
	 * @see MenuItemProviders#hideRowMenuItemProvider()
	 */
	public PopupMenuBuilder withHideRowMenuItem() {
		return withMenuItemProvider(MenuItemProviders.hideRowMenuItemProvider());
	}

	/**
	 * Adds the menu item for hiding a row to the popup menu. Uses the given String
	 * as label for the menu item.
	 * @param menuLabel The label to use for showing the item in the popup menu.
	 * @return The {@link PopupMenuBuilder} with the hide row menu item added.
	 * @see MenuItemProviders#hideRowMenuItemProvider(String)
	 */
	public PopupMenuBuilder withHideRowMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.hideRowMenuItemProvider(menuLabel));
	}

	/**
	 * Adds the menu item for showing all rows to the popup menu. Uses the default text
	 * localized in NatTable core resource bundles.
	 * @return The {@link PopupMenuBuilder} with the show all rows menu item added.
	 * @see MenuItemProviders#showAllRowsMenuItemProvider()
	 */
	public PopupMenuBuilder withShowAllRowsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.showAllRowsMenuItemProvider());
	}

	/**
	 * Adds the menu item for showing all rows to the popup menu. Uses the given String
	 * as label for the menu item.
	 * @param menuLabel The label to use for showing the item in the popup menu.
	 * @return The {@link PopupMenuBuilder} with the show all rows menu item added.
	 * @see MenuItemProviders#showAllRowsMenuItemProvider(String)
	 */
	public PopupMenuBuilder withShowAllRowsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.showAllRowsMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withAutoResizeSelectedColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.autoResizeColumnMenuItemProvider());
	}

	public PopupMenuBuilder withAutoResizeSelectedColumnsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.autoResizeColumnMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withAutoResizeSelectedRowsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.autoResizeRowMenuItemProvider());
	}

	public PopupMenuBuilder withAutoResizeSelectedRowsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.autoResizeRowMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withColumnChooserMenuItem() {
		return withMenuItemProvider(MenuItemProviders.columnChooserMenuItemProvider());
	}

	public PopupMenuBuilder withColumnChooserMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.columnChooserMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withColumnStyleEditor() {
		return withMenuItemProvider(MenuItemProviders.columnStyleEditorMenuItemProvider());
	}

	public PopupMenuBuilder withColumnStyleEditor(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.columnStyleEditorMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withColumnRenameDialog() {
		return withMenuItemProvider(MenuItemProviders.renameColumnMenuItemProvider());
	}

	public PopupMenuBuilder withColumnRenameDialog(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.renameColumnMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withCreateColumnGroupsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.createColumnGroupMenuItemProvider());
	}

	public PopupMenuBuilder withCreateColumnGroupsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.createColumnGroupMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withUngroupColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.ungroupColumnsMenuItemProvider());
	}
	
	public PopupMenuBuilder withUngroupColumnsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.ungroupColumnsMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withInspectLabelsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.inspectLabelsMenuItemProvider());
	}

	public PopupMenuBuilder withCategoriesBasedColumnChooser() {
		return withMenuItemProvider(MenuItemProviders.categoriesBasedColumnChooserMenuItemProvider());
	}

	public PopupMenuBuilder withCategoriesBasedColumnChooser(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.categoriesBasedColumnChooserMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withClearAllFilters() {
		return withMenuItemProvider(MenuItemProviders.clearAllFiltersMenuItemProvider());
	}

	public PopupMenuBuilder withClearAllFilters(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.clearAllFiltersMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withToggleFilterRow() {
		return withMenuItemProvider(MenuItemProviders.clearToggleFilterRowMenuItemProvider());
	}

	public PopupMenuBuilder withToggleFilterRow(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.clearToggleFilterRowMenuItemProvider(menuLabel));
	}

	/**
	 * Adds the menu item for opening the view management dialog to the popup menu. Uses the default text
	 * localized in NatTable core resource bundles. Uses the given String as label for the menu item.
	 * @return The {@link PopupMenuBuilder} with the menu item added for showing the view 
	 * 			management dialog for managing NatTable states.
	 * @see MenuItemProviders#stateManagerMenuItemProvider()
	 */
	public PopupMenuBuilder withStateManagerMenuItemProvider() {
		return withMenuItemProvider(MenuItemProviders.stateManagerMenuItemProvider());
	}

	/**
	 * Adds the menu item for opening the view management dialog to the popup menu. 
	 * @param menuLabel The label to use for showing the item in the popup menu.
	 * @return The {@link PopupMenuBuilder} with the menu item added for showing the view 
	 * 			management dialog for managing NatTable states.
	 * @see MenuItemProviders#stateManagerMenuItemProvider(String)
	 */
	public PopupMenuBuilder withStateManagerMenuItemProvider(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.stateManagerMenuItemProvider(menuLabel));
	}

	/**
	 * Adds a separator to the popup menu.
	 * @return The {@link PopupMenuBuilder} with an added separator.
	 * @see MenuItemProviders#separatorMenuItemProvider()
	 */
	public PopupMenuBuilder withSeparator() {
		return withMenuItemProvider(MenuItemProviders.separatorMenuItemProvider());	
	}

	/**
	 * Builds and returns the created {@link Menu}.
	 * @return The {@link Menu} that is created by this builder.
	 */
	public Menu build() {
		return popupMenu;
	}

}

