/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DefaultFloatDisplayConverterTest {

    private DefaultFloatDisplayConverter floatConverter = new DefaultFloatDisplayConverter();

    private static Locale defaultLocale;

    @BeforeAll
    public static void setup() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
    }

    @AfterAll
    public static void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("123.0", this.floatConverter.canonicalToDisplayValue(Float.valueOf("123")));
        assertEquals("23.5", this.floatConverter.canonicalToDisplayValue(Float.valueOf("23.5")));
    }

    @Test
    public void testNullDataToDisplay() {
        assertNull(this.floatConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(Float.valueOf("123"), this.floatConverter.displayToCanonicalValue("123"));
        assertEquals(Float.valueOf("23.5"), this.floatConverter.displayToCanonicalValue("23.5"));
    }

    @Test
    public void testNullDisplayToData() {
        assertNull(this.floatConverter.displayToCanonicalValue(""));
    }

    @Test
    public void testConversionException() {
        assertThrows(ConversionFailedException.class, () -> this.floatConverter.displayToCanonicalValue("abc"));
    }

    @Test
    public void testLocalizedDisplayConversion() {
        NumberFormat original = this.floatConverter.getNumberFormat();
        NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
        localized.setMinimumFractionDigits(1);
        localized.setMaximumFractionDigits(2);

        this.floatConverter.setNumberFormat(localized);
        assertEquals("123,0", this.floatConverter.canonicalToDisplayValue(Float.valueOf("123")));

        this.floatConverter.setNumberFormat(original);
    }

    @Test
    public void testLocalizedCanonicalConversion() {
        NumberFormat original = this.floatConverter.getNumberFormat();
        NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
        localized.setMinimumFractionDigits(1);
        localized.setMaximumFractionDigits(2);

        this.floatConverter.setNumberFormat(localized);
        Object result = this.floatConverter.displayToCanonicalValue("123,5");
        assertTrue(result instanceof Float);
        assertEquals(123.5f, result);

        this.floatConverter.setNumberFormat(original);
    }

    @Test
    public void testConvertLocalized() {
        this.floatConverter.setNumberFormat(NumberFormat.getInstance(Locale.ENGLISH));
        assertEquals(Float.valueOf("1234.50"), this.floatConverter.displayToCanonicalValue("1,234.50"));
        assertEquals("1,234.5", this.floatConverter.canonicalToDisplayValue(Float.valueOf("1234.50")));
    }

    @Test
    public void testFailConvertLocalized() {
        this.floatConverter.setNumberFormat(null);
        assertThrows(ConversionFailedException.class, () -> this.floatConverter.displayToCanonicalValue("1,234.50"));
    }

    @Test
    public void testConvertNonLocalized() {
        this.floatConverter.setNumberFormat(null);
        assertEquals("1234.5", this.floatConverter.canonicalToDisplayValue(Float.valueOf("1234.50")));
    }
}
