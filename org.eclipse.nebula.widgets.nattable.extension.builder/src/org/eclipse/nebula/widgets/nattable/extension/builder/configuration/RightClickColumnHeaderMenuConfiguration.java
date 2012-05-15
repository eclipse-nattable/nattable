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
package org.eclipse.nebula.widgets.nattable.extension.builder.configuration;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;

public class RightClickColumnHeaderMenuConfiguration extends AbstractUiBindingConfiguration {

	private final Menu colHeaderMenu;

	public RightClickColumnHeaderMenuConfiguration(NatTable natTable, TableModel tableModel) {

		PopupMenuBuilder builder = new PopupMenuBuilder(natTable)
			.withHideColumnMenuItem()
			.withShowAllColumnsMenuItem()
			.withAutoResizeSelectedColumnsMenuItem()
			.withColumnStyleEditor()
			.withColumnRenameDialog();

		if(tableModel.enableColumnCategories){
			builder.withSeparator();
			builder.withCategoriesBasedColumnChooser("Select columns");
		}
		if(tableModel.enableColumnGroups){
			builder.withSeparator();
			builder.withColumnChooserMenuItem();
		}

		if(tableModel.enableFilterRow){
			builder.withSeparator();
			builder.withClearAllFilters();
			builder.withToggleFilterRow();
		}

		colHeaderMenu = builder.build();
		addDisposeListeners(natTable);
	}

	private void addDisposeListeners(NatTable natTable) {
		natTable.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				colHeaderMenu.dispose();
			}
		});
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 3),
				new PopupMenuAction(colHeaderMenu));
	}

}
