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
package org.eclipse.nebula.widgets.nattable.extension.builder.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableColumn;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;



public class ColumnGroupModelAssembler {

	public static ColumnGroupModel setupColumnGroups(TableColumn[] columnProps) {
		ColumnGroupModel columnGroupModel;
		columnGroupModel = new ColumnGroupModel();
		Map<String, Set<Integer>> columnGroupToColumnIndexesMap = new HashMap<String, Set<Integer>>();

		for (int columnIndex = 0; columnIndex < columnProps.length; columnIndex++) {
			String columnGroupName = columnProps[columnIndex].groupName;

			// Column if part of a group
			if (columnGroupName != null) {
				Set<Integer> columnGroupIndexes = columnGroupToColumnIndexesMap.get(columnGroupName);
				// Create an entry in the map for the group
				if (columnGroupIndexes == null) {
					columnGroupIndexes = new HashSet<Integer>();
					columnGroupToColumnIndexesMap.put(columnGroupName, columnGroupIndexes);
				}
				// Add to map
				columnGroupIndexes.add(columnIndex);
			}
		}

		// Transfer the map created to the model
		for (String columnGroupName : columnGroupToColumnIndexesMap.keySet()) {
			Set<Integer> columnIndexes = columnGroupToColumnIndexesMap.get(columnGroupName);
			int[] intColumnIndexes = new int[columnIndexes.size()];
			int i = 0;
			for (Integer columnIndex : columnIndexes) {
				intColumnIndexes[i] = columnIndex;
				i++;
			}
			columnGroupModel.addColumnsIndexesToGroup(columnGroupName, intColumnIndexes);
		}

		return columnGroupModel;
	}
}
