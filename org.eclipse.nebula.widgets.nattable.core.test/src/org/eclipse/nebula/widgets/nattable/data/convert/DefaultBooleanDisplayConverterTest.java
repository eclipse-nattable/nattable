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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class DefaultBooleanDisplayConverterTest {

    private DefaultBooleanDisplayConverter booleanConverter = new DefaultBooleanDisplayConverter();

    @Test
    public void testNonNullDataToDisplay() {
        assertEquals("true", this.booleanConverter.canonicalToDisplayValue(Boolean.TRUE));
        assertEquals("false", this.booleanConverter.canonicalToDisplayValue(Boolean.FALSE));
    }

    @Test
    public void testNullDataToDisplay() {
        assertNull(this.booleanConverter.canonicalToDisplayValue(null));
    }

    @Test
    public void testNonNullDisplayToData() {
        assertEquals(Boolean.TRUE, this.booleanConverter.displayToCanonicalValue("true"));
        assertEquals(Boolean.FALSE, this.booleanConverter.displayToCanonicalValue("false"));
        assertEquals(Boolean.FALSE, this.booleanConverter.displayToCanonicalValue("123"));
    }

    @Test
    public void testNullDisplayToData() {
        assertEquals(Boolean.FALSE, this.booleanConverter.displayToCanonicalValue(""));
    }

}
