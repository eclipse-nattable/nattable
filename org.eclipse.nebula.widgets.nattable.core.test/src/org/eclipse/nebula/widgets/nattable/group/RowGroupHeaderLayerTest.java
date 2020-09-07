/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth
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
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultRowGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
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

public class RowGroupHeaderLayerTest {

    private IRowDataProvider<Person> bodyDataProvider;
    public RowGroupHeaderLayer<Person> rowGroupLayer;
    private RowGroupModel<Person> rowGroupModel;
    private GridLayer gridLayer;

    @Before
    public void setup() {
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };

        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");

        this.bodyDataProvider = new DefaultBodyDataProvider<>(getStaticPersonList(), propertyNames);
        DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);

        RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(columnHideShowLayer);
        this.rowGroupModel = new RowGroupModel<>();
        this.rowGroupModel.setDataProvider(this.bodyDataProvider);
        RowGroupExpandCollapseLayer<Person> rowExpandCollapseLayer =
                new RowGroupExpandCollapseLayer<>(rowHideShowLayer, this.rowGroupModel);

        SelectionLayer selectionLayer = new SelectionLayer(rowExpandCollapseLayer);
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // Column header

        DefaultColumnHeaderDataProvider defaultColumnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DefaultColumnHeaderDataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(defaultColumnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);

        // Row header

        DefaultRowHeaderDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(this.bodyDataProvider);
        DefaultRowHeaderDataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);

        RowHeaderLayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

        this.rowGroupLayer =
                new RowGroupHeaderLayer<>(rowHeaderLayer, selectionLayer, this.rowGroupModel);
        this.rowGroupLayer.setColumnWidth(20);

        // Create a group of rows for the model.
        RowGroup<Person> rowGroup = new RowGroup<>(this.rowGroupModel, "Simpson");
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(0));
        rowGroup.addStaticMemberRow(this.bodyDataProvider.getRowObject(1));
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(2));
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(3));
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(4));
        this.rowGroupModel.addRowGroup(rowGroup);

        rowGroup = new RowGroup<>(this.rowGroupModel, "Flanders");
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(5));
        rowGroup.addStaticMemberRow(this.bodyDataProvider.getRowObject(6));
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(7));
        this.rowGroupModel.addRowGroup(rowGroup);

        rowGroup = new RowGroup<>(this.rowGroupModel, "Lovejoy");
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(9));
        rowGroup.addStaticMemberRow(this.bodyDataProvider.getRowObject(10));
        rowGroup.addMemberRow(this.bodyDataProvider.getRowObject(11));
        this.rowGroupModel.addRowGroup(rowGroup);

        // Corner
        final DefaultCornerDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(defaultColumnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, this.rowGroupLayer, columnHeaderLayer);

        // Grid
        this.gridLayer =
                new GridLayer(viewportLayer, columnHeaderLayer, this.rowGroupLayer, cornerLayer);

        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 300);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    @Test
    public void shouldGetSameCellForACellInSameRowGroup() {

        ILayerCell cell = this.rowGroupLayer.getCellByPosition(0, 0);
        assertEquals("Simpson", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(20, cell.getBounds().width);
        assertEquals(100, cell.getBounds().height);

        cell = this.rowGroupLayer.getCellByPosition(0, 2);
        assertEquals("Simpson", cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(20, cell.getBounds().width);
        assertEquals(100, cell.getBounds().height);
    }

    @Test
    public void collapsedRowGroupShouldNotBeSpanned() {
        assertEquals(5, this.rowGroupLayer.getCellByPosition(0, 0).getRowSpan());

        this.rowGroupLayer.collapseRowGroupByIndex(0);

        assertEquals(1, this.rowGroupLayer.getCellByPosition(0, 0).getRowSpan());
    }

    @Test
    public void getCellForACellNotInAColumnGroup() {
        ILayerCell cell = this.rowGroupLayer.getCellByPosition(0, 8);

        assertEquals(0, cell.getBounds().x);
        assertEquals(160, cell.getBounds().y);
        assertEquals(60, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
        assertEquals(9, cell.getDataValue());
    }

    @Test
    public void getDataValueByPosition() {
        Object dataValueGroup1 = this.rowGroupLayer.getDataValueByPosition(0, 1);
        Object dataValue = this.rowGroupLayer.getDataValueByPosition(0, 8);

        assertEquals("Simpson", dataValueGroup1);
        assertEquals(9, dataValue); // Regular header
    }

    @Test
    public void getRowHeightByPosition() {
        assertEquals(20, this.rowGroupLayer.getRowHeightByPosition(0));
        assertEquals(20, this.rowGroupLayer.getRowHeightByPosition(1));
    }

    @Test
    public void getRowHeightWhenNoColumnGroupsPresent() {
        this.rowGroupLayer.clearAllGroups();
        // Height of the visible rows

        assertEquals(240, this.rowGroupLayer.getHeight());
    }

    @Test
    public void getCellSpan() {
        // Index in group: 5, 6, 7, 8
        this.rowGroupModel.getRowGroupForName("Flanders").addMemberRow(this.bodyDataProvider.getRowObject(8));
        assertEquals(4, this.rowGroupLayer.getRowSpan(5));

        this.rowGroupModel.getRowGroupForName("Flanders").collapse();
        assertEquals(1, this.rowGroupLayer.getRowSpan(5));
    }

    @Test
    public void getCellSpanWhenRowsInGroupAreHidden() {
        // Index in group: 5, 6, 7, 8
        this.rowGroupModel.getRowGroupForName("Flanders").addMemberRow(this.bodyDataProvider.getRowObject(8));
        assertEquals(4, this.rowGroupLayer.getRowSpan(5));

        // Hide position 6
        RowHideCommand hideCommand = new RowHideCommand(this.gridLayer.getBodyLayer(), 6);
        this.gridLayer.getBodyLayer().doCommand(hideCommand);

        assertEquals(3, this.rowGroupLayer.getRowSpan(5));

        // Hide position 6 (now index 7)
        hideCommand = new RowHideCommand(this.gridLayer.getBodyLayer(), 6);
        this.gridLayer.getBodyLayer().doCommand(hideCommand);

        assertEquals(2, this.rowGroupLayer.getRowSpan(5));
    }

    @Test
    public void testConfigLabels() {
        // check expanded row group
        LabelStack stack = this.rowGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(DefaultRowGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed row group
        this.rowGroupModel.getRowGroupForName("Simpson").collapse();
        stack = this.rowGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel(DefaultRowGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE));

        // check ungrouped
        stack = this.rowGroupLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(0, stack.size());
    }

    @Test
    public void testConfigLabelsWithAccumulator() {
        // set config label accumulator
        this.rowGroupLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (rowPosition == 0 || rowPosition == 3) {
                    configLabels.addLabel("custom");
                }
            }
        });

        // check expanded row group
        LabelStack stack = this.rowGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(3, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel("custom"));
        assertTrue(stack.hasLabel(DefaultRowGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.rowGroupModel.getRowGroupForName("Simpson").collapse();
        stack = this.rowGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(3, stack.size());
        assertTrue(stack.hasLabel(GridRegion.ROW_GROUP_HEADER));
        assertTrue(stack.hasLabel("custom"));
        assertTrue(stack.hasLabel(DefaultRowGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE));

        // check ungrouped
        stack = this.rowGroupLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(0, stack.size());
    }

    @Test
    public void testColumnWidthByPosition() {
        this.rowGroupLayer.clearAllGroups();
        this.rowGroupLayer.setColumnWidth(50);
        // Width of the header row column
        assertEquals(90, this.rowGroupLayer.getWidth());
        assertEquals(2, this.rowGroupLayer.getColumnCount());
        assertEquals(50, this.rowGroupLayer.getColumnWidthByPosition(0));
        assertEquals(40, this.rowGroupLayer.getColumnWidthByPosition(1));
    }

    private ArrayList<Person> getStaticPersonList() {
        ArrayList<Person> result = new ArrayList<>();

        // create some persons
        result.add(new Person(1, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(2, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
        result.add(new Person(3, "Bart", "Simpson", Gender.MALE, false, new Date()));
        result.add(new Person(4, "Lisa", "Simpson", Gender.FEMALE, false, new Date()));
        result.add(new Person(5, "Maggie", "Simpson", Gender.FEMALE, false, new Date()));

        result.add(new Person(6, "Ned", "Flanders", Gender.MALE, true, new Date()));
        result.add(new Person(7, "Maude", "Flanders", Gender.FEMALE, true, new Date()));
        result.add(new Person(8, "Rod", "Flanders", Gender.MALE, false, new Date()));
        result.add(new Person(9, "Todd", "Flanders", Gender.MALE, false, new Date()));

        result.add(new Person(10, "Timothy", "Lovejoy", Gender.MALE, true, new Date()));
        result.add(new Person(11, "Helen", "Lovejoy", Gender.FEMALE, true, new Date()));
        result.add(new Person(12, "Jessica", "Lovejoy", Gender.FEMALE, false, new Date()));

        return result;
    }
}
