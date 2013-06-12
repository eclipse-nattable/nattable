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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import static org.eclipse.nebula.widgets.nattable.test.fixture.ColumnCategoriesModelFixture.CATEGORY_B1_LABEL;
import static org.junit.Assert.assertEquals;

import java.util.List;


import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node.Type;
import org.eclipse.nebula.widgets.nattable.columnCategories.gui.ColumnCategoriesLabelProvider;
import org.eclipse.nebula.widgets.nattable.columnChooser.ColumnEntry;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnEntriesFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnCategoriesLabelProviderTest {

	private List<ColumnEntry> hiddenEntries;
	private ColumnCategoriesLabelProvider labelProvider;

	@Before
	public void setup() {
		hiddenEntries = ColumnEntriesFixture.getEntriesWithEvenIndexes();
		labelProvider = new ColumnCategoriesLabelProvider(hiddenEntries);
	}
	
	@Test
	public void shouldReturnLabelForCategoriesFromTheModel() throws Exception {
		assertEquals(CATEGORY_B1_LABEL, labelProvider.getText(new Node(CATEGORY_B1_LABEL, Type.CATEGORY)));
		assertEquals(Messages.getString("Unknown"), labelProvider.getText(new Node("2")));
	}
	
	@Test
	public void shouldReturnLabelsFromIndexesFromTheColumnEntry() throws Exception {
		assertEquals("Index2", labelProvider.getText(new Node("2", Type.COLUMN)));
		assertEquals("11", labelProvider.getText(new Node("11", Type.COLUMN)));
	}

}
