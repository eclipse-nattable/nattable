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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.util.CalculatedValueCache;
import org.eclipse.nebula.widgets.nattable.util.ICalculatedValueCacheKey;
import org.eclipse.nebula.widgets.nattable.util.ICalculator;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.matchers.Matcher;

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
	
	private final IColumnAccessor<T> columnAccessor;
	
	private final GroupByTreeFormat<T> treeFormat;
	
	private final IConfigRegistry configRegistry;
	/**
	 * The value cache that contains the summary values and performs summary calculation in 
	 * background processes if necessary.
	 */
	private CalculatedValueCache valueCache;
	
	/** Map the group to a dynamic list of group elements */
	private final Map<GroupByObject, FilterList<T>> filtersByGroup = new ConcurrentHashMap<GroupByObject, FilterList<T>>();

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
	
	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor,
			IConfigRegistry configRegistry, boolean useDefaultConfiguration) {
		this(groupByModel, eventList, columnAccessor, configRegistry, true, true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor,
			IConfigRegistry configRegistry, boolean smoothUpdates, boolean useDefaultConfiguration) {
		this.eventList = eventList;
		this.columnAccessor = columnAccessor;

		groupByModel.addObserver(this);

		this.groupByColumnAccessor = new GroupByColumnAccessor(columnAccessor);

		this.treeFormat = new GroupByTreeFormat<T>(groupByModel, (IColumnAccessor<T>) groupByColumnAccessor);
		this.treeList = new TreeList(eventList, treeFormat, new GroupByExpansionModel());

		this.treeData = new GlazedListTreeData<Object>(getTreeList());
		this.treeRowModel = new GlazedListTreeRowModel<Object>(treeData);

		this.configRegistry = configRegistry;
		
		this.valueCache = new CalculatedValueCache(this, true, false, smoothUpdates);
		
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
		//Perform the update showing the busy indicator, as creating the groupby structure
		//costs time. This is related to dynamically building a tree structure with additional objects
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			
			@Override
			public void run() {
				eventList.getReadWriteLock().writeLock().lock();
				try {
					/*
					 * The workaround for the update issue suggested on the mailing list iterates
					 * over the whole list. This causes a lot of list change events, which also cost
					 * processing time. Instead we are performing a clear()-addAll() which is slightly
					 * faster.
					 */
					EventList<T> temp = GlazedLists.eventList(eventList);
					eventList.clear();
					eventList.addAll(temp);
				} finally {
					eventList.getReadWriteLock().writeLock().unlock();
				}
			}
		});
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
			if (getGroupBySummaryProvider(configLabels) != null) {
				configLabels.addLabel(GROUP_BY_SUMMARY);
			}
		}
		return configLabels;
	}
	
	@Override
	public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
		LabelStack labelStack = getConfigLabelsByPosition(columnPosition, rowPosition);
		if (labelStack.hasLabel(GROUP_BY_OBJECT)) {
			GroupByObject groupByObject = (GroupByObject) this.treeData.getDataAtIndex(rowPosition);
			
			//ensure to only load the children if they are needed
			List<T> children = null;
			
			final IGroupBySummaryProvider<T> summaryProvider = getGroupBySummaryProvider(labelStack);
			if (summaryProvider != null) {
				children = getElementsInGroup(groupByObject);
				final List<T> c = children; 
				return this.valueCache.getCalculatedValue(columnPosition, rowPosition, 
						new GroupByValueCacheKey(columnPosition, rowPosition, groupByObject), true, 
						new ICalculator() {
							@Override
							public Object executeCalculation() {
								return summaryProvider.summarize(columnPosition, c);
							}
						});
			}
			
			if (this.configRegistry != null) {
				String childCountPattern = this.configRegistry.getConfigAttribute(
						GroupByConfigAttributes.GROUP_BY_CHILD_COUNT_PATTERN, 
						DisplayMode.NORMAL, 
						labelStack.getLabels());
				
				if (childCountPattern != null && childCountPattern.length() > 0) {
					if (children == null) {
						children = getElementsInGroup(groupByObject);
					}
					
					int directChildCount = this.treeRowModel.getDirectChildren(rowPosition).size();
					
					return groupByObject.getValue() + " " +  //$NON-NLS-1$
						MessageFormat.format(childCountPattern, children.size(), directChildCount);
				}
			}
		}
		return super.getDataValueByPosition(columnPosition, rowPosition);
	}
	
	
	@SuppressWarnings("unchecked")
	public IGroupBySummaryProvider<T> getGroupBySummaryProvider(LabelStack labelStack) {
		if (this.configRegistry != null) {
			return this.configRegistry.getConfigAttribute(
					GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER, 
					DisplayMode.NORMAL, 
					labelStack.getLabels());
		}
		
		return null;
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IVisualChangeEvent) {
			clearCache();
		}

		super.handleLayerEvent(event);
	}
	
	/**
	 * Clear the internal cache to trigger new calculations.
	 * <p>
	 * Usually it is not necessary to call this method manually. But for certain use cases
	 * it might be useful, e.g. changing the summary provider implementation at runtime.
	 * 
	 * @see CalculatedValueCache#clearCache()
	 */
	public void clearCache() {
		this.valueCache.clearCache();
	}
	
	/**
	 * Clears all values in the internal cache to trigger new calculations. This will also
	 * clear all values in the cache copy and will result in rendering like there was never
	 * a summary value calculated before.
	 * <p>
	 * Usually it is not necessary to call this method manually. But for certain use cases
	 * it might be useful, e.g. changing the summary provider implementation at runtime.
	 * 
	 * @see CalculatedValueCache#killCache()
	 */
	public void killCache() {
		this.valueCache.killCache();
	}
	
	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof CalculateSummaryRowValuesCommand) {
			//iterate over the whole tree structure and pre-calculate the summary values
			for (int i = 0; i < getRowCount(); i++) {
				if (this.treeData.getDataAtIndex(i) instanceof GroupByObject) {
					for (int j = 0; j < getColumnCount(); j++) {
						LabelStack labelStack = getConfigLabelsByPosition(j, i);
						final IGroupBySummaryProvider<T> summaryProvider = getGroupBySummaryProvider(labelStack);
						if (summaryProvider != null) {
							GroupByObject groupByObject = (GroupByObject) this.treeData.getDataAtIndex(i);
							final List<T> children = getElementsInGroup(groupByObject);
							final int col = j;
							this.valueCache.getCalculatedValue(j, i, 
								new GroupByValueCacheKey(j, i, groupByObject), false, 
								new ICalculator() {
									@Override
									public Object executeCalculation() {
										return summaryProvider.summarize(col, children);
									}
								});
						}
					}
				}
			}
			//we do not return true here, as there might be other layers involved in 
			//the composition that also need to calculate the summary values immediately
		}
		else if (command instanceof DisposeResourcesCommand) {
			this.valueCache.dispose();
		}
		
		return super.doCommand(command);
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

	/**
	 * Get the list of elements for a group, create it if it doesn't exists.<br/>
	 * We could also use treeData.getChildren(groupDescriptor, true) but it's less efficient.
	 * @param groupDescriptor The description of the group (columnIndexes..)
	 * @return The FilterList of elements
	 */
	public FilterList<T> getElementsInGroup(GroupByObject groupDescriptor) {
		FilterList<T> elementsInGroup = filtersByGroup.get(groupDescriptor);
		if (elementsInGroup == null) {
			elementsInGroup = new FilterList<T>(eventList, new GroupDescriptorMatcher<T>(groupDescriptor, columnAccessor));
			filtersByGroup.put(groupDescriptor, elementsInGroup);
		}
		return elementsInGroup;
	}
	
	/**
	 * To find out if an element is part of a group
	 */
	public static class GroupDescriptorMatcher<T> implements Matcher<T> {

		private final GroupByObject group;
		private final IColumnAccessor<T> columnAccessor;

		public GroupDescriptorMatcher(GroupByObject group, IColumnAccessor<T> columnAccessor) {
			this.group = group;
			this.columnAccessor = columnAccessor;
		}

		@Override
		public boolean matches(T element) {
			for (Entry<Integer, Object> desc : group.getDescriptor()) {
				int columnIndex = desc.getKey();
				Object groupName = desc.getValue();
				if (!groupName.equals(columnAccessor.getDataValue(element, columnIndex))) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * The ICalculatedValueCacheKey that is used for groupBy summary values.
	 * Need to be a combination of column position, row position and the GroupByObject
	 * because only using the cell coordinates could raise caching issues if the
	 * grouping is changed.
	 */
	class GroupByValueCacheKey implements ICalculatedValueCacheKey {

		private final int columnPosition;
		private final int rowPosition;
		private final GroupByObject groupBy;
		
		public GroupByValueCacheKey(int columnPosition, int rowPosition, GroupByObject groupBy) {
			this.columnPosition = columnPosition;
			this.rowPosition = rowPosition;
			this.groupBy = groupBy;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + columnPosition;
			result = prime * result
					+ ((groupBy == null) ? 0 : groupBy.hashCode());
			result = prime * result + rowPosition;
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GroupByValueCacheKey other = (GroupByValueCacheKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (columnPosition != other.columnPosition)
				return false;
			if (groupBy == null) {
				if (other.groupBy != null)
					return false;
			} else if (!groupBy.equals(other.groupBy))
				return false;
			if (rowPosition != other.rowPosition)
				return false;
			return true;
		}

		private GroupByDataLayer<T> getOuterType() {
			return GroupByDataLayer.this;
		}
	}
}
