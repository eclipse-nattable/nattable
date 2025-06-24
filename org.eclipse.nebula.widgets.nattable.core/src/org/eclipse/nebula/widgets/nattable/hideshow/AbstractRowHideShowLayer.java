/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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

import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.map.primitive.IntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.SpanningLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;

/**
 * Abstract implementation for row hide/show operations.
 */
public abstract class AbstractRowHideShowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private MutableIntIntMap cachedVisibleRowIndexPositionMapping;
    private MutableIntIntMap cachedVisibleRowPositionIndexMapping;
    private MutableIntIntMap cachedHiddenRowIndexPositionMapping;
    private MutableIntIntMap startYCache = IntIntMaps.mutable.empty();

    /**
     * Constructor.
     *
     * @param underlyingLayer
     *            The underlying layer.
     */
    public AbstractRowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
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
        if (hasHiddenRows() && event instanceof RowReorderEvent) {
            // we need to convert the before positions in the event BEFORE the
            // local states are changed, otherwise we are not able to convert
            // the before positions as the changed layer states would return
            // incorrect values
            RowReorderEvent reorderEvent = (RowReorderEvent) event;

            int[] fromPositions = reorderEvent.getBeforeFromRowIndexes().stream()
                    .mapToInt(Integer::intValue)
                    .map(this::getRowPositionByIndex)
                    .filter(pos -> pos >= 0)
                    .toArray();
            Collection<Range> fromRanges = PositionUtil.getRanges(fromPositions);

            int pos = -1;
            if (!isRowIndexHidden(reorderEvent.getBeforeToRowIndex())) {
                pos = getRowPositionByIndex(reorderEvent.getBeforeToRowIndex());
            } else {
                int i = 1;
                while (pos < 0) {
                    int next = reorderEvent.getBeforeToRowPosition() + i;
                    if (next >= this.underlyingLayer.getColumnCount()) {
                        break;
                    }
                    pos = underlyingToLocalRowPosition(this.underlyingLayer, next);
                    i++;
                }
                if (pos >= 0) {
                    reorderEvent.setBeforeToRowIndex(getRowIndexByPosition(pos));
                }
            }
            if (pos >= 0) {
                reorderEvent.setConvertedBeforePositions(this, fromRanges, pos);
            }
        }

        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isVerticalStructureChanged()) {
                // vertical structure has changed, update cached row information
                invalidateCache();
            }
        } else if (event instanceof VisualRefreshEvent) {
            // visual change, e.g. font change, the startYCache needs to be
            // cleared in order to re-render correctly
            this.startYCache = IntIntMaps.mutable.empty();
        }
        super.handleLayerEvent(event);
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ConfigureScalingCommand) {
            invalidateCache();
        }
        return super.doCommand(command);
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return getUnderlyingLayer().getColumnPositionByIndex(columnIndex);
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        if (!hasHiddenRows()) {
            return super.getRowCount();
        }
        return getCachedVisibleRowIndexPositionMapping().size();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition < 0 || rowPosition >= getRowCount()) {
            return -1;
        }

        if (!hasHiddenRows()) {
            return super.getRowIndexByPosition(rowPosition);
        }

        return getCachedVisibleRowPositionIndexMapping().getIfAbsent(rowPosition, -1);
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        if (!hasHiddenRows()) {
            return getUnderlyingLayer().getRowPositionByIndex(rowIndex);
        }

        return getCachedVisibleRowIndexPositionMapping().getIfAbsent(rowIndex, -1);
    }

    /**
     * Get the local row positions for the given row indexes.
     *
     * @param rowIndexes
     *            The row indexes for which the local row positions are
     *            requested.
     * @return The local row positions for the given row indexes.
     */
    public Collection<Integer> getRowPositionsByIndexes(Collection<Integer> rowIndexes) {
        Collection<Integer> rowPositions = new HashSet<>();
        for (int rowIndex : rowIndexes) {
            rowPositions.add(getRowPositionByIndex(rowIndex));
        }
        return rowPositions;
    }

    /**
     * Get the local row positions for the given row indexes.
     *
     * @param rowIndexes
     *            The row indexes for which the local row positions are
     *            requested.
     * @return The local row positions for the given row indexes.
     *
     * @since 2.0
     */
    public int[] getRowPositionsByIndexes(int... rowIndexes) {
        return (rowIndexes != null && rowIndexes.length > 0)
                ? Arrays.stream(rowIndexes).map(this::getRowPositionByIndex).toArray()
                : new int[0];
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        if (localRowPosition < 0 || localRowPosition >= getRowCount()) {
            return -1;
        }

        if (!hasHiddenRows()) {
            return localRowPosition;
        }

        int rowIndex = getRowIndexByPosition(localRowPosition);
        return getUnderlyingLayer().getRowPositionByIndex(rowIndex);
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
        if (!hasHiddenRows()) {
            return underlyingRowPosition;
        }

        int rowIndex = getUnderlyingLayer().getRowIndexByPosition(underlyingRowPosition);
        int rowPosition = getRowPositionByIndex(rowIndex);
        if (rowPosition >= 0) {
            return rowPosition;
        } else {
            return getCachedHiddenRowIndexPositionMapping().getIfAbsent(rowIndex, -1);
        }
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
        Collection<Range> localRowPositionRanges = new ArrayList<>(underlyingRowPositionRanges.size());

        for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
            int startRowPosition = getAdjustedUnderlyingToLocalStartPosition(
                    sourceUnderlyingLayer,
                    underlyingRowPositionRange.start,
                    underlyingRowPositionRange.end);
            int endRowPosition = getAdjustedUnderlyingToLocalEndPosition(
                    sourceUnderlyingLayer,
                    underlyingRowPositionRange.end,
                    underlyingRowPositionRange.start);

            // teichstaedt: fixes the problem that ranges where added even if
            // the corresponding startPosition weren't found in the underlying
            // layer. Without that fix a bunch of ranges of kind Range [-1, 180]
            // which causes strange behaviour in Freeze- and other Layers were
            // returned.
            if (startRowPosition > -1) {
                localRowPositionRanges.add(new Range(startRowPosition, endRowPosition));
            }
        }

        return localRowPositionRanges;
    }

    private int getAdjustedUnderlyingToLocalStartPosition(
            ILayer sourceUnderlyingLayer,
            int startUnderlyingPosition,
            int endUnderlyingPosition) {
        int localStartRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, startUnderlyingPosition);
        int offset = 0;
        while (localStartRowPosition < 0
                && (startUnderlyingPosition + offset < endUnderlyingPosition)) {
            localStartRowPosition =
                    underlyingToLocalRowPosition(sourceUnderlyingLayer, startUnderlyingPosition + offset++);
        }
        return localStartRowPosition;
    }

    private int getAdjustedUnderlyingToLocalEndPosition(
            ILayer sourceUnderlyingLayer,
            int endUnderlyingPosition,
            int startUnderlyingPosition) {
        int localEndRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, endUnderlyingPosition - 1);
        int offset = 0;
        while (localEndRowPosition < 0
                && (endUnderlyingPosition - offset > startUnderlyingPosition)) {
            localEndRowPosition =
                    underlyingToLocalRowPosition(sourceUnderlyingLayer, endUnderlyingPosition - offset++);
        }
        return localEndRowPosition + 1;
    }

    // Height

    @Override
    public int getHeight() {
        if (getRowCount() == 0) {
            return 0;
        }

        int lastRowPosition = getRowCount() - 1;
        return getStartYOfRowPosition(lastRowPosition) + getRowHeightByPosition(lastRowPosition);
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return LayerUtil.getRowPositionByY(this, y);
    }

    @Override
    public int getStartYOfRowPosition(int localRowPosition) {
        int cachedStartY = this.startYCache.getIfAbsent(localRowPosition, -1);
        if (cachedStartY != -1) {
            return cachedStartY;
        }

        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        int underlyingPosition = localToUnderlyingRowPosition(localRowPosition);
        if (underlyingPosition < 0) {
            return -1;
        }
        int underlyingStartY = underlyingLayer.getStartYOfRowPosition(underlyingPosition);
        if (underlyingStartY < 0) {
            return -1;
        }

        for (int hiddenIndex : getHiddenRowIndexesArray()) {
            int hiddenPosition = underlyingLayer.getRowPositionByIndex(hiddenIndex);
            // if the hidden position is -1, it is hidden in the underlying
            // layer therefore the underlying layer should handle the
            // positioning
            if (hiddenPosition >= 0 && hiddenPosition <= underlyingPosition) {
                underlyingStartY -= underlyingLayer.getRowHeightByPosition(hiddenPosition);
            }
        }

        this.startYCache.put(localRowPosition, underlyingStartY);
        return underlyingStartY;
    }

    // Hide/show

    /**
     * Will check if the row at the specified index is hidden or not. Checks
     * this layer and also the sublayers for the visibility.
     *
     * @param rowIndex
     *            The row index of the row whose visibility state should be
     *            checked.
     * @return <code>true</code> if the row at the specified index is hidden,
     *         <code>false</code> if it is visible.
     */
    public abstract boolean isRowIndexHidden(int rowIndex);

    /**
     * Will collect and return all indexes of the rows that are hidden in this
     * layer.
     * <p>
     * <b>Note:</b> It is not intended that it also collects the row indexes of
     * underlying layers. This would cause issues on calculating positions, as
     * every layer is responsible for those calculations itself.
     * </p>
     * <p>
     * Since 2.0 it is recommended to use {@link #getHiddenRowIndexesArray()} to
     * avoid unnecessary autoboxing operations.
     * </p>
     *
     * @return Collection of all row indexes that are hidden in this layer.
     */
    public abstract Collection<Integer> getHiddenRowIndexes();

    /**
     * Will collect and return all indexes of the rows that are hidden in this
     * layer.
     * <p>
     * <b>Note:</b> It is not intended that it also collects the row indexes of
     * underlying layers. This would cause issues on calculating positions, as
     * every layer is responsible for those calculations itself.
     * </p>
     *
     * @return All row indexes that are hidden in this layer.
     *
     * @since 2.0
     */
    public abstract int[] getHiddenRowIndexesArray();

    /**
     * Check if this layer actively hides rows.
     *
     * @return <code>true</code> if rows are hidden by this layer,
     *         <code>false</code> if not.
     *
     * @since 2.0
     */
    public abstract boolean hasHiddenRows();

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        ILayerCell cell = super.getCellByPosition(columnPosition, rowPosition);
        if (cell != null && cell.isSpannedCell()) {
            // the spanning needs to be updated to reflect the
            // hiding accordingly
            int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
            int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
            ILayerCell underlyingCell = this.underlyingLayer.getCellByPosition(underlyingColumnPosition, underlyingRowPosition);

            boolean rowSpanUpdated = false;
            int rowSpan = underlyingCell.getRowSpan();
            for (int row = 0; row < underlyingCell.getRowSpan(); row++) {
                int rowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingCell.getOriginRowPosition() + row);
                if (isRowIndexHidden(rowIndex)) {
                    rowSpan--;
                    rowSpanUpdated = true;
                }
            }

            if (rowSpanUpdated) {
                cell = new SpanningLayerCell(cell, cell.getColumnSpan(), rowSpan);
            }
        }
        return cell;
    }

    // Cache

    /**
     * Invalidate the cache to ensure that information is rebuild.
     */
    protected synchronized void invalidateCache() {
        this.cachedVisibleRowIndexPositionMapping = null;
        this.cachedVisibleRowPositionIndexMapping = null;
        this.cachedHiddenRowIndexPositionMapping = null;
        this.startYCache = IntIntMaps.mutable.empty();
    }

    private synchronized IntIntMap getCachedVisibleRowIndexPositionMapping() {
        if (this.cachedVisibleRowIndexPositionMapping == null) {
            cacheVisibleRowIndexes();
        }
        return this.cachedVisibleRowIndexPositionMapping;
    }

    private synchronized IntIntMap getCachedVisibleRowPositionIndexMapping() {
        if (this.cachedVisibleRowPositionIndexMapping == null) {
            cacheVisibleRowIndexes();
        }
        return this.cachedVisibleRowPositionIndexMapping;
    }

    private synchronized IntIntMap getCachedHiddenRowIndexPositionMapping() {
        if (this.cachedHiddenRowIndexPositionMapping == null) {
            cacheVisibleRowIndexes();
        }
        return this.cachedHiddenRowIndexPositionMapping;
    }

    /**
     * Build up the row caches.
     */
    protected synchronized void cacheVisibleRowIndexes() {

        this.cachedVisibleRowIndexPositionMapping = IntIntMaps.mutable.empty();
        this.cachedVisibleRowPositionIndexMapping = IntIntMaps.mutable.empty();
        this.cachedHiddenRowIndexPositionMapping = IntIntMaps.mutable.empty();
        this.startYCache = IntIntMaps.mutable.empty();

        // only build up a cache if it is necessary
        if (hasHiddenRows()) {
            ILayer underlyingLayer = getUnderlyingLayer();
            int rowPosition = 0;
            for (int parentRowPosition = 0; parentRowPosition < underlyingLayer.getRowCount(); parentRowPosition++) {
                int rowIndex = underlyingLayer.getRowIndexByPosition(parentRowPosition);

                if (!isRowIndexHidden(rowIndex)) {
                    this.cachedVisibleRowIndexPositionMapping.put(rowIndex, rowPosition);
                    this.cachedVisibleRowPositionIndexMapping.put(rowPosition, rowIndex);
                    rowPosition++;
                } else {
                    this.cachedHiddenRowIndexPositionMapping.put(rowIndex, rowPosition);
                }
            }
        }
    }

}
