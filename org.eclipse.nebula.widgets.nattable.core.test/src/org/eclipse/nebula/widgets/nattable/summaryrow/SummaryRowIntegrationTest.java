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
package org.eclipse.nebula.widgets.nattable.summaryrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.command.CalculateSummaryRowValuesCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.PricingTypeBean;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class SummaryRowIntegrationTest {

	private ILayer layerStackWithSummary;
	private NatTableFixture natTable;
	private List<RowDataFixture> dataList;
	private SummaryRowLayer summaryRowLayer;

	private final int askPriceColumnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.ASK_PRICE_PROP_NAME);
	private final int bidPriceColumnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.BID_PRICE_PROP_NAME);
	private final int securityIdColumnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.SECURITY_ID_PROP_NAME);
	private final int lotSizeColumnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.LOT_SIZE_PROP_NAME);
	private final int ratingColumnIndex = RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.RATING_PROP_NAME);
	private DataLayer dataLayer;
	private ListDataProvider<RowDataFixture> dataProvider;

	@Before
	public void initLayerStackWithSummaryRow() {
		dataList = RowDataListFixture.getList().subList(0, 4);
		// Rows 0, 1, 2, 3; Summary row would be position 4
		assertEquals(4, dataList.size());

		dataProvider = new ListDataProvider<RowDataFixture>(
				dataList,
				new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()));

		IConfigRegistry configRegistry = new ConfigRegistry();

		dataLayer = new DataLayer(dataProvider);
		summaryRowLayer = new SummaryRowLayer(dataLayer, configRegistry);
		IUniqueIndexLayer columnReorderLayer = new ColumnReorderLayer(summaryRowLayer);
		IUniqueIndexLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		IUniqueIndexLayer selectionLayer = new SelectionLayer(columnHideShowLayer);
		layerStackWithSummary = new ViewportLayer(selectionLayer);
		
		// NatTableFixture initializes the client area
		natTable = new NatTableFixture(layerStackWithSummary, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new TestSummaryRowConfiguration());

		natTable.configure();
	}

	@Test
	public void shouldAddExtraRowAtTheEnd() throws Exception {
		assertEquals(5, natTable.getRowCount());
	}

	@Test
	public void shouldReturnDefaultValueImmediately() throws Exception {
		// First invocation triggers the summary calculation in a separate thread
		Object askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 4);
		assertNull(askPriceSummary);
	}

	@Test
	public void shouldSummarizeAskPriceColumn() throws Exception {
		// First invocation triggers the summary calculation in a separate thread
		Object askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 4);
		assertNull(askPriceSummary);

		Thread.sleep(200);

		askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 4);
		assertEquals("110.0", askPriceSummary.toString());
	}

	@Test
	public void shouldSummarizeAskPriceColumnImmediatelyOnPreCalculation() throws Exception {
		//Trigger summary calculation via command
		natTable.doCommand(new CalculateSummaryRowValuesCommand());
		
		Object askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 4);
		assertEquals("110.0", askPriceSummary.toString());
	}

	@Test
	public void defaultHandlingOfNonNumericColumns() throws Exception {
		// Non numeric field
		Object isinSummary = natTable.getDataValueByPosition(securityIdColumnIndex, 4);
		assertNull(isinSummary);

		// Summary provider turned off
		Object bidPriceSummary = natTable.getDataValueByPosition(bidPriceColumnIndex, 4);
		assertNull(bidPriceSummary);
	}

	@Test
	public void shouldFireCellVisualChangeEventOnceSummaryIsCalculated() throws Exception {
		//need to resize because otherwise the ViewportLayer would not process the CellVisualChangeEvent any further
		natTable.setSize(800, 400);
		
		LayerListenerFixture listener = new LayerListenerFixture();
		natTable.addLayerListener(listener);

		// Trigger summary calculation
		natTable.getDataValueByPosition(askPriceColumnIndex, 4);

		Thread.sleep(500);

		assertTrue(listener.containsInstanceOf(CellVisualChangeEvent.class));
		CellVisualChangeEvent event = (CellVisualChangeEvent) listener.getReceivedEvents().get(0);

		assertEquals(askPriceColumnIndex, event.getColumnPosition());
		assertEquals(4, event.getRowPosition());
		
		
		Collection<Rectangle> changedPositionRectangles = event.getChangedPositionRectangles();
		assertEquals(1, changedPositionRectangles.size());

		//only the cell gets updated
		Rectangle rectangle = changedPositionRectangles.iterator().next();
		assertEquals(6, rectangle.x);
		assertEquals(4, rectangle.y);
		assertEquals(1, rectangle.width);
		assertEquals(1, rectangle.height);
	}

	@Test
	public void rowAddShouldClearCacheAndCalculateNewSummary() throws Exception {
		// Trigger summary calculation
		Object askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 4);
		assertNull(askPriceSummary);

		Thread.sleep(100);

		// Verify calculated summary value
		askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 4);
		assertEquals("110.0", askPriceSummary.toString());

		// Add data and fire event
		dataList.add(new RowDataFixture("SID", "SDesc", "A", new Date(), new PricingTypeBean("MN"), 2.0, 2.1, 100, true, 3.0, 1.0, 1.0, 1000, 100000, 50000));
		dataLayer.fireLayerEvent(new RowInsertEvent(dataLayer, 4));

		// Trigger summary calculation - on the new summary row
		askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 5);
		assertEquals("110.0", askPriceSummary.toString());

		Thread.sleep(100);

		// Verify summary value is REcalculated
		askPriceSummary = natTable.getDataValueByPosition(askPriceColumnIndex, 5);
		assertEquals("112.1", askPriceSummary.toString());
	}

	@Test
	public void getRowIndexByPositionForSummaryRow() throws Exception {
		assertEquals(4, natTable.getRowIndexByPosition(4));
	}

	@Test
	public void getRowHeightByPositionForSummaryRow() throws Exception {
		natTable.doCommand(new RowResizeCommand(natTable, 4, 100));
		assertEquals(100, natTable.getRowHeightByPosition(4));
	}

	@Test
	public void defaultConfigLabelsAreApplied() throws Exception {
		LabelStack configLabels = natTable.getConfigLabelsByPosition(0, 4);
		List<String> labels = configLabels.getLabels();

		assertEquals(2, labels.size());
		assertEquals(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 0, labels.get(0));
		assertEquals(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL, labels.get(1));
	}

	@Test
	public void defaultConfigLabelsAreAdded() throws Exception {
		ColumnOverrideLabelAccumulator labelAcc = new ColumnOverrideLabelAccumulator(layerStackWithSummary);
		labelAcc.registerColumnOverrides(0, "myLabel");
		((ViewportLayer)layerStackWithSummary).setConfigLabelAccumulator(labelAcc);

		LabelStack configLabels = natTable.getConfigLabelsByPosition(0, 4);
		List<String> labels = configLabels.getLabels();

		assertEquals(3, labels.size());
		assertEquals(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 0, labels.get(0));
		assertEquals(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL, labels.get(1));
		assertEquals("myLabel", labels.get(2));
	}

	@Test
	public void defaultConfigLabelsNotAddedForLayersBelow() throws Exception {
		//the AbstractOverrider is set on the DataLayer. So on retrieving the 
		dataLayer.setConfigLabelAccumulator(new AbstractOverrider() {
			@Override
			public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
				RowDataFixture rowObject = dataProvider.getRowObject(rowPosition);
				configLabels.addLabel("myLabel " + rowObject.security_id);
			}
		});

		LabelStack configLabels = natTable.getConfigLabelsByPosition(0, 4);
		List<String> labels = configLabels.getLabels();

		assertEquals(2, labels.size());
		assertEquals(SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + 0, labels.get(0));
		assertEquals(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL, labels.get(1));

		configLabels = natTable.getConfigLabelsByPosition(0, 3);
		labels = configLabels.getLabels();

		assertEquals(1, labels.size());
		assertTrue("Label in default body does not start with myLabel", labels.get(0).startsWith("myLabel"));
	}

	@Test
	public void shouldSumUpAllRowsWithAAARating() throws Exception {
		// Trigger summary calculation
		Object lotSizeSummary = natTable.getDataValueByPosition(lotSizeColumnIndex, 4);
		assertNull(lotSizeSummary);

		Thread.sleep(100);

		// Verify calculated summary value
		lotSizeSummary = natTable.getDataValueByPosition(lotSizeColumnIndex, 4);
		assertEquals("10000", lotSizeSummary.toString());
	}

	/**
	 * Sets up:
	 *  Summary for the ask price
	 *  No summary for the others
	 */
	class TestSummaryRowConfiguration extends DefaultSummaryRowConfiguration {
		@Override
		public void addSummaryProviderConfig(IConfigRegistry configRegistry) {
			// Add summaries to ask price column
			configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER,
					new SummationSummaryProvider(dataProvider),
					DisplayMode.NORMAL,
					SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + askPriceColumnIndex);

			// Add lot size if the rating is 'AAA'
			configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER,
					getTestLotSizeSummaryProvider(),
					DisplayMode.NORMAL,
					SummaryRowLayer.DEFAULT_SUMMARY_COLUMN_CONFIG_LABEL_PREFIX + lotSizeColumnIndex);

			// No Summary by default
			configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER,
					ISummaryProvider.NONE,
					DisplayMode.NORMAL,
					SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
		}

		private ISummaryProvider getTestLotSizeSummaryProvider() {
			return new ISummaryProvider() {
				@Override
				public Object summarize(int columnIndex) {
					int lotSizeSummary = 0;
					int rowCount = dataProvider.getRowCount();

					for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
						String lotSize = dataProvider.getDataValue(columnIndex, rowIndex).toString();
						String rating = dataProvider.getDataValue(ratingColumnIndex, rowIndex).toString();

						if("AAA".equals(rating)){
							lotSizeSummary = lotSizeSummary + Integer.parseInt(lotSize);
						}
					}
					return lotSizeSummary;
				}
			};
		}
	}
}
