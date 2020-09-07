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

import org.junit.Test;

public class DefaultDisplayConverterTest {

    private DefaultDisplayConverter defaultDisplayTypeConverter = new DefaultDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("abc", this.defaultDisplayTypeConverter.canonicalToDisplayValue("abc"));
    }

    @Test
    public void testNullDataToDisplay() {
        assertEquals("", this.defaultDisplayTypeConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals("abc", this.defaultDisplayTypeConverter.displayToCanonicalValue("abc"));
    }

    @Test
    public void testNullDisplayToData() {
        assertEquals(null, this.defaultDisplayTypeConverter.displayToCanonicalValue(""));
    }

}
