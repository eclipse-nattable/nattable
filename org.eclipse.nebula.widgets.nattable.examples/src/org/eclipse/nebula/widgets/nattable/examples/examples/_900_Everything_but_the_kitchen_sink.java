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
package org.eclipse.nebula.widgets.nattable.examples.examples;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.apache.commons.lang.math.RandomUtils;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.blink.BlinkConfigAttributes;
import org.eclipse.nebula.widgets.nattable.blink.BlinkingCellResolver;
import org.eclipse.nebula.widgets.nattable.blink.IBlinkingCellResolver;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.PersistentNatExampleWrapper;
import org.eclipse.nebula.widgets.nattable.examples.examples._110_Editing.EditableGridExample;
import org.eclipse.nebula.widgets.nattable.examples.examples._131_Filtering.FilterRowGridExample;
import org.eclipse.nebula.widgets.nattable.examples.examples._150_Column_and_row_grouping._300_Summary_row;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.FullFeaturedBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.examples.fixtures.FullFeaturedColumnHeaderLayerStack;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultSummaryRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.BlinkingRowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.widget.WaitDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;

public class _900_Everything_but_the_kitchen_sink extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(1000, 800, new PersistentNatExampleWrapper(new _900_Everything_but_the_kitchen_sink()));
	}
	
	@Override
	public String getDescription() {
		return "The example with everything. Well, not quite everything, but a lot.";
	}

	private static final String BLINK_UP_CONFIG_LABEL = "blinkUpConfigLabel";
	private static final String BLINK_DOWN_CONFIG_LABEL = "blinkDownConfigLabel";

	private EventList<BlinkingRowDataFixture> baseEventList;
	private PropertyChangeListener propertyChangeListener;
	private ListDataProvider<BlinkingRowDataFixture> bodyDataProvider;
	private NatTable natTable;
	private ScheduledExecutorService scheduledThreadPool;

	public Control createExampleControl(Composite parent) {
		final String[] propertyNames = RowDataListFixture.getPropertyNames();
		final Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();

		ConfigRegistry configRegistry = new ConfigRegistry();
		ColumnGroupModel columnGroupModel = new ColumnGroupModel();

		// Body

		LinkedList<BlinkingRowDataFixture> rowData = new LinkedList<BlinkingRowDataFixture>();
		baseEventList = GlazedLists.threadSafeList(GlazedLists.eventList(rowData));
		ObservableElementList<BlinkingRowDataFixture> observableElementList = new ObservableElementList<BlinkingRowDataFixture>(
				baseEventList, GlazedLists.beanConnector(BlinkingRowDataFixture.class));
		FilterList<BlinkingRowDataFixture> filterList = new FilterList<BlinkingRowDataFixture>(observableElementList);
		SortedList<BlinkingRowDataFixture> sortedList = new SortedList<BlinkingRowDataFixture>(filterList, null);

		FullFeaturedBodyLayerStack<BlinkingRowDataFixture> bodyLayer =
			new FullFeaturedBodyLayerStack<BlinkingRowDataFixture>(
				sortedList,
				BlinkingRowDataFixture.rowIdAccessor,
				propertyNames,
				configRegistry,
				columnGroupModel);

		bodyDataProvider = bodyLayer.getBodyDataProvider();
		propertyChangeListener = bodyLayer.getGlazedListEventsLayer();

		//    blinking
		registerBlinkingConfigCells(configRegistry);

		// Column header
		FullFeaturedColumnHeaderLayerStack<BlinkingRowDataFixture> columnHeaderLayer =
			new FullFeaturedColumnHeaderLayerStack<BlinkingRowDataFixture>(
	              sortedList,
	              filterList,
	              propertyNames,
	              propertyToLabelMap,
	              bodyLayer,
	              bodyLayer.getSelectionLayer(),
	              columnGroupModel,
	              configRegistry);

		//    column groups
		setUpColumnGroups(columnHeaderLayer);

		// Row header
		final DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultSummaryRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		rowHeaderDataLayer.setDefaultColumnWidth(50);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Corner
		final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderLayer.getColumnHeaderDataProvider(), rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

		// Grid
		GridLayer gridLayer = new GridLayer(
				bodyLayer,
				columnHeaderLayer,
				rowHeaderLayer,
				cornerLayer
		);

		natTable = new NatTable(parent, gridLayer, false);

		natTable.setConfigRegistry(configRegistry);

		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		//    Popup menu
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable).withColumnChooserMenuItem();
			}
		});
		
		natTable.addConfiguration(new SingleClickSortConfiguration());

		//    Editing
		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyLayer.getBodyDataLayer());
		bodyLayer.getBodyDataLayer().setConfigLabelAccumulator(columnLabelAccumulator);
		natTable.addConfiguration(EditableGridExample.editableGridConfiguration(columnLabelAccumulator, bodyDataProvider));
		natTable.addConfiguration(new FilterRowGridExample.FilterRowCustomConfiguration());

		
		//    Column chooser
		DisplayColumnChooserCommandHandler columnChooserCommandHandler = new DisplayColumnChooserCommandHandler(
				bodyLayer.getSelectionLayer(),
				bodyLayer.getColumnHideShowLayer(),
				columnHeaderLayer.getColumnHeaderLayer(),
				columnHeaderLayer.getColumnHeaderDataLayer(),
				columnHeaderLayer.getColumnGroupHeaderLayer(),
				columnGroupModel);
		bodyLayer.registerCommandHandler(columnChooserCommandHandler);

		// Summary row configuration
		natTable.addConfiguration(new _300_Summary_row<BlinkingRowDataFixture>(bodyDataProvider));
		natTable.configure();

		return natTable;
	}

	private void setUpColumnGroups(FullFeaturedColumnHeaderLayerStack<BlinkingRowDataFixture> headerLayer) {
		headerLayer.getColumnGroupHeaderLayer().addColumnsIndexesToGroup("TestGroup", 1,2);
		headerLayer.getColumnGroupHeaderLayer().addColumnsIndexesToGroup("TestGroup1", 5,6,7);
		headerLayer.getColumnGroupHeaderLayer().setGroupUnbreakable(5);
		headerLayer.getColumnGroupHeaderLayer().setGroupAsCollapsed(5);
	}

	@Override
	public void onStart() {
		Display.getDefault().asyncExec( new Runnable() {
			public void run() {
				scheduledThreadPool = Executors.newScheduledThreadPool(1);
				System.out.println("Starting data load.");
				scheduledThreadPool.schedule(new DataLoader(propertyChangeListener, baseEventList), 100L, TimeUnit.MILLISECONDS);
				scheduledThreadPool.scheduleAtFixedRate(new DataUpdater(bodyDataProvider), 100L, 5000L, TimeUnit.MILLISECONDS);
			}
		});
	}

	@Override
	public void onStop() {
		if (isNotNull(scheduledThreadPool)) {
			scheduledThreadPool.shutdownNow();
		}
	}

	private void registerBlinkingConfigCells(ConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(BlinkConfigAttributes.BLINK_RESOLVER, getBlinkResolver(), DisplayMode.NORMAL);

		// Styles
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_GREEN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, BLINK_UP_CONFIG_LABEL);

		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_RED);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, BLINK_DOWN_CONFIG_LABEL);
	}

	private IBlinkingCellResolver getBlinkResolver() {
		return new BlinkingCellResolver() {
			private final String[] configLabels = new String[1];

			public String[] resolve(Object oldValue, Object newValue) {
				double old = ((Double) oldValue).doubleValue();
				double latest = ((Double) newValue).doubleValue();
				configLabels[0] = (latest > old ? BLINK_UP_CONFIG_LABEL : BLINK_DOWN_CONFIG_LABEL);
				return configLabels;
			}
		};
	}

	private final Random random = new Random();
	private static final int DATASET_SIZE = 52000;

	/**
	 * Initial data load. Loads data in batches.
	 *
	 *	Note: It is a significantly more efficient to do the load in batches
	 * 	i.e using {@link List#addAll(java.util.Collection)} instead of adding objects
	 * 	individually.
	 *
	 *  @See https://sourceforge.net/projects/nattable/forums/forum/744994/topic/3410065
	 */
	class DataLoader implements Runnable {
		private final PropertyChangeListener changeListener;
		private final EventList<BlinkingRowDataFixture> list;
		private final String waitMsg = "Loading data. Please wait... ";
		private WaitDialog dialog;

		public DataLoader(PropertyChangeListener changeListener, EventList<BlinkingRowDataFixture> baseEventList) {
			this.changeListener = changeListener;
			this.list = baseEventList;
		}

		public void run() {
			try {
				// Display wait dialog
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						final Image icon = new Image(Display.getDefault(), getClass().getResourceAsStream("waiting.gif"));
						dialog = new WaitDialog(natTable.getShell(), SWT.NONE, waitMsg, icon);
						dialog.setBlockOnOpen(true);
						dialog.open();
					}
				});

				// Load data
				while(list.size() < DATASET_SIZE){
					//	Add to buffer
					List<BlinkingRowDataFixture> buffer = new ArrayList<BlinkingRowDataFixture>();
					for (int i = 0; i < 100; i++) {
						buffer.addAll(BlinkingRowDataFixture.getList(changeListener));
					}
					//	Load as a batch
					list.addAll(buffer);
					//	Update wait dialog
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							dialog.setMsg(waitMsg + list.size());
						}
					});
				}

				// Close wait dialog
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						dialog.close();
					}
				});
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	public Runnable runWithBusyIndicator(final Runnable runnable) {
		return new Runnable() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						BusyIndicator.showWhile(Display.getDefault(), runnable);
					}
				});
			}
		};
	}

	/**
	 * Pumps data updates to drive blinking
	 */
	class DataUpdater implements Runnable {
		ListDataProvider<BlinkingRowDataFixture> dataProvider;
		int counter = 0;
		DataUpdater(ListDataProvider<BlinkingRowDataFixture> dataProvider) {
			this.dataProvider = dataProvider;
		}
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					for (int i = 0; i < 5; i++) {
						int index = RandomUtils.nextInt(13);
						int nextAsk = random.nextInt(1000);

						if (dataProvider.getRowCount() > index) {
    						BlinkingRowDataFixture rowObject = dataProvider.getRowObject(index);
    						//System.out.println("Ask: "+rowObject.getAsk_price()+" --> "+nextAsk);
    						rowObject.setAsk_price(nextAsk);
    						rowObject.setBid_price(-1 * nextAsk);
						}
					}
				}
			});
		}
	}

}
