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
package org.eclipse.nebula.widgets.nattable.examples.examples._131_Filtering;

import java.util.Arrays;
import java.util.Comparator;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.examples._110_Editing.EditableGridExample;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.StaticFilterExampleGridLayer;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class StaticFilterGridExample extends AbstractNatExample {
	
	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(new PersistentNatExampleWrapper(new StaticFilterGridExample()));
	}

	@Override
	public String getDescription() {
		return
				"Grid demonstrates the usage of a static filter.\n" +
				"\n" +
				"Features:\n" +
				"This example shows only RowDataFixtures with ratings other than 'AAA'.\n" +
				"It is also sortable and editable and has a filter row to show that the static filter\n" +
				"also works with some other features.";
	}
	
	public Control createExampleControl(Composite parent) {
		IConfigRegistry configRegistry = new ConfigRegistry();
		StaticFilterExampleGridLayer underlyingLayer = new StaticFilterExampleGridLayer(configRegistry);

		DataLayer bodyDataLayer = underlyingLayer.getBodyDataLayer();
		IDataProvider dataProvider = underlyingLayer.getBodyDataProvider();

		// NOTE: Register the accumulator on the body data layer.
		// This ensures that the labels are bound to the column index and are unaffected by column order.
		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);

		
		NatTable natTable = new NatTable(parent, underlyingLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		//natTable.addConfiguration(new DebugMenuConfiguration(natTable));
		natTable.addConfiguration(new FilterRowCustomConfiguration());
		
		natTable.addConfiguration(EditableGridExample.editableGridConfiguration(columnLabelAccumulator, dataProvider));

		natTable.setConfigRegistry(configRegistry);
		natTable.configure();

		return natTable;
	}

	static class FilterRowCustomConfiguration extends AbstractRegistryConfiguration {

		final DefaultDoubleDisplayConverter doubleDisplayConverter = new DefaultDoubleDisplayConverter();

		public void configureRegistry(IConfigRegistry configRegistry) {
			// Configure custom comparator on the rating column
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_COMPARATOR,
					getIngnorecaseComparator(),
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 2);

			// If threshold comparison is used we have to convert the string entered by the
			// user to the correct underlying type (double), so that it can be compared

			// Configure Bid column
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
					doubleDisplayConverter,
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.TEXT_MATCHING_MODE,
					TextMatchingMode.REGULAR_EXPRESSION,
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);
			
			// Configure Ask column
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
					doubleDisplayConverter,
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 6);
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.TEXT_MATCHING_MODE,
					TextMatchingMode.REGULAR_EXPRESSION,
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 6);
		
			// Configure a combo box on the pricing type column
			
			// Register a combo box editor to be displayed in the filter row cell 
			//    when a value is selected from the combo, the object is converted to a string
			//    using the converter (registered below)
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, 
					new ComboBoxCellEditor(Arrays.asList(new PricingTypeBean("MN"), new PricingTypeBean("AT"))), 
					DisplayMode.NORMAL, 
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 4);
			
			// The pricing bean object in column is converted to using this display converter
			// A 'text' match is then performed against the value from the combo box 
			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
					PricingTypeBean.getDisplayConverter(),
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 4);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
					PricingTypeBean.getDisplayConverter(),
					DisplayMode.NORMAL,
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 4);
			
			configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, PricingTypeBean.getDisplayConverter(), DisplayMode.NORMAL, "PRICING_TYPE_PROP_NAME");
		}
	}
	
	private static Comparator<?> getIngnorecaseComparator() {
		return new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		};
	};

}
