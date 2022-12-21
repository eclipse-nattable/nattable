/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.filterrow.IFilterStrategy;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.SortModelFixture;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.junit.jupiter.api.Test;

public class ConfigLabelProviderTest {

    IRowDataProvider<Person> bodyDataProvider =
            new ListDataProvider<Person>(
                    PersonService.getPersons(10),
                    new ReflectiveColumnPropertyAccessor<Person>(
                            new String[] { "firstName", "lastName", "gender", "married", "birthday" }));
    DataLayer bodyDataLayer =
            new DataLayer(this.bodyDataProvider);
    IDataProvider columnHeaderDataProvider =
            new DummyColumnHeaderDataProvider(this.bodyDataProvider);
    DataLayer columnHeaderDataLayer =
            new DataLayer(this.columnHeaderDataProvider);
    IDataProvider rowHeaderDataProvider =
            new DefaultRowHeaderDataProvider(this.bodyDataProvider);
    DataLayer rowHeaderDataLayer =
            new DataLayer(this.rowHeaderDataProvider);
    DataLayer cornerDataLayer =
            new DataLayer(new DefaultCornerDataProvider(this.columnHeaderDataProvider, this.rowHeaderDataProvider));

    @Test
    public void testProvidedRegionLabels() {
        GridLayer grid = new GridLayer(
                this.bodyDataLayer,
                this.columnHeaderDataLayer,
                this.rowHeaderDataLayer,
                this.cornerDataLayer,
                false);

        Collection<String> labels = grid.getProvidedLabels();
        assertEquals(4, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));

        NatTable natTable = new NatTableFixture(grid);
        labels = natTable.getProvidedLabels();
        assertEquals(4, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));
    }

    @Test
    public void testProvidedGridLabels() {
        GridLayer grid = new GridLayer(
                this.bodyDataLayer,
                this.columnHeaderDataLayer,
                this.rowHeaderDataLayer,
                this.cornerDataLayer);

        Collection<String> labels = grid.getProvidedLabels();
        assertEquals(6, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE));
    }

    @Test
    public void testSelectionLabels() {
        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);

        Collection<String> labels = selectionLayer.getProvidedLabels();
        assertEquals(7, labels.size());

        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));
    }

    @Test
    public void testProvidedGridLabelsColumnGroupHeader() {
        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);
        GridLayer grid = new GridLayer(
                selectionLayer,
                new ColumnGroupHeaderLayer(
                        new ColumnHeaderLayer(this.columnHeaderDataLayer, this.bodyDataLayer, selectionLayer),
                        selectionLayer,
                        new ColumnGroupModel()),
                this.rowHeaderDataLayer,
                this.cornerDataLayer);

        Collection<String> labels = grid.getProvidedLabels();
        assertEquals(16, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));
        assertTrue(labels.contains(DefaultColumnGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultColumnGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE));
    }

    @Test
    public void testProvidedGridLabelsRowGroupHeader() {
        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);
        GridLayer grid = new GridLayer(
                selectionLayer,
                this.columnHeaderDataLayer,
                new RowGroupHeaderLayer<Person>(
                        new RowHeaderLayer(this.rowHeaderDataLayer, this.bodyDataLayer, selectionLayer),
                        selectionLayer,
                        new RowGroupModel<Person>()),
                this.cornerDataLayer);

        Collection<String> labels = grid.getProvidedLabels();
        assertEquals(16, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_GROUP_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));
        assertTrue(labels.contains(DefaultRowGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultRowGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE));
    }

    @Test
    public void testProvidedGridLabelsSortHeader() {
        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);
        GridLayer grid = new GridLayer(
                selectionLayer,
                new SortHeaderLayer<Person>(
                        new ColumnHeaderLayer(this.columnHeaderDataLayer, this.bodyDataLayer, selectionLayer),
                        new SortModelFixture()),
                this.rowHeaderDataLayer,
                this.cornerDataLayer);

        Collection<String> labels = grid.getProvidedLabels();
        assertEquals(19, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "0"));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "1"));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "2"));
    }

    @Test
    public void testProvidedGridLabelsFilterHeader() {
        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);

        GridLayer grid = new GridLayer(
                selectionLayer,
                new FilterRowHeaderComposite<Person>(
                        new IFilterStrategy<Person>() {
                            @Override
                            public void applyFilter(Map<Integer, Object> filterIndexToObjectMap) {
                            }
                        },
                        new SortHeaderLayer<Person>(
                                new ColumnHeaderLayer(this.columnHeaderDataLayer, this.bodyDataLayer, selectionLayer),
                                new SortModelFixture()),
                        this.columnHeaderDataLayer.getDataProvider(),
                        new ConfigRegistry()),
                this.rowHeaderDataLayer,
                this.cornerDataLayer);

        Collection<String> labels = grid.getProvidedLabels();
        assertEquals(26, labels.size());

        assertTrue(labels.contains(GridRegion.CORNER));
        assertTrue(labels.contains(GridRegion.COLUMN_HEADER));
        assertTrue(labels.contains(GridRegion.ROW_HEADER));
        assertTrue(labels.contains(GridRegion.BODY));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_UP_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_DOWN_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "0"));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "1"));
        assertTrue(labels.contains(DefaultSortConfiguration.SORT_SEQ_CONFIG_TYPE + "2"));

        assertTrue(labels.contains("columnHeader"));
        assertTrue(labels.contains(GridRegion.FILTER_ROW));
        assertTrue(labels.contains(FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "0"));
        assertTrue(labels.contains(FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "1"));
        assertTrue(labels.contains(FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "2"));
        assertTrue(labels.contains(FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "3"));
        assertTrue(labels.contains(FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "4"));
    }

    @Test
    public void testTreeLabels() {
        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);
        TreeLayer treeLayer = new TreeLayer(selectionLayer, new TreeRowModel<Person>(
                new ITreeData<Person>() {

                    @Override
                    public int getDepthOfData(Person object) {
                        return 0;
                    }

                    @Override
                    public int getDepthOfData(int index) {
                        return 0;
                    }

                    @Override
                    public Person getDataAtIndex(int index) {
                        return null;
                    }

                    @Override
                    public int indexOf(Person child) {
                        return 0;
                    }

                    @Override
                    public boolean hasChildren(Person object) {
                        return false;
                    }

                    @Override
                    public boolean hasChildren(int index) {
                        return false;
                    }

                    @Override
                    public List<Person> getChildren(Person object) {
                        return null;
                    }

                    @Override
                    public List<Person> getChildren(Person object, boolean fullDepth) {
                        return null;
                    }

                    @Override
                    public List<Person> getChildren(int index) {
                        return null;
                    }

                    @Override
                    public int getElementCount() {
                        return 0;
                    }

                    @Override
                    public boolean isValidIndex(int index) {
                        return false;
                    }
                }));

        Collection<String> labels = treeLayer.getProvidedLabels();
        assertEquals(16, labels.size());

        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));

        assertTrue(labels.contains(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "0"));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "1"));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "2"));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "3"));
        assertTrue(labels.contains(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "4"));
    }

    @Test
    public void testSummaryRowLabels() {
        SummaryRowLayer summaryRowLayer = new SummaryRowLayer(this.bodyDataLayer, new ConfigRegistry(), false);
        SelectionLayer selectionLayer = new SelectionLayer(summaryRowLayer);

        Collection<String> labels = selectionLayer.getProvidedLabels();
        assertEquals(13, labels.size());

        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));

        assertTrue(labels.contains(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL));
        assertTrue(labels.contains(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + "0"));
        assertTrue(labels.contains(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + "1"));
        assertTrue(labels.contains(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + "2"));
        assertTrue(labels.contains(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + "3"));
        assertTrue(labels.contains(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + "4"));
    }

    @Test
    public void testColumnOverrideLabelAccumulator() {
        ColumnOverrideLabelAccumulator accumulator = new ColumnOverrideLabelAccumulator(this.bodyDataLayer);
        accumulator.registerOverrides("all");
        accumulator.registerOverrides("all2");
        accumulator.registerOverrides(2, "two");
        accumulator.registerOverrides(3, "three");

        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(4, labels.size());

        assertTrue(labels.contains("all"));
        assertTrue(labels.contains("all2"));
        assertTrue(labels.contains("two"));
        assertTrue(labels.contains("three"));
    }

    @Test
    public void testRowOverrideLabelAccumulator() {
        RowOverrideLabelAccumulator<Person> accumulator = new RowOverrideLabelAccumulator<Person>(
                this.bodyDataProvider, new IRowIdAccessor<Person>() {

                    @Override
                    public Serializable getRowId(Person rowObject) {
                        return rowObject.getId();
                    }
                });
        accumulator.registerRowOverrides(0, "all");
        accumulator.registerRowOverrides(1, "all2");
        accumulator.registerOverrides(2, "two");
        accumulator.registerOverrides(3, "three");

        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(4, labels.size());

        assertTrue(labels.contains("all"));
        assertTrue(labels.contains("all2"));
        assertTrue(labels.contains("two"));
        assertTrue(labels.contains("three"));
    }

    @Test
    public void testCellOverrideLabelAccumulator() {
        CellOverrideLabelAccumulator<Person> accumulator = new CellOverrideLabelAccumulator<Person>(this.bodyDataProvider);
        accumulator.registerOverride("Simpson", 0, "Donuts");
        accumulator.registerOverride("Bart", 1, "cool");
        accumulator.registerOverrides(2, "two");
        accumulator.registerOverrides(3, "three");

        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(4, labels.size());

        assertTrue(labels.contains("Donuts"));
        assertTrue(labels.contains("cool"));
        assertTrue(labels.contains("two"));
        assertTrue(labels.contains("three"));
    }

    @Test
    public void testLayerAccumulatorIntegrationLabels() {
        ColumnOverrideLabelAccumulator accumulator = new ColumnOverrideLabelAccumulator(this.bodyDataLayer);
        accumulator.registerOverrides("all");
        accumulator.registerOverrides("all2");
        accumulator.registerOverrides(2, "two");
        accumulator.registerOverrides(3, "three");
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        SelectionLayer selectionLayer = new SelectionLayer(this.bodyDataLayer);

        Collection<String> labels = selectionLayer.getProvidedLabels();
        assertEquals(11, labels.size());

        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.SELECTION_ANCHOR_GRID_LINE_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_CELL));
        assertTrue(labels.contains(SelectionStyleLabels.FILL_HANDLE_REGION));
        assertTrue(labels.contains(SelectionStyleLabels.COPY_BORDER_STYLE));

        assertTrue(labels.contains("all"));
        assertTrue(labels.contains("all2"));
        assertTrue(labels.contains("two"));
        assertTrue(labels.contains("three"));
    }

    @Test
    public void testClassNameConfigLabelAccumulator() {
        ClassNameConfigLabelAccumulator accumulator = new ClassNameConfigLabelAccumulator(this.bodyDataProvider);
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(4, labels.size());

        assertTrue(labels.contains("java.lang.String"));
        assertTrue(labels.contains("org.eclipse.nebula.widgets.nattable.dataset.person.Person$Gender"));
        assertTrue(labels.contains("java.lang.Boolean"));
        assertTrue(labels.contains("java.util.Date"));
    }

    @Test
    public void testColumnLabelAccumulator() {
        ColumnLabelAccumulator accumulator = new ColumnLabelAccumulator();
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(0, labels.size());

        accumulator = new ColumnLabelAccumulator(this.bodyDataProvider);
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(5, labels.size());

        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));
    }

    @Test
    public void testAggregateConfigLabelAccumulator() {
        AggregateConfigLabelAccumulator accumulator = new AggregateConfigLabelAccumulator();
        accumulator.add(new ColumnLabelAccumulator(this.bodyDataProvider));
        accumulator.add(new ClassNameConfigLabelAccumulator(this.bodyDataProvider));
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(9, labels.size());

        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));
        assertTrue(labels.contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));

        assertTrue(labels.contains("java.lang.String"));
        assertTrue(labels.contains("org.eclipse.nebula.widgets.nattable.dataset.person.Person$Gender"));
        assertTrue(labels.contains("java.lang.Boolean"));
        assertTrue(labels.contains("java.util.Date"));
    }

    @Test
    public void testBodyOverrideConfigLabelAccumulator() {
        BodyOverrideConfigLabelAccumulator accumulator = new BodyOverrideConfigLabelAccumulator();
        accumulator.addOverride("all");
        accumulator.registerOverrides("all2", "cool");
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(3, labels.size());

        assertTrue(labels.contains("all"));
        assertTrue(labels.contains("all2"));
        assertTrue(labels.contains("cool"));
    }

    @Test
    public void testSimpleConfigLabelAccumulator() {
        SimpleConfigLabelAccumulator accumulator = new SimpleConfigLabelAccumulator("cool");
        this.bodyDataLayer.setConfigLabelAccumulator(accumulator);

        Collection<String> labels = this.bodyDataLayer.getProvidedLabels();
        assertEquals(1, labels.size());

        assertTrue(labels.contains("cool"));
    }

}
