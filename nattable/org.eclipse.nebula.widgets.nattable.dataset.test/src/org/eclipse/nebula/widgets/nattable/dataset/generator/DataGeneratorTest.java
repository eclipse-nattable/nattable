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
package org.eclipse.nebula.widgets.nattable.dataset.generator;

import org.eclipse.nebula.widgets.nattable.dataset.generator.DataGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.generator.GeneratorException;
import org.eclipse.nebula.widgets.nattable.dataset.pricing.PricingDataBean;
import org.junit.Assert;
import org.junit.Test;


public class DataGeneratorTest {

	@Test
	public void testGenerate() {
		try {
			PricingDataBean pricingDataBean = new DataGenerator<PricingDataBean>().generate(PricingDataBean.class);
			
			Assert.assertNotNull(pricingDataBean);
			Assert.assertTrue(pricingDataBean.getBid() >= 0.0d);
			Assert.assertTrue(pricingDataBean.getBid() < 10000.0d);
			
			Assert.assertTrue(pricingDataBean.getAsk() >= 0.0d);
			Assert.assertTrue(pricingDataBean.getAsk() < 10000.0d);
			
			Assert.assertTrue(pricingDataBean.getClosingPrice() >= 0.0d);
			Assert.assertTrue(pricingDataBean.getClosingPrice() < 10000.0d);
			
			Assert.assertNotNull(pricingDataBean.getPricingSource());
		} catch (GeneratorException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage() + " : " + e.getCause().getMessage());
		}
	}

}
