/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
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
import org.eclipse.nebula.widgets.nattable.dataset.car.Classification;
import org.eclipse.nebula.widgets.nattable.dataset.car.Feedback;
import org.eclipse.nebula.widgets.nattable.dataset.car.Motor;
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
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
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

    private List<Car> input = CarService.getInput();
    private EventList<HierarchicalWrapper> eventList;
    private SortedList<HierarchicalWrapper> sortedList;
    private FilterList<HierarchicalWrapper> filterList;
    private HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor;
    private IRowDataProvider<HierarchicalWrapper> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private SelectionLayer selectionLayer;
    private HierarchicalTreeLayer treeLayer;

    private ConfigRegistry configRegistry;
    private DataLayer columnHeaderDataLayer;

    private HierarchicalWrapperSortModel sortModel;

    @Before
    public void setup() {
        this.input = CarService.getInput();
        // de-normalize the object graph without parent structure objects
        List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(this.input, false, CarService.PROPERTY_NAMES_COMPACT);

        this.eventList = GlazedLists.eventList(data);
        TransformedList<HierarchicalWrapper, HierarchicalWrapper> rowObjectsGlazedList = GlazedLists.threadSafeList(this.eventList);

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

        this.sortModel = new HierarchicalWrapperSortModel(
                this.sortedList, this.columnPropertyAccessor, this.treeLayer.getLevelIndexMapping(), this.columnHeaderDataLayer, this.configRegistry);
    }

    @After
    public void tearDown() {
        this.treeLayer.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldRetainCollapsedStateOnSort() throws InterruptedException, ExecutionException, TimeoutException {
        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

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

        // sort column index 2 ASC
        this.sortModel.sort(2, SortDirectionEnum.ASC, true);

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
        this.sortModel.sort(2, SortDirectionEnum.DESC, false);

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
        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

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
        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

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
        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

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

    @Test
    public void shouldBecomeUnsorted() {
        assertEquals("Mercedes", this.treeLayer.getDataValueByPosition(1, 0));
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 5));
        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 6));

        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 0));
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 5));
        assertEquals("Mercedes", this.treeLayer.getDataValueByPosition(1, 6));

        this.sortModel.sort(0, SortDirectionEnum.DESC, false);

        assertEquals("Mercedes", this.treeLayer.getDataValueByPosition(1, 0));
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 5));
        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 6));

        this.sortModel.sort(0, SortDirectionEnum.NONE, false);

        assertEquals("Mercedes", this.treeLayer.getDataValueByPosition(1, 0));
        assertEquals("McLaren", this.treeLayer.getDataValueByPosition(1, 5));
        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 6));
    }

    @Test
    public void shouldOnlySortFirstLevel() {
        addCarToInput();

        assertEquals(16, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());

        ILayerCell cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));

        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 5));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 6));
        assertNull(this.treeLayer.getDataValueByPosition(8, 7));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 9));

        cell = this.treeLayer.getCellByPosition(1, 10);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 15));

        // sort by manufacturer
        this.sortModel.sort(0, SortDirectionEnum.DESC, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));

        // remove sorting again
        this.sortModel.sort(0, SortDirectionEnum.NONE, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));
    }

    @Test
    public void shouldOnlySortMiddleLevel() {
        addCarToInput();

        assertEquals(16, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());

        ILayerCell cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("C320", this.treeLayer.getDataValueByPosition(4, 0));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 0).getRowSpan());
        assertEquals("C220", this.treeLayer.getDataValueByPosition(4, 2));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 2).getRowSpan());
        assertEquals("C200", this.treeLayer.getDataValueByPosition(4, 3));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 3).getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 6));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 6).getRowSpan());
        assertEquals("318", this.treeLayer.getDataValueByPosition(4, 8));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 8).getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 9));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 9).getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 11));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 11).getRowSpan());
        assertEquals("312", this.treeLayer.getDataValueByPosition(4, 13));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 13).getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 14));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 14).getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));

        // sort by identifier
        this.sortModel.sort(3, SortDirectionEnum.ASC, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("C200", this.treeLayer.getDataValueByPosition(4, 0));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 0).getRowSpan());
        assertEquals("C220", this.treeLayer.getDataValueByPosition(4, 2));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 2).getRowSpan());
        assertEquals("C320", this.treeLayer.getDataValueByPosition(4, 3));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 3).getRowSpan());
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("318", this.treeLayer.getDataValueByPosition(4, 6));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 6).getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 7));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 7).getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 9));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 9).getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 7));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("312", this.treeLayer.getDataValueByPosition(4, 11));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 11).getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 12));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 13).getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 14));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 14).getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 12));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));

        // sort by identifier
        this.sortModel.sort(3, SortDirectionEnum.DESC, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("C320", this.treeLayer.getDataValueByPosition(4, 0));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 0).getRowSpan());
        assertEquals("C220", this.treeLayer.getDataValueByPosition(4, 2));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 2).getRowSpan());
        assertEquals("C200", this.treeLayer.getDataValueByPosition(4, 3));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 3).getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 6));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 6).getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 8));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 8).getRowSpan());
        assertEquals("318", this.treeLayer.getDataValueByPosition(4, 10));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 10).getRowSpan());
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 7));
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 9));
        assertNull(this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 11));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 11).getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 13));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 13).getRowSpan());
        assertEquals("312", this.treeLayer.getDataValueByPosition(4, 15));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 15).getRowSpan());
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 12));
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 14));
        assertNull(this.treeLayer.getDataValueByPosition(8, 15));

        // remove sorting again
        this.sortModel.sort(3, SortDirectionEnum.NONE, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("C320", this.treeLayer.getDataValueByPosition(4, 0));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 0).getRowSpan());
        assertEquals("C220", this.treeLayer.getDataValueByPosition(4, 2));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 2).getRowSpan());
        assertEquals("C200", this.treeLayer.getDataValueByPosition(4, 3));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 3).getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 6));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 6).getRowSpan());
        assertEquals("318", this.treeLayer.getDataValueByPosition(4, 8));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 8).getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 9));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 9).getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("320", this.treeLayer.getDataValueByPosition(4, 11));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 11).getRowSpan());
        assertEquals("312", this.treeLayer.getDataValueByPosition(4, 13));
        assertEquals(1, this.treeLayer.getCellByPosition(4, 13).getRowSpan());
        assertEquals("330", this.treeLayer.getDataValueByPosition(4, 14));
        assertEquals(2, this.treeLayer.getCellByPosition(4, 14).getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));
    }

    @Test
    public void shouldOnlySortLastLevel() {
        addCarToInput();

        assertEquals(16, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.getCollapsedNodes().size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());

        ILayerCell cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));

        // sort by comment
        this.sortModel.sort(5, SortDirectionEnum.ASC, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));

        // sort by comment
        this.sortModel.sort(5, SortDirectionEnum.DESC, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", this.treeLayer.getDataValueByPosition(1, 11));
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 15));

        // remove sorting again
        this.sortModel.sort(5, SortDirectionEnum.NONE, false);

        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals("Mercedes", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));
        assertEquals("Dingens", this.treeLayer.getDataValueByPosition(8, 1));
        assertNull(this.treeLayer.getDataValueByPosition(8, 2));
        assertEquals("bar", this.treeLayer.getDataValueByPosition(8, 3));
        assertEquals("foo", this.treeLayer.getDataValueByPosition(8, 4));

        cell = this.treeLayer.getCellByPosition(1, 5);
        assertEquals("McLaren", cell.getDataValue());
        assertEquals(1, cell.getRowSpan());
        assertNull(this.treeLayer.getDataValueByPosition(8, 5));

        cell = this.treeLayer.getCellByPosition(1, 6);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 6));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 7));
        assertNull(this.treeLayer.getDataValueByPosition(8, 8));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 9));
        assertEquals("singsingsing", this.treeLayer.getDataValueByPosition(8, 10));

        cell = this.treeLayer.getCellByPosition(1, 11);
        assertEquals("BMW", cell.getDataValue());
        assertEquals(5, cell.getRowSpan());
        assertEquals("cool", this.treeLayer.getDataValueByPosition(8, 11));
        assertEquals("awesome", this.treeLayer.getDataValueByPosition(8, 12));
        assertNull(this.treeLayer.getDataValueByPosition(8, 13));
        assertEquals("blabla", this.treeLayer.getDataValueByPosition(8, 14));
        assertEquals("sing", this.treeLayer.getDataValueByPosition(8, 15));
    }

    private void addCarToInput() {
        // add another car that shares the same values as an existing car
        Car car4 = new Car("BMW", "3er");
        Motor motor41 = new Motor("320", "140", "KW", 235);
        Feedback order411 = new Feedback(new Date(), Classification.POSITIVE, "cool");
        Feedback order412 = new Feedback(new Date(), Classification.POSITIVE, "awesome");
        motor41.setFeedbacks(Arrays.asList(order411, order412));

        Motor motor42 = new Motor("312", "100", "KW", 210);

        Motor motor43 = new Motor("330", "185", "KW", 250);
        Feedback order431 = new Feedback(new Date(), Classification.POSITIVE, "blabla");
        Feedback order432 = new Feedback(new Date(), Classification.POSITIVE, "sing");
        motor43.setFeedbacks(Arrays.asList(order431, order432));

        car4.setMotors(Arrays.asList(motor41, motor42, motor43));

        this.input.add(car4);

        List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(this.input, false, CarService.PROPERTY_NAMES_COMPACT);
        this.eventList.clear();
        this.eventList.addAll(data);

        this.treeLayer.doCommand(new StructuralRefreshCommand());
    }
}
