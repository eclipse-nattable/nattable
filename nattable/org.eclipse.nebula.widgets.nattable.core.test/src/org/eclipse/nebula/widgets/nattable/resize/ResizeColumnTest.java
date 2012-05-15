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
package org.eclipse.nebula.widgets.nattable.resize;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.Test;

public class ResizeColumnTest {

	/**
	 * Test for bug NTBL-431
	 */
	@Test
	public void reiszeColumnInATableWithNoRows() throws Exception {
		NatTableFixture natTable = new NatTableFixture(new DummyGridLayerStack(5, 0), true);

		assertEquals(100, natTable.getColumnWidthByPosition(2));
		natTable.doCommand(new ColumnResizeCommand(natTable, 2, 150));

		assertEquals(150, natTable.getColumnWidthByPosition(2));
	}

}
