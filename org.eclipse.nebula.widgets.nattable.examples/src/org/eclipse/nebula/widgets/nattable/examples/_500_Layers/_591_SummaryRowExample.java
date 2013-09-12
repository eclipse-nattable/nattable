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
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.NumberValues;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
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
public class _591_SummaryRowExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _591_SummaryRowExample());
	}

	@Override
	public String getDescription() {
		return "This example demonstrates how to add a summary row to the end of the table.\n" +
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

		IColumnPropertyAccessor<NumberValues> cpa = new ReflectiveColumnPropertyAccessor<NumberValues>(propertyNames);
		IDataProvider dataProvider = new ListDataProvider<NumberValues>(createNumberValueList(), cpa);
		
		ConfigRegistry configRegistry = new ConfigRegistry();
		
		IUniqueIndexLayer dataLayer = new DataLayer(dataProvider);

		// Plug in the SummaryRowLayer
		IUniqueIndexLayer summaryRowLayer = new SummaryRowLayer(dataLayer, configRegistry, false);
		ViewportLayer viewportLayer = new ViewportLayer(summaryRowLayer);

		NatTable natTable = new NatTable(parent, viewportLayer, false);

		// Configure custom summary formula for a column
		natTable.addConfiguration(new ExampleSummaryRowConfiguration(dataProvider));
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.setConfigRegistry(configRegistry);
		natTable.configure();

		return natTable;
	}
	
	private List<NumberValues> createNumberValueList() {
		List<NumberValues> result = new ArrayList<NumberValues>();
		
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
		return result;
	}
}

class ExampleSummaryRowConfiguration extends DefaultSummaryRowConfiguration {
	
	private final IDataProvider dataProvider;

	public ExampleSummaryRowConfiguration(IDataProvider dataProvider) {
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
