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
package org.eclipse.nebula.widgets.nattable.group.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupUtilsTest {

    ColumnGroupHeaderLayer columnGroupHeaderLayer;
    SelectionLayer selectionLayer;
    GridLayer gridLayer;

    @Before
    public void setup() {
        String[] propertyNames = {
                "firstName", "lastName", "gender", "married",
                "address.street", "address.housenumber", "address.postalCode", "address.city",
                "age", "birthday", "money",
                "description", "favouriteFood", "favouriteDrinks" };

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

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(
                        PersonService.getExtendedPersonsWithAddress(10),
                        columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);

        // TODO
        // ColumnGroupReorderLayer columnGroupReorderLayer =
        // new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);

        ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer);

        this.selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, this.selectionLayer);
        this.columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, this.selectionLayer);

        // configure the column groups
        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        // build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, this.selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, this.columnGroupHeaderLayer);

        // build the grid layer
        this.gridLayer = new GridLayer(viewportLayer, this.columnGroupHeaderLayer, rowHeaderLayer, cornerLayer);

        // configure the visible area, needed for tests in scrolled state
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                // 10 columns + row header should be visible
                return new Rectangle(0, 0, 1010, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(1, this.columnGroupHeaderLayer.getLevelCount());
    }

    @Test
    public void shouldTestInSameGroup() {
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 0, 1));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 0, 2));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 0, 3));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 0, 0));

        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 4, 5));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 4, 6));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 4, 7));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 4, 4));

        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 8, 8));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 8, 9));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 8, 10));

        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 11, 11));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 11, 12));
        assertTrue(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 11, 13));

        assertFalse(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 0, 4));
        assertFalse(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 0, 7));
        assertFalse(ColumnGroupUtils.isInTheSameGroup(this.columnGroupHeaderLayer, 0, 4, 8));
    }

}
