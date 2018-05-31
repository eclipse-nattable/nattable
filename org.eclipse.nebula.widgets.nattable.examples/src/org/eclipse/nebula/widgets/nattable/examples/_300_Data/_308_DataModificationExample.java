/*******************************************************************************
 * Copyright (c) 2016, 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
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
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
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
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
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

        // mapping from property to label, needed for column header labels
        Map<String, String> addressPropertyToLabelMap = new HashMap<>();
        addressPropertyToLabelMap.put("street", "Street");
        addressPropertyToLabelMap.put("housenumber", "Housenumber");
        addressPropertyToLabelMap.put("postalCode", "Postal Code");
        addressPropertyToLabelMap.put("city", "City");

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
                new DeleteRowCommandHandler<>(bodyDataProvider.getList()));
        bodyDataLayer.registerCommandHandler(
                new InsertRowCommandHandler<>(bodyDataProvider.getList()));

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
                            MenuItem deleteRow = new MenuItem(popupMenu, SWT.PUSH);
                            deleteRow.setText("Insert below");
                            deleteRow.setEnabled(true);

                            deleteRow.addSelectionListener(new SelectionAdapter() {
                                @Override
                                public void widgetSelected(SelectionEvent event) {
                                    int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                    natTable.doCommand(new InsertRowCommand<>(bodyDataLayer, rowPosition, PersonService.getPersons(1).get(0)));
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
                                        selectionLayer.doCommand(new DeleteRowCommand(selectionLayer, selectedRowPositions));
                                    } else {
                                        int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                        natTable.doCommand(new DeleteRowCommand(natTable, rowPosition));
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

    /**
     * Command to insert a row.
     */
    class InsertRowCommand<T> extends AbstractRowCommand {

        private final T object;

        public InsertRowCommand(ILayer layer, int rowPosition, T object) {
            super(layer, rowPosition);
            this.object = object;
        }

        protected InsertRowCommand(InsertRowCommand<T> command) {
            super(command);
            this.object = command.object;
        }

        @Override
        public ILayerCommand cloneCommand() {
            return new InsertRowCommand<>(this);
        }

        public T getObject() {
            return this.object;
        }

    }

    /**
     * The command handler for inserting a row.
     *
     * @param <T>
     */
    @SuppressWarnings("rawtypes")
    class InsertRowCommandHandler<T> implements ILayerCommandHandler<InsertRowCommand> {

        private List<T> bodyData;

        public InsertRowCommandHandler(List<T> bodyData) {
            this.bodyData = bodyData;
        }

        @Override
        public Class<InsertRowCommand> getCommandClass() {
            return InsertRowCommand.class;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean doCommand(ILayer targetLayer, InsertRowCommand command) {
            // convert the transported position to the target layer
            if (command.convertToTargetLayer(targetLayer)) {
                // remove the element
                this.bodyData.add(command.getRowPosition(), (T) command.getObject());
                // fire the event to refresh
                targetLayer.fireLayerEvent(new RowInsertEvent(targetLayer, command.getRowPosition()));
                return true;
            }
            return false;
        }

    }

    /**
     * Command to delete rows.
     */
    class DeleteRowCommand extends AbstractMultiRowCommand {

        public DeleteRowCommand(ILayer layer, int... rowPositions) {
            super(layer, rowPositions);
        }

        protected DeleteRowCommand(DeleteRowCommand command) {
            super(command);
        }

        @Override
        public ILayerCommand cloneCommand() {
            return new DeleteRowCommand(this);
        }

    }

    /**
     * The command handler for deleting a row.
     *
     * @param <T>
     */
    class DeleteRowCommandHandler<T> implements ILayerCommandHandler<DeleteRowCommand> {

        private List<T> bodyData;

        public DeleteRowCommandHandler(List<T> bodyData) {
            this.bodyData = bodyData;
        }

        @Override
        public Class<DeleteRowCommand> getCommandClass() {
            return DeleteRowCommand.class;
        }

        @Override
        public boolean doCommand(ILayer targetLayer, DeleteRowCommand command) {
            // convert the transported position to the target layer
            if (command.convertToTargetLayer(targetLayer)) {
                int[] positions = command.getRowPositions().stream().mapToInt(i -> i).toArray();
                Arrays.sort(positions);
                for (int i = positions.length - 1; i >= 0; i--) {
                    // remove the element
                    int pos = positions[i];
                    this.bodyData.remove(pos);
                }
                // fire the event to refresh
                targetLayer.fireLayerEvent(new RowDeleteEvent(targetLayer, PositionUtil.getRanges(positions)));
                return true;
            }
            return false;
        }

    }

}