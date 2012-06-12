/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.painter.cell;

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
import org.junit.Assert;
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
		natTable = new NatTableFixture();
		configRegistry = (ConfigRegistry) natTable.getConfigRegistry();
		cellPainter = new TextPainter();

		gc = new GC(Display.getDefault());
	}

	@After
	public void tearDown() {
		if (defaultFont != null) {
			defaultFont.dispose();
		}
		gc.dispose();
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
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(6, matcher.start());
		matcher = pattern.matcher("0123");
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(4, matcher.start());
		matcher = pattern.matcher("");
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(0, matcher.start());

		pattern = Pattern.compile("\\s+\\S+\\s*$");
		matcher = pattern.matcher("  Blah 	 blah		blah  			theEnd  ");
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(19, matcher.start());
	}

	@Test
	public void testLineBreak() {
		Assert.assertEquals("", "012345".substring(6));
	}

	private void registerFont(FontData fontData) {
		// Register default body font
		Style cellStyle = new Style();
		defaultFont =  GUIHelper.getFont(fontData);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, defaultFont);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
	}

	private void verifyFontAttributes() {
		// Check cell font attributes
		ILayerCell cell = natTable.getCellByPosition(2, 2);
		final FontData expectedFontData = defaultFont.getFontData()[0];
		IStyle cellStyle = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		final FontData actualFontData = cellStyle.getAttributeValue(CellStyleAttributes.FONT).getFontData()[0];
		Assert.assertEquals(actualFontData.getName(), expectedFontData.getName());
		Assert.assertEquals(actualFontData.getHeight(), expectedFontData.getHeight());
		Assert.assertEquals(actualFontData.getStyle(), expectedFontData.getStyle());

		// Draw font
		cellPainter.setupGCFromConfig(gc, cellStyle);
		final FontData exepectedDrawnFontData = gc.getFont().getFontData()[0];
		Assert.assertEquals(actualFontData.getName(), exepectedDrawnFontData.getName());
		Assert.assertEquals(actualFontData.getHeight(), exepectedDrawnFontData.getHeight());
		Assert.assertEquals(actualFontData.getStyle(), exepectedDrawnFontData.getStyle());
	}

	
	
	@Test
	public void testLineWrap() {
		String testString1 = "Hello Mister,\nhow are you?\nI'm fine!";
		String testString2 = "Hello Mister,\rhow are you?\rI'm fine!";
		String testString3 = "Hello Mister,\n\rhow are you?\n\rI'm fine!";
		String testString4 = "Hello Mister,\r\nhow are you?\r\nI'm fine!";
		
		Assert.assertEquals(3, cellPainter.getNumberOfNewLines(testString1));
		Assert.assertEquals(3, cellPainter.getNumberOfNewLines(testString2));
		Assert.assertEquals(3, cellPainter.getNumberOfNewLines(testString3));
		Assert.assertEquals(3, cellPainter.getNumberOfNewLines(testString4));
	}
}
