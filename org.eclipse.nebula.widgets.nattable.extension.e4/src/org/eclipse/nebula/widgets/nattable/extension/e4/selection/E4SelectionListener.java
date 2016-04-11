/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.e4.selection;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionUtils;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ISelectionEvent;

/**
 * Implementation of {@link ILayerListener} to support E4 selection handling.
 * Needs to be set to the {@link SelectionLayer}
 * 
 * <pre>
 * E4SelectionListener<Person> esl = new E4SelectionListener<>(service, selectionLayer, bodyDataProvider);
 * selectionLayer.addLayerListener(esl);
 * </pre>
 *
 * @param <T>
 *            The type of objects provided by the {@link IRowDataProvider}
 */
public class E4SelectionListener<T> implements ILayerListener {
    /**
     * The {@link ESelectionService} to set the selection to.
     */
    private ESelectionService selectionService;
    /**
     * The {@link SelectionLayer} that is used to retrieve the selection.
     */
    private SelectionLayer selectionLayer;
    /**
     * The {@link IRowDataProvider} to access the selected row data.
     */
    private IRowDataProvider<T> rowDataProvider;
    /**
     * Flag to determine if only fully selected rows should be used to populate
     * the selection or if any selection should be populated. Default is to only
     * populate fully selected rows.
     */
    private boolean fullySelectedRowsOnly = true;
    /**
     * Flag to configure whether a selection should be set to the
     * {@link ESelectionService} if the row selection changes or if just another
     * column is selected.
     */
    private boolean handleSameRowSelection = false;
    /**
     * Locally stored previous selection which is used to determine if a event
     * for changed selection should be fired. It is used to avoid firing events
     * if the same row is selected again (default). If handleSameRowSelection is
     * set to <code>true</code>, this value is not evaluated at runtime.
     */
    private List<T> previousSelection;

    /**
     * Flag to configure if row selection should be provided in case a column
     * selection happened.
     * <p>
     * To understand this flag consider a {@link SelectionLayer} that is
     * configured for full row selection by using the {@link RowSelectionModel}.
     * On performing column selection, all rows would get selected. This matches
     * the specification because of the {@link RowSelectionModel} and this
     * listener would process every selected row.
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
     * Flag to configure whether a list of data values should be set as
     * selection or a single value. Needed for creating typed selection
     * listeners.
     */
    private boolean multiSelection = true;

    /**
     * Create a {@link E4SelectionListener} and registers it to the given
     * {@link SelectionLayer}.
     *
     * @param service
     *            The {@link ESelectionService} to which the selection should be
     *            published to.
     * @param selectionLayer
     *            The {@link SelectionLayer} that is used to retrieve the
     *            selection.
     * @param rowDataProvider
     *            The {@link IRowDataProvider} to access the selected row data.
     */
    @Inject
    public E4SelectionListener(
            ESelectionService service,
            SelectionLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider) {

        this.selectionService = service;
        this.selectionLayer = selectionLayer;
        this.rowDataProvider = rowDataProvider;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof ISelectionEvent) {
            List<T> selection = SelectionUtils.getSelectedRowObjects(this.selectionLayer, this.rowDataProvider, this.fullySelectedRowsOnly);
            if ((this.handleSameRowSelection || !selection.equals(this.previousSelection))
                    && !(!this.processColumnSelection && event instanceof ColumnSelectionEvent)) {
                try {
                    if (this.multiSelection) {
                        this.selectionService.setSelection(selection);
                    } else {
                        this.selectionService.setSelection(!selection.isEmpty() ? selection.get(0) : null);
                    }
                } finally {
                    this.previousSelection = selection;
                }
            }
        }
    }

    /**
     *
     * @return <code>true</code> if only fully selected rows should be used to
     *         populate the selection or if any selection should be populated.
     *         Default is <code>true</code>.
     */
    public boolean isFullySelectedRowsOnly() {
        return this.fullySelectedRowsOnly;
    }

    /**
     *
     * @param fullySelectedRowsOnly
     *            <code>true</code> if only fully selected rows should be used
     *            to populate the selection <code>false</code> if any selection
     *            should be populated.
     */
    public void setFullySelectedRowsOnly(boolean fullySelectedRowsOnly) {
        this.fullySelectedRowsOnly = fullySelectedRowsOnly;
    }

    /**
     *
     * @return <code>true</code> if a selection should only be handled by the
     *         {@link ESelectionService} if the row selection changes or if just
     *         another column is selected.
     */
    public boolean isHandleSameRowSelection() {
        return this.handleSameRowSelection;
    }

    /**
     *
     * @param handleSameRowSelection
     *            <code>true</code> if a selection should only be handled by the
     *            {@link ESelectionService} if the row selection changes or if
     *            just another column is selected.
     */
    public void setHandleSameRowSelection(boolean handleSameRowSelection) {
        this.handleSameRowSelection = handleSameRowSelection;
    }

    /**
     *
     * @return <code>true</code> if column selection should trigger setting the
     *         selection via {@link ESelectionService} (that means basically a
     *         <i>select all</i>), <code>false</code> if not (needed for huge
     *         datasets to avoid a <i>select all</i>).
     */
    public boolean isProcessColumnSelection() {
        return this.processColumnSelection;
    }

    /**
     * Configure whether column selections should start row selection processing
     * or not.
     * <p>
     * This is necessary to handle issues with huge datasets. Dependent on
     * different configurations, selecting a column can cause the selection of
     * all rows in a table, which would then lead to populate the whole dataset
     * as selection via this provider. Setting the
     * {@link E4SelectionListener#processColumnSelection} flag to
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

    /**
     *
     * @return <code>true</code> if a list of data values should be set as
     *         selection or a single value.
     */
    public boolean isMultiSelection() {
        return this.multiSelection;
    }

    /**
     * Set this value to <code>true</code> if a list of values should be set as
     * selection to the {@link ESelectionService} or <code>false</code> if only
     * a single value object should be set.
     *
     * @param multiSelection
     *            <code>true</code> if a list of data values should be set as
     *            selection or a single value.
     */
    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }
}
