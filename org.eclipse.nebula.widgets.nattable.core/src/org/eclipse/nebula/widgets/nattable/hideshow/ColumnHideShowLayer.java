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

import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralChangeEventHelper;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

/**
 * Layer to add support for column hide/show feature to a NatTable. Technically
 * the columns are hidden by this layer which leads to a index-position
 * transformation. With percentage sizing this this leads to gaps for hidden
 * columns as the size of the other columns is not re-calculated. For percentage
 * sizing and size increase use the {@link ResizeColumnHideShowLayer}.
 *
 * @see ResizeColumnHideShowLayer
 */
public class ColumnHideShowLayer extends AbstractColumnHideShowLayer implements IColumnHideShowLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES = ".hiddenColumnIndexes"; //$NON-NLS-1$

    private final Set<Integer> hiddenColumnIndexes;

    public ColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);
        this.hiddenColumnIndexes = new TreeSet<Integer>();

        registerCommandHandler(new MultiColumnHideCommandHandler(this));
        registerCommandHandler(new ColumnHideCommandHandler(this));
        registerCommandHandler(new ShowAllColumnsCommandHandler(this));
        registerCommandHandler(new MultiColumnShowCommandHandler(this));
        registerCommandHandler(new ColumnShowCommandHandler(this));
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                Collection<StructuralDiff> columnDiffs = structuralChangeEvent.getColumnDiffs();

                if (columnDiffs != null && !columnDiffs.isEmpty()
                        && !StructuralChangeEventHelper.isReorder(columnDiffs)) {
                    StructuralChangeEventHelper.handleColumnDelete(columnDiffs,
                            this.underlyingLayer, this.hiddenColumnIndexes, false);
                    StructuralChangeEventHelper.handleColumnInsert(columnDiffs,
                            this.underlyingLayer, this.hiddenColumnIndexes, false);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        if (this.hiddenColumnIndexes.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.hiddenColumnIndexes) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES,
                    strBuilder.toString());
        } else {
            properties.remove(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        // Bug 396925: always clear the state of the hidden columns, whether
        // there is a state saved or not
        this.hiddenColumnIndexes.clear();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.hiddenColumnIndexes.add(Integer.valueOf(index));
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
        int underlyingPosition = localToUnderlyingColumnPosition(columnPosition);
        int leftColumnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingPosition - 1);
        if (isColumnIndexHidden(leftColumnIndex)) {
            configLabels.addLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN);
        }

        int rightColumnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingPosition + 1);
        if (isColumnIndexHidden(rightColumnIndex)) {
            configLabels.addLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN);
        }

        return configLabels;
    }

    // Hide/show

    @Override
    public boolean isColumnIndexHidden(int columnIndex) {
        return this.hiddenColumnIndexes.contains(Integer.valueOf(columnIndex));
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        return this.hiddenColumnIndexes;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    @Override
    public void hideColumnPositions(Integer... columnPositions) {
        hideColumnPositions(Arrays.asList(columnPositions));
    }

    @Override
    public void hideColumnPositions(Collection<Integer> columnPositions) {
        Set<Integer> columnIndexes = new HashSet<Integer>();
        for (Integer columnPosition : columnPositions) {
            columnIndexes.add(getColumnIndexByPosition(columnPosition));
        }
        this.hiddenColumnIndexes.addAll(columnIndexes);
        invalidateCache();
        fireLayerEvent(new HideColumnPositionsEvent(this, columnPositions, columnIndexes));
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    @Override
    public void showColumnIndexes(Integer... columnIndexes) {
        showColumnIndexes(Arrays.asList(columnIndexes));
    }

    @Override
    public void showColumnIndexes(Collection<Integer> columnIndexes) {
        this.hiddenColumnIndexes.removeAll(columnIndexes);
        Collection<Integer> positions = getColumnPositionsByIndexes(columnIndexes);
        invalidateCache();
        fireLayerEvent(new ShowColumnPositionsEvent(this, positions));
    }

    @Override
    public void showColumnPosition(int columnPosition, boolean showToLeft, boolean showAll) {
        Set<Integer> columnIndexes = new HashSet<Integer>();
        int underlyingPosition = localToUnderlyingColumnPosition(columnPosition);
        if (showToLeft) {
            int leftColumnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingPosition - 1);
            if (showAll) {
                int move = 1;
                while (isColumnIndexHidden(leftColumnIndex)) {
                    columnIndexes.add(leftColumnIndex);
                    move++;
                    leftColumnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingPosition - move);
                }
            } else if (isColumnIndexHidden(leftColumnIndex)) {
                columnIndexes.add(leftColumnIndex);
            }
        } else {
            int rightColumnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingPosition + 1);
            if (showAll) {
                int move = 1;
                while (isColumnIndexHidden(rightColumnIndex)) {
                    columnIndexes.add(rightColumnIndex);
                    move++;
                    rightColumnIndex = this.underlyingLayer.getColumnIndexByPosition(underlyingPosition + move);
                }
            } else if (isColumnIndexHidden(rightColumnIndex)) {
                columnIndexes.add(rightColumnIndex);
            }
        }

        if (!columnIndexes.isEmpty()) {
            showColumnIndexes(columnIndexes);
        }
    }

    @Override
    public void showAllColumns() {
        Collection<Integer> hiddenColumns = new ArrayList<Integer>(this.hiddenColumnIndexes);
        this.hiddenColumnIndexes.clear();
        invalidateCache();
        fireLayerEvent(new ShowColumnPositionsEvent(this, getColumnPositionsByIndexes(hiddenColumns)));
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();
        result.add(HideIndicatorConstants.COLUMN_LEFT_HIDDEN);
        result.add(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN);
        return result;
    }
}
