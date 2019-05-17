/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._511_Grouping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
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
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.performance.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.ui.menu.VisibleColumnsRemaining;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Simple example showing how to add the performance
 * {@link ColumnGroupHeaderLayer} and the {@link RowGroupHeaderLayer} to the
 * layer composition of a grid and how to add the corresponding actions to the
 * header menus.
 */
public class _5117_PerformanceColumnAndRowGroupingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(1010, 500, new _5117_PerformanceColumnAndRowGroupingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the performance ColumnGroupHeaderLayer and the "
                + "RowGroupHeaderLayer within a grid and its corresponding actions in the header menus.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "address.street", "address.housenumber",
                "address.postalCode", "address.city", "age", "birthday",
                "money", "description", "favouriteFood", "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postalcode");
        propertyToLabelMap.put("address.city", "City");
        propertyToLabelMap.put("age", "Age");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and setting the ViewportLayer as
        // underlying layer. But in this case using the ViewportLayer
        // directly as body layer is also working.
        List<ExtendedPersonWithAddress> persons = PersonService.getExtendedPersonsWithAddress(50);
        Collections.sort(persons, (o1, o2) -> {
            return o1.getLastName().compareTo(o2.getLastName());
        });

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(
                        persons,
                        columnPropertyAccessor);
        DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);
        ColumnReorderLayer columnReorderLayer =
                new ColumnReorderLayer(bodyDataLayer);
        ColumnHideShowLayer columnHideShowLayer =
                new ColumnHideShowLayer(columnReorderLayer);
        ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer =
                new ColumnGroupExpandCollapseLayer(columnHideShowLayer);
        RowReorderLayer rowReorderLayer =
                new RowReorderLayer(columnGroupExpandCollapseLayer);
        RowHideShowLayer rowHideShowLayer =
                new RowHideShowLayer(rowReorderLayer);
        RowGroupExpandCollapseLayer rowGroupExpandCollapseLayer =
                new RowGroupExpandCollapseLayer(rowHideShowLayer);

        final SelectionLayer selectionLayer =
                new SelectionLayer(rowGroupExpandCollapseLayer);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
        ColumnGroupHeaderLayer columnGroupHeaderLayer =
                new ColumnGroupHeaderLayer(columnHeaderLayer, selectionLayer, false);

        // enable column group selection bindings
        columnGroupHeaderLayer.addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(true));

        // configure the column groups
        columnGroupHeaderLayer.addGroup("Person", 0, 4);
        columnGroupHeaderLayer.addGroup("Address", 4, 4);
        columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        columnGroupHeaderLayer.setGroupUnbreakable(1, true);

        columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 0, 0, 1);
        columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 4, 5, 6);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);
        RowGroupHeaderLayer rowGroupHeaderLayer =
                new RowGroupHeaderLayer(rowHeaderLayer, selectionLayer, false);

        // enable row group selection bindings
        rowGroupHeaderLayer.addConfiguration(new DefaultRowGroupHeaderLayerConfiguration(true));

        // using this configuration instead, expand/collapse would not be
        // supported in the row header
        // in that case even the RowGroupExpandCollapseLayer in the body layer
        // stack could be removed
        // rowGroupHeaderLayer.addConfiguration(
        // new DefaultRowGroupHeaderLayerConfiguration(true, false));
        // rowGroupHeaderLayer.setDefaultCollapseable(false);

        // configure the row groups
        // collect containing last names and the number of persons with that
        // last name
        Map<String, Long> counted = persons.stream()
                .collect(Collectors.groupingBy(ExtendedPersonWithAddress::getLastName, Collectors.counting()));
        counted.entrySet().stream().forEach(e -> {
            rowGroupHeaderLayer.addGroup(
                    e.getKey(),
                    // retrieve the index of the first element with the given
                    // last name
                    IntStream.range(0, persons.size())
                            .filter(index -> persons.get(index).getLastName().equals(e.getKey()))
                            .findFirst()
                            .getAsInt(),
                    e.getValue().intValue());
        });

        // the Simpsons are unbreakable
        rowGroupHeaderLayer.setGroupUnbreakable("Simpson", true);

        // Homer should be static
        Group simpsonGroup = rowGroupHeaderLayer.getGroupByName("Simpson");
        for (int row = simpsonGroup.getStartIndex(); row < simpsonGroup.getStartIndex() + simpsonGroup.getOriginalSpan(); row++) {
            if (persons.get(row).getFirstName().equals("Homer")) {
                simpsonGroup.addStaticIndexes(row);
            }
        }

        // Group carlsonGroup = rowGroupHeaderLayer.getGroupByName("Carlson");
        // Group flandersGroup = rowGroupHeaderLayer.getGroupByName("Flanders");
        // rowGroupHeaderLayer.addGroupingLevel();
        // rowGroupHeaderLayer.addGroup(1, "Friends", 0,
        // carlsonGroup.getOriginalSpan() + flandersGroup.getOriginalSpan());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowGroupHeaderLayer, columnGroupHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnGroupHeaderLayer, rowGroupHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                PopupMenuBuilder builder = super.createColumnHeaderMenu(natTable)
                        .withHideColumnMenuItem()
                        .withShowAllColumnsMenuItem()
                        // the performance column group menu, not the old one
                        .withCreateColumnGroupMenuItem()
                        .withUngroupColumnsMenuItem()
                        .withAutoResizeSelectedColumnsMenuItem()
                        .withColumnRenameDialog()
                        .withColumnChooserMenuItem()
                        .withInspectLabelsMenuItem();
                builder.withEnabledState(
                        PopupMenuBuilder.HIDE_COLUMN_MENU_ITEM_ID,
                        new VisibleColumnsRemaining(selectionLayer));
                return builder;
            }

            @Override
            protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
                return super.createRowHeaderMenu(natTable)
                        .withHideRowMenuItem()
                        .withShowAllRowsMenuItem()
                        .withCreateRowGroupMenuItem()
                        .withUngroupRowsMenuItem()
                        .withAutoResizeSelectedRowsMenuItem()
                        .withInspectLabelsMenuItem();
            }

            @Override
            protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
                return super.createCornerMenu(natTable).withShowAllRowsMenuItem();
            }
        });

        // Column group header menu
        final Menu columnGroupHeaderMenu = new PopupMenuBuilder(natTable)
                .withRenameColumnGroupMenuItem()
                .withRemoveColumnGroupMenuItem()
                .withInspectLabelsMenuItem()
                .build();

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {
            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerFirstMouseDownBinding(
                        new MouseEventMatcher(
                                SWT.NONE,
                                GridRegion.COLUMN_GROUP_HEADER,
                                MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(columnGroupHeaderMenu));
            }
        });

        // Row group header menu
        final Menu rowGroupHeaderMenu = new PopupMenuBuilder(natTable)
                .withRenameRowGroupMenuItem()
                .withRemoveRowGroupMenuItem()
                .withInspectLabelsMenuItem()
                .build();

        natTable.addConfiguration(new AbstractUiBindingConfiguration() {
            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerFirstMouseDownBinding(
                        new MouseEventMatcher(
                                SWT.NONE,
                                GridRegion.ROW_GROUP_HEADER,
                                MouseEventMatcher.RIGHT_BUTTON),
                        new PopupMenuAction(rowGroupHeaderMenu));
            }
        });

        // Register column chooser
        DisplayColumnChooserCommandHandler columnChooserCommandHandler =
                new DisplayColumnChooserCommandHandler(
                        columnHideShowLayer,
                        columnHeaderLayer,
                        columnHeaderDataLayer,
                        columnGroupHeaderLayer,
                        false);

        viewportLayer.registerCommandHandler(columnChooserCommandHandler);

        natTable.configure();

        return natTable;
    }
}
