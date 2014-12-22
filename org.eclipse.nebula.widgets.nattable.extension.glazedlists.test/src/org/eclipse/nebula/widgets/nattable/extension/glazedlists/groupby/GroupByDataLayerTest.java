/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.SummationGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.data.Person;
import org.eclipse.nebula.widgets.nattable.test.data.PersonService;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class GroupByDataLayerTest {

    ConfigRegistry configRegistry = new ConfigRegistry();

    GroupByModel groupByModel = new GroupByModel();
    GroupByDataLayer<Person> dataLayer;

    IColumnPropertyAccessor<Person> columnPropertyAccessor;

    static final String MY_LABEL = "myLabel";

    @Before
    public void setup() {
        this.groupByModel = new GroupByModel();
        EventList<Person> eventList = GlazedLists.eventList(PersonService.getFixedPersons());

        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "money", "gender", "married", "birthday" };

        this.columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Person>(propertyNames);

        this.dataLayer = new GroupByDataLayer<Person>(this.groupByModel, eventList, this.columnPropertyAccessor, this.configRegistry);
        this.dataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
    }

    void addSummaryConfiguration() {
        this.configRegistry.registerConfigAttribute(
                GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
                new SummationGroupBySummaryProvider<Person>(GroupByDataLayerTest.this.columnPropertyAccessor),
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

        AggregrateConfigLabelAccumulator aggregate = new AggregrateConfigLabelAccumulator();
        aggregate.add(new ColumnLabelAccumulator());
        aggregate.add(conditional);

        this.dataLayer.setConfigLabelAccumulator(aggregate);
    }

    @Test
    public void testConfigLabelsWithoutGrouping() {
        // there should be never a groupBy label or groupBySummary label
        for (int row = 0; row < this.dataLayer.getRowCount(); row++) {
            for (int column = 0; column < this.dataLayer.getColumnCount(); column++) {
                LabelStack stack = this.dataLayer.getConfigLabelsByPosition(column, row);
                assertTrue("column label not found", stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column));
                assertFalse("groupBy object label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                assertFalse("groupBy column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));
                assertFalse("groupBy summary label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                assertFalse("groupBy summary column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));
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
                assertTrue("column label not found", stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column));

                if (row == 0 || row == 9) {
                    // row 0 is groupBy for flanders, row 9 groupBy for simpsons
                    // there should be groupBy labels but no groupBySummary
                    // labels
                    assertTrue("groupBy object label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                    assertTrue("groupBy column label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));

                    // respect the label order
                    assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.getLabels().get(0));
                    assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.getLabels().get(1));
                    assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.getLabels().get(2));
                }
                else {
                    assertFalse("groupBy object label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                    assertFalse("groupBy column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));
                }

                assertFalse("groupBy summary label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                assertFalse("groupBy summary column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));
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
                assertTrue("column label not found", stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column));

                if (row == 0 || row == 9) {
                    // row 0 is groupBy for flanders, row 9 groupBy for simpsons
                    assertTrue("groupBy object label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                    assertTrue("groupBy column label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));

                    if (column == 2) {
                        assertTrue("groupBy summary label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                        assertTrue("groupBy summary column label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));

                        // respect the label order
                        assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column, stack.getLabels().get(0));
                        assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY, stack.getLabels().get(1));
                        assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.getLabels().get(2));
                        assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.getLabels().get(3));
                        assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.getLabels().get(4));
                    }
                    else {
                        assertFalse("groupBy summary label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                        assertFalse("groupBy summary column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));

                        // respect the label order
                        assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.getLabels().get(0));
                        assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.getLabels().get(1));
                        assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.getLabels().get(2));
                    }
                }
                else {
                    assertFalse("groupBy object label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                    assertFalse("groupBy column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));
                    assertFalse("groupBy summary label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                    assertFalse("groupBy summary column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));
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
                assertTrue("column label not found", stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column));

                if (row == 0 || row == 9) {
                    // row 0 is groupBy for flanders, row 9 groupBy for simpsons
                    assertTrue("groupBy object label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                    assertTrue("groupBy column label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));

                    if (column == 2) {
                        assertTrue("groupBy summary label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                        assertTrue("groupBy summary column label not found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));

                        // simpsons are more than flanders, so only simpsons
                        // should have conditional formatting
                        if (row == 9) {
                            // respect the label order
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column, stack.getLabels().get(0));
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY, stack.getLabels().get(1));
                            assertEquals(MY_LABEL, stack.getLabels().get(2));
                            assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.getLabels().get(3));
                            assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.getLabels().get(4));
                            assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.getLabels().get(5));
                        }
                        else {
                            // respect the label order
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column, stack.getLabels().get(0));
                            assertEquals(GroupByDataLayer.GROUP_BY_SUMMARY, stack.getLabels().get(1));
                            assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.getLabels().get(2));
                            assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.getLabels().get(3));
                            assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.getLabels().get(4));
                        }
                    }
                    else {
                        assertFalse("groupBy summary label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                        assertFalse("groupBy summary column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));

                        // respect the label order
                        assertEquals(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column, stack.getLabels().get(0));
                        assertEquals(GroupByDataLayer.GROUP_BY_OBJECT, stack.getLabels().get(1));
                        assertEquals(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + column, stack.getLabels().get(2));
                    }
                }
                else {
                    assertFalse("groupBy object label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT));
                    assertFalse("groupBy column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + column));
                    assertFalse("groupBy summary label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY));
                    assertFalse("groupBy summary column label found", stack.hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY_COLUMN_PREFIX + column));
                }
            }
        }
    }
}
