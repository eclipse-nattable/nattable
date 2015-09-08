/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

public abstract class AbstractRowHideShowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private Map<Integer, Integer> cachedVisibleRowIndexOrder;
    private Map<Integer, Integer> cachedVisibleRowPositionOrder;

    private Map<Integer, Integer> cachedHiddenRowIndexToPositionMap;

    private final Map<Integer, Integer> startYCache = new HashMap<Integer, Integer>();

    public AbstractRowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isVerticalStructureChanged()) {
                // vertical structure has changed, update cached row information
                invalidateCache();
            }
        } else if (event instanceof VisualRefreshEvent) {
            // visual change, e.g. font change, the startYCache needs to be
            // cleared in order to re-render correctly
            this.startYCache.clear();
        }
        super.handleLayerEvent(event);
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return ((IUniqueIndexLayer) getUnderlyingLayer()).getColumnPositionByIndex(columnIndex);
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return getCachedVisibleRowIndexes().size();
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition < 0 || rowPosition >= getRowCount()) {
            return -1;
        }

        Integer rowIndex = getCachedVisibleRowPositons().get(rowPosition);
        if (rowIndex != null) {
            return rowIndex;
        } else {
            return -1;
        }
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        final Integer position = getCachedVisibleRowIndexes().get(rowIndex);
        return position != null ? position : -1;
    }

    public Collection<Integer> getRowPositionsByIndexes(Collection<Integer> rowIndexes) {
        Collection<Integer> rowPositions = new HashSet<Integer>();
        for (int rowIndex : rowIndexes) {
            rowPositions.add(getRowPositionByIndex(rowIndex));
        }
        return rowPositions;
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        int rowIndex = getRowIndexByPosition(localRowPosition);
        return ((IUniqueIndexLayer) getUnderlyingLayer()).getRowPositionByIndex(rowIndex);
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
        int rowIndex = getUnderlyingLayer().getRowIndexByPosition(underlyingRowPosition);
        int rowPosition = getRowPositionByIndex(rowIndex);
        if (rowPosition >= 0) {
            return rowPosition;
        } else {
            Integer hiddenRowPosition = this.cachedHiddenRowIndexToPositionMap.get(rowIndex);
            if (hiddenRowPosition != null) {
                return hiddenRowPosition;
            } else {
                return -1;
            }
        }
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
        Collection<Range> localRowPositionRanges = new ArrayList<Range>();

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
        Integer cachedStartY = this.startYCache.get(localRowPosition);
        if (cachedStartY != null) {
            return cachedStartY;
        }

        IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
        int underlyingPosition = localToUnderlyingRowPosition(localRowPosition);
        if (underlyingPosition < 0) {
            return -1;
        }
        int underlyingStartY = underlyingLayer.getStartYOfRowPosition(underlyingPosition);
        if (underlyingStartY < 0) {
            return -1;
        }

        for (Integer hiddenIndex : getHiddenRowIndexes()) {
            int hiddenPosition = underlyingLayer.getRowPositionByIndex(hiddenIndex);
            // if the hidden position is -1, it is hidden in the underlying
            // layertherefore the underlying layer should handle the positioning
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
     * layer. Note: It is not intended that it also collects the row indexes of
     * underlying layers. This would cause issues on calculating positions as
     * every layer is responsible for those calculations itself.
     *
     * @return Collection of all row indexes that are hidden in this layer.
     */
    public abstract Collection<Integer> getHiddenRowIndexes();

    // Cache

    /**
     * Invalidate the cache to ensure that information is rebuild.
     */
    protected void invalidateCache() {
        this.cachedVisibleRowIndexOrder = null;
        this.cachedVisibleRowPositionOrder = null;
        this.cachedHiddenRowIndexToPositionMap = null;
        this.startYCache.clear();
    }

    private Map<Integer, Integer> getCachedVisibleRowIndexes() {
        if (this.cachedVisibleRowIndexOrder == null) {
            cacheVisibleRowIndexes();
        }
        return this.cachedVisibleRowIndexOrder;
    }

    private Map<Integer, Integer> getCachedVisibleRowPositons() {
        if (this.cachedVisibleRowPositionOrder == null) {
            cacheVisibleRowIndexes();
        }
        return this.cachedVisibleRowPositionOrder;
    }

    protected void cacheVisibleRowIndexes() {
        this.cachedVisibleRowIndexOrder = new HashMap<Integer, Integer>();
        this.cachedVisibleRowPositionOrder = new HashMap<Integer, Integer>();
        this.cachedHiddenRowIndexToPositionMap = new HashMap<Integer, Integer>();
        this.startYCache.clear();

        ILayer underlyingLayer = getUnderlyingLayer();
        int rowPosition = 0;
        for (int parentRowPosition = 0; parentRowPosition < underlyingLayer.getRowCount(); parentRowPosition++) {
            int rowIndex = underlyingLayer.getRowIndexByPosition(parentRowPosition);

            if (!isRowIndexHidden(rowIndex)) {
                this.cachedVisibleRowIndexOrder.put(rowIndex, rowPosition);
                this.cachedVisibleRowPositionOrder.put(rowPosition, rowIndex);
                rowPosition++;
            } else {
                this.cachedHiddenRowIndexToPositionMap.put(rowIndex, rowPosition);
            }
        }
    }

}
