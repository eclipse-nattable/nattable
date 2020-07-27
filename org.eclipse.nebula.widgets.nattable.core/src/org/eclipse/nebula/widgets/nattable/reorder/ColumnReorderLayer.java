/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.ResetColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.config.DefaultColumnReorderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Layer that is used to add the functionality for column reordering.
 *
 * @see DefaultColumnReorderLayerConfiguration
 */
public class ColumnReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private static final Log LOG = LogFactory.getLog(ColumnReorderLayer.class);

    public static final String PERSISTENCE_KEY_COLUMN_INDEX_ORDER = ".columnIndexOrder"; //$NON-NLS-1$

    private final IUniqueIndexLayer underlying;

    /**
     * The internal cache of the column index order. Used to track the
     * reordering performed by this layer. Position X in the List contains the
     * index of column at position X.
     */
    protected final MutableIntList columnIndexOrder = IntLists.mutable.empty();

    /**
     * The internal mapping of index to position values. Used for performance
     * reasons in {@link #getColumnPositionByIndex(int)} because
     * {@link List#indexOf(Object)} doesn't scale well.
     *
     * @since 1.5
     */
    protected final MutableIntIntMap indexPositionMapping = IntIntMaps.mutable.empty();

    private final MutableIntIntMap startXCache = IntIntMaps.mutable.empty();

    private int reorderFromColumnPosition;

    /**
     * Creates a {@link ColumnReorderLayer} on top of the given
     * {@link IUniqueIndexLayer} and adds the
     * {@link DefaultColumnReorderLayerConfiguration}.
     *
     * @param underlyingLayer
     *            The underlying layer.
     */
    public ColumnReorderLayer(IUniqueIndexLayer underlyingLayer) {
        this(underlyingLayer, true);
    }

    /**
     * Creates a {@link ColumnReorderLayer} on top of the given
     * {@link IUniqueIndexLayer}.
     *
     * @param underlyingLayer
     *            The underlying layer.
     * @param useDefaultConfiguration
     *            <code>true</code> to add the
     *            {@link DefaultColumnReorderLayerConfiguration}
     */
    public ColumnReorderLayer(IUniqueIndexLayer underlyingLayer, boolean useDefaultConfiguration) {
        super(underlyingLayer);
        this.underlying = underlyingLayer;

        populateIndexOrder();

        registerCommandHandlers();

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultColumnReorderLayerConfiguration());
        }
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getColumnDiffs();
                if (structuralDiffs == null) {
                    // Assume everything changed
                    populateIndexOrder();
                } else {
                    // only react on ADD or DELETE and not on CHANGE
                    StructuralChangeEventHelper.handleColumnDelete(
                            structuralDiffs, this.underlying, this.columnIndexOrder, true);
                    StructuralChangeEventHelper.handleColumnInsert(
                            structuralDiffs, this.underlying, this.columnIndexOrder, true);
                    // update index-position mapping
                    refreshIndexPositionMapping();
                }
                invalidateCache();
            }
        }
        super.handleLayerEvent(event);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ConfigureScalingCommand) {
            // if we change the scaling, the cached start coordinates become
            // invalid
            invalidateCache();
        }
        return super.doCommand(command);
    }

    // Configuration

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new ColumnReorderCommandHandler(this));
        registerCommandHandler(new ColumnReorderStartCommandHandler(this));
        registerCommandHandler(new ColumnReorderEndCommandHandler(this));
        registerCommandHandler(new MultiColumnReorderCommandHandler(this));
        registerCommandHandler(new ResetColumnReorderCommandHandler(this));
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        if (this.columnIndexOrder.size() > 0) {
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_COLUMN_INDEX_ORDER,
                    this.columnIndexOrder.makeString(IPersistable.VALUE_SEPARATOR));
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_COLUMN_INDEX_ORDER);

        if (property != null) {
            MutableIntList newColumnIndexOrder = IntLists.mutable.empty();
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                newColumnIndexOrder.add(Integer.parseInt(index));
            }

            if (isRestoredStateValid(newColumnIndexOrder.toArray())) {
                this.columnIndexOrder.clear();
                this.columnIndexOrder.addAll(newColumnIndexOrder);
                // refresh index-position mapping
                refreshIndexPositionMapping();
            }

        }
        invalidateCache();
        fireLayerEvent(new ColumnStructuralRefreshEvent(this));
    }

    /**
     * Ensure that columns haven't changed in the underlying data source
     *
     * @param newColumnIndexOrder
     *            restored from the properties file.
     * @since 2.0
     */
    protected boolean isRestoredStateValid(int[] newColumnIndexOrder) {
        if (newColumnIndexOrder.length != getColumnCount()) {
            LOG.error("Number of persisted columns (" + newColumnIndexOrder.length + ") " + //$NON-NLS-1$ //$NON-NLS-2$
                    "is not the same as the number of columns in the data source (" //$NON-NLS-1$
                    + getColumnCount() + ").\n" + //$NON-NLS-1$
                    "Skipping restore of column ordering"); //$NON-NLS-1$
            return false;
        }

        for (int index : newColumnIndexOrder) {
            if (!this.indexPositionMapping.containsKey(index)) {
                LOG.error("Column index: " + index + " being restored, is not a available in the data soure.\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        "Skipping restore of column ordering"); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    // Columns

    /**
     *
     * @return the internal kept ordering of column indexes.
     */
    public List<Integer> getColumnIndexOrder() {
        return ArrayUtil.asIntegerList(this.columnIndexOrder.toArray());
    }

    /**
     *
     * @return the internal kept ordering of column indexes.
     * @since 2.0
     */
    public int[] getColumnIndexOrderArray() {
        return this.columnIndexOrder.toArray();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (columnPosition >= 0 && columnPosition < this.columnIndexOrder.size()) {
            return this.columnIndexOrder.get(columnPosition);
        } else {
            return -1;
        }
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.indexPositionMapping.getIfAbsent(columnIndex, -1);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        int columnIndex = getColumnIndexByPosition(localColumnPosition);
        return this.underlying.getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        int columnIndex = this.underlying.getColumnIndexByPosition(underlyingColumnPosition);
        return getColumnPositionByIndex(columnIndex);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
        MutableIntList reorderedColumnPositions = IntLists.mutable.empty();
        for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
            for (int underlyingColumnPosition = underlyingColumnPositionRange.start; underlyingColumnPosition < underlyingColumnPositionRange.end; underlyingColumnPosition++) {
                int localColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPosition);
                reorderedColumnPositions.add(localColumnPosition);
            }
        }

        return PositionUtil.getRanges(reorderedColumnPositions.toSortedArray());
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return LayerUtil.getColumnPositionByX(this, x);
    }

    @Override
    public int getStartXOfColumnPosition(int targetColumnPosition) {
        int cachedStartX = this.startXCache.getIfAbsent(targetColumnPosition, -1);
        if (cachedStartX != -1) {
            return cachedStartX;
        }

        int aggregateWidth = 0;
        for (int columnPosition = 0; columnPosition < targetColumnPosition; columnPosition++) {
            aggregateWidth += this.underlying.getColumnWidthByPosition(localToUnderlyingColumnPosition(columnPosition));
        }

        this.startXCache.put(targetColumnPosition, aggregateWidth);
        return aggregateWidth;
    }

    /**
     * Initialize the internal column index ordering from a clean state, which
     * means it reflects the ordering from the underlying layer.
     *
     * @since 1.6
     */
    protected void populateIndexOrder() {
        this.columnIndexOrder.clear();
        ILayer underlyingLayer = getUnderlyingLayer();
        for (int columnPosition = 0; columnPosition < underlyingLayer.getColumnCount(); columnPosition++) {
            int index = underlyingLayer.getColumnIndexByPosition(columnPosition);
            this.columnIndexOrder.add(index);
            this.indexPositionMapping.put(index, columnPosition);
        }
    }

    /**
     * Initializes the internal index-position-mapping to reflect the internal
     * column-index-order.
     *
     * @since 1.6
     */
    protected void refreshIndexPositionMapping() {
        this.indexPositionMapping.clear();
        for (int position = 0; position < this.columnIndexOrder.size(); position++) {
            int index = this.columnIndexOrder.get(position);
            this.indexPositionMapping.put(index, position);
        }
    }

    // Vertical features

    // Rows

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.underlying.getRowPositionByIndex(rowIndex);
    }

    /**
     * Moves the given from-column to the specified edge of the column to move
     * to.
     *
     * @param fromColumnPosition
     *            column position to move
     * @param toColumnPosition
     *            position to move the column to
     * @param reorderToLeftEdge
     *            <code>true</code> if the column should be moved to the left of
     *            the given column to move to, <code>false</code> if it should
     *            be positioned to the right
     */
    private void moveColumn(int fromColumnPosition, int toColumnPosition, boolean reorderToLeftEdge) {
        if (!reorderToLeftEdge) {
            toColumnPosition++;
        }

        int fromColumnIndex = this.columnIndexOrder.get(fromColumnPosition);
        this.columnIndexOrder.addAtIndex(toColumnPosition, fromColumnIndex);
        this.columnIndexOrder.removeAtIndex(fromColumnPosition + (fromColumnPosition > toColumnPosition ? 1 : 0));

        // update index-position mapping
        refreshIndexPositionMapping();

        invalidateCache();
    }

    /**
     * Moves the given from-column to the <b>left</b> edge of the column to move
     * to.
     *
     * @param fromColumnPosition
     *            column position to move
     * @param toColumnPosition
     *            position to move the column to
     */
    public void reorderColumnPosition(int fromColumnPosition, int toColumnPosition) {
        boolean reorderToLeftEdge;
        if (toColumnPosition < getColumnCount()) {
            reorderToLeftEdge = true;
        } else {
            reorderToLeftEdge = false;
            toColumnPosition--;
        }
        reorderColumnPosition(fromColumnPosition, toColumnPosition, reorderToLeftEdge);
    }

    /**
     * Reorders the given from-column to the specified edge of the column to
     * move to and fires a {@link ColumnReorderEvent}.
     *
     * @param fromColumnPosition
     *            column position to move
     * @param toColumnPosition
     *            position to move the column to
     * @param reorderToLeftEdge
     *            <code>true</code> if the column should be moved to the left of
     *            the given column to move to, <code>false</code> if it should
     *            be positioned to the right
     */
    public void reorderColumnPosition(int fromColumnPosition, int toColumnPosition, boolean reorderToLeftEdge) {
        // get the indexes before the move operation
        int fromColumnIndex = getColumnIndexByPosition(fromColumnPosition);
        int toColumnIndex = getColumnIndexByPosition(toColumnPosition);
        moveColumn(fromColumnPosition, toColumnPosition, reorderToLeftEdge);
        fireLayerEvent(new ColumnReorderEvent(this, fromColumnPosition, fromColumnIndex, toColumnPosition, toColumnIndex, reorderToLeftEdge));
    }

    /**
     * Reorders the given from-columns to the <b>left</b> edge of the column to
     * move to.
     *
     * @param fromColumnPositions
     *            column positions to move
     * @param toColumnPosition
     *            position to move the columns to
     */
    public void reorderMultipleColumnPositions(List<Integer> fromColumnPositions, int toColumnPosition) {
        reorderMultipleColumnPositions(
                fromColumnPositions.stream().mapToInt(Integer::intValue).toArray(),
                toColumnPosition);
    }

    /**
     * Reorders the given from-columns to the <b>left</b> edge of the column to
     * move to.
     *
     * @param fromColumnPositions
     *            column positions to move
     * @param toColumnPosition
     *            position to move the columns to
     * @since 2.0
     */
    public void reorderMultipleColumnPositions(int[] fromColumnPositions, int toColumnPosition) {
        boolean reorderToLeftEdge;
        if (toColumnPosition < getColumnCount()) {
            reorderToLeftEdge = true;
        } else {
            reorderToLeftEdge = false;
            toColumnPosition--;
        }
        reorderMultipleColumnPositions(fromColumnPositions, toColumnPosition, reorderToLeftEdge);
    }

    /**
     * Reorders the given from-columns to the specified edge of the column to
     * move to and fires a {@link ColumnReorderEvent}.
     *
     * @param fromColumnPositions
     *            column positions to move
     * @param toColumnPosition
     *            position to move the columns to
     * @param reorderToLeftEdge
     *            <code>true</code> if the columns should be moved to the left
     *            of the given column to move to, <code>false</code> if they
     *            should be positioned to the right
     */
    public void reorderMultipleColumnPositions(List<Integer> fromColumnPositions, int toColumnPosition, boolean reorderToLeftEdge) {
        reorderMultipleColumnPositions(
                fromColumnPositions.stream().mapToInt(Integer::intValue).toArray(),
                toColumnPosition,
                reorderToLeftEdge);
    }

    /**
     * Reorders the given from-columns to the specified edge of the column to
     * move to and fires a {@link ColumnReorderEvent}.
     *
     * @param fromColumnPositions
     *            column positions to move
     * @param toColumnPosition
     *            position to move the columns to
     * @param reorderToLeftEdge
     *            <code>true</code> if the columns should be moved to the left
     *            of the given column to move to, <code>false</code> if they
     *            should be positioned to the right
     * @since 2.0
     */
    public void reorderMultipleColumnPositions(int[] fromColumnPositions, int toColumnPosition, boolean reorderToLeftEdge) {
        // the position collection needs to be sorted so the move works
        // correctly
        Arrays.sort(fromColumnPositions);

        // get the indexes before the move operation
        int[] fromColumnIndexes = Arrays.stream(fromColumnPositions).map(this::getColumnIndexByPosition).toArray();
        int toColumnIndex = getColumnIndexByPosition(toColumnPosition);

        // Moving from left to right
        final int fromColumnPositionsCount = fromColumnPositions.length;

        if (toColumnPosition > fromColumnPositions[fromColumnPositionsCount - 1]) {
            int firstColumnPosition = fromColumnPositions[0];

            int moved = 0;
            for (int columnCount = 0; columnCount < fromColumnPositionsCount; columnCount++) {
                final int fromColumnPosition = fromColumnPositions[columnCount] - moved;
                moveColumn(fromColumnPosition, toColumnPosition, reorderToLeftEdge);
                moved++;
                if (fromColumnPosition < firstColumnPosition) {
                    firstColumnPosition = fromColumnPosition;
                }
            }
        } else if (toColumnPosition < fromColumnPositions[fromColumnPositionsCount - 1]) {
            // Moving from right to left
            int targetColumnPosition = toColumnPosition;
            for (int fromColumnPosition : fromColumnPositions) {
                final int fromColumnPositionInt = fromColumnPosition;
                moveColumn(fromColumnPositionInt, targetColumnPosition++, reorderToLeftEdge);
            }
        }

        fireLayerEvent(new ColumnReorderEvent(this, fromColumnPositions, fromColumnIndexes, toColumnPosition, toColumnIndex, reorderToLeftEdge));
    }

    /**
     * Reorders the given from-columns identified by index to the specified edge
     * of the column to move to and fires a {@link ColumnReorderEvent}. This
     * method can be used to reorder columns that are hidden in a higher level,
     * e.g. to reorder a column group that has hidden columns.
     *
     * @param fromColumnIndexes
     *            column indexes to move
     * @param toColumnPosition
     *            position to move the columns to
     * @param reorderToLeftEdge
     *            <code>true</code> if the columns should be moved to the left
     *            of the given column to move to, <code>false</code> if they
     *            should be positioned to the right
     *
     * @since 1.6
     */
    public void reorderMultipleColumnIndexes(List<Integer> fromColumnIndexes, int toColumnPosition, boolean reorderToLeftEdge) {
        reorderMultipleColumnIndexes(
                fromColumnIndexes.stream().mapToInt(Integer::intValue).toArray(),
                toColumnPosition,
                reorderToLeftEdge);
    }

    /**
     * Reorders the given from-columns identified by index to the specified edge
     * of the column to move to and fires a {@link ColumnReorderEvent}. This
     * method can be used to reorder columns that are hidden in a higher level,
     * e.g. to reorder a column group that has hidden columns.
     *
     * @param fromColumnIndexes
     *            column indexes to move
     * @param toColumnPosition
     *            position to move the columns to
     * @param reorderToLeftEdge
     *            <code>true</code> if the columns should be moved to the left
     *            of the given column to move to, <code>false</code> if they
     *            should be positioned to the right
     *
     * @since 2.0
     */
    public void reorderMultipleColumnIndexes(int[] fromColumnIndexes, int toColumnPosition, boolean reorderToLeftEdge) {
        // calculate positions from indexes
        int[] fromColumnPositions = Arrays.stream(fromColumnIndexes).map(this::getColumnPositionByIndex).toArray();
        reorderMultipleColumnPositions(fromColumnPositions, toColumnPosition, reorderToLeftEdge);
    }

    /**
     * Clear the internal cache.
     *
     * @since 1.6
     */
    protected void invalidateCache() {
        this.startXCache.clear();
    }

    /**
     * Returns the column position from where the reorder process started. Used
     * by the {@link ColumnReorderEndCommandHandler} which is triggered by the
     * {@link ColumnReorderDragMode} when dragging a column is finished.
     *
     * @return The column position where the reorder started.
     */
    public int getReorderFromColumnPosition() {
        return this.reorderFromColumnPosition;
    }

    /**
     * Sets the column position where a reorder process started. Typically done
     * by calling the {@link ColumnReorderStartCommand} which is triggered by
     * the {@link ColumnReorderDragMode}.
     *
     * @param fromColumnPosition
     *            The column position where the reorder started.
     */
    public void setReorderFromColumnPosition(int fromColumnPosition) {
        this.reorderFromColumnPosition = fromColumnPosition;
    }

    /**
     * Resets the reordering tracked by this layer.
     *
     * @since 1.6
     */
    public void resetReorder() {
        populateIndexOrder();
        invalidateCache();
        fireLayerEvent(new ColumnStructuralRefreshEvent(this));
    }

}
