/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoxingStyleTest {

    private static final int ROW_HEADER_COLUMN_COUNT = 1;
    private NatTable natTable;
    private ConfigRegistry configRegistry;
    private Style cellStyle;
    private TextPainter cellPainter;
    private GC gc;

    @BeforeEach
    public void setUp() throws Exception {
        this.natTable = new NatTableFixture();
        this.configRegistry = (ConfigRegistry) this.natTable.getConfigRegistry();
        this.cellStyle = new Style();
        this.cellPainter = new TextPainter();

        this.gc = new GC(Display.getDefault());
    }

    @AfterEach
    public void tearDown() {
        this.gc.dispose();
    }

    // Background color
    @Test
    public void retrievedCellShouldHaveConfiguredBackground() {
        // Register background color for body cells in normal mode
        final Color backgroundColor = Display.getDefault().getSystemColor(
                SWT.COLOR_GRAY);
        this.cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
                backgroundColor);
        this.configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                this.cellStyle, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        // Check for background color styling
        ILayerCell cell = this.natTable.getCellByPosition(2, 2);
        IStyle cellStyle = this.configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(), cell
                        .getConfigLabels());
        assertEquals(backgroundColor, cellStyle
                .getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));

        // set up painter
        this.cellPainter.setupGCFromConfig(this.gc, cellStyle);
        assertEquals(backgroundColor, this.gc.getBackground());
    }

    // Foreground color
    @Test
    public void retrievedCellShouldHaveConfiguredForegroundColor() {
        // Register foreground color for body cells in normal mode
        final Color foregroundColor = Display.getDefault().getSystemColor(
                SWT.COLOR_BLACK);
        this.cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR,
                foregroundColor);
        this.configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                this.cellStyle, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        // Check cell foreground color
        ILayerCell cell = this.natTable.getCellByPosition(2, 2);
        IStyle cellStyle = this.configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(), cell
                        .getConfigLabels());
        assertEquals(foregroundColor, cellStyle
                .getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

        // set up painter
        this.cellPainter.setupGCFromConfig(this.gc, cellStyle);
        assertEquals(foregroundColor, this.gc.getForeground());
    }

    // Horizontal alignment
    @Test
    public void retreivedCellShouldHaveRightAlignment() {
        // Register horizontal alignment
        final HorizontalAlignmentEnum hAlignment = HorizontalAlignmentEnum.RIGHT;
        this.cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                hAlignment);
        this.configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                this.cellStyle, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        // Check cell horizontal alignment
        ILayerCell cell = this.natTable.getCellByPosition(2, 2);
        assertEquals(
                hAlignment.name(),
                this.configRegistry
                        .getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                                cell.getDisplayMode(),
                                cell.getConfigLabels())
                        .getAttributeValue(
                                CellStyleAttributes.HORIZONTAL_ALIGNMENT)
                        .name());
    }

    // Vertical alignment
    @Test
    public void retreivedCellShouldHaveTopAlignment() {
        // Register vertical alignment
        final VerticalAlignmentEnum vAlignment = VerticalAlignmentEnum.TOP;
        this.cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT,
                vAlignment);
        this.configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                this.cellStyle, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);

        // Check cell vertical alignment
        ILayerCell cell = this.natTable.getCellByPosition(2, 3);
        assertEquals(
                vAlignment.name(),
                this.configRegistry
                        .getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                                cell.getDisplayMode(),
                                cell.getConfigLabels())
                        .getAttributeValue(
                                CellStyleAttributes.VERTICAL_ALIGNMENT)
                        .name());
    }

    @Test
    public void retrievedCellShouldBeConvertedUsingTheDisplayConverter()
            throws Exception {
        IConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new DisplayConverter() {

                    @Override
                    public Object canonicalToDisplayValue(Object canonicalValue) {
                        if (canonicalValue == null) {
                            return null;
                        } else {
                            return canonicalValue.toString().equals("MN") ? "Manual" : "Automatic";
                        }
                    }

                    @Override
                    public Object displayToCanonicalValue(Object displayValue) {
                        return displayValue.toString().equals("Manual") ? new PricingTypeBean("MN") : new PricingTypeBean("AT");
                    }

                });

        NatTableFixture natTableFixture = new NatTableFixture(
                new DefaultGridLayer(RowDataListFixture.getList(),
                        RowDataListFixture.getPropertyNames(),
                        RowDataListFixture.getPropertyToLabelMap()),
                false);
        natTableFixture.setConfigRegistry(configRegistry);
        natTableFixture.configure();

        int columnIndex = RowDataListFixture
                .getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME);
        Object dataValue = natTableFixture.getDataValueByPosition(columnIndex
                + ROW_HEADER_COLUMN_COUNT, 2);

        // Verify displayed value
        ILayerCell cell = natTableFixture.getCellByPosition(columnIndex
                + ROW_HEADER_COLUMN_COUNT, 2);
        TextPainter cellPainter = new TextPainter();
        assertEquals("Automatic",
                cellPainter.convertDataType(cell, configRegistry));

        // Assert that the display value is converted to an Object
        assertTrue(dataValue instanceof PricingTypeBean);
    }

}
