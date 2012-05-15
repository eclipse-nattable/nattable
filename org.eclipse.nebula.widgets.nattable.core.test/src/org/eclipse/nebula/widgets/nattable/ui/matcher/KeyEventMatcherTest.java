/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.matcher;

import java.util.HashMap;
import java.util.Map;


import org.eclipse.nebula.widgets.nattable.ui.matcher.IKeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.junit.Assert;
import org.junit.Test;

public class KeyEventMatcherTest {
	
	@Test
	public void testEquals() {
		IKeyEventMatcher matcher1 = new KeyEventMatcher(12, 101);
		IKeyEventMatcher matcher2 = new KeyEventMatcher(12, 101);
		Assert.assertEquals(matcher1, matcher2);
	}
	
	@Test
	public void testNotEqual() {
		IKeyEventMatcher matcher = new KeyEventMatcher(12, 101);

		Assert.assertFalse(matcher.equals(new KeyEventMatcher(11, 101)));
		Assert.assertFalse(matcher.equals(new KeyEventMatcher(12, 102)));
		
		Assert.assertFalse(matcher.equals(new KeyEventMatcher(11, 102)));
	}
	
	@Test
	public void testMap() {
		Map<IKeyEventMatcher, String> map = new HashMap<IKeyEventMatcher, String>();
		map.put(new KeyEventMatcher(12, 101), "ABC");
		Assert.assertEquals(1, map.size());
		map.remove(new KeyEventMatcher(12, 101));
		Assert.assertEquals(0, map.size());
	}
	
}
