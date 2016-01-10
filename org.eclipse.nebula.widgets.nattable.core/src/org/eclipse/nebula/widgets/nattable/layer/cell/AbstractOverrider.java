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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public abstract class AbstractOverrider implements IConfigLabelProvider {

    private Map<Serializable, List<String>> overrides = new HashMap<Serializable, List<String>>();

    /**
     * Remove all registered labels from the key-label overrides for the given
     * key.
     *
     * @param key
     *            The key for which all labels should be removed.
     */
    public void removeOverride(Serializable key) {
        this.overrides.remove(key);
    }

    /**
     * Add the given labels to the label collection for the given key.
     *
     * @param key
     *            The key for which the labels should be added.
     * @param configLabels
     *            The labels to add.
     */
    public void registerOverrides(Serializable key, String... configLabels) {
        registerOverrides(key, ArrayUtil.asList(configLabels));
    }

    /**
     * Add the given labels to the label collection for the given key.
     *
     * @param key
     *            The key for which the labels should be added.
     * @param configLabels
     *            The labels to add.
     */
    public void registerOverrides(Serializable key, List<String> configLabels) {
        List<String> existingOverrides = getOverrides(key);
        if (existingOverrides == null) {
            this.overrides.put(key, configLabels);
        } else {
            for (String configLabel : configLabels) {
                if (!existingOverrides.contains(configLabel)) {
                    existingOverrides.add(configLabel);
                }
            }
        }
    }

    /**
     * Add the given labels on top of the label collection for the given key.
     *
     * @param key
     *            The key for which the labels should be added.
     * @param configLabels
     *            The labels to add.
     */
    public void registerOverridesOnTop(Serializable key, String... configLabels) {
        registerOverridesOnTop(key, ArrayUtil.asList(configLabels));
    }

    /**
     * Add the given labels on top of the label collection for the given key.
     *
     * @param key
     *            The key for which the labels should be added.
     * @param configLabels
     *            The labels to add.
     */
    public void registerOverridesOnTop(Serializable key, List<String> configLabels) {
        List<String> existingOverrides = getOverrides(key);
        if (existingOverrides == null) {
            this.overrides.put(key, configLabels);
        } else {
            for (int i = configLabels.size() - 1; i >= 0; i--) {
                String configLabel = configLabels.get(i);
                if (!existingOverrides.contains(configLabel)) {
                    existingOverrides.add(0, configLabel);
                }
            }
        }
    }

    /**
     *
     * @return The map of registered key-label overrides.
     */
    public Map<Serializable, List<String>> getOverrides() {
        return this.overrides;
    }

    /**
     * Return the labels that are registered for the given key.
     *
     * @param key
     *            The key for which the labels are requested.
     * @return The labels that are registered for the given key.
     */
    public List<String> getOverrides(Serializable key) {
        return this.overrides.get(key);
    }

    /**
     * Adds the given map of key-label overrides to the override map of this
     * overrider.
     *
     * @param overrides
     *            The key-label overrides to add.
     */
    public void addOverrides(Map<Serializable, List<String>> overrides) {
        this.overrides.putAll(overrides);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = new HashSet<String>();
        for (List<String> labels : this.overrides.values()) {
            result.addAll(labels);
        }
        return result;
    }
}
