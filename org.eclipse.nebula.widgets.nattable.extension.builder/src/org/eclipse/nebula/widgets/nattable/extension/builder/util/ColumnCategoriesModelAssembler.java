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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.columnCategories.ColumnCategoriesModel;
import org.eclipse.nebula.widgets.nattable.columnCategories.Node;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableColumn;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;


public class ColumnCategoriesModelAssembler {

	public static ColumnCategoriesModel setupColumnCategories(TableColumn[] columnProps) {

		ColumnCategoriesModel model = new ColumnCategoriesModel();
		Node rootNode = model.addRootCategory("Root");
		Node all = model.addCategory(rootNode, "All");

		Map<String, List<Integer>> columnCategoryToColumnIndexesMap = new LinkedHashMap<String, List<Integer>>();
		List<Integer> indexesNotCategorized = new LinkedList<Integer>();

		for (TableColumn tableColumn : columnProps) {
			indexesNotCategorized.add(tableColumn.index);
		}

		for (int columnIndex = 0; columnIndex < columnProps.length; columnIndex++) {
			String categoryName = columnProps[columnIndex].categoryName;

			// Column if part of a category
			if (categoryName != null) {
				List<Integer> columnCategoryIndexes = columnCategoryToColumnIndexesMap.get(categoryName);

				// Create an entry in the map for the category
				if (columnCategoryIndexes == null) {
					columnCategoryIndexes = new LinkedList<Integer>();
					columnCategoryToColumnIndexesMap.put(categoryName, columnCategoryIndexes);
				}
				// Add to map
				columnCategoryIndexes.add(columnIndex);
				indexesNotCategorized.remove(Integer.valueOf(columnIndex));
			}
		}

		// Transfer the map created to the category model
		all.addChildColumnIndexes(ArrayUtil.asIntArray(indexesNotCategorized));

		for (String columnGroupName : columnCategoryToColumnIndexesMap.keySet()) {
			List<Integer> columnIndexes = columnCategoryToColumnIndexesMap.get(columnGroupName);

			int[] intColumnIndexes = new int[columnIndexes.size()];
			int i = 0;
			for (Integer columnIndex : columnIndexes) {
				intColumnIndexes[i] = columnIndex;
				i++;
			}
			Node node = model.addCategory(all, columnGroupName);
			node.addChildColumnIndexes(ArrayUtil.asIntArray(columnIndexes));
		}

		return model;
	}
}
