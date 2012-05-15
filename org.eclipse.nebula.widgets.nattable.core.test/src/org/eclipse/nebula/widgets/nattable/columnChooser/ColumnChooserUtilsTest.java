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
package org.eclipse.nebula.widgets.nattable.columnChooser;

import static org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils.getColumnLabel;
import static org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHeaderLayerFixture.getDataLayer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnChooserUtils;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnEntriesFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHeaderLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnChooserUtilsTest {

	private List<ColumnEntry> entriesFixture;
	@Before
	public void setup() {
		entriesFixture = ColumnEntriesFixture.getEntriesWithOddIndexes();
	}
	@Test
	public void find() throws Exception {
		ColumnEntry found = ColumnChooserUtils.find(entriesFixture, 5);
		assertEquals("Index5", found.getLabel());
	}

	@Test
	public void getPositionsFromEntries() throws Exception {
		List<Integer> positions = ColumnChooserUtils.getColumnEntryPositions(entriesFixture);
		assertEquals("[2, 6, 3, 4, 5]", positions.toString());
	}

	@Test
	public void getIndexesFromEntries() throws Exception {
		List<Integer> indexes = ColumnChooserUtils.getColumnEntryIndexes(entriesFixture);
		assertEquals("[1, 3, 5, 7, 9]", indexes.toString());
	}

	@Test
	public void listContainsEntry() throws Exception {
		assertTrue(ColumnChooserUtils.containsIndex(entriesFixture, 9));
		assertFalse(ColumnChooserUtils.containsIndex(entriesFixture, -9));
	}

	@Test
	public void shouldProvideRenamedLabelsIfTheColumnHasBeenRenamed() throws Exception {
		ColumnHeaderLayerFixture columnHeaderLayer = new ColumnHeaderLayerFixture();
		assertEquals("[1, 0]", getColumnLabel(columnHeaderLayer, getDataLayer(), 1));

		columnHeaderLayer.renameColumnPosition(1, "renamed");
		assertEquals("renamed*", getColumnLabel(columnHeaderLayer, getDataLayer(), 1));
	}

	@Test
	public void getVisibleColumnEntries() throws Exception {
		DefaultGridLayer gridLayer = new DefaultGridLayer(
											RowDataListFixture.getList(),
											RowDataListFixture.getPropertyNames(),
											RowDataListFixture.getPropertyToLabelMap());
		ColumnHideShowLayer columnHideShowLayer = gridLayer.getBodyLayer().getColumnHideShowLayer();
		ColumnHeaderLayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
		DataLayer columnHeaderDataLayer = (DataLayer) gridLayer.getColumnHeaderDataLayer();

		List<ColumnEntry> visibleEntries = ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);

		// All columns shown
		assertEquals(RowDataListFixture.getPropertyNames().length, visibleEntries.size());

		// Hide a few columns
		gridLayer.getBodyLayer().getColumnHideShowLayer().hideColumnPositions(Arrays.asList(1, 2, 3));
		visibleEntries = ColumnChooserUtils.getVisibleColumnsEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		assertEquals(RowDataListFixture.getPropertyNames().length - 3, visibleEntries.size());

		// Check the hidden entries
		List<ColumnEntry> hiddenEntries = ColumnChooserUtils.getHiddenColumnEntries(columnHideShowLayer, columnHeaderLayer, columnHeaderDataLayer);
		assertEquals(3, hiddenEntries.size());
	}
}
