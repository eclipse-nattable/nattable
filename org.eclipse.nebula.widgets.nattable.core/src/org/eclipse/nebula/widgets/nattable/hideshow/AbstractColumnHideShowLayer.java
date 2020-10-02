/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.SpanningLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;

/**
 * Abstract implementation for column hide/show operations.
 */
public abstract class AbstractColumnHideShowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private MutableIntIntMap cachedVisibleColumnIndexPositionMapping;
    private MutableIntIntMap cachedVisibleColumnPositionIndexMapping;
    private MutableIntIntMap cachedHiddenColumnIndexPositionMapping;
    private MutableIntIntMap startXCache = IntIntMaps.mutable.empty();

    /**
     * Constructor.
     *
     * @param underlyingLayer
     *            The underlying layer.
     */
    public AbstractColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
    }

    /**
     * @return The underlying layer.
     * @since 2.0
     */
    @Override
    protected IUniqueIndexLayer getUnderlyingLayer() {
        return (IUniqueIndexLayer) super.getUnderlyingLayer();
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (hasHiddenColumns() && event instanceof ColumnReorderEvent) {
            // we need to convert the before positions in the event BEFORE the
            // local states are changed, otherwise we are not able to convert
            // the before positions as the changed layer states would return
            // incorrect values
            ColumnReorderEvent reorderEvent = (ColumnReorderEvent) event;

            int[] fromPositions = reorderEvent.getBeforeFromColumnIndexes().stream()
                    .mapToInt(Integer::intValue)
                    .map(this::getColumnPositionByIndex)
                    .toArray();
            Collection<Range> fromRanges = PositionUtil.getRanges(fromPositions);

            int pos = -1;
            if (!isColumnIndexHidden(reorderEvent.getBeforeToColumnIndex())) {
                pos = getColumnPositionByIndex(reorderEvent.getBeforeToColumnIndex());
            } else {
                int i = 1;
                while (pos < 0) {
                    int next = reorderEvent.getBeforeToColumnPosition() + i;
                    if (next >= this.underlyingLayer.getColumnCount()) {
                        break;
                    }
                    pos = underlyingToLocalColumnPosition(this.underlyingLayer, next);
                    i++;
                }
                if (pos >= 0) {
                    reorderEvent.setBeforeToColumnIndex(getColumnIndexByPosition(pos));
                }
            }
            if (pos >= 0) {
                reorderEvent.setConvertedBeforePositions(this, fromRanges, pos);
            }
        }

        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                invalidateCache();
            }
        } else if (event instanceof VisualRefreshEvent) {
            // visual change, e.g. font change, the startXCache needs to be
            // cleared in order to re-render correctly
            this.startXCache = IntIntMaps.mutable.empty();
        }
        super.handleLayerEvent(event);
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        if (!hasHiddenColumns()) {
            return super.getColumnCount();
        }
        return getCachedVisibleColumnIndexPositionMapping().size();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (columnPosition < 0 || columnPosition >= getColumnCount()) {
            return -1;
        }

        if (!hasHiddenColumns()) {
            return super.getColumnIndexByPosition(columnPosition);
        }

        return getCachedVisibleColumnPositionIndexMapping().getIfAbsent(columnPosition, -1);
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        if (!hasHiddenColumns()) {
            return getUnderlyingLayer().getColumnPositionByIndex(columnIndex);
        }

        return getCachedVisibleColumnIndexPositionMapping().getIfAbsent(columnIndex, -1);
    }

    /**
     * Get the local column positions for the given column indexes.
     *
     * @param columnIndexes
     *            The column indexes for which the local column positions are
     *            requested.
     * @return The local column positions for the given column indexes.
     */
    public Collection<Integer> getColumnPositionsByIndexes(Collection<Integer> columnIndexes) {
        Collection<Integer> columnPositions = new HashSet<>();
        for (int columnIndex : columnIndexes) {
            columnPositions.add(getColumnPositionByIndex(columnIndex));
        }
        return columnPositions;
    }

    /**
     * Get the local column positions for the given column indexes.
     *
     * @param columnIndexes
     *            The column indexes for which the local column positions are
     *            requested.
     * @return The local column positions for the given column indexes.
     *
     * @since 2.0
     */
    public int[] getColumnPositionsByIndexes(int... columnIndexes) {
        return (columnIndexes != null && columnIndexes.length > 0)
                ? Arrays.stream(columnIndexes).map(this::getColumnPositionByIndex).toArray()
                : new int[0];
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        if (localColumnPosition < 0 || localColumnPosition >= getColumnCount()) {
            return -1;
        }

        if (!hasHiddenColumns()) {
            return localColumnPosition;
        }

        int columnIndex = getColumnIndexByPosition(localColumnPosition);
        return getUnderlyingLayer().getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        if (!hasHiddenColumns()) {
            return underlyingColumnPosition;
        }

        int columnIndex = getUnderlyingLayer().getColumnIndexByPosition(underlyingColumnPosition);
        int columnPosition = getColumnPositionByIndex(columnIndex);
        if (columnPosition >= 0) {
            return columnPosition;
        } else {
            return getCachedHiddenColumnIndexPositionMapping().getIfAbsent(columnIndex, -1);
        }
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
        Collection<Range> localColumnPositionRanges = new ArrayList<Range>(underlyingColumnPositionRanges.size());

        for (Range underlyingColumnPositionRange : underlyingColumnPositionRanges) {
            int startColumnPosition = getAdjustedUnderlyingToLocalStartPosition(
                    sourceUnderlyingLayer,
                    underlyingColumnPositionRange.start,
                    underlyingColumnPositionRange.end);
            int endColumnPosition = getAdjustedUnderlyingToLocalEndPosition(
                    sourceUnderlyingLayer,
                    underlyingColumnPositionRange.end,
                    underlyingColumnPositionRange.start);

            // teichstaedt: fixes the problem that ranges where added even if
            // the corresponding startPosition weren't found in the underlying
            // layer. Without that fix a bunch of ranges of kind Range [-1, 180]
            // which causes strange behaviour in Freeze- and other Layers were
            // returned.
            if (startColumnPosition > -1) {
                localColumnPositionRanges.add(new Range(startColumnPosition, endColumnPosition));
            }
        }

        return localColumnPositionRanges;
    }

    private int getAdjustedUnderlyingToLocalStartPosition(
            ILayer sourceUnderlyingLayer,
            int startUnderlyingPosition,
            int endUnderlyingPosition) {

        int localStartColumnPosition =
                underlyingToLocalColumnPosition(
                        sourceUnderlyingLayer,
                        startUnderlyingPosition);
        int offset = 0;
        while (localStartColumnPosition < 0
                && (startUnderlyingPosition + offset < endUnderlyingPosition)) {
            localStartColumnPosition =
                    underlyingToLocalColumnPosition(
                            sourceUnderlyingLayer,
                            startUnderlyingPosition + offset++);
        }
        return localStartColumnPosition;
    }

    private int getAdjustedUnderlyingToLocalEndPosition(
            ILayer sourceUnderlyingLayer,
            int endUnderlyingPosition,
            int startUnderlyingPosition) {

        int localEndColumnPosition =
                underlyingToLocalColumnPosition(
                        sourceUnderlyingLayer,
                        endUnderlyingPosition - 1);
        int offset = 0;
        while (localEndColumnPosition < 0
                && (endUnderlyingPosition - offset > startUnderlyingPosition)) {
            localEndColumnPosition =
                    underlyingToLocalColumnPosition(
                            sourceUnderlyingLayer,
                            endUnderlyingPosition - offset++);
        }
        return localEndColumnPosition + 1;
    }

    // Width

    @Override
    public int getWidth() {
        if (getColumnCount() == 0) {
            return 0;
        }

        int lastColumnPosition = getColumnCount() - 1;
        return getStartXOfColumnPosition(lastColumnPosition) + getColumnWidthByPosition(lastColumnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return LayerUtil.getColumnPositionByX(this, x);
    }

    @Override
    public int getStartXOfColumnPosition(int localColumnPosition) {
        int cachedStartX = this.startXCache.getIfAbsent(localColumnPosition, -1);
        if (cachedStartX != -1) {
            return cachedStartX;
        }

        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        int underlyingPosition = localToUnderlyingColumnPosition(localColumnPosition);
        if (underlyingPosition < 0) {
            return -1;
        }
        int underlyingStartX = underlyingLayer.getStartXOfColumnPosition(underlyingPosition);
        if (underlyingStartX < 0) {
            return -1;
        }

        for (int hiddenIndex : getHiddenColumnIndexesArray()) {
            int hiddenPosition = underlyingLayer.getColumnPositionByIndex(hiddenIndex);
            // if the hidden position is -1, it is hidden in the underlying
            // layer therefore the underlying layer should handle the
            // positioning
            if (hiddenPosition >= 0 && hiddenPosition <= underlyingPosition) {
                underlyingStartX -= underlyingLayer.getColumnWidthByPosition(hiddenPosition);
            }
        }

        this.startXCache.put(localColumnPosition, underlyingStartX);
        return underlyingStartX;
    }

    // Vertical features

    // Rows

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return getUnderlyingLayer().getRowPositionByIndex(rowIndex);
    }

    // Hide/show

    /**
     * Will check if the column at the specified index is hidden or not.
     *
     * @param columnIndex
     *            The column index of the column whose visibility state should
     *            be checked.
     * @return <code>true</code> if the column at the specified index is hidden,
     *         <code>false</code> if it is visible.
     */
    public abstract boolean isColumnIndexHidden(int columnIndex);

    /**
     * Will collect and return all indexes of the columns that are hidden in
     * this layer.
     * <p>
     * <b>Note:</b> It is not intended that it also collects the column indexes
     * of underlying layers. This would cause issues on calculating positions,
     * as every layer is responsible for those calculations itself.
     * </p>
     * <p>
     * Since 2.0 it is recommended to use {@link #getHiddenColumnIndexesArray()}
     * to avoid unnecessary autoboxing operations.
     * </p>
     *
     * @return Collection of all column indexes that are hidden in this layer.
     */
    public abstract Collection<Integer> getHiddenColumnIndexes();

    /**
     * Will collect and return all indexes of the columns that are hidden in
     * this layer.
     * <p>
     * <b>Note:</b> It is not intended that it also collects the column indexes
     * of underlying layers. This would cause issues on calculating positions,
     * as every layer is responsible for those calculations itself.
     * </p>
     *
     * @return All column indexes that are hidden in this layer.
     *
     * @since 2.0
     */
    public abstract int[] getHiddenColumnIndexesArray();

    /**
     * Check if this layer actively hides columns.
     *
     * @return <code>true</code> if columns are hidden by this layer,
     *         <code>false</code> if not.
     *
     * @since 2.0
     */
    public abstract boolean hasHiddenColumns();

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        ILayerCell cell = super.getCellByPosition(columnPosition, rowPosition);
        if (cell != null && cell.isSpannedCell()) {
            // the spanning needs to be updated to reflect the
            // hiding accordingly
            int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
            int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
            ILayerCell underlyingCell = this.underlyingLayer.getCellByPosition(underlyingColumnPosition, underlyingRowPosition);

            boolean columnSpanUpdated = false;
            int columnSpan = underlyingCell.getColumnSpan();
            for (int column = 0; column < underlyingCell.getColumnSpan(); column++) {
                int columnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingCell.getOriginColumnPosition() + column);
                if (isColumnIndexHidden(columnIndex)) {
                    columnSpan--;
                    columnSpanUpdated = true;
                }
            }

            if (columnSpanUpdated) {
                cell = new SpanningLayerCell(cell, columnSpan, cell.getRowSpan());
            }
        }
        return cell;
    }

    // Cache

    /**
     * Invalidate the cache to ensure that information is rebuild.
     */
    protected synchronized void invalidateCache() {
        this.cachedVisibleColumnIndexPositionMapping = null;
        this.cachedVisibleColumnPositionIndexMapping = null;
        this.cachedHiddenColumnIndexPositionMapping = null;
        this.startXCache = IntIntMaps.mutable.empty();
    }

    private synchronized IntIntMap getCachedVisibleColumnIndexPositionMapping() {
        if (this.cachedVisibleColumnIndexPositionMapping == null) {
            cacheVisibleColumnIndexes();
        }
        return this.cachedVisibleColumnIndexPositionMapping;
    }

    private synchronized IntIntMap getCachedVisibleColumnPositionIndexMapping() {
        if (this.cachedVisibleColumnPositionIndexMapping == null) {
            cacheVisibleColumnIndexes();
        }
        return this.cachedVisibleColumnPositionIndexMapping;
    }

    private synchronized IntIntMap getCachedHiddenColumnIndexPositionMapping() {
        if (this.cachedHiddenColumnIndexPositionMapping == null) {
            cacheVisibleColumnIndexes();
        }
        return this.cachedHiddenColumnIndexPositionMapping;
    }

    /**
     * Build up the column caches.
     *
     * @since 2.0
     */
    protected synchronized void cacheVisibleColumnIndexes() {
        this.cachedVisibleColumnIndexPositionMapping = IntIntMaps.mutable.empty();
        this.cachedVisibleColumnPositionIndexMapping = IntIntMaps.mutable.empty();
        this.cachedHiddenColumnIndexPositionMapping = IntIntMaps.mutable.empty();
        this.startXCache = IntIntMaps.mutable.empty();

        // only build up a cache if it is necessary
        if (hasHiddenColumns()) {
            ILayer underlyingLayer = getUnderlyingLayer();
            int columnPosition = 0;
            for (int parentColumnPosition = 0; parentColumnPosition < underlyingLayer.getColumnCount(); parentColumnPosition++) {
                int columnIndex = underlyingLayer.getColumnIndexByPosition(parentColumnPosition);

                if (!isColumnIndexHidden(columnIndex)) {
                    this.cachedVisibleColumnIndexPositionMapping.put(columnIndex, columnPosition);
                    this.cachedVisibleColumnPositionIndexMapping.put(columnPosition, columnIndex);
                    columnPosition++;
                } else {
                    this.cachedHiddenColumnIndexPositionMapping.put(columnIndex, columnPosition);
                }
            }
        }
    }

}
