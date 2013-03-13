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
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;


public class RowHideShowLayer extends AbstractRowHideShowLayer implements IRowHideShowCommandLayer {

	public static final String PERSISTENCE_KEY_HIDDEN_ROW_INDEXES = ".hiddenRowIndexes"; //$NON-NLS-1$
	
	private final Set<Integer> hiddenRowIndexes;
	
	public RowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
		this.hiddenRowIndexes = new TreeSet<Integer>();
		
		registerCommandHandler(new MultiRowHideCommandHandler(this));
		registerCommandHandler(new RowHideCommandHandler(this));
		registerCommandHandler(new ShowAllRowsCommandHandler(this));
		registerCommandHandler(new MultiRowShowCommandHandler(this));
	}

	
	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
			if (structuralChangeEvent.isVerticalStructureChanged()) {
				Collection<StructuralDiff> rowDiffs = structuralChangeEvent.getRowDiffs();
				if (rowDiffs != null && !rowDiffs.isEmpty()) {
					handleRowDelete(rowDiffs);
					handleRowInsert(rowDiffs);
				}
			}
		}
		super.handleLayerEvent(event);
	}
	
	/**
	 * Will check for events that indicate that a rows has been deleted. In that case the stored
	 * hidden indexes need to be updated because the index of the rows might have changed.
	 * E.g. Row with index 3 is hidden in this layer, deleting row at index 1 will cause the row at index
	 * 3 to be moved at index 2. Without transforming the index regarding the delete event, the wrong
	 * row would be hidden.
	 * @param rowDiffs The collection of {@link StructuralDiff}s to handle
	 */
	protected void handleRowDelete(Collection<StructuralDiff> rowDiffs) {
		List<Integer> toRemove = new ArrayList<Integer>();
		for (Iterator<StructuralDiff> diffIterator = rowDiffs.iterator(); diffIterator.hasNext();) {
			StructuralDiff rowDiff = diffIterator.next();
			if (rowDiff.getDiffType() != null && rowDiff.getDiffType().equals(DiffTypeEnum.DELETE)) {
				Range beforePositionRange = rowDiff.getBeforePositionRange();
				toRemove.add(underlyingLayer.getRowIndexByPosition(beforePositionRange.start));
			}
		}
		//remove the hidden row indexes that are deleted 
		this.hiddenRowIndexes.removeAll(toRemove);
		
		//modify hidden row indexes regarding the deleted rows
		Set<Integer> modifiedHiddenRows = new HashSet<Integer>();
		for (Integer hiddenRow : this.hiddenRowIndexes) {
			//check number of removed indexes that are lower than the current one
			int deletedBefore = 0;
			for (Integer removed : toRemove) {
				if (removed < hiddenRow) {
					deletedBefore++;
				}
			}
			modifiedHiddenRows.add(hiddenRow-deletedBefore);
		}
		this.hiddenRowIndexes.clear();
		this.hiddenRowIndexes.addAll(modifiedHiddenRows);
	}
	
	/**
	 * Will check for events that indicate that a rows are added. In that case the stored
	 * hidden indexes need to be updated because the index of the rows might have changed.
	 * E.g. Row with index 3 is hidden in this layer, adding a row at index 1 will cause the row at index
	 * 3 to be moved to index 4. Without transforming the index regarding the add event, the wrong
	 * row would be hidden.
	 * @param rowDiffs The collection of {@link StructuralDiff}s to handle
	 */
	protected void handleRowInsert(Collection<StructuralDiff> rowDiffs) {
		for (StructuralDiff rowDiff : rowDiffs) {
			if (rowDiff.getDiffType() != null && rowDiff.getDiffType().equals(DiffTypeEnum.ADD)) {
				Range beforePositionRange = rowDiff.getBeforePositionRange();
				Set<Integer> modifiedHiddenRows = new HashSet<Integer>();
				int beforeIndex = underlyingLayer.getRowIndexByPosition(beforePositionRange.start);
				for (Integer hiddenRow : this.hiddenRowIndexes) {
					if (hiddenRow >= beforeIndex) {
						modifiedHiddenRows.add(hiddenRow+1);
					}
					else {
						modifiedHiddenRows.add(hiddenRow);
					}
				}
				this.hiddenRowIndexes.clear();
				this.hiddenRowIndexes.addAll(modifiedHiddenRows);
			}
		}
	}
	
	// Persistence
	
	@Override
	public void saveState(String prefix, Properties properties) {
		if (hiddenRowIndexes.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Integer index : hiddenRowIndexes) {
				strBuilder.append(index);
				strBuilder.append(IPersistable.VALUE_SEPARATOR);
			}
			properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES, strBuilder.toString());
		}
		
		super.saveState(prefix, properties);
	}
	
	@Override
	public void loadState(String prefix, Properties properties) {
		hiddenRowIndexes.clear();
		String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES);
		if (property != null) {
			StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken();
				hiddenRowIndexes.add(Integer.valueOf(index));
			}
		}
		
		super.loadState(prefix, properties);
	}
	
	// Hide/show	
	
	@Override
	public boolean isRowIndexHidden(int rowIndex) {
		return hiddenRowIndexes.contains(Integer.valueOf(rowIndex));
	}

	@Override
	public Collection<Integer> getHiddenRowIndexes() {
		return hiddenRowIndexes; 
	}
	
	@Override
	public void hideRowPositions(Collection<Integer> rowPositions) {
		Set<Integer> rowIndexes = new HashSet<Integer>();
		for (Integer rowPosition : rowPositions) {
			rowIndexes.add(getRowIndexByPosition(rowPosition));
		}
		hiddenRowIndexes.addAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
	}

	@Override
	public void hideRowIndexes(Collection<Integer> rowIndexes) {
		Set<Integer> rowPositions = new HashSet<Integer>();
		for (Integer rowIndex : rowIndexes) {
			rowPositions.add(getRowPositionByIndex(rowIndex));
		}
		hiddenRowIndexes.addAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new HideRowPositionsEvent(this, rowPositions));
	}
	
	@Override
	public void showRowIndexes(Collection<Integer> rowIndexes) {
		hiddenRowIndexes.removeAll(rowIndexes);
		invalidateCache();
		fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(rowIndexes)));
	}

	@Override
	public void showAllRows() {
		Collection<Integer> hiddenRows = new ArrayList<Integer>(hiddenRowIndexes);
		hiddenRowIndexes.clear();
		invalidateCache();
		fireLayerEvent(new ShowRowPositionsEvent(this, hiddenRows));
	}
}
