/*******************************************************************************
 * Copyright (c) 2018 Original authors and others.
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
