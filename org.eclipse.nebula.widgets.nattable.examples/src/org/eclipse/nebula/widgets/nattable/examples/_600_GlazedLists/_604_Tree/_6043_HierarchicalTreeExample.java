/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._600_GlazedLists._604_Tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical.HierarchicalWrapperTreeFormat;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalHelper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.TreeList;

/**
 * Example showing the TreeLayer with levels over multiple columns.
 */
public class _6043_HierarchicalTreeExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(800, 400, new _6043_HierarchicalTreeExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the TreeLayer with showing nodes in multiple columns.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("manufacturer", "Manufacturer");
        propertyToLabelMap.put("model", "Model");
        propertyToLabelMap.put("motors.identifier", "Identifier");
        propertyToLabelMap.put("motors.capacity", "Capacity");
        propertyToLabelMap.put("motors.capacityUnit", "Capacity Unit");
        propertyToLabelMap.put("motors.maximumSpeed", "Maximum Speed");
        propertyToLabelMap.put("motors.feedbacks.creationTime", "Creation Time");
        propertyToLabelMap.put("motors.feedbacks.classification", "Classification");
        propertyToLabelMap.put("motors.feedbacks.comment", "Comment");

        BodyLayerStack bodyLayerStack = new BodyLayerStack(
                CarService.getInput(),
                CarService.PROPERTY_NAMES,
                new HierarchicalWrapperTreeFormat(CarService.PROPERTY_NAMES));

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(CarService.PROPERTY_NAMES, propertyToLabelMap);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                new DataLayer(columnHeaderDataProvider),
                bodyLayerStack,
                bodyLayerStack.getSelectionLayer());

        // create the row header layer stack
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DataLayer(rowHeaderDataProvider, 40, 20),
                bodyLayerStack,
                bodyLayerStack.getSelectionLayer());

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(
                new DataLayer(
                        new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                rowHeaderLayer,
                columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer =
                new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(parent, gridLayer);
        return natTable;
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     */
    class BodyLayerStack extends AbstractLayerTransform {

        private final TreeList<HierarchicalWrapper> treeList;

        private final IRowDataProvider<HierarchicalWrapper> bodyDataProvider;

        private final SelectionLayer selectionLayer;

        private final TreeLayer treeLayer;

        @SuppressWarnings("unchecked")
        public BodyLayerStack(List<?> values,
                String[] propertyNames,
                TreeList.Format<HierarchicalWrapper> treeFormat) {

            // de-normalize with parent structure objects
            List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(values, true, propertyNames);

            EventList<HierarchicalWrapper> eventList = GlazedLists.eventList(data);
            TransformedList<HierarchicalWrapper, HierarchicalWrapper> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator will be set by configuration
            SortedList<HierarchicalWrapper> sortedList = new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the TreeList
            this.treeList = new TreeList<HierarchicalWrapper>(sortedList, treeFormat, TreeList.NODES_START_EXPANDED);

            HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor =
                    new HierarchicalReflectiveColumnPropertyAccessor(propertyNames);

            this.bodyDataProvider = new ListDataProvider<>(this.treeList, columnPropertyAccessor);

            DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

            // simply apply labels for every column by index
            bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

            // layer for event handling of GlazedLists and PropertyChanges
            GlazedListsEventLayer<HierarchicalWrapper> glazedListsEventLayer = new GlazedListsEventLayer<>(bodyDataLayer, this.treeList);

            GlazedListTreeData<HierarchicalWrapper> treeData = new GlazedListTreeData<>(this.treeList);
            ITreeRowModel<HierarchicalWrapper> treeRowModel = new GlazedListTreeRowModel<>(treeData);

            this.selectionLayer = new SelectionLayer(glazedListsEventLayer);

            this.treeLayer = new TreeLayer(this.selectionLayer, treeRowModel) {
                @Override
                protected boolean isTreeColumn(int columnPosition) {
                    // for this example we know where we show a tree node, for a
                    // more dynamic implementation check the
                    // HierarchicalTreeLayer
                    return columnPosition == 0 || columnPosition == 2;
                }

                @Override
                public Object getDataValueByPosition(int columnPosition, int rowPosition) {
                    int columnIndex = getColumnIndexByPosition(columnPosition);
                    int rowIndex = getRowIndexByPosition(rowPosition);

                    HierarchicalWrapper rowObject = BodyLayerStack.this.bodyDataProvider.getRowObject(rowIndex);
                    String columnProperty = columnPropertyAccessor.getColumnProperty(columnIndex);

                    // inspect the level of the property
                    int nextLevel = columnProperty.split("\\.").length;
                    // get level object of the next level in the object
                    if ((nextLevel < rowObject.getLevels() && rowObject.getObject(nextLevel) == null) || nextLevel == rowObject.getLevels()) {
                        return super.getDataValueByPosition(columnPosition, rowPosition);
                    }
                    return null;
                }

                @Override
                public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
                    LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
                    // remove the labels that might be added by the super
                    // implementation
                    configLabels.removeLabel(TREE_COLUMN_CELL);
                    configLabels.removeLabel(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE);
                    configLabels.removeLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE);
                    configLabels.removeLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE);

                    // now add the labels from this layer
                    if (isTreeColumn(columnPosition) && getDataValueByPosition(columnPosition, rowPosition) != null) {
                        configLabels.addLabelOnTop(TREE_COLUMN_CELL);

                        int rowIndex = getRowIndexByPosition(rowPosition);
                        configLabels.addLabelOnTop(
                                DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + getModel().depth(rowIndex));
                        if (!getModel().hasChildren(rowIndex)) {
                            configLabels.addLabelOnTop(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE);
                        } else {
                            if (getModel().isCollapsed(rowIndex)) {
                                configLabels.addLabelOnTop(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE);
                            } else {
                                configLabels.addLabelOnTop(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE);
                            }
                        }
                    }
                    return configLabels;
                }

            };
            ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public TreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public TreeList<HierarchicalWrapper> getTreeList() {
            return this.treeList;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }
    }
}
