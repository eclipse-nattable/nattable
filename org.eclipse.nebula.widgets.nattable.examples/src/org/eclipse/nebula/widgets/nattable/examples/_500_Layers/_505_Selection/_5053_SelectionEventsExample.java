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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._505_Selection;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
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
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example showing custom selection event handling in a NatTable grid
 * composition.
 */
public class _5053_SelectionEventsExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _5053_SelectionEventsExample());
    }

    @Override
    public String getDescription() {
        return "This example shows custom selection event handling in a NatTable grid composition."
                + " There is different handling for cell, column and row selection.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday" };

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
        final IRowDataProvider<Person> bodyDataProvider =
                new ListDataProvider<>(data, columnPropertyAccessor);
        final DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);
        final SelectionLayer selectionLayer =
                new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        new DataLayer(columnHeaderDataProvider),
                        viewportLayer,
                        selectionLayer);

        // create the row header layer stack
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DefaultRowHeaderDataLayer(
                        new DefaultRowHeaderDataProvider(bodyDataProvider)),
                viewportLayer,
                selectionLayer);

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(
                new DataLayer(
                        new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)),
                rowHeaderLayer,
                columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        columnHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        final NatTable natTable = new NatTable(parent, gridLayer);

        // Events are fired whenever selection occurs. These can be use to
        // trigger external actions as required.
        //
        // This adds a custom ILayerListener that will listen and handle
        // selection events on NatTable level
        natTable.addLayerListener(new ILayerListener() {

            // Default selection behavior selects cells by default.
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellSelectionEvent) {
                    CellSelectionEvent cellEvent = (CellSelectionEvent) event;
                    log("Selected cell: ["
                            + cellEvent.getRowPosition()
                            + ", "
                            + cellEvent.getColumnPosition()
                            + "], "
                            + natTable.getDataValueByPosition(
                                    cellEvent.getColumnPosition(),
                                    cellEvent.getRowPosition()));
                } else if (event instanceof ColumnSelectionEvent) {
                    ColumnSelectionEvent columnEvent = (ColumnSelectionEvent) event;
                    log("Selected Column: " + columnEvent.getColumnPositionRanges());
                } else if (event instanceof RowSelectionEvent) {
                    // directly ask the SelectionLayer about the selected rows
                    // and access the data via IRowDataProvider
                    Collection<Range> selections = selectionLayer.getSelectedRowPositions();
                    StringBuilder builder = new StringBuilder("Selected Persons: ")
                            .append(selectionLayer.getSelectedRowPositions())
                            .append("[");
                    for (Range r : selections) {
                        for (int i = r.start; i < r.end; i++) {
                            Person p = bodyDataProvider.getRowObject(i);
                            if (p != null) {
                                if (!builder.toString().endsWith("[")) {
                                    builder.append(", ");
                                }
                                builder.append(p.getFirstName())
                                        .append(" ")
                                        .append(p.getLastName());
                            }
                        }
                    }
                    builder.append("]");
                    log(builder.toString());
                }
            }
        });

        // Layout widgets
        parent.setLayout(new GridLayout(1, true));
        natTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        // add a log area to the example to show the log entries
        setupTextArea(parent);

        return natTable;
    }

}
