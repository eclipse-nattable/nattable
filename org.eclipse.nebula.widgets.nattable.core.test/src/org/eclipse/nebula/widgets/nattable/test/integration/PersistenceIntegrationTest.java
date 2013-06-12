/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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
import static org.junit.Assert.assertFalse;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class PersistenceIntegrationTest {

	public static final String TEST_PERSISTENCE_PREFIX = "testPrefix";
	private Properties properties;
	private NatTableFixture natTableFixture;

	@Before
	public void setup() {
		natTableFixture = new NatTableFixture(new Shell(), 
				new DummyGridLayerStack() {
			
					@Override
					protected void init(IUniqueIndexLayer bodyDataLayer, IUniqueIndexLayer columnHeaderDataLayer, IUniqueIndexLayer rowHeaderDataLayer, IUniqueIndexLayer cornerDataLayer) {
						RowReorderLayer rowReorderLayer = new RowReorderLayer(bodyDataLayer);
						super.init(rowReorderLayer, columnHeaderDataLayer, rowHeaderDataLayer, cornerDataLayer);
					}

		});
		properties = new Properties();
	}

	@Test
	public void stateIsLoadedCorrectlyFromProperties() throws Exception {
		saveStateToPropeties();
		natTableFixture.loadState(TEST_PERSISTENCE_PREFIX, properties);

		// Originally resized position 2, after reorder became position 1
		assertEquals(200, natTableFixture.getColumnWidthByPosition(1));
		// Originally resized position 2, after reorder became position 1
		assertEquals(100, natTableFixture.getRowHeightByPosition(1));
		assertEquals(1, natTableFixture.getColumnIndexByPosition(1));
		assertEquals(0, natTableFixture.getColumnIndexByPosition(3));
	}

	public void saveStateToPropeties() throws Exception {
		// Resize column 2 to 200px
		assertEquals(100, natTableFixture.getColumnWidthByPosition(2));
		natTableFixture.doCommand(new ColumnResizeCommand(natTableFixture, 2, 200));
		assertEquals(200, natTableFixture.getColumnWidthByPosition(2));

		// Resize row 2 to 100px
		assertEquals(20, natTableFixture.getRowHeightByPosition(2));
		natTableFixture.doCommand(new RowResizeCommand(natTableFixture, 2, 100));
		assertEquals(100, natTableFixture.getRowHeightByPosition(2));

		// Reorder column 1 --> 5 (grid coordinates)
		// 0, 1, 2, 3, 4, 5,.. --> 1, 2, 3, 0, 4, 5,..
		assertEquals(0, natTableFixture.getColumnIndexByPosition(1));
		natTableFixture.doCommand(new ColumnReorderCommand(natTableFixture, 1, 5));
		assertEquals(1, natTableFixture.getColumnIndexByPosition(1));

		// Reorder row 1 --> 5 (grid coordinates)
		// 0, 1, 2, 3, 4, 5,.. --> 1, 2, 3, 0, 4, 5,..
		assertEquals(0, natTableFixture.getRowIndexByPosition(1));
		natTableFixture.doCommand(new RowReorderCommand(natTableFixture, 1, 5));
		assertEquals(1, natTableFixture.getRowIndexByPosition(1));

		// Hide column with index 3 (grid coordinates)
		assertEquals(3, natTableFixture.getColumnIndexByPosition(3));
		natTableFixture.doCommand(new ColumnHideCommand(natTableFixture, 3));
		assertEquals(0, natTableFixture.getColumnIndexByPosition(3));

		natTableFixture.saveState(TEST_PERSISTENCE_PREFIX, properties);

		// Ensure that properties got persisted
		assertEquals("true", properties.get("testPrefix.COLUMN_HEADER.columnWidth.resizableByDefault"));
		assertEquals("100", properties.get("testPrefix.COLUMN_HEADER.columnWidth.defaultSize"));
		assertEquals("true", properties.get("testPrefix.COLUMN_HEADER.rowHeight.resizableByDefault"));

		assertEquals("40", properties.get("testPrefix.ROW_HEADER.columnWidth.defaultSize"));
		assertEquals("true", properties.get("testPrefix.ROW_HEADER.rowHeight.resizableByDefault"));
		assertEquals("true", properties.get("testPrefix.ROW_HEADER.columnWidth.resizableByDefault"));
		assertEquals("40", properties.get("testPrefix.ROW_HEADER.rowHeight.defaultSize"));

		assertEquals("20", properties.get("testPrefix.CORNER.rowHeight.defaultSize"));
		assertEquals("true", properties.get("testPrefix.CORNER.columnWidth.resizableByDefault"));
		assertEquals("true", properties.get("testPrefix.CORNER.rowHeight.resizableByDefault"));

		assertEquals("20", properties.get("testPrefix.BODY.rowHeight.defaultSize"));
		assertEquals("true", properties.get("testPrefix.BODY.rowHeight.resizableByDefault"));
		assertEquals("true", properties.get("testPrefix.BODY.columnWidth.resizableByDefault"));
		assertEquals("1,2,3,0,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,", properties.get("testPrefix.BODY.columnIndexOrder"));
		assertEquals("1:100,", properties.get("testPrefix.BODY.rowHeight.sizes"));
		assertEquals("1:200,", properties.get("testPrefix.BODY.columnWidth.sizes"));
	}

	@Test
	public void tableShouldDefaultProperlyIfNoPropertiesAreLoaded() throws Exception {
		boolean exceptionOccured = false;
		try {
			natTableFixture.loadState(TEST_PERSISTENCE_PREFIX, properties);
		} catch (Exception e) {
			e.printStackTrace();
			exceptionOccured = true;
		}
		assertFalse(exceptionOccured);
	}
}
