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


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnGroupExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnGroupHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.action.CreateColumnGroupAction;
import org.eclipse.nebula.widgets.nattable.group.action.UngroupColumnsAction;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class ColumnGroupConfiguration extends DefaultColumnGroupHeaderLayerConfiguration {

	private final ColumnGroupModel columnGroupModel;
	private final TableModel tableModel;

	public ColumnGroupConfiguration(ColumnGroupModel columnGroupModel, TableModel tableModel) {
		super(columnGroupModel);
		this.columnGroupModel = columnGroupModel;
		this.tableModel = tableModel;
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// Column Group Header is a part of the Group Header.
		// Register the 'column group header matcher' first so that it gets
		// picked up before the more general 'column header matcher'.
		uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new ColumnGroupHeaderReorderDragMode(columnGroupModel)));

		uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                new ColumnHeaderReorderDragMode(columnGroupModel));

		uiBindingRegistry.registerFirstSingleClickBinding(
				MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
				new ColumnGroupExpandCollapseAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, 'g'), new CreateColumnGroupAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, 'u'), new UngroupColumnsAction());
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		Image bgImage = tableModel.tableStyle.columnHeaderBgImage;

		if (ObjectUtils.isNotNull(bgImage)) {
			TextPainter txtPainter = new TextPainter(false, false);
			ICellPainter cellPainter = new BackgroundImagePainter(txtPainter, bgImage, GUIHelper.getColor(192, 192, 192));

			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER,
					new ColumnGroupHeaderTextPainter(columnGroupModel, cellPainter),
					DisplayMode.NORMAL,
					GridRegion.COLUMN_GROUP_HEADER
			);
		} else {
			super.configureRegistry(configRegistry);
		}

	}

}
