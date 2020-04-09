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
import org.eclipse.nebula.widgets.nattable.group.command.RowGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.RowGroupReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.config.GroupHeaderConfigLabels;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class TwoLevelRowGroupHeaderLayerTest {

    GroupModel groupModel;
    RowGroupHeaderLayer rowGroupHeaderLayer;
    RowGroupExpandCollapseLayer rowGroupExpandCollapseLayer;
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
                        PersonService.getExtendedPersonsWithAddress(14),
                        columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        RowReorderLayer rowReorderLayer = new RowReorderLayer(bodyDataLayer);
        RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(rowReorderLayer);

        this.rowGroupExpandCollapseLayer = new RowGroupExpandCollapseLayer(rowHideShowLayer);

        this.selectionLayer = new SelectionLayer(this.rowGroupExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, this.selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, this.selectionLayer);
        this.rowGroupHeaderLayer = new RowGroupHeaderLayer(rowHeaderLayer, this.selectionLayer);

        this.groupModel = this.rowGroupHeaderLayer.getGroupModel();

        // configure the column groups
        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroupingLevel();
        this.rowGroupHeaderLayer.addGroup(1, "Test", 4, 7);

        // build the corner layer
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, this.rowGroupHeaderLayer, columnHeaderDataLayer);

        // build the grid layer
        this.gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, this.rowGroupHeaderLayer, cornerLayer);

        // configure the visible area, needed for tests in scrolled state
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                // 10 rows + column header should be visible
                return new Rectangle(0, 0, 1010, 220);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(2, this.rowGroupHeaderLayer.getLevelCount());
        verifyCleanState();
    }

    private void verifyCleanState() {
        assertEquals(13, this.gridLayer.getColumnCount());
        assertEquals(11, this.gridLayer.getRowCount());

        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int row = 0; row < this.rowGroupHeaderLayer.getRowCount(); row++) {
            assertTrue(this.rowGroupHeaderLayer.isPartOfAGroup(0, row));
            assertFalse(this.rowGroupHeaderLayer.isPartOfAnUnbreakableGroup(0, row));
        }

        for (int row = 0; row < this.rowGroupHeaderLayer.getRowCount(); row++) {
            assertTrue(this.rowGroupHeaderLayer.isPartOfAGroup(0, row));
            assertFalse(this.rowGroupHeaderLayer.isPartOfAnUnbreakableGroup(0, row));

            // level 2 test
            if (row < 4 || row > 10) {
                assertFalse(this.rowGroupHeaderLayer.isPartOfAGroup(1, row));
                assertFalse(this.rowGroupHeaderLayer.isPartOfAnUnbreakableGroup(1, row));
            } else {
                assertTrue(this.rowGroupHeaderLayer.isPartOfAGroup(1, row));
                assertFalse(this.rowGroupHeaderLayer.isPartOfAnUnbreakableGroup(1, row));
            }
        }

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 8);
        assertEquals(8, cell.getOriginRowPosition());
        assertEquals(8, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(160, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        // this cell is not visible because of the client area
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 8);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(8, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
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

        Group group2 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
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

        Group group3 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(8);
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

        Group group4 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
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

        Group group11 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(4);
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
    public void shouldRenderRowGroups() {
        assertEquals(13, this.gridLayer.getColumnCount());
        assertEquals(11, this.gridLayer.getRowCount());

        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(15, this.gridLayer.getRowCount());

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 8);
        assertEquals(8, cell.getOriginRowPosition());
        assertEquals(8, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(160, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 8);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(8, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);
    }

    @Test
    public void shouldConfigureReorderSupported() {
        assertTrue(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(0));
        assertTrue(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(1));

        // should even return true for levels that are not configured
        assertTrue(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(2));

        this.rowGroupHeaderLayer.setReorderSupportedOnLevel(1, false);

        assertTrue(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(0));
        assertFalse(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(1));
        assertTrue(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(2));

        // do not configure a level that is not available
        this.rowGroupHeaderLayer.setReorderSupportedOnLevel(2, false);
        assertTrue(this.rowGroupHeaderLayer.isReorderSupportedOnLevel(2));
    }

    @Test
    public void shouldReorderLevel1GroupToStart() {
        // try to reorder the Test group in level 1 to start
        this.gridLayer.doCommand(new RowGroupReorderCommand(this.gridLayer, 1, 5, 1));

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(7, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
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

        Group group2 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
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

        Group group3 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
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

        Group group4 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
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

        Group group11 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0);
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
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 1, 5));
        this.gridLayer.doCommand(new RowGroupReorderEndCommand(this.gridLayer, 1, 1));

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(7, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
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

        Group group2 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
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

        Group group3 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
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

        Group group4 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
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

        Group group11 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0);
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
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // try to reorder the Test group in level 1 to end
        this.gridLayer.doCommand(new RowGroupReorderCommand(this.gridLayer, 1, 5, 15));

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(7, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(7, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
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

        Group group2 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
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

        Group group3 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
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

        Group group4 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
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

        Group group11 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
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
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // try to reorder the Test group in level 1 to end
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 1, 5));
        this.gridLayer.doCommand(new RowGroupReorderEndCommand(this.gridLayer, 1, 15));

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(7, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(7, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(140, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(0);
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

        Group group2 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(4);
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

        Group group3 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(7);
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

        Group group4 = this.rowGroupHeaderLayer.getGroupModel(0).getGroupByPosition(11);
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

        Group group11 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
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
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non grouped column to the non-breakable column
        // group on level 0
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 3, 6));

        // no changes should have happened
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 4);
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
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non grouped column to the non-breakable column
        // group on level 0
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 3));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 6));

        // no changes should have happened
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 4);
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
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non-breakable column group on level 0 inside the
        // non-breakable level 1 group
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new RowGroupReorderEndCommand(this.gridLayer, 0, 3));

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 2);
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

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 6));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldReorderLevel0GroupToNonGroupedUnbreakable() {
        // first clear all groups
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non-breakable column group on level 0 inside the
        // non-breakable level 1 group
        this.gridLayer.doCommand(new RowGroupReorderCommand(this.gridLayer, 0, 5, 3));

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 2);
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

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 6));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldNotReorderLevel0GroupOutOfUnbreakableLevel1Group() {
        // first clear all groups
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a level 0 group out of the non-breakable column
        // group on level 1
        this.gridLayer.doCommand(new RowGroupReorderCommand(this.gridLayer, 0, 5, 10));

        // no changes should have happened
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 4);
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
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a level 0 group out of the non-breakable column
        // group on level 1
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 0, 10));

        // no changes should have happened
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 2));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 3));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 4);
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
        this.rowGroupHeaderLayer.clearAllGroups();

        assertTrue(this.rowGroupHeaderLayer.getGroupModel(0).isEmpty());
        assertTrue(this.rowGroupHeaderLayer.getGroupModel(1).isEmpty());

        this.rowGroupHeaderLayer.addGroup("Address", 4, 1);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(1, "Test", 0, 8);

        this.rowGroupHeaderLayer.setGroupUnbreakable(0, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // now try to reorder a non-breakable row group on level 0 inside the
        // non-breakable level 1 group
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 5, 3));

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 0));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 1));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(0, 2);
        assertNotNull(group);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(1, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
        int[] members = group.getMembers();
        assertEquals(1, members.length);
        assertEquals(4, members[0]);

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 3));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 4));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 5));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 6));
        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldSetGroupHeaderColumnWidth() {
        this.rowGroupHeaderLayer.setColumnWidth(100);
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldSetGroupHeaderColumnWidthByLevel() {
        this.rowGroupHeaderLayer.setColumnWidth(this.rowGroupHeaderLayer.getColumnPositionForLevel(1), 100);
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(0));
    }

    @Test
    public void shouldResizeRowGroupHeaderColumn() {
        this.gridLayer.doCommand(new ColumnResizeCommand(this.gridLayer, 1, 100));
        assertEquals(20, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldResizeRowGroupHeaderColumnWithoutDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default width of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getColumnWidthByPosition(0));

        this.gridLayer.doCommand(new ColumnResizeCommand(this.gridLayer, 0, 100));
        assertEquals(125, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(25, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldResizeRowGroupHeaderColumnWithDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default width of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getColumnWidthByPosition(0));

        this.gridLayer.doCommand(new ColumnResizeCommand(this.gridLayer, 0, 100, true));

        // down scaling in the command was enabled, therefore the value set is
        // the value that will be returned
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(25, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldMultiResizeRowGroupHeaderColumn() {
        this.gridLayer.doCommand(new MultiColumnResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldMultiResizeRowGroupHeaderAndBody() {
        this.gridLayer.doCommand(new MultiColumnResizeCommand(this.gridLayer, new int[] { 0, 1, 2 }, 100));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(2));
    }

    @Test
    public void shouldMultiResizeRowGroupHeaderColumnWithoutDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default width of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getColumnWidthByPosition(0));

        this.gridLayer.doCommand(new MultiColumnResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100));
        assertEquals(125, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(125, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldMultiResizeRowGroupHeaderColumnWithDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default width of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getColumnWidthByPosition(0));

        this.gridLayer.doCommand(new MultiColumnResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100, true));

        // down scaling in the command was enabled, therefore the value set is
        // the value that will be returned
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.gridLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void shouldReturnConfigLabelsOnLevel0() {
        // check expanded column group
        LabelStack stack = this.rowGroupHeaderLayer.getConfigLabelsByPosition(1, 4);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.rowGroupHeaderLayer.collapseGroup(0, 4);
        stack = this.rowGroupHeaderLayer.getConfigLabelsByPosition(1, 4);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));
    }

    @Test
    public void shouldReturnConfigLabelsOnLevel1() {
        // check expanded column group
        LabelStack stack = this.rowGroupHeaderLayer.getConfigLabelsByPosition(0, 4);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.rowGroupHeaderLayer.collapseGroup(1, 4);
        stack = this.rowGroupHeaderLayer.getConfigLabelsByPosition(0, 4);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));
    }

    @Test
    public void shouldReturnConfigLabelsOnLevel0WithoutLevel0() {
        // check column 0 where we only have a group on level 0 but not on level
        // 0
        LabelStack stack = this.rowGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.rowGroupHeaderLayer.collapseGroup(0);
        stack = this.rowGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));
    }

    @Test
    public void shouldExpandCollapseLevel0ViaLevel1WithoutLevel1Group() {
        Group level0Group = this.rowGroupHeaderLayer.getGroupByPosition(0, 0);
        assertFalse(level0Group.isCollapsed());

        // trigger the command for the origin column position of the row group
        this.gridLayer.doCommand(new RowGroupExpandCollapseCommand(this.gridLayer, 1, 0));

        assertTrue(level0Group.isCollapsed());
    }

    @Test
    public void shouldNotDragReorderFromUnbreakableLevel1GroupToEnd() {
        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // remove Personal group
        this.rowGroupHeaderLayer.removeGroup(0, 11);

        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);

        // try to reorder the first group in level 0 to end
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new RowGroupReorderEndCommand(this.gridLayer, 0, 15));

        // add the Personal group back
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        assertEquals(15, this.gridLayer.getRowCount());

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(0, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(4, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 8);
        assertEquals(8, cell.getOriginRowPosition());
        assertEquals(8, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(160, cell.getBounds().y);
        assertEquals(20, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(1, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(11, cell.getRowPosition());
        assertEquals(11, cell.getRowIndex());
        assertEquals(3, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(40, cell.getBounds().width);

        // second level row group
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 8);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(8, cell.getRowPosition());
        assertEquals(8, cell.getRowIndex());
        assertEquals(7, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Test", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);
    }

    @Test
    public void shouldShowRowGroupOnReorderInHiddenState() {
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide the last 3 rows in the first group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 2, 3, 4));

        // reorder the last remaining row down
        // this will avoid reordering and not put the row at the end of the
        // group
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 1, 2));

        // hide the last remaining row
        this.gridLayer.doCommand(new RowHideCommand(this.gridLayer, 1));

        // show all rows again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(0, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(0));
        assertEquals(1, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(1));
        assertEquals(2, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(2));
        assertEquals(3, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(3));
        assertEquals(4, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(4));

        Group group = this.rowGroupHeaderLayer.getGroupByPosition(1, 0);
        assertNotNull(group);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToRightEndInsideGroupWithHidden() {
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first row in second group and last row in first group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 4, 5));

        // reorder the first row in first group to the last position
        this.gridLayer.doCommand(new RowReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new RowReorderEndCommand(this.gridLayer, 4));

        // show all rows again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(1, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(0));
        assertEquals(2, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(1));
        assertEquals(0, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(2));
        assertEquals(3, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(3));
        assertEquals(4, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(4));

        Group group1 = this.rowGroupHeaderLayer.getGroupByPosition(1, 0);
        assertNotNull(group1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(1, 4);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderToRightEndInsideGroupWithHidden() {
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first row in second group and last row in first group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 4, 5));

        // reorder the first row in first group to the last position
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 1, 4));

        // show all columns again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(1, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(0));
        assertEquals(2, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(1));
        assertEquals(0, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(2));
        assertEquals(3, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(3));
        assertEquals(4, this.rowGroupHeaderLayer.getPositionLayer().getRowIndexByPosition(4));

        Group group1 = this.rowGroupHeaderLayer.getGroupByPosition(1, 0);
        assertNotNull(group1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(1, 4);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderUngroupedToRightWithFirstHidden() {
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first row in third group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 9));

        // ungroup first group
        this.rowGroupHeaderLayer.removeGroup(0);

        // reorder ungrouped row to end of second group
        this.gridLayer.doCommand(new RowReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new RowReorderEndCommand(this.gridLayer, 9));

        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(1, 3);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.rowGroupHeaderLayer.getGroupByPosition(1, 8);
        assertNotNull(group3);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

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
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first row in third group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 9));

        // ungroup first group
        this.rowGroupHeaderLayer.removeGroup(0);

        // reorder ungrouped row to end of second group
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 1, 9));

        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(1, 3);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.rowGroupHeaderLayer.getGroupByPosition(1, 8);
        assertNotNull(group3);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

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
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroup(0, "Test", 7, 1);

        // hide first row in third group and last row in second group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 8, 9));

        // try to reorder group 1 between 2 and 3
        this.gridLayer.doCommand(new RowGroupReorderCommand(this.gridLayer, 1, 1, 8));

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(3);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(9);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(9, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());

        // show all again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

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
        // configure the row groups
        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup(1, "Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup(1, "Personal", 11, 3);

        // hide first row in third group
        this.gridLayer.doCommand(new MultiRowHideCommand(this.gridLayer, 9));

        // reorder first and second row to second group end
        this.gridLayer.doCommand(new MultiRowReorderCommand(this.gridLayer, Arrays.asList(1, 2), 9));

        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(2, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(3, cell.getDataValue());
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(20, cell.getBounds().height);
        assertEquals(80, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 1);
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(3, cell.getRowIndex());
        assertEquals(1, cell.getRowSpan());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(4, cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(80, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 2);
        assertEquals(2, cell.getOriginRowPosition());
        assertEquals(2, cell.getRowPosition());
        assertEquals(4, cell.getRowIndex());
        assertEquals(6, cell.getRowSpan());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(40, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(20, cell.getBounds().width);
        assertEquals(120, cell.getBounds().height);

        assertNull(this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(0));
        assertNull(this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(1));

        Group group = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(2);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());

        Group group1 = this.rowGroupHeaderLayer.getGroupModel(1).getGroupByPosition(8);
        assertEquals(8, group1.getStartIndex());
        assertEquals(9, group1.getVisibleStartIndex());
        assertEquals(8, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

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
        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        // also check with group change
        // this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        // this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroupingLevel();
        this.rowGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.rowGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder row 10 to end
        this.gridLayer.doCommand(new RowReorderStartCommand(this.gridLayer, 10));
        this.gridLayer.doCommand(new RowReorderEndCommand(this.gridLayer, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getRowIndexByPosition(13));
    }

    @Test
    public void shouldReorderToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        // also check with group change
        // this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        // this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroupingLevel();
        this.rowGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.rowGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder row 10 to end
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 10, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getRowIndexByPosition(13));
    }

    @Test
    public void shouldMultiReorderToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        // also check with group change
        // this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        // this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        this.rowGroupHeaderLayer.addGroupingLevel();
        this.rowGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.rowGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder row 10 to end
        this.gridLayer.doCommand(new MultiRowReorderCommand(this.gridLayer, Arrays.asList(10), 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getRowIndexByPosition(13));
    }

    @Test
    public void shouldDragReorderGroupToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.rowGroupHeaderLayer.clearAllGroups();

        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Test", 9, 1);

        this.rowGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.rowGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder row 10 to end
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 0, 10));
        this.gridLayer.doCommand(new RowGroupReorderEndCommand(this.gridLayer, 0, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getRowIndexByPosition(13));
    }

    @Test
    public void shouldDragReorderUnbreakableGroupToEndWithLevel1AllAndNoLevel0() {
        // increase the client area to show all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 300);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.rowGroupHeaderLayer.clearAllGroups();
        this.rowGroupHeaderLayer.setDefaultUnbreakable(true);

        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Test", 9, 1);

        this.rowGroupHeaderLayer.addGroup(1, "Person with Address", 0, 8);
        this.rowGroupHeaderLayer.addGroup(1, "Additional Information", 8, 6);

        // reorder column 10 to end
        this.gridLayer.doCommand(new RowGroupReorderStartCommand(this.gridLayer, 0, 10));
        this.gridLayer.doCommand(new RowGroupReorderEndCommand(this.gridLayer, 0, 15));

        // check that reorder actually happened
        assertEquals(9, this.selectionLayer.getRowIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLastSpanningSecondLevel() {
        // remove first position from first group
        this.rowGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make groups unbreakable
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new RowHideCommand(this.gridLayer, 8));

        Group group1 = this.rowGroupHeaderLayer.getGroupByPosition(0, 1);
        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(0, 4);

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
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 1, 8));

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

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 6));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

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

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLastSameGroupBorders() {
        // remove second level group
        this.rowGroupHeaderLayer.removeGroup(1, 4);

        // specify two second level groups with same border as first level
        this.rowGroupHeaderLayer.addGroup(1, "Test1", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Test2", 8, 3);

        // remove first position from first group
        this.rowGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make groups unbreakable
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new RowHideCommand(this.gridLayer, 8));

        Group group1 = this.rowGroupHeaderLayer.getGroupByPosition(0, 1);
        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(0, 4);

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
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 1, 8));

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

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 6));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

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

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(0, 7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLastSpanningFirstLevel() {
        // remove second level group
        this.rowGroupHeaderLayer.removeGroup(1, 4);

        // specify two second level groups with same border as first level
        this.rowGroupHeaderLayer.addGroup(1, "Test1", 4, 4);
        this.rowGroupHeaderLayer.addGroup(1, "Test2", 8, 3);

        // remove first level group
        this.rowGroupHeaderLayer.removeGroup(0, 4);
        this.rowGroupHeaderLayer.removeGroup(0, 8);

        // specify spanning first level group
        this.rowGroupHeaderLayer.addGroup(0, "FirstLevel", 4, 7);

        // remove first position from first group
        this.rowGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make groups unbreakable
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 4, true);
        this.rowGroupHeaderLayer.setGroupUnbreakable(1, 8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new RowHideCommand(this.gridLayer, 8));

        Group group2 = this.rowGroupHeaderLayer.getGroupByPosition(1, 4);

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // reorder first position between second and third group
        this.gridLayer.doCommand(new RowReorderCommand(this.gridLayer, 1, 8));

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(1, 6));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllRowsCommand());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertNull(this.rowGroupHeaderLayer.getGroupByPosition(1, 7));
    }

}
