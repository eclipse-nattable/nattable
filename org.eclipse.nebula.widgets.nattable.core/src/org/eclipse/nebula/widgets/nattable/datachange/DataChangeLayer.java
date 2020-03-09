/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.config.DefaultDataChangeConfiguration;
import org.eclipse.nebula.widgets.nattable.datachange.event.DiscardDataChangesCompletedEvent;
import org.eclipse.nebula.widgets.nattable.datachange.event.SaveDataChangesCompletedEvent;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

/**
 * {@link ILayer} that can be used to add a mechanism that highlights cells
 * whose data has been changed.
 *
 * @since 1.6
 */
public class DataChangeLayer extends AbstractIndexLayerTransform {

    private static final Log LOG = LogFactory.getLog(DataChangeLayer.class);

    /**
     * Label that is applied to cells that are marked as modified/dirty in this
     * layer.
     */
    public static final String DIRTY = "DIRTY"; //$NON-NLS-1$

    /**
     * The {@link DataChangeHandler} registered with this
     * {@link DataChangeLayer} to keep track of data changes.
     */
    protected final List<DataChangeHandler> dataChangeHandler = new ArrayList<DataChangeHandler>();

    /**
     * The list of {@link DataChange}s that need to be handled on save or
     * discard.
     */
    protected final List<DataChange> dataChanges = new ArrayList<DataChange>();

    /**
     * Data provider that returns temporary stored data changes.
     */
    private TemporaryDataProvider temporaryDataProvider;

    /**
     * Create a new {@link DataChangeLayer} that does not track row structural
     * changes and uses the default configuration.
     *
     * @param underlyingLayer
     *            The {@link ILayer} on top of which this
     *            {@link DataChangeLayer} should be created. Typically the
     *            {@link DataLayer}.
     * @param keyHandler
     *            The {@link CellKeyHandler} that should be used to store
     *            dataChanges for a specific key.
     * @param temporaryDataStorage
     *            <code>true</code> if the data changes should be handled
     *            temporary in this layer and update the model on save,
     *            <code>false</code> if the data changes should be directly
     *            applied to the underlying model and on save some additional
     *            save operations should be performed.
     */
    public DataChangeLayer(IUniqueIndexLayer underlyingLayer, CellKeyHandler<?> keyHandler, boolean temporaryDataStorage) {
        this(underlyingLayer, keyHandler, temporaryDataStorage, false, true);
    }

    /**
     * Create a new {@link DataChangeLayer} that uses the default configuration.
     *
     * @param underlyingLayer
     *            The {@link ILayer} on top of which this
     *            {@link DataChangeLayer} should be created. Typically the
     *            {@link DataLayer}.
     * @param keyHandler
     *            The {@link CellKeyHandler} that should be used to store
     *            dataChanges for a specific key.
     * @param temporaryDataStorage
     *            <code>true</code> if the data changes should be handled
     *            temporary in this layer and update the model on save,
     *            <code>false</code> if the data changes should be directly
     *            applied to the underlying model and on save some additional
     *            save operations should be performed.
     * @param trackRowStructuralChanges
     *            <code>true</code> if structural changes like inserting or
     *            deleting a row should be tracked, <code>false</code> if such
     *            changes should not be tracked.
     */
    public DataChangeLayer(IUniqueIndexLayer underlyingLayer, CellKeyHandler<?> keyHandler, boolean temporaryDataStorage, boolean trackRowStructuralChanges) {
        this(underlyingLayer, keyHandler, temporaryDataStorage, trackRowStructuralChanges, true);
    }

    /**
     * Create a new {@link DataChangeLayer}.
     *
     * @param underlyingLayer
     *            The {@link ILayer} on top of which this
     *            {@link DataChangeLayer} should be created. Typically the
     *            {@link DataLayer}.
     * @param keyHandler
     *            The {@link CellKeyHandler} that should be used to store
     *            dataChanges for a specific key.
     * @param temporaryDataStorage
     *            <code>true</code> if the data changes should be handled
     *            temporary in this layer and update the model on save,
     *            <code>false</code> if the data changes should be directly
     *            applied to the underlying model and on save some additional
     *            save operations should be performed.
     * @param trackRowStructuralChanges
     *            <code>true</code> if structural changes like inserting or
     *            deleting a row should be tracked, <code>false</code> if such
     *            changes should not be tracked.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if not.
     */
    public DataChangeLayer(IUniqueIndexLayer underlyingLayer, CellKeyHandler<?> keyHandler, boolean temporaryDataStorage, boolean trackRowStructuralChanges, boolean useDefaultConfiguration) {
        super(underlyingLayer);
        registerCommandHandlers();

        if (temporaryDataStorage && trackRowStructuralChanges) {
            LOG.warn("tracking row structural changes is not supported in temporary data storage mode"); //$NON-NLS-1$
        }

        if (temporaryDataStorage) {
            TemporaryUpdateDataChangeHandler handler = new TemporaryUpdateDataChangeHandler(this, keyHandler);
            registerDataChangeHandler(handler);
            this.temporaryDataProvider = handler;
        } else {
            PersistenceUpdateDataChangeHandler handler = new PersistenceUpdateDataChangeHandler(this, keyHandler);
            registerDataChangeHandler(handler);
            if (trackRowStructuralChanges) {
                handler.setUpdateOnVerticalChanges(false);
                registerDataChangeHandler(new RowInsertDataChangeHandler(this, keyHandler));
                registerDataChangeHandler(new RowDeleteDataChangeHandler(this, keyHandler));
            }
        }

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultDataChangeConfiguration());
        }
    }

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new DiscardDataChangesCommandHandler(this));
        registerCommandHandler(new SaveDataChangesCommandHandler(this));
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack labels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
        if (isCellDirty(columnPosition, rowPosition)) {
            labels.addLabelOnTop(DIRTY);
        }
        return labels;
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        if (this.temporaryDataProvider != null && this.temporaryDataProvider.tracksDataChange(columnPosition, rowPosition)) {
            return this.temporaryDataProvider.getDataValueByPosition(columnPosition, rowPosition);
        }
        return super.getDataValueByPosition(columnPosition, rowPosition);
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.getColumnDiffs() == null
                    && structuralChangeEvent.getRowDiffs() == null
                    && structuralChangeEvent.isHorizontalStructureChanged()
                    && structuralChangeEvent.isVerticalStructureChanged()) {
                // Assume everything changed
                clearDataChanges();
            } else {
                for (DataChangeHandler handler : this.dataChangeHandler) {
                    handler.handleStructuralChange(structuralChangeEvent);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    /**
     * Registers the given {@link DataChangeHandler} to keep track of data
     * changes.
     *
     * @param handler
     *            The {@link DataChangeHandler} to register.
     */
    public final void registerDataChangeHandler(DataChangeHandler handler) {
        this.dataChangeHandler.add(handler);

        if (handler instanceof ILayerCommandHandler) {
            registerCommandHandler((ILayerCommandHandler<?>) handler);
        }

        if (handler instanceof ILayerEventHandler) {
            registerEventHandler((ILayerEventHandler<?>) handler);
        }
    }

    /**
     * Unregisters the given {@link DataChangeHandler}.
     *
     * @param handler
     *            The {@link DataChangeHandler} to unregister.
     */
    public final void unregisterDataChangeHandler(DataChangeHandler handler) {
        this.dataChangeHandler.remove(handler);

        if (handler instanceof ILayerCommandHandler) {
            unregisterCommandHandler(((ILayerCommandHandler<?>) handler).getCommandClass());
        }

        if (handler instanceof ILayerEventHandler) {
            unregisterEventHandler((ILayerEventHandler<?>) handler);
        }
    }

    /**
     *
     * @return The {@link DataChangeHandler} registered with this
     *         {@link DataChangeLayer} to keep track of data changes.
     */
    public final List<DataChangeHandler> getDataChangeHandler() {
        return this.dataChangeHandler;
    }

    /**
     * Adds a {@link DataChange} to the list of locally tracked data changes
     * that need to be handled on save or discard.
     *
     * @param change
     *            The {@link DataChange} to add.
     */
    public void addDataChange(DataChange change) {
        synchronized (this.dataChanges) {
            this.dataChanges.add(change);
        }
    }

    /**
     *
     * @return The list of {@link DataChange}s that need to be handled on save
     *         or discard.
     */
    public List<DataChange> getDataChanges() {
        return this.dataChanges;
    }

    /**
     * Discards the tracked data changes. In case temporary data storage is
     * disabled, the applied changes are undone by restoring the previous values
     * via dedicated {@link UpdateDataCommand}s.
     */
    public void discardDataChanges() {
        // avoid handling of data change tracking that are caused by restoring
        // the previous data states
        for (DataChangeHandler handler : this.dataChangeHandler) {
            handler.disableTracking();
        }

        synchronized (this.dataChanges) {
            ListIterator<DataChange> listIter = this.dataChanges.listIterator(this.dataChanges.size());
            while (listIter.hasPrevious()) {
                DataChange change = listIter.previous();
                change.discard(this);
            }
        }

        // enable tracking again
        for (DataChangeHandler handler : this.dataChangeHandler) {
            handler.enableTracking();
        }

        clearDataChanges();
        fireLayerEvent(new DiscardDataChangesCompletedEvent(this));
    }

    /**
     * Saves the tracked data changes. In case temporary data storage is enabled
     * this means the underlying data model is updated. Otherwise the stored
     * data changes are simply cleared.
     * <p>
     * <b>Note:</b> In case temporary data storage is disabled and a custom save
     * operation should be performed on save, a custom
     * {@link SaveDataChangesCommandHandler} should be registered that first
     * performs a custom action and afterwards calls this method to ensure a
     * clear state in this layer.
     * </p>
     */
    public void saveDataChanges() {
        // avoid handling of data change tracking that are caused by saving data
        // states
        for (DataChangeHandler handler : this.dataChangeHandler) {
            handler.disableTracking();
        }

        synchronized (this.dataChanges) {
            for (DataChange change : this.dataChanges) {
                change.save(this);
            }
        }

        // enable tracking again
        for (DataChangeHandler handler : this.dataChangeHandler) {
            handler.enableTracking();
        }

        clearDataChanges();
        fireLayerEvent(new SaveDataChangesCompletedEvent(this));
    }

    /**
     * Clear the locally stored changes.
     */
    public void clearDataChanges() {
        for (DataChangeHandler handler : this.dataChangeHandler) {
            handler.clearDataChanges();
        }
        synchronized (this.dataChanges) {
            this.dataChanges.clear();
        }
    }

    /**
     * Checks if the column with the given position contains cells in a dirty
     * state.
     *
     * @param columnPosition
     *            The position of the column whose dirty state should be
     *            checked.
     * @return <code>true</code> if the column contains cells that are marked as
     *         dirty (data has changed and not saved yet), <code>false</code> if
     *         not.
     */
    public boolean isColumnDirty(int columnPosition) {
        for (DataChangeHandler handler : this.dataChangeHandler) {
            if (handler.isColumnDirty(columnPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the row with the given position contains cells in a dirty
     * state.
     *
     * @param rowPosition
     *            The position of the row whose dirty state should be checked.
     * @return <code>true</code> if the row contains cells that are marked as
     *         dirty (data has changed and not saved yet), <code>false</code> if
     *         not.
     */
    public boolean isRowDirty(int rowPosition) {
        for (DataChangeHandler handler : this.dataChangeHandler) {
            if (handler.isRowDirty(rowPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the cell at the given position is dirty.
     *
     * @param columnPosition
     *            The column position of the cell whose dirty state should be
     *            checked.
     * @param rowPosition
     *            The row position of the cell whose dirty state should be
     *            checked.
     * @return <code>true</code> if the cell is dirty (data has changed and not
     *         saved yet), <code>false</code> if not.
     */
    public boolean isCellDirty(int columnPosition, int rowPosition) {
        for (DataChangeHandler handler : this.dataChangeHandler) {
            if (handler.isCellDirty(columnPosition, rowPosition)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> providedLabels = super.getProvidedLabels();
        providedLabels.add(DIRTY);
        return providedLabels;
    }
}
