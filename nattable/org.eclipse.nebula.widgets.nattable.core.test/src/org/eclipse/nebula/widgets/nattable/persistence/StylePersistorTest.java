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
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.eclipse.nebula.widgets.nattable.persistence.IPersistable.DOT;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.BG_COLOR_PREFIX;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.BORDER_PREFIX;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.FG_COLOR_PREFIX;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.FONT_PREFIX;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.H_ALIGNMENT_PREFIX;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.STYLE_PERSISTENCE_PREFIX;
import static org.eclipse.nebula.widgets.nattable.persistence.StylePersistor.V_ALIGNMENT_PREFIX;
import static org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes.BACKGROUND_COLOR;
import static org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes.FOREGROUND_COLOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;


import org.eclipse.nebula.widgets.nattable.persistence.ColorPersistor;
import org.eclipse.nebula.widgets.nattable.persistence.StylePersistor;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.CellStyleFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.PropertiesFixture;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.junit.Before;
import org.junit.Test;

public class StylePersistorTest {
	private static final String TEST_PREFIX = "TEST_PREFIX";
	private PropertiesFixture propertiesFixture;

	@Before
	public void setup() {
		propertiesFixture = new PropertiesFixture().addStyleProperties(TEST_PREFIX);
	}

	@Test
	public void persistColorToProperties() throws Exception {
		StylePersistor.saveColor(TEST_PREFIX, propertiesFixture, CellStyleFixture.TEST_BG_COLOR);

		assertEquals("255,255,255", propertiesFixture.getProperty(TEST_PREFIX + DOT + ColorPersistor.STYLE_PERSISTENCE_PREFIX));
	}

	@Test
	public void persistStyleSettingsToProperties() throws Exception {
		Properties properties = new Properties();
		StylePersistor.saveStyle(TEST_PREFIX, properties, new CellStyleFixture());

		String expectedPrefix = TEST_PREFIX + DOT + STYLE_PERSISTENCE_PREFIX + DOT;

		assertEquals("255,255,255", properties.getProperty(expectedPrefix + BG_COLOR_PREFIX + DOT + ColorPersistor.STYLE_PERSISTENCE_PREFIX));
		assertEquals("0,0,0", properties.getProperty(expectedPrefix + FG_COLOR_PREFIX + DOT + ColorPersistor.STYLE_PERSISTENCE_PREFIX));

		assertEquals("LEFT", properties.getProperty(expectedPrefix + H_ALIGNMENT_PREFIX));
		assertEquals("MIDDLE", properties.getProperty(expectedPrefix + V_ALIGNMENT_PREFIX));

		assertEquals(CellStyleFixture.TEST_FONT.getFontData()[0].toString(), properties.getProperty(expectedPrefix + FONT_PREFIX));
		assertEquals(CellStyleFixture.TEST_BORDER_STYLE.toString(), properties.getProperty(expectedPrefix + BORDER_PREFIX));
	}

	@Test
	public void loadPersistedColorSettings() throws Exception {
		Style style = StylePersistor.loadStyle(TEST_PREFIX, propertiesFixture);

		Color fgColor = style.getAttributeValue(FOREGROUND_COLOR);
		assertEquals("RGB {100, 110, 120}", fgColor.getRGB().toString());

		Color bgColor = style.getAttributeValue(BACKGROUND_COLOR);
		assertEquals("RGB {200, 210, 220}", bgColor.getRGB().toString());
	}

	@Test
	public void loadPersistedAlignmentSettings() throws Exception {
		Style style = StylePersistor.loadStyle(TEST_PREFIX, propertiesFixture);

		HorizontalAlignmentEnum expecetdHAlign = style.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
		assertEquals(HorizontalAlignmentEnum.LEFT, expecetdHAlign);

		VerticalAlignmentEnum expecetdVAlign = style.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
		assertEquals(VerticalAlignmentEnum.TOP, expecetdVAlign);
	}

	@Test
	public void loadFontSettings() throws Exception {
		Style style = StylePersistor.loadStyle(TEST_PREFIX, propertiesFixture);

		Font font = style.getAttributeValue(CellStyleAttributes.FONT);
		assertTrue(font.getFontData()[0].toString().contains("|Tahoma|8.25|"));
	}

	@Test
	public void loadBorderStyleSettings() throws Exception {
		Style style = StylePersistor.loadStyle(TEST_PREFIX, propertiesFixture);

		BorderStyle borderStyle = style.getAttributeValue(CellStyleAttributes.BORDER_STYLE);
		assertEquals(2, borderStyle.getThickness());
		assertEquals(100, borderStyle.getColor().getRed());
		assertEquals(110, borderStyle.getColor().getGreen());
		assertEquals(120, borderStyle.getColor().getBlue());
		assertEquals(LineStyleEnum.DASHDOTDOT, borderStyle.getLineStyle());
	}
}
