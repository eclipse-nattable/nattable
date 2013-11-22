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
package org.eclipse.nebula.widgets.nattable.style.editor.command;

import static org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes.CELL_STYLE;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.IValueIterator;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.persistence.StylePersistor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.editor.ColumnStyleEditorDialog;

/**
 * 
 * 1. Captures a new style using the <code>StyleEditorDialog</code> 
 * 2. Registers style from step 1 in the <code>ConfigRegistry</code> with a new label 
 * 3. Applies the label from step 2 to all cells in the selected column
 * 
 */
public class DisplayColumnStyleEditorCommandHandler extends AbstractLayerCommandHandler<DisplayColumnStyleEditorCommand> implements IPersistable {
	
	protected static final String PERSISTENCE_PREFIX = "userDefinedColumnStyle"; //$NON-NLS-1$
	protected static final String USER_EDITED_STYLE_LABEL = "USER_EDITED_STYLE_FOR_INDEX_"; //$NON-NLS-1$

	protected final SelectionLayer selectionLayer;
	protected ColumnOverrideLabelAccumulator columnLabelAccumulator;
	private final IConfigRegistry configRegistry;
	protected ColumnStyleEditorDialog dialog;
	protected final Map<String, Style> stylesToPersist = new HashMap<String, Style>();

	public DisplayColumnStyleEditorCommandHandler(SelectionLayer selectionLayer, ColumnOverrideLabelAccumulator labelAccumulator, IConfigRegistry configRegistry) {
		this.selectionLayer = selectionLayer;
		this.columnLabelAccumulator = labelAccumulator;
		this.configRegistry = configRegistry;
	}

	@Override
	public boolean doCommand(DisplayColumnStyleEditorCommand command) {
		int columnIndexOfClick = command.getNattableLayer().getColumnIndexByPosition(command.columnPosition);
		
		LabelStack configLabels = new LabelStack();
		columnLabelAccumulator.accumulateConfigLabels(configLabels, columnIndexOfClick, 0);
		configLabels.addLabel(getConfigLabel(columnIndexOfClick));
		
		// Column style
		Style clickedCellStyle = (Style) configRegistry.getConfigAttribute(CELL_STYLE, NORMAL, configLabels.getLabels());
		
		dialog = new ColumnStyleEditorDialog(Display.getCurrent().getActiveShell(), clickedCellStyle);
		dialog.open();

		if(dialog.isCancelPressed()) {
			return true;
		}
		
		applySelectedStyleToColumns(command, selectionLayer.getSelectedColumnPositions());
		return true;
	}

	public Class<DisplayColumnStyleEditorCommand> getCommandClass() {
		return DisplayColumnStyleEditorCommand.class;
	}

	protected void applySelectedStyleToColumns(DisplayColumnStyleEditorCommand command,
			final RangeList columnPositions) {
		for (final IValueIterator columnIter = columnPositions.values().iterator(); columnIter.hasNext(); ) {
			final int columnIndex = selectionLayer.getColumnIndexByPosition(columnIter.nextValue());
			// Read the edited styles
			Style newColumnCellStyle = dialog.getNewColumnCellStyle(); 
			
			String configLabel = getConfigLabel(columnIndex);
			if (newColumnCellStyle == null) {
				stylesToPersist.remove(configLabel);
			} else {
				newColumnCellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, dialog.getNewColumnBorderStyle());
				stylesToPersist.put(configLabel, newColumnCellStyle);
			}
			configRegistry.registerConfigAttribute(CELL_STYLE, newColumnCellStyle, NORMAL, configLabel);
			columnLabelAccumulator.registerColumnOverridesOnTop(columnIndex, configLabel);
		}
	}

	protected String getConfigLabel(int columnIndex) {
		return USER_EDITED_STYLE_LABEL + columnIndex;
	}

	public void loadState(String prefix, Properties properties) {
		prefix = prefix + DOT + PERSISTENCE_PREFIX;
		Set<Object> keySet = properties.keySet();

		for (Object key : keySet) {
			String keyString = (String) key;

			// Relevant Key
			if (keyString.contains(PERSISTENCE_PREFIX)) {
				int colIndex = parseColumnIndexFromKey(keyString);

				// Has the config label been processed
				if (!stylesToPersist.keySet().contains(getConfigLabel(colIndex))) {
					Style savedStyle = StylePersistor.loadStyle(prefix + DOT + getConfigLabel(colIndex), properties);

					configRegistry.registerConfigAttribute(CELL_STYLE, savedStyle, NORMAL, getConfigLabel(colIndex));
					stylesToPersist.put(getConfigLabel(colIndex), savedStyle);
					columnLabelAccumulator.registerColumnOverrides(colIndex, getConfigLabel(colIndex));
				}
			}
		}
	}

	protected int parseColumnIndexFromKey(String keyString) {
		int colLabelStartIndex = keyString.indexOf(USER_EDITED_STYLE_LABEL);
		String columnConfigLabel = keyString.substring(colLabelStartIndex, keyString.indexOf('.', colLabelStartIndex));
		int lastUnderscoreInLabel = columnConfigLabel.lastIndexOf('_', colLabelStartIndex);

		return Integer.parseInt(columnConfigLabel.substring(lastUnderscoreInLabel + 1));
	}

	public void saveState(String prefix, Properties properties) {
		prefix = prefix + DOT + PERSISTENCE_PREFIX;

		for (Map.Entry<String, Style> labelToStyle : stylesToPersist.entrySet()) {
			Style style = labelToStyle.getValue();
			String label = labelToStyle.getKey();

			StylePersistor.saveStyle(prefix + DOT + label, properties, style);
		}
	}
}
