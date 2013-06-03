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

public class GroupByHeaderMenuConfiguration extends AbstractUiBindingConfiguration {
	
	private final NatTable natTable;
	private final GroupByHeaderLayer groupByHeaderLayer;

	public GroupByHeaderMenuConfiguration(NatTable natTable, GroupByHeaderLayer groupByHeaderLayer) {
		this.natTable = natTable;
		this.groupByHeaderLayer = groupByHeaderLayer;
	}
	
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(
				new MouseEventMatcher(SWT.NONE, GroupByHeaderLayer.GROUP_BY_REGION, MouseEventMatcher.RIGHT_BUTTON) {
					@Override
					public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
						if (super.matches(natTable, event, regionLabels)) {
							int groupByColumnIndex = groupByHeaderLayer.getGroupByColumnIndexAtXY(event.x, event.y);
							return groupByColumnIndex >= 0;
						}
						return false;
					}
				},
				new PopupMenuAction(
						new PopupMenuBuilder(natTable)
						.withMenuItemProvider(
								new IMenuItemProvider() {
									public void addMenuItem(final NatTable natTable, Menu popupMenu) {
										MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
										menuItem.setText(Messages.getString("GroupByHeaderMenuConfiguration.ungroupBy")); //$NON-NLS-1$
										menuItem.setEnabled(true);
				
										menuItem.addSelectionListener(new SelectionAdapter() {
											@Override
											public void widgetSelected(SelectionEvent event) {
												NatEventData natEventData = MenuItemProviders.getNatEventData(event);
												MouseEvent originalEvent = natEventData.getOriginalEvent();
												
												int groupByColumnIndex = groupByHeaderLayer.getGroupByColumnIndexAtXY(
														originalEvent.x, originalEvent.y);
												
												natTable.doCommand(new UngroupByColumnIndexCommand(groupByColumnIndex));
											}
										});
									}
								}
						).build()
				)
		);
	}
	
}
