/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
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

/**
 * Layer that is used to add the functionality for column reordering.
 *
 * @see DefaultColumnReorderLayerConfiguration
 */
public class ColumnReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private static final Log LOG = LogFactory.getLog(ColumnReorderLayer.class);

    public static final String PERSISTENCE_KEY_COLUMN_INDEX_ORDER = ".columnIndexOrder"; //$NON-NLS-1$

    private final IUniqueIndexLayer underlyingLayer;

    /**
     * The internal cache of the column index order. Used to track the
     * reordering performed by this layer. Position X in the List contains the
     * index of column at position X.
     */
    protected final List<Integer> columnIndexOrder = new ArrayList<Integer>();

    /**
     * The internal mapping of index to position values. Used for performance
     * reasons in {@link #getColumnPositionByIndex(int)} because
     * {@link List#indexOf(Object)} doesn't scale well.
     *
     * @since 1.5
     */
    protected final Map<Integer, Integer> indexPositionMapping = new HashMap<Integer, Integer>();

    private final Map<Integer, Integer> startXCache = new HashMap<Integer, Integer>();

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
        this.underlyingLayer = underlyingLayer;

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
                            structuralDiffs, this.underlyingLayer, this.columnIndexOrder, true);
                    StructuralChangeEventHelper.handleColumnInsert(
                            structuralDiffs, this.underlyingLayer, this.columnIndexOrder, true);
                    // update index-position mapping
                    refreshIndexPositionMapping();
                }
                invalidateCache();
            }
        }
        super.handleLayerEvent(event);
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
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.columnIndexOrder) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(prefix + PERSISTENCE_KEY_COLUMN_INDEX_ORDER, strBuilder.toString());
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_COLUMN_INDEX_ORDER);

        if (property != null) {
            List<Integer> newColumnIndexOrder = new ArrayList<Integer>();
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                newColumnIndexOrder.add(Integer.valueOf(index));
            }

            if (isRestoredStateValid(newColumnIndexOrder)) {
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
     */
    protected boolean isRestoredStateValid(List<Integer> newColumnIndexOrder) {
        if (newColumnIndexOrder.size() != getColumnCount()) {
            LOG.error("Number of persisted columns (" + newColumnIndexOrder.size() + ") " + //$NON-NLS-1$ //$NON-NLS-2$
                    "is not the same as the number of columns in the data source (" //$NON-NLS-1$
                    + getColumnCount() + ").\n" + //$NON-NLS-1$
                    "Skipping restore of column ordering"); //$NON-NLS-1$
            return false;
        }

        for (Integer index : newColumnIndexOrder) {
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
        return this.columnIndexOrder;
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
        Integer result = this.indexPositionMapping.get(columnIndex);
        return (result != null) ? result : -1;
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        int columnIndex = getColumnIndexByPosition(localColumnPosition);
        return this.underlyingLayer.getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        int columnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingColumnPosition);
        return getColumnPositionByIndex(columnIndex);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
        List<Integer> reorderedColumnPositions = new ArrayList<Integer>();
        for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
            for (int underlyingColumnPosition = underlyingColumnPositionRange.start; underlyingColumnPosition < underlyingColumnPositionRange.end; underlyingColumnPosition++) {
                int localColumnPosition = underlyingToLocalColumnPosition(sourceUnderlyingLayer, underlyingColumnPosition);
                reorderedColumnPositions.add(Integer.valueOf(localColumnPosition));
            }
        }
        Collections.sort(reorderedColumnPositions);

        return PositionUtil.getRanges(reorderedColumnPositions);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return LayerUtil.getColumnPositionByX(this, x);
    }

    @Override
    public int getStartXOfColumnPosition(int targetColumnPosition) {
        Integer cachedStartX = this.startXCache.get(Integer.valueOf(targetColumnPosition));
        if (cachedStartX != null) {
            return cachedStartX.intValue();
        }

        int aggregateWidth = 0;
        for (int columnPosition = 0; columnPosition < targetColumnPosition; columnPosition++) {
            aggregateWidth += this.underlyingLayer.getColumnWidthByPosition(localToUnderlyingColumnPosition(columnPosition));
        }

        this.startXCache.put(Integer.valueOf(targetColumnPosition), Integer.valueOf(aggregateWidth));
        return aggregateWidth;
    }

    /**
     * Initialize the internal column index ordering from a clean state, which
     * means it reflects the ordering from the underlying layer.
     */
    private void populateIndexOrder() {
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
     */
    private void refreshIndexPositionMapping() {
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
        return this.underlyingLayer.getRowPositionByIndex(rowIndex);
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

        Integer fromColumnIndex = this.columnIndexOrder.get(fromColumnPosition);
        this.columnIndexOrder.add(toColumnPosition, fromColumnIndex);
        this.columnIndexOrder.remove(fromColumnPosition + (fromColumnPosition > toColumnPosition ? 1 : 0));

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
        // get the indexes before the move operation
        List<Integer> fromColumnIndexes = new ArrayList<Integer>();
        for (int fromColumnPosition : fromColumnPositions) {
            fromColumnIndexes.add(getColumnIndexByPosition(fromColumnPosition));
        }
        int toColumnIndex = getColumnIndexByPosition(toColumnPosition);

        // Moving from left to right
        final int fromColumnPositionsCount = fromColumnPositions.size();

        if (toColumnPosition > fromColumnPositions.get(fromColumnPositionsCount - 1)) {
            int firstColumnPosition = fromColumnPositions.get(0).intValue();

            int moved = 0;
            for (int columnCount = 0; columnCount < fromColumnPositionsCount; columnCount++) {
                final int fromColumnPosition = fromColumnPositions.get(columnCount) - moved;
                moveColumn(fromColumnPosition, toColumnPosition, reorderToLeftEdge);
                moved++;
                if (fromColumnPosition < firstColumnPosition) {
                    firstColumnPosition = fromColumnPosition;
                }
            }
        } else if (toColumnPosition < fromColumnPositions.get(fromColumnPositionsCount - 1).intValue()) {
            // Moving from right to left
            int targetColumnPosition = toColumnPosition;
            for (Integer fromColumnPosition : fromColumnPositions) {
                final int fromColumnPositionInt = fromColumnPosition.intValue();
                moveColumn(fromColumnPositionInt, targetColumnPosition++, reorderToLeftEdge);
            }
        }

        fireLayerEvent(new ColumnReorderEvent(this, fromColumnPositions, fromColumnIndexes, toColumnPosition, toColumnIndex, reorderToLeftEdge));
    }

    /**
     * Clear the internal cache.
     */
    private void invalidateCache() {
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
