/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.eclipse.nebula.widgets.nattable.dataset.person.Address;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExtendedReflectiveColumnPropertyAccessorTest {

    private ExtendedReflectiveColumnPropertyAccessor<PersonWithAddress> accessor;
    private PersonWithAddress person;

    private ExtendedReflectiveColumnPropertyAccessor<Bean> beanAccessor;
    private Bean testBean;

    @BeforeEach
    public void setup() {
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday",
                "address.street",
                "address.housenumber",
                "address.postalCode",
                "address.city", };

        this.accessor = new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        Address address = new Address();
        address.setStreet("Some Street");
        address.setHousenumber(42);
        address.setPostalCode(12345);
        address.setCity("In the clouds");
        this.person = new PersonWithAddress(42, "Ralph", "Wiggum", Gender.MALE, false, new Date(), address);

        String[] beanPropertyNames = {
                "name",
                "bean.active",
                "bean.sub.value",
                "bean.sub.byteAmount",
                "bean.sub.shortAmount",
                "bean.sub.intAmount",
                "bean.sub.longAmount",
                "bean.sub.floatAmount",
                "bean.sub.doubleAmount",
                "bean.sub.charAmount" };

        this.beanAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(beanPropertyNames);

        SubSubBean subSub = new SubSubBean();
        subSub.value = "SubSubBean";
        subSub.byteAmount = 2;
        subSub.shortAmount = 12;
        subSub.intAmount = 500;
        subSub.longAmount = 30000l;
        subSub.floatAmount = 23f;
        subSub.doubleAmount = 42d;
        subSub.charAmount = 'S';

        SubBean sub = new SubBean();
        sub.active = true;
        sub.sub = subSub;

        this.testBean = new Bean();
        this.testBean.name = "Multi-Hierarchical-Test";
        this.testBean.bean = sub;
    }

    @Test
    public void shouldGetFirstLevelProperty() {
        assertEquals("Ralph", this.accessor.getDataValue(this.person, 0));
        assertEquals("Wiggum", this.accessor.getDataValue(this.person, 1));
        assertEquals(Gender.MALE, this.accessor.getDataValue(this.person, 2));
        assertEquals(Boolean.FALSE, this.accessor.getDataValue(this.person, 3));
    }

    @Test
    public void shouldGetSecondLevelProperty() {
        assertEquals("Some Street", this.accessor.getDataValue(this.person, 5));
        assertEquals(42, this.accessor.getDataValue(this.person, 6));
        assertEquals(12345, this.accessor.getDataValue(this.person, 7));
        assertEquals("In the clouds", this.accessor.getDataValue(this.person, 8));
    }

    @Test
    public void shouldSetFirstLevelProperty() {
        this.accessor.setDataValue(this.person, 0, "Sarah");
        this.accessor.setDataValue(this.person, 2, Gender.FEMALE);
        this.accessor.setDataValue(this.person, 3, Boolean.TRUE);

        assertEquals("Sarah", this.accessor.getDataValue(this.person, 0));
        assertEquals("Wiggum", this.accessor.getDataValue(this.person, 1));
        assertEquals(Gender.FEMALE, this.accessor.getDataValue(this.person, 2));
        assertEquals(Boolean.TRUE, this.accessor.getDataValue(this.person, 3));
    }

    @Test
    public void shouldSetFirstLevelPropertyToNull() {
        // set value to null
        this.accessor.setDataValue(this.person, 0, null);

        assertNull(this.accessor.getDataValue(this.person, 0));
        assertEquals("Wiggum", this.accessor.getDataValue(this.person, 1));
        assertEquals(Gender.MALE, this.accessor.getDataValue(this.person, 2));
        assertEquals(Boolean.FALSE, this.accessor.getDataValue(this.person, 3));

        // change null value again
        this.accessor.setDataValue(this.person, 0, "Clancy");

        assertEquals("Clancy", this.accessor.getDataValue(this.person, 0));
        assertEquals("Wiggum", this.accessor.getDataValue(this.person, 1));
        assertEquals(Gender.MALE, this.accessor.getDataValue(this.person, 2));
        assertEquals(Boolean.FALSE, this.accessor.getDataValue(this.person, 3));
    }

    @Test
    public void shouldSetSecondLevelProperty() {
        this.accessor.setDataValue(this.person, 5, "Evergreen Terrace");

        assertEquals("Evergreen Terrace", this.accessor.getDataValue(this.person, 5));
        assertEquals(42, this.accessor.getDataValue(this.person, 6));
        assertEquals(12345, this.accessor.getDataValue(this.person, 7));
        assertEquals("In the clouds", this.accessor.getDataValue(this.person, 8));
    }

    @Test
    public void shouldSetSecondLevelPropertyToNull() {
        // set value to null
        this.accessor.setDataValue(this.person, 5, null);

        assertNull(this.accessor.getDataValue(this.person, 5));
        assertEquals(42, this.accessor.getDataValue(this.person, 6));
        assertEquals(12345, this.accessor.getDataValue(this.person, 7));
        assertEquals("In the clouds", this.accessor.getDataValue(this.person, 8));

        // change null value again
        this.accessor.setDataValue(this.person, 5, "Evergreen Terrace");

        assertEquals("Evergreen Terrace", this.accessor.getDataValue(this.person, 5));
        assertEquals(42, this.accessor.getDataValue(this.person, 6));
        assertEquals(12345, this.accessor.getDataValue(this.person, 7));
        assertEquals("In the clouds", this.accessor.getDataValue(this.person, 8));
    }

    @Test
    public void shouldGetValuesFromMultiHierarchial() {
        assertEquals("Multi-Hierarchical-Test", this.beanAccessor.getDataValue(this.testBean, 0));
        assertEquals(Boolean.TRUE, this.beanAccessor.getDataValue(this.testBean, 1));
        assertEquals("SubSubBean", this.beanAccessor.getDataValue(this.testBean, 2));
        assertEquals((byte) 2, this.beanAccessor.getDataValue(this.testBean, 3));
        assertEquals((short) 12, this.beanAccessor.getDataValue(this.testBean, 4));
        assertEquals(500, this.beanAccessor.getDataValue(this.testBean, 5));
        assertEquals(30000l, this.beanAccessor.getDataValue(this.testBean, 6));
        assertEquals(23f, this.beanAccessor.getDataValue(this.testBean, 7));
        assertEquals(42d, this.beanAccessor.getDataValue(this.testBean, 8));
        assertEquals('S', this.beanAccessor.getDataValue(this.testBean, 9));
    }

    @Test
    public void shouldSetValuesInMultiHierarchial() {
        this.beanAccessor.setDataValue(this.testBean, 0, "Blubb");
        this.beanAccessor.setDataValue(this.testBean, 1, Boolean.FALSE);
        this.beanAccessor.setDataValue(this.testBean, 2, "Dingens");
        this.beanAccessor.setDataValue(this.testBean, 3, Byte.valueOf("7"));
        this.beanAccessor.setDataValue(this.testBean, 4, Short.valueOf("23"));
        this.beanAccessor.setDataValue(this.testBean, 5, Integer.valueOf("1000"));
        this.beanAccessor.setDataValue(this.testBean, 6, Long.valueOf("250000"));
        this.beanAccessor.setDataValue(this.testBean, 7, Float.valueOf("0.5"));
        this.beanAccessor.setDataValue(this.testBean, 8, Double.valueOf("3.14"));
        this.beanAccessor.setDataValue(this.testBean, 9, 'X');

        assertEquals("Blubb", this.beanAccessor.getDataValue(this.testBean, 0));
        assertEquals(Boolean.FALSE, this.beanAccessor.getDataValue(this.testBean, 1));
        assertEquals("Dingens", this.beanAccessor.getDataValue(this.testBean, 2));
        assertEquals((byte) 7, this.beanAccessor.getDataValue(this.testBean, 3));
        assertEquals((short) 23, this.beanAccessor.getDataValue(this.testBean, 4));
        assertEquals(1000, this.beanAccessor.getDataValue(this.testBean, 5));
        assertEquals(250000l, this.beanAccessor.getDataValue(this.testBean, 6));
        assertEquals(0.5f, this.beanAccessor.getDataValue(this.testBean, 7));
        assertEquals(3.14d, this.beanAccessor.getDataValue(this.testBean, 8));
        assertEquals('X', this.beanAccessor.getDataValue(this.testBean, 9));
    }

    @Test
    public void shouldSetNullValuesInHierarchical() {
        this.beanAccessor.setDataValue(this.testBean, 2, null);
        assertNull(this.beanAccessor.getDataValue(this.testBean, 2));

        this.beanAccessor.setDataValue(this.testBean, 2, "Weiner");
        assertEquals("Weiner", this.beanAccessor.getDataValue(this.testBean, 2));
    }

    @Test
    public void shouldHandleNullValuesInHierarchical() {
        this.testBean.bean.sub = null;

        assertNull(this.beanAccessor.getDataValue(this.testBean, 2));
        assertNull(this.beanAccessor.getDataValue(this.testBean, 3));
    }

    @Test
    public void shouldThrowExceptionOnNotAvailableGetter() {
        String[] beanPropertyNames = {
                "names",
                "bean.active",
                "bean.sub.value",
                "bean.sub.byteAmount",
                "bean.sub.shortAmount",
                "bean.sub.intAmounts",
                "bean.sub.longAmount",
                "bean.sub.floatAmount",
                "bean.sub.doubleAmount",
                "bean.sub.charAmount" };

        this.beanAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(beanPropertyNames);

        assertThrows(IllegalStateException.class, () -> this.beanAccessor.getDataValue(this.testBean, 0));
        assertThrows(IllegalStateException.class, () -> this.beanAccessor.getDataValue(this.testBean, 5));
    }

    @Test
    public void shouldThrowExceptionOnNotAvailableSetter() {
        String[] beanPropertyNames = {
                "names",
                "bean.active",
                "bean.sub.values",
                "bean.sub.byteAmount",
                "bean.sub.shortAmount",
                "bean.sub.intAmounts",
                "bean.sub.longAmount",
                "bean.sub.floatAmount",
                "bean.sub.doubleAmount",
                "bean.sub.charAmount" };

        this.beanAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(beanPropertyNames);

        assertThrows(IllegalStateException.class, () -> this.beanAccessor.setDataValue(this.testBean, 0, "Test"));
        assertThrows(IllegalStateException.class, () -> this.beanAccessor.setDataValue(this.testBean, 2, "Test"));
        assertThrows(IllegalStateException.class, () -> this.beanAccessor.setDataValue(this.testBean, 5, 1111));
    }

    // sample structure to test multi-level object hierarchies

    class SubSubBean {
        String value;
        byte byteAmount;
        short shortAmount;
        int intAmount;
        long longAmount;
        float floatAmount;
        double doubleAmount;
        char charAmount;

        public byte getByteAmount() {
            return this.byteAmount;
        }

        public void setByteAmount(byte byteAmount) {
            this.byteAmount = byteAmount;
        }

        public short getShortAmount() {
            return this.shortAmount;
        }

        public void setShortAmount(short shortAmount) {
            this.shortAmount = shortAmount;
        }

        public int getIntAmount() {
            return this.intAmount;
        }

        public void setIntAmount(int intAmount) {
            this.intAmount = intAmount;
        }

        public long getLongAmount() {
            return this.longAmount;
        }

        public void setLongAmount(long longAmount) {
            this.longAmount = longAmount;
        }

        public float getFloatAmount() {
            return this.floatAmount;
        }

        public void setFloatAmount(float floatAmount) {
            this.floatAmount = floatAmount;
        }

        public double getDoubleAmount() {
            return this.doubleAmount;
        }

        public void setDoubleAmount(double doubleAmount) {
            this.doubleAmount = doubleAmount;
        }

        public char getCharAmount() {
            return this.charAmount;
        }

        public void setCharAmount(char charAmount) {
            this.charAmount = charAmount;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    class SubBean {
        boolean active;
        SubSubBean sub;

        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public SubSubBean getSub() {
            return this.sub;
        }

        public void setSub(SubSubBean sub) {
            this.sub = sub;
        }
    }

    class Bean {
        String name;
        SubBean bean;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SubBean getBean() {
            return this.bean;
        }

        public void setBean(SubBean bean) {
            this.bean = bean;
        }
    }
}
