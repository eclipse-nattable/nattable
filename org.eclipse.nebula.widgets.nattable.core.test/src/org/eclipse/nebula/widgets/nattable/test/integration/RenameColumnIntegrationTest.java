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
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHeaderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.Test;

public class RenameColumnIntegrationTest {

	private static final String TEST_COLUMN_NAME = "Test column name";

	@Test
	public void shouldRenameColumnHeader() throws Exception {
		NatTable natTableFixture = new NatTableFixture();

		String originalColumnHeader = natTableFixture.getDataValueByPosition(2, 0).toString();
		assertEquals("Column 2", originalColumnHeader);

		natTableFixture.doCommand(new RenameColumnHeaderCommand(natTableFixture, 2, TEST_COLUMN_NAME));
		String renamedColumnHeader = natTableFixture.getDataValueByPosition(2, 0).toString();
		assertEquals(TEST_COLUMN_NAME, renamedColumnHeader);
	}

	@Test
	public void shouldRenameColumnHeaderForReorderedColumn() throws Exception {
		NatTable natTableFixture = new NatTableFixture();

		String originalColumnHeader = natTableFixture.getDataValueByPosition(2, 0).toString();
		assertEquals("Column 2", originalColumnHeader);

		natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 1, 5));

		originalColumnHeader = natTableFixture.getDataValueByPosition(2, 0).toString();
		assertEquals("Column 3", originalColumnHeader);

		natTableFixture.doCommand(new RenameColumnHeaderCommand(natTableFixture, 2, TEST_COLUMN_NAME));
		String renamedColumnHeader = natTableFixture.getDataValueByPosition(2, 0).toString();
		assertEquals(TEST_COLUMN_NAME, renamedColumnHeader);

	}
}
