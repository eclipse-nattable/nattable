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
package org.eclipse.nebula.widgets.nattable.examples.examples._130_Sorting;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.GlazedListsGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TransformedList;

/**
 * Example to demonstrate sorting of the columns.
 */
public class SortableGridExample extends AbstractNatExample {

	private static final String CUSTOM_COMPARATOR_LABEL = "customComparatorLabel";
	protected static final String NO_SORT_LABEL = "noSortLabel";

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(700, 400, new PersistentNatExampleWrapper(new SortableGridExample()));
	}
	
	@Override
	public String getDescription() {
		return
				"Grid demonstrates sorting moving data.\n" +
				"\n" +
				"Features:\n" +
				"The contents of the grid are kept in sorted order as the rows are added/removed.\n" +
				"Custom comparators can be applied to each column.\n" +
				"Ignorecase comparator applied to the 'Rating' column.\n" +
				"Sorting can be turned off on the selective columns.\n" +
				"'Ask price' field is not sortable.\n" +
				"\n" +
				"Key bindings:\n" +
				"Sort by left clicking on the column header.\n" +
				"Add columns to the existing sort by (Alt. + left click) on the column header\n" +
				"\n" +
				"Technical information:\n" +
				"The default implementation uses GlazedLists to sort the backing data source.";
	}

	private TransformedList<RowDataFixture, RowDataFixture> rowObjectsGlazedList;
	private NatTable nattable;

	/**
	 * @see GlazedListsGridLayer to see the required stack setup.
	 * 	Basically the {@link SortHeaderLayer} needs to be a part of the Column header layer stack.
	 */
	public Control createExampleControl(Composite parent) {
		EventList<RowDataFixture> eventList = GlazedLists.eventList(RowDataListFixture.getList());
		rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

		ConfigRegistry configRegistry = new ConfigRegistry();
		GlazedListsGridLayer<RowDataFixture> glazedListsGridLayer = new GlazedListsGridLayer<RowDataFixture>(
				rowObjectsGlazedList,
				RowDataListFixture.getPropertyNames(),
				RowDataListFixture.getPropertyToLabelMap(),
				configRegistry);

		nattable = new NatTable(parent, glazedListsGridLayer, false);

		nattable.setConfigRegistry(configRegistry);
		nattable.addConfiguration(new DefaultNatTableStyleConfiguration());

		// Change the default sort key bindings. Note that 'auto configure' was turned off
		// for the SortHeaderLayer (setup in the GlazedListsGridLayer)
		nattable.addConfiguration(new SingleClickSortConfiguration());
		nattable.addConfiguration(getCustomComparatorConfiguration(glazedListsGridLayer.getColumnHeaderLayerStack().getDataLayer()));
		nattable.addConfiguration(new DefaultSelectionStyleConfiguration());

		nattable.addConfiguration(new HeaderMenuConfiguration(nattable) {
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable).withStateManagerMenuItemProvider();
			}
		});
		
		nattable.configure();
		
		//add the DisplayPersistenceDialogCommandHandler with the created NatTable instance after configure()
		//so all configuration and states are correctly applied before storing the default state
		glazedListsGridLayer.registerCommandHandler(new DisplayPersistenceDialogCommandHandler(nattable));
		return nattable;
	}

	/**
	 * NOTE: The labels for the the custom comparators must go on the columnHeaderDataLayer - since,
	 * 		the SortHeaderLayer will resolve cell labels with respect to its underlying layer i.e columnHeaderDataLayer
	 */
	private IConfiguration getCustomComparatorConfiguration(final AbstractLayer columnHeaderDataLayer) {

		return new AbstractRegistryConfiguration() {

			public void configureRegistry(IConfigRegistry configRegistry) {
				// Add label accumulator
				ColumnOverrideLabelAccumulator labelAccumulator = new ColumnOverrideLabelAccumulator(columnHeaderDataLayer);
				columnHeaderDataLayer.setConfigLabelAccumulator(labelAccumulator);

				// Register labels
				labelAccumulator.registerColumnOverrides(
	               RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.RATING_PROP_NAME),
	               CUSTOM_COMPARATOR_LABEL);

				labelAccumulator.registerColumnOverrides(
                     RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.ASK_PRICE_PROP_NAME),
                     NO_SORT_LABEL);

				// Register custom comparator
				configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
									                       getCustomComparator(),
									                       DisplayMode.NORMAL,
									                       CUSTOM_COMPARATOR_LABEL);

				// Register null comparator to disable sort
				configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
				                                       new NullComparator(),
				                                       DisplayMode.NORMAL,
				                                       NO_SORT_LABEL);
			}
		};
	}

	private Comparator<?> getCustomComparator() {
		return new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		};
	};
}
