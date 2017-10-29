/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.config.DefaultDataChangeConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * {@link ILayer} that can be used to add a mechanism that highlights cells
 * whose data has been changed.
 *
 * @since 1.6
 */
public class DataChangeLayer extends AbstractIndexLayerTransform {

    /**
     * Label that is applied to cells that are marked as modified/dirty in this
     * layer.
     */
    public static final String DIRTY = "DIRTY"; //$NON-NLS-1$

    /**
     * The column indexes of columns that contain dirty cells.
     */
    protected final Set<Integer> changedColumns = new HashSet<Integer>();

    /**
     * The row indexes of rows that contain dirty cells.
     */
    protected final Set<Integer> changedRows = new HashSet<Integer>();

    /**
     * Collection of modified cells and corresponding {@link UpdateDataCommand}s
     * that are collected in this layer. Used to perform a model update if the
     * {@link SaveDataChangesCommand} is executed, or cleared without data model
     * changes if the {@link DiscardDataChangesCommand} is executed. Will only
     * contain {@link UpdateDataCommand}s in case temporaryDataStorage is
     * enabled.
     */
    protected Map<Object, UpdateDataCommand> dataChanges = new LinkedHashMap<Object, UpdateDataCommand>();;

    /**
     * Flag that is used to configure whether the {@link UpdateDataCommand}
     * should be handled in this layer until a save operation is triggered.
     */
    private final boolean temporaryDataStorage;

    /**
     * Flag that is used to temporarily disable event handling. Used for example
     * to not handle {@link DataUpdateEvent}s on save.
     */
    protected boolean handleDataUpdateEvents = true;

    /**
     * The {@link CellKeyHandler} that is used to store dataChanges for a
     * specific key.
     */
    @SuppressWarnings("rawtypes")
    protected CellKeyHandler keyHandler;

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
     */
    public DataChangeLayer(IUniqueIndexLayer underlyingLayer, CellKeyHandler<?> keyHandler, boolean temporaryDataStorage) {
        this(underlyingLayer, keyHandler, temporaryDataStorage, true);
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
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if not.
     */
    public DataChangeLayer(IUniqueIndexLayer underlyingLayer, CellKeyHandler<?> keyHandler, boolean temporaryDataStorage, boolean useDefaultConfiguration) {
        super(underlyingLayer);
        this.keyHandler = keyHandler;
        this.temporaryDataStorage = temporaryDataStorage;
        registerCommandHandlers();

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
        if (this.dataChanges.containsKey(this.keyHandler.getKey(columnPosition, rowPosition))) {
            labels.addLabel(DIRTY);
        }
        return labels;
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        Object key = this.keyHandler.getKey(columnPosition, rowPosition);
        if (this.temporaryDataStorage && this.dataChanges.containsKey(key)) {
            return this.dataChanges.get(key).getNewValue();
        }
        return super.getDataValueByPosition(columnPosition, rowPosition);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleLayerEvent(ILayerEvent event) {
        // if temporaryDataStorage is disabled the underlying data model is
        // updated and we remember the modifications via DataUpdateEvent
        if (!this.temporaryDataStorage && this.handleDataUpdateEvents && event instanceof DataUpdateEvent) {
            DataUpdateEvent updateEvent = (DataUpdateEvent) event;
            this.changedColumns.add(updateEvent.getColumnPosition());
            this.changedRows.add(updateEvent.getRowPosition());
            // store an UpdateDataCommand that can be used to revert the change
            this.dataChanges.put(
                    this.keyHandler.getKey(updateEvent.getColumnPosition(), updateEvent.getRowPosition()),
                    new UpdateDataCommand(this, updateEvent.getColumnPosition(), updateEvent.getRowPosition(), updateEvent.getOldValue()));
        } else if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.getColumnDiffs() == null
                    && structuralChangeEvent.getRowDiffs() == null
                    && structuralChangeEvent.isHorizontalStructureChanged()
                    && structuralChangeEvent.isVerticalStructureChanged()) {
                // Assume everything changed
                clearDataChanges();
            } else if (structuralChangeEvent.isHorizontalStructureChanged()
                    && structuralChangeEvent.getColumnDiffs() != null
                    && this.keyHandler.updateOnHorizontalStructuralChange()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getColumnDiffs();
                StructuralChangeEventHelper.handleColumnDelete(structuralDiffs, this.dataChanges, this.keyHandler);
                StructuralChangeEventHelper.handleColumnInsert(structuralDiffs, this.dataChanges, this.keyHandler);
            } else if (structuralChangeEvent.isVerticalStructureChanged()
                    && structuralChangeEvent.getRowDiffs() != null
                    && this.keyHandler.updateOnVerticalStructuralChange()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getRowDiffs();
                StructuralChangeEventHelper.handleRowDelete(structuralDiffs, this.dataChanges, this.keyHandler);
                StructuralChangeEventHelper.handleRowInsert(structuralDiffs, this.dataChanges, this.keyHandler);
            }
            rebuildPositionCollections();
        }

        super.handleLayerEvent(event);
    }

    /**
     * Rebuilds the {@link #changedColumns} and {@link #changedRows} collections
     * based on the updated {@link #dataChanges} map.
     */
    @SuppressWarnings("unchecked")
    protected void rebuildPositionCollections() {
        this.changedColumns.clear();
        this.changedRows.clear();
        for (Iterator<Object> it = this.dataChanges.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            int columnIndex = this.keyHandler.getColumnIndex(key);
            int rowIndex = this.keyHandler.getRowIndex(key);
            if (columnIndex >= 0 && rowIndex >= 0) {
                this.changedColumns.add(columnIndex);
                this.changedRows.add(rowIndex);
            } else {
                // remove the change as we noticed that the object does not
                // exist anymore
                it.remove();
            }
        }
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (this.temporaryDataStorage
                && command instanceof UpdateDataCommand
                && command.convertToTargetLayer(this)) {

            UpdateDataCommand updateCommand = (UpdateDataCommand) command;
            int columnPosition = updateCommand.getColumnPosition();
            int rowPosition = updateCommand.getRowPosition();
            Object currentValue = getDataValueByPosition(columnPosition, rowPosition);
            if (currentValue == null
                    || updateCommand.getNewValue() == null
                    || !currentValue.equals(updateCommand.getNewValue())) {

                if ((updateCommand.getNewValue() == null
                        && getUnderlyingLayer().getDataValueByPosition(columnPosition, rowPosition) == null)
                        || (updateCommand.getNewValue() != null
                                && updateCommand.getNewValue().equals(getUnderlyingLayer().getDataValueByPosition(columnPosition, rowPosition)))) {
                    // the value was changed back to the original value in the
                    // underlying layer simply remove the local storage to not
                    // showing the cell as dirty
                    this.dataChanges.remove(this.keyHandler.getKey(updateCommand.getColumnPosition(), updateCommand.getRowPosition()));
                    rebuildPositionCollections();
                } else {
                    this.changedColumns.add(updateCommand.getColumnPosition());
                    this.changedRows.add(updateCommand.getRowPosition());
                    this.dataChanges.put(this.keyHandler.getKey(updateCommand.getColumnPosition(), updateCommand.getRowPosition()), updateCommand);
                }
                fireLayerEvent(new CellVisualChangeEvent(this, updateCommand.getColumnPosition(), updateCommand.getRowPosition()));
            }
            return true;
        }
        return super.doCommand(command);
    }

    /**
     *
     * @return <code>true</code> if data changes are stored locally by this
     *         layer, <code>false</code> if data changes are applied to the
     *         underlying data model and this layer reacts on
     *         {@link DataUpdateEvent}s.
     */
    public boolean isStoringDataChangesTemporarily() {
        return this.temporaryDataStorage;
    }

    /**
     * Discards the tracked data changes. In case temporary data storage is
     * disabled, the applied changes are undone by restoring the previous values
     * via dedicated {@link UpdateDataCommand}s.
     */
    public void discardDataChanges() {
        if (!this.temporaryDataStorage) {
            // avoid handling of DataUpdateEvents that are caused by restoring
            // the previous data states
            this.handleDataUpdateEvents = false;
            // if temporary data storage is disabled, the previous state is
            // restored by executing the created UpdateDataCommands for the
            // old values
            for (Map.Entry<Object, UpdateDataCommand> entry : this.dataChanges.entrySet()) {
                getUnderlyingLayer().doCommand(getUpdateDataCommand(entry.getKey(), entry.getValue()));
            }
            this.handleDataUpdateEvents = true;
        }
        clearDataChanges();
        fireLayerEvent(new VisualRefreshEvent(this));
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
        if (this.temporaryDataStorage) {
            for (Map.Entry<Object, UpdateDataCommand> entry : this.dataChanges.entrySet()) {
                getUnderlyingLayer().doCommand(getUpdateDataCommand(entry.getKey(), entry.getValue()));
            }
        }
        clearDataChanges();
        fireLayerEvent(new VisualRefreshEvent(this));
    }

    /**
     *
     * @param key
     *            The key of the cell that should be modified.
     * @param cmd
     *            The {@link UpdateDataCommand} that is stored for the given
     *            key.
     * @return A new {@link UpdateDataCommand} if the cell indexes for the given
     *         key have changed, or the given {@link UpdateDataCommand} if the
     *         indexes still match.
     */
    @SuppressWarnings("unchecked")
    protected UpdateDataCommand getUpdateDataCommand(Object key, UpdateDataCommand cmd) {
        int columnIndex = this.keyHandler.getColumnIndex(key);
        int rowIndex = this.keyHandler.getRowIndex(key);
        if (cmd.getColumnPosition() != columnIndex || cmd.getRowPosition() != rowIndex) {
            return new UpdateDataCommand(this, columnIndex, rowIndex, cmd.getNewValue());
        }
        return cmd;
    }

    /**
     * Clear the locally stored changes.
     */
    public void clearDataChanges() {
        this.changedColumns.clear();
        this.changedRows.clear();
        this.dataChanges.clear();
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
        return this.changedColumns.contains(columnPosition);
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
        return this.changedRows.contains(rowPosition);
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
        return this.dataChanges.containsKey(this.keyHandler.getKey(columnPosition, rowPosition));
    }

}
