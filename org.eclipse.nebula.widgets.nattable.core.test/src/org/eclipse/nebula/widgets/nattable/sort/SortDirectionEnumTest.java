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
package org.eclipse.nebula.widgets.nattable.sort;

import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.ASC;
import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.DESC;
import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.NONE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SortDirectionEnumTest {

	@Test
	public void shouldCalculateNextSortDirectionCorrectly() throws Exception {
		assertEquals(ASC, NONE.getNextSortDirection());
		assertEquals(DESC, ASC.getNextSortDirection());
		assertEquals(NONE, DESC.getNextSortDirection());
	}
}
