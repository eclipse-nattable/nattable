/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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
	 * 
	 * <p>As an example you might want to create a PopupMenuBuilder by using a configured menu
	 * with the id <i>org.eclipse.nebula.widgets.nattable.example.contextmenu</i>
	 * <br/><br/>
	 * <code>
	 * ISelectionProvider isp = new RowSelectionProvider<?>(selectionLayer, bodyDataProvider, false);<br/>
	 * MenuManager menuManager = new MenuManager();<br/>
	 * menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));<br/>
	 * getSite().registerContextMenu("org.eclipse.nebula.widgets.nattable.example.contextmenu", menuManager, isp);<br/>
	 * PopupMenuBuilder popupMenu = new PopupMenuBuilder(menuManager.createContextMenu(natTable));<br/>
	 * </code>
	 * 
	 * @param natTable The active NatTable instance which might be needed for creation of 
	 * 			menu items that need the NatTable instance to work.
	 * @param menuId The id of the registered context menu.
	 */
	public PopupMenuBuilder(NatTable natTable, Menu menu) {
		this.natTable = natTable;
		this.popupMenu = menu;
	}

	/**
	 * Use this to add your own item to the popup menu.
	 */
	public PopupMenuBuilder withMenuItemProvider(IMenuItemProvider menuItemProvider){
		menuItemProvider.addMenuItem(natTable, popupMenu);
		return this;
	}

	public PopupMenuBuilder withHideColumnMenuItem() {
		return withMenuItemProvider(MenuItemProviders.hideColumnMenuItemProvider());
	}

	public PopupMenuBuilder withHideColumnMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.hideColumnMenuItemProvider(menuLabel));
	}

	public PopupMenuBuilder withShowAllColumnsMenuItem() {
		return withMenuItemProvider(MenuItemProviders.showAllColumnMenuItemProvider());
	}

	public PopupMenuBuilder withShowAllColumnsMenuItem(String menuLabel) {
		return withMenuItemProvider(MenuItemProviders.showAllColumnMenuItemProvider(menuLabel));
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

	public PopupMenuBuilder withSeparator(){
		return withMenuItemProvider(MenuItemProviders.separatorMenuItemProvider());	}

	public Menu build(){
		return popupMenu;
	}

}

