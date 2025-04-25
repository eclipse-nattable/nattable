/*******************************************************************************
 * Copyright (c) 2019, 2025 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import org.eclipse.nebula.widgets.nattable.group.command.CreateColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.RemoveColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.UngroupColumnCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.command.ColumnGroupReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.group.performance.config.GroupHeaderConfigLabels;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.FixedScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualUpdateEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ResetColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnGroupHeaderLayerTest {

    GroupModel groupModel;
    HoverLayer columnHeaderHoverLayer;
    ColumnHeaderLayer columnHeaderLayer;
    ColumnGroupHeaderLayer columnGroupHeaderLayer;
    ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
    SelectionLayer selectionLayer;
    GridLayer gridLayer;

    @BeforeEach
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
        this.columnHeaderHoverLayer = new HoverLayer(columnHeaderDataLayer);
        this.columnHeaderLayer = new ColumnHeaderLayer(this.columnHeaderHoverLayer, viewportLayer, this.selectionLayer);
        this.columnGroupHeaderLayer = new ColumnGroupHeaderLayer(this.columnHeaderLayer, this.selectionLayer);

        this.groupModel = this.columnGroupHeaderLayer.getGroupModel();

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
        verifyCleanState();
    }

    private void verifyCleanState() {
        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals(3, group1.getGroupEndPosition(this.selectionLayer));

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals(7, group2.getGroupEndPosition(this.selectionLayer));

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals(10, group3.getGroupEndPosition(this.selectionLayer));

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals(13, group4.getGroupEndPosition(this.selectionLayer));
    }

    @Test
    public void shouldRenderColumnGroups() {
        assertEquals(11, this.gridLayer.getColumnCount());
        assertEquals(12, this.gridLayer.getRowCount());

        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldReturnSameCellForDifferentColumnPositions() {
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(cell, this.columnGroupHeaderLayer.getCellByPosition(1, 0));
        assertEquals(cell, this.columnGroupHeaderLayer.getCellByPosition(2, 0));
        assertEquals(cell, this.columnGroupHeaderLayer.getCellByPosition(3, 0));

        // the next cell is the start of the next column group
        assertFalse(cell.equals(this.columnGroupHeaderLayer.getCellByPosition(4, 0)));
    }

    @Test
    public void shouldRenderGroupInScrolledState() {
        assertEquals(0, this.gridLayer.getBodyLayer().getColumnIndexByPosition(0));

        // scroll
        this.gridLayer.doCommand(new ShowColumnInViewportCommand(11));

        assertEquals(2, this.gridLayer.getBodyLayer().getColumnIndexByPosition(0));

        int visibleStartPosition = this.groupModel.getGroupByPosition(0).getVisibleStartPosition();
        assertEquals(0, visibleStartPosition);
        assertEquals(2, this.columnGroupHeaderLayer.getColumnIndexByPosition(visibleStartPosition));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(-2, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(new Rectangle(-230, 0, 400, 20), cell.getBounds());
    }

    @Test
    public void shouldCheckIfPartOfGroup() {
        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // set second group as unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {

            // check part of a group
            if (column == 3) {
                assertFalse(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            } else {
                assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            }

            // check part of an unbreakable group
            if (column >= 4 && column < 8) {
                assertTrue(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
            } else {
                assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
            }
        }

    }

    @Test
    public void shouldRemoveLastColumnFromGroup() {
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);
    }

    @Test
    public void shouldRemoveFirstColumnFromGroup() {
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // remove first column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldRemoveMiddleColumnFromGroup() {
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // remove middle column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 1);

        // the result is the same as removing the last column in a group, as it
        // is not possible to split a column group by removing a middle group
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);
    }

    @Test
    public void shouldAddColumnToEndOfGroup() {
        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // add the column back again
        this.columnGroupHeaderLayer.addPositionsToGroup(0, 0, 3);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldAddColumnAtStartOfGroup() {
        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // add the column as first column to the second group
        this.columnGroupHeaderLayer.addPositionsToGroup(0, 4, 3);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldHideColumnInMiddleOfGroup() {
        if (this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 3))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(4, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(7, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(10);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(10, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideLastColumnInGroup() {
        if (this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 4))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(4, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(7, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(10);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(10, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideFirstColumnInGroup() {
        if (this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 5))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(4, group2.getStartIndex());
            assertEquals(5, group2.getVisibleStartIndex());
            assertEquals(4, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(3, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(7, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(10);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(10, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleMiddleColumns() {
        if (this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 3, 6, 10))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(300, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
            assertEquals(6, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(600, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(3, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(6, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(2, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(8, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleFirstColumns() {
        if (this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 1, 5, 9))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(300, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
            assertEquals(6, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(600, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(1, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group2.getStartIndex());
            assertEquals(5, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(3, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
            assertEquals(8, group3.getStartIndex());
            assertEquals(9, group3.getVisibleStartIndex());
            assertEquals(6, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(2, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(8, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleLastColumns() {
        // trigger the command on the SelectionLayer as we hide a column that is
        // not visible which would be blocked by command handling through the
        // ViewportLayer
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 3, 7, 10))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(300, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
            assertEquals(6, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(600, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(3, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(6, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(2, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(8, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleMixedColumns() {
        // last/first/middle
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 3, 4, 9))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(300, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
            assertEquals(6, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(600, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group2.getStartIndex());
            assertEquals(5, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(3, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(6, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(2, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(8, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleColumnsInOneGroup() {
        // first two in second group
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 4, 5))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
            assertEquals(6, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(600, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(9, 0);
            assertEquals(9, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(900, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(4, group2.getStartIndex());
            assertEquals(6, group2.getVisibleStartIndex());
            assertEquals(4, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(2, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(6, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(9);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(9, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideAllColumnsInOneGroup() {
        // second group
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 4, 5, 6, 7))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
            assertEquals(7, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(700, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(4, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(7, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());

            // this group is not visible by column position, so we retrieve it
            // by name
            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByName("Address");
            assertEquals(4, group2.getStartIndex());
            assertEquals(-1, group2.getVisibleStartIndex());
            assertEquals(-1, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(0, group2.getVisibleSpan());
            assertEquals(-1, group2.getGroupEndPosition(this.selectionLayer));
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideColumnBetweenGroups() {
        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // hide column
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3))) {
            cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(300, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
            assertEquals(4, group.getStartIndex());
            assertEquals(4, group.getVisibleStartIndex());
            assertEquals(3, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(4, group.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show column again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell.getOriginColumnPosition());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(2, cell.getRowSpan());
            assertEquals("Married", cell.getDataValue());
            assertEquals(300, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(100, cell.getBounds().width);
            assertEquals(40, cell.getBounds().height);

            Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2);
            assertEquals(0, group.getStartIndex());
            assertEquals(0, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(3, group.getOriginalSpan());
            assertEquals(3, group.getVisibleSpan());

            assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3));

            group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(4, group.getStartIndex());
            assertEquals(4, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(4, group.getVisibleSpan());
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleColumnsInMiddleOfTableToReduceColumnCountMoreThanEventEnd() {
        // this test is for handling modification of the
        // HideColumnPositionEventRanges in case the column count after hide is
        // less than the end of the hide event range

        // this hides completely the Address and Facts group and the first item
        // of the Personal group
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 4, 5, 6, 7, 8, 9, 10, 11))) {
            assertEquals(6, this.selectionLayer.getColumnCount());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(12, cell.getColumnIndex());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(11, group4.getStartIndex());
            assertEquals(12, group4.getVisibleStartIndex());
            assertEquals(4, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(2, group4.getVisibleSpan());

            // these groups are not visible by column position, so we retrieve
            // it by name
            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByName("Address");
            assertEquals(4, group2.getStartIndex());
            assertEquals(-1, group2.getVisibleStartIndex());
            assertEquals(-1, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(0, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByName("Facts");
            assertEquals(8, group3.getStartIndex());
            assertEquals(-1, group3.getVisibleStartIndex());
            assertEquals(-1, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(0, group3.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleColumnsInMiddleOfTableTwiceToReduceColumnCountMoreThanEventEnd() {
        // this test is for handling modification of the
        // HideColumnPositionEventRanges in case the column count after hide is
        // less than the end of the hide event range

        // this hides completely the Address and Facts group and the first item
        // of the Personal group via two separate commands
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 4, 5, 6, 7));
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 4, 5, 6, 7));

        assertEquals(6, this.selectionLayer.getColumnCount());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(12, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(11, group4.getStartIndex());
        assertEquals(12, group4.getVisibleStartIndex());
        assertEquals(4, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        // these groups are not visible by column position, so we retrieve
        // it by name
        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByName("Address");
        assertEquals(4, group2.getStartIndex());
        assertEquals(-1, group2.getVisibleStartIndex());
        assertEquals(-1, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(0, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByName("Facts");
        assertEquals(8, group3.getStartIndex());
        assertEquals(-1, group3.getVisibleStartIndex());
        assertEquals(-1, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(0, group3.getVisibleSpan());

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            verifyCleanState();
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldCollapseExpandGroup() {
        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // collapse group with no static indexes
        this.columnGroupHeaderLayer.collapseGroup(0);

        int[] hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexesArray();
        assertEquals(3, hiddenColumnIndexes.length);
        assertEquals(1, hiddenColumnIndexes[0]);
        assertEquals(2, hiddenColumnIndexes[1]);
        assertEquals(3, hiddenColumnIndexes[2]);

        assertTrue(this.columnGroupExpandCollapseLayer.isColumnIndexHidden(1));
        assertTrue(this.columnGroupExpandCollapseLayer.isColumnIndexHidden(2));
        assertTrue(this.columnGroupExpandCollapseLayer.isColumnIndexHidden(3));
        assertTrue(this.columnGroupExpandCollapseLayer.hasHiddenColumns());

        assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertTrue(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // expand group with no static indexes
        this.columnGroupHeaderLayer.expandGroup(0);

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexesArray();
        assertEquals(0, hiddenColumnIndexes.length);

        assertFalse(this.columnGroupExpandCollapseLayer.isColumnIndexHidden(1));
        assertFalse(this.columnGroupExpandCollapseLayer.isColumnIndexHidden(2));
        assertFalse(this.columnGroupExpandCollapseLayer.isColumnIndexHidden(3));
        assertFalse(this.columnGroupExpandCollapseLayer.hasHiddenColumns());

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertFalse(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldExpandCollapseNonVisibleGroup() {
        // with the ColumnGroupExpandCollapseCommand it should be possible to
        // expand and collapse groups outside the viewport
        this.gridLayer.doCommand(new ColumnGroupExpandCollapseCommand(this.selectionLayer, 11));

        assertEquals(12, this.selectionLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleSpan());
        assertTrue(group.isCollapsed());

        this.gridLayer.doCommand(new ColumnGroupExpandCollapseCommand(this.selectionLayer, 11));

        assertEquals(14, this.selectionLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldCollapseGroupWithStaticColumns() {
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 4, 6, 7);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // collapse group with static indexes
        this.columnGroupHeaderLayer.collapseGroup(4);

        assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // expand group with static indexes
        this.columnGroupHeaderLayer.expandGroup(4);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldShowFirstVisibleColumnOnCollapseWhenFirstColumnIsHidden() {
        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // hide first column in group
        if (this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1))) {
            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            // collapse group with no static indexes
            this.columnGroupHeaderLayer.collapseGroup(0);

            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group.isCollapsed());
            assertEquals(0, group.getStartIndex());
            assertEquals(1, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(1, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());

            // expand group with no static indexes
            this.columnGroupHeaderLayer.expandGroup(0);

            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertFalse(group.isCollapsed());
            assertEquals(0, group.getStartIndex());
            assertEquals(1, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(3, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(1, cell.getColumnIndex());
            assertEquals(3, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertFalse(group.isCollapsed());
            assertEquals(0, group.getStartIndex());
            assertEquals(0, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(4, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(0, cell.getColumnIndex());
            assertEquals(4, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldNotShowHiddenColumnInCollapsedGroup() {
        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // hide column in group
        if (this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 3))) {
            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            // collapse group
            this.columnGroupHeaderLayer.collapseGroup(0);

            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group.isCollapsed());
            assertEquals(0, group.getStartIndex());
            assertEquals(0, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(0, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        // collapsed columns should stay hidden
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group.isCollapsed());
            assertEquals(0, group.getStartIndex());
            assertEquals(0, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(0, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand again to check that the group state is not changed
        this.columnGroupHeaderLayer.expandGroup(0);

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertFalse(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldNotShowHiddenFirstColumnInCollapsedGroup() {
        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // hide column in group
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 4))) {
            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            // collapse group
            this.columnGroupHeaderLayer.collapseGroup(4);

            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(5, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(5, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        // collapsed columns should stay hidden
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(4, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(4, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand again to check that the group state is not changed
        this.columnGroupHeaderLayer.expandGroup(4);

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldNotShowHiddenLastColumnInCollapsedGroup() {
        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // hide column in group
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 7))) {
            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            // collapse group
            this.columnGroupHeaderLayer.collapseGroup(4);

            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(4, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(4, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        // collapsed columns should stay hidden
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(4, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(4, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand again to check that the group state is not changed
        this.columnGroupHeaderLayer.expandGroup(4);

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldNotShowHiddenColumnsInMultipleGroups() {
        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // hide last column in first group and first column in second group
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 3, 4))) {
            assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

            // collapse group
            this.columnGroupHeaderLayer.collapseGroup(4);
            this.columnGroupHeaderLayer.collapseGroup(0);

            assertEquals(8, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group1.isCollapsed());
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(1, group1.getVisibleSpan());

            ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell1.getOriginColumnPosition());
            assertEquals(0, cell1.getColumnPosition());
            assertEquals(0, cell1.getColumnIndex());
            assertEquals(1, cell1.getColumnSpan());
            assertEquals(1, cell1.getRowSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1);
            assertTrue(group2.isCollapsed());
            assertEquals(4, group2.getStartIndex());
            assertEquals(5, group2.getVisibleStartIndex());
            assertEquals(1, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(1, group2.getVisibleSpan());

            ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
            assertEquals(1, cell2.getOriginColumnPosition());
            assertEquals(1, cell2.getColumnPosition());
            assertEquals(5, cell2.getColumnIndex());
            assertEquals(1, cell2.getColumnSpan());
            assertEquals(1, cell2.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        // collapsed columns should stay hidden
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(8, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group1.isCollapsed());
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(1, group1.getVisibleSpan());

            ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell1.getOriginColumnPosition());
            assertEquals(0, cell1.getColumnPosition());
            assertEquals(0, cell1.getColumnIndex());
            assertEquals(1, cell1.getColumnSpan());
            assertEquals(1, cell1.getRowSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(1);
            assertTrue(group2.isCollapsed());
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(1, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(1, group2.getVisibleSpan());

            ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
            assertEquals(1, cell2.getOriginColumnPosition());
            assertEquals(1, cell2.getColumnPosition());
            assertEquals(4, cell2.getColumnIndex());
            assertEquals(1, cell2.getColumnSpan());
            assertEquals(1, cell2.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand again to check that the group state is not changed
        this.columnGroupHeaderLayer.expandGroup(1);
        this.columnGroupHeaderLayer.expandGroup(0);

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertFalse(group1.isCollapsed());
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell1.getOriginColumnPosition());
        assertEquals(0, cell1.getColumnPosition());
        assertEquals(0, cell1.getColumnIndex());
        assertEquals(4, cell1.getColumnSpan());
        assertEquals(1, cell1.getRowSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group2.isCollapsed());
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell2.getOriginColumnPosition());
        assertEquals(4, cell2.getColumnPosition());
        assertEquals(4, cell2.getColumnIndex());
        assertEquals(4, cell2.getColumnSpan());
        assertEquals(1, cell2.getRowSpan());
    }

    @Test
    public void shouldShowNonGroupColumnIfAdjacentGroupsAreCollapsed() {
        // remove a column between two groups
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // hide that column
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3))) {
            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertFalse(group1.isCollapsed());
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(3, group1.getOriginalSpan());
            assertEquals(3, group1.getVisibleSpan());

            ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell1.getOriginColumnPosition());
            assertEquals(0, cell1.getColumnPosition());
            assertEquals(0, cell1.getColumnIndex());
            assertEquals(3, cell1.getColumnSpan());
            assertEquals(1, cell1.getRowSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(3);
            assertFalse(group2.isCollapsed());
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(3, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(4, group2.getVisibleSpan());

            ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
            assertEquals(3, cell2.getOriginColumnPosition());
            assertEquals(3, cell2.getColumnPosition());
            assertEquals(4, cell2.getColumnIndex());
            assertEquals(4, cell2.getColumnSpan());
            assertEquals(1, cell2.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // collapse both groups
        this.columnGroupHeaderLayer.collapseGroup(4);
        this.columnGroupHeaderLayer.collapseGroup(0);

        assertEquals(8, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group11 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertTrue(group11.isCollapsed());
        assertEquals(0, group11.getStartIndex());
        assertEquals(0, group11.getVisibleStartIndex());
        assertEquals(0, group11.getVisibleStartPosition());
        assertEquals(3, group11.getOriginalSpan());
        assertEquals(1, group11.getVisibleSpan());

        ILayerCell cell11 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell11.getOriginColumnPosition());
        assertEquals(0, cell11.getColumnPosition());
        assertEquals(0, cell11.getColumnIndex());
        assertEquals(1, cell11.getColumnSpan());
        assertEquals(1, cell11.getRowSpan());

        Group group22 = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertTrue(group22.isCollapsed());
        assertEquals(4, group22.getStartIndex());
        assertEquals(4, group22.getVisibleStartIndex());
        assertEquals(1, group22.getVisibleStartPosition());
        assertEquals(4, group22.getOriginalSpan());
        assertEquals(1, group22.getVisibleSpan());

        ILayerCell cell22 = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell22.getOriginColumnPosition());
        assertEquals(1, cell22.getColumnPosition());
        assertEquals(4, cell22.getColumnIndex());
        assertEquals(1, cell22.getColumnSpan());
        assertEquals(1, cell22.getRowSpan());

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(9, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group1.isCollapsed());
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(3, group1.getOriginalSpan());
            assertEquals(1, group1.getVisibleSpan());

            ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell1.getOriginColumnPosition());
            assertEquals(0, cell1.getColumnPosition());
            assertEquals(0, cell1.getColumnIndex());
            assertEquals(1, cell1.getColumnSpan());
            assertEquals(1, cell1.getRowSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
            assertTrue(group2.isCollapsed());
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(2, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(1, group2.getVisibleSpan());

            ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
            assertEquals(2, cell2.getOriginColumnPosition());
            assertEquals(2, cell2.getColumnPosition());
            assertEquals(4, cell2.getColumnIndex());
            assertEquals(1, cell2.getColumnSpan());
            assertEquals(1, cell2.getRowSpan());

            ILayerCell cell3 = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
            assertEquals(1, cell3.getOriginColumnPosition());
            assertEquals(1, cell3.getColumnPosition());
            assertEquals(3, cell3.getColumnIndex());
            assertEquals(1, cell3.getColumnSpan());
            assertEquals(2, cell3.getRowSpan());
            assertEquals("Married", cell3.getDataValue());
        } else {
            fail("Columns not shown again");
        }

        // expand both groups again
        this.columnGroupHeaderLayer.expandGroup(2);
        this.columnGroupHeaderLayer.expandGroup(0);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        group11 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertFalse(group11.isCollapsed());
        assertEquals(0, group11.getStartIndex());
        assertEquals(0, group11.getVisibleStartIndex());
        assertEquals(0, group11.getVisibleStartPosition());
        assertEquals(3, group11.getOriginalSpan());
        assertEquals(3, group11.getVisibleSpan());

        cell11 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell11.getOriginColumnPosition());
        assertEquals(0, cell11.getColumnPosition());
        assertEquals(0, cell11.getColumnIndex());
        assertEquals(3, cell11.getColumnSpan());
        assertEquals(1, cell11.getRowSpan());

        group22 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group22.isCollapsed());
        assertEquals(4, group22.getStartIndex());
        assertEquals(4, group22.getVisibleStartIndex());
        assertEquals(4, group22.getVisibleStartPosition());
        assertEquals(4, group22.getOriginalSpan());
        assertEquals(4, group22.getVisibleSpan());

        cell22 = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell22.getOriginColumnPosition());
        assertEquals(4, cell22.getColumnPosition());
        assertEquals(4, cell22.getColumnIndex());
        assertEquals(4, cell22.getColumnSpan());
        assertEquals(1, cell22.getRowSpan());

        ILayerCell cell33 = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell33.getOriginColumnPosition());
        assertEquals(3, cell33.getColumnPosition());
        assertEquals(3, cell33.getColumnIndex());
        assertEquals(1, cell33.getColumnSpan());
        assertEquals(2, cell33.getRowSpan());
        assertEquals("Married", cell33.getDataValue());
    }

    @Test
    public void shouldOnlyShowNonGroupColumnIfAdjacentGroupsAreCollapsed() {
        // remove a column between two groups
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // hide the last column of the first group
        // hide the non grouped column
        // hide the first column of the second groupd
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2, 3, 4))) {
            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertFalse(group1.isCollapsed());
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(3, group1.getOriginalSpan());
            assertEquals(2, group1.getVisibleSpan());

            ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell1.getOriginColumnPosition());
            assertEquals(0, cell1.getColumnPosition());
            assertEquals(0, cell1.getColumnIndex());
            assertEquals(2, cell1.getColumnSpan());
            assertEquals(1, cell1.getRowSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
            assertFalse(group2.isCollapsed());
            assertEquals(4, group2.getStartIndex());
            assertEquals(5, group2.getVisibleStartIndex());
            assertEquals(2, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(3, group2.getVisibleSpan());

            ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
            assertEquals(2, cell2.getOriginColumnPosition());
            assertEquals(2, cell2.getColumnPosition());
            assertEquals(5, cell2.getColumnIndex());
            assertEquals(3, cell2.getColumnSpan());
            assertEquals(1, cell2.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // collapse both groups
        this.columnGroupHeaderLayer.collapseGroup(2);
        this.columnGroupHeaderLayer.collapseGroup(0);

        assertEquals(8, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group11 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertTrue(group11.isCollapsed());
        assertEquals(0, group11.getStartIndex());
        assertEquals(0, group11.getVisibleStartIndex());
        assertEquals(0, group11.getVisibleStartPosition());
        assertEquals(3, group11.getOriginalSpan());
        assertEquals(1, group11.getVisibleSpan());

        ILayerCell cell11 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell11.getOriginColumnPosition());
        assertEquals(0, cell11.getColumnPosition());
        assertEquals(0, cell11.getColumnIndex());
        assertEquals(1, cell11.getColumnSpan());
        assertEquals(1, cell11.getRowSpan());

        Group group22 = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertTrue(group22.isCollapsed());
        assertEquals(4, group22.getStartIndex());
        assertEquals(5, group22.getVisibleStartIndex());
        assertEquals(1, group22.getVisibleStartPosition());
        assertEquals(4, group22.getOriginalSpan());
        assertEquals(1, group22.getVisibleSpan());

        ILayerCell cell22 = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell22.getOriginColumnPosition());
        assertEquals(1, cell22.getColumnPosition());
        assertEquals(5, cell22.getColumnIndex());
        assertEquals(1, cell22.getColumnSpan());
        assertEquals(1, cell22.getRowSpan());

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(9, this.columnGroupExpandCollapseLayer.getColumnCount());

            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group1.isCollapsed());
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(3, group1.getOriginalSpan());
            assertEquals(1, group1.getVisibleSpan());

            ILayerCell cell1 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell1.getOriginColumnPosition());
            assertEquals(0, cell1.getColumnPosition());
            assertEquals(0, cell1.getColumnIndex());
            assertEquals(1, cell1.getColumnSpan());
            assertEquals(1, cell1.getRowSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
            assertTrue(group2.isCollapsed());
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(2, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(1, group2.getVisibleSpan());

            ILayerCell cell2 = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
            assertEquals(2, cell2.getOriginColumnPosition());
            assertEquals(2, cell2.getColumnPosition());
            assertEquals(4, cell2.getColumnIndex());
            assertEquals(1, cell2.getColumnSpan());
            assertEquals(1, cell2.getRowSpan());

            ILayerCell cell3 = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
            assertEquals(1, cell3.getOriginColumnPosition());
            assertEquals(1, cell3.getColumnPosition());
            assertEquals(3, cell3.getColumnIndex());
            assertEquals(1, cell3.getColumnSpan());
            assertEquals(2, cell3.getRowSpan());
            assertEquals("Married", cell3.getDataValue());
        } else {
            fail("Columns not shown again");
        }

        // expand both groups again
        this.columnGroupHeaderLayer.expandGroup(2);
        this.columnGroupHeaderLayer.expandGroup(0);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        group11 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertFalse(group11.isCollapsed());
        assertEquals(0, group11.getStartIndex());
        assertEquals(0, group11.getVisibleStartIndex());
        assertEquals(0, group11.getVisibleStartPosition());
        assertEquals(3, group11.getOriginalSpan());
        assertEquals(3, group11.getVisibleSpan());

        cell11 = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell11.getOriginColumnPosition());
        assertEquals(0, cell11.getColumnPosition());
        assertEquals(0, cell11.getColumnIndex());
        assertEquals(3, cell11.getColumnSpan());
        assertEquals(1, cell11.getRowSpan());

        group22 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group22.isCollapsed());
        assertEquals(4, group22.getStartIndex());
        assertEquals(4, group22.getVisibleStartIndex());
        assertEquals(4, group22.getVisibleStartPosition());
        assertEquals(4, group22.getOriginalSpan());
        assertEquals(4, group22.getVisibleSpan());

        cell22 = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell22.getOriginColumnPosition());
        assertEquals(4, cell22.getColumnPosition());
        assertEquals(4, cell22.getColumnIndex());
        assertEquals(4, cell22.getColumnSpan());
        assertEquals(1, cell22.getRowSpan());

        ILayerCell cell33 = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell33.getOriginColumnPosition());
        assertEquals(3, cell33.getColumnPosition());
        assertEquals(3, cell33.getColumnIndex());
        assertEquals(1, cell33.getColumnSpan());
        assertEquals(2, cell33.getRowSpan());
        assertEquals("Married", cell33.getDataValue());
    }

    @Test
    public void shouldHideStaticColumnInCollapsedState() {
        // set last two columns in second group as static
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 4, 6, 7);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // collapse group with static indexes
        this.columnGroupHeaderLayer.collapseGroup(4);

        assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide first static column
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 4))) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(7, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(7, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // expand group with static indexes
        this.columnGroupHeaderLayer.expandGroup(4);

        assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldShowHiddenFirstStaticColumnInCollapsedState() {
        // set last two columns in second group as static
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 4, 6, 7);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // collapse group with static indexes
        this.columnGroupHeaderLayer.collapseGroup(4);

        assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide first static column
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 4))) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(7, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(7, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(6, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(2, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(6, cell.getColumnIndex());
            assertEquals(2, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand group with static indexes
        this.columnGroupHeaderLayer.expandGroup(4);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldShowHiddenLastStaticColumnInCollapsedState() {
        // set last two columns in second group as static
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 4, 6, 7);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // collapse group with static indexes
        this.columnGroupHeaderLayer.collapseGroup(4);

        assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide last static column
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 5))) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(6, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(6, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(6, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(2, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(6, cell.getColumnIndex());
            assertEquals(2, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand group with static indexes
        this.columnGroupHeaderLayer.expandGroup(4);

        verifyCleanState();
    }

    @Test
    public void shouldShowAllHiddenStaticColumnsInCollapsedState() {
        // set last two columns in second group as static
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 4, 6, 7);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        // collapse group with static indexes
        this.columnGroupHeaderLayer.collapseGroup(4);

        assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide all static columns
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 4, 5))) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertFalse(group.isCollapsed());
            assertEquals("Facts", group.getName());
            assertEquals(8, group.getStartIndex());
            assertEquals(8, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(3, group.getOriginalSpan());
            assertEquals(3, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(8, cell.getColumnIndex());
            assertEquals(3, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(4);
            assertTrue(group.isCollapsed());
            assertEquals(4, group.getStartIndex());
            assertEquals(6, group.getVisibleStartIndex());
            assertEquals(4, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(2, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnPosition());
            assertEquals(6, cell.getColumnIndex());
            assertEquals(2, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand group with static indexes
        this.columnGroupHeaderLayer.expandGroup(4);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertFalse(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldHideShowFirstGroupInCollapsedState() {
        // collapse group without static indexes
        this.columnGroupHeaderLayer.collapseGroup(0);

        assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertTrue(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide visible column in group
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 0))) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertFalse(group.isCollapsed());
            assertEquals("Address", group.getName());
            assertEquals(4, group.getStartIndex());
            assertEquals(4, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(4, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(4, cell.getColumnIndex());
            assertEquals(4, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());

            // check group by name
            group = this.columnGroupHeaderLayer.getGroupByName("Person");
            assertNotNull(group);
            assertEquals(0, group.getStartIndex());
            assertEquals(-1, group.getVisibleStartIndex());
            assertEquals(-1, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(0, group.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            group = this.columnGroupHeaderLayer.getGroupByPosition(0);
            assertTrue(group.isCollapsed());
            assertEquals(0, group.getStartIndex());
            assertEquals(0, group.getVisibleStartIndex());
            assertEquals(0, group.getVisibleStartPosition());
            assertEquals(4, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(0, cell.getColumnPosition());
            assertEquals(0, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // expand group
        this.columnGroupHeaderLayer.expandGroup(0);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        verifyCleanState();
    }

    @Test
    public void shouldHideShowLastGroupInCollapsedState() {
        // collapse last group without static indexes
        this.columnGroupHeaderLayer.collapseGroup(11);

        assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(11);
        assertTrue(group.isCollapsed());
        assertEquals(11, group.getStartIndex());
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(11, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide visible column in group
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 11))) {
            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            group = this.columnGroupHeaderLayer.getGroupByPosition(11);
            assertNull(group);

            // check group by name
            group = this.columnGroupHeaderLayer.getGroupByName("Personal");
            assertNotNull(group);
            // it is the last column so we where not able to update
            assertEquals(11, group.getStartIndex());
            assertEquals(-1, group.getVisibleStartIndex());
            assertEquals(-1, group.getVisibleStartPosition());
            assertEquals(3, group.getOriginalSpan());
            assertEquals(0, group.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(12, this.columnGroupExpandCollapseLayer.getColumnCount());

            group = this.columnGroupHeaderLayer.getGroupByPosition(11);
            assertTrue(group.isCollapsed());
            assertEquals(11, group.getStartIndex());
            assertEquals(11, group.getVisibleStartIndex());
            assertEquals(11, group.getVisibleStartPosition());
            assertEquals(3, group.getOriginalSpan());
            assertEquals(1, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals(11, cell.getColumnPosition());
            assertEquals(11, cell.getColumnIndex());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // scroll to show the last column only a bit

        // expand group
        this.columnGroupHeaderLayer.expandGroup(11);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        verifyCleanState();
    }

    @Test
    public void shouldHideShowLastGroupInCollapsedStateWithStatics() {
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 11, 12, 13);

        // collapse last group with static indexes
        this.columnGroupHeaderLayer.collapseGroup(11);

        assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(11);
        assertTrue(group.isCollapsed());
        assertEquals(11, group.getStartIndex());
        assertEquals(12, group.getVisibleStartIndex());
        assertEquals(11, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnPosition());
        assertEquals(12, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());

        // hide visible column in group
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 11, 12))) {
            assertEquals(11, this.columnGroupExpandCollapseLayer.getColumnCount());

            group = this.columnGroupHeaderLayer.getGroupByPosition(11);
            assertNull(group);

            // check group by name
            group = this.columnGroupHeaderLayer.getGroupByName("Personal");
            assertNotNull(group);
            // it is the last column so we where not able to update
            assertEquals(11, group.getStartIndex());
            assertEquals(-1, group.getVisibleStartIndex());
            assertEquals(-1, group.getVisibleStartPosition());
            assertEquals(3, group.getOriginalSpan());
            assertEquals(0, group.getVisibleSpan());
        } else {
            fail("Column not hidden");
        }

        // show all columns again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            assertEquals(13, this.columnGroupExpandCollapseLayer.getColumnCount());

            group = this.columnGroupHeaderLayer.getGroupByPosition(11);
            assertTrue(group.isCollapsed());
            assertEquals(11, group.getStartIndex());
            assertEquals(12, group.getVisibleStartIndex());
            assertEquals(11, group.getVisibleStartPosition());
            assertEquals(3, group.getOriginalSpan());
            assertEquals(2, group.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals(11, cell.getColumnPosition());
            assertEquals(12, cell.getColumnIndex());
            assertEquals(2, cell.getColumnSpan());
            assertEquals(1, cell.getRowSpan());
        } else {
            fail("Columns not shown again");
        }

        // scroll to show the last column only a bit

        // expand group
        this.columnGroupHeaderLayer.expandGroup(11);

        assertEquals(14, this.columnGroupExpandCollapseLayer.getColumnCount());

        verifyCleanState();
    }

    @Test
    public void shouldHideLastColumnInLastGroup() {
        // special test case for hide operations at the end of a table. The
        // HideColumnEvent is not transported up to the ColumnGroupHeaderLayer
        // because the conversion is not able to convert the position outside
        // the new structure and the start position will be -1

        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1100, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(2, cell.getColumnSpan());
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            // modifed verifyCleanState as we changed the client area
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1100, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(4, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(4, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(8, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(11, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleColumnsAfterFirstHideAtEndOfTable() {
        // special test case for hide operations at the end of a table. The
        // HideColumnEvent is not transported up to the ColumnGroupHeaderLayer
        // because the conversion is not able to convert the position outside
        // the new structure and the start position will be -1

        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        // first hide the first column in the last group
        if (this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 11))) {
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1100, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(2, cell.getColumnSpan());
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);
        } else {
            fail("Column not hidden");
        }

        // now hide the last column of the previous group and the now first
        // column of the last group. this looks like a contiguous selection, but
        // internally there is a gap. the second range
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 10, 11))) {
            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(8);
            assertEquals(8, group1.getStartIndex());
            assertEquals(8, group1.getVisibleStartIndex());
            assertEquals(8, group1.getVisibleStartPosition());
            assertEquals(3, group1.getOriginalSpan());
            assertEquals(2, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(10);
            assertEquals(11, group2.getStartIndex());
            assertEquals(13, group2.getVisibleStartIndex());
            assertEquals(10, group2.getVisibleStartPosition());
            assertEquals(3, group2.getOriginalSpan());
            assertEquals(1, group2.getVisibleSpan());

            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(10, 0);
            assertEquals(10, cell.getOriginColumnPosition());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1000, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(1, cell.getColumnSpan());
            assertEquals(100, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            // modifed verifyCleanState as we changed the client area
            ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1100, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(4, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(4, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(8, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
            assertEquals(11, group4.getStartIndex());
            assertEquals(11, group4.getVisibleStartIndex());
            assertEquals(11, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(3, group4.getVisibleSpan());
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldHideMultipleColumnsAfterCollapseWithStaticsAtEndOfTable() {
        // special test case for hide operations at the end of a table. The
        // HideColumnEvent is not transported up to the ColumnGroupHeaderLayer
        // because the conversion is not able to convert the position outside
        // the new structure and the start position will be -1

        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        // set last two columns in the last group static
        this.columnGroupHeaderLayer.addStaticColumnIndexesToGroup(0, 11, 12, 13);

        // first collapse the last group
        this.columnGroupHeaderLayer.collapseGroup(11);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(12, cell.getColumnIndex());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(1100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(2, cell.getColumnSpan());
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(11);
        assertTrue(group2.isCollapsed());
        assertEquals(11, group2.getStartIndex());
        assertEquals(12, group2.getVisibleStartIndex());
        assertEquals(11, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());

        // now hide the last column of the previous group and the now first
        // column of the last group. this looks like a contiguous selection, but
        // internally there is a gap. the second range
        if (this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 10, 11))) {
            Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(8);
            assertEquals(8, group1.getStartIndex());
            assertEquals(8, group1.getVisibleStartIndex());
            assertEquals(8, group1.getVisibleStartPosition());
            assertEquals(3, group1.getOriginalSpan());
            assertEquals(2, group1.getVisibleSpan());

            group2 = this.columnGroupHeaderLayer.getGroupByPosition(10);
            assertEquals(11, group2.getStartIndex());
            assertEquals(13, group2.getVisibleStartIndex());
            assertEquals(10, group2.getVisibleStartPosition());
            assertEquals(3, group2.getOriginalSpan());
            assertEquals(1, group2.getVisibleSpan());

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(10, 0);
            assertEquals(10, cell.getOriginColumnPosition());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1000, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(1, cell.getColumnSpan());
            assertEquals(100, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);
        } else {
            fail("Column not hidden");
        }

        // show again
        if (this.gridLayer.doCommand(new ShowAllColumnsCommand())) {
            // modifed verifyCleanState as we changed the client area
            cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
            assertEquals(0, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Person", cell.getDataValue());
            assertEquals(0, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
            assertEquals(4, cell.getOriginColumnPosition());
            assertEquals(4, cell.getColumnSpan());
            assertEquals("Address", cell.getDataValue());
            assertEquals(400, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(400, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
            assertEquals(8, cell.getOriginColumnPosition());
            assertEquals(3, cell.getColumnSpan());
            assertEquals("Facts", cell.getDataValue());
            assertEquals(800, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(300, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
            assertEquals(11, cell.getOriginColumnPosition());
            assertEquals(2, cell.getColumnSpan());
            assertEquals("Personal", cell.getDataValue());
            assertEquals(1100, cell.getBounds().x);
            assertEquals(0, cell.getBounds().y);
            assertEquals(200, cell.getBounds().width);
            assertEquals(20, cell.getBounds().height);

            Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
            assertEquals(0, group1.getStartIndex());
            assertEquals(0, group1.getVisibleStartIndex());
            assertEquals(0, group1.getVisibleStartPosition());
            assertEquals(4, group1.getOriginalSpan());
            assertEquals(4, group1.getVisibleSpan());

            group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
            assertEquals(4, group2.getStartIndex());
            assertEquals(4, group2.getVisibleStartIndex());
            assertEquals(4, group2.getVisibleStartPosition());
            assertEquals(4, group2.getOriginalSpan());
            assertEquals(4, group2.getVisibleSpan());

            Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
            assertEquals(8, group3.getStartIndex());
            assertEquals(8, group3.getVisibleStartIndex());
            assertEquals(8, group3.getVisibleStartPosition());
            assertEquals(3, group3.getOriginalSpan());
            assertEquals(3, group3.getVisibleSpan());

            Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
            assertTrue(group4.isCollapsed());
            assertEquals(11, group4.getStartIndex());
            assertEquals(12, group4.getVisibleStartIndex());
            assertEquals(11, group4.getVisibleStartPosition());
            assertEquals(3, group4.getOriginalSpan());
            assertEquals(2, group4.getVisibleSpan());
        } else {
            fail("Columns not shown again");
        }
    }

    @Test
    public void shouldExpandOnRemoveGroupByPosition() {
        this.columnGroupHeaderLayer.collapseGroup(4);

        Collection<Integer> hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertEquals(3, hiddenColumnIndexes.size());
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(5)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(6)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(7)));

        assertEquals(11, this.selectionLayer.getColumnCount());

        this.columnGroupHeaderLayer.removeGroup(4);

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertTrue(hiddenColumnIndexes.isEmpty());

        assertEquals(14, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldExpandOnRemoveGroupByName() {
        this.columnGroupHeaderLayer.collapseGroup(0);

        Collection<Integer> hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertEquals(3, hiddenColumnIndexes.size());
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(1)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(2)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(3)));

        assertEquals(11, this.selectionLayer.getColumnCount());

        this.columnGroupHeaderLayer.removeGroup("Person");

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertTrue(hiddenColumnIndexes.isEmpty());

        assertEquals(14, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldExpandOnRemovePositionFromGroup() {
        this.columnGroupHeaderLayer.collapseGroup("Address");

        Collection<Integer> hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertEquals(3, hiddenColumnIndexes.size());
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(5)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(6)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(7)));

        assertEquals(11, this.selectionLayer.getColumnCount());

        // Note: we can only remove the visible position
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 4);

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertTrue(hiddenColumnIndexes.isEmpty());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        assertEquals("Address", group.getName());
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(14, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldExpandOnRemovePositionsFromMultipleGroups() {
        this.columnGroupHeaderLayer.collapseGroup("Person");
        this.columnGroupHeaderLayer.collapseGroup("Address");

        Collection<Integer> hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertEquals(6, hiddenColumnIndexes.size());
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(1)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(2)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(3)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(5)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(6)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(7)));

        assertEquals(8, this.selectionLayer.getColumnCount());

        // Note: we can only remove the visible position
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0, 1);

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertTrue(hiddenColumnIndexes.isEmpty());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals("Person", group.getName());
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        assertEquals("Address", group.getName());
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(14, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldExpandOnAddPositionToGroup() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 7);

        this.columnGroupHeaderLayer.collapseGroup("Address");

        Collection<Integer> hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertEquals(2, hiddenColumnIndexes.size());
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(5)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(6)));

        assertEquals(12, this.selectionLayer.getColumnCount());

        this.columnGroupHeaderLayer.addPositionsToGroup("Address", 7);

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertTrue(hiddenColumnIndexes.isEmpty());

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        verifyCleanState();
    }

    @Test
    public void shouldExpandOnClearGroups() {

        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        this.columnGroupHeaderLayer.collapseGroup(11);
        this.columnGroupHeaderLayer.collapseGroup(8);
        this.columnGroupHeaderLayer.collapseGroup("Address");
        this.columnGroupHeaderLayer.collapseGroup("Person");

        Collection<Integer> hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertEquals(10, hiddenColumnIndexes.size());
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(1)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(2)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(3)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(5)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(6)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(7)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(9)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(10)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(12)));
        assertTrue(hiddenColumnIndexes.contains(Integer.valueOf(13)));

        assertEquals(4, this.selectionLayer.getColumnCount());

        this.columnGroupHeaderLayer.clearAllGroups();

        hiddenColumnIndexes = this.columnGroupExpandCollapseLayer.getHiddenColumnIndexes();
        assertTrue(hiddenColumnIndexes.isEmpty());

        assertEquals(14, this.columnGroupHeaderLayer.getColumnCount());

        assertTrue(this.columnGroupHeaderLayer.getGroupModel().getGroups().isEmpty());

        ILayerCell cell = null;
        for (int i = 0; i < 14; i++) {
            cell = this.columnGroupHeaderLayer.getCellByPosition(i, 0);
            assertEquals(i, cell.getColumnPosition());
            assertEquals(1, cell.getColumnSpan());
            assertEquals(2, cell.getRowSpan());
        }
    }

    @Test
    public void shouldCollapseExpandAll() {
        this.columnGroupHeaderLayer.collapseAllGroups();

        assertEquals(4, this.columnGroupHeaderLayer.getColumnCount());

        // verify collapsed states
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertNotNull(group);
        assertEquals("Person", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertNotNull(group);
        assertEquals("Address", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Facts", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(3);
        assertNotNull(group);
        assertEquals("Personal", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(11, group.getStartIndex());
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        // expand all
        this.columnGroupHeaderLayer.expandAllGroups();
        verifyCleanState();
    }

    @Test
    public void shouldLoadStateWithExpandCollapseStates() {
        verifyCleanState();

        Properties properties = new Properties();
        this.gridLayer.saveState("clean", properties);

        // collapse
        this.columnGroupHeaderLayer.collapseGroup("Address");

        this.gridLayer.saveState("one", properties);

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();

        // load single collapsed
        this.gridLayer.loadState("one", properties);

        assertEquals(11, this.selectionLayer.getColumnCount());

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertNotNull(group);
        assertEquals("Address", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        // collapse all
        this.columnGroupHeaderLayer.collapseAllGroups();

        this.gridLayer.saveState("all", properties);

        // load single collapsed
        this.gridLayer.loadState("one", properties);

        // verify only Address is collapsed and other groups are not
        // collapsed and in correct state
        assertEquals(11, this.selectionLayer.getColumnCount());

        group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertNotNull(group);
        assertEquals("Person", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertNotNull(group);
        assertEquals("Address", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        assertNotNull(group);
        assertEquals("Facts", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(8);
        assertNotNull(group);
        assertEquals("Personal", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(11, group.getStartIndex());
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(8, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // now load all collapsed again
        this.gridLayer.loadState("all", properties);

        // verify all collapsed
        assertEquals(4, this.columnGroupHeaderLayer.getColumnCount());

        // verify collapsed states
        group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertNotNull(group);
        assertEquals("Person", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertNotNull(group);
        assertEquals("Address", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Facts", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupByPosition(3);
        assertNotNull(group);
        assertEquals("Personal", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(11, group.getStartIndex());
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();
    }

    @Test
    public void shouldDragReorderWithinGroup() {
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 2));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // no changes in the group
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderFirstColumnWithinGroup() {
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToFirstColumnWithinGroup() {
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 1));

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderUngroupLastColumnInGroup() {
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 5));

        // group header cell has less column span
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
    }

    @Test
    public void shouldNotDragReorderUngroupMiddleColumnInGroup() {
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 3));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 3));

        // group header cell has not changed
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // group has not changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderRightAddUngroupedToGroupAsFirstColumn() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(3));

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // drag reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 5));

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderLeftAddUngroupedToGroupAsLastColumn() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // drag reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderUngroupFirstColumnInGroup() {
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group1.getStartIndex());
        assertEquals(5, group1.getVisibleStartIndex());
        assertEquals(5, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderBetweenGroupsLeft() {
        // second column in second group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 6));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderFirstColumnBetweenGroupsLeft() {
        // first column in second group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToFirstColumnBetweenGroupsLeft() {
        // first column in second group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderBetweenGroupsRight() {
        // last column in first group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        // to middle of second group
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderFirstColumnBetweenGroupsRight() {
        // middle column in first group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 3));
        // to first position in second group
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToFirstColumnBetweenGroupsRight() {
        // first column in first group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderWithinGroup() {
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 4));

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // no changes in the group
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderFirstColumnWithinGroup() {
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 4));

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderToFirstColumnWithinGroup() {
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 1));

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupLastColumnInGroup() {
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 4));

        // group header cell has less column span
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
    }

    @Test
    public void shouldNotReorderUngroupMiddleColumnInGroup() {
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 3));

        // group header cell has not changed
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // group has not changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderRightAddUngroupedToGroupAsFirstColumn() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // no changes in the group header cell
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(3));

        // only the visible start index should have changed
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // drag reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 5));

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderLeftAddUngroupedToGroupAsLastColumn() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // drag reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 4));

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible start index should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupFirstColumnInGroup() {
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group1.getStartIndex());
        assertEquals(5, group1.getVisibleStartIndex());
        assertEquals(5, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
    }

    @Test
    public void shouldReorderBetweenGroupsLeft() {
        // second column in second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 6, 4));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderFirstColumnBetweenGroupsLeft() {
        // first column in second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 4));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderToFirstColumnBetweenGroupsLeft() {
        // first column in second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderBetweenGroupsRight() {
        // last column in first group
        // to middle of second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderFirstColumnBetweenGroupsRight() {
        // middle column in first group
        // to first position in second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderToFirstColumnBetweenGroupsRight() {
        // first column in first group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // check group
        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupedAddColumnToGroupRight() {
        // remove group 1
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder third column to second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Lastname", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupedAddColumnToGroupLeft() {
        // remove group 2
        this.columnGroupHeaderLayer.removeGroup(4);

        // reorder fifth column to first group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 6, 3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupColumnFromGroupLeft() {
        // remove group 1
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder sixth column in second group to second column
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 6, 3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Lastname", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Housenumber", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderUngroupColumnGroupRight() {
        // remove group 2
        this.columnGroupHeaderLayer.removeGroup(4);

        // reorder third column out of group 1
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleUngroupedAddColumnToGroupRight() {
        // remove group 1
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder first and third column to second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(1, 3), 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Lastname", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

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

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleUngroupedAddColumnToGroupLeft() {
        // remove group 2
        this.columnGroupHeaderLayer.removeGroup(4);

        // reorder fifth and seventh column to first group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(5, 7), 2));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(6, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(600, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Housenumber", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleColumnsFromOneGroupToOtherGroupRight() {
        // reorder first and third column to second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(1, 3), 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

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

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(2, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(6, group2.getOriginalSpan());
        assertEquals(6, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleColumnsFromOneGroupToOtherGroupLeft() {
        // reorder fifth and seventh column to first group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(5, 7), 2));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(6, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(600, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(6, group1.getOriginalSpan());
        assertEquals(6, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(6, group2.getVisibleStartPosition());
        assertEquals(2, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleColumnsInsideGroupRight() {
        // reorder first two columns in second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(5, 6), 9));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(6, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleColumnsInsideGroupLeft() {
        // reorder first two columns in second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(7, 8), 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(6, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleColumnsInsideGroupToUngroupRight() {
        // reorder last two columns in second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(7, 8), 9));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Postalcode", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6));
    }

    @Test
    public void shouldReorderMultipleColumnsInsideGroupToUngroupLeft() {
        // reorder first two columns in second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(5, 6), 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Housenumber", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(6, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(6, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5));
    }

    @Test
    public void shouldReorderMultipleUngroupedNotAddColumnToGroupRightOnEdge() {
        // remove group 1
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder first and third column to second group
        // nothing should happen as multi column reorder is not possible via UI
        // drag and drop
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(2, 4), 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Lastname", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3));

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
    }

    @Test
    public void shouldReorderMultipleUngroupedAddColumnToGroupLeftOnEdge() {
        // remove group 2
        this.columnGroupHeaderLayer.removeGroup(4);

        // reorder fourth and sixth column to first group
        // nothing should happen as multi column reorder is not possible via UI
        // drag and drop
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(5, 7), 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Postalcode", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Housenumber", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderToUnbreakable() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // try to reorder a column from first group to second
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 5));

        // nothing should have been changed
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderFromUnbreakable() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // try to reorder a column from second group to first
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 2));

        // nothing should have been changed
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderToUnbreakable() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // try to reorder a column from first group to second
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 2));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 6));

        // nothing should have been changed
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderFromUnbreakable() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // try to reorder a column from second group to first
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 6));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 2));

        // nothing should have been changed
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderMultipleToUnbreakable() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // try to reorder a column from first group to second
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(2, 3), 6));

        // nothing should have been changed
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderMultipleFromUnbreakable() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // try to reorder a column from second group to first
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, Arrays.asList(6, 7), 2));

        // nothing should have been changed
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderToUnbreakableEdgeRight() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // remove first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // try to reorder column 4 to second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 5));

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3));

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderToUnbreakableEdgeLeft() {
        // set first group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        // remove second group
        this.columnGroupHeaderLayer.removeGroup(4);

        // try to reorder column 4 to first group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7));

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderBetweenGroupsRight() {
        // set second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // remove first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // try to reorder column 4 to second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 9));

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2));

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7));

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderUnbreakableGroupsBetweenGroupsLeft() {
        // set all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // try to reorder group 3 between group 1 and 2
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 9, 5));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        assertEquals("Address", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldReorderUnbreakableGroupsToStartLeft() {
        // set all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // try to reorder group 3 to start
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 9, 1));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(8, group1.getStartIndex());
        assertEquals(8, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        assertEquals("Facts", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        assertEquals("Address", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldReorderUnbreakableGroupsToEndRight() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // set all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // try to reorder group 2 to end
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 15));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(4, group4.getStartIndex());
        assertEquals(4, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(4, group4.getOriginalSpan());
        assertEquals(4, group4.getVisibleSpan());
        assertEquals("Address", group4.getName());
    }

    @Test
    public void shouldReorderUnbreakableGroupsToRight() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        // set all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // try to reorder group 1 between 2 and 3
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 1, 9));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldDragReorderEntireColumnGroupToStart() {
        // reorder second group to first
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 6));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderEntireColumnGroupToStart() {
        // reorder second group to first
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderEntireColumnGroupToLast() {
        // start reorder second group
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 6));

        // scroll to show last column
        this.gridLayer.doCommand(new ShowColumnInViewportCommand(13));

        // end reorder to last position
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 11));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(-30, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(11, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(270, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(570, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // the position is related to the positionLayer, which is the
        // SelectionLayer
        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(10);
        assertEquals(4, group4.getStartIndex());
        assertEquals(4, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(4, group4.getOriginalSpan());
        assertEquals(4, group4.getVisibleSpan());
        assertEquals("Address", group4.getName());
    }

    @Test
    public void shouldReorderEntireColumnGroupToLast() {
        // reorder second group to first
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderEntireColumnGroupBetweenOtherGroups() {
        // reorder third group between first and second first
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 9, 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
    }

    @Test
    public void shouldNotUngroupOnReorderEntireGroupToGroupStart() {
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 5));
        verifyCleanState();
    }

    @Test
    public void shouldNotUngroupOnReorderEntireGroupToGroupEnd() {
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 6, 9));
        verifyCleanState();
    }

    @Test
    public void shouldReorderGroupToNonVisibleArea() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder first group to be the second group
        this.columnGroupHeaderLayer.reorderColumnGroup(0, 0, 8);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldReorderGroupToNonVisibleAreaEnd() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder first group to be the last group
        this.columnGroupHeaderLayer.reorderColumnGroup(0, 0, 14);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(0, group4.getStartIndex());
        assertEquals(0, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(4, group4.getOriginalSpan());
        assertEquals(4, group4.getVisibleSpan());
        assertEquals("Person", group4.getName());
    }

    @Test
    public void shouldReorderGroupFromNonVisibleArea() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder last group to be the first group
        this.columnGroupHeaderLayer.reorderColumnGroup(0, 11, 0);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(11, group1.getStartIndex());
        assertEquals(11, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        assertEquals("Personal", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        assertEquals("Address", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(8, group4.getStartIndex());
        assertEquals(8, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Facts", group4.getName());
    }

    @Test
    public void shouldReorderGroupOutsideVisibleArea() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder last group to be the third group
        this.columnGroupHeaderLayer.reorderColumnGroup(0, 11, 8);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Address", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(8, group4.getStartIndex());
        assertEquals(8, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Facts", group4.getName());
    }

    @Test
    public void shouldReorderGroupToNonVisibleAreaWithCommand() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder first group to be the second group
        this.gridLayer.doCommand(
                new ColumnGroupReorderCommand(this.columnGroupHeaderLayer.getPositionLayer(), 0, 0, 8, false));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldReorderGroupToNonVisibleAreaEndWithCommand() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder first group to be the last group
        this.gridLayer.doCommand(
                new ColumnGroupReorderCommand(this.columnGroupHeaderLayer.getPositionLayer(), 0, 0, 14, false));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(0, group4.getStartIndex());
        assertEquals(0, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(4, group4.getOriginalSpan());
        assertEquals(4, group4.getVisibleSpan());
        assertEquals("Person", group4.getName());
    }

    @Test
    public void shouldReorderGroupFromNonVisibleAreaWithCommand() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder last group to be the first group
        this.gridLayer.doCommand(
                new ColumnGroupReorderCommand(this.columnGroupHeaderLayer.getPositionLayer(), 0, 11, 0, false));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(11, group1.getStartIndex());
        assertEquals(11, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        assertEquals("Personal", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(4, group3.getStartIndex());
        assertEquals(4, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        assertEquals("Address", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(8, group4.getStartIndex());
        assertEquals(8, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Facts", group4.getName());
    }

    @Test
    public void shouldReorderGroupOutsideVisibleAreaWithCommand() {
        // reduce the client area to only show the first and half of the second
        // group
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 640, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(7, this.gridLayer.getColumnCount());

        // reorder last group to be the third group
        this.gridLayer.doCommand(
                new ColumnGroupReorderCommand(this.columnGroupHeaderLayer.getPositionLayer(), 0, 11, 8, false));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Address", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(8, group4.getStartIndex());
        assertEquals(8, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Facts", group4.getName());
    }

    // reordering with expand/collapse

    @Test
    public void shouldReorderLeftAddUngroupedToCollapsedGroupLeft() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        // reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 2));

        // added column is not shown as part of collapsed group
        // group is collapsed and therefore the added column is hidden
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible span should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(0);

        verifyCleanState();
    }

    @Test
    public void shouldReorderLeftAddUngroupedToCollapsedGroupLeftJump() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        // reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 1));

        // added column is not shown as part of collapsed group
        // group is collapsed and therefore the added column is hidden
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // the visible span and the start index should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(0);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderLeftAddUngroupedToCollapsedGroupRight() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());

        // reorder to right to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 5));

        // added column is shown as part of collapsed group
        // group is collapsed and therefore the added column is shown as first
        // column
        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the visible span should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group1.getStartIndex());
        assertEquals(8, group1.getVisibleStartIndex());
        assertEquals(4, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(3);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(3, group2.getStartIndex());
        assertEquals(3, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderRightAddUngroupedToCollapsedGroupLeft() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 4);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4));

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(5);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());

        // reorder to right to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 6));

        // added column is shown as visible column in collapsed group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the original span should have changed
        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        verifyCleanState();
    }

    @Test
    public void shouldReorderRightAddUngroupedToCollapsedGroupRight() {
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 4);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(5);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        // reorder to right to add to next group at the end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 7));

        // added column is not shown as visible column in collapsed group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the original span should have changed
        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderInsideCollapsedGroupWithStatics() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        group1.addStaticIndexes(1, 2);

        // collapse
        this.columnGroupHeaderLayer.collapseGroup(this.groupModel, group1);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(2, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // expand
        this.columnGroupHeaderLayer.expandGroup(0);

        verifyCleanState();
    }

    @Test
    public void shouldReorderInsideCollapsedGroupWithStaticsFromBeginning() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        group1.addStaticIndexes(0, 1);

        // collapse
        this.columnGroupHeaderLayer.collapseGroup(this.groupModel, group1);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder again first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // expand
        this.columnGroupHeaderLayer.expandGroup(0);

        verifyCleanState();
    }

    @Test
    public void shouldDragReorderInsideCollapsedGroupWithStaticsFromBeginning() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        group1.addStaticIndexes(0, 1);

        // collapse
        this.columnGroupHeaderLayer.collapseGroup(this.groupModel, group1);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder again first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // expand
        this.columnGroupHeaderLayer.expandGroup(0);

        verifyCleanState();
    }

    @Test
    public void shouldReorderLeftAddColumnToCollapsedGroupWithStatics() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        group1.addStaticIndexes(1, 2);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        // reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 3));

        // added column is not shown as part of collapsed group
        // group is collapsed and therefore the added column is hidden
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the original span should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(2, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(0);

        verifyCleanState();
    }

    @Test
    public void shouldDragReorderLeftAddColumnToCollapsedGroupWithStatics() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        group1.addStaticIndexes(1, 2);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        // reorder in same column to add to next group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 3));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 3));

        // added column is not shown as part of collapsed group
        // group is collapsed and therefore the added column is hidden
        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the original span should have changed
        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(2, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(0);

        verifyCleanState();
    }

    @Test
    public void shouldReorderRightAddColumnToCollapsedGroupWithStatics() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        group.addStaticIndexes(5, 6);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 4);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4));

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(5);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder to right to add to next group at the beginning
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 6));

        // added column is not shown as visible column in collapsed group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the original span should have changed
        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(6, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        verifyCleanState();
    }

    @Test
    public void shouldReorderLeftAddColumnToCollapsedGroupWithStaticsInGroupLeftEdge() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        group.addStaticIndexes(5, 6);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 7);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("City", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7));

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("City", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        assertEquals(4, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder to left to add to previous group at the beginning
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 7, 5));

        // added column is not shown as visible column in collapsed group
        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // only the original span should have changed
        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(6, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        verifyCleanState();
    }

    @Test
    public void shouldReorderRightAddColumnToCollapsedGroupWithStaticsInGroupRightEdge() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        group.addStaticIndexes(5, 6);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(5);

        // reorder to right to add to next group at the end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 7));

        // added column is not shown as visible column in collapsed group
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        // Married column to the right of the visible column at the end while
        // collapsed
        assertEquals(3, this.columnGroupHeaderLayer.getColumnIndexByPosition(6));
        assertEquals(7, this.columnGroupHeaderLayer.getColumnIndexByPosition(7));

        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderRightAddColumnToCollapsedGroupWithStaticsInGroupRightEdge() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        group.addStaticIndexes(5, 6);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(5);

        // reorder to right to add to next group at the end
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 7));

        // added column is not shown as visible column in collapsed group
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        // Married column to the right of the visible column at the end while
        // collapsed
        assertEquals(3, this.columnGroupHeaderLayer.getColumnIndexByPosition(6));
        assertEquals(7, this.columnGroupHeaderLayer.getColumnIndexByPosition(7));

        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderRightAddColumnToCollapsedGroupWithStaticsInMiddleOfGroup() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(5);
        group.addStaticIndexes(5, 6);

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(5);

        // reorder to right to add to next group in the middle
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 6));

        // added column is not shown as visible column in collapsed group
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(4, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(6);
        assertEquals(8, group.getStartIndex());
        assertEquals(8, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        // Married column in middle of Address group
        assertEquals(3, this.columnGroupHeaderLayer.getColumnIndexByPosition(5));

        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderLeftRemoveColumnFromCollapsedGroup() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
    }

    @Test
    public void shouldReorderFromCollapsedGroupLeftToRemoveAndRightToAddColumnAgain() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        // collapse second group again
        this.columnGroupHeaderLayer.collapseGroup(5);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 6));

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
        assertTrue(group.isCollapsed());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        verifyCleanState();
    }

    @Test
    public void shouldDragReorderFromCollapsedGroupLeftToRemoveAndRightToAddColumnAgain() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 5));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        // collapse second group agani
        this.columnGroupHeaderLayer.collapseGroup(5);

        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 6));

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
        assertTrue(group.isCollapsed());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        verifyCleanState();
    }

    @Test
    public void shouldReorderRightRemoveColumnFromCollapsedGroup() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 6));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 1);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
        assertTrue(group.hasMember(4));
    }

    @Test
    public void shouldReorderFromCollapsedGroupRightToRemoveAndLeftToAddColumnAgain() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 6));

        // first reorder will expand and reorder first column to last in group
        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(7));

        // reorder last to ungroup
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 9));

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        // collapse
        this.columnGroupHeaderLayer.collapseGroup(4);

        // reorder left to add to collapsed group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 6, 6));

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
        assertTrue(group.isCollapsed());

        // expand again
        this.columnGroupHeaderLayer.expandGroup(4);

        // verifyCleanState modified throuhg reorder
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(4, this.columnGroupHeaderLayer.getColumnIndexByPosition(7));

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
    }

    @Test
    public void shouldReorderLeftRemoveFromCollapsedGroupAddToOtherGroup() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(6));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        // verifyCleanState modified through reorder
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());

        assertEquals(4, this.columnGroupHeaderLayer.getColumnIndexByPosition(2));

        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(5, group1.getMembers().length);
        assertTrue(group1.hasMember(0));
        assertTrue(group1.hasMember(1));
        assertTrue(group1.hasMember(2));
        assertTrue(group1.hasMember(3));
        assertTrue(group1.hasMember(4));

        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldDragReorderLeftRemoveFromCollapsedGroupAddToOtherGroup() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(5, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(6));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        // verifyCleanState modified through reorder
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(500, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());

        assertEquals(4, this.columnGroupHeaderLayer.getColumnIndexByPosition(2));

        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(5, group1.getMembers().length);
        assertTrue(group1.hasMember(0));
        assertTrue(group1.hasMember(1));
        assertTrue(group1.hasMember(2));
        assertTrue(group1.hasMember(3));
        assertTrue(group1.hasMember(4));

        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldReorderRightRemoveFromCollapsedGroupAddToOtherGroup() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // the out of the group triggers expand
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        assertFalse(group3.isCollapsed());

        // verifyCleanState modified through reorder
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(4, this.columnGroupHeaderLayer.getColumnIndexByPosition(8));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(4, group3.getMembers().length);
        assertTrue(group3.hasMember(8));
        assertTrue(group3.hasMember(9));
        assertTrue(group3.hasMember(10));
        assertTrue(group3.hasMember(4));

        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldDragReorderRightRemoveFromCollapsedGroupAddToOtherGroup() {
        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(8, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());
        assertFalse(group3.isCollapsed());

        // verifyCleanState modified through reorder
        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(4, this.columnGroupHeaderLayer.getColumnIndexByPosition(8));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(4, group3.getOriginalSpan());
        assertEquals(4, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(4, group3.getMembers().length);
        assertTrue(group3.hasMember(8));
        assertTrue(group3.hasMember(9));
        assertTrue(group3.hasMember(10));
        assertTrue(group3.hasMember(4));

        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldReorderLeftColumnFromCollapsedGroupWithStatics() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        group.addStaticIndexes(5, 6);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // since the visible static column is not the first column in the group,
        // the reorder will move the column to the first position in the group
        // and expand it, but it will still be part of the group and not be
        // ungrouped
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // the start index has changed
        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(4, group2.getMembers().length);
        assertTrue(group2.hasMember(4));
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldDragReorderLeftColumnFromCollapsedGroupWithStatics() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        group.addStaticIndexes(5, 6);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // since the visible static column is not the first column in the group,
        // the reorder will move the column to the first position in the group
        // and expand it, but it will still be part of the group and not be
        // ungrouped
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 5));

        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // the start index has changed
        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(4, group2.getMembers().length);
        assertTrue(group2.hasMember(4));
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldReorderRightRemoveColumnFromCollapsedGroupWithStatics() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        group.addStaticIndexes(5, 6);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // reorder the most right static column in a collapsed group will
        // expand and reorder if that column is not at the group end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 6, 7));

        // should expand
        assertEquals(14, this.selectionLayer.getColumnCount());

        // only a reorder happened
        assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 1);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Postalcode", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // reorder the most right static column in a collapsed group will
        // ungroup if the column is the right-most column in the expanded group,
        // works because of reordering to right reorders correctly in the lower
        // layers
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 6, 7));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Postalcode", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // the span has changed
        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7));

        group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(4));
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(7));

        assertEquals(1, group2.getStaticIndexes().length);
        assertTrue(group2.containsStaticIndex(5));
    }

    @Test
    public void shouldDragReorderRightRemoveColumnFromCollapsedGroupWithStatics() {
        Group group = this.columnGroupHeaderLayer.getGroupByPosition(4);
        group.addStaticIndexes(5, 6);

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // reorder the most right static column in a collapsed group will
        // expand and reorder if that column is not at the group end
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 6));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 7));

        // should expand
        assertEquals(14, this.selectionLayer.getColumnCount());

        // only a reorder happened
        assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(7));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 1);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Postalcode", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(20, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        // collapse second group
        this.columnGroupHeaderLayer.collapseGroup(4);

        // reorder the most right static column in a collapsed group will
        // ungroup if the column is the right-most column in the expanded group,
        // works because of reordering to right reorders correctly in the lower
        // layers
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 6));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 7));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(7, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Postalcode", cell.getDataValue());
        assertEquals(700, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // this cell is not visible because of the client area
        cell = this.columnGroupHeaderLayer.getCellByPosition(11, 0);
        assertEquals(11, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(0, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        // the span has changed
        group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7));

        group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(4));
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(7));

        assertEquals(1, group2.getStaticIndexes().length);
        assertTrue(group2.containsStaticIndex(5));
    }

    @Test
    public void shouldNotRemoveGroupOnReorderLastColumnInGroup() {
        // if a group has only one column left, a reorder operation on the same
        // column should not remove the group to avoid that by accident a group
        // is destroyed

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        assertEquals(4, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(1, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
    }

    @Test
    public void shouldNotRemoveGroupOnDragReorderLastColumnInGroup() {
        // if a group has only one column left, a reorder operation on the same
        // column should not remove the group to avoid that by accident a group
        // is destroyed

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);

        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 5));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 5));

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        assertEquals(4, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(1, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
    }

    @Test
    public void shouldNotRemoveGroupOnReorderLastColumnInGroupToNonGrouped() {
        // if a group has only one column left, a reorder operation to a
        // position between some non grouped columns should not remove the group
        // to avoid that by accident a group is destroyed, as it could also mean
        // the group itself was reordered

        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 10));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));
        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(8));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(9));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(10));

        assertEquals(3, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(8, group2.getVisibleStartPosition());
        assertEquals(1, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
    }

    @Test
    public void shouldRemoveGroupOnReorderLastColumnInGroupToOtherGroup() {
        // if a group has only one column left, a reorder operation to another
        // group should remove the group as the reorder operation is clearly to
        // another position

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 2));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        assertEquals(3, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());
    }

    @Test
    public void shouldNotRemoveGroupOnReorderLastColumnInGroupToUnbreakableGroup() {
        // reorder to an unbreakable group should never work

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 2));

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        assertEquals(4, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(1, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
    }

    @Test
    public void shouldNotRemoveGroupOnReorderRemoveLastFromCollapsed() {
        // if a group has only one column left, a reorder operation on the same
        // column should not remove the group to avoid that by accident a group
        // is destroyed

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 5));

        assertNotNull(this.columnGroupHeaderLayer.getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        assertEquals(4, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(1, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
    }

    @Test
    public void shouldRemoveGroupOnReorderLastFromCollapsedGroupToOtherGroup() {
        // if a group has only one column left, a reorder operation to another
        // group should remove the group as the reorder operation is clearly to
        // another position

        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 5, 6, 7);
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 2));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));

        assertEquals(3, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(5, group1.getOriginalSpan());
        assertEquals(5, group1.getVisibleSpan());
    }

    @Test
    public void shouldNotExpandCollapsedGroupOnReorderLeft() {
        this.columnGroupHeaderLayer.collapseGroup(4);

        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(1, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldNotExpandCollapsedGroupOnDragReorderToStart() {
        this.columnGroupHeaderLayer.collapseGroup(4);

        // reorder second group to first
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 5));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 1));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(1, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldNotExpandCollapsedGroupOnReorderToRight() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.collapseGroup(0);

        // try to reorder group 1 between 2 and 3
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 1, 6));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(5, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(8, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertEquals("Personal", group4.getName());
    }

    @Test
    public void shouldNotExpandCollapsedGroupOnDragReorderToEnd() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.collapseGroup(0);

        // try to reorder group 1 to end
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 1));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 12));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(10);
        assertEquals(0, group4.getStartIndex());
        assertEquals(0, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(4, group4.getOriginalSpan());
        assertEquals(1, group4.getVisibleSpan());
        assertEquals("Person", group4.getName());
    }

    @Test
    public void shouldNotExpandCollapsedGroupOnReorderToEnd() {
        // increase the client area to show all columns
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.columnGroupHeaderLayer.collapseGroup(0);

        // try to reorder group 1 to end
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 1, 12));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(8, group2.getStartIndex());
        assertEquals(8, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertEquals("Facts", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(11, group3.getStartIndex());
        assertEquals(11, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
        assertEquals("Personal", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(10);
        assertEquals(0, group4.getStartIndex());
        assertEquals(0, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(4, group4.getOriginalSpan());
        assertEquals(1, group4.getVisibleSpan());
        assertEquals("Person", group4.getName());
    }

    @Test
    public void shouldAvoidReorderGroupInOtherGroup() {
        // try to reorder group 2 into group 1
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 3));

        // nothing should have happened
        verifyCleanState();
    }

    @Test
    public void shouldAvoidReorderCollapsedGroupInOtherGroup() {
        this.columnGroupHeaderLayer.collapseGroup(4);

        // try to reorder group 2 into group 1
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 3));

        // nothing should have happened
        assertEquals(11, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
        assertTrue(group2.isCollapsed());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(5);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(5, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(8, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldReorderToLastWithHidden() {
        // remove last item from second group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 7);

        // hide new last column in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 7));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        // reorder removed column to second group again
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 7, 7));

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        verifyCleanState();
    }

    @Test
    public void shouldReorderToFirstWithHidden() {
        // remove first item from second group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 4);

        // hide new first column in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 6));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(6, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        // reorder removed column to second group again
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 5, 6));

        assertEquals(5, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(14, this.selectionLayer.getColumnCount());

        for (int column = 0; column < this.columnGroupHeaderLayer.getColumnCount(); column++) {
            assertTrue(this.columnGroupHeaderLayer.isPartOfAGroup(column));
            assertFalse(this.columnGroupHeaderLayer.isPartOfAnUnbreakableGroup(column));
        }

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(8, 0);
        assertEquals(8, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(800, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(4, group2.getMembers().length);
        assertTrue(group2.hasMember(4));
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));
    }

    @Test
    public void shouldReorderInsideGroupWithHiddenColumnsAtEnd() {

        // hide last two columns at group end
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 3, 4));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder again first visible to last visible inside a group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 3));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());

        group2 = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        verifyCleanState();
    }

    @Test
    public void shouldCreateColumnGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.clear();

        assertTrue(groupModel.isEmpty());

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, true, false));

        assertEquals(4, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Person"));

        assertEquals(1, groupModel.size());

        Group group = groupModel.getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldCreateTwoColumnGroupsWithSameName() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.clear();

        assertTrue(groupModel.isEmpty());

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, true, false));

        assertEquals(4, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Person"));

        // create a second group with the same name
        // this fails with the old column grouping
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 7, 0, true, false));

        assertEquals(4, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Person"));

        assertEquals(2, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        Group group2 = groupModel.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());
    }

    @Test
    public void shouldCreateGroupFromUncontiguous() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.clear();

        assertTrue(groupModel.isEmpty());

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 6, 0, false, true));

        assertEquals(4, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Person"));

        assertEquals(1, groupModel.size());

        Group group = groupModel.getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(2, members[1]);
        assertEquals(4, members[2]);
        assertEquals(6, members[3]);

        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.columnGroupHeaderLayer.getColumnIndexByPosition(1));
        assertEquals(4, this.columnGroupHeaderLayer.getColumnIndexByPosition(2));
        assertEquals(6, this.columnGroupHeaderLayer.getColumnIndexByPosition(3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldCreateGroupFromSingleColumn() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.clear();

        assertTrue(groupModel.isEmpty());

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));

        assertEquals(4, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Person"));

        assertEquals(1, groupModel.size());

        Group group = groupModel.getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        int[] members = group.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        // select a single column next to the previous
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, false));

        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Test"));

        assertEquals(2, groupModel.size());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        group = groupModel.getGroupByPosition(4);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(1, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldUngroupLastItemInGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, false));

        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        Group group = groupModel.getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        int[] members = group.getMembers();
        assertEquals(3, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);

        assertNull(groupModel.getGroupByPosition(3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldUngroupFirstItemInGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, false));

        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        Group group2 = groupModel.getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        members = group2.getMembers();
        assertEquals(3, members.length);
        assertEquals(5, members[0]);
        assertEquals(6, members[1]);
        assertEquals(7, members[2]);

        assertNull(groupModel.getGroupByPosition(4));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldUngroupMiddleItemInGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, false));

        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        Group group = groupModel.getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertFalse(group.isCollapsed());

        int[] members = group.getMembers();
        assertEquals(3, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(3, members[2]);

        assertNull(groupModel.getGroupByPosition(3));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(3, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);
    }

    @Test
    public void shouldUngroupMultipleFirstItemsInGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 5, 0, false, true));

        assertEquals(2, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        int[] members = group1.getMembers();
        assertEquals(4, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);
        assertEquals(3, members[3]);

        assertNull(groupModel.getGroupByPosition(4));
        assertNull(groupModel.getGroupByPosition(5));

        Group group2 = groupModel.getGroupByPosition(6);
        assertEquals(6, group2.getStartIndex());
        assertEquals(6, group2.getVisibleStartIndex());
        assertEquals(6, group2.getVisibleStartPosition());
        assertEquals(2, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        members = group2.getMembers();
        assertEquals(2, members.length);
        assertEquals(6, members[0]);
        assertEquals(7, members[1]);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Street", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(5, 0);
        assertEquals(5, cell.getOriginColumnPosition());
        assertEquals(5, cell.getColumnPosition());
        assertEquals(5, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Housenumber", cell.getDataValue());
        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(6, 0);
        assertEquals(6, cell.getOriginColumnPosition());
        assertEquals(6, cell.getColumnPosition());
        assertEquals(6, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(600, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldUngroupMultipleLastItemsInGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));

        assertEquals(2, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(2, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        int[] members = group1.getMembers();
        assertEquals(2, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);

        assertNull(groupModel.getGroupByPosition(2));
        assertNull(groupModel.getGroupByPosition(3));

        Group group2 = groupModel.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        members = group2.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldUngroupMultipleItemsInMiddleOfGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, true));

        assertEquals(2, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(2, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        int[] members = group1.getMembers();
        assertEquals(2, members.length);
        assertEquals(0, members[0]);
        assertEquals(3, members[1]);

        assertNull(groupModel.getGroupByPosition(2));
        assertNull(groupModel.getGroupByPosition(3));

        Group group2 = groupModel.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        members = group2.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(2, 0);
        assertEquals(2, cell.getOriginColumnPosition());
        assertEquals(2, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Lastname", cell.getDataValue());
        assertEquals(200, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldUngroupMultipleItemsFirstLastOfGroup() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 0, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, true));

        assertEquals(2, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        assertEquals(4, groupModel.size());

        assertNull(groupModel.getGroupByPosition(0));

        Group group1 = groupModel.getGroupByPosition(1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(2, group1.getOriginalSpan());
        assertEquals(2, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());

        int[] members = group1.getMembers();
        assertEquals(2, members.length);
        assertEquals(1, members[0]);
        assertEquals(2, members[1]);

        assertNull(groupModel.getGroupByPosition(3));

        Group group2 = groupModel.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());

        members = group2.getMembers();
        assertEquals(4, members.length);
        assertEquals(4, members[0]);
        assertEquals(5, members[1]);
        assertEquals(6, members[2]);
        assertEquals(7, members[3]);

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(1, cell.getColumnIndex());
        assertEquals(2, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(3, 0);
        assertEquals(3, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(300, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(4, 0);
        assertEquals(4, cell.getOriginColumnPosition());
        assertEquals(4, cell.getColumnPosition());
        assertEquals(4, cell.getColumnIndex());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(400, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(400, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldUpdateGroupOnCreate() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.removeGroup(4);
        groupModel.removeGroup(8);
        groupModel.removeGroup(11);

        assertEquals(1, groupModel.size());

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertEquals(2, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Person"));

        assertEquals(1, groupModel.size());

        Group group = groupModel.getGroupByPosition(0);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldNotModifyUnbreakableGroupOnCreate() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.removeGroup(4);
        groupModel.removeGroup(8);
        groupModel.removeGroup(11);

        assertEquals(1, groupModel.size());

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 4, 0, false, true));

        assertEquals(2, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Test"));

        assertEquals(2, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());
        assertFalse(group1.isCollapsed());

        Group group2 = groupModel.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(1, group2.getOriginalSpan());
        assertEquals(1, group2.getVisibleSpan());
        assertEquals("Test", group2.getName());
        assertFalse(group2.isCollapsed());
    }

    @Test
    public void shouldNotModifyUnbreakableGroupOnCreateAndCreateNewNonContiguous() {
        GroupModel groupModel = this.columnGroupHeaderLayer.getGroupModel();
        groupModel.removeGroup(4);
        groupModel.removeGroup(8);
        groupModel.removeGroup(11);

        assertEquals(1, groupModel.size());

        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 2, 0, false, false));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 5, 0, false, true));
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 7, 0, false, true));

        assertEquals(3, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new CreateColumnGroupCommand("Test"));

        assertEquals(2, groupModel.size());

        Group group1 = groupModel.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertEquals("Person", group1.getName());
        assertFalse(group1.isCollapsed());

        assertNull(groupModel.getGroupByPosition(4));

        Group group2 = groupModel.getGroupByPosition(5);
        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(2, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());
        assertEquals("Test", group2.getName());
        assertFalse(group2.isCollapsed());

        assertNull(groupModel.getGroupByPosition(8));

        assertEquals(7, this.columnGroupHeaderLayer.getColumnIndexByPosition(6));
    }

    @Test
    public void shouldNotUngroupFromUnbreakableGroup() {
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 3, 0, false, false));

        assertEquals(1, this.selectionLayer.getSelectedColumnPositions().length);

        this.gridLayer.doCommand(new UngroupColumnCommand());

        // revert unbreakable change so verifyCleanState is correct
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, false);
        verifyCleanState();
    }

    @Test
    public void shouldRemoveColumnGroup() {
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(4));

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(4));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(5));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));
        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));
        assertNull(this.columnGroupHeaderLayer.getGroupByName("Address"));

        assertEquals(3, this.columnGroupHeaderLayer.getGroupModel().size());
    }

    @Test
    public void shouldRemoveAllFromColumnGroup() {
        Group group = this.groupModel.getGroupByPosition(0);
        group.removeMembers(0, 1, 2, 3);

        assertEquals(0, group.getMembers().length);
        assertFalse(group.hasMember(0));
        assertFalse(group.hasMember(1));
        assertFalse(group.hasMember(2));
        assertFalse(group.hasMember(3));

        assertEquals(-1, group.getGroupEndPosition(this.selectionLayer));
    }

    @Test
    public void shouldNotRemoveUnbreakableColumnGroup() {
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        this.gridLayer.doCommand(new RemoveColumnGroupCommand(4));

        // revert unbreakable change so verifyCleanState is correct
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, false);
        verifyCleanState();
    }

    // TODO shouldRenameColumnGroup
    // currently not testable because the DisplayColumnGroupRenameDialogCommand
    // opens the dialog and directly sets the column group name afterwards.
    // Needs to be fixed with NatTable 2.0 to only set the name and not opening
    // a dialog.

    @Test
    public void shouldReturnConfigLabels() {
        // check expanded column group
        LabelStack stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.columnGroupHeaderLayer.collapseGroup(0);
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));

        // expand again as positions are visible and otherwise we cannot remove
        // a column from the group
        this.columnGroupHeaderLayer.expandGroup(0);

        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // check ungrouped
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(0, stack.size());
    }

    @Test
    public void shouldReturnConfigLabelsWithAccumulator() {
        // set config label accumulator
        this.columnGroupHeaderLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (columnPosition == 0 || columnPosition == 3) {
                    configLabels.addLabel("custom");
                }
            }
        });

        // check expanded column group
        LabelStack stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(3, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel("custom"));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.columnGroupHeaderLayer.collapseGroup(0);
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(3, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel("custom"));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_COLLAPSED_CONFIG_TYPE));

        // expand again as positions are visible and otherwise we cannot remove
        // a column from the group
        this.columnGroupHeaderLayer.expandGroup(0);

        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // check ungrouped
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(0, stack.size());
    }

    @Test
    public void shouldReturnConfigLabelsFromColumnHeader() {
        // set config label accumulator
        this.columnHeaderLayer.setConfigLabelAccumulator((configLabels, columnPosition, rowPosition) -> {
            if (rowPosition == 0) {
                configLabels.addLabel("columnHeaderRow");
            }
            if (columnPosition == 0 || columnPosition == 3) {
                configLabels.addLabel("custom");
            }
        });

        // check column group
        LabelStack stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(GroupHeaderConfigLabels.GROUP_EXPANDED_CONFIG_TYPE));

        // check column header row
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(0, 1);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel("columnHeaderRow"));
        assertTrue(stack.hasLabel("custom"));

        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // check ungrouped
        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel("columnHeaderRow"));
        assertTrue(stack.hasLabel("custom"));

        stack = this.columnGroupHeaderLayer.getConfigLabelsByPosition(3, 1);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel("columnHeaderRow"));
        assertTrue(stack.hasLabel("custom"));
    }

    @Test
    public void shouldReturnDisplayModeFromColumnHeader() {
        // remove last column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // the column group header only supports DisplayMode#NORMAL
        assertEquals(DisplayMode.NORMAL, this.columnGroupHeaderLayer.getDisplayModeByPosition(0, 0));

        // select a cell in the body
        this.selectionLayer.selectCell(0, 0, false, false);

        assertEquals(DisplayMode.NORMAL, this.columnGroupHeaderLayer.getDisplayModeByPosition(0, 0));
        assertEquals(DisplayMode.SELECT, this.columnGroupHeaderLayer.getDisplayModeByPosition(0, 1));

        // select a cell in the column that was removed from the group
        this.selectionLayer.selectCell(3, 0, false, false);

        assertEquals(DisplayMode.SELECT, this.columnGroupHeaderLayer.getDisplayModeByPosition(3, 0));
        assertEquals(DisplayMode.SELECT, this.columnGroupHeaderLayer.getDisplayModeByPosition(3, 1));

        // set a column header cell hovered
        this.columnHeaderHoverLayer.setCurrentHoveredCellByIndex(0, 0);
        assertEquals(DisplayMode.NORMAL, this.columnGroupHeaderLayer.getDisplayModeByPosition(0, 0));
        assertEquals(DisplayMode.HOVER, this.columnGroupHeaderLayer.getDisplayModeByPosition(0, 1));

        // set a column header cell hovered in the column that was removed from
        // the group
        this.columnHeaderHoverLayer.setCurrentHoveredCellByIndex(3, 0);
        assertEquals(DisplayMode.SELECT_HOVER, this.columnGroupHeaderLayer.getDisplayModeByPosition(3, 0));
        assertEquals(DisplayMode.SELECT_HOVER, this.columnGroupHeaderLayer.getDisplayModeByPosition(3, 1));
    }

    @Test
    public void shouldConvertPositionsInEvent() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.gridLayer.addLayerListener(listener);

        // fire a CellVisualChangeEvent
        this.columnHeaderLayer.fireLayerEvent(new CellVisualUpdateEvent(this.columnHeaderLayer, 2, 0));

        assertTrue(listener.containsInstanceOf(CellVisualUpdateEvent.class));
        CellVisualUpdateEvent event = (CellVisualUpdateEvent) listener.getReceivedEvent(CellVisualUpdateEvent.class);

        // column position changed from 2 to 3 because of the row header layer
        assertEquals(3, event.getColumnPosition());
        // row position changed from 0 to 1 because of the column group header
        // layer
        assertEquals(1, event.getRowPosition());
    }

    @Test
    public void shouldCalculateRowHeightByPosition() {
        this.columnGroupHeaderLayer.clearAllGroups();
        this.columnGroupHeaderLayer.setRowHeight(100);
        // Height of the header column row - see fixture
        assertEquals(120, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());
        assertEquals(100, this.columnGroupHeaderLayer.getRowHeightByPosition(0));
        assertEquals(20, this.columnGroupHeaderLayer.getRowHeightByPosition(1));
        // Test calculated height
        this.columnGroupHeaderLayer.setCalculateHeight(true);
        assertEquals(20, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());
        assertEquals(0, this.columnGroupHeaderLayer.getRowHeightByPosition(0));
        assertEquals(20, this.columnGroupHeaderLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldCalculateRowHeightOnGroupModelChanges() {
        this.columnGroupHeaderLayer.setCalculateHeight(true);

        assertEquals(40, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(1, cell.getRowSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 1);
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowSpan());

        this.columnGroupHeaderLayer.clearAllGroups();

        assertEquals(20, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());
        assertEquals(0, this.columnGroupHeaderLayer.getRowHeightByPosition(0));
        assertEquals(20, this.columnGroupHeaderLayer.getRowHeightByPosition(1));

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(2, cell.getRowSpan());

        this.columnGroupHeaderLayer.setCalculateHeight(false);

        assertEquals(40, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(2, cell.getRowSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 1);
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(2, cell.getRowSpan());

        // add group again
        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);

        assertEquals(40, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(4, cell.getColumnSpan());
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(0, cell.getRowPosition());
        assertEquals(1, cell.getRowSpan());

        cell = this.columnGroupHeaderLayer.getCellByPosition(0, 1);
        assertEquals("Firstname", cell.getDataValue());
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(1, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowPosition());
        assertEquals(1, cell.getRowSpan());
    }

    @Test
    public void shouldSetGroupHeaderRowHeight() {
        this.columnGroupHeaderLayer.setRowHeight(100);
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldResizeColumnGroupHeaderRow() {
        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 0, 100));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldResizeColumnHeaderRow() {
        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 1, 100));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldResizeColumnGroupHeaderRowWithoutDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 0, 100));
        assertEquals(125, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldResizeColumnGroupHeaderRowWithDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 0, 100, true));

        // down scaling in the command was enabled, therefore the value set is
        // the value that will be returned
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderRow() {
        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0 }, 100));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderAndColumnHeader() {
        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderRowWithoutDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0 }, 100));
        assertEquals(125, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldMultiResizeColumnGroupHeaderRowWithDownScale() {
        this.gridLayer.doCommand(new ConfigureScalingCommand(new FixedScalingDpiConverter(120)));

        // scaling enabled, therefore default height of 20 pixels is up scaled
        // to 25
        assertEquals(25, this.gridLayer.getRowHeightByPosition(0));

        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0 }, 100, true));

        // down scaling in the command was enabled, therefore the value set is
        // the value that will be returned
        assertEquals(100, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldNotResizeNotResizableColumnGroupHeaderRow() {
        this.columnGroupHeaderLayer.setRowPositionResizable(0, false);
        this.gridLayer.doCommand(new RowResizeCommand(this.gridLayer, 0, 100));
        assertEquals(20, this.gridLayer.getRowHeightByPosition(0));
    }

    @Test
    public void shouldNotResizeNotResizableColumnGroupHeaderRowMulti() {
        this.columnGroupHeaderLayer.setRowPositionResizable(0, false);
        this.gridLayer.doCommand(new MultiRowResizeCommand(this.gridLayer, new int[] { 0, 1 }, 100));
        assertEquals(20, this.gridLayer.getRowHeightByPosition(0));
        assertEquals(100, this.gridLayer.getRowHeightByPosition(1));
    }

    @Test
    public void shouldHandleResetOfColumnReordering() {
        Group group1 = this.groupModel.getGroupByPosition(0);
        group1.addStaticIndexes(0, 1);

        Group group2 = this.groupModel.getGroupByPosition(4);
        group2.addStaticIndexes(5, 6);

        // reorder some columns to the first position of a group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 1));
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 5));

        assertEquals(3, group1.getStartIndex());
        assertEquals(3, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(7, group2.getStartIndex());
        assertEquals(7, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        group1.removeStaticIndexes(0, 1);
        group2.removeStaticIndexes(5, 6);

        verifyCleanState();
    }

    @Test
    public void shouldHandleResetOfColumnReorderEndOfGroupRightToLeft() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 8 to position 4
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 4));

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        Group group = this.groupModel.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(7));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
    }

    @Test
    public void shouldHandleResetOfColumnReorderMiddleOfGroupRightToLeft() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 8 to position 3
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 3));

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        Group group = this.groupModel.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(7));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
    }

    @Test
    public void shouldHandleResetOfColumnReorderStartOfGroupRightToLeft() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 8 to position 1
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 1));

        this.columnGroupHeaderLayer.addGroup("Person", 7, 4);
        Group group = this.groupModel.getGroupByPosition(0);

        assertEquals(7, group.getStartIndex());
        assertEquals(7, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(7));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        // as there is only a single column moved out of the group, the group is
        // kept and the start index is updated.
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
    }

    @Test
    public void shouldHandleResetOfColumnReorderEndOfGroupLeftToRight() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 1 to position 8
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 8));

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        Group group = this.groupModel.getGroupByPosition(4);

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(1));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
    }

    @Test
    public void shouldHandleResetOfColumnReorderMiddleOfGroupLeftToRight() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 1 to position 5
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 6));

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        Group group = this.groupModel.getGroupByPosition(4);

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(1));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
    }

    @Test
    public void shouldHandleResetOfColumnReorderStartOfGroupLeftToRight() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 1 to position 4
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 5));

        this.columnGroupHeaderLayer.addGroup("Address", 1, 4);
        Group group = this.groupModel.getGroupByPosition(4);

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(3, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(1));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(4, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
    }

    @Test
    public void shouldHandleResetOfColumnReorderEndOfGroupRightToLeftSameSize() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 8 to position 3
        // reorder column 9 to position 4
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 3));
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 9, 4));

        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        Group group = this.groupModel.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(7));
        assertTrue(group.hasMember(8));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
    }

    @Test
    public void shouldHandleResetOfColumnReorderStartOfGroupRightToLeftSameSize() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 8 to position 1
        // reorder column 9 to position 2
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 8, 1));
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 9, 2));

        this.columnGroupHeaderLayer.addGroup("Person", 7, 4);
        Group group = this.groupModel.getGroupByPosition(0);

        assertEquals(7, group.getStartIndex());
        assertEquals(7, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(7));
        assertTrue(group.hasMember(8));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        // the reset splits the group into two subgroups with same size, the
        // most left is kept in this case
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
    }

    @Test
    public void shouldHandleResetOfColumnReorderEndOfGroupLeftToRightSameSize() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 0 to position 8
        // reorder column 1 to position 9
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 2, 7));
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 7));

        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        Group group = this.groupModel.getGroupByPosition(4);

        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
    }

    @Test
    public void shouldHandleResetOfColumnReorderStartOfGroupLeftToRightSameSize() {
        // remove all groups
        this.columnGroupHeaderLayer.removeGroup(0);
        this.columnGroupHeaderLayer.removeGroup(4);
        this.columnGroupHeaderLayer.removeGroup(8);
        this.columnGroupHeaderLayer.removeGroup(11);

        // reorder column 0 to position 4
        // reorder column 1 to position 5
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 5));
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 5));

        this.columnGroupHeaderLayer.addGroup("Address", 0, 4);
        Group group = this.groupModel.getGroupByPosition(4);

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(4));
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));

        // reset reordering
        this.gridLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
    }

    @Test
    public void shouldReorderGroupWithHiddenColumns() {
        // remove the first column group
        this.columnGroupHeaderLayer.removeGroup(0);

        // hide the last two columns in the second group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 7, 8));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());

        // reorder the second group to position 0
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 1));

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());

        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(0));
        assertEquals(5, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(1));
        assertEquals(0, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(2));

        // show all columns again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        // we expect that the column group is intact
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(0));
        assertEquals(5, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(1));
        assertEquals(6, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(2));
        assertEquals(7, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(3));
        assertEquals(0, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(4));
    }

    @Test
    public void shouldReorderGroupWithReorderedColumns() {
        // remove the first column group
        this.columnGroupHeaderLayer.removeGroup(0);

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);

        // reorder the first two columns in the second group to the end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.selectionLayer, 4, 8));
        this.gridLayer.doCommand(new ColumnReorderCommand(this.selectionLayer, 4, 8));

        assertEquals(6, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(4));
        assertEquals(7, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(5));
        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(6));
        assertEquals(5, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(7));

        // reorder second group to position 2
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 5, 3));

        // we expect that the column group is intact
        assertEquals(6, group2.getStartIndex());
        assertEquals(6, group2.getVisibleStartIndex());
        assertEquals(2, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(1, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(1));
        assertEquals(6, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(2));
        assertEquals(7, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(3));
        assertEquals(4, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(4));
        assertEquals(5, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(5));
        assertEquals(2, this.columnGroupHeaderLayer.getPositionLayer().getColumnIndexByPosition(6));
    }

    @Test
    public void shouldShowColumnGroupOnReorderInHiddenState() {
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

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertNotNull(group);
        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderToRightEndInsideGroupWithHidden() {
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

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertNotNull(group1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldReorderToRightEndInsideGroupWithHidden() {
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

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertNotNull(group1);
        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
    }

    @Test
    public void shouldDragReorderUngroupedToRightWithFirstHidden() {
        // hide first column in third group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 9));

        // ungroup first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder ungrouped column to end of second group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 9));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(3);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
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
        // hide first column in third group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 9));

        // ungroup first group
        this.columnGroupHeaderLayer.removeGroup(0);

        // reorder ungrouped column to end of second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 9));

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(3);
        assertNotNull(group2);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(5, group2.getOriginalSpan());
        assertEquals(5, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
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
        // hide first column in third group and last column in second group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 8, 9));

        // try to reorder group 1 between 2 and 3
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 1, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(4, group1.getStartIndex());
        assertEquals(4, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        assertEquals("Address", group1.getName());

        Group group2 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(3);
        assertEquals(0, group2.getStartIndex());
        assertEquals(0, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());
        assertEquals("Person", group2.getName());

        Group group3 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(7);
        assertEquals(8, group3.getStartIndex());
        assertEquals(9, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());
        assertEquals("Facts", group3.getName());

        Group group4 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(9);
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
        // remove group 1
        this.columnGroupHeaderLayer.removeGroup(0);

        // hide first column in third group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 9));

        // reorder first and second column to second group end
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 1, 2 }, 9));

        ILayerCell cell = this.columnGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginColumnPosition());
        assertEquals(0, cell.getColumnPosition());
        assertEquals(2, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Gender", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

        cell = this.columnGroupHeaderLayer.getCellByPosition(1, 0);
        assertEquals(1, cell.getOriginColumnPosition());
        assertEquals(1, cell.getColumnPosition());
        assertEquals(3, cell.getColumnIndex());
        assertEquals(1, cell.getColumnSpan());
        assertEquals(2, cell.getRowSpan());
        assertEquals("Married", cell.getDataValue());
        assertEquals(100, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(100, cell.getBounds().width);
        assertEquals(40, cell.getBounds().height);

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

        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(0));
        assertNull(this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(1));

        Group group = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(2);
        assertEquals(4, group.getStartIndex());
        assertEquals(4, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());

        Group group1 = this.columnGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
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
    public void shouldShowGroupHiddenByLoadState() {
        this.columnGroupHeaderLayer.removeGroup(0);

        this.columnGroupHeaderLayer.addGroup("Person", 1, 1);

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);

        Properties properties = new Properties();
        properties.put("test" +
                ColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES, "1");
        this.columnGroupHeaderLayer.getPositionLayer().loadState("test", properties);

        // state after refresh without details via loadState
        assertEquals(-1, group.getStartIndex());
        assertEquals(-1, group.getVisibleStartIndex());
        assertEquals(-1, group.getVisibleStartPosition());
        assertEquals(1, group.getOriginalSpan());
        assertEquals(0, group.getVisibleSpan());

        // show all again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(1, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToEndWithHiddenLast() {
        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make first and second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // hide last position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 4));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder first position between first and second group
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 4));

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(2));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(3));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderUngroupedToEndWithHiddenLast() {
        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make first and second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // hide last position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 4));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder first position between first and second group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 4));

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(2));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(3));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnMultiReorderUngroupedToEndWithHiddenLast() {
        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make first and second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // hide last position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 4));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder first position between first and second group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 1 }, 4));

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(2));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(3));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderFromGroupToEndWithHiddenLast() {
        // make second and third group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderFromGroupToEndWithHiddenLast() {
        // make second and third group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        // reorder first position between second and third group
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 8));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnMultiReorderFromGroupToEndWithHiddenLast() {
        // make second and third group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide last position in second group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 8));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        // reorder first position between second and third group
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 1 }, 8));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(6));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(7));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderUngroupedToStartWithHiddenFirst() {
        // remove last position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // make first and second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // hide first position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(0, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder fourth position to first position
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 3, 1));

        assertEquals(0, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderUngroupedToStartWithHiddenFirst() {
        // remove last position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // make first and second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // hide first position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(0, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder fourth position to first position
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 3));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 1));

        assertEquals(0, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnMultiReorderUngroupedToStartWithHiddenFirst() {
        // remove last position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 3);

        // make first and second group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);

        // hide first position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(0, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // reorder fourth position to first position
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 3 }, 1));

        assertEquals(0, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderFromGroupToStartWithHiddenFirst() {
        // make first group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        // hide first position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first position in second group to first position in table
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 4, 1));

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
        assertEquals(4, this.selectionLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderFromGroupToStartWithHiddenFirst() {
        // make first group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        // hide first position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first position in second group to first position in table
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 4));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 1));

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
        assertEquals(4, this.selectionLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnMultiReorderFromGroupToStartWithHiddenFirst() {
        // make first group unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(0, true);

        // hide first position in first group
        this.gridLayer.doCommand(new ColumnHideCommand(this.gridLayer, 1));

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(1);
        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        // reorder first position in second group to first position in table
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 4 }, 1));

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(5, group2.getStartIndex());
        assertEquals(5, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(3, group2.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(0));
        assertEquals(4, this.selectionLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderUngroupedToEndWithHiddenLastTableEnd() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // remove first column from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(1, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        // reorder first position to last position in table
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 14));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(12));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderGroupToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // reorder first group to table end
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 1, 14));

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(9, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(4, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(7, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(10, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(4, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(7, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderGroupToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // reorder first group to table end
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 1));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 14));

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(9, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(4, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(7, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(10, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(4, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(7, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderGroupToStartWithHiddenFirst() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide first column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 0))) {
            fail("Column not hidden");
        }

        // reorder third group to table start
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 9, 1));

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(6, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(0, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(7, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(0, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderGroupToStartWithHiddenFirst() {
        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);

        // hide first column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 0))) {
            fail("Column not hidden");
        }

        // reorder third group to table start
        this.gridLayer.doCommand(new ColumnGroupReorderStartCommand(this.gridLayer, 0, 9));
        this.gridLayer.doCommand(new ColumnGroupReorderEndCommand(this.gridLayer, 0, 1));

        assertEquals(0, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(6, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(0, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(3, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(7, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(0, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());
    }

    @Test
    public void shouldNotBreakUnbreakableCollapsedGroupOnReorderUngroupedToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse last group
        this.columnGroupHeaderLayer.collapseGroup(11);

        // reorder first position to table end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 13));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(1, group4.getVisibleSpan());
        assertTrue(group4.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(11));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());
        this.columnGroupHeaderLayer.expandGroup(10);

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertFalse(group4.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableCollapsedGroupOnDragReorderUngroupedToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse last group
        this.columnGroupHeaderLayer.collapseGroup(11);

        // reorder first position to table end
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 13));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(1, group4.getVisibleSpan());
        assertTrue(group4.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(11));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());
        this.columnGroupHeaderLayer.expandGroup(10);

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertFalse(group4.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableCollapsedGroupOnMultiReorderUngroupedToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // remove first position from first group
        this.columnGroupHeaderLayer.removePositionsFromGroup(0, 0);

        // make all groups unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(1, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse last group
        this.columnGroupHeaderLayer.collapseGroup(11);

        // reorder first position to table end
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 1 }, 13));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(1, group4.getVisibleSpan());
        assertTrue(group4.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(11));

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());
        this.columnGroupHeaderLayer.expandGroup(10);

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
        assertFalse(group4.isCollapsed());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderFromCollapsedToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // make group 2, 3 and 4 unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        // reorder first position to table end
        this.gridLayer.doCommand(new ColumnReorderCommand(this.gridLayer, 1, 11));

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        // reorder from a collapsed group triggers expand
        assertFalse(group1.isCollapsed());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(12));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnDragReorderFromCollapsedToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // make group 2, 3 and 4 unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        // reorder first position to table end
        this.gridLayer.doCommand(new ColumnReorderStartCommand(this.gridLayer, 1));
        this.gridLayer.doCommand(new ColumnReorderEndCommand(this.gridLayer, 11));

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        // reorder from a collapsed group triggers expand
        assertFalse(group1.isCollapsed());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(12));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnMultiReorderFromCollapsedToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // make group 2, 3 and 4 unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        // reorder first position to table end
        this.gridLayer.doCommand(new MultiColumnReorderCommand(this.gridLayer, new int[] { 1 }, 11));

        assertEquals(1, group1.getStartIndex());
        assertEquals(1, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(3, group1.getVisibleSpan());
        // reorder from a collapsed group triggers expand
        assertFalse(group1.isCollapsed());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(3, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(12));

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

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(7, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(10, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());

        assertNull(this.columnGroupHeaderLayer.getGroupByPosition(13));
        assertEquals(0, this.columnGroupHeaderLayer.getColumnIndexByPosition(13));
    }

    @Test
    public void shouldNotBreakUnbreakableGroupOnReorderCollapsedGroupToEndWithHiddenLast() {
        // increase visible area to show all
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1500, 250);
            }

        });

        Group group1 = this.columnGroupHeaderLayer.getGroupByPosition(0);
        Group group2 = this.columnGroupHeaderLayer.getGroupByPosition(4);
        Group group3 = this.columnGroupHeaderLayer.getGroupByPosition(8);
        Group group4 = this.columnGroupHeaderLayer.getGroupByPosition(11);

        // make group 2, 3 and 4 unbreakable
        this.columnGroupHeaderLayer.setGroupUnbreakable(4, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(8, true);
        this.columnGroupHeaderLayer.setGroupUnbreakable(11, true);

        // hide last column in table
        if (!this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 13))) {
            fail("Column not hidden");
        }

        // collapse first group
        this.columnGroupHeaderLayer.collapseGroup(0);

        // reorder first group to table end
        this.gridLayer.doCommand(new ColumnGroupReorderCommand(this.gridLayer, 0, 1, 11));

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(9, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(1, group1.getVisibleSpan());
        assertTrue(group1.isCollapsed());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(4, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(7, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(2, group4.getVisibleSpan());

        // show all positions again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());
        this.columnGroupHeaderLayer.expandGroup(10);

        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(10, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(0, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(4, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(7, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldLoadStateWithReorderedColumns() {
        verifyCleanState();

        Properties properties = new Properties();
        this.gridLayer.saveState("clean", properties);

        // remove all groups
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(0));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(4));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(8));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(11));

        assertEquals(0, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        // reorder
        this.gridLayer.doCommand(new ColumnReorderCommand(this.selectionLayer, 2, 1));

        assertEquals(2, this.selectionLayer.getColumnIndexByPosition(1));

        // create column group
        this.selectionLayer.selectColumn(2, 0, true, true);
        this.selectionLayer.selectColumn(3, 0, true, true);
        this.gridLayer.doCommand(new CreateColumnGroupCommand("Test"));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        this.gridLayer.saveState("one", properties);

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();

        // load reordered
        this.gridLayer.loadState("one", properties);

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();
    }

    @Test
    public void shouldLoadStateWithHiddenFirstColumn() {
        verifyCleanState();

        Properties properties = new Properties();
        this.gridLayer.saveState("clean", properties);

        // remove all groups
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(0));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(4));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(8));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(11));

        assertEquals(0, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        // create column group
        this.selectionLayer.selectColumn(2, 0, true, true);
        this.selectionLayer.selectColumn(3, 0, true, true);
        this.selectionLayer.selectColumn(4, 0, true, true);
        this.gridLayer.doCommand(new CreateColumnGroupCommand("Test"));

        // hide first column in group
        this.gridLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 2));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(2, group.getStartIndex());
        assertEquals(3, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        this.gridLayer.saveState("one", properties);

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();

        // load hidden
        this.gridLayer.loadState("one", properties);

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(2, group.getStartIndex());
        assertEquals(3, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        // collapse
        this.gridLayer.doCommand(new ColumnGroupExpandCollapseCommand(this.selectionLayer, 2));

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(2, group.getStartIndex());
        assertEquals(3, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();
    }

    @Test
    public void shouldLoadStateWithCollapsedNonConsecutive() {
        verifyCleanState();

        Properties properties = new Properties();
        this.gridLayer.saveState("clean", properties);

        // remove all groups
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(0));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(4));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(8));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(11));

        assertEquals(0, this.columnGroupHeaderLayer.getGroupModel().getGroups().size());

        // create column group
        this.selectionLayer.selectColumn(2, 0, false, true);
        this.selectionLayer.selectColumn(3, 0, false, true);
        this.selectionLayer.selectColumn(5, 0, false, true);
        this.selectionLayer.selectColumn(6, 0, false, true);
        this.gridLayer.doCommand(new CreateColumnGroupCommand("Test"));

        Group group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertFalse(group.isCollapsed());
        assertEquals(2, group.getStartIndex());
        assertEquals(2, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        // collapse
        this.gridLayer.doCommand(new ColumnGroupExpandCollapseCommand(this.selectionLayer, 2));

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(2, group.getStartIndex());
        assertEquals(2, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        this.gridLayer.saveState("one", properties);

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();

        // load collapsed
        this.gridLayer.loadState("one", properties);

        group = this.columnGroupHeaderLayer.getGroupByPosition(2);
        assertNotNull(group);
        assertEquals("Test", group.getName());
        assertTrue(group.isCollapsed());
        assertEquals(2, group.getStartIndex());
        assertEquals(2, group.getVisibleStartIndex());
        assertEquals(2, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(1, group.getVisibleSpan());

        // restore the clean state again
        this.gridLayer.loadState("clean", properties);

        verifyCleanState();
    }

    @Test
    public void shouldUpdateHeightOnHidingGroupedColumns() {
        Group nameGroup = this.columnGroupHeaderLayer.getGroupByName("Person");
        Group addressGroup = this.columnGroupHeaderLayer.getGroupByName("Address");

        // remove facts and personal group
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(8));
        this.gridLayer.doCommand(new RemoveColumnGroupCommand(11));

        this.columnGroupHeaderLayer.setCalculateHeight(true);

        assertTrue(this.columnGroupHeaderLayer.getGroupModel().isVisible());
        assertEquals(4, nameGroup.getVisibleSpan());
        assertEquals(4, addressGroup.getVisibleSpan());
        assertEquals(40, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());

        // hide columns in Person group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 1, 2, 3, 4));

        assertTrue(this.columnGroupHeaderLayer.getGroupModel().isVisible());
        assertEquals(0, nameGroup.getVisibleSpan());
        assertEquals(4, addressGroup.getVisibleSpan());
        assertEquals(40, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());

        // hide columns in Address group
        this.gridLayer.doCommand(new MultiColumnHideCommand(this.gridLayer, 1, 2, 3, 4));

        assertFalse(this.columnGroupHeaderLayer.getGroupModel().isVisible());
        assertEquals(0, nameGroup.getVisibleSpan());
        assertEquals(0, addressGroup.getVisibleSpan());
        assertEquals(20, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());

        // show all columns again
        this.gridLayer.doCommand(new ShowAllColumnsCommand());

        assertTrue(this.columnGroupHeaderLayer.getGroupModel().isVisible());
        assertEquals(4, nameGroup.getVisibleSpan());
        assertEquals(4, addressGroup.getVisibleSpan());
        assertEquals(40, this.columnGroupHeaderLayer.getHeight());
        assertEquals(2, this.columnGroupHeaderLayer.getRowCount());
    }
}
