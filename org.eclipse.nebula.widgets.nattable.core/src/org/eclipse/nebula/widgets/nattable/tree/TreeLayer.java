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
package org.eclipse.nebula.widgets.nattable.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractRowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeExportFormatter;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.SWT;


public class TreeLayer extends AbstractRowHideShowLayer {

	public static final String TREE_COLUMN_CELL = "TREE_COLUMN_CELL"; //$NON-NLS-1$

	public static final int TREE_COLUMN_NUMBER = 0;

	private final ITreeRowModel<?> treeRowModel;

	private final Set<Integer> hiddenRowIndexes;

	private IndentedTreeImagePainter indentedTreeImagePainter;
	private ICellPainter treeImagePainter;
	
	public TreeLayer(IUniqueIndexLayer underlyingLayer, ITreeRowModel<?> treeRowModel) {
		super(underlyingLayer);
		this.treeRowModel = treeRowModel;

		this.hiddenRowIndexes = new TreeSet<Integer>();

		indentedTreeImagePainter = new IndentedTreeImagePainter(treeRowModel);
		treeImagePainter = indentedTreeImagePainter.getTreeImagePainter();

		registerCommandHandler(new TreeExpandCollapseCommandHandler(this));
	
	}

	@Override
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
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
		
		uiBindingRegistry.registerFirstDoubleClickBinding(
				MouseEventMatcher.bodyLeftClick(SWT.NONE), 
				new TreeExpandCollapseAction()
		);
		
		uiBindingRegistry.registerFirstSingleClickBinding(
				new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, treeImagePainter),
				new TreeExpandCollapseAction()
		);
	}
	
	public ITreeRowModel<?> getModel() {
		return this.treeRowModel;
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack stack = super.getConfigLabelsByPosition(columnPosition, rowPosition);
		if (columnPosition == TREE_COLUMN_NUMBER ) {
			stack.addLabelOnTop(TREE_COLUMN_CELL);
		}
		return stack;
	}

	@Override
	public LabelStack getRegionLabelsByXY(int x, int y) {
		LabelStack stack = super.getRegionLabelsByXY(x, y);
		if (TREE_COLUMN_NUMBER == getColumnPositionByX(x) ) {
			stack.addLabelOnTop(TREE_COLUMN_CELL);
		}

		return stack;
	}
	
	@Override
	public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		ICellPainter cellPainter = super.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
		
		if (columnPosition == TREE_COLUMN_NUMBER) {
			cellPainter = new BackgroundPainter(new CellPainterDecorator(cellPainter, CellEdgeEnum.LEFT, indentedTreeImagePainter));
		}
		
		return cellPainter;
	}

	@Override
	public boolean isRowIndexHidden(int rowIndex) {
		return this.hiddenRowIndexes.contains(Integer.valueOf(rowIndex));
	}

	@Override
	public Collection<Integer> getHiddenRowIndexes() {
		return this.hiddenRowIndexes;
	}

	public void expandOrCollapseIndex(int parentIndex) {
		if (this.treeRowModel.isCollapsed(parentIndex)) {
			expandTreeRow(parentIndex);
		} else {
			collapseTreeRow(parentIndex);
		}
	}

	public void collapseTreeRow(int parentIndex) {
		List<Integer> rowIndexes = this.treeRowModel.collapse(parentIndex);
		List<Integer> rowPositions = new ArrayList<Integer>();
		for (Integer rowIndex : rowIndexes) {
			rowPositions.add(getRowPositionByIndex(rowIndex));
		}
		this.hiddenRowIndexes.addAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
	}

	public void expandTreeRow(int parentIndex) {
		List<Integer> rowIndexes = 	this.treeRowModel.expand(parentIndex);
		this.hiddenRowIndexes.removeAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new ShowRowPositionsEvent(this, rowIndexes));
	}
}
