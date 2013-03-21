/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Helper class providing support for modifying cached index lists for
 * IStructuralChangeEvents.
 * 
 * @author Dirk Fauth
 *
 */
public class StructuralChangeEventHelper {

	
	/**
	 * Will check for events that indicate that a rows has been deleted. In that case the given
	 * cached indexes for the given layer need to be updated because the index of the rows might have changed.
	 * E.g. Row with index 3 is hidden in the given layer, deleting row at index 1 will cause the row at index
	 * 3 to be moved at index 2. Without transforming the index regarding the delete event, the wrong
	 * row would be hidden.
	 * @param rowDiffs The collection of {@link StructuralDiff}s to handle
	 * @param underlyingLayer The underlying layer of the layer who caches the indexes. Needed to
	 * 			translate the transported row positions to indexes, because the conversion to the
	 * 			layer who caches the index is done before it is fired further in the layer stack
	 * @param cachedRowIndexes The collection of indexes that is cached by the layer that needs
	 * 			transformation
	 * @param handleNotFound flag to tell whether the not found row indexes should be taken into account
	 * 			or not. Needed for last row checks
	 */
	public static void handleRowDelete(Collection<StructuralDiff> rowDiffs, ILayer underlyingLayer, 
			Collection<Integer> cachedRowIndexes, boolean handleNotFound) {
		
		//the number of all deleted rows that don't have a corresponding index anymore (last row cases)
		int numberOfNoIndex = 0;
		List<Integer> toRemove = new ArrayList<Integer>();
		for (Iterator<StructuralDiff> diffIterator = rowDiffs.iterator(); diffIterator.hasNext();) {
			StructuralDiff rowDiff = diffIterator.next();
			if (rowDiff.getDiffType() != null && rowDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
				Range beforePositionRange = rowDiff.getBeforePositionRange();
				for (int i = beforePositionRange.start; i < beforePositionRange.end; i++) {
					int index = i;//underlyingLayer.getRowIndexByPosition(i);
					if (index >= 0)
						toRemove.add(index);
					else
						numberOfNoIndex++;
				}
			}
		}
		//remove the row indexes that are deleted 
		cachedRowIndexes.removeAll(toRemove);
		
		//modify row indexes regarding the deleted rows
		List<Integer> modifiedRows = new ArrayList<Integer>();
		for (Integer row : cachedRowIndexes) {
			//check number of removed indexes that are lower than the current one
			int deletedBefore = handleNotFound ? numberOfNoIndex : 0;
			for (Integer removed : toRemove) {
				if (removed < row) {
					deletedBefore++;
				}
			}
			int modRow = row-deletedBefore;
			if (modRow >= 0)
				modifiedRows.add(modRow);
		}
		cachedRowIndexes.clear();
		cachedRowIndexes.addAll(modifiedRows);
	}

	
	/**
	 * Will check for events that indicate that a rows are added. In that case the given
	 * cached indexes need to be updated because the index of the rows might have changed.
	 * E.g. Row with index 3 is hidden in the given layer, adding a row at index 1 will cause the row at index
	 * 3 to be moved to index 4. Without transforming the index regarding the add event, the wrong
	 * row would be hidden.
	 * @param rowDiffs The collection of {@link StructuralDiff}s to handle
	 * @param underlyingLayer The underlying layer of the layer who caches the indexes. Needed to
	 * 			translate the transported row positions to indexes, because the conversion to the
	 * 			layer who caches the index is done before it is fired further in the layer stack
	 * @param cachedRowIndexes The collection of indexes that is cached by the layer that needs
	 * 			transformation
	 * @param addToCache Flag to configure if the added value should be added to the cache or not.
	 * 			This is necessary to differ whether cachedRowIndexes are a collection of all indexes
	 * 			that need to be updated (e.g. row reordering) or just a collection of indexes that
	 * 			are applied for a specific state (e.g. row hide state)
	 */
	public static void handleRowInsert(Collection<StructuralDiff> rowDiffs, ILayer underlyingLayer, 
			Collection<Integer> cachedRowIndexes, boolean addToCache) {
		
		for (StructuralDiff rowDiff : rowDiffs) {
			if (rowDiff.getDiffType() != null && rowDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
				Range beforePositionRange = rowDiff.getBeforePositionRange();
				List<Integer> modifiedHiddenRows = new ArrayList<Integer>();
				int beforeIndex = underlyingLayer.getRowIndexByPosition(beforePositionRange.start);
				for (Integer hiddenRow : cachedRowIndexes) {
					if (hiddenRow >= beforeIndex) {
						modifiedHiddenRows.add(hiddenRow+1);
					}
					else {
						modifiedHiddenRows.add(hiddenRow);
					}
				}
				
				if (addToCache)
					modifiedHiddenRows.add(beforeIndex, beforePositionRange.start);
				
				cachedRowIndexes.clear();
				cachedRowIndexes.addAll(modifiedHiddenRows);
			}
		}
	}

}
