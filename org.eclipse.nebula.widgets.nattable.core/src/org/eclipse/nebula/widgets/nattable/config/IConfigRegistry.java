/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.config;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IDisplayModeOrdering;

/**
 * Holds all the settings, bindings and other configuration for NatTable.
 * <p>
 * See ConfigRegistryTest for a better understanding.
 *
 * @see ConfigRegistry
 */
public interface IConfigRegistry {

    /**
     * If retrieving registered values
     * <p>
     * Example 1:
     * <p>
     * configRegistry.getConfigAttribute(attribute, DisplayMode.EDIT);
     * </p>
     * <ol>
     * <li>It will look for an attribute registered using the EDIT display mode</li>
     * <li>If it can't find that it will try and find an attribute under the
     * NORMAL mode</li>
     * <li>If it can't find one it will try and find one registered without a
     * display mode {@link #registerConfigAttribute(ConfigAttribute, Object)}</li>
     * </ol>
     * Example 2:
     * <p>
     * configRegistry.getConfigAttribute(attribute, DisplayMode.NORMAL,
     * "testLabel", "testLabel_1");
     * </p>
     * <ol>
     * <li>It will look for an attribute registered by display mode NORMAL and
     * "testLabel"</li>
     * <li>It will look for an attribute registered by display mode NORMAL and
     * "testLabel_1"</li>
     * </ol>
     *
     * @param <T>
     *            Type of the attribute
     * @param configAttribute
     *            to be registered
     * @param targetDisplayMode
     *            display mode the cell needs to be in, for this attribute to be
     *            returned
     * @param configLabels
     *            the cell needs to have, for this attribute to be returned
     * @return the configAttribute, if the display mode and the configLabels
     *         match
     */
    public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute,
            String targetDisplayMode, String... configLabels);

    /**
     * @see #getConfigAttribute(ConfigAttribute, String, String...)
     */
    public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute,
            String targetDisplayMode, List<String> configLabels);

    /**
     * @see #getConfigAttribute(ConfigAttribute, String, String...)
     */
    public <T> T getSpecificConfigAttribute(ConfigAttribute<T> configAttribute,
            String displayMode, String configLabel);

    /**
     * Register a configuration attribute.
     *
     * @param configAttribute
     *            The {@link ConfigAttribute} for which a value should be
     *            registered.
     * @param attributeValue
     *            The value that should be set for the given The
     *            {@link ConfigAttribute}.
     */
    public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute,
            T attributeValue);

    /**
     * Register a configuration attribute against a {@link DisplayMode}.
     *
     * @param configAttribute
     *            The {@link ConfigAttribute} for which a value should be
     *            registered.
     * @param attributeValue
     *            The value that should be set for the given The
     *            {@link ConfigAttribute}.
     * @param targetDisplayMode
     *            The {@link DisplayMode} for which the {@link ConfigAttribute}
     *            should be registered.
     */
    public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute,
            T attributeValue, String targetDisplayMode);

    /**
     * Register an attribute against a {@link DisplayMode} and configuration
     * label (applied to cells)
     *
     * @param configAttribute
     *            The {@link ConfigAttribute} for which a value should be
     *            registered.
     * @param attributeValue
     *            The value that should be set for the given The
     *            {@link ConfigAttribute}.
     * @param targetDisplayMode
     *            The {@link DisplayMode} for which the {@link ConfigAttribute}
     *            should be registered.
     * @param configLabel
     *            The configuration label against which the
     *            {@link ConfigAttribute} should be registered.
     */
    public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute,
            T attributeValue, String targetDisplayMode, String configLabel);

    /**
     * Unregister the given configuration attribute.
     *
     * @param configAttributeType
     *            The {@link ConfigAttribute} to unregister.
     */
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType);

    /**
     * Unregister the given configuration attribute for the given
     * {@link DisplayMode}.
     *
     * @param configAttributeType
     *            The {@link ConfigAttribute} to unregister.
     * @param displayMode
     *            The {@link DisplayMode} for which the {@link ConfigAttribute}
     *            should be unregistered.
     */
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType, String displayMode);

    /**
     * Unregister the given configuration attribute for the given
     * {@link DisplayMode} that was registered against the given configuration
     * label.
     *
     * @param configAttributeType
     *            The {@link ConfigAttribute} to unregister.
     * @param displayMode
     *            The {@link DisplayMode} for which the {@link ConfigAttribute}
     *            should be unregistered.
     * @param configLabel
     *            The configuration label against which the
     *            {@link ConfigAttribute} was registered.
     */
    public <T> void unregisterConfigAttribute(
            ConfigAttribute<T> configAttributeType, String displayMode,
            String configLabel);

    /**
     *
     * @return The {@link IDisplayModeOrdering} which is used to specify in
     *         which order to search through the {@link IConfigRegistry} for
     *         {@link DisplayMode}.
     */
    public IDisplayModeOrdering getDisplayModeOrdering();

}
