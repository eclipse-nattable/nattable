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
package org.eclipse.nebula.widgets.nattable.group;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;


/**
 * Tracks the Expand/Collapse of a Column Group header
 *    NOTE: Only relevant when Column Grouping is enabled.
 */
public class ColumnGroupExpandCollapseLayer extends AbstractColumnHideShowLayer implements IColumnGroupModelListener {

	private final ColumnGroupModel model;

	public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
		super(underlyingLayer);
		this.model = model;

		model.registerColumnGroupModelListener(this);

		registerCommandHandler(new ColumnGroupExpandCollapseCommandHandler(this));
	}

	public ColumnGroupModel getModel() {
		return model;
	}

	// Expand/collapse

	@Override
	public boolean isColumnIndexHidden(int columnIndex) {
		
		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		
		boolean isHiddeninUnderlyingLayer = 
			ColumnGroupUtils.isColumnIndexHiddenInUnderLyingLayer(columnIndex, this, underlyingLayer);
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		boolean isCollapsedAndStaticColumn = columnGroup != null && columnGroup.isCollapsed() &&
			!ColumnGroupUtils.isStaticOrFirstVisibleColumn(columnIndex, underlyingLayer, underlyingLayer, model);
		
		return isHiddeninUnderlyingLayer || isCollapsedAndStaticColumn;
	}

	@Override
	public Collection<Integer> getHiddenColumnIndexes() {
		Collection<Integer> hiddenColumnIndexes = new HashSet<Integer>();

		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		int underlyingColumnCount = underlyingLayer.getColumnCount();
		for (int i = 0; i < underlyingColumnCount; i++) {
			int columnIndex = underlyingLayer.getColumnIndexByPosition(i);
			ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

			if (columnGroup != null && columnGroup.isCollapsed()) {
				if (!ColumnGroupUtils.isStaticOrFirstVisibleColumn(columnIndex, underlyingLayer, underlyingLayer, model)) {
					hiddenColumnIndexes.add(Integer.valueOf(columnIndex));
				}
			}
		}

		return hiddenColumnIndexes;
	}

	// IColumnGroupModelListener

	public void columnGroupModelChanged() {
		invalidateCache();
	}

}
