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
import java.util.Observable;
import java.util.Observer;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TreeList;

public class GroupByDataLayer<T> extends DataLayer implements Observer {

	private final GroupByModel groupByModel;
	private final EventList<T> eventList;
	private final IColumnAccessor<T> columnAccessor;
	private final TreeList.ExpansionModel<Object> treeExpansionModel;
	
	private final GlazedListTreeData<Object> treeData;
	private final GlazedListTreeRowModel<Object> treeRowModel;
	private final GroupByDataProvider groupByDataProvider;

	
	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor) {
		this.groupByModel = groupByModel;
		this.eventList = eventList;
		this.columnAccessor = columnAccessor;
		
		groupByModel.addObserver(this);
		
		treeExpansionModel = new TreeList.ExpansionModel<Object>() {
			public boolean isExpanded(Object arg0, List<Object> arg1) {
				return true;
			}
			public void setExpanded(Object arg0, List<Object> arg1, boolean arg2) {
			}
		};
		
		treeData = new GlazedListTreeData<Object>(null);
		
		TreeList.Format<Object> treeFormat = new GroupByTreeFormat<T>(groupByModel, columnAccessor);
		TreeList<Object> treeList = new TreeList(eventList, treeFormat, treeExpansionModel);
		
		treeData.setTreeList(treeList);
		treeRowModel = new GlazedListTreeRowModel<Object>(treeData);
		
		IColumnAccessor<Object> groupByColumnAccessor = new GroupByColumnAccessor<T>(columnAccessor);
		groupByDataProvider = new GroupByDataProvider(treeList, groupByColumnAccessor);
		
		setDataProvider(groupByDataProvider);
	}
	
	private void resetTreeList() {
		TreeList.Format<Object> treeFormat = new GroupByTreeFormat<T>(groupByModel, columnAccessor);
		TreeList<Object> treeList = new TreeList(eventList, treeFormat, treeExpansionModel);
		
		treeData.setTreeList(treeList);
		groupByDataProvider.setList(treeList);
	}
	
	public void update(Observable o, Object arg) {
		resetTreeList();
		fireLayerEvent(new RowStructuralRefreshEvent(this));
	}

	public GlazedListTreeRowModel<Object> getTreeRowModel() {
		return treeRowModel;
	}
	
	class GroupByDataProvider extends GlazedListsDataProvider<Object> {

		public GroupByDataProvider(EventList<Object> list, IColumnAccessor<Object> columnAccessor) {
			super(list, columnAccessor);
		}
		
		public void setList(List<Object> list) {
			this.list = list;
		}
		
	}
	
}
