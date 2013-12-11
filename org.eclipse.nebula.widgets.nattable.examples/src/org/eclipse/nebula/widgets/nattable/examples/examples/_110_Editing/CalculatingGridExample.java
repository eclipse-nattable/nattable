/*******************************************************************************
 * Copyright (c) 2012, 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._110_Editing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultSummaryRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummationSummaryProvider;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * Example that demonstrates how to implement a NatTable instance that shows
 * calculated values.
 * Also demonstrates the usage of the SummaryRow on updating the NatTable.
 * 
 * @author Dirk Fauth
 *
 */
public class CalculatingGridExample extends AbstractNatExample {

	public static String COLUMN_ONE_LABEL = "ColumnOneLabel";
	public static String COLUMN_TWO_LABEL = "ColumnTwoLabel";
	public static String COLUMN_THREE_LABEL = "ColumnThreeLabel";
	public static String COLUMN_FOUR_LABEL = "ColumnFourLabel";
	public static String COLUMN_FIVE_LABEL = "ColumnFiveLabel";
	
	private EventList<NumberValues> valuesToShow = GlazedLists.eventList(new ArrayList<NumberValues>());
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new CalculatingGridExample());
	}

	/**	
	 * @Override
	 */
	public String getDescription() {
		return "Demonstrates how to implement a editable grid with calculated column values.\n" +
				"Also adds the SummaryRow to demonstrate how the SummaryRow updates on changes " +
				"within the grid.";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.examples.INatExample#createExampleControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createExampleControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);
		
		Composite gridPanel = new Composite(panel, SWT.NONE);
		gridPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(gridPanel);
		
		Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(buttonPanel);

		//property names of the NumberValues class
		String[] propertyNames = {"columnOneNumber", "columnTwoNumber", "columnThreeNumber", "columnFourNumber", 
				"columnFiveNumber"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("columnOneNumber", "100%");
		propertyToLabelMap.put("columnTwoNumber", "Value One");
		propertyToLabelMap.put("columnThreeNumber", "Value Two");
		propertyToLabelMap.put("columnFourNumber", "Sum");
		propertyToLabelMap.put("columnFiveNumber", "Percentage");

		valuesToShow.add(createNumberValues());
		valuesToShow.add(createNumberValues());
		
		ConfigRegistry configRegistry = new ConfigRegistry();
		
		CalculatingGridLayer gridLayer = new CalculatingGridLayer(valuesToShow, configRegistry, 
				propertyNames, propertyToLabelMap);
		DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		
		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
		registerColumnLabels(columnLabelAccumulator);
		
		final NatTable natTable = new NatTable(gridPanel, gridLayer, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new CalculatingEditConfiguration());
		natTable.configure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

		Button addRowButton = new Button(buttonPanel, SWT.PUSH);
		addRowButton.setText("add row");
		addRowButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				valuesToShow.add(createNumberValues());
			}
		});

		Button resetButton = new Button(buttonPanel, SWT.PUSH);
		resetButton.setText("reset");
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				valuesToShow.clear();
				valuesToShow.add(createNumberValues());
				valuesToShow.add(createNumberValues());
			}
		});

		return panel;
	}
	
	private void registerColumnLabels(ColumnOverrideLabelAccumulator columnLabelAccumulator) {
		columnLabelAccumulator.registerColumnOverrides(0, COLUMN_ONE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(1, COLUMN_TWO_LABEL);
		columnLabelAccumulator.registerColumnOverrides(2, COLUMN_THREE_LABEL);
		columnLabelAccumulator.registerColumnOverrides(3, COLUMN_FOUR_LABEL);
		columnLabelAccumulator.registerColumnOverrides(4, COLUMN_FIVE_LABEL);
	}
	
	private NumberValues createNumberValues() {
		NumberValues nv = new NumberValues();
		nv.setColumnOneNumber(100);  //the value which should be used as 100%
		nv.setColumnTwoNumber(20);   //the value 1 for calculation
		nv.setColumnThreeNumber(30); //the value 2 for calculation
		//as column 4 and 5 should be calculated values, we don't set them to the NumberValues object
		return nv;
	}

}

/**
 * The column accessor which is used for retrieving the data out of the model.
 * While the values for the first three columns are returned directly, the values for
 * column four and five are calculated.
 */
class CalculatingDataProvider implements IColumnAccessor<NumberValues> {

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IColumnAccessor#getDataValue(java.lang.Object, int)
	 */
	public Object getDataValue(NumberValues rowObject, int columnIndex) {
		switch(columnIndex) {
			case 0: return rowObject.getColumnOneNumber();
			case 1: return rowObject.getColumnTwoNumber();
			case 2: return rowObject.getColumnThreeNumber();
			case 3: //calculate the sum
					return rowObject.getColumnTwoNumber() + rowObject.getColumnThreeNumber();
			case 4: //calculate the percentage
					return new Double(rowObject.getColumnTwoNumber() + rowObject.getColumnThreeNumber()) / rowObject.getColumnOneNumber();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IColumnAccessor#setDataValue(java.lang.Object, int, java.lang.Object)
	 */
	public void setDataValue(NumberValues rowObject, int columnIndex, Object newValue) {
		//because of the registered conversion, the new value has to be an Integer
		switch(columnIndex) {
			case 0: rowObject.setColumnOneNumber((Integer)newValue);
					break;
			case 1: rowObject.setColumnTwoNumber((Integer)newValue);
					break;
			case 2: rowObject.setColumnThreeNumber((Integer)newValue);
					break;
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.IColumnAccessor#getColumnCount()
	 */
	public int getColumnCount() {
		//this example will show exactly 5 columns
		return 5;
	}
	
}

/**
 * The body layer stack for the {@link CalculatingGridExample}.
 * Consists of
 * <ol>
 * <li>ViewportLayer</li>
 * <li>SelectionLayer</li>
 * <li>ColumnHideShowLayer</li>
 * <li>ColumnReorderLayer</li>
 * <li>SummaryRowLayer</li>
 * <li>GlazedListsEventLayer</li>
 * <li>DataLayer</li>
 * </ol>
 */
class CalculatingBodyLayerStack extends AbstractLayerTransform {

	private final DataLayer bodyDataLayer;
	private final GlazedListsEventLayer<NumberValues> glazedListsEventLayer;
	private final SummaryRowLayer summaryRowLayer;
	private final ColumnReorderLayer columnReorderLayer;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;

	public CalculatingBodyLayerStack(EventList<NumberValues> valuesToShow, ConfigRegistry configRegistry) {
		IDataProvider dataProvider = new GlazedListsDataProvider<NumberValues>(valuesToShow, new CalculatingDataProvider());
		bodyDataLayer = new DataLayer(dataProvider);
		glazedListsEventLayer = new GlazedListsEventLayer<NumberValues>(bodyDataLayer, valuesToShow);
		summaryRowLayer = new SummaryRowLayer(glazedListsEventLayer, configRegistry, false);
		summaryRowLayer.addConfiguration(new CalculatingSummaryRowConfiguration(bodyDataLayer.getDataProvider()));
		columnReorderLayer = new ColumnReorderLayer(summaryRowLayer);
		columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		selectionLayer = new SelectionLayer(columnHideShowLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		setUnderlyingLayer(viewportLayer);

		registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
	}

	public DataLayer getDataLayer() {
		return this.bodyDataLayer;
	}
	
	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}
}

/**
 * The {@link GridLayer} used by the {@link CalculatingGridExample}.
 */
class CalculatingGridLayer extends GridLayer {

	public CalculatingGridLayer(EventList<NumberValues> valuesToShow, ConfigRegistry configRegistry,
			final String[] propertyNames, Map<String, String> propertyToLabelMap) {
		super(true);
		init(valuesToShow, configRegistry, propertyNames, propertyToLabelMap);
	}


	private void init(EventList<NumberValues> valuesToShow, ConfigRegistry configRegistry,
			final String[] propertyNames, Map<String, String> propertyToLabelMap) {
		// Body
		CalculatingBodyLayerStack bodyLayer = new CalculatingBodyLayerStack(valuesToShow, configRegistry);
		
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

		// Column header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(
				 new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), 
				bodyLayer, selectionLayer);
		
		// Row header
		IDataProvider rowHeaderDataProvider = new DefaultSummaryRowHeaderDataProvider(
				bodyLayer.getDataLayer().getDataProvider(), "\u2211");
		ILayer rowHeaderLayer = new RowHeaderLayer(
				new DefaultRowHeaderDataLayer(rowHeaderDataProvider), 
				bodyLayer, selectionLayer);
		
		// Corner
		ILayer cornerLayer = new CornerLayer(
				new DataLayer(new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)), 
				rowHeaderLayer, columnHeaderLayer);
		
		setBodyLayer(bodyLayer);
		setColumnHeaderLayer(columnHeaderLayer);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
	}
	
	
	public DataLayer getBodyDataLayer() {
		return ((CalculatingBodyLayerStack)getBodyLayer()).getDataLayer();
	}
}

/**
 * Configuration for enabling and configuring edit behaviour.
 */
class CalculatingEditConfiguration extends AbstractRegistryConfiguration  {

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE);
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE, 
				DisplayMode.EDIT, CalculatingGridExample.COLUMN_FOUR_LABEL);
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE, 
				DisplayMode.EDIT, CalculatingGridExample.COLUMN_FIVE_LABEL);
		//configure the summary row to be not editable
		configRegistry.registerConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE, 
				DisplayMode.EDIT, SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, new DefaultIntegerDisplayConverter(), DisplayMode.NORMAL);
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, new DefaultIntegerDisplayConverter(), DisplayMode.EDIT);

		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, new PercentageDisplayConverter(), 
				DisplayMode.NORMAL, CalculatingGridExample.COLUMN_FIVE_LABEL);

		configRegistry.registerConfigAttribute(
				CellConfigAttributes.DISPLAY_CONVERTER, new PercentageDisplayConverter(), 
				DisplayMode.NORMAL, SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 4);
	}
}


class CalculatingSummaryRowConfiguration extends DefaultSummaryRowConfiguration {
	
	private final IDataProvider dataProvider;

	public CalculatingSummaryRowConfiguration(IDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		summaryRowBgColor = GUIHelper.COLOR_BLUE;
		summaryRowFgColor = GUIHelper.COLOR_WHITE;
	}

	@Override
	public void addSummaryProviderConfig(IConfigRegistry configRegistry) {
		// Labels are applied to the summary row and cells by default to make configuration easier.
		// See the Javadoc for the SummaryRowLayer

		// Default summary provider
		configRegistry.registerConfigAttribute(
				SummaryRowConfigAttributes.SUMMARY_PROVIDER,
				new SummationSummaryProvider(dataProvider),
				DisplayMode.NORMAL,
				SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

		// Average summary provider for column index 2
		configRegistry.registerConfigAttribute(
				SummaryRowConfigAttributes.SUMMARY_PROVIDER,
				new AverageSummaryProvider(),
				DisplayMode.NORMAL,
				SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 4);
	}

	/**
	 * Custom summary provider which averages out the contents of the column
	 */
	class AverageSummaryProvider implements ISummaryProvider {
		public Object summarize(int columnIndex) {
			double total = 0;
			int rowCount = dataProvider.getRowCount();

			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				Object dataValue = dataProvider.getDataValue(columnIndex, rowIndex);
				total = total + Double.parseDouble(dataValue.toString());
			}
			return total / rowCount;
		}
	}

}
