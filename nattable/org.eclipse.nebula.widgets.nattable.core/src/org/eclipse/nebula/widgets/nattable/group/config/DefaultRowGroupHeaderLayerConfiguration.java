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
package org.eclipse.nebula.widgets.nattable.group.config;


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.group.action.RowGroupExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectRowGroupsAction;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultRowGroupHeaderLayerConfiguration<T> implements IConfiguration {

	private final IRowGroupModel<T> rowGroupModel;
	
	public DefaultRowGroupHeaderLayerConfiguration(final IRowGroupModel<T> rowGroupModel) {
		this.rowGroupModel = rowGroupModel;
	}
	
	public void configureLayer(ILayer layer) {
		// No op
	}

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER,
				new BeveledBorderDecorator(new RowGroupHeaderTextPainter<T>(rowGroupModel)),
				DisplayMode.NORMAL,
				GridRegion.ROW_GROUP_HEADER
		);
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		
		final IMouseAction action = new SelectRowGroupsAction();
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE), action);
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowGroupHeaderLeftClick(SWT.CTRL), action);
		uiBindingRegistry.registerSingleClickBinding(MouseEventMatcher.rowGroupHeaderLeftClick(SWT.SHIFT), action);
		
		uiBindingRegistry.registerDoubleClickBinding(
				MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE),
				new RowGroupExpandCollapseAction());
	}

}
