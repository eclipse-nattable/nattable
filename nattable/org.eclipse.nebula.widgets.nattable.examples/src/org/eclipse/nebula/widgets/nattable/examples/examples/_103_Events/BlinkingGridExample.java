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
package org.eclipse.nebula.widgets.nattable.examples.examples._103_Events;

import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.blink.BlinkConfigAttributes;
import org.eclipse.nebula.widgets.nattable.blink.BlinkLayer;
import org.eclipse.nebula.widgets.nattable.blink.BlinkingCellResolver;
import org.eclipse.nebula.widgets.nattable.blink.IBlinkingCellResolver;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.BlinkingRowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

public class BlinkingGridExample extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new BlinkingGridExample());
	}
	
	private static final String BLINK_UP_CONFIG_LABEL = "blinkUpConfigLabel";
	private static final String BLINK_DOWN_CONFIG_LABEL = "blinkDownConfigLabel";

	private ListDataProvider<BlinkingRowDataFixture> bodyDataProvider;

	private ScheduledExecutorService scheduledThreadPool;
	
	public Control createExampleControl(Composite parent) {
		final String[] propertyNames = RowDataListFixture.getPropertyNames();
		final Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();
		
		ConfigRegistry configRegistry = new ConfigRegistry();
		
		// Body
		LinkedList<BlinkingRowDataFixture> rowData = new LinkedList<BlinkingRowDataFixture>();
		EventList<BlinkingRowDataFixture> eventList = GlazedLists.eventList(rowData);
		ObservableElementList<BlinkingRowDataFixture> observableElementList = new ObservableElementList<BlinkingRowDataFixture>(eventList, GlazedLists.beanConnector(BlinkingRowDataFixture.class));
		IColumnPropertyAccessor<BlinkingRowDataFixture> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<BlinkingRowDataFixture>(propertyNames);
		bodyDataProvider = new ListDataProvider<BlinkingRowDataFixture>(observableElementList, columnPropertyAccessor);
		
		final DataLayer bodyLayer = new DataLayer(bodyDataProvider);
		
		GlazedListsEventLayer<BlinkingRowDataFixture> glazedListsEventLayer = new GlazedListsEventLayer<BlinkingRowDataFixture>(bodyLayer, observableElementList);
		BlinkLayer<BlinkingRowDataFixture> blinkingLayer = new BlinkLayer<BlinkingRowDataFixture>(glazedListsEventLayer, bodyDataProvider, BlinkingRowDataFixture.rowIdAccessor, columnPropertyAccessor, configRegistry);
		registerBlinkingConfigCells(configRegistry);
		insertRowData(glazedListsEventLayer, bodyDataProvider);
		
		// Column header
		final DefaultColumnHeaderDataProvider defaultColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		
		// Row header
		final DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		
		// Corner
		final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(defaultColumnHeaderDataProvider, rowHeaderDataProvider);

		// Grid
		GridLayer gridLayer = new DefaultGridLayer(
				blinkingLayer,
				new DefaultColumnHeaderDataLayer(defaultColumnHeaderDataProvider),
				new DefaultRowHeaderDataLayer(rowHeaderDataProvider),
				new DataLayer(cornerDataProvider)
		);

		NatTable natTable = new NatTable(parent, gridLayer, false);
		
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		natTable.configure();
		
		return natTable;
	}
	
	/**
	 * Start threads to fire data updates (at intervals)
	 */
	@Override
	public void onStart() {
		Display.getDefault().asyncExec( new Runnable() {
			public void run() {
				scheduledThreadPool = Executors.newScheduledThreadPool(1);
				
				// Fire updates to indexes 1,3,5
				scheduledThreadPool.scheduleAtFixedRate(new DataPumper(bodyDataProvider, 1, 3, 5), 500L, 5000L, TimeUnit.MILLISECONDS);
				
				// while they are still blinking update index 1
				scheduledThreadPool.scheduleAtFixedRate(new DataPumper(bodyDataProvider, 1), 750L, 5000L, TimeUnit.MILLISECONDS);
				
				// While the above are still blinking update indexes 2,8
				scheduledThreadPool.scheduleAtFixedRate(new DataPumper(bodyDataProvider, 2, 8), 1000L, 5000L, TimeUnit.MILLISECONDS);
			}
		});
	}
	
	@Override
	public void onStop() {
		scheduledThreadPool.shutdown();
	}

	private void registerBlinkingConfigCells(ConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(BlinkConfigAttributes.BLINK_RESOLVER, getBlinkResolver(), DisplayMode.NORMAL);

		// Bg color styles to be used for blinking cells
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_GREEN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, BLINK_UP_CONFIG_LABEL);

		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, BLINK_DOWN_CONFIG_LABEL);
	}

	/**
	 * The blinking resolver decides how the cell should blink
	 * i.e what styles should be applied depending on the update.
	 * This one returns a green color label when the value goee up, a red one otherwise.
	 */
	private IBlinkingCellResolver getBlinkResolver() {
		return new BlinkingCellResolver() {
			private String[] configLabels = new String[1];

			public String[] resolve(Object oldValue, Object newValue) {
				double old = ((Double) oldValue).doubleValue();
				double latest = ((Double) newValue).doubleValue();
				configLabels[0] = (latest > old ? BLINK_UP_CONFIG_LABEL : BLINK_DOWN_CONFIG_LABEL);
				return configLabels;
			};
		};
	}

	private void insertRowData(PropertyChangeListener changeListener, ListDataProvider<BlinkingRowDataFixture> dataProvider) {
		List<BlinkingRowDataFixture> listFixture = BlinkingRowDataFixture.getList(changeListener);
		for (BlinkingRowDataFixture rowObject : listFixture) {
			dataProvider.getList().add(rowObject);
		}
	}
	
	final Random random = new Random();
	
	/**
	 * Util class to fire periodic updates
	 */
	class DataPumper implements Runnable {
		ListDataProvider<BlinkingRowDataFixture> dataProvider;
		private final int[] rowIndexes;

		DataPumper(ListDataProvider<BlinkingRowDataFixture> dataProvider, final int... rowIndexes) {
			this.dataProvider = dataProvider;
			this.rowIndexes = rowIndexes;
		}

		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					for (int i = 0; i < rowIndexes.length; i++) {
						double nextPrice = random.nextInt(1000);
						BlinkingRowDataFixture rowObject = dataProvider.getRowObject(rowIndexes[i]);
						rowObject.setAsk_price(nextPrice);
					}
				}
			});
		}
	}
	
}
