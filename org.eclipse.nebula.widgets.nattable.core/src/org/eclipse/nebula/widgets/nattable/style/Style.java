/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.style;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;

/**
 * {@link IStyle} implementation that carries style {@link ConfigAttribute}s in
 * a local map. Style configurations are applied for cell styling and need to be
 * registered for {@link CellConfigAttributes#CELL_STYLE}
 */
public class Style implements IStyle {

    private final Map<ConfigAttribute<?>, Object> styleAttributeValueMap = new HashMap<ConfigAttribute<?>, Object>();

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
            resultBuilder.append(entry.getKey()
                    + ": " + entry.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return resultBuilder.toString();
    }

    /**
     * @since 1.4
     */
    @Override
    public Style clone() {
        Style clone = new Style();
        clone.styleAttributeValueMap.putAll(this.styleAttributeValueMap);
        return clone;
    }
}
