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
package org.eclipse.nebula.widgets.nattable.paste;

import org.junit.Assert;
import org.junit.Test;


//TODO -- Uncomment and enable tests so when we figure out how to make cglib work.
public class NatTableBulkUpdateSupportTest {
//	private static final int TOTAL = 10;
	
//	private DataUpdateHelper<PricingDataBean> helper;
//	private List<Serializable> rowIds;
//
//	@Before
//	public void init() {
//		ContentConfigRegistry contentConfigRegistry = new ContentConfigRegistry();
//		contentConfigRegistry.registerValidator("alias", new IDataValidator() {
//
//			public boolean validate(Object oldValue, Object newValue) {
//				return ((Double) newValue).doubleValue() < 20;
//			}
//
//		});		
//		helper = DataUpdateHelperCreator.getUpdateHelper(contentConfigRegistry, ColumnHeaders.getPropertyNamesToTypeMap(), "isin", PricingDataBean.class);
//	}
//	
//	@Test @Ignore
//	public void startBulkUpdateTransaction() {
//
//		DefaultBulkUpdateSupport<PricingDataBean> bulkUpdater = (DefaultBulkUpdateSupport<PricingDataBean>)helper.getBulkUpdate();
//		List<PricingDataBean> existingData = PricingDataBeanGenerator.getData(TOTAL * TOTAL);		
//
//		// compile data
//		createUpdateListFromClipboard(bulkUpdater);
//		// add half of the copied data to the list, other half will be inserts		
//		for (int i = 0; i < TOTAL / 2; i++) {
//			PricingDataBean bean = existingData.get(i);
//			bean.setIsin((String) rowIds.get(i));
//		}
//
//		
//		// simulates obtaining lock on underlying list prior to committing
//		// updates
//		synchronized (existingData) {
//			bulkUpdater.commitUpdates(existingData, helper);
//		}
//		// assert the list contains the inserts
//		Assert.assertEquals(TOTAL * TOTAL + TOTAL / 2, existingData.size());
//	}
//
//	private void createUpdateListFromClipboard(IBulkUpdateSupport<PricingDataBean> updater) {
//		// describe which properties to paste into
//		List<String> props = new ArrayList<String>();
//		props.add("isin");
//		props.add("pricingModel");
//		props.add("ask");
//
//		List<PricingDataBean> gridData = PricingDataBeanGenerator.getData(TOTAL);
//		rowIds = new ArrayList<Serializable>();
//		for (PricingDataBean bean : gridData) {
//			List<Object> cells = new ArrayList<Object>();
//			Serializable id = (Serializable) bean.getIsin();
//			rowIds.add(id);
//			cells.add(id);
//			cells.add(bean.getPricingModel());
//			cells.add(Double.valueOf(bean.getAsk()));
//			updater.addUpdates(id, cells, props, helper);
//		}
//	}
	
	@Test
	public void dummyTest() {
		Assert.assertTrue(true);
	}
}
