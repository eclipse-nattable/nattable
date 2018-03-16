/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.car.Car;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalHelper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalSpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer.HierarchicalTreeNode;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapperComparator;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.Matcher;

public class HierarchicalTreeLayerGlazedListsTest {

    private SortedList<HierarchicalWrapper> sortedList;
    private FilterList<HierarchicalWrapper> filterList;
    private HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor;
    private IRowDataProvider<HierarchicalWrapper> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private SelectionLayer selectionLayer;
    private HierarchicalTreeLayer treeLayer;

    private ConfigRegistry configRegistry;
    private DataLayer columnHeaderDataLayer;

    @Before
    public void setup() {
        // de-normalize the object graph without parent structure objects
        List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(CarService.getInput(), false, CarService.PROPERTY_NAMES_COMPACT);

        EventList<HierarchicalWrapper> eventList = GlazedLists.eventList(data);
        TransformedList<HierarchicalWrapper, HierarchicalWrapper> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

        this.columnPropertyAccessor = new HierarchicalReflectiveColumnPropertyAccessor(CarService.PROPERTY_NAMES_COMPACT);

        this.sortedList = new SortedList<>(
                rowObjectsGlazedList,
                new HierarchicalWrapperComparator(
                        this.columnPropertyAccessor,
                        HierarchicalHelper.getLevelIndexMapping(CarService.PROPERTY_NAMES_COMPACT)));
        this.filterList = new FilterList<>(this.sortedList);

        this.bodyDataProvider = new ListDataProvider<>(this.filterList, this.columnPropertyAccessor);
        HierarchicalSpanningDataProvider spanningDataProvider = new HierarchicalSpanningDataProvider(this.bodyDataProvider, CarService.PROPERTY_NAMES_COMPACT);
        this.bodyDataLayer = new SpanningDataLayer(spanningDataProvider);

        // simply apply labels for every column by index
        this.bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        // layer for event handling of GlazedLists and PropertyChanges
        GlazedListsEventLayer<HierarchicalWrapper> glazedListsEventLayer = new GlazedListsEventLayer<>(this.bodyDataLayer, this.filterList);
        glazedListsEventLayer.setTestMode(true);

        this.selectionLayer = new SelectionLayer(glazedListsEventLayer);
        this.treeLayer = new HierarchicalTreeLayer(this.selectionLayer, this.filterList, CarService.PROPERTY_NAMES_COMPACT);

        // create a dummy config registry
        this.configRegistry = new ConfigRegistry();
        this.configRegistry.registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR,
                DefaultComparator.getInstance());

        this.columnHeaderDataLayer = new DataLayer(new DefaultColumnHeaderDataProvider(CarService.PROPERTY_NAMES_COMPACT));
    }

    @After
    public void tearDown() {
        this.treeLayer.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldRetainCollapsedStateOnSort() throws InterruptedException, ExecutionException, TimeoutException {
        // collapse second level of first item of mercedes (row index 6 hidden)
        // mind the initial sorting
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(6, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        HierarchicalTreeNode node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(6, node.rowIndex);
        assertEquals(2, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(7, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        // sort column index 2 DESC
        HierarchicalWrapperSortModel sortModel = new HierarchicalWrapperSortModel(
                this.sortedList, this.columnPropertyAccessor, this.treeLayer.getLevelIndexMapping(), this.columnHeaderDataLayer, this.configRegistry);
        sortModel.sort(2, SortDirectionEnum.DESC, false);

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        // collapsed now row index 3
        node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(9, node.rowIndex);
        assertEquals(2, node.columnIndex);
        assertNotNull(node.rowObject);
        // hidden row now index 4
        assertEquals(10, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());
    }

    @Test
    public void shouldFindIndex() {
        HierarchicalWrapper wrapper = this.sortedList.get(0);
        assertEquals(0, this.sortedList.indexOf(wrapper));
    }

    @Test
    public void shouldRetainCollapsedStateOnDescSorting() throws InterruptedException, ExecutionException, TimeoutException {

        HierarchicalWrapper rowObject = this.sortedList.get(0);

        // collapse first level of first item
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        HierarchicalTreeNode node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        // sort column index 2 DESC
        HierarchicalWrapperSortModel sortModel = new HierarchicalWrapperSortModel(
                this.sortedList, this.columnPropertyAccessor, this.treeLayer.getLevelIndexMapping(), this.columnHeaderDataLayer, this.configRegistry);
        sortModel.sort(2, SortDirectionEnum.DESC, false);

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        int row = this.treeLayer.findTopRowIndex(0, rowObject);
        assertEquals(0, row);

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void shouldRetainCollapsedStateOnFilter() throws InterruptedException, ExecutionException, TimeoutException {
        // collapse first level of first item
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        HierarchicalTreeNode node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 1));

        // filter out BMW which is the first row that is collapsed
        this.filterList.setMatcher(new Matcher<HierarchicalWrapper>() {

            @Override
            public boolean matches(HierarchicalWrapper item) {
                return !((Car) item.getObject(0)).getManufacturer().equals("BMW");
            }
        });

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        assertEquals(6, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        node = this.treeLayer.getCollapsedNodes().iterator().next();
        // row index -1 as we retain but the object is not available anymore
        assertEquals(-1, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        // nothing hidden as it is filtered
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 0));

        this.filterList.setMatcher(null);

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        // bring it back to is previous state and the collapsed state is
        // restored
        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 1));
    }

    @Test
    public void shouldClearCollapsedStateOnFilter() throws InterruptedException, ExecutionException, TimeoutException {
        // remove deleted objects from the collection
        this.treeLayer.setRetainRemovedRowObjectNodes(false);

        // collapse first level of first item
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        HierarchicalTreeNode node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 1));

        // filter out BMW which is the first row that is collapsed
        this.filterList.setMatcher(new Matcher<HierarchicalWrapper>() {

            @Override
            public boolean matches(HierarchicalWrapper item) {
                return !((Car) item.getObject(0)).getManufacturer().equals("BMW");
            }
        });

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        assertEquals(6, this.treeLayer.getRowCount());
        // nothing collapsed anymore as collapsed node was removed/filtered
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 0));

        this.filterList.setMatcher(null);

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        // no restore of collapsed nodes
        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 1));
    }

    @Test
    public void shouldCleanupRetainedCollapsedStates() throws InterruptedException, ExecutionException, TimeoutException {
        // collapse first level of first item
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        HierarchicalTreeNode node = this.treeLayer.getCollapsedNodes().iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 1));

        // filter out BMW which is the first row that is collapsed
        this.filterList.setMatcher(new Matcher<HierarchicalWrapper>() {

            @Override
            public boolean matches(HierarchicalWrapper item) {
                return !((Car) item.getObject(0)).getManufacturer().equals("BMW");
            }
        });

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        assertEquals(6, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.getCollapsedNodes().size());
        node = this.treeLayer.getCollapsedNodes().iterator().next();
        // row index -1 as we retain but the object is not available anymore
        assertEquals(-1, node.rowIndex);
        assertEquals(0, node.columnIndex);
        assertNotNull(node.rowObject);
        // nothing hidden as it is filtered
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 0));

        // cleanup retained
        this.treeLayer.cleanupRetainedCollapsedNodes();

        assertEquals(6, this.treeLayer.getRowCount());
        // nothing collapsed anymore as collapsed node was removed/filtered
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 0));

        this.filterList.setMatcher(null);

        // refresh the layers to ensure the state is current
        // sometimes in the tests list change events where missing
        this.treeLayer.doCommand(new StructuralRefreshCommand());

        // no restore of collapsed nodes
        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 1));
    }

}
