/*******************************************************************************
 * Copyright (c) 2016, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.junit.jupiter.api.Test;

public class AutomaticSpanningDataProviderTest {

    AutomaticSpanningDataProvider spanning = new AutomaticSpanningDataProvider(new DummyBodyDataProvider(10, 10), false, false);

    @Test
    public void testValuesNotEqual() {
        assertTrue(this.spanning.valuesNotEqual(Integer.valueOf(5), Integer.valueOf(7)));
        assertTrue(this.spanning.valuesNotEqual("Flanders", "Simpson"));
    }

    @Test
    public void testValuesEqual() {
        assertFalse(this.spanning.valuesNotEqual(Integer.valueOf(5), Integer.valueOf(5)));
        assertFalse(this.spanning.valuesNotEqual("Simpson", "Simpson"));
    }

    @Test
    public void testNullValuesNotEqual() {
        assertTrue(this.spanning.valuesNotEqual("Simpson", null));
        assertTrue(this.spanning.valuesNotEqual(null, "Simpson"));
    }

    @Test
    public void testBothValueNull() {
        assertFalse(this.spanning.valuesNotEqual(null, null));
    }
}
