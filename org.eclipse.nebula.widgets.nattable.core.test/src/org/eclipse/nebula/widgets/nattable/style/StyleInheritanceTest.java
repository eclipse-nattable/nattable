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
package org.eclipse.nebula.widgets.nattable.style;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

public class StyleInheritanceTest {

    private static final FontData FONT_DATA = new FontData("Arial", 10, SWT.BOLD | SWT.ITALIC);

    private NatTable natTable;
    private IStyle evenCellStyle;
    private IStyle oddCellStyle;
    private Style superCellStyle;
    private IConfigRegistry configRegistry;
    private Color defaultBackgroundColor;
    private final Font font = GUIHelper.getFont(FONT_DATA);

    @Before
    public void setUp() throws Exception {
        this.natTable = new NatTableFixture();
        this.superCellStyle = new Style();
        this.superCellStyle.setAttributeValue(CellStyleAttributes.FONT, this.font);
        this.defaultBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        this.superCellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                this.defaultBackgroundColor);
        this.superCellStyle.setAttributeValue(
                CellStyleAttributes.VERTICAL_ALIGNMENT,
                VerticalAlignmentEnum.TOP);

        this.configRegistry = this.natTable.getConfigRegistry();
        this.configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                this.superCellStyle);

        // Setup even row style
        this.evenCellStyle = new Style();
        this.evenCellStyle.setAttributeValue(
                CellStyleAttributes.BACKGROUND_COLOR,
                Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
        this.evenCellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

        this.configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                this.evenCellStyle,
                DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);

        // Setup odd row style
        this.oddCellStyle = new Style();
        this.oddCellStyle.setAttributeValue(
                CellStyleAttributes.FOREGROUND_COLOR,
                Display.getDefault().getSystemColor(SWT.COLOR_RED));
        this.configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                this.oddCellStyle,
                DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
    }

    @Test
    public void shouldFallBackToSuperTypeAttributesForEvenCell() {
        ILayerCell cell = this.natTable.getCellByPosition(2, 2);

        // Test cell even attributes
        IStyle cellInstanceStyle = this.configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cell.getDisplayMode(),
                cell.getConfigLabels());
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_BLACK),
                cellInstanceStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_GRAY),
                cellInstanceStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));

        // Test super cell attributes
        StyleProxy cellProxy = new CellStyleProxy(this.configRegistry, cell.getDisplayMode(), cell.getConfigLabels());
        Font fontAttribute = cellProxy.getAttributeValue(CellStyleAttributes.FONT);
        assertEquals(FONT_DATA.getName(), fontAttribute.getFontData()[0].getName());
        assertEquals(FONT_DATA.getStyle(), fontAttribute.getFontData()[0].getStyle());
    }

    @Test
    public void shouldFallBackToSuperTypeAttributesForOddCell() {
        ILayerCell cell = this.natTable.getCellByPosition(2, 3);

        // Test cell odd attributes
        IStyle cellInstanceStyle = this.configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cell.getDisplayMode(),
                cell.getConfigLabels());
        assertEquals(
                Display.getDefault().getSystemColor(SWT.COLOR_RED),
                cellInstanceStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

        // Test super odd attributes
        StyleProxy cellProxy = new CellStyleProxy(this.configRegistry, cell.getDisplayMode(), cell.getConfigLabels());
        Color fontAttributeValue = cellProxy.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
        assertEquals(this.defaultBackgroundColor, fontAttributeValue);
    }
}
