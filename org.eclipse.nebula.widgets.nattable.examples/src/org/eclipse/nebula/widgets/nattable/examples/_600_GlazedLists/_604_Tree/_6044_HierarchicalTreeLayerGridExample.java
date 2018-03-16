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
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical.HierarchicalWrapperSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalHelper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalSpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapperComparator;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeLayerExpandCollapseKeyBindings;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * Example showing the HierarchicalTreeLayer in a grid composition.
 */
public class _6044_HierarchicalTreeLayerGridExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(800, 400, new _6044_HierarchicalTreeLayerGridExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the HierarchicalTreeLayer to implement a multi level tree in a grid composition.";
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

        ConfigRegistry configRegistry = new ConfigRegistry();

        BodyLayerStack bodyLayerStack = new BodyLayerStack(CarService.getInput(), CarService.PROPERTY_NAMES);

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(CarService.PROPERTY_NAMES, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                columnHeaderDataLayer,
                bodyLayerStack,
                bodyLayerStack.getSelectionLayer());
        // add the SortHeaderLayer to the column header layer stack
        final SortHeaderLayer<HierarchicalWrapper> sortHeaderLayer =
                new SortHeaderLayer<>(
                        columnHeaderLayer,
                        new HierarchicalWrapperSortModel(
                                bodyLayerStack.getSortedList(),
                                bodyLayerStack.getColumnPropertyAccessor(),
                                bodyLayerStack.getTreeLayer().getLevelIndexMapping(),
                                columnHeaderDataLayer,
                                configRegistry),
                        false);

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
                sortHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer =
                new GridLayer(bodyLayerStack, sortHeaderLayer, rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(parent, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
            {
                this.vAlign = VerticalAlignmentEnum.TOP;
                this.hAlign = HorizontalAlignmentEnum.LEFT;
                this.cellPainter = new PaddingDecorator(new TextPainter(), 2);
            }
        });

        // adds the key bindings that allows pressing space bar to
        // expand/collapse tree nodes
        natTable.addConfiguration(
                new TreeLayerExpandCollapseKeyBindings(
                        bodyLayerStack.getTreeLayer(),
                        bodyLayerStack.getSelectionLayer()));

        // add sorting configuration
        natTable.addConfiguration(new SingleClickSortConfiguration());

        natTable.configure();

        return natTable;
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     */
    class BodyLayerStack extends AbstractLayerTransform {

        private final HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor;

        private final SortedList<HierarchicalWrapper> sortedList;

        private final IRowDataProvider<HierarchicalWrapper> bodyDataProvider;

        private final HierarchicalSpanningDataProvider spanningDataProvider;

        private final SelectionLayer selectionLayer;

        private final HierarchicalTreeLayer treeLayer;

        public BodyLayerStack(List<?> values, String[] propertyNames) {

            // de-normalize the object graph without parent structure objects
            List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(values, false, propertyNames);

            EventList<HierarchicalWrapper> eventList = GlazedLists.eventList(data);
            TransformedList<HierarchicalWrapper, HierarchicalWrapper> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

            this.columnPropertyAccessor = new HierarchicalReflectiveColumnPropertyAccessor(propertyNames);

            // use the SortedList constructor with a
            // HierarchicalWrapperComparator for initial sorting
            // for dynamic sorting the Comparator will be set by configuration
            this.sortedList = new SortedList<>(
                    rowObjectsGlazedList,
                    new HierarchicalWrapperComparator(this.columnPropertyAccessor, HierarchicalHelper.getLevelIndexMapping(propertyNames)));

            this.bodyDataProvider = new ListDataProvider<>(this.sortedList, this.columnPropertyAccessor);
            this.spanningDataProvider = new HierarchicalSpanningDataProvider(this.bodyDataProvider, propertyNames);
            DataLayer bodyDataLayer = new SpanningDataLayer(this.spanningDataProvider);

            // simply apply labels for every column by index
            bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

            // layer for event handling of GlazedLists and PropertyChanges
            GlazedListsEventLayer<HierarchicalWrapper> glazedListsEventLayer = new GlazedListsEventLayer<>(bodyDataLayer, this.sortedList);

            ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer, false);

            this.selectionLayer = new SelectionLayer(columnReorderLayer);
            this.treeLayer = new HierarchicalTreeLayer(this.selectionLayer, this.sortedList, propertyNames);
            this.treeLayer.setShowTreeLevelHeader(false);
            ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public HierarchicalTreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }

        public HierarchicalSpanningDataProvider getSpanningDataProvider() {
            return this.spanningDataProvider;
        }

        public SortedList<HierarchicalWrapper> getSortedList() {
            return this.sortedList;
        }

        public HierarchicalReflectiveColumnPropertyAccessor getColumnPropertyAccessor() {
            return this.columnPropertyAccessor;
        }
    }

}
