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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultBigDecimalDisplayConverterTest {

    private DefaultBigDecimalDisplayConverter bigDecConverter = new DefaultBigDecimalDisplayConverter();

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
        assertEquals("123", this.bigDecConverter.canonicalToDisplayValue(BigDecimal.valueOf(123)));
        assertEquals("23.5", this.bigDecConverter.canonicalToDisplayValue(BigDecimal.valueOf(23.5)));
    }

    @Test
    public void testNullDataToDisplay() {
        assertNull(this.bigDecConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(BigDecimal.valueOf(123), this.bigDecConverter.displayToCanonicalValue("123"));
        assertEquals(BigDecimal.valueOf(23.5), this.bigDecConverter.displayToCanonicalValue("23.5"));
    }

    @Test
    public void testNullDisplayToData() {
        assertNull(this.bigDecConverter.displayToCanonicalValue(""));
    }

    @Test(expected = ConversionFailedException.class)
    public void testConversionException() {
        this.bigDecConverter.displayToCanonicalValue("abc");
    }

    @Test
    public void testLocalizedDisplayConversion() {
        NumberFormat original = this.bigDecConverter.getNumberFormat();
        NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
        localized.setMinimumFractionDigits(0);
        localized.setMaximumFractionDigits(2);

        this.bigDecConverter.setNumberFormat(localized);
        assertEquals("123,5", this.bigDecConverter.canonicalToDisplayValue(new BigDecimal("123.5")));

        this.bigDecConverter.setNumberFormat(original);
    }

    @Test
    public void testLocalizedCanonicalConversion() {
        NumberFormat original = this.bigDecConverter.getNumberFormat();
        NumberFormat localized = NumberFormat.getInstance(Locale.GERMAN);
        localized.setMinimumFractionDigits(0);
        localized.setMaximumFractionDigits(2);
        ((DecimalFormat) localized).setParseBigDecimal(true);

        this.bigDecConverter.setNumberFormat(localized);
        Object result = this.bigDecConverter.displayToCanonicalValue("123,5");
        assertTrue(result instanceof BigDecimal);
        assertEquals(new BigDecimal("123.5"), result);

        this.bigDecConverter.setNumberFormat(original);
    }
}
