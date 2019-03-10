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
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
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

    @Test
    public void shouldTestIsReorderValidWithoutUnbreakable() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // in same group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 0, 4, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 4, 4, true));
        // to other group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 0, 6, true));
        // to start of first group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 8, 0, true));
        // to end of last group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 0, 13, false));
    }

    @Test
    public void shouldTestIsReorderValidWithUnbreakableFromNonGroupedRight() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // remove first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // set all remaining groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // reorder outside group valid
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 0, 4, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 3, 4, true));
        // in same group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 6, 7, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 7, 4, true));
        // in same group where adjacent group is also unbreakable
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 4, 8, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 10, 8, true));
        // to any other group
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 3, 5, true));
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 0, 6, true));
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 1, 9, true));
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 2, 13, true));
        // to start of table
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 3, 0, true));
        // to end of last group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 0, 13, false));
        // between unbreakable groups
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 3, 8, true));
    }

    @Test
    public void shouldTestIsReorderValidWithUnbreakableFromNonGroupedLeft() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // remove first group
        this.columnGroupHeaderLayer.removeGroup(11);

        // set all remaining groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // between unbreakable groups
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 12, 8, true));
        // to start of table
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 13, 0, true));
    }

    @Test
    public void shouldTestIsReorderValidFromUnbreakable() {
        // remove first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // set second and third group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // in same group
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 4, 6, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 5, 6, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 7, 6, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 4, 8, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 7, 4, true));
        assertTrue(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 10, 8, true));
        // to other group
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 5, 9, true));
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 8, 5, true));
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 9, 4, true));
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 11, 8, true));
        // to start of first group
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 4, 0, true));
        // to end of last group
        assertFalse(ColumnGroupUtils.isReorderValid(this.columnGroupHeaderLayer, 4, 13, false));
    }

    @Test
    public void shouldTestIsBetweenTwoGroups() {
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 0, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 1, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 2, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 3, true, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 4, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 5, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 6, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 7, true, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 8, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 9, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 10, true, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 11, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 12, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 13, true, MoveDirectionEnum.LEFT));

        // move right reorderToLeftEdge
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 0, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 1, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 2, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 3, true, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 4, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 5, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 6, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 7, true, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 8, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 9, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 10, true, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 11, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 12, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 13, true, MoveDirectionEnum.RIGHT));

        // move left reorderToRightEdge
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 0, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 1, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 2, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 3, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 4, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 5, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 6, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 7, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 8, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 9, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 10, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 11, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 12, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 13, false, MoveDirectionEnum.LEFT));

        // move right reorderToRightEdge
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 0, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 1, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 2, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 3, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 4, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 5, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 6, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 7, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 8, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 9, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 10, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 11, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 12, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 0, 13, false, MoveDirectionEnum.RIGHT));
    }

    @Test
    public void shouldTestIsBetweenTwoGroupsOnLevel() {
        // add a second level
        this.columnGroupHeaderLayer.addGroupingLevel();
        // group spans Address and Facts
        this.columnGroupHeaderLayer.addGroup(1, "Test", 4, 7);

        // tests on level 0
        // additional level should not change the results
        shouldTestIsBetweenTwoGroups();

        // tests on level 1
        // note that if check on deeper level is false, it also needs to be
        // false on the upper level

        // move left reorderToLeftEdge
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 0, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 1, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 2, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 3, true, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 4, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 5, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 6, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 7, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 8, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 9, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 10, true, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 11, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 12, true, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 13, true, MoveDirectionEnum.LEFT));

        // move right reorderToLeftEdge
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 0, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 1, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 2, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 3, true, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 4, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 5, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 6, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 7, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 8, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 9, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 10, true, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 11, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 12, true, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 13, true, MoveDirectionEnum.RIGHT));

        // move left reorderToRightEdge
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 0, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 1, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 2, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 3, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 4, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 5, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 6, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 7, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 8, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 9, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 10, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 11, false, MoveDirectionEnum.LEFT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 12, false, MoveDirectionEnum.LEFT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 13, false, MoveDirectionEnum.LEFT));

        // move right reorderToRightEdge
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 0, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 1, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 2, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 3, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 4, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 5, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 6, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 7, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 8, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 9, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 10, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 11, false, MoveDirectionEnum.RIGHT));
        assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 12, false, MoveDirectionEnum.RIGHT));
        assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer, 1, 13, false, MoveDirectionEnum.RIGHT));

        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 0, true, MoveDirectionEnum.LEFT));
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 4, true, MoveDirectionEnum.LEFT));
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 11, true, MoveDirectionEnum.LEFT));
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 13, false, MoveDirectionEnum.RIGHT));
        //
        // assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 8, true, MoveDirectionEnum.LEFT));
        // assertFalse(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 5, true, MoveDirectionEnum.LEFT));

        // TODO remove group on level 0
        // no group on level 1 at this positions
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 0, false, MoveDirectionEnum.LEFT));
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 2, true, MoveDirectionEnum.LEFT));
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 12, true, MoveDirectionEnum.LEFT));
        // assertTrue(ColumnGroupUtils.isBetweenTwoGroups(this.columnGroupHeaderLayer,
        // 1, 13, true, MoveDirectionEnum.RIGHT));
    }
}
