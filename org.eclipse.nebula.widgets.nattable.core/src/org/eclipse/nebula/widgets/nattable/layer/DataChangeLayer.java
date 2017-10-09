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
package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.DiscardDataChangesCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.SaveDataChangesCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.swt.graphics.Point;

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

    protected final Set<Integer> changedColumns = new HashSet<Integer>();

    protected final Set<Integer> changedRows = new HashSet<Integer>();

    /**
     * Collection of modified cells and corresponding {@link UpdateDataCommand}s
     * that are collected in this layer. Used to perform a model update if the
     * {@link SaveDataChangesCommand} is executed, or cleared without data model
     * changes if the {@link DiscardDataChangesCommand} is executed. Will only
     * contain {@link UpdateDataCommand}s in case temporaryDataStorage is
     * enabled.
     */
    protected Map<Point, UpdateDataCommand> dataChanges = new LinkedHashMap<Point, UpdateDataCommand>();

    /**
     * Flag that is used to configure whether the {@link UpdateDataCommand}
     * should be handled in this layer until a save operation is triggered.
     */
    private final boolean temporaryDataStorage;

    protected boolean handleDataUpdateEvents = true;

    /**
     * Create a new {@link DataChangeLayer}.
     *
     * @param underlyingLayer
     *            The {@link ILayer} on top of which this
     *            {@link DataChangeLayer} should be created. Typically the
     *            {@link DataLayer}.
     * @param temporaryDataStorage
     *            <code>true</code> if the data changes should be handled
     *            temporary in this layer and update the model on save,
     *            <code>false</code> if the data changes should be directly
     *            applied to the underlying model and on save some additional
     *            save operations should be performed.
     */
    public DataChangeLayer(IUniqueIndexLayer underlyingLayer, boolean temporaryDataStorage) {
        super(underlyingLayer);
        this.temporaryDataStorage = temporaryDataStorage;
        registerCommandHandlers();
    }

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new DiscardDataChangesCommandHandler(this));
        registerCommandHandler(new SaveDataChangesCommandHandler(this));
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack labels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
        if (this.dataChanges.containsKey(new Point(columnPosition, rowPosition))) {
            labels.addLabel(DIRTY);
        }
        return labels;
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        Point point = new Point(columnPosition, rowPosition);
        if (this.temporaryDataStorage && this.dataChanges.containsKey(point)) {
            return this.dataChanges.get(point).getNewValue();
        }
        return super.getDataValueByPosition(columnPosition, rowPosition);
    }

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
                    new Point(updateEvent.getColumnPosition(), updateEvent.getRowPosition()),
                    new UpdateDataCommand(this, updateEvent.getColumnPosition(), updateEvent.getRowPosition(), updateEvent.getOldValue()));
        } else if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.getColumnDiffs() == null && structuralChangeEvent.getRowDiffs() == null) {
                // Assume everything changed
                clearDataChanges();
            } else if (structuralChangeEvent.isHorizontalStructureChanged()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getColumnDiffs();
                StructuralChangeEventHelper.handleColumnDelete(structuralDiffs, this.dataChanges);
                StructuralChangeEventHelper.handleColumnInsert(structuralDiffs, this.dataChanges);

                rebuildPositionCollections();
            } else if (structuralChangeEvent.isVerticalStructureChanged()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getRowDiffs();
                StructuralChangeEventHelper.handleRowDelete(structuralDiffs, this.dataChanges);
                StructuralChangeEventHelper.handleRowInsert(structuralDiffs, this.dataChanges);

                rebuildPositionCollections();
            }
        }

        super.handleLayerEvent(event);
    }

    /**
     * Rebuilds the {@link #changedColumns} and {@link #changedRows} collections
     * based on the updated {@link #dataChanges} map.
     */
    protected void rebuildPositionCollections() {
        this.changedColumns.clear();
        this.changedRows.clear();
        // iterate over dataChanges and rebuild changed collections
        for (Point point : this.dataChanges.keySet()) {
            this.changedColumns.add(point.x);
            this.changedRows.add(point.y);
        }
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (this.temporaryDataStorage && command instanceof UpdateDataCommand && command.convertToTargetLayer(this)) {
            UpdateDataCommand updateCommand = (UpdateDataCommand) command;
            this.changedColumns.add(updateCommand.getColumnPosition());
            this.changedRows.add(updateCommand.getRowPosition());
            this.dataChanges.put(new Point(updateCommand.getColumnPosition(), updateCommand.getRowPosition()), updateCommand);
            fireLayerEvent(new CellVisualChangeEvent(this, updateCommand.getColumnPosition(), updateCommand.getRowPosition()));
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
            for (UpdateDataCommand cmd : this.dataChanges.values()) {
                getUnderlyingLayer().doCommand(cmd);
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
            for (UpdateDataCommand cmd : this.dataChanges.values()) {
                getUnderlyingLayer().doCommand(cmd);
            }
        }
        clearDataChanges();
        fireLayerEvent(new VisualRefreshEvent(this));
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
        return isCellDirty(new Point(columnPosition, rowPosition));
    }

    /**
     * Checks if the cell at the given position is dirty.
     *
     * @param cellPosition
     *            The cell position represented as {@link Point} whose dirty
     *            state should be checked.
     * @return <code>true</code> if the cell is dirty (data has changed and not
     *         saved yet), <code>false</code> if not.
     */
    public boolean isCellDirty(Point cellPosition) {
        return this.dataChanges.containsKey(cellPosition);
    }
}
