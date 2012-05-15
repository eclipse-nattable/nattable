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
package org.eclipse.nebula.widgets.nattable.examples.examples._150_Column_and_row_grouping;


import org.apache.commons.lang.ArrayUtils;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnCategories.ChooseColumnsFromCategoriesCommandHandler;
import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.GlazedListsGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnCategoriesModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.GlazedLists;

public class _010_Column_categories extends AbstractNatExample {

	private GlazedListsGridLayer<RowDataFixture> gridLayer;

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 600, new _010_Column_categories());
	}

	@Override
	public String getDescription() {
		return
				"This example demonstrates an alternative column chooser.\n" +
				"\n" +
				"- Right click on the column header.\n" +
				"- Select the Last option 'Choose columns'\n" +
				"- Hide some columns using the dialog\n" +
				"\n" +
				"This column chooser allows you to group the available columns into 'Categories'. Categories are a read " +
				"only concept and cannot be edited. The intent is to make it easier for the users to choose columns " +
				"when a large number of columns are available.";
	}
	
	public Control createExampleControl(Composite parent) {
		ConfigRegistry configRegistry = new ConfigRegistry();

		gridLayer = new GlazedListsGridLayer<RowDataFixture>(
				GlazedLists.eventList(RowDataListFixture.getList()),
				(String[])ArrayUtils.subarray(RowDataListFixture.getPropertyNames(), 0, 20),
				RowDataListFixture.getPropertyToLabelMap(),
				configRegistry);

		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable).withCategoriesBasedColumnChooser("Choose columns");
			}
		});

		configureColumnCategoriesInChooser();

		natTable.configure();
		return natTable;
	}

	private void configureColumnCategoriesInChooser() {
		DefaultBodyLayerStack bodyLayer = gridLayer.getBodyLayerStack();
		ColumnCategoriesModel model = new ColumnCategoriesModelFixture();

		bodyLayer.registerCommandHandler(
				new ChooseColumnsFromCategoriesCommandHandler(
						bodyLayer.getColumnHideShowLayer(),
						gridLayer.getColumnHeaderLayerStack().getColumnHeaderLayer(),
						gridLayer.getColumnHeaderLayerStack().getDataLayer(),
						model));
	}
}
