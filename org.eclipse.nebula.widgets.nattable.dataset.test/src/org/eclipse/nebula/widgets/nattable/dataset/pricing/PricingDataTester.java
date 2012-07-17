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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PricingDataTester {

	@Test
	public void readUsingTabDelimReader() throws Exception {
		final DelimitedFileReader reader = new DelimitedFileReader(new BufferedReader(new InputStreamReader(getClass()
				.getResourceAsStream("pricing_data.txt"))), '\t');
		try {
			if (reader.ready() && reader.markSupported()) {
				while (reader.read() >= 0) {
					final StringTokenizer tabbedData = reader.getTabbedLineRead();
					Assert.assertEquals(18, tabbedData.countTokens());
				}
			}
		} finally {
			reader.close();
		}
	}

	@Test
	public void loadAPSBeans() throws Exception {
		final PricingDataFileLoader<PricingDataBean> gen = new PricingDataFileLoader<PricingDataBean>();
		final List<PricingDataBean> data = gen.loadDataFromFile();
		Assert.assertEquals(46, data.size());
		Assert.assertEquals("USA 4 15FEB15".trim(), data.get(28).getIsin());
	}
}
