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
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;

public class HeaderMenuConfiguration extends AbstractUiBindingConfiguration {

	private Menu colHeaderMenu;
	private Menu rowHeaderMenu;
	public enum MenuOptions {HideColumn, ShowColumn};

	public HeaderMenuConfiguration(NatTable natTable) {
		colHeaderMenu = createColumnHeaderMenu(natTable).build();
		rowHeaderMenu = createRowHeaderMenu(natTable).build();

		natTable.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				colHeaderMenu.dispose();
				rowHeaderMenu.dispose();
			}

		});
	}

	protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
		return new PopupMenuBuilder(natTable)
								.withHideColumnMenuItem()
								.withShowAllColumnsMenuItem()
								.withCreateColumnGroupsMenuItem()
								.withUngroupColumnsMenuItem()
								.withAutoResizeSelectedColumnsMenuItem()
								.withColumnStyleEditor()
								.withColumnRenameDialog()
								.withClearAllFilters();
	}

	protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
		return new PopupMenuBuilder(natTable)
								.withAutoResizeSelectedRowsMenuItem();
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, MouseEventMatcher.RIGHT_BUTTON),
				new PopupMenuAction(colHeaderMenu));

		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, MouseEventMatcher.RIGHT_BUTTON),
				new PopupMenuAction(rowHeaderMenu));
	}

}
