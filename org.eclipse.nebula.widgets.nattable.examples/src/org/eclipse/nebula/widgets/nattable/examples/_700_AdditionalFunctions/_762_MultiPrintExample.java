/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._700_AdditionalFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
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
import org.eclipse.nebula.widgets.nattable.painter.NatTableBorderOverlayPainter;
import org.eclipse.nebula.widgets.nattable.print.LayerPrinter;
import org.eclipse.nebula.widgets.nattable.print.command.PrintCommand;
import org.eclipse.nebula.widgets.nattable.print.command.PrintCommandHandler;
import org.eclipse.nebula.widgets.nattable.print.config.DefaultPrintBindings;
import org.eclipse.nebula.widgets.nattable.print.config.PrintConfigAttributes;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _762_MultiPrintExample extends AbstractNatExample {

    Button joinTablesButton;
    Button repeatHeaderTableButton;
    Button repeatColumnHeaderButton;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _762_MultiPrintExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to trigger printing of multiple NatTable instances in one print job";
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
        buttonPanel.setLayout(new GridLayout(4, false));
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        NatTable headerTable = createSmallTable(gridPanel);
        NatTable bodyTable = createGrid(gridPanel);

        // create a custom command handler for printing of multiple NatTable
        // instances
        PrintCommandHandler handler = new PrintCommandHandler(headerTable.getLayer()) {
            @Override
            public boolean doCommand(PrintCommand command) {
                LayerPrinter printer = new LayerPrinter(headerTable, headerTable.getConfigRegistry(), _762_MultiPrintExample.this.repeatHeaderTableButton.getSelection());
                printer.joinPrintTargets(_762_MultiPrintExample.this.joinTablesButton.getSelection());
                if (_762_MultiPrintExample.this.repeatColumnHeaderButton.getSelection()) {
                    printer.addPrintTarget(bodyTable, ((GridLayer) bodyTable.getLayer()).getColumnHeaderLayer(), bodyTable.getConfigRegistry());
                } else {
                    printer.addPrintTarget(bodyTable, bodyTable.getConfigRegistry());
                }
                printer.print(headerTable.getShell());
                return true;
            };
        };

        // register the handler to both NatTable instances
        headerTable.getLayer().registerCommandHandler(handler);
        bodyTable.getLayer().registerCommandHandler(handler);

        Composite multiTableConfigPanel = new Composite(buttonPanel, SWT.NONE);
        multiTableConfigPanel.setLayout(new RowLayout(SWT.VERTICAL));
        this.joinTablesButton = new Button(multiTableConfigPanel, SWT.CHECK);
        this.joinTablesButton.setText("Join Tables");
        this.repeatHeaderTableButton = new Button(multiTableConfigPanel, SWT.CHECK);
        this.repeatHeaderTableButton.setText("Repeat Header Table");
        this.repeatColumnHeaderButton = new Button(multiTableConfigPanel, SWT.CHECK);
        this.repeatColumnHeaderButton.setText("Repeat Column Header");

        Composite fittingConfigPanel = new Composite(buttonPanel, SWT.NONE);
        fittingConfigPanel.setLayout(new RowLayout(SWT.VERTICAL));
        Button fitHorizontalButton = new Button(fittingConfigPanel, SWT.CHECK);
        fitHorizontalButton.setText("Fit Horizontally");
        Button fitVerticalButton = new Button(fittingConfigPanel, SWT.CHECK);
        fitVerticalButton.setText("Fit Vertically");
        Button stretchButton = new Button(fittingConfigPanel, SWT.CHECK);
        stretchButton.setText("Stretch");
        stretchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                headerTable.getConfigRegistry().registerConfigAttribute(
                        PrintConfigAttributes.STRETCH,
                        stretchButton.getSelection());
            }
        });

        fitHorizontalButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFittingConfig(
                        headerTable.getConfigRegistry(),
                        fitHorizontalButton.getSelection(),
                        fitVerticalButton.getSelection());
            }
        });

        fitVerticalButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateFittingConfig(
                        headerTable.getConfigRegistry(),
                        fitHorizontalButton.getSelection(),
                        fitVerticalButton.getSelection());
            }
        });

        Button addColumnButton = new Button(buttonPanel, SWT.PUSH);
        addColumnButton.setText("Print");
        addColumnButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                headerTable.doCommand(
                        new PrintCommand(
                                headerTable.getConfigRegistry(),
                                headerTable.getShell()));
            }
        });

        return panel;
    }

    private void updateFittingConfig(IConfigRegistry configRegistry, boolean fitHorizontally, boolean fitVertically) {
        Direction dir = Direction.NONE;
        if (fitHorizontally && fitVertically) {
            dir = Direction.BOTH;
        } else if (fitHorizontally && !fitVertically) {
            dir = Direction.HORIZONTAL;
        } else if (!fitHorizontally && fitVertically) {
            dir = Direction.VERTICAL;
        }
        configRegistry.registerConfigAttribute(
                PrintConfigAttributes.FITTING_MODE,
                dir);
    }

    private NatTable createSmallTable(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        IDataProvider bodyDataProvider =
                new DefaultBodyDataProvider<>(
                        PersonService.getPersons(3), propertyNames);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        final NatTable natTable = new NatTable(parent, viewportLayer, false);

        // adding this configuration adds the styles and the painters to use
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DefaultPrintBindings() {
            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                super.configureRegistry(configRegistry);

                configRegistry.registerConfigAttribute(
                        PrintConfigAttributes.FOOTER_PAGE_PATTERN,
                        "Page {0} of {1}");
            }
        });

        natTable.configure();

        natTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        return natTable;
    }

    private NatTable createGrid(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "birthday", "address.street", "address.housenumber",
                "address.postalCode", "address.city" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postal Code");
        propertyToLabelMap.put("address.city", "City");

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and
        // setting the ViewportLayer as underlying layer. But in this case using
        // the ViewportLayer
        // directly as body layer is also working.
        List<PersonWithAddress> data = PersonService.getPersonsWithAddress(100);

        IColumnPropertyAccessor<PersonWithAddress> accessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);
        IDataProvider bodyDataProvider =
                new ListDataProvider<>(data, accessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
        SelectionLayer selectionLayer = new SelectionLayer(columnHideShowLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(parent, gridLayer);

        natTable.addOverlayPainter(new NatTableBorderOverlayPainter());

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        return natTable;

    }
}
