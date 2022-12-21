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
package org.eclipse.nebula.widgets.nattable.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReflectiveColumnAccessorTest {

    private ReflectiveColumnPropertyAccessor<TestBean> accessor;

    @BeforeEach
    public void setup() {
        this.accessor = new ReflectiveColumnPropertyAccessor<>(
                new String[] { "stringField", "booleanField", "floatField" });
    }

    @Test
    public void testGetterInvocations() throws Exception {
        TestBean testBean = new TestBean("One", true, 100.00F);

        assertEquals("One", this.accessor.getDataValue(testBean, 0));
        assertEquals(Boolean.TRUE, this.accessor.getDataValue(testBean, 1));
        assertEquals(Float.valueOf(100.00f), this.accessor.getDataValue(testBean, 2));
    }

    @Test
    public void testSetterInvocations() throws Exception {
        TestBean testBean = new TestBean("One", true, 100.00F);

        this.accessor.setDataValue(testBean, 0, "Two");
        this.accessor.setDataValue(testBean, 1, false);
        this.accessor.setDataValue(testBean, 2, 42f);

        assertEquals("Two", this.accessor.getDataValue(testBean, 0));
        assertEquals(Boolean.FALSE, this.accessor.getDataValue(testBean, 1));
        assertEquals(Float.valueOf(42f), this.accessor.getDataValue(testBean, 2));
    }

    @Test
    public void testSubclassGetterAccess() {
        SubBean1 sub1 = new SubBean1("Two", true, 42f, "Bart");
        SubBean2 sub2 = new SubBean2("Three", false, 23f, "Lisa");

        assertEquals("Two", this.accessor.getDataValue(sub1, 0));
        assertEquals(Boolean.TRUE, this.accessor.getDataValue(sub1, 1));
        assertEquals(Float.valueOf(42f), this.accessor.getDataValue(sub1, 2));

        assertEquals("Three", this.accessor.getDataValue(sub2, 0));
        assertEquals(Boolean.FALSE, this.accessor.getDataValue(sub2, 1));
        assertEquals(Float.valueOf(23f), this.accessor.getDataValue(sub2, 2));
    }

    @Test
    public void testInterfaceGetterAccess() {
        ReflectiveColumnPropertyAccessor<Bean> accessor = new ReflectiveColumnPropertyAccessor<>(
                new String[] { "stringField", "booleanField", "floatField", "additionalField" });

        SubBean1 sub1 = new SubBean1("Two", true, 42f, "Bart");
        SubBean2 sub2 = new SubBean2("Three", false, 23f, "Lisa");

        assertEquals("Bart", accessor.getDataValue(sub1, 3));
        assertEquals("Lisa", accessor.getDataValue(sub2, 3));
    }

    @Test
    public void testSubclassSetterAccess() {
        SubBean1 sub1 = new SubBean1("Two", true, 42f, "Bart");
        SubBean2 sub2 = new SubBean2("Three", false, 23f, "Lisa");

        this.accessor.setDataValue(sub1, 0, "Simpson");
        this.accessor.setDataValue(sub1, 1, false);
        this.accessor.setDataValue(sub1, 2, 100f);

        this.accessor.setDataValue(sub2, 0, "Flanders");
        this.accessor.setDataValue(sub2, 1, true);
        this.accessor.setDataValue(sub2, 2, 200f);

        assertEquals("Simpson", this.accessor.getDataValue(sub1, 0));
        assertEquals(Boolean.FALSE, this.accessor.getDataValue(sub1, 1));
        assertEquals(Float.valueOf(100f), this.accessor.getDataValue(sub1, 2));

        assertEquals("Flanders", this.accessor.getDataValue(sub2, 0));
        assertEquals(Boolean.TRUE, this.accessor.getDataValue(sub2, 1));
        assertEquals(Float.valueOf(200f), this.accessor.getDataValue(sub2, 2));
    }

    @Test
    public void testInterfaceSetterAccess() {
        ReflectiveColumnPropertyAccessor<Bean> accessor = new ReflectiveColumnPropertyAccessor<>(
                new String[] { "stringField", "booleanField", "floatField", "additionalField" });

        SubBean1 sub1 = new SubBean1("Two", true, 42f, "Bart");
        SubBean2 sub2 = new SubBean2("Three", false, 23f, "Lisa");

        accessor.setDataValue(sub1, 3, "Homer");
        accessor.setDataValue(sub2, 3, "Marge");

        assertEquals("Homer", accessor.getDataValue(sub1, 3));
        assertEquals("Marge", accessor.getDataValue(sub2, 3));
    }

    class TestBean {
        private String stringField;
        private boolean booleanField;
        private float floatField;

        public TestBean(String stringField, boolean booleanField, float floatField) {
            this.stringField = stringField;
            this.booleanField = booleanField;
            this.floatField = floatField;
        }

        public String getStringField() {
            return this.stringField;
        }

        public void setStringField(String stringField) {
            this.stringField = stringField;
        }

        public boolean isBooleanField() {
            return this.booleanField;
        }

        public void setBooleanField(boolean booleanField) {
            this.booleanField = booleanField;
        }

        public float getFloatField() {
            return this.floatField;
        }

        public void setFloatField(float floatField) {
            this.floatField = floatField;
        }
    }

    interface Bean {
        String getAdditionalField();

        void setAdditionalField(String value);
    }

    class SubBean1 extends TestBean implements Bean {
        private String additionalField;

        public SubBean1(String stringField, boolean booleanField, float floatField, String additional) {
            super(stringField, booleanField, floatField);
            this.additionalField = additional;
        }

        @Override
        public String getAdditionalField() {
            return this.additionalField;
        }

        @Override
        public void setAdditionalField(String value) {
            this.additionalField = value;
        }
    }

    class SubBean2 extends TestBean implements Bean {
        private String additionalField;

        public SubBean2(String stringField, boolean booleanField, float floatField, String additional) {
            super(stringField, booleanField, floatField);
            this.additionalField = additional;
        }

        @Override
        public String getAdditionalField() {
            return this.additionalField;
        }

        @Override
        public void setAdditionalField(String value) {
            this.additionalField = value;
        }
    }
}
