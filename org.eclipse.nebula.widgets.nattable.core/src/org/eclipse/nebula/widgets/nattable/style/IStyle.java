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
package org.eclipse.nebula.widgets.nattable.style;

/**
 * Used to store attributes reflecting a (usually display) style.
 */
public interface IStyle {

    /**
     * Returns the configured value for the given {@link ConfigAttribute}.
     *
     * @param <T>
     *            The value type of the style {@link ConfigAttribute}.
     * @param styleAttribute
     *            The requested style {@link ConfigAttribute}.
     * @return The value for the given {@link ConfigAttribute} or
     *         <code>null</code> if no value is found.
     */
    <T> T getAttributeValue(ConfigAttribute<T> styleAttribute);

    /**
     * Returns the configured value for the given {@link ConfigAttribute}.
     *
     * @param <T>
     *            The value type of the style {@link ConfigAttribute}.
     * @param styleAttribute
     *            The requested style {@link ConfigAttribute}.
     * @param defaultValue
     *            The value that should be returned if no value is found for the
     *            {@link ConfigAttribute}.
     * @return The value for the given {@link ConfigAttribute} or
     *         <code>null</code> if no value is found.
     * @since 2.0
     */
    default <T> T getAttributeValue(ConfigAttribute<T> styleAttribute, T defaultValue) {
        T result = getAttributeValue(styleAttribute);
        return result != null ? result : defaultValue;
    }

    /**
     * Set the value for the {@link ConfigAttribute}.
     *
     * @param <T>
     *            The value type of the style {@link ConfigAttribute}.
     * @param styleAttribute
     *            The style {@link ConfigAttribute} to configure.
     * @param value
     *            The value to set for the given style {@link ConfigAttribute}.
     */
    <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value);
}
