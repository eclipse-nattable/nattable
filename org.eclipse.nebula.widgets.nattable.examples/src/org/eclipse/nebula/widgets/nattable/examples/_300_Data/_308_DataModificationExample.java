/*******************************************************************************
 * Copyright (c) 2016, 2021 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.command.RowDeleteCommand;
import org.eclipse.nebula.widgets.nattable.data.command.RowDeleteCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Example that shows how to add/remove data from the table at runtime.
 */
public class _308_DataModificationExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 650, new _308_DataModificationExample());
    }

    @Override
    public String getDescription() {
        return "This example shows how to add and remove data values at runtime. "
                + "Via right-click on a cell it is possible to insert a new row object below the current selected row or to delete the current selected row.";
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
        Map<String, String> personPropertyToLabelMap = new HashMap<>();
        personPropertyToLabelMap.put("firstName", "Firstname");
        personPropertyToLabelMap.put("lastName", "Lastname");
        personPropertyToLabelMap.put("gender", "Gender");
        personPropertyToLabelMap.put("married", "Married");
        personPropertyToLabelMap.put("birthday", "Birthday");

        ListDataProvider<Person> bodyDataProvider = new ListDataProvider<>(
                PersonService.getPersons(10),
                new ReflectiveColumnPropertyAccessor<Person>(personPropertyNames));

        IDataProvider personColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(personPropertyNames, personPropertyToLabelMap);

        IDataProvider personRowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);

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
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        bodyDataLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

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
        });
        DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(bodyDataLayer);

        bodyDataLayer.registerCommandHandler(
                new RowDeleteCommandHandler<>(bodyDataProvider.getList()));
        bodyDataLayer.registerCommandHandler(
                new RowInsertCommandHandler<>(bodyDataProvider.getList()));

        // create the column header layer stack
        DataLayer columnHeaderDataLayer = new DataLayer(personColumnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                columnHeaderDataLayer,
                bodyLayerStack.getViewportLayer(),
                bodyLayerStack.getSelectionLayer());

        // create the row header layer stack
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(personRowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(
                rowHeaderDataLayer,
                bodyLayerStack.getViewportLayer(),
                bodyLayerStack.getSelectionLayer());

        // create the corner layer stack
        ILayer cornerLayer = new CornerLayer(new DataLayer(
                new DefaultCornerDataProvider(
                        personColumnHeaderDataProvider,
                        personRowHeaderDataProvider)),
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

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {

            private final Menu bodyMenu = new PopupMenuBuilder(natTable)
                    .withMenuItemProvider(new IMenuItemProvider() {

                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem insertRow = new MenuItem(popupMenu, SWT.PUSH);
                            insertRow.setText("Insert below");
                            insertRow.setEnabled(true);

                            insertRow.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent event) {
                                    int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                    int rowIndex = natTable.getRowIndexByPosition(rowPosition);

                                    Person ralph = new Person(bodyDataLayer.getRowCount() + 1, "Ralph", "Wiggum", Gender.MALE, false, new Date());
                                    natTable.doCommand(new RowInsertCommand<>(rowIndex + 1, ralph));
                                }
                            });
                        }
                    })
                    .withMenuItemProvider(new IMenuItemProvider() {

                        @Override
                        public void addMenuItem(NatTable natTable, Menu popupMenu) {
                            MenuItem deleteRow = new MenuItem(popupMenu, SWT.PUSH);
                            deleteRow.setText("Delete");
                            deleteRow.setEnabled(true);

                            deleteRow.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent event) {
                                    SelectionLayer selectionLayer = bodyLayerStack.getSelectionLayer();
                                    int[] selectedRowPositions = PositionUtil.getPositions(selectionLayer.getSelectedRowPositions());

                                    if (selectedRowPositions.length > 0) {
                                        selectionLayer.doCommand(new RowDeleteCommand(selectionLayer, selectedRowPositions));
                                    } else {
                                        int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                        natTable.doCommand(new RowDeleteCommand(natTable, rowPosition));
                                    }
                                }
                            });
                        }
                    })
                    .build();

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerMouseDownBinding(
                        new MouseEventMatcher(
                                SWT.NONE,
                                GridRegion.BODY,
                                MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(this.bodyMenu) {
                            @Override
                            public void run(NatTable natTable, MouseEvent event) {
                                int columnPosition = natTable.getColumnPositionByX(event.x);
                                int rowPosition = natTable.getRowPositionByY(event.y);

                                SelectionLayer selectionLayer = bodyLayerStack.getSelectionLayer();

                                int bodyRowPosition = LayerUtil.convertRowPosition(natTable, rowPosition, selectionLayer);

                                if (!selectionLayer.isRowPositionFullySelected(bodyRowPosition)
                                        && !selectionLayer.isRowPositionSelected(bodyRowPosition)) {
                                    natTable.doCommand(
                                            new SelectRowsCommand(
                                                    natTable,
                                                    columnPosition,
                                                    rowPosition,
                                                    false,
                                                    false));
                                }

                                super.run(natTable, event);
                            }
                        });
            }

        });

        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        return panel;
    }

}