/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.convert;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.Test;

public class DefaultIntegerDisplayConverterTest {

    private DefaultIntegerDisplayConverter intConverter = new DefaultIntegerDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("123", this.intConverter.canonicalToDisplayValue(Integer.valueOf("123")));
    }

    @Test
    public void testNullDataToDisplay() {
        assertEquals(null, this.intConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(Integer.valueOf("123"), this.intConverter.displayToCanonicalValue("123"));
    }

    @Test
    public void testNullDisplayToData() {
        assertEquals(null, this.intConverter.displayToCanonicalValue(""));
    }

    @Test(expected = ConversionFailedException.class)
    public void testConversionException() {
        this.intConverter.displayToCanonicalValue("abc");
    }

    @Test(expected = ConversionFailedException.class)
    public void testConversionExceptionTooBig() {
        this.intConverter.setNumberFormat(null);
        this.intConverter.displayToCanonicalValue(Long.valueOf(Integer.MAX_VALUE) + 1);
    }

    @Test
    public void testConvertLocalized() {
        this.intConverter.setNumberFormat(NumberFormat.getInstance(Locale.ENGLISH));
        assertEquals(Integer.valueOf("1234"), this.intConverter.displayToCanonicalValue("1,234"));
        assertEquals("1,234", this.intConverter.canonicalToDisplayValue(Integer.valueOf("1234")));
    }

    @Test(expected = ConversionFailedException.class)
    public void testFailConvertLocalized() {
        this.intConverter.setNumberFormat(null);
        assertEquals(Integer.valueOf("1234"), this.intConverter.displayToCanonicalValue("1,234"));
    }

    @Test
    public void testConvertNonLocalized() {
        this.intConverter.setNumberFormat(null);
        assertEquals("1234", this.intConverter.canonicalToDisplayValue(Integer.valueOf("1234")));
    }
}
