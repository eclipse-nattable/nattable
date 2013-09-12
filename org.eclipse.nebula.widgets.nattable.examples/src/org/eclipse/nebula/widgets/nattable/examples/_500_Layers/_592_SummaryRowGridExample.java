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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example that demonstrates how to implement a NatTable instance that shows
 * calculated values.
 * Also demonstrates the usage of the SummaryRow on updating the NatTable.
 * 
 * @author Dirk Fauth
 *
 */
public class _592_SummaryRowGridExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _592_SummaryRowGridExample());
	}

	@Override
	public String getDescription() {
		return "This example demonstrates how to add a summary row to the end of a grid.\n" +
				"\n" +
				"Features\n" +
				"	Different style can be applied to the whole row\n" +
				"	Different style can be applied to the individual cells in the summary row\n" +
				"	Plug-in your own summary formulas via ISummaryProvider interface (Default is summation)";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.examples.INatExample#createExampleControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createExampleControl(Composite parent) {
		//property names of the NumberValues class
		String[] propertyNames = {"columnOneNumber", "columnTwoNumber", "columnThreeNumber", "columnFourNumber", 
				"columnFiveNumber"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("columnOneNumber", "Column 1");
		propertyToLabelMap.put("columnTwoNumber", "Column 2");
		propertyToLabelMap.put("columnThreeNumber", "Column 3");
		propertyToLabelMap.put("columnFourNumber", "Column 4");
		propertyToLabelMap.put("columnFiveNumber", "Column 5");

		IColumnPropertyAccessor<NumberValues> cpa = 
				new ReflectiveColumnPropertyAccessor<NumberValues>(propertyNames);
		IDataProvider dataProvider = 
				new ListDataProvider<NumberValues>(createNumberValueList(), cpa);

		ConfigRegistry configRegistry = new ConfigRegistry();
		
		SummaryRowGridLayer gridLayer = new SummaryRowGridLayer(dataProvider, configRegistry, 
				propertyNames, propertyToLabelMap);

		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.configure();

		return natTable;
	}
	
	private List<NumberValues> createNumberValueList() {
		List<NumberValues> result = new ArrayList<NumberValues>();
		
		for (int i = 0; i < 25; i++) {
			NumberValues nv = new NumberValues();
			nv.setColumnOneNumber(5);
			nv.setColumnTwoNumber(4);
			nv.setColumnThreeNumber(3);
			nv.setColumnFourNumber(1);
			nv.setColumnFiveNumber(1);
			result.add(nv);
			
			nv = new NumberValues();
			nv.setColumnOneNumber(1);
			nv.setColumnTwoNumber(1);
			nv.setColumnThreeNumber(2);
			nv.setColumnFourNumber(2);
			nv.setColumnFiveNumber(3);
			result.add(nv);
			
			nv = new NumberValues();
			nv.setColumnOneNumber(1);
			nv.setColumnTwoNumber(2);
			nv.setColumnThreeNumber(2);
			nv.setColumnFourNumber(3);
			nv.setColumnFiveNumber(3);
			result.add(nv);
			
			nv = new NumberValues();
			nv.setColumnOneNumber(1);
			nv.setColumnTwoNumber(2);
			nv.setColumnThreeNumber(4);
			nv.setColumnFourNumber(4);
			nv.setColumnFiveNumber(3);
			result.add(nv);
			
			nv = new NumberValues();
			nv.setColumnOneNumber(5);
			nv.setColumnTwoNumber(4);
			nv.setColumnThreeNumber(4);
			nv.setColumnFourNumber(4);
			nv.setColumnFiveNumber(7);
			result.add(nv);
		}
			
		return result;
	}
}


/**
 * The body layer stack for the {@link _592_SummaryRowGridExample}.
 * Consists of
 * <ol>
 * <li>ViewportLayer</li>
 * <li>SelectionLayer</li>
 * <li>ColumnHideShowLayer</li>
 * <li>ColumnReorderLayer</li>
 * <li>SummaryRowLayer</li>
 * <li>DataLayer</li>
 * </ol>
 */
class SummaryRowBodyLayerStack extends AbstractLayerTransform {

	private final DataLayer bodyDataLayer;
	private final SummaryRowLayer summaryRowLayer;
	private final ColumnReorderLayer columnReorderLayer;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;

	public SummaryRowBodyLayerStack(IDataProvider dataProvider, ConfigRegistry configRegistry) {
		bodyDataLayer = new DataLayer(dataProvider);
		summaryRowLayer = new SummaryRowLayer(bodyDataLayer, configRegistry, false);
		summaryRowLayer.addConfiguration(new ExampleSummaryRowGridConfiguration(bodyDataLayer.getDataProvider()));
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
 * The {@link GridLayer} used by the {@link _592_SummaryRowGridExample}.
 */
class SummaryRowGridLayer extends GridLayer {

	public SummaryRowGridLayer(IDataProvider dataProvider, ConfigRegistry configRegistry,
			final String[] propertyNames, Map<String, String> propertyToLabelMap) {
		super(true);
		init(dataProvider, configRegistry, propertyNames, propertyToLabelMap);
	}


	private void init(IDataProvider dataProvider, ConfigRegistry configRegistry,
			final String[] propertyNames, Map<String, String> propertyToLabelMap) {
		// Body
		SummaryRowBodyLayerStack bodyLayer = new SummaryRowBodyLayerStack(dataProvider, configRegistry);
		
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

		// Column header
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(
				 new DefaultColumnHeaderDataLayer(columnHeaderDataProvider), 
				bodyLayer, selectionLayer);
		
		// Row header
		// Adding the specialized DefaultSummaryRowHeaderDataProvider to indicate the summary row in the row header
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
		return ((SummaryRowBodyLayerStack)getBodyLayer()).getDataLayer();
	}
}

class ExampleSummaryRowGridConfiguration extends DefaultSummaryRowConfiguration {
	
	private final IDataProvider dataProvider;

	public ExampleSummaryRowGridConfiguration(IDataProvider dataProvider) {
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
		@Override
		public Object summarize(int columnIndex) {
			double total = 0;
			int rowCount = dataProvider.getRowCount();

			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				Object dataValue = dataProvider.getDataValue(columnIndex, rowIndex);
				total = total + Double.parseDouble(dataValue.toString());
			}
			return "Avg: " + total / rowCount;
		}
	}

}
