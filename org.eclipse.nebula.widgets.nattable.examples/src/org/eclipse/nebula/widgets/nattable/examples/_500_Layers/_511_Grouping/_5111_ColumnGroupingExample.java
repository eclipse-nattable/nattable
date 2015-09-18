/*******************************************************************************
 * Copyright (c) 2013, 2014, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Roman Flueckiger <roman.flueckiger@mac.com> - added example implementation for Bug 451486
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._511_Grouping;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.ui.menu.VisibleColumnsRemaining;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Simple example showing how to add the {@link ColumnGroupHeaderLayer} to the
 * layer composition of a grid and how to add the corresponding actions to the
 * column header menu.
 */
public class _5111_ColumnGroupingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(new _5111_ColumnGroupingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the ColumnGroupHeaderLayer within a grid and "
                + "its corresponding actions in the column header menu. If you perform a right "
                + "click on the column header, you are able to hide the current selected "
                + "column or show all columns again.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        // property names of the Person class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "address.street", "address.housenumber",
                "address.postalCode", "address.city", "age", "birthday",
                "money", "description", "favouriteFood", "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<String, String>();
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
                new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(propertyNames);

        ColumnGroupModel columnGroupModel = new ColumnGroupModel();

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and setting the ViewportLayer as
        // underlying layer. But in this case using the ViewportLayer
        // directly as body layer is also working.
        IDataProvider bodyDataProvider =
                new ListDataProvider<ExtendedPersonWithAddress>(
                        PersonService.getExtendedPersonsWithAddress(10),
                        columnPropertyAccessor);
        DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);
        ColumnReorderLayer columnReorderLayer =
                new ColumnReorderLayer(bodyDataLayer);
        ColumnGroupReorderLayer columnGroupReorderLayer =
                new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
        ColumnHideShowLayer columnHideShowLayer =
                new ColumnHideShowLayer(columnGroupReorderLayer);
        ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer =
                new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
        final SelectionLayer selectionLayer =
                new SelectionLayer(columnGroupExpandCollapseLayer);
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
                new ColumnGroupHeaderLayer(columnHeaderLayer, selectionLayer, columnGroupModel);

        // configure the column groups
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Person", 0, 1, 2, 3);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Address", 4, 5, 6, 7);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Facts", 8, 9, 10);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Personal", 11, 12, 13);
        columnGroupHeaderLayer.setStaticColumnIndexesByGroup("Person", 0, 1);
        columnGroupHeaderLayer.setStaticColumnIndexesByGroup("Address", 4, 5, 6);
        columnGroupHeaderLayer.setGroupUnbreakable(1);

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
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnGroupHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(viewportLayer, columnGroupHeaderLayer, rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer, false);

        // as the autoconfiguration of the NatTable is turned off, we have to
        // add the DefaultNatTableStyleConfiguration manually
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

        natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
            @Override
            protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
                PopupMenuBuilder builder = super.createColumnHeaderMenu(natTable).withColumnChooserMenuItem();
                builder.withEnabledState(
                        PopupMenuBuilder.HIDE_COLUMN_MENU_ITEM_ID,
                        new VisibleColumnsRemaining(selectionLayer));
                return builder;
            }
        });

        // Column group header menu
        final Menu columnGroupHeaderMenu = new PopupMenuBuilder(natTable)
                .withRenameColumnGroupMenuItem()
                .withRemoveColumnGroupMenuItem()
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

        // enable this configuration to verify the automatic height calculation
        // when using vertical text painter
        // natTable.addConfiguration(new AbstractRegistryConfiguration() {
        //
        // @Override
        // public void configureRegistry(IConfigRegistry configRegistry) {
        // ICellPainter cellPainter = new BeveledBorderDecorator(new
        // VerticalTextPainter(false, true, 5, true, true));
        // configRegistry.registerConfigAttribute(
        // CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL,
        // GridRegion.COLUMN_HEADER);
        // }
        // });

        // Register column chooser
        DisplayColumnChooserCommandHandler columnChooserCommandHandler =
                new DisplayColumnChooserCommandHandler(
                        selectionLayer,
                        columnHideShowLayer,
                        columnHeaderLayer,
                        columnHeaderDataLayer,
                        columnGroupHeaderLayer,
                        columnGroupModel,
                        false,
                        true);

        viewportLayer.registerCommandHandler(columnChooserCommandHandler);

        natTable.configure();

        return natTable;
    }
}
