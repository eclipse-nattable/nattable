/*******************************************************************************
 * Copyright (c) 2012, 2026 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AutoResizeColumnsTest {

    private ConfigRegistry configRegistry;
    private Image img;
    private GCFactory gcFactory;

    @BeforeEach
    public void setUp() {
        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration()
                .configureRegistry(this.configRegistry);

        this.img = new Image(Display.getDefault(), 200, 150);
        this.gcFactory = new GCFactory(this.img);

        // Use a common, foxed width font to avoid failing the test on a
        // different platform
        Font normalFont = GUIHelper.getFont(new FontData("Courier", 8,
                SWT.NORMAL));
        Style cellStyle = new Style();
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, normalFont);
        this.configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                cellStyle, DisplayMode.NORMAL);
    }

    @AfterEach
    public void tearDown() {
        this.img.dispose();
    }

    private void setClientAreaProvider(ILayer layer) {
        layer.setClientAreaProvider(() -> new Rectangle(0, 0, 1050, 250));
        layer.doCommand(new ClientAreaResizeCommand(new Shell(Display
                .getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    /**
     * These sequence of actions were causing a nasty bug in AutoResize
     */
    @Test
    public void autoResizeOneColumn() {
        GridLayer gridLayer = new DummyGridLayerStack();
        setClientAreaProvider(gridLayer);

        // Resize column
        gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 2, 10));
        assertEquals(10, gridLayer.getColumnWidthByPosition(2));

        // Auto resize the one column
        InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand(
                gridLayer, 2, this.configRegistry, this.gcFactory);
        gridLayer.doCommand(command);
        // Note: the actual resized width is platform specific (font
        // dependency),
        // hence we can't compare against a fixed value.
        int columnWidth = gridLayer.getColumnWidthByPosition(2);
        assertTrue(columnWidth > 10);

        // Reorder columns
        gridLayer.doCommand(new ColumnReorderCommand(gridLayer, 2, 1));
        assertEquals(columnWidth, gridLayer.getColumnWidthByPosition(1));

        // Select all columns
        gridLayer.doCommand(new SelectAllCommand());

        // Resize all selected columns
        command = new InitializeAutoResizeColumnsCommand(gridLayer, 1,
                this.configRegistry, this.gcFactory);
        gridLayer.doCommand(command);

        for (int columnPosition = 1; columnPosition <= 20; columnPosition++) {
            assertTrue(
                    gridLayer.getColumnWidthByPosition(columnPosition) != 100,
                    "column "
                            + columnPosition
                            + " should have been resized, but it is still its original width");
        }
    }

    /**
     * Scenario: Multiple columns are selected but a non selected column is auto
     * resized.
     */
    @Test
    public void shouldAutoResizeCorrectlyIfMultipleColumnsAreSelected() {
        GridLayer gridLayer = new DefaultGridLayer(
                RowDataListFixture.getList(),
                RowDataListFixture.getPropertyNames(),
                RowDataListFixture.getPropertyToLabelMap());
        setClientAreaProvider(gridLayer);

        // Resize grid column 1, 2
        gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 1, 10));
        gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 2, 10));
        assertEquals(10, gridLayer.getColumnWidthByPosition(1));
        assertEquals(10, gridLayer.getColumnWidthByPosition(2));

        // Fully select columns 1, 2
        SelectionLayer selectionLayer = ((DefaultBodyLayerStack) gridLayer
                .getBodyLayer()).getSelectionLayer();
        selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0,
                false, false));
        selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0,
                true, false));
        assertEquals(2, selectionLayer.getFullySelectedColumnPositions().length);

        // Resize grid column 5
        gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 5, 10));
        assertEquals(10, gridLayer.getColumnWidthByPosition(5));

        // Auto resize column 5
        InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand(
                gridLayer, 5, this.configRegistry, this.gcFactory);
        gridLayer.doCommand(command);

        // Columns 1 and 2 should not be resized
        assertEquals(10, gridLayer.getColumnWidthByPosition(1));
        assertEquals(10, gridLayer.getColumnWidthByPosition(2));
        assertTrue(gridLayer.getColumnWidthByPosition(5) > 10);
    }
}
