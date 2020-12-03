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
package org.eclipse.nebula.widgets.nattable.config;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DefaultDisplayModeOrdering;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IDisplayModeOrdering;

public class ConfigRegistry implements IConfigRegistry {

    // Map<configAttributeType, Map<displayMode, Map<configLabel, value>>>
    Map<ConfigAttribute<?>, EnumMap<DisplayMode, Map<String, ?>>> registry = new HashMap<>();

    @Override
    public <T> T getConfigAttribute(
            ConfigAttribute<T> configAttribute,
            String targetDisplayMode,
            String... configLabels) {

        return getConfigAttribute(
                configAttribute,
                targetDisplayMode,
                Arrays.asList(configLabels));
    }

    @Override
    public <T> T getConfigAttribute(
            ConfigAttribute<T> configAttribute,
            String targetDisplayMode,
            List<String> configLabels) {

        return getConfigAttribute(
                configAttribute,
                DisplayMode.valueOf(targetDisplayMode),
                configLabels);
    }

    @Override
    public <T> T getConfigAttribute(
            ConfigAttribute<T> configAttribute,
            DisplayMode targetDisplayMode,
            String... configLabels) {

        return getConfigAttribute(
                configAttribute,
                targetDisplayMode,
                Arrays.asList(configLabels));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfigAttribute(
            ConfigAttribute<T> configAttribute,
            DisplayMode targetDisplayMode,
            List<String> configLabels) {

        T attributeValue = null;

        EnumMap<DisplayMode, Map<String, ?>> displayModeConfigAttributeMap = this.registry.get(configAttribute);
        if (displayModeConfigAttributeMap != null) {
            for (DisplayMode displayMode : this.displayModeOrdering.getDisplayModeOrdering(targetDisplayMode)) {
                Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
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
    public <T> T getSpecificConfigAttribute(
            ConfigAttribute<T> configAttribute,
            String displayMode,
            String configLabel) {

        return getSpecificConfigAttribute(
                configAttribute,
                DisplayMode.valueOf(displayMode),
                configLabel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSpecificConfigAttribute(
            ConfigAttribute<T> configAttribute,
            DisplayMode displayMode,
            String configLabel) {

        T attributeValue = null;

        EnumMap<DisplayMode, Map<String, ?>> displayModeConfigAttributeMap = this.registry.get(configAttribute);
        if (displayModeConfigAttributeMap != null) {
            Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
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
    public <T> void registerConfigAttribute(
            ConfigAttribute<T> configAttribute,
            T attributeValue) {

        registerConfigAttribute(
                configAttribute,
                attributeValue,
                DisplayMode.NORMAL,
                null);
    }

    @Override
    public <T> void registerConfigAttribute(
            ConfigAttribute<T> configAttribute,
            T attributeValue,
            String displayMode) {

        registerConfigAttribute(
                configAttribute,
                attributeValue,
                DisplayMode.valueOf(displayMode),
                null);
    }

    @Override
    public <T> void registerConfigAttribute(
            ConfigAttribute<T> configAttribute,
            T attributeValue,
            DisplayMode targetDisplayMode) {

        registerConfigAttribute(
                configAttribute,
                attributeValue,
                targetDisplayMode,
                null);
    }

    @Override
    public <T> void registerConfigAttribute(
            ConfigAttribute<T> configAttribute,
            T attributeValue,
            String displayMode,
            String configLabel) {

        registerConfigAttribute(
                configAttribute,
                attributeValue,
                DisplayMode.valueOf(displayMode),
                configLabel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void registerConfigAttribute(
            ConfigAttribute<T> configAttribute,
            T attributeValue,
            DisplayMode targetDisplayMode,
            String configLabel) {

        EnumMap<DisplayMode, Map<String, ?>> displayModeConfigAttributeMap =
                this.registry.computeIfAbsent(configAttribute, cf -> new EnumMap<DisplayMode, Map<String, ?>>(DisplayMode.class));

        Map<String, T> configAttributeMap =
                (Map<String, T>) displayModeConfigAttributeMap.computeIfAbsent(targetDisplayMode, dm -> new HashMap<>());

        configAttributeMap.put(configLabel, attributeValue);
    }

    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType) {

        unregisterConfigAttribute(
                configAttributeType,
                DisplayMode.NORMAL,
                null);
    }

    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType,
            String displayMode) {

        unregisterConfigAttribute(
                configAttributeType,
                DisplayMode.valueOf(displayMode),
                null);
    }

    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType,
            DisplayMode displayMode) {

        unregisterConfigAttribute(
                configAttributeType,
                displayMode,
                null);
    }

    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType,
            String displayMode,
            String configLabel) {

        unregisterConfigAttribute(
                configAttributeType,
                DisplayMode.valueOf(displayMode),
                configLabel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType,
            DisplayMode displayMode,
            String configLabel) {

        EnumMap<DisplayMode, Map<String, ?>> displayModeConfigAttributeMap = this.registry.get(configAttributeType);
        if (displayModeConfigAttributeMap != null) {
            Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
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
