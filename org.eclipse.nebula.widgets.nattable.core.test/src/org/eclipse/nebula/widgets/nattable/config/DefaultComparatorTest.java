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
package org.eclipse.nebula.widgets.nattable.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DefaultComparatorTest {

    private DefaultComparator defaultComparator = DefaultComparator.getInstance();

    @Test
    public void testCompareNonNullComparables() {
        assertEquals(0, this.defaultComparator.compare("abc", "abc"));
        assertEquals("abc".compareTo("def"), this.defaultComparator.compare("abc", "def"));
        assertEquals("def".compareTo("abc"), this.defaultComparator.compare("def", "abc"));
    }

    @Test
    public void testCompareNullAB() {
        assertEquals(0, this.defaultComparator.compare(null, null));
    }

    @Test
    public void testCompareNullA() {
        assertEquals(-1, this.defaultComparator.compare(null, "abc"));
    }

    @Test
    public void testCompareNullB() {
        assertEquals(1, this.defaultComparator.compare("abc", null));
    }

    @Test
    public void testCompareNonComparables() {
        assertEquals(0, this.defaultComparator.compare(new SimpleObject("Test"), new SimpleObject("Test")));
    }

    @Test
    public void testCompareNonComparables1() {
        assertEquals(-1, this.defaultComparator.compare(new SimpleObject("Test1"), new SimpleObject("Test2")));
    }

    @Test
    public void testCompareNonComparables2() {
        assertEquals(1, this.defaultComparator.compare(new SimpleObject("Test2"), new SimpleObject("Test1")));
    }

    class SimpleObject {
        final String value;

        public SimpleObject(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value.toString();
        }
    }
}
