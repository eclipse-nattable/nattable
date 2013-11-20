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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.GroupBySummaryConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class GroupByColumnAccessor<T> implements IColumnAccessor<Object> {

	protected final IColumnAccessor<T> columnAccessor;
	
	private final GroupByDataLayer<T> groupByDataLayer; 
	
	private final IConfigRegistry configRegistry;

	public GroupByColumnAccessor(IColumnAccessor<T> columnAccessor) {
		this(columnAccessor, null, null);
	}
	
	public GroupByColumnAccessor(IColumnAccessor<T> columnAccessor, 
			GroupByDataLayer<T> groupByDataLayer, IConfigRegistry configRegistry) {
		this.columnAccessor = columnAccessor;
		this.groupByDataLayer = groupByDataLayer;
		this.configRegistry = configRegistry;
	}


	@Override
	@SuppressWarnings("unchecked")
	public Object getDataValue(Object rowObject, int columnIndex) {
		if (rowObject instanceof GroupByObject) {
			GroupByObject groupByObject = (GroupByObject) rowObject;

			if (this.groupByDataLayer != null) {
				int columnPosition = groupByDataLayer.getColumnPositionByIndex(columnIndex);
				LabelStack labelStack = groupByDataLayer.getConfigLabelsByPosition(columnPosition, 
						((ListDataProvider<Object>)groupByDataLayer.getDataProvider()).indexOfRowObject(rowObject));
				
				IGroupBySummaryProvider<T> summaryProvider = getGroupBySummaryProvider(labelStack);
				
				if (summaryProvider != null) {
					List<T> children = groupByDataLayer.getElementsInGroup(groupByObject);
					return summaryProvider.summarize(columnIndex, children);
				}
			}
			
			return groupByObject.getValue();
		} else {
			return columnAccessor.getDataValue((T) rowObject, columnIndex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setDataValue(Object rowObject, int columnIndex, Object newValue) {
		if (rowObject instanceof GroupByObject) {
			// do nothing
		} else {
			columnAccessor.setDataValue((T) rowObject, columnIndex, newValue);
		}
	}

	@Override
	public int getColumnCount() {
		return columnAccessor.getColumnCount();
	}

	
	@SuppressWarnings("unchecked")
	public IGroupBySummaryProvider<T> getGroupBySummaryProvider(LabelStack labelStack) {
		if (this.configRegistry != null) {
			return configRegistry.getConfigAttribute(
					GroupBySummaryConfigAttributes.GROUP_BY_SUMMARY_PROVIDER, 
					DisplayMode.NORMAL, 
					labelStack.getLabels());
		}
		
		return null;
	}
}
