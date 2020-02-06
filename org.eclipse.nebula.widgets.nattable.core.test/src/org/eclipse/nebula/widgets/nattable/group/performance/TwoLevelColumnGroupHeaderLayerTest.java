/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.config.GroupHeaderConfigLabels;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class TwoLevelColumnGroupHeaderLayerTest {

    GroupModel groupModel;
    ColumnGroupHeaderLayer columnGroupHeaderLayer;
    ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
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

        this.columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer);

        this.selectionLayer = new SelectionLayer(this.columnGroupExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, this.selectionLayer);
        this.columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, this.selectionLayer);

        this.groupModel = this.columnGroupHeaderLayer.getGroupModel();

        // configure the column groups
        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroupingLevel();
        this.columnGroupHeaderLayer.addGroup(1, "Test", 4, 7);

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

        assertEquals(2, this.columnGroupHeaderLayer.getLevelCount());
        verifyCleanState();
    }

    private void verifyCleanState() {
        assertEquals(11, this.gridLayer.getColumnCount());
        assertEquals(13, this.gridLayer.getRowCount());

        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(0, column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(0, column));
        }

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(0, column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(0, column));

            // level 2 test
            if (column < 4 || column > 10) {
                assertFalse(this.columnGroupHeaderLayer.isPartOfAGroup(1, column));
                assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(1, column));
            } else {
                assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(1, column));
                assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(1, column));
            }
        }

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 1);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 1);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 1);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        members = group2.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        Group group3 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        members = group3.getMembers();
        assertEquals(3, members.length);
        assertEquals(8, members[0]);
        assertEquals(9, members[1]);
        assertEquals(10, members[2]);

        Group group4 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        members = group4.getMembers();
        assertEquals(3, members.length);
        assertEquals(11, members[0]);
        assertEquals(12, members[1]);
        assertEquals(13, members[2]);

        Group group11 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(4);
        assertEquals(4, group11.getStartIndex());
        assertEquals(4, group11.getVisibleStartIndex());
        assertEquals(4, group11.getVisibleStartPosition());
        assertEquals(7, group11.getOriginalSpan());
        assertEquals(7, group11.getVisibleSpan());
        members = group11.getMembers();
        assertEquals(7, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
        assertEquals(8, members[4]);
        assertEquals(9, members[5]);
        assertEquals(10, members[6]);
    }

    @Test
    public void shouldRenderColumnGroups() {
        assertEquals(11, this.gridLayer.getColumnCount());
        assertEquals(13, this.gridLayer.getRowCount());

        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(15, this.gridLayer.getColumnCount());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 1);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 1);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 1);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldConfigureReorderSupported() {
        assertTrue(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(0));
        assertTrue(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(1));

        // should even return true for levels that are not configured
        assertTrue(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(2));

        this.columnGroupHeaderLayer.setReorderSupportedOnLevel(1, false);

        assertTrue(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(0));
        assertFalse(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(1));
        assertTrue(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(2));

        // do not configure a level that is not available
        this.columnGroupHeaderLayer.setReorderSupportedOnLevel(2, false);
        assertTrue(this.columnGroupHeaderLayer.isReorderSupportedOnLevel(2));
    }

    @Test
    public void shouldReorderLevel1GroupToStart() {
        // try to reorder the Test group in level 1 to start
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 1, 5, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 1);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 1);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        members = group2.getMembers();
        assertEquals(3, members.length);
        assertEquals(8, members[0]);
        assertEquals(9, members[1]);
        assertEquals(10, members[2]);

        Group group3 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
        assertEquals(0, group3.getStartIndex());
        assertEquals(0, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        members = group3.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        Group group4 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        members = group4.getMembers();
        assertEquals(3, members.length);
        assertEquals(11, members[0]);
        assertEquals(12, members[1]);
        assertEquals(13, members[2]);

        Group group11 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0);
        assertEquals(4, group11.getStartIndex());
        assertEquals(4, group11.getVisibleStartIndex());
        assertEquals(0, group11.getVisibleStartPosition());
        assertEquals(7, group11.getOriginalSpan());
        assertEquals(7, group11.getVisibleSpan());
        members = group11.getMembers();
        assertEquals(7, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
        assertEquals(8, members[4]);
        assertEquals(9, members[5]);
        assertEquals(10, members[6]);
    }

    @Test
    public void shouldDragReorderLevel1GroupToStart() {
        // try to reorder the Test group in level 1 to start
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 1, 5));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 1, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 1);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 1);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        members = group2.getMembers();
        assertEquals(3, members.length);
        assertEquals(8, members[0]);
        assertEquals(9, members[1]);
        assertEquals(10, members[2]);

        Group group3 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
        assertEquals(0, group3.getStartIndex());
        assertEquals(0, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        members = group3.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        Group group4 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        members = group4.getMembers();
        assertEquals(3, members.length);
        assertEquals(11, members[0]);
        assertEquals(12, members[1]);
        assertEquals(13, members[2]);

        Group group11 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0);
        assertEquals(4, group11.getStartIndex());
        assertEquals(4, group11.getVisibleStartIndex());
        assertEquals(0, group11.getVisibleStartPosition());
        assertEquals(7, group11.getOriginalSpan());
        assertEquals(7, group11.getVisibleSpan());
        members = group11.getMembers();
        assertEquals(7, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
        assertEquals(8, members[4]);
        assertEquals(9, members[5]);
        assertEquals(10, members[6]);
    }

    @Test
    public void shouldReorderLevel1GroupToEnd() {

        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // try to reorder the Test group in level 1 to end
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 1, 5, 15));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 1);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 1);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
        assertEquals(11, group2.getStartIndex());
        assertEquals(11, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        members = group2.getMembers();
        assertEquals(3, members.length);
        assertEquals(11, members[0]);
        assertEquals(12, members[1]);
        assertEquals(13, members[2]);

        Group group3 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        members = group3.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        Group group4 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
        assertEquals(8, group4.getStartIndex());
        assertEquals(8, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        members = group4.getMembers();
        assertEquals(3, members.length);
        assertEquals(8, members[0]);
        assertEquals(9, members[1]);
        assertEquals(10, members[2]);

        Group group11 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
        assertEquals(4, group11.getStartIndex());
        assertEquals(4, group11.getVisibleStartIndex());
        assertEquals(7, group11.getVisibleStartPosition());
        assertEquals(7, group11.getOriginalSpan());
        assertEquals(7, group11.getVisibleSpan());
        members = group11.getMembers();
        assertEquals(7, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
        assertEquals(8, members[4]);
        assertEquals(9, members[5]);
        assertEquals(10, members[6]);
    }

    @Test
    public void shouldDragReorderLevel1GroupToEnd() {

        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // try to reorder the Test group in level 1 to end
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 1, 5));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 1, 15));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 1);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 1);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
        assertEquals(11, group2.getStartIndex());
        assertEquals(11, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        members = group2.getMembers();
        assertEquals(3, members.length);
        assertEquals(11, members[0]);
        assertEquals(12, members[1]);
        assertEquals(13, members[2]);

        Group group3 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        members = group3.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        Group group4 = this.columnGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
        assertEquals(8, group4.getStartIndex());
        assertEquals(8, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        members = group4.getMembers();
        assertEquals(3, members.length);
        assertEquals(8, members[0]);
        assertEquals(9, members[1]);
        assertEquals(10, members[2]);

        Group group11 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
        assertEquals(4, group11.getStartIndex());
        assertEquals(4, group11.getVisibleStartIndex());
        assertEquals(7, group11.getVisibleStartPosition());
        assertEquals(7, group11.getOriginalSpan());
        assertEquals(7, group11.getVisibleSpan());
        members = group11.getMembers();
        assertEquals(7, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
        assertEquals(8, members[4]);
        assertEquals(9, members[5]);
        assertEquals(10, members[6]);
    }

    @Test
    public void shouldNotReorderNonGroupedToUnbreakableLevel1Group() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non grouped column to the non-breakable column
        // group on level 0
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 6));

        // no changes should have happened
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
    }

    @Test
    public void shouldNotDragReorderNonGroupedToUnbreakableLevel1Group() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non grouped column to the non-breakable column
        // group on level 0
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 3));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 6));

        // no changes should have happened
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
    }

    @Test
    public void shouldDragReorderLevel0GroupToNonGroupedUnbreakable() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non-breakable column group on level 0 inside the
        // non-breakable level 1 group
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 3));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 2);
        assertNotNull(group);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldReorderLevel0GroupToNonGroupedUnbreakable() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non-breakable column group on level 0 inside the
        // non-breakable level 1 group
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 3));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 2);
        assertNotNull(group);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldNotReorderLevel0GroupOutOfUnbreakableLevel1Group() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a level 0 group out of the non-breakable column
        // group on level 1
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 10));

        // no changes should have happened
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
    }

    @Test
    public void shouldNotDragReorderLevel0GroupOutOfUnbreakableLevel1Group() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a level 0 group out of the non-breakable column
        // group on level 1
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 10));

        // no changes should have happened
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);
    }

    @Test
    public void shouldReorderLevel0GroupWithOneColumnToNonGroupedUnbreakable() {
        // first clear all groups
        this.columnGroupHeaderLayer.clearAllGroups();

        assertTrue(this.columnGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.columnGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.columnGroupHeaderLayer.addGroup("Address", 4, 1);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non-breakable column group on level 0 inside the
        // non-breakable level 1 group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 3));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 1));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0, 2);
        assertNotNull(group);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(1, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(1, members.length);
        assertEquals(4, members[0]);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 3));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldSetGroupHeaderRowHeight() {
        this.columnGroupHeaderLayer.setRowHeight(100);
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldSetGroupHeaderRowHeightByLevel() {
        this.columnGroupHeaderLayer.setRowHeight(this.columnGroupHeaderLayer.getRowPositionForLevel(1), 100);
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldResizeColumnGroupHeaderRow() {
        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 1, 100));
        assertEquals(20, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldResizeColumnGroupHeaderRowWithoutDownScale() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 120;
            }

        };
        this.gridLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 0, 100));
        assertEquals(125, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(25, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldResizeColumnGroupHeaderRowWithDownScale() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 120;
            }

        };
        this.gridLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 0, 100, true));

        // down scaling in the command was enabled, therefore the value set is
        // the value that will be returned
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(25, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderRow() {
        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderAndBody() {
        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0, 1, 2 }, 100));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(2));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderRowWithoutDownScale() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 120;
            }

        };
        this.gridLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100));
        assertEquals(125, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(125, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderRowWithDownScale() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 120;
            }

        };
        this.gridLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100, true));

        // down scaling in the command was enabled, therefore the value set is
        // the value that will be returned
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldReturnConfigLabelsOnLevel0() {
        // check expanded column group
        LabelStack stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(4, 1);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.columnGroupHeaderLayer.collapseGroup(0, 4);
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(4, 1);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));
    }

    @Test
    public void shouldReturnConfigLabelsOnLevel1() {
        // check expanded column group
        LabelStack stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.columnGroupHeaderLayer.collapseGroup(1, 4);
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));
    }

    @Test
    public void shouldReturnConfigLabelsOnLevel0WithoutLevel0() {
        // check column 0 where we only have a group on level 0 but not on level
        // 0
        LabelStack stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.columnGroupHeaderLayer.collapseGroup(0);
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));
    }

    @Test
    public void shouldExpandCollapseLevel0ViaLevel1WithoutLevel1Group() {
        Group level0Group = this.columnGroupHeaderLayer.getGroupByPosition(0, 0);
        assertFalse(level0Group.isCollapsed());

        // trigger the command for the origin row position of the column group
        this.gridLayer.doCommand(new ColumnGroupExpandCollapseCommand(this.gridLayer, 1, 0));

        assertTrue(level0Group.isCollapsed());
    }

    @Test
    public void shouldNotDragReorderFromUnbreakableLevel1GroupToEnd() {

        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // remove Personal group
        this.columnGroupHeaderLayer.removeGroup(0, 11);

        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // try to reorder the first group in level 0 to end
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 15));

        // add the Personal group back
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        // verify that nothing has changed
        assertEquals(15, this.gridLayer.getColumnCount());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 1);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 1);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 1);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowIndex());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // second level column group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(7, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(700, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldShowColumnGroupOnReorderInHiddenState() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide the last 3 columns in the first group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 2, 3, 4));

        // reorder the last remaining column to the right
        // this will avoid reordering and not put the column at the end of the
        // group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 2));

        // hide the last remaining column
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        // show all columns again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(0));
        assertEquals(1, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(1));
        assertEquals(2, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(2));
        assertEquals(3, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(3));
        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1, 0);
        assertNotNull(group);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToRightEndInsideGroupWithHidden() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first column in second group and last column in first group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 4, 5));

        // reorder the first column in first group to the last position
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        // show all columns again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(0));
        assertEquals(2, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(1));
        assertEquals(0, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(2));
        assertEquals(3, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(3));
        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(4));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(1, 0);
        assertNotNull(group1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1, 4);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderToRightEndInsideGroupWithHidden() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first column in second group and last column in first group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 4, 5));

        // reorder the first column in first group to the last position
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 4));

        // show all columns again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(0));
        assertEquals(2, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(1));
        assertEquals(0, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(2));
        assertEquals(3, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(3));
        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(4));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(1, 0);
        assertNotNull(group1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1, 4);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderUngroupedToRightWithFirstHidden() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first column in third group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 9));

        // ungroup first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder ungrouped column to end of second group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 9));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1, 3);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(1, 8);
        assertNotNull(group3);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupedToRightWithFirstHidden() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first column in third group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 9));

        // ungroup first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder ungrouped column to end of second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 9));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1, 3);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(1, 8);
        assertNotNull(group3);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
    }

    @Test
    public void shouldReorderGroupBetweenHiddenColumns() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.columnGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first column in third group and last column in second group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 8, 9));

        Group group2 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Address", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        // try to reorder group 1 between 2 and 3
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 1, 1, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        group2 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(3);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        group3 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(9);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(9, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldReorderMultipleUngroupedToGroupOnRightEdgeWithHidden() {
        // configure the column groups
        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        // hide first column in third group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 9));

        // reorder first and second column to second group end
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(1, 2), 9));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(60, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(60, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(6, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(600, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0));
        assertNull(this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(1));

        Group group = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(2);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());

        Group group1 = this.columnGroupHeaderLayer.getGroupModel(1).getGroupByPosition(8);
        assertEquals(8, group1.getStartIndex());
        assertEquals(9, group1.getVisibleStartIndex());
        assertEquals(8, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());

        assertEquals(8, group1.getStartIndex());
        assertEquals(8, group1.getVisibleStartIndex());
        assertEquals(8, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);

        this.columnGroupHeaderLayer.addGroupingLevel();
        this.columnGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.columnGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder column 10 to end
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 10));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldReorderToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);

        this.columnGroupHeaderLayer.addGroupingLevel();
        this.columnGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.columnGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder column 10 to end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 10, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldMultiReorderToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);

        this.columnGroupHeaderLayer.addGroupingLevel();
        this.columnGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.columnGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder column 10 to end
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(10), 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldDragReorderGroupToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.clearAllGroups();

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Test", 9, 1);

        this.columnGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.columnGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder column 10 to end
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 10));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldDragReorderUnbreakableGroupToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.clearAllGroups();
        this.columnGroupHeaderLayer.setDefaultUnbreakable(true);

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Test", 9, 1);

        this.columnGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.columnGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder column 10 to end
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 10));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLastSpanningSecondLevel() {
        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0, 1);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(0, 4);

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // reorder first position between second and third group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 8));

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 6));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLastSameGroupBorders() {
        // remove second level group
        this.columnGroupHeaderLayer.removeGroup(1, 4);

        // specify two second level groups with same border as first level
        this.columnGroupHeaderLayer.addGroup(1, "Test1", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Test2", 8, 3);

        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0, 1);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(0, 4);

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // reorder first position between second and third group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 8));

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 6));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLastSpanningFirstLevel() {
        // remove second level group
        this.columnGroupHeaderLayer.removeGroup(1, 4);

        // specify two second level groups with same border as first level
        this.columnGroupHeaderLayer.addGroup(1, "Test1", 4, 4);
        this.columnGroupHeaderLayer.addGroup(1, "Test2", 8, 3);

        // remove first level group
        this.columnGroupHeaderLayer.removeGroup(0, 4);
        this.columnGroupHeaderLayer.removeGroup(0, 8);

        // specify spanning first level group
        this.columnGroupHeaderLayer.addGroup(0, "FirstLevel", 4, 7);

        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, 8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 8));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1, 4);

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // reorder first position between second and third group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 8));

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(1, 6));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(1, 7));
    }
}
