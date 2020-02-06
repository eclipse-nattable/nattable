/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.ResetRowReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.config.DefaultRowReorderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;

/**
 * Layer that is used to add the functionality for row reordering.
 *
 * @see DefaultRowReorderLayerConfiguration
 */
public class RowReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private static final Log LOG = LogFactory.getLog(RowReorderLayer.class);

    public static final String PERSISTENCE_KEY_ROW_INDEX_ORDER = ".rowIndexOrder"; //$NON-NLS-1$

    private final IUniqueIndexLayer underlyingLayer;

    /**
     * The local cache of the row index order. Used to track the reordering
     * performed by this layer. Position Y in the List contains the index of row
     * at position Y.
     */
    protected final MutableIntList rowIndexOrder = IntLists.mutable.empty();

    /**
     * The internal mapping of index to position values. Used for performance
     * reasons in {@link #getColumnPositionByIndex(int)} because
     * {@link List#indexOf(Object)} doesn't scale well.
     *
     * @since 1.5
     */
    protected final MutableIntIntMap indexPositionMapping = IntIntMaps.mutable.empty();

    /**
     * Caching of the starting y positions of the rows. Used to reduce
     * calculation time on rendering
     */
    private final MutableIntIntMap startYCache = IntIntMaps.mutable.empty();

    /**
     * Local cached position of the row that is currently reordered.
     */
    private int reorderFromRowPosition;

    /**
     * Creates a {@link RowReorderLayer} on top of the given
     * {@link IUniqueIndexLayer} and adds the
     * {@link DefaultRowReorderLayerConfiguration}.
     *
     * @param underlyingLayer
     *            The underlying layer.
     */
    public RowReorderLayer(IUniqueIndexLayer underlyingLayer) {
        this(underlyingLayer, true);
    }

    /**
     * Creates a {@link RowReorderLayer} on top of the given
     * {@link IUniqueIndexLayer}.
     *
     * @param underlyingLayer
     *            The underlying layer.
     * @param useDefaultConfiguration
     *            <code>true</code> to add the
     *            {@link DefaultRowReorderLayerConfiguration}
     */
    public RowReorderLayer(IUniqueIndexLayer underlyingLayer, boolean useDefaultConfiguration) {
        super(underlyingLayer);
        this.underlyingLayer = underlyingLayer;

        populateIndexOrder();

        registerCommandHandlers();

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultRowReorderLayerConfiguration());
        }
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isVerticalStructureChanged()) {
                Collection<StructuralDiff> structuralDiffs = structuralChangeEvent.getRowDiffs();
                if (structuralDiffs == null) {
                    // Assume everything changed
                    populateIndexOrder();
                } else {
                    // only react on ADD or DELETE and not on CHANGE
                    StructuralChangeEventHelper.handleRowDelete(
                            structuralDiffs, this.underlyingLayer, this.rowIndexOrder, true);
                    StructuralChangeEventHelper.handleRowInsert(
                            structuralDiffs, this.underlyingLayer, this.rowIndexOrder, true);
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
        registerCommandHandler(new RowReorderCommandHandler(this));
        registerCommandHandler(new RowReorderStartCommandHandler(this));
        registerCommandHandler(new RowReorderEndCommandHandler(this));
        registerCommandHandler(new MultiRowReorderCommandHandler(this));
        registerCommandHandler(new ResetRowReorderCommandHandler(this));
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        if (this.rowIndexOrder.size() > 0) {
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_ROW_INDEX_ORDER,
                    this.rowIndexOrder.makeString(IPersistable.VALUE_SEPARATOR));
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_ROW_INDEX_ORDER);

        if (property != null) {
            MutableIntList newRowIndexOrder = IntLists.mutable.empty();
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                newRowIndexOrder.add(Integer.parseInt(index));
            }

            if (isRestoredStateValid(newRowIndexOrder.toArray())) {
                this.rowIndexOrder.clear();
                this.rowIndexOrder.addAll(newRowIndexOrder);
                // refresh index-position mapping
                refreshIndexPositionMapping();
            }

        }
        invalidateCache();
        fireLayerEvent(new RowStructuralRefreshEvent(this));
    }

    /**
     * Ensure that rows haven't changed in the underlying data source
     *
     * @param newRowIndexOrder
     *            restored from the properties file.
     * @since 2.0
     */
    protected boolean isRestoredStateValid(int[] newRowIndexOrder) {
        if (newRowIndexOrder.length != getRowCount()) {
            LOG.error("Number of persisted rows (" + newRowIndexOrder.length + ") " + //$NON-NLS-1$ //$NON-NLS-2$
                    "is not the same as the number of rows in the data source (" //$NON-NLS-1$
                    + getRowCount() + ").\n" + //$NON-NLS-1$
                    "Skipping restore of row ordering"); //$NON-NLS-1$
            return false;
        }

        for (int index : newRowIndexOrder) {
            if (!this.indexPositionMapping.containsKey(index)) {
                LOG.error("Row index: " + index + " being restored, is not a available in the data soure.\n" + //$NON-NLS-1$ //$NON-NLS-2$
                        "Skipping restore of row ordering"); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    // Columns

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return this.underlyingLayer.getColumnPositionByIndex(columnIndex);
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return LayerUtil.getRowPositionByY(this, y);
    }

    @Override
    public int getStartYOfRowPosition(int targetRowPosition) {
        int cachedStartY = this.startYCache.getIfAbsent(targetRowPosition, -1);
        if (cachedStartY != -1) {
            return cachedStartY;
        }

        int aggregateWidth = 0;
        for (int rowPosition = 0; rowPosition < targetRowPosition; rowPosition++) {
            aggregateWidth += this.underlyingLayer.getRowHeightByPosition(localToUnderlyingRowPosition(rowPosition));
        }

        this.startYCache.put(targetRowPosition, aggregateWidth);
        return aggregateWidth;
    }

    /**
     * Initially populate the index order to the local cache.
     *
     * @since 1.6
     */
    protected void populateIndexOrder() {
        this.rowIndexOrder.clear();
        ILayer underlyingLayer = getUnderlyingLayer();
        for (int rowPosition = 0; rowPosition < underlyingLayer.getRowCount(); rowPosition++) {
            int index = underlyingLayer.getRowIndexByPosition(rowPosition);
            this.rowIndexOrder.add(index);
            this.indexPositionMapping.put(index, rowPosition);
        }
    }

    /**
     * Initializes the internal index-position-mapping to reflect the internal
     * row-index-order.
     *
     * @since 1.6
     */
    protected void refreshIndexPositionMapping() {
        this.indexPositionMapping.clear();
        for (int position = 0; position < this.rowIndexOrder.size(); position++) {
            int index = this.rowIndexOrder.get(position);
            this.indexPositionMapping.put(index, position);
        }
    }

    // Vertical features

    // Rows
    /**
     * @return The local cache of the row index order.
     */
    public List<Integer> getRowIndexOrder() {
        return this.rowIndexOrder.primitiveStream().boxed().collect(Collectors.toList());
    }

    /**
     * @return The local cache of the row index order.
     * @since 2.0
     */
    public int[] getRowIndexOrderArray() {
        return this.rowIndexOrder.toArray();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition >= 0 && rowPosition < this.rowIndexOrder.size()) {
            return this.rowIndexOrder.get(rowPosition);
        } else {
            return -1;
        }
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return this.indexPositionMapping.getIfAbsent(rowIndex, -1);
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        int rowIndex = getRowIndexByPosition(localRowPosition);
        return this.underlyingLayer.getRowPositionByIndex(rowIndex);
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
        int rowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingRowPosition);
        return getRowPositionByIndex(rowIndex);
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
        MutableIntList reorderedRowPositions = IntLists.mutable.empty();
        for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
            for (int underlyingRowPosition = underlyingRowPositionRange.start; underlyingRowPosition < underlyingRowPositionRange.end; underlyingRowPosition++) {
                int localRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.start);
                reorderedRowPositions.add(localRowPosition);
            }
        }

        return PositionUtil.getRanges(reorderedRowPositions.toSortedArray());
    }

    /**
     * Moves the row at the given from position to the <i>TOP</i> of the of the
     * given to position. This is the internal implementation for reordering a
     * row.
     *
     * @param fromRowPosition
     *            row position to move
     * @param toRowPosition
     *            position to move the row to
     * @param reorderToTopEdge
     *            whether the move should be done above the given to position or
     *            not
     */
    private void moveRow(int fromRowPosition, int toRowPosition, boolean reorderToTopEdge) {
        if (!reorderToTopEdge) {
            toRowPosition++;
        }

        int fromRowIndex = this.rowIndexOrder.get(fromRowPosition);
        this.rowIndexOrder.addAtIndex(toRowPosition, fromRowIndex);
        this.rowIndexOrder.removeAtIndex(fromRowPosition + (fromRowPosition > toRowPosition ? 1 : 0));

        // update index-position mapping
        refreshIndexPositionMapping();

        invalidateCache();
    }

    /**
     * Reorders the row at the given from position to the <i>TOP</i> of the of
     * the given to position. Will calculate whether the move is done above the
     * to position or not regarding the position in the NatTable.
     *
     * @param fromRowPosition
     *            row position to move
     * @param toRowPosition
     *            position to move the row to
     */
    public void reorderRowPosition(int fromRowPosition, int toRowPosition) {
        boolean reorderToTopEdge;
        if (toRowPosition < getRowCount()) {
            reorderToTopEdge = true;
        } else {
            reorderToTopEdge = false;
            toRowPosition--;
        }
        reorderRowPosition(fromRowPosition, toRowPosition, reorderToTopEdge);
    }

    /**
     * Reorders the row at the given from position to the <i>TOP</i> of the of
     * the given to position.
     *
     * @param fromRowPosition
     *            row position to move
     * @param toRowPosition
     *            position to move the row to
     * @param reorderToTopEdge
     *            whether the move should be done above the given to position or
     *            not
     */
    public void reorderRowPosition(int fromRowPosition, int toRowPosition, boolean reorderToTopEdge) {
        // get the indexes before the move operation
        int fromRowIndex = getRowIndexByPosition(fromRowPosition);
        int toRowIndex = getRowIndexByPosition(toRowPosition);
        moveRow(fromRowPosition, toRowPosition, reorderToTopEdge);
        fireLayerEvent(new RowReorderEvent(this, fromRowPosition, fromRowIndex, toRowPosition, toRowIndex, reorderToTopEdge));
    }

    /**
     * Reorders the rows at the given from positions to the <i>TOP</i> of the of
     * the given to position. Will calculate whether the move is done above the
     * to position or not regarding the position in the NatTable.
     *
     * @param fromRowPositions
     *            row positions to move
     * @param toRowPosition
     *            position to move the rows to
     */
    public void reorderMultipleRowPositions(List<Integer> fromRowPositions, int toRowPosition) {
        reorderMultipleRowPositions(
                fromRowPositions.stream().mapToInt(Integer::intValue).toArray(),
                toRowPosition);
    }

    /**
     * Reorders the rows at the given from positions to the <i>TOP</i> of the of
     * the given to position. Will calculate whether the move is done above the
     * to position or not regarding the position in the NatTable.
     *
     * @param fromRowPositions
     *            row positions to move
     * @param toRowPosition
     *            position to move the rows to
     * @since 2.0
     */
    public void reorderMultipleRowPositions(int[] fromRowPositions, int toRowPosition) {
        boolean reorderToTopEdge;
        if (toRowPosition < getRowCount()) {
            reorderToTopEdge = true;
        } else {
            reorderToTopEdge = false;
            toRowPosition--;
        }
        reorderMultipleRowPositions(fromRowPositions, toRowPosition, reorderToTopEdge);
    }

    /**
     * Reorders the rows at the given from positions to the <i>TOP</i> of the of
     * the given to position.
     *
     * @param fromRowPositions
     *            row positions to move
     * @param toRowPosition
     *            position to move the rows to
     * @param reorderToTopEdge
     *            whether the move should be done above the given to position or
     *            not
     */
    public void reorderMultipleRowPositions(List<Integer> fromRowPositions, int toRowPosition, boolean reorderToTopEdge) {
        reorderMultipleRowPositions(
                fromRowPositions.stream().mapToInt(Integer::intValue).toArray(),
                toRowPosition,
                reorderToTopEdge);
    }

    /**
     * Reorders the rows at the given from positions to the <i>TOP</i> of the of
     * the given to position.
     *
     * @param fromRowPositions
     *            row positions to move
     * @param toRowPosition
     *            position to move the rows to
     * @param reorderToTopEdge
     *            whether the move should be done above the given to position or
     *            not
     * @since 2.0
     */
    public void reorderMultipleRowPositions(int[] fromRowPositions, int toRowPosition, boolean reorderToTopEdge) {
        // the position collection needs to be sorted so the move works
        // correctly
        Arrays.sort(fromRowPositions);

        // get the indexes before the move operation
        int[] fromRowIndexes = Arrays.stream(fromRowPositions).map(this::getRowIndexByPosition).toArray();
        int toRowIndex = getRowIndexByPosition(toRowPosition);

        final int fromRowPositionsCount = fromRowPositions.length;

        if (toRowPosition > fromRowPositions[fromRowPositionsCount - 1]) {
            // Moving from top to bottom
            int firstRowPosition = fromRowPositions[0];

            int moved = 0;
            for (int rowCount = 0; rowCount < fromRowPositionsCount; rowCount++) {
                final int fromRowPosition = fromRowPositions[rowCount] - moved;
                moveRow(fromRowPosition, toRowPosition, reorderToTopEdge);
                moved++;
                if (fromRowPosition < firstRowPosition) {
                    firstRowPosition = fromRowPosition;
                }
            }
        } else if (toRowPosition < fromRowPositions[fromRowPositionsCount - 1]) {
            // Moving from bottom to top
            int targetRowPosition = toRowPosition;
            for (int fromRowPosition : fromRowPositions) {
                final int fromRowPositionInt = fromRowPosition;
                moveRow(fromRowPositionInt, targetRowPosition++, reorderToTopEdge);
            }
        }

        fireLayerEvent(new RowReorderEvent(this, fromRowPositions, fromRowIndexes, toRowPosition, toRowIndex, reorderToTopEdge));
    }

    /**
     * Reorders the given from-rows identified by index to the specified edge of
     * the row to move to and fires a {@link RowReorderEvent}. This method can
     * be used to reorder rows that are hidden in a higher level, e.g. to
     * reorder a row group that has hidden rows.
     *
     * @param fromRowIndexes
     *            row indexes to move
     * @param toRowPosition
     *            position to move the rows to
     * @param reorderToTopEdge
     *            whether the move should be done above the given to position or
     *            not
     *
     * @since 1.6
     */
    public void reorderMultipleRowIndexes(List<Integer> fromRowIndexes, int toRowPosition, boolean reorderToTopEdge) {
        reorderMultipleRowIndexes(
                fromRowIndexes.stream().mapToInt(Integer::intValue).toArray(),
                toRowPosition,
                reorderToTopEdge);
    }

    /**
     * Reorders the given from-rows identified by index to the specified edge of
     * the row to move to and fires a {@link RowReorderEvent}. This method can
     * be used to reorder rows that are hidden in a higher level, e.g. to
     * reorder a row group that has hidden rows.
     *
     * @param fromRowIndexes
     *            row indexes to move
     * @param toRowPosition
     *            position to move the rows to
     * @param reorderToTopEdge
     *            whether the move should be done above the given to position or
     *            not
     *
     * @since 2.0
     */
    public void reorderMultipleRowIndexes(int[] fromRowIndexes, int toRowPosition, boolean reorderToTopEdge) {
        // calculate positions from indexes
        int[] fromRowPositions = Arrays.stream(fromRowIndexes).map(this::getRowPositionByIndex).toArray();
        reorderMultipleRowPositions(fromRowPositions, toRowPosition, reorderToTopEdge);
    }

    /**
     * Clear the caching of the starting Y positions
     *
     * @since 1.6
     */
    protected void invalidateCache() {
        this.startYCache.clear();
    }

    /**
     * @return Local cached position of the row that is currently reordered.
     */
    public int getReorderFromRowPosition() {
        return this.reorderFromRowPosition;
    }

    /**
     * Locally cache the position of the row that is currently reordered.
     *
     * @param fromRowPosition
     *            Position of the row that is currently reordered.
     */
    public void setReorderFromRowPosition(int fromRowPosition) {
        this.reorderFromRowPosition = fromRowPosition;
    }

    /**
     * Resets the reordering tracked by this layer.
     *
     * @since 1.6
     */
    public void resetReorder() {
        populateIndexOrder();
        invalidateCache();
        fireLayerEvent(new RowStructuralRefreshEvent(this));
    }

}
