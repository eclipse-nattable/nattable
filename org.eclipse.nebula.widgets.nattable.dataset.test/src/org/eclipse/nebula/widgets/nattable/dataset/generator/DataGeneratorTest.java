/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.generator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.nebula.widgets.nattable.dataset.pricing.PricingDataBean;
import org.junit.Test;

public class DataGeneratorTest {

    @Test
    public void testGenerate() {
        try {
            PricingDataBean pricingDataBean = new DataGenerator<PricingDataBean>()
                    .generate(PricingDataBean.class);

            assertNotNull(pricingDataBean);
            assertTrue(pricingDataBean.getBid() >= 0.0d);
            assertTrue(pricingDataBean.getBid() < 10000.0d);

            assertTrue(pricingDataBean.getAsk() >= 0.0d);
            assertTrue(pricingDataBean.getAsk() < 10000.0d);

            assertTrue(pricingDataBean.getClosingPrice() >= 0.0d);
            assertTrue(pricingDataBean.getClosingPrice() < 10000.0d);

            assertNotNull(pricingDataBean.getPricingSource());
        } catch (GeneratorException e) {
            e.printStackTrace();
            fail(e.getMessage() + " : " + e.getCause().getMessage());
        }
    }

}
