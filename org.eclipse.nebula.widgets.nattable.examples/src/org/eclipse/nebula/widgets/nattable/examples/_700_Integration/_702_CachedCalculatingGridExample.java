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
package org.eclipse.nebula.widgets.nattable.examples._700_Integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
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
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummationSummaryProvider;
import org.eclipse.nebula.widgets.nattable.util.CalculatedValueCache;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.ICalculator;
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
 * calculated values by using the CalculatedValueCache.
 * Also demonstrates the usage of the SummaryRow on updating the NatTable.
 * 
 * @author Dirk Fauth
 *
 */
public class _702_CachedCalculatingGridExample extends AbstractNatExample {

	public static String COLUMN_ONE_LABEL = "ColumnOneLabel";
	public static String COLUMN_TWO_LABEL = "ColumnTwoLabel";
	public static String COLUMN_THREE_LABEL = "ColumnThreeLabel";
	public static String COLUMN_FOUR_LABEL = "ColumnFourLabel";
	public static String COLUMN_FIVE_LABEL = "ColumnFiveLabel";
	
	private EventList<NumberValues> valuesToShow = GlazedLists.eventList(new ArrayList<NumberValues>());
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _702_CachedCalculatingGridExample());
	}

	/**	
	 * @Override
	 */
	@Override
	public String getDescription() {
		return "This example demonstrates how to create a NatTable that contains calculated values.\n"
				+ "The first three columns are editable, while the last two columns contain the calculated values.\n"
				+ "The values in column four and five will automatically update when committing the edited values.\n"
				+ "This example also contains a summary row to show that it is even possible to update the summary "
				+ "row in a editable grid. The value calculation is processed in background threads by using the "
				+ "CalculatedValueCache in a specialised ListDataProvider.";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.examples.INatExample#createExampleControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
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
		DataLayer bodyDataLayer = gridLayer.getBodyDataLayer();
		
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

	
	/**
	 * The column accessor which is used for retrieving the basic data out of the model.
	 * The values for the first three columns are returned directly. The values for column
	 * four and five are calculated in the CachedValueCalculatingDataProvider by using the
	 * CalculatedValueCache.
	 */
	class BasicDataColumnAccessor implements IColumnAccessor<NumberValues> {
		
		/* (non-Javadoc)
		 * @see org.eclipse.nebula.widgets.nattable.data.IColumnAccessor#getDataValue(java.lang.Object, int)
		 */
		@Override
		public Object getDataValue(NumberValues rowObject, int columnIndex) {
			switch(columnIndex) {
				case 0: return rowObject.getColumnOneNumber();
				case 1: return rowObject.getColumnTwoNumber();
				case 2: return rowObject.getColumnThreeNumber();
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.nebula.widgets.nattable.data.IColumnAccessor#setDataValue(java.lang.Object, int, java.lang.Object)
		 */
		@Override
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
		@Override
		public int getColumnCount() {
			//this example will show exactly 5 columns
			return 5;
		}
		
	}
	
	/**
	 * Specialised ListDataProvider that is using the CalculatedValueCache for background processing
	 * of calculated column values.
	 * <p>
	 * As the updates after the calculation processing need to be fired via ILayer, the CalculatedValueCache
	 * is created with no ILayer at construction time. But the DataLayer e.g. needs to be set in order to
	 * make the automatic updates on data modifications work.
	 */
	class CachedValueCalculatingDataProvider<T> extends GlazedListsDataProvider<T> {

		private CalculatedValueCache valueCache;
		
		public CachedValueCalculatingDataProvider(EventList<T> list, IColumnAccessor<T> columnAccessor) {
			super(list, columnAccessor);
			//create the CalculatedValueCache without layer reference, as the data provider is no layer
			this.valueCache = new CalculatedValueCache(null, true, true);
		}
		
		@Override
		public Object getDataValue(final int colIndex, final int rowIndex) {
			if (colIndex == 3) {
				Object result = this.valueCache.getCalculatedValue(colIndex, rowIndex, true, new ICalculator() {
					@Override
					public Object executeCalculation() {
						//calculate the sum
						int colTwo = (Integer) getDataValue(1, rowIndex);
						int colThree = (Integer) getDataValue(2, rowIndex);
						//add some delay 
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
						return colTwo + colThree;
					}
				});
				
				return result == null ? 0 : result;
			}
			else if (colIndex == 4) {
				Object result = this.valueCache.getCalculatedValue(colIndex, rowIndex, true, new ICalculator() {
					@Override
					public Object executeCalculation() {
						//calculate the percentage
						int colOne = (Integer) getDataValue(0, rowIndex);
						int colTwo = (Integer) getDataValue(1, rowIndex);
						int colThree = (Integer) getDataValue(2, rowIndex);
						//add some delay 
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
						return new Double(colTwo + colThree) / colOne;
					}
				});
				
				return result == null ? new Double(0) : result;
			}
			
			return super.getDataValue(colIndex, rowIndex);
		}
		
		public void setCacheEventLayer(ILayer layer) {
			this.valueCache.setLayer(layer);
		}
	}
	
	
	/**
	 * The body layer stack for the {@link _702_CachedCalculatingGridExample}.
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
			final CachedValueCalculatingDataProvider<NumberValues> dataProvider = 
					new CachedValueCalculatingDataProvider<NumberValues>(valuesToShow, new BasicDataColumnAccessor());
			bodyDataLayer = new DataLayer(dataProvider);
			//adding this listener will trigger updates on data changes
			bodyDataLayer.addLayerListener(new ILayerListener() {
				@Override
				public void handleLayerEvent(ILayerEvent event) {
					if (event instanceof IVisualChangeEvent) {
						dataProvider.valueCache.clearCache();
					}
				}
			});
			//register a layer listener to ensure the value cache gets disposed
			bodyDataLayer.registerCommandHandler(new ILayerCommandHandler<DisposeResourcesCommand>() {

				@Override
				public boolean doCommand(ILayer targetLayer, DisposeResourcesCommand command) {
					dataProvider.valueCache.dispose();
					return false;
				}
				
				@Override
				public Class<DisposeResourcesCommand> getCommandClass() {
					return DisposeResourcesCommand.class;
				}
			});
			
			//connect the DataLayer with the data provider so the value cache knows how to fire updates
			dataProvider.setCacheEventLayer(bodyDataLayer);
			
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
	 * The {@link GridLayer} used by the {@link _702_CachedCalculatingGridExample}.
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
		
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE);
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE, 
					DisplayMode.EDIT, _702_CachedCalculatingGridExample.COLUMN_FOUR_LABEL);
			configRegistry.registerConfigAttribute(
					EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE, 
					DisplayMode.EDIT, _702_CachedCalculatingGridExample.COLUMN_FIVE_LABEL);
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
					DisplayMode.NORMAL, _702_CachedCalculatingGridExample.COLUMN_FIVE_LABEL);
			
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
			@Override
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

}
