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
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class KeyEventMatcherTest {

    @Test
    public void testEquals() {
        IKeyEventMatcher matcher1 = new KeyEventMatcher(12, 101);
        IKeyEventMatcher matcher2 = new KeyEventMatcher(12, 101);
        assertEquals(matcher1, matcher2);
    }

    @Test
    public void testNotEqual() {
        IKeyEventMatcher matcher = new KeyEventMatcher(12, 101);

        assertFalse(matcher.equals(new KeyEventMatcher(11, 101)));
        assertFalse(matcher.equals(new KeyEventMatcher(12, 102)));

        assertFalse(matcher.equals(new KeyEventMatcher(11, 102)));
    }

    @Test
    public void testMap() {
        Map<IKeyEventMatcher, String> map = new HashMap<IKeyEventMatcher, String>();
        map.put(new KeyEventMatcher(12, 101), "ABC");
        assertEquals(1, map.size());
        map.remove(new KeyEventMatcher(12, 101));
        assertEquals(0, map.size());
    }

}
