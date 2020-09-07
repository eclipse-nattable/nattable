/*******************************************************************************
 * Copyright (c) 2018, 2020 Original authors and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class PercentageDisplayConverterTest {

    @Test
    public void testPercentageDisplayConverterWithNumberTypes() {
        PercentageDisplayConverter percentageDisplayConverter = new PercentageDisplayConverter();

        Object displayValue = percentageDisplayConverter.canonicalToDisplayValue(Float.valueOf(1.00f));
        assertDisplayValue(displayValue);

        displayValue = percentageDisplayConverter.canonicalToDisplayValue(Double.valueOf(1.00));
        assertDisplayValue(displayValue);

        displayValue = percentageDisplayConverter.canonicalToDisplayValue(Long.valueOf(1));
        assertDisplayValue(displayValue);

        displayValue = percentageDisplayConverter.canonicalToDisplayValue(Integer.valueOf(1));
        assertDisplayValue(displayValue);

        displayValue = percentageDisplayConverter.canonicalToDisplayValue(new AtomicInteger(1));
        assertDisplayValue(displayValue);

        displayValue = percentageDisplayConverter.canonicalToDisplayValue(Short.valueOf((short) 1));
        assertDisplayValue(displayValue);
    }

    private void assertDisplayValue(Object displayValue) {
        assertNotNull(displayValue);
        assertTrue(displayValue instanceof String);
        assertEquals("100%", displayValue);
    }

}
