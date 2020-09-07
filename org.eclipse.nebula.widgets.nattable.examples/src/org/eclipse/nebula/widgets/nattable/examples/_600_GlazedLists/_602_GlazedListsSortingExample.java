/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._600_GlazedLists;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.menu.DebugMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

/**
 * Example showing how to add the {@link SortHeaderLayer} to the layer
 * composition of a grid that is using GlazedList for sorting operations.
 */
public class _602_GlazedListsSortingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _602_GlazedListsSortingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the SortHeaderLayer within a grid"
                + " that is using GlazedLists SortedList for sorting.\n"
                + "\n"
                + "Features:\n"
                + "The contents of the grid are kept in sorted order as the rows are added/removed.\n"
                + "Custom comparators can be applied to each column.\n"
                + "Custom comparator applied to the 'Lastname' column that will always sort 'Simpson' at the top.\n"
                + "Sorting is turned off for the 'Gender' column.\n"
                + "\n"
                + "Key bindings:\n"
                + "Sort by left clicking on the column header.\n"
                + "Add columns to the existing sort by (Alt + left click) on the column header\n";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and setting the ViewportLayer as
        // underlying layer. But in this case using the ViewportLayer
        // directly as body layer is also working.

        EventList<Person> persons =
                GlazedLists.eventList(PersonService.getPersons(10));
        SortedList<Person> sortedList =
                new SortedList<>(persons, null);

        IColumnPropertyAccessor<Person> accessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);
        IDataProvider bodyDataProvider =
                new ListDataProvider<>(sortedList, accessor);
        DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);

        GlazedListsEventLayer<Person> eventLayer =
                new GlazedListsEventLayer<>(bodyDataLayer, sortedList);

        ColumnReorderLayer columnReorderLayer =
                new ColumnReorderLayer(eventLayer);
        ColumnHideShowLayer columnHideShowLayer =
                new ColumnHideShowLayer(columnReorderLayer);
        SelectionLayer selectionLayer =
                new SelectionLayer(columnHideShowLayer);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // add default column labels to the label stack
        // need to be done on the column header data layer, otherwise the label
        // stack does not contain the necessary labels at the time the
        // comparator is searched
        columnHeaderDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        ConfigRegistry configRegistry = new ConfigRegistry();

        // add the SortHeaderLayer to the column header layer stack
        // as we use GlazedLists, we use the GlazedListsSortModel which
        // delegates the sorting to the SortedList
        final SortHeaderLayer<Person> sortHeaderLayer =
                new SortHeaderLayer<>(
                        columnHeaderLayer,
                        new GlazedListsSortModel<>(
                                sortedList,
                                accessor,
                                configRegistry,
                                columnHeaderDataLayer),
                        false);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(
                        columnHeaderDataProvider,
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        sortHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        sortHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer, false);

        natTable.setConfigRegistry(configRegistry);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        // override the default sort configuration and change the mouse bindings
        // to sort on a single click
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.addConfiguration(new DebugMenuConfiguration(natTable));

        // add some custom sort configurations regarding comparators
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                // Register custom comparator for last name column
                // when sorting via last name, Simpson will always win
                configRegistry.registerConfigAttribute(
                        SortConfigAttributes.SORT_COMPARATOR,
                        new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {

                                // check the sort order
                                boolean sortDesc = sortHeaderLayer
                                        .getSortModel().getSortDirection(1)
                                        .equals(SortDirectionEnum.DESC);
                                if ("Simpson".equals(o1)
                                        && !"Simpson".equals(o2)) {
                                    return sortDesc ? 1 : -1;
                                } else if (!"Simpson".equals(o1)
                                        && "Simpson".equals(o2)) {
                                    return sortDesc ? -1 : 1;
                                }
                                return o1.compareToIgnoreCase(o2);
                            }
                        },
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1);

                // Register null comparator to disable sorting for gender column
                configRegistry.registerConfigAttribute(
                        SortConfigAttributes.SORT_COMPARATOR,
                        new NullComparator(),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
            }
        });

        natTable.configure();

        return natTable;
    }

}
