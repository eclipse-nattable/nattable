/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.After;
import org.junit.Test;

/**
 * @author Dirk Fauth
 *
 */
public class CalculatedValueCacheTest {

	CalculatedValueCache valueCache;
	
	ICalculator calculator = new ICalculator() {
		
		@Override
		public Object executeCalculation() {

			//simply add some delay
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			
			return Integer.valueOf(42);
		}
	};
	
	@Test
	public void testCalculateInBackgroundWithCoordsKey() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, true);

		Object result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as calculation is performed in background, the immediate return value is null
		assertNull(result);
		//now wait so the background process is able to finish 
		Thread.sleep(210);
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test
	public void testCalculateInSameThreadWithCoordsKey() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, true);

		Object result = this.valueCache.getCalculatedValue(0, 0, false, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test
	public void testCalculateInBackgroundWithCoordsKeyClearSmooth() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, true);

		Object result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as calculation is performed in background, the immediate return value is null
		assertNull(result);
		//now wait so the background process is able to finish 
		Thread.sleep(210);
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
		
		this.valueCache.clearCache();
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as the cache is configured for smooth updates, the value should be still there
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test
	public void testCalculateInBackgroundWithCoordsKeyClearNonSmooth() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, true, false);

		Object result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as calculation is performed in background, the immediate return value is null
		assertNull(result);
		//now wait so the background process is able to finish 
		Thread.sleep(210);
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
		
		this.valueCache.clearCache();
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as the cache is configured for non smooth updates, the value should be null again
		assertNull(result);
	}
	
	@Test
	public void testCalculateInBackgroundWithCoordsKeyKillSmooth() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, true);

		Object result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as calculation is performed in background, the immediate return value is null
		assertNull(result);
		//now wait so the background process is able to finish 
		Thread.sleep(210);
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
		
		this.valueCache.killCache();
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//killing the cache should have the same effect as clearing non smooth
		assertNull(result);
	}
	
	
	@Test
	public void testCalculateInBackgroundWithColumnKey() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, false);

		Object result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as calculation is performed in background, the immediate return value is null
		assertNull(result);
		//now wait so the background process is able to finish 
		Thread.sleep(210);
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test
	public void testCalculateInSameThreadWithColumnKey() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), true, false);

		Object result = this.valueCache.getCalculatedValue(0, 0, false, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test
	public void testCalculateInBackgroundWithRowKey() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), false, true);

		Object result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		//as calculation is performed in background, the immediate return value is null
		assertNull(result);
		//now wait so the background process is able to finish 
		Thread.sleep(210);
		result = this.valueCache.getCalculatedValue(0, 0, true, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test
	public void testCalculateInSameThreadWithRowKey() throws Exception {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), false, true);

		Object result = this.valueCache.getCalculatedValue(0, 0, false, calculator);
		assertEquals(Integer.valueOf(42), Integer.valueOf(result.toString()));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testIllegalState() {
		this.valueCache = new CalculatedValueCache(new DataLayer(new DummyBodyDataProvider(10, 10)), false, false);
		this.valueCache.getCalculatedValue(0, 0, false, calculator);
	}
	
	@After
	public void tearDown() {
		this.valueCache.dispose();
	}

	//TODO test cache clearing
	//TODO test cache kill
	//TODO test smooth updates
}
