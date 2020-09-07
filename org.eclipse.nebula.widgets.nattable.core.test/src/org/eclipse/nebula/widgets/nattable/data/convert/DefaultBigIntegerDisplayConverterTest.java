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

import java.math.BigInteger;

import org.junit.Test;

public class DefaultBigIntegerDisplayConverterTest {

    private DefaultBigIntegerDisplayConverter bigIntConverter = new DefaultBigIntegerDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("123", this.bigIntConverter.canonicalToDisplayValue(new BigInteger("123")));
    }

    @Test
    public void testNullDataToDisplay() {
        assertNull(this.bigIntConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(new BigInteger("123"), this.bigIntConverter.displayToCanonicalValue("123"));
    }

    @Test
    public void testNullDisplayToData() {
        assertNull(this.bigIntConverter.displayToCanonicalValue(""));
    }

    @Test(expected = ConversionFailedException.class)
    public void testConversionException() {
        this.bigIntConverter.displayToCanonicalValue("abc");
    }
}
