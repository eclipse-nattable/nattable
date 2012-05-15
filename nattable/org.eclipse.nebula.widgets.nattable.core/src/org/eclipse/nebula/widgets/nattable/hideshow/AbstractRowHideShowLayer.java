/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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
import java.util.Map;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;


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
				invalidateCache();
			}
		}
		super.handleLayerEvent(event);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		fireLayerEvent(new RowStructuralRefreshEvent(this));
	}
	
	// Horizontal features

	// Columns
	
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
			return rowIndex.intValue();
		} else {
			return -1;
		}
	}
	
	public int getRowPositionByIndex(int rowIndex) {
		final Integer position = getCachedVisibleRowIndexes().get(Integer.valueOf(rowIndex));
		return position != null ? position : -1;
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
			Integer hiddenRowPosition = cachedHiddenRowIndexToPositionMap.get(Integer.valueOf(rowIndex));
			if (hiddenRowPosition != null) {
				return hiddenRowPosition.intValue();
			} else {
				return -1;
			}
		}
	}

	@Override
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		Collection<Range> localRowPositionRanges = new ArrayList<Range>();

		for (Range underlyingRowPositionRange : underlyingRowPositionRanges) {
			int startRowPosition = getAdjustedUnderlyingToLocalStartPosition(sourceUnderlyingLayer, underlyingRowPositionRange.start, underlyingRowPositionRange.end);
			int endRowPosition = getAdjustedUnderlyingToLocalEndPosition(sourceUnderlyingLayer, underlyingRowPositionRange.end, underlyingRowPositionRange.start);

			// teichstaedt: fixes the problem that ranges where added even if the
			// corresponding startPosition weren't found in the underlying layer.
			// Without that fix a bunch of ranges of kind Range [-1, 180] which
			// causes strange behaviour in Freeze- and other Layers were returned.
			if (startRowPosition > -1) {
				localRowPositionRanges.add(new Range(startRowPosition, endRowPosition));
			}
		}

		return localRowPositionRanges;
	}
	
	private int getAdjustedUnderlyingToLocalStartPosition(ILayer sourceUnderlyingLayer, int startUnderlyingPosition, int endUnderlyingPosition) {
		int localStartRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, startUnderlyingPosition);
		int offset = 0;
		while (localStartRowPosition < 0 && (startUnderlyingPosition + offset < endUnderlyingPosition)) {
			localStartRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, startUnderlyingPosition + offset++);
		}
		return localStartRowPosition;
	}

	private int getAdjustedUnderlyingToLocalEndPosition(ILayer sourceUnderlyingLayer, int endUnderlyingPosition, int startUnderlyingPosition) {
		int localEndRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, endUnderlyingPosition - 1);
		int offset = 0;
		while (localEndRowPosition < 0 && (endUnderlyingPosition - offset > startUnderlyingPosition)) {
			localEndRowPosition = underlyingToLocalRowPosition(sourceUnderlyingLayer, endUnderlyingPosition - offset++);
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
		Integer cachedStartY = startYCache.get(Integer.valueOf(localRowPosition));
		if (cachedStartY != null) {
			return cachedStartY.intValue();
		}
		
		IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
		int underlyingPosition = localToUnderlyingRowPosition(localRowPosition);
		int underlyingStartY = underlyingLayer.getStartYOfRowPosition(underlyingPosition);

		for (Integer hiddenIndex : getHiddenRowIndexes()) {
			int hiddenPosition = underlyingLayer.getRowPositionByIndex(hiddenIndex.intValue());
			if (hiddenPosition <= underlyingPosition) {
				underlyingStartY -= underlyingLayer.getRowHeightByPosition(hiddenPosition);
			}
		}

		startYCache.put(Integer.valueOf(localRowPosition), Integer.valueOf(underlyingStartY));
		return underlyingStartY;
	}
	
	// Hide/show

	public abstract boolean isRowIndexHidden(int rowIndex);

	public abstract Collection<Integer> getHiddenRowIndexes();
	
	// Cache

	protected void invalidateCache() {
		cachedVisibleRowIndexOrder = null;
		cachedVisibleRowPositionOrder = null;
		startYCache.clear();
	}

	private Map<Integer, Integer> getCachedVisibleRowIndexes() {
		if (cachedVisibleRowIndexOrder == null) {
			cacheVisibleRowIndexes();
		}
		return cachedVisibleRowIndexOrder;
	}
	
	private Map<Integer, Integer> getCachedVisibleRowPositons() {
		if (cachedVisibleRowPositionOrder == null) {
			cacheVisibleRowIndexes();
		}
		return cachedVisibleRowPositionOrder;
	}

	protected void cacheVisibleRowIndexes() {
		cachedVisibleRowIndexOrder = new HashMap<Integer, Integer>();
		cachedVisibleRowPositionOrder = new HashMap<Integer, Integer>();
		cachedHiddenRowIndexToPositionMap = new HashMap<Integer, Integer>();
		startYCache.clear();

		ILayer underlyingLayer = getUnderlyingLayer();
		int rowPosition = 0;
		for (int parentRowPosition = 0; parentRowPosition < underlyingLayer.getRowCount(); parentRowPosition++) {
			int rowIndex = underlyingLayer.getRowIndexByPosition(parentRowPosition);

			if (!isRowIndexHidden(rowIndex)) {
				cachedVisibleRowIndexOrder.put(Integer.valueOf(rowIndex), rowPosition);
				cachedVisibleRowPositionOrder.put(rowPosition, Integer.valueOf(rowIndex));
				rowPosition++;
			} else {
				cachedHiddenRowIndexToPositionMap.put(Integer.valueOf(rowIndex), Integer.valueOf(rowPosition));
			}
		}
	}

}
