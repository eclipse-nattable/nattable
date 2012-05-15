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
package org.eclipse.nebula.widgets.nattable.grid.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;


import org.eclipse.nebula.widgets.nattable.columnRename.RenameColumnHelper;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHeaderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.PersistenceIntegrationTest;
import org.junit.Before;
import org.junit.Test;

public class RenameColumnHelperTest {

	private RenameColumnHelper helper;
	private Properties properties;

	private static String PREFIX = PersistenceIntegrationTest.TEST_PERSISTENCE_PREFIX;
	private static String KEY = RenameColumnHelper.PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS;

	@Before
	public void setup() {
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayerFixture();
		properties = new Properties();
		helper = new RenameColumnHelper(columnHeaderLayer);
	}

	@Test
	public void shouldRenameColumn() throws Exception {
		assertTrue(helper.renameColumnPosition(1, "one"));
		assertEquals("one", helper.getRenamedColumnLabel(1));

		// Invalid position
		assertFalse(helper.renameColumnPosition(-1, "badone"));
		assertEquals("one", helper.getRenamedColumnLabel(1));

		// new name is null
		assertTrue(helper.renameColumnPosition(1, null));
		assertEquals(null, helper.getRenamedColumnLabel(1));
	}

	@Test
	public void doNotSaveStateIfNoColumnAreRenamed() throws Exception {
		helper.saveState(PREFIX, properties);
		assertNull(properties.get(KEY));
	}

	@Test
	public void saveRenamedColumnInProperties() throws Exception {
		helper.renameColumnPosition(2, "Renamed 2");
		helper.saveState(PREFIX, properties);

		assertEquals("2:Renamed 2|", properties.get(PREFIX + KEY));

		helper.renameColumnPosition(1, "Renamed 1");
		helper.saveState(PREFIX, properties);

		assertEquals("1:Renamed 1|2:Renamed 2|", properties.get(PREFIX + KEY));
	}

	@Test
	public void loadStateWhenNoPrepertiesArePersisted() throws Exception {
		helper.loadState(PREFIX, properties);
		assertFalse(helper.isAnyColumnRenamed());
	}

	@Test
	public void loadStateFromProperties() throws Exception {
		properties.put(PREFIX + KEY, "2:Renamed 2|1:Renamed 1|");
		helper.loadState(PREFIX, properties);

		assertTrue(helper.isAnyColumnRenamed());
		assertEquals("Renamed 2", helper.getRenamedColumnLabel(2));
		assertEquals("Renamed 1", helper.getRenamedColumnLabel(1));
	}

}
