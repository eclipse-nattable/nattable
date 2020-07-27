/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 487913, 488067
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExtendedReflectiveColumnPropertyAccessor<R> extends ReflectiveColumnPropertyAccessor<R> {

    private static final Log LOG = LogFactory.getLog(ExtendedReflectiveColumnPropertyAccessor.class);

    private static final String ERROR_LOG_STATEMENT = "Error on accessing the data model via reflection"; //$NON-NLS-1$

    /**
     * @param propertyNames
     *            of the members of the row bean
     */
    public ExtendedReflectiveColumnPropertyAccessor(String... propertyNames) {
        super(propertyNames);
    }

    /**
     * @param propertyNames
     *            of the members of the row bean
     * @since 1.4
     */
    public ExtendedReflectiveColumnPropertyAccessor(List<String> propertyNames) {
        super(propertyNames);
    }

    @Override
    public Object getDataValue(R rowObj, int columnIndex) {
        String propertyName = getColumnProperty(columnIndex);
        if (propertyName.contains(".")) { //$NON-NLS-1$
            return getPropertyValue(rowObj, propertyName);
        } else {
            return super.getDataValue(rowObj, columnIndex);
        }
    }

    @Override
    public void setDataValue(R rowObj, int columnIndex, Object newValue) {
        String propertyName = getColumnProperty(columnIndex);
        if (propertyName.contains(".")) { //$NON-NLS-1$
            setPropertyValue(rowObj, propertyName, newValue);
        } else {
            super.setDataValue(rowObj, columnIndex, newValue);
        }
    }

    /**
     * Reads the value of a property out of a given bean via reflection.
     *
     * @param object
     *            the bean out of which the property value should be read
     * @param propertyName
     *            the name of the property which value should be read
     * @return the property value of the bean
     */
    private Object getPropertyValue(Object object, String propertyName) {
        assert object != null : "object can not be null!"; //$NON-NLS-1$

        String[] propertyChain = null;
        if (propertyName.contains(".")) { //$NON-NLS-1$
            propertyChain = propertyName.split("\\."); //$NON-NLS-1$
        } else {
            propertyChain = new String[] { propertyName };
        }

        Object child = object;
        Class<?> objectClass = object.getClass();
        String getterName = null;
        Method getterMethod = null;
        for (String pc : propertyChain) {
            getterName = "get" + pc.substring(0, 1).toUpperCase() + pc.substring(1); //$NON-NLS-1$
            try {
                getterMethod = objectClass.getMethod(getterName);
                child = getterMethod.invoke(child);
            } catch (NoSuchMethodException e) {
                try {
                    getterName = "is" + pc.substring(0, 1).toUpperCase() + pc.substring(1); //$NON-NLS-1$
                    getterMethod = objectClass.getMethod(getterName);
                    child = getterMethod.invoke(child);
                } catch (Exception e1) {
                    LOG.error(ERROR_LOG_STATEMENT, e1);
                    throw new IllegalStateException(e);
                }
            } catch (Exception e) {
                LOG.error(ERROR_LOG_STATEMENT, e);
                throw new IllegalStateException(e);
            }

            if (child != null) {
                objectClass = child.getClass();
            } else {
                // null is returned by reflection, therefore we can not go
                // further and null is the correct return value
                break;
            }
        }

        return child;
    }

    /**
     * Sets a value to the property of a bean via reflection. Also supports dot
     * separated property names to access properties anywhere within the object
     * graph.
     *
     * @param object
     *            the bean where the property value should be set
     * @param propertyName
     *            the name of the property which should be set
     * @param value
     *            the value to set
     */
    private void setPropertyValue(Object object, String propertyName, Object value) {
        assert object != null : "object can not be null!"; //$NON-NLS-1$

        try {
            Object singlePropertyObject = null;
            String singlePropertyName = null;
            if (propertyName.contains(".")) { //$NON-NLS-1$
                singlePropertyObject = getPropertyValue(
                        object,
                        propertyName.substring(0, propertyName.lastIndexOf('.')));
                singlePropertyName = propertyName.substring(propertyName.lastIndexOf('.') + 1);
            } else {
                singlePropertyObject = object;
                singlePropertyName = propertyName;
            }

            if (singlePropertyObject == null) {
                // no object found, stop further processing
                return;
            }

            String setterName = "set" //$NON-NLS-1$
                    + singlePropertyName.substring(0, 1).toUpperCase()
                    + singlePropertyName.substring(1);
            Method setterMethod = null;
            if (value != null) {
                setterMethod = getSetterMethodByNameAndType(singlePropertyObject, setterName, value);
            } else {
                // as the value is null we can not access the setter method
                // directly and have to search for the method
                Method[] methods = singlePropertyObject.getClass().getMethods();
                setterMethod = Arrays.stream(methods).filter(m -> m.getName().equals(setterName)).findFirst().orElse(null);
            }

            if (setterMethod != null) {
                setterMethod.invoke(singlePropertyObject, value);
            }
        } catch (Exception e) {
            LOG.error(ERROR_LOG_STATEMENT, e);
            throw new IllegalStateException(e);
        }
    }

    private Method getSetterMethodByNameAndType(Object singlePropertyObject, String setterName, Object value) throws NoSuchMethodException {
        Method setterMethod = null;
        try {
            setterMethod = singlePropertyObject.getClass().getMethod(setterName, value.getClass());
        } catch (NoSuchMethodException e) {
            // if the method was not found, check if the value is a
            // wrapper type and check for the primitive type
            if (value.getClass() == Boolean.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Boolean.TYPE);
            } else if (value.getClass() == Byte.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Byte.TYPE);
            } else if (value.getClass() == Short.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Short.TYPE);
            } else if (value.getClass() == Integer.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Integer.TYPE);
            } else if (value.getClass() == Long.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Long.TYPE);
            } else if (value.getClass() == Float.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Float.TYPE);
            } else if (value.getClass() == Double.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Double.TYPE);
            } else if (value.getClass() == Character.class) {
                setterMethod = singlePropertyObject.getClass().getMethod(setterName, Character.TYPE);
            } else {
                throw e;
            }
        }
        return setterMethod;
    }
}
