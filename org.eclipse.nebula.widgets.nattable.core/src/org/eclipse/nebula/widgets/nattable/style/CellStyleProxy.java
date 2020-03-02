/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    public CellStyleProxy(IConfigRegistry configRegistry, String targetDisplayMode, List<String> configLabels) {
        super(CellConfigAttributes.CELL_STYLE, configRegistry, targetDisplayMode, configLabels);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
        if (CellStyleAttributes.FONT.equals(styleAttribute)) {
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
