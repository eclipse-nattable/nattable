/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupby;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dirk Fauth
 *
 */
public class GroupByModelPersistenceTest {

	private GroupByModel model;
	
	@Before
	public void setup() {
		model = new GroupByModel();
	}
	
	@Test
	public void testSaveState() {
		model.addGroupByColumnIndex(5);
		model.addGroupByColumnIndex(3);
		model.addGroupByColumnIndex(7);
		
		Properties properties = new Properties();
		model.saveState("prefix", properties);
		
		assertEquals(1, properties.size());
		assertEquals("5,3,7,", properties.getProperty("prefix"+GroupByModel.PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES));
	}
	
	@Test
	public void testLoadState() {
		Properties properties = new Properties();
		properties.setProperty("prefix"+GroupByModel.PERSISTENCE_KEY_GROUP_BY_COLUMN_INDEXES, 
				"9,5,7,");
		
		model.loadState("prefix", properties);
		
		List<Integer> indexes = model.getGroupByColumnIndexes();
		assertEquals(9, indexes.get(0).intValue());
		assertEquals(5, indexes.get(1).intValue());
		assertEquals(7, indexes.get(2).intValue());
	}
}
