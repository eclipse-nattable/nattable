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

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NatTableConfigAttributes;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

public class CellStyleProxy extends StyleProxy {

    /**
     *
     * @param configRegistry
     *            The {@link IConfigRegistry}.
     * @param targetDisplayMode
     *            The {@link DisplayMode}.
     * @param configLabels
     *            The config labels.
     * @deprecated Use constructor with {@link DisplayMode} parameter.
     */
    @Deprecated
    public CellStyleProxy(IConfigRegistry configRegistry, String targetDisplayMode, List<String> configLabels) {
        super(CellConfigAttributes.CELL_STYLE, configRegistry, DisplayMode.valueOf(targetDisplayMode), configLabels);
    }

    /**
     *
     * @param configRegistry
     *            The {@link IConfigRegistry}.
     * @param targetDisplayMode
     *            The {@link DisplayMode}.
     * @param configLabels
     *            The config labels.
     * @since 2.0
     */
    public CellStyleProxy(IConfigRegistry configRegistry, DisplayMode targetDisplayMode, List<String> configLabels) {
        super(CellConfigAttributes.CELL_STYLE, configRegistry, targetDisplayMode, configLabels);
    }

    @Override
    public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
        return getAttributeValue(styleAttribute, false);
    }

    /**
     * Returns the configured value for the given {@link ConfigAttribute}.
     *
     * @param <T>
     *            The value type of the style {@link ConfigAttribute}.
     * @param styleAttribute
     *            The requested style {@link ConfigAttribute}.
     * @param noModification
     *            <code>true</code> if an unmodified value should be returned,
     *            <code>false</code> if an internal modification like font
     *            scaling should be performed.
     * @return The value for the given {@link ConfigAttribute} or
     *         <code>null</code> if no value is found.
     *
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute, boolean noModification) {
        if (!noModification && CellStyleAttributes.FONT.equals(styleAttribute)) {
            Float scalingFactor = this.configRegistry.getConfigAttribute(
                    NatTableConfigAttributes.FONT_SCALING_FACTOR,
                    DisplayMode.NORMAL);

            return (T) GUIHelper.getScaledFont(
                    super.getAttributeValue(CellStyleAttributes.FONT),
                    scalingFactor != null ? scalingFactor.floatValue() : 1.0f);

        }
        return super.getAttributeValue(styleAttribute);
    }

    @Override
    public <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value) {
        throw new UnsupportedOperationException("Not implemented yet"); //$NON-NLS-1$
    }

}
