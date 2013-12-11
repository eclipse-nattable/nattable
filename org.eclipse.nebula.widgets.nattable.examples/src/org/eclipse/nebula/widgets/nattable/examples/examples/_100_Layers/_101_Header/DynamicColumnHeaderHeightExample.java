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
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers._101_Header;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.extension.poi.HSSFExcelExporter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;


public class DynamicColumnHeaderHeightExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new DynamicColumnHeaderHeightExample());
	}

	@Override
	public String getDescription() {
		return
				"This example demonstrates dynamic column header height configuration.\n" +
				"It uses column grouping and filter row which are are related for height calculation.\n" +
				"\n" +
				"Following functions will change the column header height:\n" + 
				"* GROUP SELECTED COLUMNS with ctrl-g or popup menu.\n" +
				"* UNGROUP SELECTED COLUMNS with ctrl-u or popup menu until no column group is available.\n" +
				"* Press button to enable/disable the filter row.";
	}

	private final ColumnGroupModel columnGroupModel = new ColumnGroupModel();
	private ColumnHeaderLayer columnHeaderLayer;

	@Override
	public Control createExampleControl(Composite parent) {
		IConfigRegistry configRegistry = new ConfigRegistry();

		// Underlying data source
		EventList<RowDataFixture> eventList = GlazedLists.eventList(RowDataListFixture.getList(200));
		FilterList<RowDataFixture> filterList = new FilterList<RowDataFixture>(eventList);
		String[] propertyNames = RowDataListFixture.getPropertyNames();
		Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();

		// Body
		IColumnPropertyAccessor<RowDataFixture> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames);
		ListDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(filterList, columnPropertyAccessor);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		ColumnGroupBodyLayerStack bodyLayer = new ColumnGroupBodyLayerStack(bodyDataLayer, columnGroupModel);

		ColumnOverrideLabelAccumulator bodyLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(bodyLabelAccumulator);
		
		bodyLabelAccumulator.registerColumnOverrides(
		           RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME),
		           "PRICING_TYPE_PROP_NAME");

		
		// Column header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());
		ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, bodyLayer.getSelectionLayer(), columnGroupModel);

		columnGroupHeaderLayer.addColumnsIndexesToGroup("Group 1", 1,2);
		
		//calculate the height of the column header area dependent if column groups exist or not
		columnGroupHeaderLayer.setCalculateHeight(true);

		//	Note: The column header layer is wrapped in a filter row composite.
		//	This plugs in the filter row functionality
		final FilterRowHeaderComposite<RowDataFixture> filterRowHeaderLayer =
			new FilterRowHeaderComposite<RowDataFixture>(
					new DefaultGlazedListsFilterStrategy<RowDataFixture>(filterList, columnPropertyAccessor, configRegistry),
					columnGroupHeaderLayer, columnHeaderDataProvider, configRegistry
			);

		filterRowHeaderLayer.setFilterRowVisible(false);
		
		ColumnOverrideLabelAccumulator labelAccumulator = new ColumnOverrideLabelAccumulator(columnHeaderDataLayer);
		columnHeaderDataLayer.setConfigLabelAccumulator(labelAccumulator);

		// Register labels
		labelAccumulator.registerColumnOverrides(
           RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.RATING_PROP_NAME),
           "CUSTOM_COMPARATOR_LABEL");
		
		// Row header

		final DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Corner

		final DefaultCornerDataProvider cornerDataProvider =
			new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, filterRowHeaderLayer);

		// Grid
		GridLayer gridLayer = new GridLayer(
				bodyLayer,
				filterRowHeaderLayer,
				rowHeaderLayer,
				cornerLayer);


		NatTable natTable = new NatTable(parent, gridLayer, false);

		// Register create column group command handler

		// Register column chooser
		DisplayColumnChooserCommandHandler columnChooserCommandHandler = new DisplayColumnChooserCommandHandler(
				bodyLayer.getSelectionLayer(),
				bodyLayer.getColumnHideShowLayer(),
				columnHeaderLayer,
				columnHeaderDataLayer,
				columnGroupHeaderLayer,
				columnGroupModel);
		bodyLayer.registerCommandHandler(columnChooserCommandHandler);

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable).withColumnChooserMenuItem();
			}
		});
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(ExportConfigAttributes.EXPORTER, new HSSFExcelExporter());
			}
		});
		natTable.addConfiguration(new FilterRowCustomConfiguration());

		natTable.setConfigRegistry(configRegistry);
		natTable.configure();
		
		//add button
		Button button = new Button(parent, SWT.NONE);
		button.setText("Switch FilterRow visibility");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filterRowHeaderLayer.setFilterRowVisible(!filterRowHeaderLayer.isFilterRowVisible());
			}
		});
		
		return natTable;
	}

	
	public static class FilterRowCustomConfiguration extends AbstractRegistryConfiguration {

		final DefaultDoubleDisplayConverter doubleDisplayConverter = new DefaultDoubleDisplayConverter();

		@Override
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

			// Shade the row to be slightly darker than the blue background.
			final Style rowStyle = new Style();
			rowStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(197, 212, 231));
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle, DisplayMode.NORMAL, GridRegion.FILTER_ROW);
		}
	}
	
	private static Comparator<?> getIngnorecaseComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		};
	};
	
}
