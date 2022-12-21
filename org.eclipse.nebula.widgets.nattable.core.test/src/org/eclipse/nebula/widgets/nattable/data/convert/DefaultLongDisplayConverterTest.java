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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.NumberFormat;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class DefaultLongDisplayConverterTest {

    private DefaultLongDisplayConverter longConverter = new DefaultLongDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("123", this.longConverter.canonicalToDisplayValue(Long.valueOf("123")));
    }

    @Test
    public void testNullDataToDisplay() {
        assertEquals(null, this.longConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(Long.valueOf("123"), this.longConverter.displayToCanonicalValue("123"));
    }

    @Test
    public void testNullDisplayToData() {
        assertEquals(null, this.longConverter.displayToCanonicalValue(""));
    }

    @Test
    public void testConversionException() {
        assertThrows(ConversionFailedException.class, () -> this.longConverter.displayToCanonicalValue("abc"));
    }

    @Test
    public void testConvertLocalized() {
        this.longConverter.setNumberFormat(NumberFormat.getInstance(Locale.ENGLISH));
        assertEquals(Long.valueOf("1234"), this.longConverter.displayToCanonicalValue("1,234"));
        assertEquals("1,234", this.longConverter.canonicalToDisplayValue(Long.valueOf("1234")));
    }

    @Test
    public void testFailConvertLocalized() {
        this.longConverter.setNumberFormat(null);
        assertThrows(ConversionFailedException.class, () -> this.longConverter.displayToCanonicalValue("1,234"));
    }

    @Test
    public void testConvertNonLocalized() {
        this.longConverter.setNumberFormat(null);
        assertEquals("1234", this.longConverter.canonicalToDisplayValue(Long.valueOf("1234")));
    }
}
