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

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;

/**
 * {@link IStyle} implementation that carries style {@link ConfigAttribute}s in
 * a local map. Style configurations are applied for cell styling and need to be
 * registered for {@link CellConfigAttributes#CELL_STYLE}
 */
public class Style implements IStyle {

    private final HashMap<ConfigAttribute<?>, Object> styleAttributeValueMap = new HashMap<>();

    public Style() {
        // empty default constructor.
    }

    /**
     * Copy constructor.
     *
     * @param style
     *            The {@link Style} object to copy.
     * @since 2.0
     */
    public Style(Style style) {
        this.styleAttributeValueMap.putAll(style.styleAttributeValueMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
        return (T) this.styleAttributeValueMap.get(styleAttribute);
    }

    @Override
    public <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value) {
        this.styleAttributeValueMap.put(styleAttribute, value);
    }

    @Override
    public String toString() {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(this.getClass().getSimpleName() + ": "); //$NON-NLS-1$

        for (Entry<ConfigAttribute<?>, Object> entry : this.styleAttributeValueMap.entrySet()) {
            resultBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"); //$NON-NLS-1$//$NON-NLS-2$
        }

        return resultBuilder.toString();
    }
}
