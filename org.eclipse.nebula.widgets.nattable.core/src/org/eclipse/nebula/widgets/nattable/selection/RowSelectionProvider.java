/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

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
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ISelectionEvent;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * Implementation of ISelectionProvider to add support for JFace selection
 * handling.
 *
 * @param <T>
 *            The type of objects provided by the IRowDataProvider
 */
public class RowSelectionProvider<T> implements ISelectionProvider, ILayerListener {

    /**
     * The SelectionLayer this ISelectionProvider is connected to.
     */
    private SelectionLayer selectionLayer;
    /**
     * The IRowDataProvider to access the selected row data.
     */
    private IRowDataProvider<T> rowDataProvider;
    /**
     * Flag to determine if only fully selected rows should be used to populate
     * the selection or if any selection should be populated. Default is to only
     * populate fully selected rows.
     */
    private final boolean fullySelectedRowsOnly;
    /**
     * Flag to configure whether only SelectionChangedEvents should be fired if
     * the row selection changes or even if you just select another column.
     */
    private final boolean handleSameRowSelection;
    /**
     * Collection of ISelectionChangedListeners to this ISelectionProvider
     */
    private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();
    /**
     * Locally stored previous selection which is used to determine if a
     * SelectionChangedEvent should be fired. It is used to avoid firing events
     * if the same row is selected again (default). If handleSameRowSelection is
     * set to <code>true</code>, this value is not evaluated at runtime.
     */
    private ISelection previousSelection;

    /**
     * Flag to configure whether <code>setSelection()</code> should add or set
     * the selection.
     * <p>
     * This was added for convenience because the initial code always added the
     * selection on <code>setSelection()</code> by creating a SelectRowsCommand
     * with the withControlMask set to <code>true</code>. Looking at the
     * specification, <code>setSelection()</code> is used to set the <b>new</b>
     * selection. So the default here is now to set instead of add. But for
     * convenience to older code that relied on the add behaviour it is now
     * possible to change it back to adding.
     * </p>
     */
    private boolean addSelectionOnSet = false;

    /**
     * Flag to configure if row selection should be provided in case a column
     * selection happened.
     * <p>
     * To understand this flag consider a SelectionLayer that is configured for
     * full row selection by using the RowSelectionModel. On performing column
     * selection, all rows would get selected. This matches the specification
     * because of the RowSelectionModel and this RowSelectionProvider would
     * process every selected row.
     * </p>
     * <p>
     * As described in Bug 421848 this causes serious issues for huge datasets.
     * To avoid such issues by still providing the ability to perform and
     * provide multiple row selections, this flag was introduced. Setting the
     * value to <code>false</code> will avoid processing in case of column
     * selections.
     * </p>
     *
     * @see RowSelectionModel
     */
    private boolean processColumnSelection = true;

    /**
     * Create a RowSelectionProvider that only handles fully selected rows and
     * only fires SelectionChangedEvents if the row selection changes.
     *
     * @param selectionLayer
     *            The SelectionLayer this ISelectionProvider should be connected
     *            to.
     * @param rowDataProvider
     *            The IRowDataProvider that should be used to access the
     *            selected row data.
     */
    public RowSelectionProvider(
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider) {

        this(selectionLayer, rowDataProvider, true);
    }

    /**
     * Create a RowSelectionProvider that only fires SelectionChangedEvents if
     * the row selection changes.
     *
     * @param selectionLayer
     *            The SelectionLayer this ISelectionProvider should be connected
     *            to.
     * @param rowDataProvider
     *            The IRowDataProvider that should be used to access the
     *            selected row data.
     * @param fullySelectedRowsOnly
     *            Flag to determine if only fully selected rows should be used
     *            to populate the selection or if any selection should be
     *            populated.
     */
    public RowSelectionProvider(
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider,
            boolean fullySelectedRowsOnly) {

        this(selectionLayer, rowDataProvider, fullySelectedRowsOnly, false);
    }

    /**
     * Create a RowSelectionProvider configured with the given parameters.
     *
     * @param selectionLayer
     *            The SelectionLayer this ISelectionProvider should be connected
     *            to.
     * @param rowDataProvider
     *            The IRowDataProvider that should be used to access the
     *            selected row data.
     * @param fullySelectedRowsOnly
     *            Flag to determine if only fully selected rows should be used
     *            to populate the selection or if any selection should be
     *            populated.
     * @param handleSameRowSelection
     *            Flag to configure whether only SelectionChangedEvents should
     *            be fired if the row selection changes or even if you just
     *            select another column.
     */
    public RowSelectionProvider(
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider,
            boolean fullySelectedRowsOnly,
            boolean handleSameRowSelection) {

        this.selectionLayer = selectionLayer;
        this.rowDataProvider = rowDataProvider;
        this.fullySelectedRowsOnly = fullySelectedRowsOnly;
        this.handleSameRowSelection = handleSameRowSelection;

        this.selectionLayer.addLayerListener(this);
    }

    /**
     * Updates this RowSelectionProvider so it handles the selection of another
     * SelectionLayer and IRowDataProvider.
     * <p>
     * This method was introduced to add support for multiple selection provider
     * within one part. As replacing the selection provider during the lifetime
     * of a part is not properly supported by the workbench, this implementation
     * adds the possibility to exchange the control that serves as selection
     * provider by exchanging the references in the selection provider itself.
     * </p>
     *
     * @param selectionLayer
     *            The SelectionLayer this ISelectionProvider should be connected
     *            to.
     * @param rowDataProvider
     *            The IRowDataProvider that should be used to access the
     *            selected row data.
     */
    public void updateSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider) {
        // unregister as listener from the current set SelectionLayer
        this.selectionLayer.removeLayerListener(this);

        // update the references on which this RowSelectionProvider should
        // operate
        this.selectionLayer = selectionLayer;
        this.rowDataProvider = rowDataProvider;

        // register on the new set SelectionLayer as listener
        this.selectionLayer.addLayerListener(this);
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    @Override
    public ISelection getSelection() {
        return populateRowSelection(this.selectionLayer, this.rowDataProvider, this.fullySelectedRowsOnly);
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        this.listeners.remove(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelection(ISelection selection) {
        if (this.selectionLayer != null && selection instanceof IStructuredSelection) {
            if (!this.addSelectionOnSet || selection.isEmpty()) {
                this.selectionLayer.clear(false);
            }
            if (!selection.isEmpty()) {
                List<T> rowObjects = ((IStructuredSelection) selection).toList();
                Set<Integer> rowPositions = new HashSet<Integer>();
                for (T rowObject : rowObjects) {
                    int rowIndex = this.rowDataProvider.indexOfRowObject(rowObject);
                    int rowPosition = this.selectionLayer.getRowPositionByIndex(rowIndex);
                    rowPositions.add(Integer.valueOf(rowPosition));
                }
                int intValue = -1;
                if (!rowPositions.isEmpty()) {
                    Integer max = Collections.max(rowPositions);
                    intValue = max.intValue();
                }
                if (intValue >= 0) {
                    this.selectionLayer.doCommand(
                            new SelectRowsCommand(
                                    this.selectionLayer,
                                    0,
                                    ObjectUtils.asIntArray(rowPositions),
                                    false,
                                    true,
                                    intValue));
                }
            } else {
                this.selectionLayer.fireCellSelectionEvent(
                        this.selectionLayer.getLastSelectedCell().columnPosition,
                        this.selectionLayer.getLastSelectedCell().rowPosition,
                        false, false, false);
            }
        }
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof ISelectionEvent) {
            ISelection selection = getSelection();
            if ((this.handleSameRowSelection || !selection.equals(this.previousSelection))
                    && !(!this.processColumnSelection && event instanceof ColumnSelectionEvent)) {
                try {
                    for (ISelectionChangedListener listener : this.listeners) {
                        listener.selectionChanged(new SelectionChangedEvent(this, selection));
                    }
                } finally {
                    this.previousSelection = selection;
                }
            }
        }
    }

    static <T> StructuredSelection populateRowSelection(
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider,
            boolean fullySelectedRowsOnly) {
        List<T> rowObjects = SelectionUtils.getSelectedRowObjects(selectionLayer, rowDataProvider, fullySelectedRowsOnly);
        return rowObjects.isEmpty() ? StructuredSelection.EMPTY : new StructuredSelection(rowObjects);
    }

    /**
     * Configure whether <code>setSelection()</code> should add or set the
     * selection.
     * <p>
     * This was added for convenience because the initial code always added the
     * selection on <code>setSelection()</code> by creating a SelectRowsCommand
     * with the withControlMask set to <code>true</code>. Looking at the
     * specification, <code>setSelection()</code> is used to set the <b>new</b>
     * selection. So the default here is now to set instead of add. But for
     * convenience to older code that relied on the add behaviour it is now
     * possible to change it back to adding.
     * </p>
     *
     * @param addSelectionOnSet
     *            <code>true</code> to add the selection on calling
     *            <code>setSelection()</code> The default is <code>false</code>
     *            to behave like specified in RowSelectionProvider
     */
    public void setAddSelectionOnSet(boolean addSelectionOnSet) {
        this.addSelectionOnSet = addSelectionOnSet;
    }

    /**
     * Configure whether column selections should start row selection processing
     * or not.
     * <p>
     * This is necessary to handle issues with huge datasets. Dependent on
     * different configurations, selecting a column can cause the selection of
     * all rows in a table, which would then lead to populate the whole dataset
     * as selection via this provider. Setting the
     * {@link RowSelectionProvider#processColumnSelection} flag to
     * <code>false</code> will skip processing to avoid such issues.
     * </p>
     *
     * @param processColumnSelection
     *            <code>true</code> to process row selection in case of column
     *            selections (default) <code>false</code> to skip processing in
     *            case of column selections to avoid issues on large datasets.
     */
    public void setProcessColumnSelection(boolean processColumnSelection) {
        this.processColumnSelection = processColumnSelection;
    }

}
