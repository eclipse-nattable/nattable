/*******************************************************************************
 * Copyright (c) 2016, 2023 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.examples._800_Integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;
import org.eclipse.nebula.widgets.nattable.data.command.RowObjectDeleteCommand;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.validate.DefaultDataValidator;
import org.eclipse.nebula.widgets.nattable.dataset.person.Address;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditCellCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.event.InlineCellEditEventHandler;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.data.command.GlazedListsRowInsertCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.data.command.GlazedListsRowObjectDeleteCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.ComboBoxFilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByConfigLabelModifier;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.SummationGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * Example showing how to add the group by feature to the layer composition of a
 * grid in conjunction with showing summary values of groupings. Additionally
 * excel like filters are supported together with the ability to sort.
 */
public class _813_SortableGroupByWithComboBoxFilterExample extends AbstractNatExample {

    private IGroupBySummaryProvider<ExtendedPersonWithAddress> sumMoneySummaryProvider;
    private IGroupBySummaryProvider<ExtendedPersonWithAddress> avgMoneySummaryProvider;

    private boolean useMoneySum = true;

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _813_SortableGroupByWithComboBoxFilterExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the group by feature in conjunction with summary values of the groupings.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        final ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the ExtendedPersonWithAddress class
        String[] propertyNames = { "firstName", "lastName", "gender", "married",
                "age", "birthday", "money",
                "address.street", "address.housenumber", "address.postalCode", "address.city",
                "description", "favouriteFood", "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("age", "Age");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postalcode");
        propertyToLabelMap.put("address.city", "City");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        final IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        // to enable the group by summary feature, the GroupByDataLayer needs to
        // know the ConfigRegistry
        final BodyLayerStack<ExtendedPersonWithAddress> bodyLayerStack =
                new BodyLayerStack<>(
                        PersonService.getExtendedPersonsWithAddress(10000),
                        columnPropertyAccessor,
                        configRegistry);

        bodyLayerStack.getBodyDataLayer().setConfigLabelAccumulator(new ColumnLabelAccumulator());

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

        // add sorting
        GlazedListsSortModel<ExtendedPersonWithAddress> sortModel = new GlazedListsSortModel<>(
                bodyLayerStack.getSortedList(),
                columnPropertyAccessor,
                configRegistry,
                columnHeaderDataLayer);
        SortHeaderLayer<ExtendedPersonWithAddress> sortHeaderLayer = new SortHeaderLayer<>(
                columnHeaderLayer,
                sortModel,
                false);

        // connect sortModel to GroupByDataLayer to support sorting by group by
        // summary values
        bodyLayerStack.getBodyDataLayer().initializeTreeComparator(
                sortHeaderLayer.getSortModel(),
                bodyLayerStack.getTreeLayer(),
                true);

        ComboBoxFilterRowHeaderComposite<ExtendedPersonWithAddress> filterRowHeaderLayer =
                new ComboBoxFilterRowHeaderComposite<>(
                        bodyLayerStack.getFilterList(),
                        bodyLayerStack.getGlazedListsEventLayer(),
                        bodyLayerStack.getSortedList(),
                        columnPropertyAccessor,
                        sortHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry);

        bodyLayerStack.getBodyDataLayer().enableFilterSupport(
                filterRowHeaderLayer.getFilterRowDataLayer().getFilterRowDataProvider());

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, filterRowHeaderLayer);

        // build the grid layer
        GridLayer gridLayer = new GridLayer(bodyLayerStack, filterRowHeaderLayer, rowHeaderLayer, cornerLayer, false);

        // set the group by header on top of the grid
        CompositeLayer compositeGridLayer = new CompositeLayer(1, 2);
        final GroupByHeaderLayer groupByHeaderLayer =
                new GroupByHeaderLayer(bodyLayerStack.getGroupByModel(), gridLayer, columnHeaderDataProvider);
        compositeGridLayer.setChildLayer(GroupByHeaderLayer.GROUP_BY_REGION, groupByHeaderLayer, 0, 0);
        compositeGridLayer.setChildLayer("Grid", gridLayer, 0, 1);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        final NatTable natTable = new NatTable(container, compositeGridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
        // manually
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        // add some additional styling
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new CheckBoxPainter(),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3);

                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        new DefaultDoubleDisplayConverter(),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 6);
            }
        });

        // add sorting configuration
        natTable.addConfiguration(new SingleClickSortConfiguration());

        // add some minor editing support
        compositeGridLayer.addConfiguration(new AbstractLayerConfiguration<AbstractLayer>() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE,
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE,
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1);

                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.DATA_VALIDATOR,
                        new DefaultDataValidator());

                // register matching editors
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new TextCellEditor());
            }

            @Override
            public void configureTypedLayer(AbstractLayer layer) {
                layer.registerCommandHandler(new EditCellCommandHandler());
                layer.registerEventHandler(new InlineCellEditEventHandler(layer));
            }

        });
        compositeGridLayer.addConfiguration(new DefaultEditBindings());

        this.sumMoneySummaryProvider = new SummationGroupBySummaryProvider<>(columnPropertyAccessor);
        this.avgMoneySummaryProvider = new AverageMoneyGroupBySummaryProvider();

        // add group by summary configuration
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
                        _813_SortableGroupByWithComboBoxFilterExample.this.sumMoneySummaryProvider,
                        DisplayMode.NORMAL,
                        GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 6);

                configRegistry.registerConfigAttribute(
                        GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
                        new AverageAgeGroupBySummaryProvider(),
                        DisplayMode.NORMAL,
                        GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 4);

                configRegistry.registerConfigAttribute(
                        GroupByConfigAttributes.GROUP_BY_CHILD_COUNT_PATTERN,
                        "[{0}] - ({1})");

                configRegistry.registerConfigAttribute(
                        GroupByConfigAttributes.GROUP_BY_HINT,
                        "Drag columns here");

                Style hintStyle = new Style();
                hintStyle.setAttributeValue(
                        CellStyleAttributes.FONT,
                        GUIHelper.getFont(new FontData("Arial", 10, SWT.ITALIC)));
                configRegistry.registerConfigAttribute(
                        GroupByConfigAttributes.GROUP_BY_HINT_STYLE,
                        hintStyle);
            }
        });

        // add group by header configuration
        natTable.addConfiguration(new GroupByHeaderMenuConfiguration(natTable, groupByHeaderLayer));

        natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {

            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return super.createColumnHeaderMenu(natTable)
                        .withHideColumnMenuItem()
                        .withShowAllColumnsMenuItem()
                        .withClearAllFilters()
                        .withStateManagerMenuItemProvider();
            }

            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable)
                        .withShowAllColumnsMenuItem()
                        .withStateManagerMenuItemProvider();
            }
        });

        // add a body menu configuration to be able to add or delete rows
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
                                    int rowIndex = natTable.getRowIndexByPosition(rowPosition);

                                    Person person = new Person(bodyLayerStack.getEventList().size() + 1, "John", "Doe", Gender.MALE, false, new Date());
                                    Address address = new Address();
                                    address.setStreet("Some Street");
                                    address.setHousenumber(42);
                                    address.setPostalCode(12345);
                                    address.setCity("In the clouds");

                                    ExtendedPersonWithAddress entry = new ExtendedPersonWithAddress(person, address,
                                            "0000", "Some custom person", 0,
                                            new ArrayList<String>(), new ArrayList<String>());
                                    natTable.doCommand(new RowInsertCommand<>(rowIndex + 1, entry));
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
                                        List<ExtendedPersonWithAddress> toDelete = new ArrayList<>();
                                        for (int pos : selectedRowPositions) {
                                            int idx = selectionLayer.getRowIndexByPosition(pos);
                                            Object o = bodyLayerStack.bodyDataProvider.getRowObject(idx);
                                            if (o instanceof ExtendedPersonWithAddress) {
                                                toDelete.add((ExtendedPersonWithAddress) o);
                                            }
                                        }
                                        selectionLayer.doCommand(new RowObjectDeleteCommand<>(toDelete));
                                    } else {
                                        int rowPosition = MenuItemProviders.getNatEventData(event).getRowPosition();
                                        int pos = LayerUtil.convertRowPosition(natTable, rowPosition, selectionLayer);
                                        int idx = selectionLayer.getRowIndexByPosition(pos);

                                        natTable.doCommand(new RowObjectDeleteCommand<>(bodyLayerStack.bodyDataProvider.getRowObject(idx)));
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

        natTable.registerCommandHandler(
                new DisplayPersistenceDialogCommandHandler(natTable));

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        Composite buttonPanel = new Composite(container, SWT.NONE);
        buttonPanel.setLayout(new RowLayout());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);

        Button toggleHeaderButton = new Button(buttonPanel, SWT.PUSH);
        toggleHeaderButton.setText("Toggle Group By Header");
        toggleHeaderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                groupByHeaderLayer.setVisible(!groupByHeaderLayer.isVisible());
            }
        });

        Button toggleFilterButton = new Button(buttonPanel, SWT.PUSH);
        toggleFilterButton.setText("Toggle Filter Row");
        toggleFilterButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                filterRowHeaderLayer.setFilterRowVisible(!filterRowHeaderLayer.isFilterRowVisible());
            }
        });

        Button collapseAllButton = new Button(buttonPanel, SWT.PUSH);
        collapseAllButton.setText("Collapse All");
        collapseAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(new TreeCollapseAllCommand());
            }
        });

        Button expandAllButton = new Button(buttonPanel, SWT.PUSH);
        expandAllButton.setText("Expand All");
        expandAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(new TreeExpandAllCommand());
            }
        });

        Button toggleMoneySummaryButton = new Button(buttonPanel, SWT.PUSH);
        toggleMoneySummaryButton.setText("Toggle Money Group Summary (SUM/AVG)");
        toggleMoneySummaryButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // clear the group by summary cache so the new summary
                // calculation gets triggered
                bodyLayerStack.getBodyDataLayer().clearCache();

                _813_SortableGroupByWithComboBoxFilterExample.this.useMoneySum = !_813_SortableGroupByWithComboBoxFilterExample.this.useMoneySum;
                if (_813_SortableGroupByWithComboBoxFilterExample.this.useMoneySum) {
                    configRegistry.registerConfigAttribute(
                            GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
                            _813_SortableGroupByWithComboBoxFilterExample.this.sumMoneySummaryProvider,
                            DisplayMode.NORMAL,
                            GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 6);
                } else {
                    configRegistry.registerConfigAttribute(
                            GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
                            _813_SortableGroupByWithComboBoxFilterExample.this.avgMoneySummaryProvider,
                            DisplayMode.NORMAL,
                            GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 6);
                }
                natTable.doCommand(new VisualRefreshCommand());
            }
        });

        // this button adds data to the grid
        // try to group by last name, sort by last name desc and then add
        // dynamic data for verification
        Button addDynamicDataButton = new Button(buttonPanel, SWT.PUSH);
        addDynamicDataButton.setText("Add Data");
        addDynamicDataButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Address address = new Address();
                address.setStreet("Some Street");
                address.setHousenumber(42);
                address.setPostalCode(12345);
                address.setCity("In the clouds");

                bodyLayerStack.getEventList().getReadWriteLock().writeLock().lock();
                try {
                    Person person = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
                    ExtendedPersonWithAddress entry = new ExtendedPersonWithAddress(person, address,
                            "0000", "The little Ralphy", 0,
                            new ArrayList<String>(), new ArrayList<String>());
                    bodyLayerStack.getEventList().add(entry);

                    person = new Person(42, "Clancy", "Wiggum", Gender.MALE, true, new Date());
                    entry = new ExtendedPersonWithAddress(person, address,
                            "XXXL", "It is Chief Wiggum", 0, new ArrayList<String>(), new ArrayList<String>());
                    bodyLayerStack.getEventList().add(entry);

                    person = new Person(42, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
                    entry = new ExtendedPersonWithAddress(person, address,
                            "mommy", "Little Ralphy's mother", 0,
                            new ArrayList<String>(), new ArrayList<String>());
                    bodyLayerStack.getEventList().add(entry);
                } finally {
                    bodyLayerStack.getEventList().getReadWriteLock().writeLock().unlock();
                    // Inserting new objects could cause the creation of new
                    // GroupByObjects dependent on the GroupBy state. If
                    // additionally to the GroupBy state a sorting is applied on
                    // a column that contain a GroupBy summary value, the
                    // comparison and therefore the sorting is incorrect as the
                    // GroupBy summary value cannot be calculated. Therefore the
                    // sorting is re-applied after the insert operation to have
                    // a reliable sorting.
                    sortModel.refresh();
                }
            }
        });

        return container;
    }

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <T>
     */
    class BodyLayerStack<T> extends AbstractLayerTransform {

        private final EventList<T> eventList;
        private final SortedList<T> sortedList;
        private final FilterList<T> filterList;

        private final IRowDataProvider<Object> bodyDataProvider;

        private final GroupByDataLayer<T> bodyDataLayer;

        private final SelectionLayer selectionLayer;

        private final TreeLayer treeLayer;

        private final GroupByModel groupByModel = new GroupByModel();

        private final GlazedListsEventLayer<Object> glazedListsEventLayer;

        @SuppressWarnings("unchecked")
        public BodyLayerStack(List<T> values,
                IColumnPropertyAccessor<T> columnPropertyAccessor,
                ConfigRegistry configRegistry) {
            // wrapping of the list to show into GlazedLists
            // see http://publicobject.com/glazedlists/ for further information
            this.eventList = GlazedLists.eventList(values);
            TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(this.eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator
            // will be set by configuration
            this.sortedList = new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the FilterList
            this.filterList = new FilterList<>(this.sortedList);

            // Use the GroupByDataLayer instead of the default DataLayer
            this.bodyDataLayer = new GroupByDataLayer<>(
                    getGroupByModel(),
                    this.filterList,
                    columnPropertyAccessor,
                    configRegistry);

            // register the RowObjectDeleteCommandHandler for delete operations
            // by object, e.g. delete by UI interaction
            this.bodyDataLayer.registerCommandHandler(new GlazedListsRowObjectDeleteCommandHandler<>(this.filterList));

            this.bodyDataLayer.registerCommandHandler(new GlazedListsRowInsertCommandHandler<>(this.filterList));

            // get the IDataProvider that was created by the GroupByDataLayer
            this.bodyDataProvider = (IRowDataProvider<Object>) this.bodyDataLayer.getDataProvider();

            // layer for event handling of GlazedLists and PropertyChanges
            this.glazedListsEventLayer =
                    new GlazedListsEventLayer<>(this.bodyDataLayer, this.bodyDataLayer.getTreeList());

            // NOTE:
            // we need to tell the GroupByDataLayer to clear its cache if
            // a IVisualChangeEvent occurs. This is necessary because the
            // GlazedListsEventLayer transforms GlazedLists change events to
            // NatTable change events and fires the event the layer stack
            // upwards. But as it sits on top of the GroupByDataLayer, the
            // GroupByDataLayer never gets informed about the change.
            this.glazedListsEventLayer.addLayerListener(new ILayerListener() {

                @Override
                public void handleLayerEvent(ILayerEvent event) {
                    if (event instanceof IVisualChangeEvent) {
                        BodyLayerStack.this.bodyDataLayer.clearCache();
                    }
                }
            });

            ColumnReorderLayer columnReorderLayer =
                    new ColumnReorderLayer(this.glazedListsEventLayer);
            ColumnHideShowLayer columnHideShowLayer =
                    new ColumnHideShowLayer(columnReorderLayer);
            this.selectionLayer =
                    new SelectionLayer(columnHideShowLayer);

            // add a tree layer to visualise the grouping
            this.treeLayer = new TreeLayer(this.selectionLayer, this.bodyDataLayer.getTreeRowModel());

            ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);

            // this will avoid tree specific rendering regarding alignment and
            // indentation in case no grouping is active
            viewportLayer.setConfigLabelAccumulator(new GroupByConfigLabelModifier(getGroupByModel()));

            setUnderlyingLayer(viewportLayer);
        }

        public ILayer getGlazedListsEventLayer() {
            return this.glazedListsEventLayer;
        }

        public TreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public EventList<T> getEventList() {
            return this.eventList;
        }

        public SortedList<T> getSortedList() {
            return this.sortedList;
        }

        public FilterList<T> getFilterList() {
            return this.filterList;
        }

        public IRowDataProvider<Object> getBodyDataProvider() {
            return this.bodyDataProvider;
        }

        public GroupByDataLayer<T> getBodyDataLayer() {
            return this.bodyDataLayer;
        }

        public GroupByModel getGroupByModel() {
            return this.groupByModel;
        }
    }

    /**
     * Example implementation for a typed IGroupBySummaryProvider that
     * calculates the average age of ExtendedPersonWithAddress objects in a
     * grouping.
     */
    class AverageAgeGroupBySummaryProvider implements IGroupBySummaryProvider<ExtendedPersonWithAddress> {

        @Override
        public Object summarize(int columnIndex, List<ExtendedPersonWithAddress> children) {
            int summaryValue = 0;
            for (ExtendedPersonWithAddress child : children) {
                summaryValue += child.getAge();
            }
            return summaryValue / (children.size() > 0 ? children.size() : 1);
        }

    }

    /**
     * Example implementation for a typed IGroupBySummaryProvider that
     * calculates the average money of ExtendedPersonWithAddress objects in a
     * grouping.
     */
    class AverageMoneyGroupBySummaryProvider implements IGroupBySummaryProvider<ExtendedPersonWithAddress> {

        @Override
        public Object summarize(int columnIndex, List<ExtendedPersonWithAddress> children) {
            int summaryValue = 0;
            for (ExtendedPersonWithAddress child : children) {
                summaryValue += child.getMoney();
            }
            return summaryValue / (children.size() > 0 ? children.size() : 1);
        }

    }
}
