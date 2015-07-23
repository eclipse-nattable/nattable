/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Registers/Adds configuration labels for a given column (by index). Custom
 * {@link ICellEditor}, {@link ICellPainter}, {@link IStyle} can then be
 * registered in the {@link IConfigRegistry} against these labels.
 *
 * Also @see {@link RowOverrideLabelAccumulator}
 */
public class ColumnOverrideLabelAccumulator extends AbstractOverrider implements IPersistable {

    public static final String PERSISTENCE_KEY = ".columnOverrideLabelAccumulator"; //$NON-NLS-1$
    private final ILayer layer;

    /**
     * This key is used to register overrides for all columns.
     */
    public static final String ALL_COLUMN_KEY = "ALL_COLUMNS"; //$NON-NLS-1$

    public ColumnOverrideLabelAccumulator(ILayer layer) {
        this.layer = layer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        int columnIndex = this.layer.getColumnIndexByPosition(columnPosition);

        addOverrides(configLabels, Integer.valueOf(columnIndex));

        // first add the labels that should be applied for all columns
        addOverrides(configLabels, ALL_COLUMN_KEY);
    }

    private void addOverrides(LabelStack configLabels, Serializable key) {
        List<String> overrides = getOverrides(key);
        if (overrides != null) {
            for (String configLabel : overrides) {
                configLabels.addLabel(configLabel);
            }
        }
    }

    /**
     * Register labels to be contributed a column. This label will be applied to
     * all cells in the column.
     *
     * @param columnIndex
     *            The column index of the column to which the config label
     *            should be contributed.
     * @param configLabels
     *            The config labels to add.
     */
    public void registerColumnOverrides(int columnIndex, String... configLabels) {
        super.registerOverrides(columnIndex, configLabels);
    }

    /**
     * Register labels to be contributed a column. This label will be applied to
     * all cells in the column.
     *
     * @param columnIndex
     *            The column index of the column to which the config label
     *            should be contributed.
     * @param configLabels
     *            The config labels to add.
     */
    public void registerColumnOverrides(int columnIndex, List<String> configLabels) {
        super.registerOverrides(columnIndex, configLabels);
    }

    /**
     * Register labels to be contributed a column. This label will be applied to
     * all cells in the column. Using this method will add the labels on top of
     * the label stack.
     *
     * @param columnIndex
     *            The column index of the column to which the config label
     *            should be contributed.
     * @param configLabels
     *            The config labels to add.
     */
    public void registerColumnOverridesOnTop(int columnIndex, String... configLabels) {
        super.registerOverridesOnTop(columnIndex, configLabels);
    }

    /**
     * Register labels to be contributed a column. This label will be applied to
     * all cells in the column. Using this method will add the labels on top of
     * the label stack.
     *
     * @param columnIndex
     *            The column index of the column to which the config label
     *            should be contributed.
     * @param configLabels
     *            The config labels to add.
     */
    public void registerColumnOverridesOnTop(int columnIndex, List<String> configLabels) {
        super.registerOverridesOnTop(columnIndex, configLabels);
    }

    /**
     * Unregister a label that was contributed for a column.
     *
     * @param columnIndex
     *            The column index of the column to which a config label was
     *            contributed.
     * @param configLabel
     *            The config label to remove.
     */
    public void unregisterOverrides(int columnIndex, String configLabel) {
        List<String> overrides = getOverrides(columnIndex);
        if (overrides != null) {
            overrides.remove(configLabel);
        }
    }

    /**
     * Unregister labels that were contributed for a column.
     *
     * @param columnIndex
     *            The column index of the column to which a config label was
     *            contributed.
     * @param configLabels
     *            The config labels to remove.
     */
    public void unregisterOverrides(int columnIndex, String... configLabels) {
        List<String> overrides = getOverrides(columnIndex);
        if (overrides != null) {
            overrides.removeAll(ArrayUtil.asList(configLabels));
        }
    }

    /**
     * Register a label to be contributed to all columns. This label will be
     * applied to all cells.
     *
     * @param configLabel
     *            The config label that should be added to all cells.
     */
    public void registerOverrides(String configLabel) {
        super.registerOverrides(ALL_COLUMN_KEY, configLabel);
    }

    /**
     * Register labels to be contributed to all columns. These labels will be
     * applied to all cells.
     *
     * @param configLabels
     *            The config labels that should be added to all cells.
     */
    public void registerOverrides(List<String> configLabels) {
        super.registerOverrides(ALL_COLUMN_KEY, configLabels);
    }

    /**
     * Register a label to be contributed to all columns. This label will be
     * applied to all cells.
     *
     * @param configLabel
     *            The config label that should be added to all cells.
     */
    public void registerOverridesOnTop(String configLabel) {
        super.registerOverridesOnTop(ALL_COLUMN_KEY, configLabel);
    }

    /**
     * Register labels to be contributed to all columns. These labels will be
     * applied to all cells.
     *
     * @param configLabels
     *            The config labels that should be added to all cells.
     */
    public void registerOverridesOnTop(List<String> configLabels) {
        super.registerOverridesOnTop(ALL_COLUMN_KEY, configLabels);
    }

    /**
     * Unregister a label that was contributed for all columns.
     *
     * @param configLabel
     *            The config label to remove.
     */
    public void unregisterOverrides(String configLabel) {
        List<String> overrides = getOverrides(ALL_COLUMN_KEY);
        if (overrides != null) {
            overrides.remove(configLabel);
            if (overrides.isEmpty()) {
                removeOverride(ALL_COLUMN_KEY);
            }
        }
    }

    /**
     * Unregister labels that were contributed for all columns.
     *
     * @param configLabels
     *            The config labels to remove.
     */
    public void unregisterOverrides(List<String> configLabels) {
        List<String> overrides = getOverrides(ALL_COLUMN_KEY);
        if (overrides != null) {
            overrides.removeAll(configLabels);
            if (overrides.isEmpty()) {
                removeOverride(ALL_COLUMN_KEY);
            }
        }
    }

    /**
     * Save the overrides to a properties file. A line is stored for every
     * column.
     *
     * Example for column 0: prefix.columnOverrideLabelAccumulator.0 =
     * LABEL1,LABEL2
     */
    @Override
    public void saveState(String prefix, Properties properties) {
        Map<Serializable, List<String>> overrides = getOverrides();

        for (Map.Entry<Serializable, List<String>> entry : overrides.entrySet()) {
            StringBuilder strBuilder = new StringBuilder();
            for (String columnLabel : entry.getValue()) {
                strBuilder.append(columnLabel);
                strBuilder.append(VALUE_SEPARATOR);
            }
            // Strip the last comma
            String propertyValue = strBuilder.toString();
            if (propertyValue.endsWith(VALUE_SEPARATOR)) {
                propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
            }
            String propertyKey = prefix + PERSISTENCE_KEY + DOT + entry.getKey();
            properties.setProperty(propertyKey, propertyValue);
        }
    }

    /**
     * Load the overrides state from the given properties file.
     *
     * @see #saveState(String, Properties)
     */
    @Override
    public void loadState(String prefix, Properties properties) {
        Set<Object> keySet = properties.keySet();
        for (Object key : keySet) {
            String keyString = (String) key;
            if (keyString.contains(PERSISTENCE_KEY)) {
                String labelsFromPropertyValue = properties.getProperty(keyString).trim();
                String overrideKey = keyString.substring(keyString.lastIndexOf(DOT) + 1);
                try {
                    registerColumnOverrides(
                            Integer.parseInt(overrideKey),
                            labelsFromPropertyValue.split(VALUE_SEPARATOR));
                } catch (NumberFormatException e) {
                    // if the last part of the key can not be parsed to an
                    // Integer, we use the key as is
                    registerOverrides(overrideKey, labelsFromPropertyValue.split(VALUE_SEPARATOR));
                }
            }
        }
    }
}
