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
package org.eclipse.nebula.widgets.nattable.data;

import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReflectiveColumnAccessorTest {

    private TestBean testBean1;
    private ReflectiveColumnPropertyAccessor<TestBean> accessor;

    @Before
    public void setup() {
        this.testBean1 = new TestBean("One", true, 100.00F);

        this.accessor = new ReflectiveColumnPropertyAccessor<TestBean>(new String[] {
                "stringField", "booleanField", "floatField" });
    }

    @Test
    public void getterInvocations() throws Exception {
        Assert.assertEquals("One", this.accessor.getDataValue(this.testBean1, 0));
        Assert.assertEquals(Boolean.TRUE, this.accessor.getDataValue(this.testBean1, 1));
        Assert.assertEquals(Float.valueOf(100.00f),
                this.accessor.getDataValue(this.testBean1, 2));
    }

    class TestBean {
        private String stringField;
        private boolean booleanField;
        private float floatField;

        public TestBean(String stringField, boolean booleanField,
                float floatField) {
            this.stringField = stringField;
            this.booleanField = booleanField;
            this.floatField = floatField;
        }

        public String getStringField() {
            return this.stringField;
        }

        public boolean isBooleanField() {
            return this.booleanField;
        }

        public float getFloatField() {
            return this.floatField;
        }
    }
}
