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
 *    Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453791
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._505_Selection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultRowSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example showing how selection works in a NatTable grid composition. This
 * example also uses row selection.
 */
public class _5052_RowSelectionExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5052_RowSelectionExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the SelectionLayer in a GridLayer. You will notice the cell selection"
                + " is also covered in the headers.\n"
                + "It also shows how to enable row only selections.";
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

        IColumnPropertyAccessor<Person> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);

        final List<Person> data = PersonService.getPersons(10);

        // create the body layer stack
        IRowDataProvider<Person> bodyDataProvider =
                new ListDataProvider<>(data, columnPropertyAccessor);
        final DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);
        // create a SelectionLayer without using the default configuration
        // this enables us to add the row selection configuration cleanly
        // afterwards
        final SelectionLayer selectionLayer =
                new SelectionLayer(bodyDataLayer, false);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // use a RowSelectionModel that will perform row selections and is able
        // to identify a row via unique ID
        selectionLayer.setSelectionModel(new RowSelectionModel<>(
                selectionLayer, bodyDataProvider, new IRowIdAccessor<Person>() {

                    @Override
                    public Serializable getRowId(Person rowObject) {
                        return rowObject.getId();
                    }

                }));

        // register the DefaultRowSelectionLayerConfiguration that contains the
        // default styling and functionality bindings (search, tick update)
        // and different configurations for a move command handler that always
        // moves by a row and row only selection bindings
        selectionLayer.addConfiguration(new DefaultRowSelectionLayerConfiguration());

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        new DataLayer(columnHeaderDataProvider), viewportLayer, selectionLayer);

        // create the row header layer stack
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        new DefaultRowHeaderDataLayer(
                                new DefaultRowHeaderDataProvider(bodyDataProvider)),
                        viewportLayer,
                        selectionLayer);

        // create the corner layer stack
        ILayer cornerLayer =
                new CornerLayer(
                        new DataLayer(
                                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                        rowHeaderLayer,
                        columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        return new NatTable(parent, gridLayer);
    }

}
