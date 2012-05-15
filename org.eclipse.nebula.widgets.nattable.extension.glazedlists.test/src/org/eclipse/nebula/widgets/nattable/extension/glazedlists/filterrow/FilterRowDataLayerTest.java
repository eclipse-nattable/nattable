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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;


import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;

public class FilterRowDataLayerTest {

	private DataLayerFixture columnHeaderLayer;
	private IConfigRegistry configRegistry;
	private FilterList<RowDataFixture> filterList;
	private FilterRowDataLayer<RowDataFixture> layerUnderTest;
	private LayerListenerFixture listener;

	@Before
	public void setup() {
		columnHeaderLayer = new DataLayerFixture(10, 2, 100, 50);

		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
		new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

		filterList = new FilterList<RowDataFixture>(GlazedLists.eventList(RowDataListFixture.getList()));
		
		CompositeMatcherEditor<RowDataFixture> autoFilterMatcherEditor = new CompositeMatcherEditor<RowDataFixture>();
		filterList.setMatcherEditor(autoFilterMatcherEditor);
		
		layerUnderTest = new FilterRowDataLayer<RowDataFixture>(
				new DefaultGlazedListsFilterStrategy<RowDataFixture>(
					autoFilterMatcherEditor,
					new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()),
					configRegistry
				),
				columnHeaderLayer, columnHeaderLayer.getDataProvider(), configRegistry);
		listener = new LayerListenerFixture();
		layerUnderTest.addLayerListener(listener);
	}

	@Test
	public void shouldHandleClearFilterCommand() throws Exception {
		Assert.assertEquals(13, filterList.size());

		layerUnderTest.doCommand(new UpdateDataCommand(layerUnderTest, 1, 0, "ford"));
		Assert.assertEquals(1, filterList.size());

		layerUnderTest.doCommand(new ClearFilterCommand(layerUnderTest, 1));
		Assert.assertEquals(13, filterList.size());

		listener.containsInstanceOf(RowStructuralRefreshEvent.class);
	}

	@Test
	public void shouldHandleTheClearAllFiltersCommand() throws Exception {
		Assert.assertEquals(13, filterList.size());

		layerUnderTest.doCommand(new UpdateDataCommand(layerUnderTest, 1, 0, "ford"));
		Assert.assertEquals(1, filterList.size());

		layerUnderTest.doCommand(new UpdateDataCommand(layerUnderTest, 0, 0, "XXX"));
		Assert.assertEquals(0, filterList.size());

		layerUnderTest.doCommand(new ClearAllFiltersCommand());
		Assert.assertEquals(13, filterList.size());

		listener.containsInstanceOf(RowStructuralRefreshEvent.class);
	}

	@Test
	public void shouldHandleTheToggeleFilterRowCommand() throws Exception {
		Assert.assertEquals(1, layerUnderTest.getRowCount());
		layerUnderTest.doCommand(new ToggleFilterRowCommand());
		//as the command is handled by the FilterRowHeaderComposite now, it should
		//have no effect to do the command on the FilterRowDataLayer
		Assert.assertEquals(1, layerUnderTest.getRowCount());
	}

}

