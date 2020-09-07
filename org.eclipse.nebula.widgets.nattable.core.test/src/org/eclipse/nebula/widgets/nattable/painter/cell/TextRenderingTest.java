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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextRenderingTest {

    private NatTable natTable;
    private ConfigRegistry configRegistry;
    private TextPainter cellPainter;
    private GC gc;
    private Font defaultFont;

    @Before
    public void setUp() throws Exception {
        this.natTable = new NatTableFixture();
        this.configRegistry = (ConfigRegistry) this.natTable.getConfigRegistry();
        this.cellPainter = new TextPainter();

        this.gc = new GC(Display.getDefault());
    }

    @After
    public void tearDown() {
        if (this.defaultFont != null) {
            this.defaultFont.dispose();
        }
        this.gc.dispose();
    }

    @Test
    public void gcShouldHaveConfiguredBoldFont() {
        registerFont(new FontData("Verdana", 10, SWT.BOLD));
        verifyFontAttributes();
    }

    @Test
    public void gcShouldHaveConfiguredItalicFont() {
        registerFont(new FontData("Verdana", 10, SWT.BOLD | SWT.ITALIC));
        verifyFontAttributes();
    }

    @Test
    public void testWhitespacePattern() {
        Pattern pattern = Pattern.compile("\\s*$");
        Matcher matcher = pattern.matcher("012345        		   ");
        assertTrue(matcher.find());
        assertEquals(6, matcher.start());
        matcher = pattern.matcher("0123");
        assertTrue(matcher.find());
        assertEquals(4, matcher.start());
        matcher = pattern.matcher("");
        assertTrue(matcher.find());
        assertEquals(0, matcher.start());

        pattern = Pattern.compile("\\s+\\S+\\s*$");
        matcher = pattern.matcher("  Blah 	 blah		blah  			theEnd  ");
        assertTrue(matcher.find());
        assertEquals(19, matcher.start());
    }

    @Test
    public void testLineBreak() {
        assertEquals("", "012345".substring(6));
    }

    private void registerFont(FontData fontData) {
        // Register default body font
        Style cellStyle = new Style();
        this.defaultFont = GUIHelper.getFont(fontData);
        cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.defaultFont);
        this.configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                cellStyle, DisplayMode.NORMAL,
                AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
    }

    private void verifyFontAttributes() {
        // Check cell font attributes
        ILayerCell cell = this.natTable.getCellByPosition(2, 2);
        final FontData expectedFontData = this.defaultFont.getFontData()[0];
        IStyle cellStyle = this.configRegistry.getConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cell.getDisplayMode(),
                cell.getConfigLabels());
        final FontData actualFontData = cellStyle.getAttributeValue(CellStyleAttributes.FONT).getFontData()[0];
        assertEquals(actualFontData.getName(), expectedFontData.getName());
        assertEquals(actualFontData.getHeight(), expectedFontData.getHeight());
        assertEquals(actualFontData.getStyle(), expectedFontData.getStyle());

        // Draw font
        this.cellPainter.setupGCFromConfig(this.gc, cellStyle);
        final FontData expectedDrawnFontData = this.gc.getFont().getFontData()[0];
        assertEquals(actualFontData.getName(), expectedDrawnFontData.getName());
        assertEquals(actualFontData.getHeight(), expectedDrawnFontData.getHeight());
        assertEquals(actualFontData.getStyle(), expectedDrawnFontData.getStyle());
    }

    @Test
    public void testLineWrap() {
        String testString1 = "Hello Mister,\nhow are you?\nI'm fine!";
        String testString2 = "Hello Mister,\rhow are you?\rI'm fine!";
        String testString3 = "Hello Mister,\n\rhow are you?\n\rI'm fine!";
        String testString4 = "Hello Mister,\r\nhow are you?\r\nI'm fine!";

        assertEquals(3, this.cellPainter.getNumberOfNewLines(testString1));
        assertEquals(3, this.cellPainter.getNumberOfNewLines(testString2));
        assertEquals(3, this.cellPainter.getNumberOfNewLines(testString3));
        assertEquals(3, this.cellPainter.getNumberOfNewLines(testString4));
    }
}
