/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

public abstract class AbstractColumnHideShowLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private List<Integer> cachedVisibleColumnIndexOrder;
    private Map<Integer, Integer> cachedVisibleColumnIndexPositionMap;

    private Map<Integer, Integer> cachedHiddenColumnIndexToPositionMap;

    private final Map<Integer, Integer> startXCache = new HashMap<Integer, Integer>();

    public AbstractColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                invalidateCache();
            }
        }
        super.handleLayerEvent(event);
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return getCachedVisibleColumnIndexes().size();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (columnPosition < 0 || columnPosition >= getColumnCount()) {
            return -1;
        }

        Integer columnIndex = getCachedVisibleColumnIndexes().get(columnPosition);
        return (columnIndex != null) ? columnIndex : -1;
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        if (this.cachedVisibleColumnIndexPositionMap == null) {
            cacheVisibleColumnIndexes();
        }

        Integer position = this.cachedVisibleColumnIndexPositionMap.get(columnIndex);
        return (position != null) ? position : -1;
    }

    public Collection<Integer> getColumnPositionsByIndexes(Collection<Integer> columnIndexes) {
        Collection<Integer> columnPositions = new HashSet<Integer>();
        for (int columnIndex : columnIndexes) {
            columnPositions.add(getColumnPositionByIndex(columnIndex));
        }
        return columnPositions;
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        int columnIndex = getColumnIndexByPosition(localColumnPosition);
        return ((IUniqueIndexLayer) getUnderlyingLayer()).getColumnPositionByIndex(columnIndex);
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
        int columnIndex = getUnderlyingLayer().getColumnIndexByPosition(underlyingColumnPosition);
        int columnPosition = getColumnPositionByIndex(columnIndex);
        if (columnPosition >= 0) {
            return columnPosition;
        } else {
            Integer hiddenColumnPosition = this.cachedHiddenColumnIndexToPositionMap.get(columnIndex);
            return (hiddenColumnPosition != null) ? hiddenColumnPosition : -1;
        }
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        Collection<Range> localColumnPositionRanges = new ArrayList<Range>();

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
                localColumnPositionRanges.add(
                        new Range(startColumnPosition, endColumnPosition));
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
        int lastColumnPosition = getColumnCount() - 1;
        return getStartXOfColumnPosition(lastColumnPosition)
                + getColumnWidthByPosition(lastColumnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return LayerUtil.getColumnPositionByX(this, x);
    }

    @Override
    public int getStartXOfColumnPosition(int localColumnPosition) {
        Integer cachedStartX = this.startXCache.get(localColumnPosition);
        if (cachedStartX != null) {
            return cachedStartX.intValue();
        }

        IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
        int underlyingPosition = localToUnderlyingColumnPosition(localColumnPosition);
        if (underlyingPosition < 0) {
            return -1;
        }
        int underlyingStartX = underlyingLayer.getStartXOfColumnPosition(underlyingPosition);
        if (underlyingStartX < 0) {
            return -1;
        }

        for (Integer hiddenIndex : getHiddenColumnIndexes()) {
            int hiddenPosition = underlyingLayer.getColumnPositionByIndex(hiddenIndex);
            if (hiddenPosition <= underlyingPosition) {
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
        return ((IUniqueIndexLayer) getUnderlyingLayer()).getRowPositionByIndex(rowIndex);
    }

    // Hide/show

    /**
     * Will check if the column at the specified index is hidden or not. Checks
     * this layer and also the sublayers for the visibility. Note: As the
     * {@link ColumnGroup}s are created index based, this method only works
     * correctly with indexes rather than positions.
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
     * this layer. Note: It is not intended that it also collects the column
     * indexes of underlying layers. This would cause issues on calculating
     * positions as every layer is responsible for those calculations itself.
     *
     * @return Collection of all column indexes that are hidden in this layer.
     */
    public abstract Collection<Integer> getHiddenColumnIndexes();

    // Cache

    /**
     * Invalidate the cache to ensure that information is rebuild.
     */
    protected void invalidateCache() {
        this.cachedVisibleColumnIndexOrder = null;
        this.cachedVisibleColumnIndexPositionMap = null;
        this.startXCache.clear();
    }

    private List<Integer> getCachedVisibleColumnIndexes() {
        if (this.cachedVisibleColumnIndexOrder == null) {
            cacheVisibleColumnIndexes();
        }
        return this.cachedVisibleColumnIndexOrder;
    }

    private void cacheVisibleColumnIndexes() {
        this.cachedVisibleColumnIndexOrder = new ArrayList<Integer>();
        this.cachedVisibleColumnIndexPositionMap = new HashMap<Integer, Integer>();
        this.cachedHiddenColumnIndexToPositionMap = new HashMap<Integer, Integer>();
        this.startXCache.clear();

        ILayer underlyingLayer = getUnderlyingLayer();
        int columnPosition = 0;
        for (int parentColumnPosition = 0; parentColumnPosition < underlyingLayer.getColumnCount(); parentColumnPosition++) {
            int columnIndex = underlyingLayer.getColumnIndexByPosition(parentColumnPosition);

            if (!isColumnIndexHidden(columnIndex)) {
                this.cachedVisibleColumnIndexOrder.add(columnIndex);
                this.cachedVisibleColumnIndexPositionMap.put(columnIndex, columnPosition);
                columnPosition++;
            } else {
                this.cachedHiddenColumnIndexToPositionMap.put(columnIndex, columnPosition);
            }
        }
    }

}
