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
package org.eclipse.nebula.widgets.nattable.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DefaultDisplayModeOrdering;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IDisplayModeOrdering;

public class ConfigRegistry implements IConfigRegistry {

    // Map<configAttributeType, Map<displayMode, Map<configLabel, value>>>
    Map<ConfigAttribute<?>, Map<String, Map<String, ?>>> configRegistry = new HashMap<ConfigAttribute<?>, Map<String, Map<String, ?>>>();

    @Override
    public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute,
            String targetDisplayMode, String... configLabels) {
        return getConfigAttribute(configAttribute, targetDisplayMode,
                Arrays.asList(configLabels));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute,
            String targetDisplayMode, List<String> configLabels) {
        T attributeValue = null;

        Map<String, Map<String, ?>> displayModeConfigAttributeMap = this.configRegistry
                .get(configAttribute);
        if (displayModeConfigAttributeMap != null) {
            for (String displayMode : this.displayModeOrdering
                    .getDisplayModeOrdering(targetDisplayMode)) {
                Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap
                        .get(displayMode);
                if (configAttributeMap != null) {
                    for (String configLabel : configLabels) {
                        attributeValue = configAttributeMap.get(configLabel);
                        if (attributeValue != null) {
                            return attributeValue;
                        }
                    }

                    // default config type
                    attributeValue = configAttributeMap.get(null);
                    if (attributeValue != null) {
                        return attributeValue;
                    }
                }
            }
        }

        return attributeValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSpecificConfigAttribute(ConfigAttribute<T> configAttribute,
            String displayMode, String configLabel) {
        T attributeValue = null;

        Map<String, Map<String, ?>> displayModeConfigAttributeMap = this.configRegistry
                .get(configAttribute);
        if (displayModeConfigAttributeMap != null) {
            Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap
                    .get(displayMode);
            if (configAttributeMap != null) {
                attributeValue = configAttributeMap.get(configLabel);
                if (attributeValue != null) {
                    return attributeValue;
                }
            }
        }

        return attributeValue;
    }

    @Override
    public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute,
            T attributeValue) {
        registerConfigAttribute(configAttribute, attributeValue,
                DisplayMode.NORMAL);
    }

    @Override
    public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute,
            T attributeValue, String displayMode) {
        registerConfigAttribute(configAttribute, attributeValue, displayMode,
                null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute,
            T attributeValue, String displayMode, String configLabel) {
        Map<String, Map<String, ?>> displayModeConfigAttributeMap = this.configRegistry
                .get(configAttribute);
        if (displayModeConfigAttributeMap == null) {
            displayModeConfigAttributeMap = new HashMap<String, Map<String, ?>>();
            this.configRegistry.put(configAttribute, displayModeConfigAttributeMap);
        }

        Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap
                .get(displayMode);
        if (configAttributeMap == null) {
            configAttributeMap = new HashMap<String, T>();
            displayModeConfigAttributeMap.put(displayMode, configAttributeMap);
        }

        configAttributeMap.put(configLabel, attributeValue);
    };

    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType) {
        unregisterConfigAttribute(configAttributeType, DisplayMode.NORMAL);
    }

    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType, String displayMode) {
        unregisterConfigAttribute(configAttributeType, displayMode, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType, String displayMode,
            String configLabel) {
        Map<String, Map<String, ?>> displayModeConfigAttributeMap = this.configRegistry
                .get(configAttributeType);
        if (displayModeConfigAttributeMap != null) {
            Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap
                    .get(displayMode);
            if (configAttributeMap != null) {
                configAttributeMap.remove(configLabel);
            }
        }
    }

    // Display mode ordering //////////////////////////////////////////////////

    IDisplayModeOrdering displayModeOrdering = new DefaultDisplayModeOrdering();

    @Override
    public IDisplayModeOrdering getDisplayModeOrdering() {
        return this.displayModeOrdering;
    }

    public void setDisplayModeOrdering(IDisplayModeOrdering displayModeOrdering) {
        this.displayModeOrdering = displayModeOrdering;
    }

}
