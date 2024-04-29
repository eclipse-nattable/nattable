/*******************************************************************************
 * Copyright (c) 2014, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsStaticFilterStrategy;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByComparator;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByObject;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.SummationGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

public class GroupByDataLayerTest {

    ConfigRegistry configRegistry = new ConfigRegistry();

    GroupByModel groupByModel = new GroupByModel();
    GroupByDataLayer<Person> dataLayer;

    IColumnPropertyAccessor<Person> columnPropertyAccessor;

    SortedList<Person> sortedList;
    ISortModel sortModel;

    FilterList<Person> filterList;
    FilterRowDataProvider<Person> filterRowDataProvider;

    static final String MY_LABEL = "myLabel";

    // property names of the Person class
    String[] propertyNames = { "firstName", "lastName", "money", "gender", "married", "birthday" };

    @BeforeEach
    public void setup() {
        this.groupByModel = new GroupByModel();
        EventList<Person> eventList = GlazedLists.eventList(PersonService.getFixedPersons());
        this.sortedList = new SortedList<>(eventList, null);
        this.filterList = new FilterList<>(this.sortedList);

        this.columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(this.propertyNames);

        this.dataLayer = new GroupByDataLayer<>(this.groupByModel, this.filterList, this.columnPropertyAccessor, this.configRegistry);
        this.dataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
    }

    void addSummaryConfiguration() {
        this.configRegistry.registerConfigAttribute(
                GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
                new SummationGroupBySummaryProvider<>(GroupByDataLayerTest.this.columnPropertyAccessor),
                DisplayMode.NORMAL,
                GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 2);

        this.configRegistry.registerConfigAttribute(
                GroupByConfigAttributes.GROUP_BY_CHILD_COUNT_PATTERN,
                "[{0}] - ({1})");
    }

    void addConditionalStyling() {
        IConfigLabelAccumulator conditional = new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (columnPosition == 2 && configLabels.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT)) {
                    Double value = (Double) GroupByDataLayerTest.this.dataLayer.getDataValueByPosition(
                            columnPosition, rowPosition, configLabels, false);
                    if (value > 800d) {
                        configLabels.addLabelOnTop(MY_LABEL);
                    }
                }
            }
        };

        AggregateConfigLabelAccumulator aggregate = new AggregateConfigLabelAccumulator();
        aggregate.add(new ColumnLabelAccumulator());
        aggregate.add(conditional);

        this.dataLayer.setConfigLabelAccumulator(aggregate);
    }

    void addSortingCapability() {
        this.dataLayer.setComparator(new GroupByComparator<Person>(this.groupByModel, this.columnPropertyAccessor) {
            @Override
            protected boolean isTreeColumn(int columnIndex) {
                // since we don't have a TreeLayer in the test setup, we specify
                // that column index 0 is the tree column
                return columnIndex == 0;
            }
        });

        // the ColumnHeaderDataLayer is needed to retrieve the comparator per
        // column
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(this.propertyNames);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        columnHeaderDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        this.sortModel = new GlazedListsSortModel<>(
                this.sortedList,
                this.columnPropertyAccessor,
                this.configRegistry,
                columnHeaderDataLayer);
        this.dataLayer.initializeTreeComparator(this.sortModel, null, true);

        this.configRegistry.registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR,
                DefaultComparator.getInstance());
    }

    void addFilterCapability() {
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(this.propertyNames);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);

        this.filterRowDataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsStaticFilterStrategy<>(
                        this.filterList,
                        this.columnPropertyAccessor,
                        this.configRegistry),
                columnHeaderDataLayer,
                columnHeaderDataProvider,
                this.configRegistry);

        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                new DefaultDisplayConverter());
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.CONTAINS);

        this.dataLayer.enableFilterSupport(this.filterRowDataProvider);
    }

    @Test
    public void testOneLevelGrouping() {
        assertEquals(18, this.dataLayer.getRowCount());

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // 18 data rows + 2 GroupBy rows
        assertEquals(20, this.dataLayer.getRowCount());

        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
    }

    @Test
    public void testGetElementsInGroupWithNullValue() {
        // Test with another list with null values
        this.sortedList.clear();
        this.sortedList.addAll(PersonService.getFixedPersonsWithNull());

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        // collect GroupBy Objects
        List<GroupByObject> groupByObjects = new ArrayList<>();
        for (Object o : this.dataLayer.getTreeList()) {
            if (o instanceof GroupByObject) {
                groupByObjects.add((GroupByObject) o);
            }
        }

        // test with getElementsInGroup() class GroupDescriptorMatcher.
        // testing like this, cause setup would be complex
        for (GroupByObject o : groupByObjects) {
            this.dataLayer.getItemsInGroup(o);
        }

        // if we get here, there is no NullPointerException in
        // GroupDescriptorMatcher.

    }

    @Test
    public void testTwoLevelGrouping() {
        assertEquals(18, this.dataLayer.getRowCount());

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        this.groupByModel.addGroupByColumnIndex(0);

        // 18 data rows + 2 GroupBy rows lastname + 8 data rows firstname
        assertEquals(28, this.dataLayer.getRowCount());

        // Flanders
        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(1);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Maude", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(4);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Ned", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(7);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Rodd", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(10);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Todd", ((GroupByObject) o).getValue());

        // Simpsons
        o = this.dataLayer.getTreeList().get(13);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(14);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Bart", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(18);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Homer", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(22);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Lisa", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(25);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Marge", ((GroupByObject) o).getValue());
    }

    @Test
    public void testTwoLevelGroupingAddRemoveAll() {
        assertEquals(18, this.dataLayer.getRowCount());

        // groupBy lastname and firstname
        boolean changed = this.groupByModel.addAllGroupByColumnIndexes(1, 0);
        assertTrue(changed);

        changed = this.groupByModel.addAllGroupByColumnIndexes(1, 0);
        assertFalse(changed);

        // 18 data rows + 2 GroupBy rows lastname + 8 data rows firstname
        assertEquals(28, this.dataLayer.getRowCount());

        // Flanders
        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(1);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Maude", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(4);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Ned", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(7);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Rodd", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(10);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Todd", ((GroupByObject) o).getValue());

        // Simpsons
        o = this.dataLayer.getTreeList().get(13);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(14);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Bart", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(18);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Homer", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(22);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Lisa", ((GroupByObject) o).getValue());

        o = this.dataLayer.getTreeList().get(25);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Marge", ((GroupByObject) o).getValue());

        // remove groupBy lastname and firstname
        changed = this.groupByModel.removeAllGroupByColumnIndexes(1, 0);
        assertTrue(changed);

        changed = this.groupByModel.removeAllGroupByColumnIndexes(1, 0);
        assertFalse(changed);

        assertEquals(18, this.dataLayer.getRowCount());
    }

    @Test
    public void testConfigLabelsWithoutGrouping() {
        // there should be never a groupBy label or groupBySummary label
        for (int row = 0; row < this.dataLayer.getRowCount(); row++) {
            for (int column = 0; column < this.dataLayer.getColumnCount(); column++) {
                LabelStack stack = this.dataLayer.getConfigLabelsByPosition(column, row);
                assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column), "column label not found");
                assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label found");
                assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label found");
                assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label found");
                assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label found");
            }
        }
    }

    @Test
    public void testConfigLabelsWithGrouping() {
        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        for (int row = 0; row < this.dataLayer.getRowCount(); row++) {
            for (int column = 0; column < this.dataLayer.getColumnCount(); column++) {
                LabelStack stack = this.dataLayer.getConfigLabelsByPosition(column, row);
                assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column), "column label not found");

                if (row == 0 || row == 9) {
                    // row 0 is groupBy for flanders, row 9 groupBy for simpsons
                    // there should be groupBy labels but no groupBySummary
                    // labels
                    assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label not found");
                    assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label not found");

                    // respect the label order
                    assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.get(0));
                    assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.get(1));
                    assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.get(2));
                } else {
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label found");
                }

                assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label found");
                assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label found");
            }
        }
    }

    @Test
    public void testConfigLabelsWithGroupingAndSummary() {
        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        // add summary configuration
        addSummaryConfiguration();

        for (int row = 0; row < this.dataLayer.getRowCount(); row++) {
            for (int column = 0; column < this.dataLayer.getColumnCount(); column++) {
                LabelStack stack = this.dataLayer.getConfigLabelsByPosition(column, row);
                assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column), "column label not found");

                if (row == 0 || row == 9) {
                    // row 0 is groupBy for flanders, row 9 groupBy for simpsons
                    assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label not found");
                    assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label not found");

                    if (column == 2) {
                        assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label not found");
                        assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label not found");

                        // respect the label order
                        assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column, stack.get(0));
                        assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY, stack.get(1));
                        assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.get(2));
                        assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.get(3));
                        assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.get(4));
                    } else {
                        assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label found");
                        assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label found");

                        // respect the label order
                        assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.get(0));
                        assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.get(1));
                        assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.get(2));
                    }
                } else {
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label found");
                }
            }
        }
    }

    @Test
    public void testConfigLabelsWithGroupingAndSummaryWithConditional() {
        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        // add summary configuration
        addSummaryConfiguration();

        // add conditional styling labels
        addConditionalStyling();

        for (int row = 0; row < this.dataLayer.getRowCount(); row++) {
            for (int column = 0; column < this.dataLayer.getColumnCount(); column++) {
                LabelStack stack = this.dataLayer.getConfigLabelsByPosition(column, row);
                assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column), "column label not found");

                if (row == 0 || row == 9) {
                    // row 0 is groupBy for flanders, row 9 groupBy for simpsons
                    assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label not found");
                    assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label not found");

                    if (column == 2) {
                        assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label not found");
                        assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label not found");

                        // simpsons are more than flanders, so only simpsons
                        // should have conditional formatting
                        if (row == 9) {
                            // respect the label order
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column, stack.get(0));
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY, stack.get(1));
                            assertEquals(MY_LABEL, stack.get(2));
                            assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.get(3));
                            assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.get(4));
                            assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.get(5));
                        } else {
                            // respect the label order
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column, stack.get(0));
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY, stack.get(1));
                            assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.get(2));
                            assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.get(3));
                            assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.get(4));
                        }
                    } else {
                        assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label found");
                        assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label found");

                        // respect the label order
                        assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.get(0));
                        assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.get(1));
                        assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.get(2));
                    }
                } else {
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT), "groupBy object label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column), "groupBy column label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY), "groupBy summary label found");
                    assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column), "groupBy summary column label found");
                }
            }
        }
    }

    @Test
    public void testOneLevelGroupSortTree() {
        addSortingCapability();

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // 18 data rows + 2 GroupBy rows
        assertEquals(20, this.dataLayer.getRowCount());

        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        // unsorted leafs, first leaf in Simpson is Homer
        o = this.dataLayer.getTreeList().get(10);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Homer", ((Person) o).getFirstName());

        // sort ascending
        this.sortModel.sort(0, SortDirectionEnum.ASC, false);

        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        // ascending sorted leafs, first leaf in Simpson is Bart
        o = this.dataLayer.getTreeList().get(10);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Bart", ((Person) o).getFirstName());

        // sort descending
        this.sortModel.sort(0, SortDirectionEnum.DESC, false);

        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(11);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        // descending sorted leafs, first leaf in Flanders is Todd
        o = this.dataLayer.getTreeList().get(12);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Todd", ((Person) o).getFirstName());
    }

    @Test
    public void testOneLevelGroupSortOther() {
        addSortingCapability();

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // 18 data rows + 2 GroupBy rows
        assertEquals(20, this.dataLayer.getRowCount());

        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        // unsorted leafs, Maude is on position 3 within Flanders
        o = this.dataLayer.getTreeList().get(3);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Maude", ((Person) o).getFirstName());

        // sort ascending by gender
        this.sortModel.sort(3, SortDirectionEnum.ASC, false);

        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        // ascending sorted leafs, Maude should be on last position within
        // Flanders
        o = this.dataLayer.getTreeList().get(8);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Maude", ((Person) o).getFirstName());

        // sort descending by gender
        this.sortModel.sort(3, SortDirectionEnum.DESC, false);

        // no changes to tree
        o = this.dataLayer.getTreeList().get(0);
        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        // descending sorted leafs, Maude should be on first position within
        // Flanders
        o = this.dataLayer.getTreeList().get(1);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Maude", ((Person) o).getFirstName());
    }

    @Test
    public void testOneLevelGroupSortSummary() {
        addSortingCapability();
        addSummaryConfiguration();

        // increase the money amount for all flanders to show that the sort
        // order is related to the summary value and not the groupBy value
        double value = 600.0d;
        for (int i = 10; i < this.sortedList.size(); i++) {
            if ((i - 10) % 2 == 0) {
                value -= 100.0d;
            }
            this.sortedList.get(i).setMoney(value);
        }

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // 18 data rows + 2 GroupBy rows
        assertEquals(20, this.dataLayer.getRowCount());

        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        LabelStack labelStack = this.dataLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(2800.0d, this.dataLayer.getDataValueByPosition(2, 0, labelStack, false));

        o = this.dataLayer.getTreeList().get(1);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Ned", ((Person) o).getFirstName());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 1);
        assertEquals(500.0d, this.dataLayer.getDataValueByPosition(2, 1, labelStack, false));

        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 9);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, 9, labelStack, false));

        // sort ascending by money
        this.sortModel.sort(2, SortDirectionEnum.ASC, false);

        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, 0, labelStack, false));

        o = this.dataLayer.getTreeList().get(11);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 11);
        assertEquals(2800.0d, this.dataLayer.getDataValueByPosition(2, 11, labelStack, false));

        o = this.dataLayer.getTreeList().get(12);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Todd", ((Person) o).getFirstName());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 1);
        assertEquals(100.0d, this.dataLayer.getDataValueByPosition(2, 1, labelStack, false));

        // sort descending by money
        this.sortModel.sort(2, SortDirectionEnum.DESC, false);

        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(2800.0d, this.dataLayer.getDataValueByPosition(2, 0, labelStack, false));

        o = this.dataLayer.getTreeList().get(9);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 9);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, 9, labelStack, false));

        o = this.dataLayer.getTreeList().get(1);
        assertTrue(o instanceof Person, "Object is not a Person");
        assertEquals("Ned", ((Person) o).getFirstName());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, 1);
        assertEquals(500.0d, this.dataLayer.getDataValueByPosition(2, 1, labelStack, false));
    }

    @Test
    public void testTwoLevelGroupSortSummary() {
        addSortingCapability();
        addSummaryConfiguration();

        // increase the money amount for all flanders to show that the sort
        // order is related to the summary value and not the groupBy value
        double value = 600.0d;
        for (int i = 10; i < this.sortedList.size(); i++) {
            if ((i - 10) % 2 == 0) {
                value -= 100.0d;
            }
            this.sortedList.get(i).setMoney(value);
        }

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // groupBy firstname
        this.groupByModel.addGroupByColumnIndex(0);
        // 18 data rows + 2 GroupBy rows lastname + 8 data rows firstname
        assertEquals(28, this.dataLayer.getRowCount());

        // Flanders
        int row = 0;
        Object o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        LabelStack labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(2800.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 1;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Maude", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(800.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 4;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Ned", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 7;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Rodd", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(600.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 10;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Todd", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(400.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        // Simpsons
        row = 13;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 14;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Bart", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(300.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 18;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Homer", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(300.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 22;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Lisa", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(200.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 25;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Marge", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(200.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        // sort ascending by money
        this.sortModel.sort(2, SortDirectionEnum.ASC, false);

        // Simpsons
        row = 0;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 1;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Lisa", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(200.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 4;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Marge", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(200.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 7;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Bart", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(300.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 11;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Homer", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(300.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        // Flanders
        row = 15;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(2800.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 16;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Todd", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(400.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 19;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Rodd", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(600.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 22;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Maude", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(800.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 25;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Ned", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        // sort descending by money
        this.sortModel.sort(2, SortDirectionEnum.DESC, false);

        // Flanders
        row = 0;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(2800.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 1;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Ned", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 4;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Maude", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(800.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 7;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Rodd", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(600.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 10;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Todd", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(400.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        // Simpsons
        row = 13;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(1000.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 14;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Homer", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(300.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 18;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Bart", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(300.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 22;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Marge", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(200.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));

        row = 25;
        o = this.dataLayer.getTreeList().get(row);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Lisa", ((GroupByObject) o).getValue());
        labelStack = this.dataLayer.getConfigLabelsByPosition(2, row);
        assertEquals(200.0d, this.dataLayer.getDataValueByPosition(2, row, labelStack, false));
    }

    @Test
    public void testTwoLevelGroupWithCustomComparator() {
        addSortingCapability();

        this.configRegistry.registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR,
                new Comparator<Double>() {

                    @Override
                    public int compare(Double o1, Double o2) {
                        return o2.compareTo(o1);
                    }

                },
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);

        // groupBy money
        this.groupByModel.addGroupByColumnIndex(2);
        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        // if we get here, there is no class cast exception as reported in
        // Bug 459422

    }

    @Test
    public void testKeepSortOnGrouping() {
        addSortingCapability();

        // sort by lastname descending
        this.sortModel.sort(1, SortDirectionEnum.DESC, false);

        // 10 Simpsons, 8 Flanders
        assertEquals(18, this.dataLayer.getRowCount());
        for (int i = 0; i < this.dataLayer.getRowCount(); i++) {
            if (i < 10) {
                assertEquals("Simpson", this.dataLayer.getDataValue(1, i));
            } else {
                assertEquals("Flanders", this.dataLayer.getDataValue(1, i));
            }
        }

        // groupby firstname
        this.groupByModel.addGroupByColumnIndex(0);

        // 10 Simpsons, 8 Flanders, 8 GroupByObjects for firstnames
        assertEquals(26, this.dataLayer.getRowCount());

        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Bart", ((GroupByObject) o).getValue());
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 3));

        o = this.dataLayer.getTreeList().get(4);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Homer", ((GroupByObject) o).getValue());
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 5));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 6));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 7));

        o = this.dataLayer.getTreeList().get(8);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Lisa", ((GroupByObject) o).getValue());
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 10));

        o = this.dataLayer.getTreeList().get(11);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Marge", ((GroupByObject) o).getValue());
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 12));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 13));

        o = this.dataLayer.getTreeList().get(14);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Maude", ((GroupByObject) o).getValue());
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 15));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 16));

        o = this.dataLayer.getTreeList().get(17);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Ned", ((GroupByObject) o).getValue());
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 18));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 19));

        o = this.dataLayer.getTreeList().get(20);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Rodd", ((GroupByObject) o).getValue());
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 21));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 22));

        o = this.dataLayer.getTreeList().get(23);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Todd", ((GroupByObject) o).getValue());
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 24));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 25));
    }

    @Test
    public void testProvidedLabels() {
        Collection<String> labels = this.dataLayer.getProvidedLabels();
        assertEquals(14, labels.size());

        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_OBJECT));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 0));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 1));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 2));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 3));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 4));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 5));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + 0));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + 1));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + 2));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + 3));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + 4));
        assertTrue(labels.contains(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + 5));
    }

    @Test
    public void testGroupByItemCount() {
        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        GroupByObject flanders = (GroupByObject) this.dataLayer.getTreeList().get(0);
        assertEquals("Flanders", flanders.getValue());
        GroupByObject simpsons = (GroupByObject) this.dataLayer.getTreeList().get(9);
        assertEquals("Simpson", simpsons.getValue());

        List<Person> itemsInGroup = this.dataLayer.getItemsInGroup(flanders);
        List<Object> rowModelChildren = this.dataLayer.getTreeRowModel().getChildren(0);
        assertEquals(8, itemsInGroup.size());
        assertEquals(8, rowModelChildren.size());
        assertEquals(itemsInGroup, rowModelChildren);

        itemsInGroup = this.dataLayer.getItemsInGroup(simpsons);
        rowModelChildren = this.dataLayer.getTreeRowModel().getChildren(9);
        assertEquals(10, itemsInGroup.size());
        assertEquals(10, rowModelChildren.size());
        assertEquals(itemsInGroup, rowModelChildren);
    }

    @Test
    public void testGroupByItemCountAfterListChange() {
        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        GroupByObject flanders = (GroupByObject) this.dataLayer.getTreeList().get(0);
        assertEquals("Flanders", flanders.getValue());
        GroupByObject simpsons = (GroupByObject) this.dataLayer.getTreeList().get(9);
        assertEquals("Simpson", simpsons.getValue());

        List<Person> itemsInGroup = this.dataLayer.getItemsInGroup(flanders);
        List<Object> rowModelChildren = this.dataLayer.getTreeRowModel().getChildren(0);
        assertEquals(8, itemsInGroup.size());
        assertEquals(8, rowModelChildren.size());
        assertEquals(itemsInGroup, rowModelChildren);

        this.sortedList.addListEventListener(new ListEventListener<Person>() {

            @Override
            public void listChanged(ListEvent<Person> listChanges) {
                GroupByDataLayerTest.this.dataLayer.clearCache();
            }
        });

        // add new Flanders
        Person p = PersonService.createPersonWithAddress(4711);
        p.setLastName("Flanders");

        this.sortedList.add(p);

        itemsInGroup = this.dataLayer.getItemsInGroup(flanders);
        rowModelChildren = this.dataLayer.getTreeRowModel().getChildren(0);
        assertEquals(9, itemsInGroup.size());
        assertEquals(9, rowModelChildren.size());
        assertEquals(itemsInGroup, rowModelChildren);
    }

    @Test
    public void testRetainCollapsedStateOnGrouping() {
        assertEquals(18, this.dataLayer.getRowCount());

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // 18 data rows + 2 GroupBy rows
        assertEquals(20, this.dataLayer.getRowCount());

        // collapse Flanders
        this.dataLayer.getTreeRowModel().collapse(0);

        // 18 data rows + 2 GroupBy rows - 8 flanders data rows collapsed
        assertEquals(12, this.dataLayer.getRowCount());

        // groupBy firstname
        this.groupByModel.addGroupByColumnIndex(0);

        // 18 data rows + 2 GroupBy rows lastname + 8 data rows firstname
        // - 12 because Flanders is collapsed
        assertEquals(16, this.dataLayer.getRowCount());
    }

    @Test
    public void testClearCollapsedStateOnReGrouping() {
        assertEquals(18, this.dataLayer.getRowCount());

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // groupBy firstname
        this.groupByModel.addGroupByColumnIndex(0);

        // 18 data rows + 2 GroupBy rows lastname + 8 GroupBy rows firstname
        assertEquals(28, this.dataLayer.getRowCount());

        // collapse all Flanders sub-nodes
        this.dataLayer.getTreeRowModel().collapse(10);
        this.dataLayer.getTreeRowModel().collapse(7);
        this.dataLayer.getTreeRowModel().collapse(4);
        this.dataLayer.getTreeRowModel().collapse(1);

        // 18 data rows + 2 GroupBy rows lastname + 8 GroupBy rows firstname - 8
        // flanders data rows collapsed
        assertEquals(20, this.dataLayer.getRowCount());

        // ungroup firstname
        this.groupByModel.removeGroupByColumnIndex(0);

        // 18 data rows + 2 GroupBy rows
        assertEquals(20, this.dataLayer.getRowCount());

        // re-group firstname - new firstname groups start expanded
        this.groupByModel.addGroupByColumnIndex(0);
        assertEquals(28, this.dataLayer.getRowCount());
    }

    @Test
    public void shouldKeepInitialOrderOnFilteredGrouping() {
        addFilterCapability();

        assertEquals(18, this.dataLayer.getRowCount());
        assertEquals("Homer", this.dataLayer.getDataValue(0, 0));

        // apply a filter
        this.filterRowDataProvider.setDataValue(0, 0, "Homer");
        assertEquals(3, this.dataLayer.getRowCount());

        // group by last name
        this.groupByModel.addGroupByColumnIndex(1);

        assertEquals(4, this.dataLayer.getRowCount());
        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        // ungroup lastname
        this.groupByModel.removeGroupByColumnIndex(1);
        assertEquals(3, this.dataLayer.getRowCount());

        // remove the filter
        this.filterRowDataProvider.setDataValue(0, 0, null);
        assertEquals(18, this.dataLayer.getRowCount());

        // Homer should be still at the first position
        // without setting the FilterRowDataProvider to the GroupByDataLayer,
        // this will fail
        assertEquals("Homer", this.dataLayer.getDataValue(0, 0));
    }

    @Test
    public void shouldGetCustomConfigLabelsByRowIndex() {
        // add a config label accumulator that uses the row index
        this.dataLayer.setConfigLabelAccumulator((configLabels, columnPosition, rowPosition) -> configLabels.add("ROW_" + rowPosition));

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);

        LabelStack stack = this.dataLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(stack.hasLabel("ROW_0"));
        assertTrue(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));

        stack = this.dataLayer.getConfigLabelsByPosition(0, 1);
        assertTrue(stack.hasLabel("ROW_1"));
        assertFalse(stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
    }

    @Test
    public void shouldSwitchGroupingTypesOnLoad() {
        // change the birthday to get reliable results
        @SuppressWarnings("deprecation")
        Date temp1 = new Date(1978, 9, 13);
        this.filterList.get(0).setBirthday(temp1);
        this.filterList.get(1).setBirthday(temp1);
        this.filterList.get(2).setBirthday(temp1);
        this.filterList.get(3).setBirthday(temp1);
        this.filterList.get(4).setBirthday(temp1);
        this.filterList.get(5).setBirthday(temp1);

        @SuppressWarnings("deprecation")
        Date temp2 = new Date(1976, 1, 24);
        this.filterList.get(6).setBirthday(temp2);
        this.filterList.get(7).setBirthday(temp2);
        this.filterList.get(8).setBirthday(temp2);
        this.filterList.get(9).setBirthday(temp2);

        @SuppressWarnings("deprecation")
        Date temp3 = new Date(2012, 0, 19);
        this.filterList.get(10).setBirthday(temp3);
        this.filterList.get(11).setBirthday(temp3);
        this.filterList.get(12).setBirthday(temp3);
        this.filterList.get(13).setBirthday(temp3);
        this.filterList.get(14).setBirthday(temp3);
        this.filterList.get(15).setBirthday(temp3);
        this.filterList.get(16).setBirthday(temp3);
        this.filterList.get(17).setBirthday(temp3);

        // add a static filter so a list change is propagated on update()
        addFilterCapability();
        ((DefaultGlazedListsStaticFilterStrategy<Person>) this.filterRowDataProvider.getFilterStrategy()).addStaticFilter(item -> !item.getFirstName().equals("Ned"));

        assertEquals(16, this.filterList.size());

        // groupBy lastname
        this.groupByModel.addGroupByColumnIndex(1);
        // 16 data rows + 2 GroupBy rows
        assertEquals(18, this.dataLayer.getRowCount());

        Object o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(7);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        // save state
        Properties props = new Properties();
        this.groupByModel.saveState("lastname", props);

        // clear grouping
        this.groupByModel.clearGroupByColumnIndexes();

        // group by birthday
        this.groupByModel.addGroupByColumnIndex(5);
        // 16 data rows + 3 GroupBy rows
        assertEquals(19, this.dataLayer.getRowCount());

        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals(temp2, ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(5);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals(temp1, ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(12);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals(temp3, ((GroupByObject) o).getValue());

        // save state
        this.groupByModel.saveState("birthday", props);

        // clear grouping
        this.groupByModel.clearGroupByColumnIndexes();

        // load first state
        this.groupByModel.loadState("lastname", props);

        assertEquals(18, this.dataLayer.getRowCount());
        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Flanders", ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(7);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals("Simpson", ((GroupByObject) o).getValue());

        // load second state
        this.groupByModel.loadState("birthday", props);

        assertEquals(19, this.dataLayer.getRowCount());
        o = this.dataLayer.getTreeList().get(0);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals(temp2, ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(5);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals(temp1, ((GroupByObject) o).getValue());
        o = this.dataLayer.getTreeList().get(12);
        assertTrue(o instanceof GroupByObject, "Object is not a GroupByObject");
        assertEquals(temp3, ((GroupByObject) o).getValue());
    }

    @Test
    public void shouldUpdateGroupByModelViaAPI() {
        this.groupByModel.addGroupByColumnIndex(1);
        assertEquals(Arrays.asList(1), this.groupByModel.getGroupByColumnIndexes());

        this.groupByModel.addAllGroupByColumnIndexes(4, 0);
        assertEquals(Arrays.asList(1, 4, 0), this.groupByModel.getGroupByColumnIndexes());

        this.groupByModel.setGroupByColumnIndexes(0, 2);
        assertEquals(Arrays.asList(0, 2), this.groupByModel.getGroupByColumnIndexes());

        this.groupByModel.clearGroupByColumnIndexes();
        assertTrue(this.groupByModel.getGroupByColumnIndexes().isEmpty());
    }
}
