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
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

public class RowHideShowLayer extends AbstractRowHideShowLayer implements IRowHideShowLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_ROW_INDEXES = ".hiddenRowIndexes"; //$NON-NLS-1$

    private final Set<Integer> hiddenRowIndexes;

    public RowHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
        this.hiddenRowIndexes = new TreeSet<Integer>();

        registerCommandHandler(new MultiRowHideCommandHandler(this));
        registerCommandHandler(new RowHideCommandHandler(this));
        registerCommandHandler(new ShowAllRowsCommandHandler(this));
        registerCommandHandler(new MultiRowShowCommandHandler(this));
        registerCommandHandler(new RowPositionHideCommandHandler(this));
        registerCommandHandler(new RowShowCommandHandler(this));
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
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.hiddenRowIndexes) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES, strBuilder.toString());
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.hiddenRowIndexes.clear();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_ROW_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.hiddenRowIndexes.add(Integer.valueOf(index));
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
        return this.hiddenRowIndexes.contains(Integer.valueOf(rowIndex));
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        return this.hiddenRowIndexes;
    }

    @Override
    public void hideRowPositions(int... rowPositions) {
        hideRowPositions(Arrays.stream(rowPositions).boxed().collect(Collectors.toList()));
    }

    @Override
    public void hideRowPositions(Collection<Integer> rowPositions) {
        Set<Integer> rowIndexes = new HashSet<Integer>();
        for (Integer rowPosition : rowPositions) {
            rowIndexes.add(getRowIndexByPosition(rowPosition));
        }
        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions, rowIndexes));
    }

    @Override
    public void hideRowIndexes(int... rowIndexes) {
        hideRowIndexes(Arrays.stream(rowIndexes).boxed().collect(Collectors.toList()));
    }

    @Override
    public void hideRowIndexes(Collection<Integer> rowIndexes) {
        Set<Integer> rowPositions = new HashSet<Integer>();
        for (Integer rowIndex : rowIndexes) {
            rowPositions.add(getRowPositionByIndex(rowIndex));
        }
        this.hiddenRowIndexes.addAll(rowIndexes);
        invalidateCache();
        fireLayerEvent(new HideRowPositionsEvent(this, rowPositions, rowIndexes));
    }

    @Override
    public void showRowIndexes(int... rowIndexes) {
        showRowIndexes(Arrays.stream(rowIndexes).boxed().collect(Collectors.toList()));
    }

    @Override
    public void showRowIndexes(Collection<Integer> rowIndexes) {
        this.hiddenRowIndexes.removeAll(rowIndexes);
        invalidateCache();
        Collection<Integer> positions = getRowPositionsByIndexes(rowIndexes);
        fireLayerEvent(new ShowRowPositionsEvent(this, positions));
    }

    @Override
    public void showRowPosition(int rowPosition, boolean showToTop, boolean showAll) {
        Set<Integer> rowIndexes = new HashSet<Integer>();
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
            showRowIndexes(rowIndexes);
        }
    }

    @Override
    public void showAllRows() {
        Collection<Integer> hiddenRows = new ArrayList<Integer>(this.hiddenRowIndexes);
        this.hiddenRowIndexes.clear();
        invalidateCache();
        fireLayerEvent(new ShowRowPositionsEvent(this, getRowPositionsByIndexes(hiddenRows)));
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();
        result.add(HideIndicatorConstants.ROW_TOP_HIDDEN);
        result.add(HideIndicatorConstants.ROW_BOTTOM_HIDDEN);
        return result;
    }
}
