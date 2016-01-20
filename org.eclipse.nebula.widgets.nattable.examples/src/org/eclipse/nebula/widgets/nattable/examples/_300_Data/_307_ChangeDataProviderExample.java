/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
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
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that shows how to exchange the IDataProvider at runtime. This feature
 * allows to exchange the shown data structure without the need to re-create the
 * layer stack.
 */
public class _307_ChangeDataProviderExample extends AbstractNatExample {

    private IDataProvider personBodyDataProvider;
    private IDataProvider personColumnHeaderDataProvider;
    private IDataProvider personRowHeaderDataProvider;
    private IConfigLabelAccumulator personAccumulator = new IConfigLabelAccumulator() {

        @Override
        public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
            switch (columnPosition) {
                case 3:
                    configLabels.addLabel("MARRIED");
                    break;
                case 4:
                    configLabels.addLabel("DATE");
                    break;
            }
        }
    };

    private IDataProvider addressBodyDataProvider;
    private IDataProvider addressColumnHeaderDataProvider;
    private IDataProvider addressRowHeaderDataProvider;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 650, new _307_ChangeDataProviderExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to exchange the IDataProvider at runtime. "
                + "This feature allows to exchange the shown data structure without the need to re-create the layer stack.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // set the GridLayout because the FillLayout seems to introduce a
        // scrollbar rendering issue on changing the content
        parent.setLayout(new GridLayout());

        // property names of the Person class
        String[] personPropertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday" };

        // mapping from property to label, needed for column header labels
        Map<String, String> personPropertyToLabelMap = new HashMap<String, String>();
        personPropertyToLabelMap.put("firstName", "Firstname");
        personPropertyToLabelMap.put("lastName", "Lastname");
        personPropertyToLabelMap.put("gender", "Gender");
        personPropertyToLabelMap.put("married", "Married");
        personPropertyToLabelMap.put("birthday", "Birthday");

        this.personBodyDataProvider = new ListDataProvider<>(
                PersonService.getPersons(10),
                new ReflectiveColumnPropertyAccessor<Person>(personPropertyNames));

        this.personColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(personPropertyNames, personPropertyToLabelMap);

        this.personRowHeaderDataProvider = new DefaultRowHeaderDataProvider(this.personBodyDataProvider);

        // property names of the Address class
        String[] addressPropertyNames = {
                "street",
                "housenumber",
                "postalCode",
                "city" };

        // mapping from property to label, needed for column header labels
        Map<String, String> addressPropertyToLabelMap = new HashMap<String, String>();
        addressPropertyToLabelMap.put("street", "Street");
        addressPropertyToLabelMap.put("housenumber", "Housenumber");
        addressPropertyToLabelMap.put("postalCode", "Postal Code");
        addressPropertyToLabelMap.put("city", "City");

        this.addressBodyDataProvider = new ListDataProvider<>(
                PersonService.getAddress(20),
                new ReflectiveColumnPropertyAccessor<>(addressPropertyNames));

        this.addressColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(addressPropertyNames, addressPropertyToLabelMap);

        this.addressRowHeaderDataProvider = new DefaultRowHeaderDataProvider(this.addressBodyDataProvider);

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
        DataLayer bodyDataLayer = new DataLayer(this.personBodyDataProvider);
        bodyDataLayer.setConfigLabelAccumulator(this.personAccumulator);
        DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(bodyDataLayer);

        // create the column header layer stack
        DataLayer columnHeaderDataLayer = new DataLayer(this.personColumnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                columnHeaderDataLayer,
                bodyLayerStack.getViewportLayer(),
                bodyLayerStack.getSelectionLayer());

        // create the row header layer stack
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(this.personRowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(
                rowHeaderDataLayer,
                bodyLayerStack.getViewportLayer(),
                bodyLayerStack.getSelectionLayer());

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(new DataLayer(
                new DefaultCornerDataProvider(
                        this.personColumnHeaderDataProvider,
                        this.personRowHeaderDataProvider)),
                rowHeaderLayer,
                columnHeaderLayer);

        // create the grid layer composed with the prior created layer stacks
        GridLayer gridLayer = new GridLayer(
                bodyLayerStack,
                columnHeaderLayer,
                rowHeaderLayer,
                cornerLayer);

        final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
        natTable.addConfiguration(new SingleClickSortConfiguration());

        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new CheckBoxPainter(),
                        DisplayMode.NORMAL,
                        "MARRIED");
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        new DefaultDateDisplayConverter("yyyy-MM-dd"),
                        DisplayMode.NORMAL,
                        "DATE");
            }
        });

        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Button showPersonsButton = new Button(buttonPanel, SWT.PUSH);
        showPersonsButton.setText("Show Persons");
        showPersonsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                bodyDataLayer.setDataProvider(_307_ChangeDataProviderExample.this.personBodyDataProvider);
                columnHeaderDataLayer.setDataProvider(_307_ChangeDataProviderExample.this.personColumnHeaderDataProvider);
                rowHeaderDataLayer.setDataProvider(_307_ChangeDataProviderExample.this.personRowHeaderDataProvider);

                bodyDataLayer.setConfigLabelAccumulator(_307_ChangeDataProviderExample.this.personAccumulator);

                natTable.refresh();

                natTable.getHorizontalBar().setVisible(false);
                natTable.getVerticalBar().setVisible(false);
            }
        });

        Button showAddressButton = new Button(buttonPanel, SWT.PUSH);
        showAddressButton.setText("Show Address");
        showAddressButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                bodyDataLayer.setDataProvider(_307_ChangeDataProviderExample.this.addressBodyDataProvider);
                columnHeaderDataLayer.setDataProvider(_307_ChangeDataProviderExample.this.addressColumnHeaderDataProvider);
                rowHeaderDataLayer.setDataProvider(_307_ChangeDataProviderExample.this.addressRowHeaderDataProvider);

                bodyDataLayer.setConfigLabelAccumulator(null);

                natTable.refresh();
            }
        });

        return panel;
    }

}