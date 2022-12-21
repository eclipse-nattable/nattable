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

import org.junit.jupiter.api.Test;

public class DefaultByteDisplayConverterTest {

    private DefaultByteDisplayConverter byteConverter = new DefaultByteDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("123", this.byteConverter.canonicalToDisplayValue(Byte.valueOf("123")));
    }

    @Test
    public void testNullDataToDisplay() {
        assertEquals(null, this.byteConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(Byte.valueOf("123"), this.byteConverter.displayToCanonicalValue("123"));
    }

    @Test
    public void testNullDisplayToData() {
        assertNull(this.byteConverter.displayToCanonicalValue(""));
    }

    @Test
    public void testConversionException() {
        assertThrows(ConversionFailedException.class, () -> this.byteConverter.displayToCanonicalValue("abc"));
    }

    @Test
    public void testConversionExceptionTooBig() {
        assertThrows(ConversionFailedException.class, () -> this.byteConverter.displayToCanonicalValue("129"));
    }
}
