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
package org.eclipse.nebula.widgets.nattable.blink;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.blink.BlinkConfigAttributes;
import org.eclipse.nebula.widgets.nattable.blink.BlinkLayer;
import org.eclipse.nebula.widgets.nattable.blink.BlinkingCellResolver;
import org.eclipse.nebula.widgets.nattable.blink.IBlinkingCellResolver;
import org.eclipse.nebula.widgets.nattable.blink.UpdateEventsCache;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.BlinkingRowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

public class BlinkLayerTest {

	private static final String NOT_BLINKING_LABEL = "Not Blinking";
	private static final String BLINKING_LABEL = "Blinking";

	private BlinkLayer<BlinkingRowDataFixture> layerUnderTest;
	private final ConfigRegistry configRegistry = new ConfigRegistry();
	private List<BlinkingRowDataFixture> dataList;
	private ListDataProvider<BlinkingRowDataFixture> listDataProvider;
	private PropertyChangeListener propertyChangeListener;
	private Display display;

	@Before
	public void setUp() {
		display = Display.getDefault();
		dataList = new LinkedList<BlinkingRowDataFixture>();
		IColumnPropertyAccessor<BlinkingRowDataFixture> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<BlinkingRowDataFixture>(RowDataListFixture.getPropertyNames());
		listDataProvider = new ListDataProvider<BlinkingRowDataFixture>(dataList, columnPropertyAccessor);
		propertyChangeListener = getPropertyChangeListener();

		DataLayer dataLayer = new DataLayer(listDataProvider);
		layerUnderTest = new BlinkLayer<BlinkingRowDataFixture>(
				dataLayer,
				listDataProvider,
				BlinkingRowDataFixture.rowIdAccessor,
				columnPropertyAccessor,
				configRegistry);

		layerUnderTest.blinkingEnabled = true;

		registerBlinkConfigTypes();
		load10Rows();
	}

	@Test
	public void shouldReturnTheBlinkConfigTypeWhenARowIsUpdated() throws Exception {
		layerUnderTest.setBlinkDurationInMilis(100);

		dataList.get(0).setAsk_price(100);
		LabelStack blinkLabels = layerUnderTest.getConfigLabelsByPosition(6, 0);

		// Blink started
		assertEquals(1, blinkLabels.getLabels().size());
		assertEquals(BLINKING_LABEL, blinkLabels.getLabels().get(0));

		// After 50 ms
		Thread.sleep(50);
		blinkLabels = layerUnderTest.getConfigLabelsByPosition(6, 0);
		assertEquals(1, blinkLabels.getLabels().size());

		//Wait for blink to elapse
		Thread.sleep(110);
		// Force running the event queue to ensure any Display.asyncExecs are run.
		while(display.readAndDispatch());

		blinkLabels = layerUnderTest.getConfigLabelsByPosition(6, 0);
		assertEquals(0, blinkLabels.getLabels().size());
	}

	/**
	 * Sets the even rows to blink
	 */
	private void registerBlinkConfigTypes() {
		IBlinkingCellResolver blinkingCellResolver = new BlinkingCellResolver() {
			public String[] resolve(Object oldValue, Object newValue) {
				Double doubleValue = Double.valueOf(newValue.toString());
				return doubleValue.intValue() % 2 == 0 ? new String[] { BLINKING_LABEL } : new String[] { NOT_BLINKING_LABEL };
			}
		};

		configRegistry.registerConfigAttribute(BlinkConfigAttributes.BLINK_RESOLVER, blinkingCellResolver, DisplayMode.NORMAL);
	}

	/**
	 * Listen for updates and put them in the {@link UpdateEventsCache}.
	 * BlinkLayer needs this cache to be updated in order to work.
	 */
	private PropertyChangeListener getPropertyChangeListener() {
		return new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event) {
				PropertyUpdateEvent<BlinkingRowDataFixture> updateEvent = new PropertyUpdateEvent<BlinkingRowDataFixture>(
						new DataLayerFixture(), (BlinkingRowDataFixture)event.getSource(), event.getPropertyName(), event.getOldValue(), event.getNewValue());
				layerUnderTest.handleLayerEvent(updateEvent);
			}
		};
	}

	private void load10Rows() {
		List<BlinkingRowDataFixture> list = BlinkingRowDataFixture.getList(propertyChangeListener);
		for (BlinkingRowDataFixture blinkingRowDataFixture : list) {
			dataList.add(blinkingRowDataFixture);
		}
	}
}
