/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class that is used to handle differences in platform implementations
 * of SWT. For example to call methods that are available in SWT but not in the
 * RAP/RWT implementation.
 *
 * @since 2.6
 */
public final class PlatformHelper {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformHelper.class);

    private static final boolean IS_MAC;
    private static final boolean IS_RAP;

    static {
        IS_MAC = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0; //$NON-NLS-1$ //$NON-NLS-2$
        IS_RAP = "rap".equals(SWT.getPlatform()); //$NON-NLS-1$
    }

    private PlatformHelper() {
        // empty private constructor to prevent instantiation
    }

    /**
     * Checks the system property <i>os.name</i> if we are running on a Mac.
     *
     * @return <code>true</code> if we we are executed on a mac,
     *         <code>false</code> if not.
     */
    public static boolean isMAC() {
        return IS_MAC;
    }

    /**
     *
     * @return <code>true</code> if the SWT platform is <i>rap</i>,
     *         <code>false</code> if not.
     */
    public static boolean isRAP() {
        return IS_RAP;
    }

    private static Map<String, Optional<Method>> METHOD_MAPPING = new ConcurrentHashMap<>();

    /**
     * Tries to invoke a getter method on the given object.
     *
     * @param obj
     *            The object on which the getter method should be invoked.
     * @param methodName
     *            The name of the getter method to invoke.
     * @return The return value of the getter method or <code>null</code> if the
     *         method does not exist or fails.
     */
    public static Object callGetter(Object obj, String methodName) {
        String key = obj.getClass().getName() + "#" + methodName; //$NON-NLS-1$
        Optional<Method> method = METHOD_MAPPING.computeIfAbsent(key, (k) -> {
            try {
                Method m = obj.getClass().getMethod(methodName);
                return Optional.of(m);
            } catch (NoSuchMethodException | IllegalArgumentException e) {
                return Optional.empty();
            }

        });
        if (method.isPresent()) {
            try {
                return method.get().invoke(obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Failed to invoke method \"{}\" on \"{}\"", methodName, obj.getClass().getName(), e); //$NON-NLS-1$
                return null;
            }
        }
        return null;
    }

    /**
     * Tries to invoke a setter method on the given object. Simply does nothing
     * if the setter method does not exist or the invocation fails.
     *
     * @param obj
     *            The object on which the setter method should be invoked.
     * @param methodName
     *            The name of the setter method to invoke.
     * @param parameterType
     *            The method parameter.
     * @param parameterValue
     *            The parameter value to set.
     */
    public static void callSetter(Object obj, String methodName, Class<?> parameterType, Object parameterValue) {
        callSetter(obj, methodName, new Class<?>[] { parameterType }, new Object[] { parameterValue });
    }

    /**
     * Tries to invoke a setter method on the given object. Simply does nothing
     * if the setter method does not exist or the invocation fails.
     *
     * @param obj
     *            The object on which the setter method should be invoked.
     * @param methodName
     *            The name of the setter method to invoke.
     * @param parameterTypes
     *            The list of method parameters.
     * @param parameterValues
     *            The list of parameter values to set.
     */
    public static void callSetter(Object obj, String methodName, Class<?>[] parameterTypes, Object[] parameterValues) {
        String key = obj.getClass().getName() + "#" + methodName + "#" + Arrays.toString(parameterTypes); //$NON-NLS-1$ //$NON-NLS-2$
        Optional<Method> method = METHOD_MAPPING.computeIfAbsent(key, (k) -> {
            try {
                Method m = obj.getClass().getMethod(methodName, parameterTypes);
                return Optional.of(m);
            } catch (NoSuchMethodException | IllegalArgumentException e) {
                return Optional.empty();
            }

        });
        if (method.isPresent()) {
            try {
                method.get().invoke(obj, parameterValues);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Failed to invoke method \"{}\" on \"{}\"", methodName, obj.getClass().getName(), e); //$NON-NLS-1$
            }
        }
        // else simply do nothing, method does not exist
    }

    /**
     * Get a label from {@link IDialogConstants}. Needed because in SWT you can
     * access the constants directly, while in RWT you need to get an instance
     * from which to access the constant.
     *
     * @param label
     *            The name of the constant for which the label is requested.
     * @return The label for the given label key.
     */
    public static String getIDialogConstantsLabel(String label) {
        try {
            if (IDialogConstants.class.isInterface()) {
                return IDialogConstants.class.getField(label).get(null).toString();
            }
            Method get = IDialogConstants.class.getMethod("get"); //$NON-NLS-1$
            IDialogConstants invoke = (IDialogConstants) get.invoke(null);
            return invoke.getClass().getField(label).get(invoke).toString();
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | InvocationTargetException e) {
            return ""; //$NON-NLS-1$
        }
    }
}
