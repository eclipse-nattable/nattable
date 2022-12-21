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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.DataValueGenerator;
import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;
import org.junit.jupiter.api.Test;

public class DataValueGeneratorTest {

    @Test
    public void testClassAnnotationIsAccessible() {
        PricingDataBean bean = new PricingDataBean();

        boolean foundAnnotation = false;
        for (Field field : bean.getClass().getDeclaredFields()) {

            foundAnnotation |= field
                    .isAnnotationPresent(DataValueGenerator.class);
        }

        assertTrue(foundAnnotation);
    }

    // TODO The following test probably has a race condition - test
    // non-deterministic
    @Test
    public void testClassAnnotationDataCanBeAccessed() throws Exception {
        final Random random = new Random();
        final PricingDataBean bean = new PricingDataBean();

        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(DataValueGenerator.class)) {
                field.setAccessible(true);
                try {
                    Class<? extends IValueGenerator> generatorClass = field
                            .getAnnotation(DataValueGenerator.class).value();
                    IValueGenerator generator = generatorClass.newInstance();
                    Object newValue = generator.newValue(random);
                    // System.out.println("newValue: "+newValue + "\t" +
                    // generator.getClass().getName());
                    assertNotNull(newValue);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
