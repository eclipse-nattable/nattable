/*******************************************************************************
 * Copyright (c) 2013, 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
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
    protected final List<Integer> rowIndexOrder = new ArrayList<Integer>();

    /**
     * The internal mapping of index to position values. Used for performance
     * reasons in {@link #getColumnPositionByIndex(int)} because
     * {@link List#indexOf(Object)} doesn't scale well.
     *
     * @since 1.5
     */
    protected final Map<Integer, Integer> indexPositionMapping = new HashMap<Integer, Integer>();

    /**
     * Caching of the starting y positions of the rows. Used to reduce
     * calculation time on rendering
     */
    private final Map<Integer, Integer> startYCache = new HashMap<Integer, Integer>();

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
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.rowIndexOrder) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(prefix + PERSISTENCE_KEY_ROW_INDEX_ORDER, strBuilder.toString());
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_ROW_INDEX_ORDER);

        if (property != null) {
            List<Integer> newRowIndexOrder = new ArrayList<Integer>();
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                newRowIndexOrder.add(Integer.valueOf(index));
            }

            if (isRestoredStateValid(newRowIndexOrder)) {
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
     */
    protected boolean isRestoredStateValid(List<Integer> newRowIndexOrder) {
        if (newRowIndexOrder.size() != getRowCount()) {
            LOG.error("Number of persisted rows (" + newRowIndexOrder.size() + ") " + //$NON-NLS-1$ //$NON-NLS-2$
                    "is not the same as the number of rows in the data source (" //$NON-NLS-1$
                    + getRowCount() + ").\n" + //$NON-NLS-1$
                    "Skipping restore of row ordering"); //$NON-NLS-1$
            return false;
        }

        for (Integer index : newRowIndexOrder) {
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
        Integer cachedStartY = this.startYCache.get(targetRowPosition);
        if (cachedStartY != null) {
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
     */
    private void populateIndexOrder() {
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
     */
    private void refreshIndexPositionMapping() {
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
        return this.rowIndexOrder;
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition >= 0 && rowPosition < this.rowIndexOrder.size()) {
            return this.rowIndexOrder.get(rowPosition).intValue();
        } else {
            return -1;
        }
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        Integer result = this.indexPositionMapping.get(rowIndex);
        return (result != null) ? result : -1;
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
        List<Integer> reorderedRowPositions = new ArrayList<Integer>();
        for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
            for (int underlyingRowPosition = underlyingRowPositionRange.start; underlyingRowPosition < underlyingRowPositionRange.end; underlyingRowPosition++) {
                int localRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, underlyingRowPositionRange.start);
                reorderedRowPositions.add(localRowPosition);
            }
        }
        Collections.sort(reorderedRowPositions);

        return PositionUtil.getRanges(reorderedRowPositions);
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

        Integer fromRowIndex = this.rowIndexOrder.get(fromRowPosition);
        this.rowIndexOrder.add(toRowPosition, fromRowIndex);
        this.rowIndexOrder.remove(fromRowPosition + (fromRowPosition > toRowPosition ? 1 : 0));

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
        // get the indexes before the move operation
        List<Integer> fromRowIndexes = new ArrayList<Integer>();
        for (int fromRowPosition : fromRowPositions) {
            fromRowIndexes.add(getRowIndexByPosition(fromRowPosition));
        }
        int toRowIndex = getRowIndexByPosition(toRowPosition);

        final int fromRowPositionsCount = fromRowPositions.size();

        if (toRowPosition > fromRowPositions.get(fromRowPositionsCount - 1)) {
            // Moving from top to bottom
            int firstRowPosition = fromRowPositions.get(0);

            int moved = 0;
            for (int rowCount = 0; rowCount < fromRowPositionsCount; rowCount++) {
                final int fromRowPosition = fromRowPositions.get(rowCount) - moved;
                moveRow(fromRowPosition, toRowPosition, reorderToTopEdge);
                moved++;
                if (fromRowPosition < firstRowPosition) {
                    firstRowPosition = fromRowPosition;
                }
            }
        } else if (toRowPosition < fromRowPositions.get(fromRowPositionsCount - 1)) {
            // Moving from bottom to top
            int targetRowPosition = toRowPosition;
            for (Integer fromRowPosition : fromRowPositions) {
                final int fromRowPositionInt = fromRowPosition;
                moveRow(fromRowPositionInt, targetRowPosition++, reorderToTopEdge);
            }
        }

        fireLayerEvent(new RowReorderEvent(this, fromRowPositions, fromRowIndexes, toRowPosition, toRowIndex, reorderToTopEdge));
    }

    /**
     * Clear the caching of the starting Y positions
     */
    private void invalidateCache() {
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
