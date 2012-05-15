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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Properties;


import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortStatePersistor;
import org.eclipse.nebula.widgets.nattable.sort.SortStatePersistor.SortState;
import org.eclipse.nebula.widgets.nattable.test.fixture.SortModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class SortStatePersistorTest {

	private static final String TEST_PREFIX = "test_prefix";
	private static final String KEY = TEST_PREFIX + SortStatePersistor.PERSISTENCE_KEY_SORTING_STATE;
	private Properties properties;
	private SortModelFixture sortModel;
	private SortHeaderLayer<RowDataFixture> sortHeaderLayer;
	private SortStatePersistor<RowDataFixture> sortStatePersistor;

	@Before
	public void setup(){
		properties = new Properties();
		sortModel = new SortModelFixture();
		sortHeaderLayer = new SortHeaderLayer<RowDataFixture>(
				new DataLayerFixture(10, 20, 100, 20), sortModel);
		sortStatePersistor = new SortStatePersistor<RowDataFixture>(sortModel);
	}

	@Test
	public void shouldSaveSortedColumnsAndSortOrder() throws Exception {
		sortStatePersistor.saveState(TEST_PREFIX, properties);
		String savedProperty = properties.getProperty(KEY);
		assertEquals("0:DESC:3|5:DESC:1|6:ASC:0|3:ASC:2|", savedProperty);
	}

	@Test
	public void shouldNotSaveIfNothingIsSorted() throws Exception {
		sortModel = SortModelFixture.getEmptyModel();
		sortHeaderLayer = new SortHeaderLayer<RowDataFixture>(
				new DataLayerFixture(10, 20, 100, 20), sortModel);
		sortStatePersistor = new SortStatePersistor<RowDataFixture>(sortModel);

		sortStatePersistor.saveState(TEST_PREFIX, properties);
		assertNull(properties.getProperty(KEY));
	}
	
	/**
	 * Save string format:
	 * 	column index : sort direction : sort order |
	 */
	@Test
	public void loadStateFromProperties() throws Exception {
		properties.put(KEY, "0:DESC:3|3:ASC:2|5:DESC:1|6:ASC:0|");
		sortModel = SortModelFixture.getEmptyModel();
		sortHeaderLayer = new SortHeaderLayer<RowDataFixture>(
							new DataLayerFixture(10, 20, 100, 20), 
							sortModel);
		sortStatePersistor = new SortStatePersistor<RowDataFixture>(sortModel);
		sortStatePersistor.loadState(TEST_PREFIX, properties);
		ISortModel sortModel = sortHeaderLayer.getSortModel();

		// Sort direction
		assertEquals(SortDirectionEnum.DESC, sortModel.getSortDirection(0));
		assertEquals(SortDirectionEnum.ASC, sortModel.getSortDirection(3));
		assertEquals(SortDirectionEnum.DESC, sortModel.getSortDirection(5));
		assertEquals(SortDirectionEnum.ASC, sortModel.getSortDirection(6));

		// Sort order
		assertEquals(3, sortModel.getSortOrder(0));
		assertEquals(2, sortModel.getSortOrder(3));
		assertEquals(1, sortModel.getSortOrder(5));
		assertEquals(0, sortModel.getSortOrder(6));
		
		// No other columns should be flagged as sorted
		assertEquals(-1, sortModel.getSortOrder(4));
		assertFalse(sortModel.isColumnIndexSorted(4));
		assertEquals(SortDirectionEnum.NONE, sortModel.getSortDirection(4));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void shouldParseTheSavedStringCorrectly() throws Exception {
		SortState sortState = sortStatePersistor.getSortStateFromString("0:DESC:3");
		assertEquals(0, sortState.columnIndex);
		assertEquals(SortDirectionEnum.DESC, sortState.sortDirection);
		assertEquals(3, sortState.sortOrder);
	}
}
