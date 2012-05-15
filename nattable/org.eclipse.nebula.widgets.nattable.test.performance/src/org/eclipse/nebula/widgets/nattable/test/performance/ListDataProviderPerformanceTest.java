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
package org.eclipse.nebula.widgets.nattable.test.performance;

import java.util.List;
import java.util.Map;


import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.junit.Assert;
import org.junit.Test;

import ca.odell.glazedlists.GlazedLists;

public class ListDataProviderPerformanceTest extends AbstractLayerPerformanceTest {

	// Bench marked ~ 65 milliseconds. Intel 2GHZ, 2GB Ram
	@Test
	public void performanceOfListDataProvider() throws Exception {
		List<RowDataFixture> largeList = RowDataListFixture.getList(26000);
		Assert.assertTrue(largeList.size() > 25000);
		
		layer = new DefaultGridLayer(
					largeList, 
					RowDataListFixture.getPropertyNames(),
					RowDataListFixture.getPropertyToLabelMap());
	}

	// Bench marked ~ 45 milliseconds. Intel 2GHZ, 2GB Ram
	@Test
	public void performanceOfGlazedListDataProvider() throws Exception {
		String[] propertyNames = RowDataListFixture.getPropertyNames();
		Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();
		List<RowDataFixture> largeList = RowDataListFixture.getList(26000);

		Assert.assertTrue(largeList.size() > 25000);
		
		IDataProvider glazedListsDataProvider =	new GlazedListsDataProvider<RowDataFixture>(
													GlazedLists.eventList(largeList), 
													new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames));
		
		IDataProvider columnHeaderDataProvider = 
			new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);

		layer = new DefaultGridLayer(glazedListsDataProvider, columnHeaderDataProvider);
	}
}
