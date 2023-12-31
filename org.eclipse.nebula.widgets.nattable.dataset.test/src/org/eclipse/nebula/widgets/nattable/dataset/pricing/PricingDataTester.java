/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class PricingDataTester {

    @Test
    public void readUsingTabDelimReader() throws Exception {
        final DelimitedFileReader reader = new DelimitedFileReader(
                new BufferedReader(new InputStreamReader(getClass()
                        .getResourceAsStream("pricing_data.txt"))),
                '\t');
        try {
            if (reader.ready() && reader.markSupported()) {
                while (reader.read() >= 0) {
                    final StringTokenizer tabbedData = reader
                            .getTabbedLineRead();
                    assertEquals(18, tabbedData.countTokens());
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
        assertEquals(46, data.size());
        assertEquals("USA 4 15FEB15".trim(), data.get(28).getIsin());
    }
}
