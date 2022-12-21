/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeRowCommand;
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
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowColumnInViewportCommand;
import org.eclipse.nebula.widgets.nattable.viewport.command.ShowRowInViewportCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupHeaderFreezeTest {

    GroupModel columnGroupModel;
    ColumnGroupHeaderLayer columnGroupHeaderLayer;
    ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;

    GroupModel rowGroupModel;
    RowGroupHeaderLayer rowGroupHeaderLayer;
    RowGroupExpandCollapseLayer rowGroupExpandCollapseLayer;

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
                        PersonService.getExtendedPersonsWithAddress(14),
                        columnPropertyAccessor);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);

        this.columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer);

        RowReorderLayer rowReorderLayer = new RowReorderLayer(this.columnGroupExpandCollapseLayer);
        RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(rowReorderLayer);

        this.rowGroupExpandCollapseLayer = new RowGroupExpandCollapseLayer(rowHideShowLayer);

        this.selectionLayer = new SelectionLayer(this.rowGroupExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);

        FreezeLayer freezeLayer = new FreezeLayer(this.selectionLayer);
        CompositeFreezeLayer compositeFreezeLayer =
                new CompositeFreezeLayer(freezeLayer, viewportLayer, this.selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, compositeFreezeLayer, this.selectionLayer);
        this.columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, this.selectionLayer);

        this.columnGroupModel = this.columnGroupHeaderLayer.getGroupModel();

        // configure the column groups
        this.columnGroupHeaderLayer.addGroup("Person", 0, 4);
        this.columnGroupHeaderLayer.addGroup("Address", 4, 4);
        this.columnGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.columnGroupHeaderLayer.addGroup("Personal", 11, 3);

        // build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, compositeFreezeLayer, this.selectionLayer);

        this.rowGroupHeaderLayer = new RowGroupHeaderLayer(rowHeaderLayer, this.selectionLayer);

        this.rowGroupModel = this.rowGroupHeaderLayer.getGroupModel();

        // configure the row groups
        this.rowGroupHeaderLayer.addGroup("Person", 0, 4);
        this.rowGroupHeaderLayer.addGroup("Address", 4, 4);
        this.rowGroupHeaderLayer.addGroup("Facts", 8, 3);
        this.rowGroupHeaderLayer.addGroup("Personal", 11, 3);

        // build the corner layer
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, this.rowGroupHeaderLayer, this.columnGroupHeaderLayer);

        // build the grid layer
        this.gridLayer = new GridLayer(compositeFreezeLayer, this.columnGroupHeaderLayer, this.rowGroupHeaderLayer, cornerLayer);

        // configure the visible area, needed for tests in scrolled state
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                // 10 columns + 10 rows with header and group header should be
                // visible
                return new Rectangle(0, 0, 1010, 240);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(1, this.columnGroupHeaderLayer.getLevelCount());
        verifyCleanState();
    }

    private void verifyCleanState() {
        // columns

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

        // rows

        // nothing hidden below the SelectionLayer
        assertEquals(14, this.selectionLayer.getRowCount());

        for (int row = 0; row < this.rowGroupHeaderLayer.getRowCount(); row++) {
            assertTrue(this.rowGroupHeaderLayer.isPartOfAGroup(row));
            assertFalse(this.rowGroupHeaderLayer.isPartOfAnUnbreakableGroup(row));
        }

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 8);
        assertEquals(8, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(160, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        // this cell is not visible because of the client area
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(-1, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        group1 = this.rowGroupHeaderLayer.getGroupModel().getGroupByPosition(0);
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());

        group2 = this.rowGroupHeaderLayer.getGroupModel().getGroupByPosition(4);
        assertEquals(4, group2.getStartIndex());
        assertEquals(4, group2.getVisibleStartIndex());
        assertEquals(4, group2.getVisibleStartPosition());
        assertEquals(4, group2.getOriginalSpan());
        assertEquals(4, group2.getVisibleSpan());

        group3 = this.rowGroupHeaderLayer.getGroupModel().getGroupByPosition(8);
        assertEquals(8, group3.getStartIndex());
        assertEquals(8, group3.getVisibleStartIndex());
        assertEquals(8, group3.getVisibleStartPosition());
        assertEquals(3, group3.getOriginalSpan());
        assertEquals(3, group3.getVisibleSpan());

        group4 = this.rowGroupHeaderLayer.getGroupModel().getGroupByPosition(11);
        assertEquals(11, group4.getStartIndex());
        assertEquals(11, group4.getVisibleStartIndex());
        assertEquals(11, group4.getVisibleStartPosition());
        assertEquals(3, group4.getOriginalSpan());
        assertEquals(3, group4.getVisibleSpan());
    }

    @Test
    public void shouldRenderGroups() {
        assertEquals(12, this.gridLayer.getColumnCount());
        assertEquals(12, this.gridLayer.getRowCount());

        // increase the client area to show all columns and all rows
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1600, 320);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        assertEquals(16, this.gridLayer.getColumnCount());
        assertEquals(16, this.gridLayer.getRowCount());

        // columns

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

        // rows

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 8);
        assertEquals(8, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(160, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);
    }

    @Test
    public void shouldRenderGroupsWithFrozenColumns() {
        this.gridLayer.doCommand(new FreezeColumnCommand(this.gridLayer, 5));

        // freeze should not affect the rendering
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

        assertEquals(9, this.gridLayer.getColumnIndexByPosition(11));

        // scroll to get the last column into the viewport
        this.gridLayer.doCommand(new ShowColumnInViewportCommand(13));

        assertEquals(13, this.gridLayer.getColumnIndexByPosition(11));

        // the first 4 columns are not visible anymore, so we need to verify
        // against column position 7
        cell = this.columnGroupHeaderLayer.getCellByPosition(7, 0);
        assertEquals(7, cell.getOriginColumnPosition());
        assertEquals(3, cell.getColumnSpan());
        assertEquals("Personal", cell.getDataValue());
        // client area width 1010 - 300 for last 3 columns - row header offset
        // (20+40)
        assertEquals(650, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(300, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void shouldRenderGroupsWithFrozenRows() {
        this.gridLayer.doCommand(new FreezeRowCommand(this.gridLayer, 5));

        // freeze should not affect the rendering
        ILayerCell cell = this.rowGroupHeaderLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowSpan());
        assertEquals("Person", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 4);
        assertEquals(4, cell.getOriginRowPosition());
        assertEquals(4, cell.getRowSpan());
        assertEquals("Address", cell.getDataValue());
        assertEquals(80, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(80, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 8);
        assertEquals(8, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Facts", cell.getDataValue());
        assertEquals(160, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 11);
        assertEquals(11, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        assertEquals(220, cell.getBounds().y);
        assertEquals(0, cell.getBounds().x);
        assertEquals(60, cell.getBounds().height);
        assertEquals(20, cell.getBounds().width);

        assertEquals(9, this.gridLayer.getRowIndexByPosition(11));

        // scroll to get the last row into the viewport
        this.gridLayer.doCommand(new ShowRowInViewportCommand(13));

        assertEquals(13, this.gridLayer.getRowIndexByPosition(11));

        // the first 4 rows are not visible anymore, so we need to verify
        // against row position 7
        cell = this.rowGroupHeaderLayer.getCellByPosition(0, 7);
        assertEquals(7, cell.getOriginRowPosition());
        assertEquals(3, cell.getRowSpan());
        assertEquals("Personal", cell.getDataValue());
        // client area height 240 - 60 for last 3 rows - column header
        // offset (20+20)
        assertEquals(0, cell.getBounds().x);
        assertEquals(140, cell.getBounds().y);
        assertEquals(20, cell.getBounds().width);
        assertEquals(60, cell.getBounds().height);
    }
}
