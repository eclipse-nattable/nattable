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
package org.eclipse.nebula.widgets.nattable.tree.config;


import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.SWT;

public class DefaultTreeLayerConfiguration implements IConfiguration {

	private final ITreeRowModel<?> treeRowModel;
	
	private IndentedTreeImagePainter indentedTreeImagePainter;
	private ICellPainter treeImagePainter;
	
	public DefaultTreeLayerConfiguration(ITreeRowModel<?> treeRowModel) {
		this.treeRowModel = treeRowModel;
		
		indentedTreeImagePainter = new IndentedTreeImagePainter(treeRowModel);
		treeImagePainter = indentedTreeImagePainter.getTreeImagePainter();
	}
	
	public void configureLayer(ILayer layer) {
		// No op
	}

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER,
				new BackgroundPainter(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.LEFT, indentedTreeImagePainter)),
				DisplayMode.NORMAL,
				TreeLayer.TREE_COLUMN_CELL
		);
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_STYLE,
				new Style() {{
					setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT,  HorizontalAlignmentEnum.LEFT);
				}},
				DisplayMode.NORMAL,
				TreeLayer.TREE_COLUMN_CELL
		);
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.EXPORT_FORMATTER,
				new TreeExportFormatter(this.treeRowModel),
				DisplayMode.NORMAL,
				TreeLayer.TREE_COLUMN_CELL
		);
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerFirstDoubleClickBinding(
				MouseEventMatcher.bodyLeftClick(SWT.NONE), 
				new TreeExpandCollapseAction()
		);
		
		uiBindingRegistry.registerFirstSingleClickBinding(
				new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, treeImagePainter),
				new TreeExpandCollapseAction()
		);
	}

}
