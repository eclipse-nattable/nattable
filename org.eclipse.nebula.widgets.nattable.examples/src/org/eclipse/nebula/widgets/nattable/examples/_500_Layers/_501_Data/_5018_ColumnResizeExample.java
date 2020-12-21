/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._501_Data;

import java.util.HashMap;
import java.util.stream.IntStream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.resize.AutoResizeRowPaintListener;
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _5018_ColumnResizeExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(1024, 400,
                new _5018_ColumnResizeExample());
    }

    @Override
    public String getDescription() {
        return "Example showing how to programmatically resize columns.\n\n"
                + "* AUTO RESIZE - Perform an auto resize on all columns and activate lazy automatic row resizing.\n"
                + "* FIXED DATALAYER - Set columns to some fixed values directly via DataLayer.\n"
                + "* FIXED COMMAND - Set columns to some fixed values via commands. This way only the visible columns are resized.\n"
                + "* PERCENTAGE - Enable percentage sizing for columns and set a min width to avoid that columns vanish if less space is available.\n"
                + "* RESET - Reset the column and row size configuration to the default.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        panel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

        Composite gridPanel = new Composite(panel, SWT.NONE);
        gridPanel.setLayout(layout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);

        Composite buttonPanel = new Composite(panel, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        String[] propertyNames = {
                "firstName",
                "lastName",
                "password",
                "description",
                "age",
                "money",
                "married",
                "gender",
                "address.street",
                "address.city",
                "favouriteFood",
                "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        HashMap<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("password", "Password");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("age", "Age");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.city", "City");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(
                        PersonService.getExtendedPersonsWithAddress(1000),
                        new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames));

        DefaultGridLayer gridLayer =
                new DefaultGridLayer(bodyDataProvider,
                        new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap));

        DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();

        NatTable natTable = new NatTable(gridPanel, gridLayer);
        natTable.setTheme(new ModernNatTableThemeConfiguration());
        natTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        AutoResizeRowPaintListener resizeRowPaintListener =
                new AutoResizeRowPaintListener(natTable, gridLayer.getBodyLayer().getViewportLayer(), bodyDataLayer);

        Button autoResizeButton = new Button(buttonPanel, SWT.PUSH);
        autoResizeButton.setText("Auto resize");
        autoResizeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // first reset to start the size configuration from a clean
                // state
                reset(natTable, bodyDataLayer, resizeRowPaintListener);

                // perform an auto resize for all columns
                natTable.doCommand(
                        new AutoResizeColumnsCommand(
                                natTable,
                                IntStream.range(0, bodyDataLayer.getColumnCount() + 1).toArray()));

                // register the AutoResizeRowPaintListener for lazy auto row
                // resize
                natTable.addPaintListener(resizeRowPaintListener);
            }
        });

        Button fixedSizeButton = new Button(buttonPanel, SWT.PUSH);
        fixedSizeButton.setText("Fixed DataLayer");
        fixedSizeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // first reset to start the size configuration from a clean
                // state
                reset(natTable, bodyDataLayer, resizeRowPaintListener);

                for (int i = 0; i < bodyDataLayer.getColumnCount(); i++) {
                    // resize columns to fixed values
                    bodyDataLayer.setColumnWidthByPosition(i, (i % 2 == 0) ? 150 : 50);
                }
            }
        });

        Button fixedCommandButton = new Button(buttonPanel, SWT.PUSH);
        fixedCommandButton.setText("Fixed Command");
        fixedCommandButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // first reset to start the size configuration from a clean
                // state
                reset(natTable, bodyDataLayer, resizeRowPaintListener);

                for (int i = natTable.getColumnCount() - 1; i > 0; i--) {
                    // resize columns to fixed values
                    // as we increase the column width we operate from right to
                    // left to avoid issues when columns move out the viewport
                    natTable.doCommand(new ColumnResizeCommand(natTable, i, 150));
                }
            }
        });

        Button percentageSizeButton = new Button(buttonPanel, SWT.PUSH);
        percentageSizeButton.setText("Percentage");
        percentageSizeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // first reset to start the size configuration from a clean
                // state
                reset(natTable, bodyDataLayer, resizeRowPaintListener);

                // specify a min width to avoid that columns vanish
                // because of less space
                bodyDataLayer.setDefaultMinColumnWidth(50);

                // enable column width percentage sizing
                bodyDataLayer.setColumnPercentageSizing(true);

                // execute a ClientAreaResizeCommand to trigger percentage
                // calculation
                // -> only needed if percentage sizing is enabled
                // programmatically at runtime
                natTable.doCommand(new ClientAreaResizeCommand(natTable));
            }
        });

        Button resetResizeButton = new Button(buttonPanel, SWT.PUSH);
        resetResizeButton.setText("Reset");
        resetResizeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                reset(natTable, bodyDataLayer, resizeRowPaintListener);
            }
        });

        return panel;
    }

    private void reset(NatTable natTable, DataLayer bodyDataLayer, AutoResizeRowPaintListener resizeRowPaintListener) {
        // remove the AutoResizeRowPaintListener to avoid lazy automatic
        // row resize
        natTable.removePaintListener(resizeRowPaintListener);

        // disable column width percentage sizing
        bodyDataLayer.setColumnPercentageSizing(false);

        // reset the size configuration to show the default sizes again
        bodyDataLayer.resetColumnWidthConfiguration(true);
        bodyDataLayer.resetRowHeightConfiguration(true);
    }
}
