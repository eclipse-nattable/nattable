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
package org.eclipse.nebula.widgets.nattable.examples.examples._101_Data;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
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

public class Creating_a_summary_row extends AbstractNatExample {

	private IDataProvider myDataProvider;

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new Creating_a_summary_row());
	}

	@Override
	public String getDescription() {
		return
				"Grid demonstrates adding a Summary row at the end of the table.\n" +
				"\n" +
				"Features\n" +
				"	Different style can be applied to the whole row\n" +
				"	Different style can be applied to the individual cells in the summary row\n" +
				"	Plug-in your own summary formulas via ISummaryProvider interface (Default is summation)";
	}
	
	public Control createExampleControl(Composite parent) {
		myDataProvider = new IDataProvider() {

			public int getColumnCount() {
				return 4;
			}

			public int getRowCount() {
				return 10;
			}

			public Object getDataValue(int columnIndex, int rowIndex) {
				if(columnIndex >= getColumnCount() || rowIndex >= getRowCount()){
					throw new RuntimeException("Data value requested is out of bounds");
				}
				return (columnIndex % 2 == 0) ? 10 : "Apple";
			}

			public void setDataValue(int columnIndex, int rowIndex, Object newValue) {}
		};

		IConfigRegistry configRegistry = new ConfigRegistry();
		IUniqueIndexLayer dataLayer = new DataLayer(myDataProvider);

		// Plug in the SummaryRowLayer
		IUniqueIndexLayer summaryRowLayer = new SummaryRowLayer(dataLayer, configRegistry, false);
		ViewportLayer viewportLayer = new ViewportLayer(summaryRowLayer);

		NatTable natTable = new NatTable(parent, viewportLayer, false);

		// Configure custom summary formula for a column
		natTable.addConfiguration(new MySummaryRowConfig(myDataProvider));
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.setConfigRegistry(configRegistry);
		natTable.configure();

		return natTable;
	}

	/**
	 * Custom summary provider which averages out the contents of the column
	 */
	class AverageSummaryProvider implements ISummaryProvider {
		public Object summarize(int columnIndex) {
			int total = 0;
			int rowCount = myDataProvider.getRowCount();

			for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
				Object dataValue = myDataProvider.getDataValue(columnIndex, rowIndex);
				total = total + Integer.parseInt(dataValue.toString());
			}
			return "Average: " + total / rowCount;
		}
	}

	/**
	 * Override the DefaultSummaryRowConfiguration for customizing the summary row style and/or summary formulas
	 */
	class MySummaryRowConfig extends DefaultSummaryRowConfiguration {

		private final IDataProvider myDataProvider;

		public MySummaryRowConfig(IDataProvider myDataProvider) {
			this.myDataProvider = myDataProvider;
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
					new SummationSummaryProvider(myDataProvider),
					DisplayMode.NORMAL,
					SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

			// Average summary provider for column index 2
			configRegistry.registerConfigAttribute(
					SummaryRowConfigAttributes.SUMMARY_PROVIDER,
					new AverageSummaryProvider(),
					DisplayMode.NORMAL,
					SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 2);
		}
	}

}
