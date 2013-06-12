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
package org.eclipse.nebula.widgets.nattable.columnRename;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.util.PersistenceUtils;


public class RenameColumnHelper implements IPersistable {

	public static final String PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS = ".renamedColumnHeaders"; //$NON-NLS-1$

	private final ColumnHeaderLayer columnHeaderLayer;

	/** Tracks the renamed labels provided by the users */
	protected Map<Integer, String> renamedColumnsLabelsByIndex = new TreeMap<Integer, String>();

	public RenameColumnHelper(ColumnHeaderLayer columnHeaderLayer) {
		this.columnHeaderLayer = columnHeaderLayer;
	}

	/**
	 * Rename the column at the given position.
	 * Note: This does not change the underlying column name.
	 *
	 * @return
	 */
    public boolean renameColumnPosition(int columnPosition, String customColumnName) {
        int index = columnHeaderLayer.getColumnIndexByPosition(columnPosition);
        return renameColumnIndex(index,customColumnName);
    }
    
    public boolean renameColumnIndex(int index, String customColumnName) {
        if (index >= 0) {
            if (customColumnName == null) {
                renamedColumnsLabelsByIndex.remove(index);
            } else {
                renamedColumnsLabelsByIndex.put(index, customColumnName);
            }
            return true;
        }
        return false;
    }
	
	/**
	 * @return the custom label for this column as specified by the user
	 * 	Null if the columns is not renamed
	 */
	public String getRenamedColumnLabel(int columnIndex) {
		return renamedColumnsLabelsByIndex.get(columnIndex);
	}

	/**
	 * @return TRUE if the column has been renamed
	 */
	public boolean isColumnRenamed(int columnIndex) {
		return renamedColumnsLabelsByIndex.get(columnIndex) != null;
	}

	public boolean isAnyColumnRenamed() {
		return renamedColumnsLabelsByIndex.size() > 0;
	}

	public void loadState(String prefix, Properties properties) {
		Object property = properties.get(prefix + PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS);

		try {
			renamedColumnsLabelsByIndex = PersistenceUtils.parseString(property);
		} catch (Exception e) {
			System.err.println("Error while restoring renamed column headers: " + e.getMessage()); //$NON-NLS-1$
			System.err.println("Skipping restore."); //$NON-NLS-1$
			renamedColumnsLabelsByIndex.clear();
		}
	}

	public void saveState(String prefix, Properties properties) {
		String string = PersistenceUtils.mapAsString(renamedColumnsLabelsByIndex);
		if (!isEmpty(string)) {
			properties.put(prefix + PERSISTENCE_KEY_RENAMED_COLUMN_HEADERS, string);
		}
	}
}
