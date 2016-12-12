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
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

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
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that shows how to create a custom IColumnPropertyAccessor that
 * supports dynamic column creation at runtime.
 *
 * @author Dirk Fauth
 *
 */
public class _304_DynamicColumnExample extends AbstractNatExample {

    private List<String> columns = new ArrayList<>();

    private List<Map<String, String>> values = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 650,
                new _304_DynamicColumnExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to create a custom IColumnPropertyAccessor that supports"
                + " dynamic column creation at runtime.";
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

        // create the body layer stack
        IDataProvider bodyDataProvider = new ListDataProvider<>(
                this.values, new MyColumnPropertyAccessor());
        final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(
                bodyDataLayer);

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider = new SimpleColumnHeaderDataProvider();
        ILayer columnHeaderLayer = new ColumnHeaderLayer(new DataLayer(
                columnHeaderDataProvider), bodyLayerStack.getViewportLayer(),
                bodyLayerStack.getSelectionLayer());

        // create the row header layer stack
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                bodyDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(
                new DefaultRowHeaderDataLayer(new DefaultRowHeaderDataProvider(
                        bodyDataProvider)), bodyLayerStack.getViewportLayer(),
                bodyLayerStack.getSelectionLayer());

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(new DataLayer(
                new DefaultCornerDataProvider(columnHeaderDataProvider,
                        rowHeaderDataProvider)), rowHeaderLayer,
                columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer = new GridLayer(bodyLayerStack, columnHeaderLayer,
                rowHeaderLayer, cornerLayer);

        final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
        addColumnButton.setText("Add Column");
        addColumnButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String newColumn = "Column_" + _304_DynamicColumnExample.this.columns.size();
                _304_DynamicColumnExample.this.columns.add(newColumn);

                for (Map<String, String> value : _304_DynamicColumnExample.this.values) {
                    String prefix = value.get("Column_0");
                    prefix = prefix.substring(0, prefix.indexOf("_"));
                    value.put(newColumn, prefix + "_" + (_304_DynamicColumnExample.this.columns.size() - 1));
                }

                bodyDataLayer.fireLayerEvent(new ColumnInsertEvent(
                        bodyDataLayer, _304_DynamicColumnExample.this.columns.size() - 1));
            }
        });

        return panel;
    }

    private Map<String, String> createValueRow(String value) {
        Map<String, String> valueRow = new HashMap<>();

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
            return _304_DynamicColumnExample.this.columns.size();
        }

        @Override
        public String getColumnProperty(int columnIndex) {
            return _304_DynamicColumnExample.this.columns.get(columnIndex);
        }

        @Override
        public int getColumnIndex(String propertyName) {
            return _304_DynamicColumnExample.this.columns.indexOf(propertyName);
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
            return _304_DynamicColumnExample.this.columns.size();
        }

        @Override
        public int getRowCount() {
            return 1;
        }

    }
}