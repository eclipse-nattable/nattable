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

/**
 * Implementation of ISelectionProvider to add support for JFace selection handling.
 * 
 * @param <T> The type of objects provided by the IRowDataProvider
 */
public class RowSelectionProvider<T> implements ISelectionProvider, ILayerListener {
	
	/**
	 * The SelectionLayer this ISelectionProvider is connected to
	 */
	private SelectionLayer selectionLayer;
	/**
	 * The IRowDataProvider needed to access the selected row data
	 */
	private final IRowDataProvider<T> rowDataProvider;
	/**
	 * Flag to determine if only fully selected rows should be used to populate the selection
	 * or if any selection should be populated. Default is to only populate fully selected rows.
	 */
	private final boolean fullySelectedRowsOnly;
	/**
	 * Flag to configure whether only SelectionChangedEvents should be fired if the row selection
	 * changes or even if you just select another column.
	 */
	private final boolean handleSameRowSelection;
	/**
	 * Collection of ISelectionChangedListeners to this ISelectionProvider
	 */
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
	/**
	 * Locally stored previous selection which is used to determine if a SelectionChangedEvent
	 * should be fired. It is used to avoid firing events if the same row is selected again (default).
	 * If handleSameRowSelection is set to <code>true</code>, this value is not evaluated at runtime.
	 */
	private ISelection previousSelection;

	/**
	 * Flag to configure whether <code>setSelection()</code> should add or set the selection.
	 * <p>
	 * This was added for convenience because the initial code always added the selection
	 * on <code>setSelection()</code> by creating a SelectRowsCommand with the withControlMask set to <code>true</code>.
	 * Looking at the specification, <code>setSelection()</code> is used to set the <b>new</b> selection.
	 * So the default here is now to set instead of add. But for convenience to older code 
	 * that relied on the add behaviour it is now possible to change it back to adding.
	 */
	private boolean addSelectionOnSet = false;
	
	/**
	 * Create a RowSelectionProvider that only handles fully selected rows and only fires 
	 * SelectionChangedEvents if the row selection changes.
	 * @param selectionLayer The SelectionLayer this ISelectionProvider is connected to
	 * @param rowDataProvider The IRowDataProvider needed to access the selected row data
	 */
	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider) {
		this(selectionLayer, rowDataProvider, true);
	}
	
	/**
	 * Create a RowSelectionProvider that only fires SelectionChangedEvents if the row selection changes.
	 * @param selectionLayer The SelectionLayer this ISelectionProvider is connected to
	 * @param rowDataProvider The IRowDataProvider needed to access the selected row data
	 * @param fullySelectedRowsOnly Flag to determine if only fully selected rows should be used 
	 * 			to populate the selection or if any selection should be populated.
	 */
	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider, 
			boolean fullySelectedRowsOnly) {
		
		this(selectionLayer, rowDataProvider, fullySelectedRowsOnly, false);
	}
	
	/**
	 * Create a RowSelectionProvider configured with the given parameters.
	 * @param selectionLayer The SelectionLayer this ISelectionProvider is connected to
	 * @param rowDataProvider The IRowDataProvider needed to access the selected row data
	 * @param fullySelectedRowsOnly Flag to determine if only fully selected rows should be used 
	 * 			to populate the selection or if any selection should be populated.
	 * @param handleSameRowSelection Flag to configure whether only SelectionChangedEvents should be 
	 * 			fired if the row selection changes or even if you just select another column.
	 */
	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider, 
			boolean fullySelectedRowsOnly, boolean handleSameRowSelection) {
		
		this.selectionLayer = selectionLayer;
		this.rowDataProvider = rowDataProvider;
		this.fullySelectedRowsOnly = fullySelectedRowsOnly;
		this.handleSameRowSelection = handleSameRowSelection;
		
		selectionLayer.addLayerListener(this);
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return populateRowSelection(selectionLayer, rowDataProvider, fullySelectedRowsOnly);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
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
					selectionLayer.doCommand(
							new SelectRowsCommand(selectionLayer, 0, ObjectUtils.asIntArray(rowPositions), 
									false, addSelectionOnSet, intValue));
				}
			}
		}
	}

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof ISelectionEvent) {
			ISelection selection = getSelection();
			if (handleSameRowSelection || !selection.equals(previousSelection)) {
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

	/**
	 * Configure whether <code>setSelection()</code> should add or set the selection.
	 * <p>
	 * This was added for convenience because the initial code always added the selection
	 * on <code>setSelection()</code> by creating a SelectRowsCommand with the withControlMask set to <code>true</code>.
	 * Looking at the specification, <code>setSelection()</code> is used to set the <b>new</b> selection.
	 * So the default here is now to set instead of add. But for convenience to older code 
	 * that relied on the add behaviour it is now possible to change it back to adding.
	 * 
	 * @param addSelectionOnSet <code>true</code> to add the selection on calling <code>setSelection()</code>
	 * 			The default is <code>false</code> to behave like specified in RowSelectionProvider
	 */
	public void setAddSelectionOnSet(boolean addSelectionOnSet) {
		this.addSelectionOnSet = addSelectionOnSet;
	}

}
