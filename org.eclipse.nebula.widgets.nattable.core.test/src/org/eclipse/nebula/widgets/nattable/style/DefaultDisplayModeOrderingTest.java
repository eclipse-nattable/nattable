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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.style.DefaultDisplayModeOrdering;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultDisplayModeOrderingTest {

	private DefaultDisplayModeOrdering ordering;

	@Before
	public void setup() {
		ordering = new DefaultDisplayModeOrdering();
	}
	
	@Test
	public void orderingForSelectMode() throws Exception {
		List<String> selectModeOrdering = ordering.getDisplayModeOrdering(DisplayMode.SELECT);
		
		Assert.assertEquals(2, selectModeOrdering.size());
		Assert.assertEquals(DisplayMode.SELECT, selectModeOrdering.get(0));
		Assert.assertEquals(DisplayMode.NORMAL, selectModeOrdering.get(1));
	}

	@Test
	public void orderingForEditMode() throws Exception {
		List<String> editModeOrdering = ordering.getDisplayModeOrdering(DisplayMode.EDIT);
		
		Assert.assertEquals(2, editModeOrdering.size());
		Assert.assertEquals(DisplayMode.EDIT, editModeOrdering.get(0));
		Assert.assertEquals(DisplayMode.NORMAL, editModeOrdering.get(1));
	}

	@Test
	public void orderingForNormalMode() throws Exception {
		List<String> selectModeOrdering = ordering.getDisplayModeOrdering(DisplayMode.NORMAL);
		
		Assert.assertEquals(1, selectModeOrdering.size());
		Assert.assertEquals(DisplayMode.NORMAL, selectModeOrdering.get(0));
	}
}
