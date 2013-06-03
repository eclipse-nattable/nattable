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


/**
 * Registers/Adds configuration labels for a given column (by index).
 * Custom {@link ICellEditor}, {@link ICellPainter}, {@link IStyle} can then 
 * be registered in the {@link IConfigRegistry} against these labels.
 * 
 * Also @see {@link RowOverrideLabelAccumulator} 
 */
public class ColumnOverrideLabelAccumulator extends AbstractOverrider implements IPersistable {
	
	public static final String PERSISTENCE_KEY = ".columnOverrideLabelAccumulator"; //$NON-NLS-1$
	private final ILayer layer;

	public ColumnOverrideLabelAccumulator(ILayer layer) {
		this.layer = layer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		int columnIndex = layer.getColumnIndexByPosition(columnPosition);
		List<String> overrides = getOverrides(Integer.valueOf(columnIndex));
		if (overrides != null) {
			for (String configLabel : overrides) {
				configLabels.addLabel(configLabel);
			}
		}
	}

	/**
	 * Register labels to be contributed a column. This label will be applied to
	 * all cells in the column.
	 */
	public void registerColumnOverrides(int columnIndex, String... configLabels) {
		super.registerOverrides(Integer.valueOf(columnIndex), configLabels);
	}
	
	/**
	 * Register labels to be contributed a column. This label will be applied to
	 * all cells in the column.
	 */
	public void registerColumnOverridesOnTop(int columnIndex, String... configLabels) {
		super.registerOverridesOnTop(Integer.valueOf(columnIndex), configLabels);
	}
	
	/** 
	 * Save the overrides to a properties file. A line is stored for every column.
	 * 
	 * Example for column 0:
	 * prefix.columnOverrideLabelAccumulator.0 = LABEL1,LABEL2
	 */
	public void saveState(String prefix, Properties properties) {
		Map<Serializable, List<String>> overrides = getOverrides();

		for (Map.Entry<Serializable, List<String>> entry : overrides.entrySet()) {
			StringBuilder strBuilder = new StringBuilder();
			for (String columnLabel : entry.getValue()) {
				strBuilder.append(columnLabel);
				strBuilder.append(VALUE_SEPARATOR);
			}
			//Strip the last comma
			String propertyValue = strBuilder.toString();
			if(propertyValue.endsWith(VALUE_SEPARATOR)){
				propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
			}
			String propertyKey = prefix + PERSISTENCE_KEY + DOT + entry.getKey();
			properties.setProperty(propertyKey, propertyValue);
		}
	}

	/**
	 * Load the overrides state from the given properties file.
	 * @see #saveState(String, Properties)
	 */
	public void loadState(String prefix, Properties properties) {
		Set<Object> keySet = properties.keySet();
		for (Object key : keySet) {
			String keyString = (String) key;
			if(keyString.contains(PERSISTENCE_KEY)){
				String labelsFromPropertyValue = properties.getProperty(keyString).trim();
				String columnIndexFromKey = keyString.substring(keyString.lastIndexOf(DOT) + 1);
				registerColumnOverrides(Integer.parseInt(columnIndexFromKey), labelsFromPropertyValue.split(VALUE_SEPARATOR));
			}
		}
	}	
}
