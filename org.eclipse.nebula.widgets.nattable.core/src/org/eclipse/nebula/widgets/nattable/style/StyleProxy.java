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
package org.eclipse.nebula.widgets.nattable.style;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;

public abstract class StyleProxy implements IStyle {

    private final ConfigAttribute<IStyle> styleConfigAttribute;
    private final IConfigRegistry configRegistry;
    private final String targetDisplayMode;
    private final List<String> configLabels;

    public StyleProxy(ConfigAttribute<IStyle> styleConfigAttribute,
            IConfigRegistry configRegistry, String targetDisplayMode,
            List<String> configLabels) {
        this.styleConfigAttribute = styleConfigAttribute;
        this.configRegistry = configRegistry;
        this.targetDisplayMode = targetDisplayMode;
        this.configLabels = configLabels;
    }

    @Override
    public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
        T styleAttributeValue = null;
        IDisplayModeOrdering displayModeOrdering = this.configRegistry
                .getDisplayModeOrdering();

        for (String displayMode : displayModeOrdering
                .getDisplayModeOrdering(this.targetDisplayMode)) {
            for (String configLabel : this.configLabels) {
                IStyle cellStyle = this.configRegistry.getSpecificConfigAttribute(
                        this.styleConfigAttribute, displayMode, configLabel);
                if (cellStyle != null) {
                    styleAttributeValue = cellStyle
                            .getAttributeValue(styleAttribute);
                    if (styleAttributeValue != null) {
                        return styleAttributeValue;
                    }
                }
            }

            // default
            IStyle cellStyle = this.configRegistry.getSpecificConfigAttribute(
                    this.styleConfigAttribute, displayMode, null);
            if (cellStyle != null) {
                styleAttributeValue = cellStyle
                        .getAttributeValue(styleAttribute);
                if (styleAttributeValue != null) {
                    return styleAttributeValue;
                }
            }
        }

        return null;
    }

}
