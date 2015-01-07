/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Roman Flueckiger <roman.flueckiger@mac.com> - Bug 454566
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 448115, 449361, 453874
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 444839, 444855, 453885
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.util.CalculatedValueCache;
import org.eclipse.nebula.widgets.nattable.util.ICalculatedValueCacheKey;
import org.eclipse.nebula.widgets.nattable.util.ICalculator;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.TreeList.ExpansionModel;
import ca.odell.glazedlists.matchers.Matcher;

public class GroupByDataLayer<T> extends DataLayer implements Observer {

    /**
     * Label that indicates the shown tree item object as GroupByObject
     */
    public static final String GROUP_BY_OBJECT = "GROUP_BY_OBJECT"; //$NON-NLS-1$
    /**
     * Label prefix for labels that are added to cells for a group by object.
     */
    public static final String GROUP_BY_COLUMN_PREFIX = "GROUP_BY_COLUMN_"; //$NON-NLS-1$
    /**
     * Label that indicates the shown tree item object as GroupByObject and
     * contains a summary value.
     */
    public static final String GROUP_BY_SUMMARY = "GROUP_BY_SUMMARY"; //$NON-NLS-1$
    /**
     * Label prefix for labels that are added to cells for a group by object
     * summary.
     */
    public static final String GROUP_BY_SUMMARY_COLUMN_PREFIX = "GROUP_BY_SUMMARY_COLUMN_"; //$NON-NLS-1$
    /**
     * The underlying base EventList.
     */
    private final EventList<T> eventList;
    /**
     * Convenience class to retrieve information and operate on the TreeList.
     */
    private final GlazedListTreeData<Object> treeData;
    /**
     * The ITreeRowModel that is responsible to retrieve information and operate
     * on tree items.
     */
    private final GlazedListTreeRowModel<Object> treeRowModel;
    /**
     * The TreeList that is created internally by this GroupByDataLayer to
     * enable groupBy.
     */
    private final TreeList<Object> treeList;

    private final GroupByColumnAccessor<T> groupByColumnAccessor;

    private final IColumnAccessor<T> columnAccessor;

    private final GroupByTreeFormat<T> treeFormat;

    private final IConfigRegistry configRegistry;
    /**
     * The value cache that contains the summary values and performs summary
     * calculation in background processes if necessary.
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

        this(groupByModel, eventList, columnAccessor, configRegistry, true, useDefaultConfiguration);
    }

    public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor,
            IConfigRegistry configRegistry, boolean smoothUpdates, boolean useDefaultConfiguration) {

        this(groupByModel, eventList, columnAccessor, null, configRegistry, smoothUpdates, useDefaultConfiguration);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GroupByDataLayer(GroupByModel groupByModel, EventList<T> eventList, IColumnAccessor<T> columnAccessor,
            ExpansionModel<Object> expansionModel, IConfigRegistry configRegistry, boolean smoothUpdates, boolean useDefaultConfiguration) {

        this.eventList = eventList;
        this.columnAccessor = columnAccessor;

        groupByModel.addObserver(this);

        this.groupByColumnAccessor = new GroupByColumnAccessor(columnAccessor);

        this.treeFormat = createGroupByTreeFormat(groupByModel, (IColumnAccessor<T>) this.groupByColumnAccessor);
        this.treeFormat.setComparator(new GroupByComparator<T>(groupByModel, columnAccessor, this));

        this.treeList = new TreeList(eventList, this.treeFormat, expansionModel != null ? expansionModel : new GroupByExpansionModel());

        this.treeData = new GlazedListTreeData<Object>(getTreeList());
        this.treeRowModel = new GlazedListTreeRowModel<Object>(this.treeData);

        this.configRegistry = configRegistry;

        this.valueCache = new CalculatedValueCache(this, true, false, smoothUpdates);

        setDataProvider(new GlazedListsDataProvider<Object>(getTreeList(), this.groupByColumnAccessor));

        if (useDefaultConfiguration) {
            addConfiguration(new GroupByDataLayerConfiguration(this));
        }
    }

    /**
     *
     * @param groupByModel
     *            The {@link GroupByModel} that is used to specify the tree
     *            structure.
     * @param groupByColumnAccessor
     *            The {@link IColumnAccessor} that is used to access the values
     *            in the data model, should be of type
     *            {@link GroupByColumnAccessor}.
     * @return The {@link GroupByTreeFormat} that is used to build the tree
     *         structure.
     */
    protected GroupByTreeFormat<T> createGroupByTreeFormat(GroupByModel groupByModel, IColumnAccessor<T> groupByColumnAccessor) {
        return new GroupByTreeFormat<T>(groupByModel, groupByColumnAccessor);
    }

    /**
     * @param model
     *            The {@link ISortModel} that should be set to the
     *            {@link IGroupByComparator} that is necessary to create the
     *            sorted tree structure.
     * @see IGroupByComparator#setSortModel(ISortModel)
     * @deprecated use
     *             {@link #initializeTreeComparator(ISortModel, IUniqueIndexLayer, boolean)}
     */
    @Deprecated
    public void setSortModel(ISortModel model) {
        this.treeFormat.setSortModel(model);
    }

    /**
     * Initialize the {@link Comparator} that is used to build the tree
     * structure. Adding all the below information will enable correct sorting
     * of the tree structure taking the summary values and the groupBy values
     * correctly into account.
     *
     * @param sortModel
     *            The {@link ISortModel} that should be set to the
     *            {@link IGroupByComparator}. Setting the {@link ISortModel}
     *            enables the usage of the configured {@link Comparator} per
     *            column on creating the sorted tree structure.
     * @param treeLayer
     *            The {@link IUniqueIndexLayer} that should be set to the
     *            {@link IGroupByComparator}. Typically the {@link TreeLayer}
     *            and is needed to determine if the sort operation is performed
     *            on the tree column. Will only be inspected if a valid
     *            {@link ISortModel} is set.
     * @param setDataLayerReference
     *            <code>true</code> for setting the {@link GroupByDataLayer}
     *            reference to this instance to the {@link GroupByComparator},
     *            <code>false</code> to set the reference to <code>null</code>.
     *            The {@link GroupByDataLayer} reference is used in the
     *            comparator to be able to sort by summary values. If summary
     *            values are not configured or the sorting by summary value is
     *            not needed, you should avoid setting the reference.
     *
     * @see IGroupByComparator#setSortModel(ISortModel)
     * @see IGroupByComparator#setTreeLayer(IUniqueIndexLayer)
     * @see IGroupByComparator#setDataLayer(GroupByDataLayer)
     */
    public void initializeTreeComparator(ISortModel sortModel, IUniqueIndexLayer treeLayer, boolean setDataLayerReference) {
        this.treeFormat.setSortModel(sortModel);
        this.treeFormat.setTreeLayer(treeLayer);
        this.treeFormat.setDataLayer(setDataLayerReference ? this : null);
    }

    /**
     *
     * @param comparator
     *            The {@link IGroupByComparator} that is necessary to create the
     *            sorted tree structure. Can not be <code>null</code>.
     */
    public void setComparator(IGroupByComparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("IGroupByComparator can not be null"); //$NON-NLS-1$
        }
        this.treeFormat.setComparator(comparator);
    }

    /**
     * Method to update the tree list after filter or TreeList.Format changed.
     * Need this workaround to update the tree list for presentation because of
     * <a
     * href="http://java.net/jira/browse/GLAZEDLISTS-521">http://java.net/jira
     * /browse/GLAZEDLISTS-521</a>
     * <p>
     * For more information you can also have a look at this discussion: <a
     * href=
     * "http://glazedlists.1045722.n5.nabble.com/sorting-a-treelist-td4704550.html"
     * > http://glazedlists.1045722.n5.nabble.com/sorting-a-treelist-td4704550.
     * html</a>
     * </p>
     */
    protected void updateTree() {
        // Perform the update showing the busy indicator, as creating the
        // groupby structure costs time. This is related to dynamically building
        // a tree structure with additional objects
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

            @Override
            public void run() {
                GroupByDataLayer.this.eventList.getReadWriteLock().writeLock().lock();
                try {
                    /*
                     * The workaround for the update issue suggested on the
                     * mailing list iterates over the whole list. This causes a
                     * lot of list change events, which also cost processing
                     * time. Instead we are performing a clear()-addAll() which
                     * is slightly faster.
                     */
                    EventList<T> temp = GlazedLists.eventList(GroupByDataLayer.this.eventList);
                    GroupByDataLayer.this.eventList.clear();
                    GroupByDataLayer.this.eventList.addAll(temp);
                } finally {
                    GroupByDataLayer.this.eventList.getReadWriteLock().writeLock().unlock();
                }
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        // if we know the sort model, we need to clear the sort model to avoid
        // strange side effects while updating the tree structure (e.g. not
        // applied sorting although showing the sort indicator)
        // for better user experience we remember the sort state and reapply it
        // after the tree update
        List<Integer> sortedIndexes = null;
        List<SortDirectionEnum> sortDirections = null;
        if (this.treeFormat.getSortModel() != null) {
            sortedIndexes = this.treeFormat.getSortModel().getSortedColumnIndexes();
            sortDirections = new ArrayList<SortDirectionEnum>();
            for (Integer index : sortedIndexes) {
                sortDirections.add(this.treeFormat.getSortModel().getSortDirection(index));
            }
            this.treeFormat.getSortModel().clear();
        }

        updateTree();

        // re-apply the sorting after the tree update
        if (this.treeFormat.getSortModel() != null) {
            for (int i = 0; i < sortedIndexes.size(); i++) {
                Integer index = sortedIndexes.get(i);
                this.treeFormat.getSortModel().sort(index, sortDirections.get(i), true);
            }
        }

        fireLayerEvent(new RowStructuralRefreshEvent(this));
    }

    /**
     * @return The ITreeRowModel that is responsible to retrieve information and
     *         operate on tree items.
     */
    public GlazedListTreeRowModel<Object> getTreeRowModel() {
        return this.treeRowModel;
    }

    /**
     * @return The TreeList that is created internally by this GroupByDataLayer
     *         to enable groupBy.
     */
    public TreeList<Object> getTreeList() {
        return this.treeList;
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        if (this.treeData.getDataAtIndex(getRowIndexByPosition(rowPosition)) instanceof GroupByObject) {
            LabelStack configLabels = new LabelStack();
            configLabels.addLabel(GROUP_BY_COLUMN_PREFIX + columnPosition);
            configLabels.addLabel(GROUP_BY_OBJECT);

            if (this.getConfigLabelAccumulator() != null) {
                this.getConfigLabelAccumulator().accumulateConfigLabels(configLabels, columnPosition, rowPosition);
            }
            if (this.getRegionName() != null) {
                configLabels.addLabel(this.getRegionName());
            }

            if (getGroupBySummaryProvider(configLabels) != null) {
                configLabels.addLabelOnTop(GROUP_BY_SUMMARY);
                configLabels.addLabelOnTop(GROUP_BY_SUMMARY_COLUMN_PREFIX + columnPosition);
            }

            return configLabels;
        }
        return super.getConfigLabelsByPosition(columnPosition, rowPosition);
    }

    @Override
    public Object getDataValueByPosition(final int columnPosition, final int rowPosition) {
        LabelStack labelStack = getConfigLabelsByPosition(columnPosition, rowPosition);
        return getDataValueByPosition(columnPosition, rowPosition, labelStack, true);
    }

    /**
     * This method is used to retrieve a data value of an {@link ILayerCell}. It
     * is intended to be used for conditional formatting. It allows to specify
     * the {@link LabelStack} and to disable background calculation processing,
     * since the conditional formatting needs the summary value without a delay.
     *
     * @param columnPosition
     *            The column position of the cell whose data value is requested.
     * @param rowPosition
     *            The row position of the cell whose data value is requested.
     * @param labelStack
     *            The {@link LabelStack} of the cell whose data value is
     *            requested. Needed to retrieve a possible existing
     *            {@link IGroupBySummaryProvider}.
     * @param calculateInBackground
     *            <code>true</code> to calculate the summary value in the
     *            background, <code>false</code> if the calculation should be
     *            processed in the UI thread.
     * @return The data value for the {@link ILayerCell} at the given
     *         coordinates.
     */
    public Object getDataValueByPosition(final int columnPosition, final int rowPosition,
            LabelStack labelStack, boolean calculateInBackground) {

        if (labelStack.hasLabel(GROUP_BY_OBJECT)) {
            GroupByObject groupByObject = (GroupByObject) this.treeData.getDataAtIndex(rowPosition);

            final IGroupBySummaryProvider<T> summaryProvider = getGroupBySummaryProvider(labelStack);
            if (summaryProvider != null) {
                final List<T> children = getElementsInGroup(groupByObject);
                return this.valueCache.getCalculatedValue(
                        columnPosition,
                        rowPosition,
                        new GroupByValueCacheKey(columnPosition, rowPosition, groupByObject),
                        calculateInBackground,
                        new ICalculator() {
                            @Override
                            public Object executeCalculation() {
                                return summaryProvider.summarize(columnPosition, children);
                            }
                        });
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
     * Usually it is not necessary to call this method manually. But for certain
     * use cases it might be useful, e.g. changing the summary provider
     * implementation at runtime.
     *
     * @see CalculatedValueCache#clearCache()
     */
    public void clearCache() {
        this.valueCache.clearCache();
        // also clear the comparator cache to ensure correct sorting
        this.treeFormat.clearComparatorCache();
    }

    /**
     * Clears all values in the internal cache to trigger new calculations. This
     * will also clear all values in the cache copy and will result in rendering
     * like there was never a summary value calculated before.
     * <p>
     * Usually it is not necessary to call this method manually. But for certain
     * use cases it might be useful, e.g. changing the summary provider
     * implementation at runtime.
     *
     * @see CalculatedValueCache#killCache()
     */
    public void killCache() {
        this.valueCache.killCache();
        // also clear the comparator cache to ensure correct sorting
        this.treeFormat.clearComparatorCache();
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof CalculateSummaryRowValuesCommand) {
            // iterate over the whole tree structure and pre-calculate the
            // summary values
            for (int i = 0; i < getRowCount(); i++) {
                if (this.treeData.getDataAtIndex(i) instanceof GroupByObject) {
                    for (int j = 0; j < getColumnCount(); j++) {
                        LabelStack labelStack = getConfigLabelsByPosition(j, i);
                        final IGroupBySummaryProvider<T> summaryProvider = getGroupBySummaryProvider(labelStack);
                        if (summaryProvider != null) {
                            GroupByObject groupByObject = (GroupByObject) this.treeData.getDataAtIndex(i);
                            final List<T> children = getElementsInGroup(groupByObject);
                            final int col = j;
                            this.valueCache.getCalculatedValue(j, i, new GroupByValueCacheKey(j, i, groupByObject), false, new ICalculator() {
                                @Override
                                public Object executeCalculation() {
                                    return summaryProvider.summarize(col, children);
                                }
                            });
                        }
                    }
                }
            }
            // we do not return true here, as there might be other layers
            // involved in the composition that also need to calculate the
            // summary values immediately
        } else if (command instanceof DisposeResourcesCommand) {
            // ensure to clear the caches to avoid memory leaks
            this.treeFormat.clearComparatorCache();
            this.valueCache.killCache();
            this.valueCache.dispose();
        }

        return super.doCommand(command);
    }

    /**
     * Simple {@link ExpansionModel} that shows every node expanded initially.
     * <p>
     * It is not strictly necessary for implementors to record the
     * expand/collapsed state of all nodes, since TreeList caches node state
     * internally. But because of the update workaround on changes to the
     * {@link TreeList#Format}, we need to keep track of the expand/collapse
     * state ourself.
     * </p>
     *
     * @see http://publicobject.com/glazedlists/glazedlists-1.8.0/api/ca/odell/
     *      glazedlists/TreeList.ExpansionModel.html
     */
    private class GroupByExpansionModel implements TreeList.ExpansionModel<Object> {

        // remember expand states because of update workaround
        Map<Object, Boolean> expandStates = new HashMap<Object, Boolean>();

        /**
         * Determine the specified element's initial expand/collapse state.
         */
        @Override
        public boolean isExpanded(final Object element, final List<Object> path) {
            if (!this.expandStates.containsKey(element)) {
                this.expandStates.put(element, true);
            }
            return this.expandStates.get(element);
        }

        /**
         * Notifies this handler that the specified element's expand/collapse
         * state has changed.
         */
        @Override
        public void setExpanded(final Object element, final List<Object> path, final boolean expanded) {
            this.expandStates.put(element, expanded);
        }
    }

    /**
     * Get the list of elements for a group, create it if it doesn't exists.<br/>
     * We could also use treeData.getChildren(groupDescriptor, true) but it's
     * less efficient.
     *
     * @param group
     *            The {@link GroupByObject} for which the children should be
     *            retrieved.
     * @return The {@link FilterList} of elements
     */
    public FilterList<T> getElementsInGroup(GroupByObject group) {
        FilterList<T> elementsInGroup = this.filtersByGroup.get(group);
        if (elementsInGroup == null) {
            elementsInGroup = new FilterList<T>(this.eventList, getGroupDescriptorMatcher(group, this.columnAccessor));
            this.filtersByGroup.put(group, elementsInGroup);
        }
        return elementsInGroup;
    }

    /**
     *
     * @param group
     *            The {@link GroupByObject} for which the children should be
     *            retrieved.
     * @param columnAccessor
     *            The {@link IColumnAccessor} that is used to retrieve column
     *            value of an element.
     * @return The {@link Matcher} that is used to identify the children of a
     *         {@link GroupByObject}
     *
     * @see GroupDescriptorMatcher
     */
    protected Matcher<T> getGroupDescriptorMatcher(GroupByObject group, IColumnAccessor<T> columnAccessor) {
        return new GroupDescriptorMatcher<T>(group, columnAccessor);
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
            for (Entry<Integer, Object> desc : this.group.getDescriptor().entrySet()) {
                int columnIndex = desc.getKey();
                Object groupName = desc.getValue();
                if (!groupName.equals(this.columnAccessor.getDataValue(element, columnIndex))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * The ICalculatedValueCacheKey that is used for groupBy summary values.
     * Need to be a combination of column position, row position and the
     * GroupByObject because only using the cell coordinates could raise caching
     * issues if the grouping is changed.
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
            result = prime * result + this.columnPosition;
            result = prime * result + ((this.groupBy == null) ? 0 : this.groupBy.hashCode());
            result = prime * result + this.rowPosition;
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
            if (this.columnPosition != other.columnPosition)
                return false;
            if (this.groupBy == null) {
                if (other.groupBy != null)
                    return false;
            } else if (!this.groupBy.equals(other.groupBy))
                return false;
            if (this.rowPosition != other.rowPosition)
                return false;
            return true;
        }

        private GroupByDataLayer<T> getOuterType() {
            return GroupByDataLayer.this;
        }
    }
}
