/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupHeaderLayerTest {

    public static final String TEST_GROUP_NAME_3 = "testGroupName3";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    public static final String NO_GROUP_NAME = "";
    public ColumnGroupHeaderLayer columnGroupLayer;
    private ColumnGroupModel model;
    private DefaultGridLayer gridLayer;

    @Before
    public void setup() {
        this.gridLayer = new GridLayerFixture();
        this.model = new ColumnGroupModel();
        // 10 columns in header
        this.columnGroupLayer = new ColumnGroupHeaderLayer(
                this.gridLayer.getColumnHeaderLayer(),
                this.gridLayer.getBodyLayer().getSelectionLayer(),
                this.model);

        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1);
        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 5, 6);
        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 8, 9);

        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(
                new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    @Test
    public void getCellForACellInAColumnGroup() {
        ILayerCell cell = this.columnGroupLayer.getCellByPosition(0, 0);

        assertEquals(TEST_GROUP_NAME_1, cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);

        cell = this.columnGroupLayer.getCellByPosition(1, 0);
        assertEquals(TEST_GROUP_NAME_1, cell.getDataValue());
        assertEquals(0, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void aCollapsedColumnGroupShouldNotBeSpanned() {
        assertEquals(2, this.columnGroupLayer.getCellByPosition(0, 0).getColumnSpan());

        this.columnGroupLayer.setGroupAsCollapsed(0);

        assertEquals(1, this.columnGroupLayer.getCellByPosition(0, 0).getColumnSpan());

    }

    @Test
    public void getCellForACellNotInAColumnGroup() {
        ILayerCell cell = this.columnGroupLayer.getCellByPosition(5, 0);

        assertEquals(500, cell.getBounds().x);
        assertEquals(0, cell.getBounds().y);
        assertEquals(200, cell.getBounds().width);
        assertEquals(20, cell.getBounds().height);
    }

    @Test
    public void getDataValueByPosition() {
        String dataValueGroup1 = (String) this.columnGroupLayer.getDataValueByPosition(1, 0);
        String dataValue = (String) this.columnGroupLayer.getDataValueByPosition(2, 0);

        assertEquals(TEST_GROUP_NAME_1, dataValueGroup1);
        assertEquals("[2,0]", dataValue); // Regular header
    }

    @Test
    public void getColumnWidthByPosition() {
        // Col 0,1 are in group 150 + 100
        assertEquals(100, this.columnGroupLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.columnGroupLayer.getColumnWidthByPosition(1));
    }

    @Test
    public void getColumnWidthWhenNoColumnGroupsPresent() {
        this.columnGroupLayer.clearAllGroups();
        // Width of the vsible columns - see fixture

        assertEquals(1000, this.columnGroupLayer.getWidth());
    }

    @Test
    public void getColumnWidthByPositionForAColumnOutsideTheViewport() {
        // Returns default column width from DataLayer
        assertEquals(100, this.columnGroupLayer.getColumnWidthByPosition(100));
    }

    @Test
    public void getCellSpan() {
        // Index in group: 5, 6, 7
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7);
        assertEquals(3, this.columnGroupLayer.getColumnSpan(5));

        this.model.getColumnGroupByIndex(5).setCollapsed(true);
        assertEquals(1, this.columnGroupLayer.getColumnSpan(5));
    }

    @Test
    public void getCellSpanWhenColumnsInGroupAreHidden() {
        // Index in group: 5, 6, 7
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7);
        assertEquals(3, this.columnGroupLayer.getColumnSpan(5));

        // Hide position 6
        ColumnHideCommand hideColumnCommand = new ColumnHideCommand(this.gridLayer.getBodyLayer(), 6);
        this.gridLayer.getBodyLayer().getColumnHideShowLayer().doCommand(hideColumnCommand);

        assertEquals(2, this.columnGroupLayer.getColumnSpan(5));

        // Hide position 5
        hideColumnCommand = new ColumnHideCommand(this.gridLayer.getBodyLayer(), 6);
        this.gridLayer.getBodyLayer().getColumnHideShowLayer().doCommand(hideColumnCommand);

        assertEquals(1, this.columnGroupLayer.getColumnSpan(5));
    }

    @Test
    public void testConfigLabels() {
        // check expanded column group
        LabelStack stack = this.columnGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(DefaultColumnGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.model.getColumnGroupByIndex(0).setCollapsed(true);
        stack = this.columnGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(2, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel(DefaultColumnGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE));

        // check ungrouped
        stack = this.columnGroupLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(0, stack.size());
    }

    @Test
    public void testConfigLabelsWithAccumulator() {
        // set config label accumulator
        this.columnGroupLayer.setConfigLabelAccumulator(new IConfigLabelAccumulator() {

            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                if (columnPosition == 0 || columnPosition == 3) {
                    configLabels.addLabel("custom");
                }
            }
        });

        // check expanded column group
        LabelStack stack = this.columnGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(3, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel("custom"));
        assertTrue(stack.hasLabel(DefaultColumnGroupHeaderLayerConfiguration.GROUP_EXPANDED_CONFIG_TYPE));

        // check collapsed column group
        this.model.getColumnGroupByIndex(0).setCollapsed(true);
        stack = this.columnGroupLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(3, stack.size());
        assertTrue(stack.hasLabel(GridRegion.COLUMN_GROUP_HEADER));
        assertTrue(stack.hasLabel("custom"));
        assertTrue(stack.hasLabel(DefaultColumnGroupHeaderLayerConfiguration.GROUP_COLLAPSED_CONFIG_TYPE));

        // check ungrouped
        stack = this.columnGroupLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(0, stack.size());
    }

    @Test
    public void testRowHeightByPosition() {
        this.columnGroupLayer.clearAllGroups();
        this.columnGroupLayer.setRowHeight(100);
        // Height of the header column row - see fixture
        assertEquals(120, this.columnGroupLayer.getHeight());
        assertEquals(2, this.columnGroupLayer.getRowCount());
        assertEquals(100, this.columnGroupLayer.getRowHeightByPosition(0));
        assertEquals(20, this.columnGroupLayer.getRowHeightByPosition(1));
        // Test calculated height
        this.columnGroupLayer.setCalculateHeight(true);
        assertEquals(20, this.columnGroupLayer.getHeight());
        assertEquals(1, this.columnGroupLayer.getRowCount());
        assertEquals(20, this.columnGroupLayer.getRowHeightByPosition(0));
    }
}
