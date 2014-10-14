/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._800_Integration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

/**
 * Simple example showing how to add the {@link ColumnGroupHeaderLayer} to the
 * layer composition of a grid and how to add the corresponding actions to the
 * column header menu. This example also adds the ability to sort the data
 * model.
 * 
 * @author Dirk Fauth
 *
 */
public class _806_SortableColumnGroupingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner
                .run(new _806_SortableColumnGroupingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the ColumnGroupHeaderLayer within a grid and "
                + "its corresponding actions in the column header menu. If you perform a right "
                + "click on the column header, you are able to hide the current selected "
                + "column or show all columns again.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<String, String>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        ColumnGroupModel columnGroupModel = new ColumnGroupModel();

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and
        // setting the ViewportLayer as underlying layer. But in this case using
        // the ViewportLayer
        // directly as body layer is also working.

        EventList<Person> persons = GlazedLists.eventList(PersonService
                .getPersons(10));
        SortedList<Person> sortedList = new SortedList<Person>(persons, null);

        IColumnPropertyAccessor<Person> accessor = new ReflectiveColumnPropertyAccessor<Person>(
                propertyNames);
        IDataProvider bodyDataProvider = new ListDataProvider<Person>(
                sortedList, accessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        GlazedListsEventLayer<Person> eventLayer = new GlazedListsEventLayer<Person>(
                bodyDataLayer, sortedList);

        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(
                eventLayer);
        ColumnGroupReorderLayer columnGroupReorderLayer = new ColumnGroupReorderLayer(
                columnReorderLayer, columnGroupModel);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(
                columnGroupReorderLayer);
        ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                columnHideShowLayer, columnGroupModel);
        SelectionLayer selectionLayer = new SelectionLayer(
                columnGroupExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(
                columnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer,
                viewportLayer, selectionLayer);

        ConfigRegistry configRegistry = new ConfigRegistry();
        SortHeaderLayer<Person> sortHeaderLayer = new SortHeaderLayer<Person>(
                columnHeaderLayer, new GlazedListsSortModel<Person>(sortedList,
                        accessor, configRegistry, columnHeaderDataLayer));

        ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(
                sortHeaderLayer, selectionLayer, columnGroupModel);

        // configure the column groups
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Name", 0, 1);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Details", 2, 3, 4);
        columnGroupHeaderLayer.setGroupUnbreakable(1);

        // build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                bodyDataProvider);
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(
                rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                viewportLayer, selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer,
                columnGroupHeaderLayer);

        // build the grid layer
        GridLayer gridLayer = new GridLayer(viewportLayer,
                columnGroupHeaderLayer, rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer, false);

        natTable.setConfigRegistry(configRegistry);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the
        // DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new SingleClickSortConfiguration());

        natTable.configure();

        return natTable;
    }

}
