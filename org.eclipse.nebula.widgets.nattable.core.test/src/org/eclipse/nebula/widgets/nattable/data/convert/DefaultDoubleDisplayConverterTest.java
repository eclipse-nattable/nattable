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
package org.eclipse.nebula.widgets.nattable.data.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultDoubleDisplayConverterTest {

    private DefaultDoubleDisplayConverter doubleConverter = new DefaultDoubleDisplayConverter();

    private static Locale defaultLocale;

    @BeforeClass
    public static void setup() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
    }

    @AfterClass
    public static void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("123.0", this.doubleConverter.canonicalToDisplayValue(Double.valueOf("123")));
        assertEquals("23.5", this.doubleConverter.canonicalToDisplayValue(Double.valueOf("23.5")));
    }

    @Test
    public void testNullDataToDisplay() {
        assertNull(this.doubleConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(Double.valueOf("123"), this.doubleConverter.displayToCanonicalValue("123"));
        assertEquals(Double.valueOf("23.5"), this.doubleConverter.displayToCanonicalValue("23.5"));
    }

    @Test
    public void testNullDisplayToData() {
        assertNull(this.doubleConverter.displayToCanonicalValue(""));
    }

    @Test(expected = ConversionFailedException.class)
    public void testConversionException() {
        this.doubleConverter.displayToCanonicalValue("abc");
    }

    @Test
    public void testLocalizedDisplayConversion() {
        NumberFormat original = this.doubleConverter.getNumberFormat();
        NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
        localized.setMinimumFractionDigits(1);
        localized.setMaximumFractionDigits(2);

        this.doubleConverter.setNumberFormat(localized);
        assertEquals("123,0", this.doubleConverter.canonicalToDisplayValue(Double.valueOf("123")));

        this.doubleConverter.setNumberFormat(original);
    }

    @Test
    public void testLocalizedCanonicalConversion() {
        NumberFormat original = this.doubleConverter.getNumberFormat();
        NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
        localized.setMinimumFractionDigits(1);
        localized.setMaximumFractionDigits(2);

        this.doubleConverter.setNumberFormat(localized);
        Object result = this.doubleConverter.displayToCanonicalValue("123,5");
        assertTrue(result instanceof Double);
        assertEquals(123.5, result);

        this.doubleConverter.setNumberFormat(original);
    }

    @Test
    public void testConvertLocalized() {
        this.doubleConverter.setNumberFormat(NumberFormat.getInstance(Locale.ENGLISH));
        assertEquals(Double.valueOf("1234.50"), this.doubleConverter.displayToCanonicalValue("1,234.50"));
        assertEquals("1,234.5", this.doubleConverter.canonicalToDisplayValue(Double.valueOf("1234.50")));
    }

    @Test(expected = ConversionFailedException.class)
    public void testFailConvertLocalized() {
        this.doubleConverter.setNumberFormat(null);
        assertEquals(Double.valueOf("1234.50"), this.doubleConverter.displayToCanonicalValue("1,234.50"));
    }

    @Test
    public void testConvertNonLocalized() {
        this.doubleConverter.setNumberFormat(null);
        assertEquals("1234.5", this.doubleConverter.canonicalToDisplayValue(Double.valueOf("1234.50")));
    }
}
