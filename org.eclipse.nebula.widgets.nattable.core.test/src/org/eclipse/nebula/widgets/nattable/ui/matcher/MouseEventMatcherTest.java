/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MouseEventMatcherTest {

    @Test
    public void testDefaultEquals() {
        IMouseEventMatcher matcher1 = new MouseEventMatcher();
        IMouseEventMatcher matcher2 = new MouseEventMatcher();
        assertEquals(matcher1, matcher2);
    }

    @Test
    public void testFullConstructorEquals() {
        IMouseEventMatcher matcher1 = new MouseEventMatcher(5, "Test_Region", 1);
        IMouseEventMatcher matcher2 = new MouseEventMatcher(5, "Test_Region", 1);
        assertEquals(matcher1, matcher2);
    }

    @Test
    public void testNotEqual() {
        IMouseEventMatcher matcher = new MouseEventMatcher(5, "Test_Region", 1);

        assertFalse(matcher.equals(new MouseEventMatcher(4,
                "Test_Region", 1)));
        assertFalse(matcher.equals(new MouseEventMatcher(5, "X_Region",
                1)));
        assertFalse(matcher.equals(new MouseEventMatcher(5,
                "Test_Region", 2)));

        assertFalse(matcher.equals(new MouseEventMatcher(4, "X_Region",
                2)));
    }

    @Test
    public void testMap() {
        Map<IMouseEventMatcher, String> map = new HashMap<IMouseEventMatcher, String>();
        map.put(new MouseEventMatcher(), "ABC");
        assertEquals(1, map.size());
        map.remove(new MouseEventMatcher());
        assertEquals(0, map.size());
    }

}
