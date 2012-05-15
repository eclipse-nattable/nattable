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
package org.eclipse.nebula.widgets.nattable.dataset.pricing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.dataset.generator.DataGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.generator.GeneratorException;


public class PricingDataBeanGenerator {

	private static DataGenerator<PricingDataBean> beanGenerator = new DataGenerator<PricingDataBean>();
	
	public static List<PricingDataBean> getData(int num) {
		List<PricingDataBean> beans = new ArrayList<PricingDataBean>();
		
		try {
			for (int i = 0; i < num; i++) {
				beans.add(beanGenerator.generate(PricingDataBean.class));
				if ((i + 1) % 1000 == 0) {
					System.out.println("Generated " + (i + 1) + " rows");
				}
			}
		} catch (GeneratorException e) {
			throw new RuntimeException(e);
		}
		return beans;
	}
}
