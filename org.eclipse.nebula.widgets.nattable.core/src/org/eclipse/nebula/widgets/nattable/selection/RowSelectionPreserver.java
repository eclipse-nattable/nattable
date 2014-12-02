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
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.event.SortColumnEvent;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * Preserves the selected row when the underlying data changes or column is
 * sorted.
 * <p>
 * <b>Example:</b> Data 'A' is the 1st row. An update comes in and data 'A'
 * moves to the 5th row. This class clears current selection and selects row 5.
 *
 * @param <T>
 *            Type of row object beans in the underlying data source
 * @deprecated Use SelectionLayer.setSelectionModel(new RowSelectionModel(...))
 *             instead
 */
@Deprecated
public class RowSelectionPreserver<T> implements
        ILayerEventHandler<IVisualChangeEvent> {

    private final SelectionLayer selectionLayer;
    private final RowSelectionProvider<T> selectionProvider;
    private final IRowDataProvider<T> rowDataProvider;

    /** Track the selected objects */
    private List<T> selectedRowObjects = new ArrayList<T>();

    public RowSelectionPreserver(SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider) {
        this.selectionLayer = selectionLayer;
        this.rowDataProvider = rowDataProvider;

        this.selectionProvider = new RowSelectionProvider<T>(selectionLayer,
                rowDataProvider, true);

        this.selectionProvider
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void selectionChanged(SelectionChangedEvent event) {
                        RowSelectionPreserver.this.selectedRowObjects = ((StructuredSelection) event
                                .getSelection()).toList();
                    }
                });
    }

    /**
     * Checks if all the previously selected objects are available in the data
     * provider. Previously selected object might have been deleted from the
     * list.
     */
    private List<T> getValidSelections() {
        List<T> newSelection = new ArrayList<T>();

        for (T rowObj : this.selectedRowObjects) {
            int index = this.rowDataProvider.indexOfRowObject(rowObj);
            if (index != -1) {
                newSelection.add(rowObj);
            }
        }
        return newSelection;
    }

    /**
     * On a change in the underlying data:
     * <ol>
     * <li>Clears the selection
     * <li>Re-select the row objects selected earlier.
     * </ol>
     */
    @Override
    public void handleLayerEvent(IVisualChangeEvent event) {
        if (ObjectUtils.isEmpty(this.selectedRowObjects)) {
            return;
        }

        if (event instanceof RowStructuralRefreshEvent
                || event instanceof RowStructuralChangeEvent
                || event instanceof SortColumnEvent) {
            this.selectionLayer.clear();
            this.selectionProvider.setSelection(new StructuredSelection(
                    getValidSelections()));
        }
    }

    @Override
    public Class<IVisualChangeEvent> getLayerEventClass() {
        return IVisualChangeEvent.class;
    }
}
