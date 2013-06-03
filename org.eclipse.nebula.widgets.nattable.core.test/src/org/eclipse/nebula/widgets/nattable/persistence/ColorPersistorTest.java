/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.persistence.ColorPersistor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

public class ColorPersistorTest {

	private static final Color TEST_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_RED);;
	Properties properties;

	@Before
	public void setup() {
		properties = new Properties();
	}

	@Test
	public void shouldSaveTheColorAsAString() throws Exception {
		ColorPersistor.saveColor("prefix", properties, TEST_COLOR);
		assertEquals("255,0,0", properties.getProperty("prefix.color"));
	}

	@Test
	public void shouldLoadColorFromSavedRGBString() throws Exception {
		properties.setProperty("prefix.color", "255, 0, 0");
		Color actual = ColorPersistor.loadColor("prefix", properties);
		assertEquals(TEST_COLOR, actual);
	}

	@Test
	public void shouldFailToLoadForMissingRGBString() throws Exception {
		Color actual = ColorPersistor.loadColor("missing", properties);
		assertNull(actual);
	}
}
