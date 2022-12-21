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
package org.eclipse.nebula.widgets.nattable.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColorPersistorTest {

    private static final Color TEST_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_RED);
    Properties properties;

    @BeforeEach
    public void setup() {
        this.properties = new Properties();
    }

    @Test
    public void shouldSaveTheColorAsAString() throws Exception {
        ColorPersistor.saveColor("prefix", this.properties, TEST_COLOR);
        assertEquals("255,0,0", this.properties.getProperty("prefix.color"));
    }

    @Test
    public void shouldLoadColorFromSavedRGBString() throws Exception {
        this.properties.setProperty("prefix.color", "255, 0, 0");
        Color actual = ColorPersistor.loadColor("prefix", this.properties);
        assertEquals(TEST_COLOR, actual);
    }

    @Test
    public void shouldFailToLoadForMissingRGBString() throws Exception {
        Color actual = ColorPersistor.loadColor("missing", this.properties);
        assertNull(actual);
    }
}
