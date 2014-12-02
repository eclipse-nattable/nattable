/*******************************************************************************
 * Copyright (c) 2012, 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._103_Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.GlazedListsGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class DynamicColumnExample extends AbstractNatExample {

    private List<String> columns = new ArrayList<String>();

    private EventList<Map<String, String>> values = GlazedLists
            .eventList(new ArrayList<Map<String, String>>());

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 650, new DynamicColumnExample());
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // start with 3 columns
        this.columns.add("Column_0");
        this.columns.add("Column_1");
        this.columns.add("Column_2");

        this.values.add(createValueRow("Homer"));
        this.values.add(createValueRow("Marge"));
        this.values.add(createValueRow("Bart"));
        this.values.add(createValueRow("Lisa"));
        this.values.add(createValueRow("Maggie"));

        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonPanel);

        ConfigRegistry configRegistry = new ConfigRegistry();

        final GlazedListsGridLayer<Map<String, String>> glazedListsGridLayer = new GlazedListsGridLayer<Map<String, String>>(
                this.values, new MyColumnPropertyAccessor(),
                new SimpleColumnHeaderDataProvider(), configRegistry, true);

        final NatTable natTable = new NatTable(gridPanel, glazedListsGridLayer,
                false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
        addColumnButton.setText("add column");
        addColumnButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String newColumn = "Column_" + DynamicColumnExample.this.columns.size();
                DynamicColumnExample.this.columns.add(newColumn);

                for (Map<String, String> value : DynamicColumnExample.this.values) {
                    String prefix = value.get("Column_0");
                    prefix = prefix.substring(0, prefix.indexOf("_"));
                    value.put(newColumn, prefix + "_" + (DynamicColumnExample.this.columns.size() - 1));
                }

                glazedListsGridLayer.getBodyDataLayer().fireLayerEvent(
                        new ColumnInsertEvent(glazedListsGridLayer
                                .getBodyDataLayer(), DynamicColumnExample.this.columns.size() - 1));
            }
        });

        natTable.doCommand(new ColumnHideCommand(glazedListsGridLayer
                .getBodyLayer(), 1));

        return panel;
    }

    private Map<String, String> createValueRow(String value) {
        Map<String, String> valueRow = new HashMap<String, String>();

        for (int i = 0; i < this.columns.size(); i++) {
            String column = this.columns.get(i);
            valueRow.put(column, value + "_" + i);
        }

        return valueRow;
    }

    class MyColumnPropertyAccessor implements
            IColumnPropertyAccessor<Map<String, String>> {

        @Override
        public Object getDataValue(Map<String, String> rowObject,
                int columnIndex) {
            return rowObject.get(getColumnProperty(columnIndex));
        }

        @Override
        public void setDataValue(Map<String, String> rowObject,
                int columnIndex, Object newValue) {
            rowObject.put(getColumnProperty(columnIndex), newValue.toString());
        }

        @Override
        public int getColumnCount() {
            return DynamicColumnExample.this.columns.size();
        }

        @Override
        public String getColumnProperty(int columnIndex) {
            return DynamicColumnExample.this.columns.get(columnIndex);
        }

        @Override
        public int getColumnIndex(String propertyName) {
            return DynamicColumnExample.this.columns.indexOf(propertyName);
        }
    }

    class SimpleColumnHeaderDataProvider implements IDataProvider {

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return "Column " + (columnIndex + 1); //$NON-NLS-1$
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return DynamicColumnExample.this.columns.size();
        }

        @Override
        public int getRowCount() {
            return 1;
        }

    }
}