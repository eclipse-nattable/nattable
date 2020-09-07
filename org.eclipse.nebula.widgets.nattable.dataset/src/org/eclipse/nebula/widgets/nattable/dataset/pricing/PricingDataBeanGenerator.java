/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
