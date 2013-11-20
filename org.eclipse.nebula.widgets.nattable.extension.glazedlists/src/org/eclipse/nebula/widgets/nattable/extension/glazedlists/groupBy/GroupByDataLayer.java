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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TreeList;

public class GroupByDataLayer<T> extends DataLayer implements Observer {

	/**
	 * Label that indicates the shown tree item object as GroupByObject
	 */
	public static final String GROUP_BY_OBJECT = "GROUP_BY_OBJECT"; //$NON-NLS-1$
	/**
	 * Label that indicates the shown tree item object as GroupByObject and contains a summary value.
	 */
	public static final String GROUP_BY_SUMMARY = "GROUP_BY_SUMMARY"; //$NON-NLS-1$
	/**
	 * Label prefix for labels that are added to cells for a group by object.
	 */
	public static final String GROUP_BY_COLUMN_PREFIX = "GROUP_BY_COLUMN_"; //$NON-NLS-1$
	/**
	 * The underlying base EventList.
	 */
	private final EventList<T> eventList;
	/**
	 * Convenience class to retrieve information and operate on the TreeList.
	 */
	private final GlazedListTreeData<Object> treeData;
	/**
	 * The ITreeRowModel that is responsible to retrieve information and operate on tree items.
	 */
	private final GlazedListTreeRowModel<Object> treeRowModel;
	/**
	 * The TreeList that is created internally by this GroupByDataLayer to enable groupBy.
	 */
	private final TreeList<Object> treeList;

	private final GroupByColumnAccessor<T> groupByColumnAccessor;
	
	private final GroupByTreeFormat<T> treeFormat;

	/** Map the group to a dynamic list of group elements */

	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor) {
		this(groupByModel, eventList, columnAccessor, null, true);
	}

	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor, 
			boolean useDefaultConfiguration) {
		this(groupByModel, eventList, columnAccessor, null, useDefaultConfiguration);
	}

	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor,
			IConfigRegistry configRegistry) {
		this(groupByModel, eventList, columnAccessor, configRegistry, true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor,
			IConfigRegistry configRegistry, boolean useDefaultConfiguration) {
		this.eventList = eventList;

		groupByModel.addObserver(this);

		this.groupByColumnAccessor = new GroupByColumnAccessor(columnAccessor, this, configRegistry);

		this.treeFormat = new GroupByTreeFormat<T>(groupByModel, (IColumnAccessor<T>) groupByColumnAccessor);
		this.treeList = new TreeList(eventList, treeFormat, new GroupByExpansionModel());

		this.treeData = new GlazedListTreeData<Object>(getTreeList());
		this.treeRowModel = new GlazedListTreeRowModel<Object>(treeData);

		setDataProvider(new GlazedListsDataProvider<Object>(getTreeList(), groupByColumnAccessor));

		if (useDefaultConfiguration) {
			addConfiguration(new GroupByDataLayerConfiguration());
		}
	}

	public void setSortModel(ISortModel model) {
		this.treeFormat.setSortModel(model);
	}

	/**
	 * Method to update the tree list after filter or TreeList.Format changed.
	 * Need this workaround to update the tree list for presentation because of
	 * http://java.net/jira/browse/GLAZEDLISTS-521
	 * 
	 * @see http://glazedlists.1045722.n5.nabble.com/sorting-a-treelist-td4704550.html
	 */
	protected void updateTree() {
		this.eventList.getReadWriteLock().writeLock().lock();
		try {
			for (int i = 0; i < this.eventList.size(); i++) {
				this.eventList.set(i,
						this.eventList.get(i));
			}
		} finally {
			this.eventList.getReadWriteLock().writeLock().unlock();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		updateTree();
		fireLayerEvent(new RowStructuralRefreshEvent(this));
	}

	/**
	 * @return The ITreeRowModel that is responsible to retrieve information and operate on tree items.
	 */
	public GlazedListTreeRowModel<Object> getTreeRowModel() {
		return this.treeRowModel;
	}

	/**
	 * @return The TreeList that is created internally by this GroupByDataLayer to enable groupBy.
	 */
	public TreeList<Object> getTreeList() {
		return this.treeList;
	}

	@Override
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
		if (this.treeData.getDataAtIndex(getRowIndexByPosition(rowPosition)) instanceof GroupByObject) {
			configLabels.addLabelOnTop(GROUP_BY_OBJECT);
			configLabels.addLabelOnTop(GROUP_BY_COLUMN_PREFIX + columnPosition);
			if (this.groupByColumnAccessor.getGroupBySummaryProvider(configLabels) != null) {
				configLabels.addLabel(GROUP_BY_SUMMARY);
			}
		}
		return configLabels;
	}

	/**
	 * Simple {@link ExpansionModel} that shows every node expanded initially
	 * and doesn't react on expand/collapse state changes.
	 * 
	 * It is not strictly necessary for implementors to record the
	 * expand/collapsed state of all nodes, since TreeList caches node state
	 * internally.
	 * 
	 * @see http://publicobject.com/glazedlists/glazedlists-1.8.0/api/ca/odell/
	 * glazedlists/TreeList.ExpansionModel.html
	 */
	private class GroupByExpansionModel implements TreeList.ExpansionModel<Object> {
		/**
		 * Determine the specified element's initial expand/collapse state.
		 */
		@Override
		public boolean isExpanded(final Object element, final List<Object> path) {
			return true;
		}

		/**
		 * Notifies this handler that the specified element's expand/collapse
		 * state has changed.
		 */
		@Override
		public void setExpanded(final Object element, final List<Object> path, final boolean expanded) {
			//do nothing
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> getElementsInGroup(GroupByObject groupDescriptor) {
		List<T> children = new ArrayList<T>();
		for (Object o : treeData.getChildren(groupDescriptor, true)) {
			if (o instanceof GroupByObject) {
				// do nothing
			} else {
				children.add((T) o);
			}
		}
		return children;
	}

}
