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
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Simple example showing how to add the performance {@link RowGroupHeaderLayer}
 * to the layer composition of a grid and how to add the corresponding actions
 * to the row header menu.
 */
public class _5116_PerformanceRowGroupingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(1010, 500, new _5116_PerformanceRowGroupingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the performance RowGroupHeaderLayer within a grid and "
                + "its corresponding actions in the row header menu. If you perform a right "
                + "click on the row header, you are able to create a group out of the current selected "
                + "rows, remove a row group or even rename a row group.";
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
        RowReorderLayer rowReorderLayer =
                new RowReorderLayer(bodyDataLayer);

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

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

        RowGroupHeaderLayer rowGroupHeaderLayer =
                new RowGroupHeaderLayer(rowHeaderLayer, selectionLayer);

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
                new CornerLayer(cornerDataLayer, rowGroupHeaderLayer, columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnHeaderLayer, rowGroupHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                return super.createColumnHeaderMenu(natTable).withAutoResizeSelectedColumnsMenuItem();
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

        natTable.configure();

        return natTable;
    }
}
