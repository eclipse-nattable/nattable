/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._511_Grouping;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing how to add the {@link ColumnGroupHeaderLayer} and the
 * {@link ColumnGroupGroupHeaderLayer} to the layer composition of a grid and
 * how to add the corresponding actions to the column header menu.
 *
 * @author Dirk Fauth
 *
 */
public class _5112_TwoLevelColumnGroupingExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner
                .run(new _5112_TwoLevelColumnGroupingExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the ColumnGroupHeaderLayer and the ColumnGroupHeaderLayer "
                + "within a grid and its corresponding actions in the column header menu. If you perform a "
                + "right click on the column header, you are able to hide the current selected "
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

        IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(
                propertyNames);

        ColumnGroupModel columnGroupModel = new ColumnGroupModel();
        ColumnGroupModel sndColumnGroupModel = new ColumnGroupModel();

        // build the body layer stack
        // Usually you would create a new layer stack by extending
        // AbstractIndexLayerTransform and
        // setting the ViewportLayer as underlying layer. But in this case using
        // the ViewportLayer
        // directly as body layer is also working.
        IDataProvider bodyDataProvider = new ListDataProvider<ExtendedPersonWithAddress>(
                PersonService.getExtendedPersonsWithAddress(10),
                columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(
                bodyDataLayer);
        ColumnGroupReorderLayer columnGroupReorderLayer = new ColumnGroupReorderLayer(
                columnReorderLayer, columnGroupModel);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(
                columnGroupReorderLayer);
        ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                columnHideShowLayer, sndColumnGroupModel, columnGroupModel);
        SelectionLayer selectionLayer = new SelectionLayer(
                columnGroupExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(
                columnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer,
                viewportLayer, selectionLayer);
        ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(
                columnHeaderLayer, selectionLayer, columnGroupModel);

        // configure the column groups
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Person", 0, 1, 2, 3);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Address", 4, 5, 6, 7);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Facts", 8, 9, 10);
        columnGroupHeaderLayer.addColumnsIndexesToGroup("Personal", 11, 12, 13);
        columnGroupHeaderLayer.setStaticColumnIndexesByGroup("Person", 0, 1);
        columnGroupHeaderLayer
                .setStaticColumnIndexesByGroup("Address", 4, 5, 6);
        columnGroupHeaderLayer.setGroupUnbreakable(1);

        ColumnGroupGroupHeaderLayer sndGroup = new ColumnGroupGroupHeaderLayer(
                columnGroupHeaderLayer, selectionLayer, sndColumnGroupModel);

        sndGroup.addColumnsIndexesToGroup("PersonWithAddress", 0, 1, 2, 3, 4,
                5, 6, 7);
        sndGroup.addColumnsIndexesToGroup("Additional Information", 8, 9, 10,
                11, 12, 13);

        sndGroup.setStaticColumnIndexesByGroup("PersonWithAddress", 0, 1);

        // build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                bodyDataProvider);
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(
                rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                viewportLayer, selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer,
                sndGroup);

        // build the grid layer
        GridLayer gridLayer = new GridLayer(viewportLayer, sndGroup,
                rowHeaderLayer, cornerLayer);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        NatTable natTable = new NatTable(parent, gridLayer);

        return natTable;
    }

}
