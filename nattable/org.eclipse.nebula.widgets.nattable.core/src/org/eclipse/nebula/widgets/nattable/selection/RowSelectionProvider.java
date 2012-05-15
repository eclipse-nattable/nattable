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
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.ISelectionEvent;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

public class RowSelectionProvider<T> implements ISelectionProvider, ILayerListener {
	
	private SelectionLayer selectionLayer;
	private final IRowDataProvider<T> rowDataProvider;
	private final boolean fullySelectedRowsOnly;
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
	private ISelection previousSelection;

	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider) {
		this(selectionLayer, rowDataProvider, true);
	}
	
	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider, boolean fullySelectedRowsOnly) {
		this.selectionLayer = selectionLayer;
		this.rowDataProvider = rowDataProvider;
		this.fullySelectedRowsOnly = fullySelectedRowsOnly;
		
		selectionLayer.addLayerListener(this);
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		return populateRowSelection(selectionLayer, rowDataProvider, fullySelectedRowsOnly);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public void setSelection(ISelection selection) {
		if (selectionLayer != null && selection instanceof IStructuredSelection) {
			selectionLayer.clear();
			if (!selection.isEmpty()) {
    			List<T> rowObjects = ((IStructuredSelection) selection).toList();
    			Set<Integer> rowPositions = new HashSet<Integer>();
    			for (T rowObject : rowObjects) {
    				int rowIndex = rowDataProvider.indexOfRowObject(rowObject);
    				int rowPosition = selectionLayer.getRowPositionByIndex(rowIndex);
    				rowPositions.add(Integer.valueOf(rowPosition));
    			}
				int intValue = -1;
				if (!rowPositions.isEmpty()) {
					Integer max = Collections.max(rowPositions);
					intValue = max.intValue();
				}
				if (intValue >= 0) {
					selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 0, ObjectUtils.asIntArray(rowPositions), false, true, intValue));
				}
			}
		}
	}

	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof ISelectionEvent) {
			ISelection selection = getSelection();
			if (!selection.equals(previousSelection)) {
				try {
					for (ISelectionChangedListener listener : listeners) {
						listener.selectionChanged(new SelectionChangedEvent(this, selection));
					}
				} finally {
					previousSelection = selection;
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	static StructuredSelection populateRowSelection(SelectionLayer selectionLayer, IRowDataProvider rowDataProvider, boolean fullySelectedRowsOnly) {
		List<RowObjectIndexHolder<Object>> rows = new ArrayList<RowObjectIndexHolder<Object>>();

		if (selectionLayer != null) {
			if (fullySelectedRowsOnly) {
				for (int rowPosition : selectionLayer.getFullySelectedRowPositions()) {
					addToSelection(rows, rowPosition, selectionLayer, rowDataProvider);
				}
			} else {
				Set<Range> rowRanges = selectionLayer.getSelectedRowPositions();
				for (Range rowRange : rowRanges) {
					for (int rowPosition = rowRange.start; rowPosition < rowRange.end; rowPosition++) {
						addToSelection(rows, rowPosition, selectionLayer, rowDataProvider);
					}
				}
			}
		}
		Collections.sort(rows);
		List<Object> rowObjects = new ArrayList<Object>();
		for(RowObjectIndexHolder<Object> holder : rows){
			rowObjects.add(holder.getRow());
		}
		return rows.isEmpty() ? StructuredSelection.EMPTY : new StructuredSelection(rowObjects);
	}
	
	@SuppressWarnings("rawtypes")
	private static void addToSelection(List<RowObjectIndexHolder<Object>> rows, int rowPosition, SelectionLayer selectionLayer, IRowDataProvider rowDataProvider) {
		int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
		if (rowIndex >= 0 && rowIndex < rowDataProvider.getRowCount()) {
			Object rowObject = rowDataProvider.getRowObject(rowIndex);
			rows.add(new RowObjectIndexHolder<Object>(rowIndex, rowObject));
		}
	}

}
