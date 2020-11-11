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

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.hideshow.command.HideRowByIndexCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowPositionHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllRowsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public class RowHideShowLayer extends AbstractRowHideShowLayer implements IRowHideShowLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_ROW_INDEXES = ".hiddenRowIndexes"; //$NON-NLS-1$

    private MutableIntSet hiddenRowIndexes = IntSets.mutable.empty();

    public RowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);

        registerCommandHandler(new MultiRowHideCommandHandler(this));
        registerCommandHandler(new RowHideCommandHandler(this));
        registerCommandHandler(new ShowAllRowsCommandHandler(this));
        registerCommandHandler(new MultiRowShowCommandHandler(this));
        registerCommandHandler(new RowPositionHideCommandHandler(this));
        registerCommandHandler(new RowShowCommandHandler(this));
        registerCommandHandler(new HideRowByIndexCommandHandler(this));
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isVerticalStructureChanged()) {
                Collection<StructuralDiff> rowDiffs = structuralChangeEvent.getRowDiffs();
                if (rowDiffs != null && !rowDiffs.isEmpty()
                        && !StructuralChangeEventHelper.isReorder(rowDiffs)) {
                    StructuralChangeEventHelper.handleRowDelete(
                            rowDiffs,
                            this.underlyingLayer,
                            this.hiddenRowIndexes,
                            false);
                    StructuralChangeEventHelper.handleRowInsert(
                            rowDiffs,
                            this.underlyingLayer,
                            this.hiddenRowIndexes,
                            false);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        if (this.hiddenRowIndexes.size() > 0) {
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES,
                    this.hiddenRowIndexes.toSortedList().makeString(IPersistable.VALUE_SEPARATOR));
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.hiddenRowIndexes = IntSets.mutable.empty();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.hiddenRowIndexes.add(Integer.parseInt(index));
            }
        }

        super.loadState(prefix, properties);
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack configLabels = super.getConfigLabelsByPosition(columnPosition, rowPosition);

        // we need to check the hidden state of an adjacent position via the
        // underlying layer as in the hide layer the position might be
        // hidden
        int underlyingPosition = localToUnderlyingRowPosition(rowPosition);
        int upRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition - 1);
        if (isRowIndexHidden(upRowIndex)) {
            configLabels.addLabel(HideIndicatorConstants.ROW_TOP_HIDDEN);
        }

        int downRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition + 1);
        if (isRowIndexHidden(downRowIndex)) {
            configLabels.addLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN);
        }

        return configLabels;
    }

    // Hide/show

    @Override
    public boolean isRowIndexHidden(int rowIndex) {
        return this.hiddenRowIndexes.contains(rowIndex);
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        return ArrayUtil.asIntegerList(this.hiddenRowIndexes.toSortedArray());
    }

    @Override
    public int[] getHiddenRowIndexesArray() {
        return this.hiddenRowIndexes.toSortedArray();
    }

    @Override
    public boolean hasHiddenRows() {
        return !this.hiddenRowIndexes.isEmpty();
    }

    @Override
    public void hideRowPositions(int... rowPositions) {
        int[] rowIndexes = Arrays.stream(rowPositions)
                .map(this::getRowIndexByPosition)
                .sorted()
                .toArray();
        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions, rowIndexes));
    }

    @Override
    public void hideRowPositions(Collection<Integer> rowPositions) {
        hideRowPositions(rowPositions.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void hideRowIndexes(int... rowIndexes) {
        int[] rowPositions = Arrays.stream(rowIndexes)
                .map(this::getRowPositionByIndex)
                .sorted()
                .toArray();
        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions, rowIndexes));
    }

    @Override
    public void hideRowIndexes(Collection<Integer> rowIndexes) {
        hideRowIndexes(rowIndexes.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void showRowIndexes(int... rowIndexes) {
        MutableIntList toProcess = IntLists.mutable.of(rowIndexes);

        // only handle row indexes that are hidden
        toProcess.retainAll(this.hiddenRowIndexes);

        this.hiddenRowIndexes.removeAll(toProcess);
        invalidateCache();
        int[] positions = getRowPositionsByIndexes(toProcess.toArray());
        fireLayerEvent(new ShowRowPositionsEvent(this, positions));
    }

    @Override
    public void showRowIndexes(Collection<Integer> rowIndexes) {
        showRowIndexes(rowIndexes.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void showRowPosition(int rowPosition, boolean showToTop, boolean showAll) {
        MutableIntSet rowIndexes = IntSets.mutable.empty();
        int underlyingPosition = localToUnderlyingRowPosition(rowPosition);
        if (showToTop) {
            int topRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition - 1);
            if (showAll) {
                int move = 1;
                while (isRowIndexHidden(topRowIndex)) {
                    rowIndexes.add(topRowIndex);
                    move++;
                    topRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition - move);
                }
            } else if (isRowIndexHidden(topRowIndex)) {
                rowIndexes.add(topRowIndex);
            }
        } else {
            int bottomRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition + 1);
            if (showAll) {
                int move = 1;
                while (isRowIndexHidden(bottomRowIndex)) {
                    rowIndexes.add(bottomRowIndex);
                    move++;
                    bottomRowIndex = this.underlyingLayer.getRowIndexByPosition(underlyingPosition + move);
                }
            } else if (isRowIndexHidden(bottomRowIndex)) {
                rowIndexes.add(bottomRowIndex);
            }
        }

        if (!rowIndexes.isEmpty()) {
            showRowIndexes(rowIndexes.toArray());
        }
    }

    @Override
    public void showAllRows() {
        int[] hidden = this.hiddenRowIndexes.toSortedArray();
        this.hiddenRowIndexes = IntSets.mutable.empty();
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(hidden)));
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();
        result.add(HideIndicatorConstants.ROW_TOP_HIDDEN);
        result.add(HideIndicatorConstants.ROW_BOTTOM_HIDDEN);
        return result;
    }
}
