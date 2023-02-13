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

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.HideColumnByIndexCommandHandler;
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
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

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

    private MutableIntSet hiddenColumnIndexes = IntSets.mutable.empty();

    public ColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);

        registerCommandHandler(new MultiColumnHideCommandHandler(this));
        registerCommandHandler(new ColumnHideCommandHandler(this));
        registerCommandHandler(new ShowAllColumnsCommandHandler(this));
        registerCommandHandler(new MultiColumnShowCommandHandler(this));
        registerCommandHandler(new ColumnShowCommandHandler(this));
        registerCommandHandler(new HideColumnByIndexCommandHandler(this));
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof IStructuralChangeEvent) {
            IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
            if (structuralChangeEvent.isHorizontalStructureChanged()) {
                Collection<StructuralDiff> columnDiffs = structuralChangeEvent.getColumnDiffs();

                if (columnDiffs != null && !columnDiffs.isEmpty()
                        && !StructuralChangeEventHelper.isReorder(columnDiffs)) {
                    StructuralChangeEventHelper.handleColumnDelete(
                            columnDiffs,
                            this.underlyingLayer,
                            this.hiddenColumnIndexes,
                            false);
                    StructuralChangeEventHelper.handleColumnInsert(
                            columnDiffs,
                            this.underlyingLayer,
                            this.hiddenColumnIndexes,
                            false);
                }
            }
        }
        super.handleLayerEvent(event);
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        if (this.hiddenColumnIndexes.size() > 0) {
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES,
                    this.hiddenColumnIndexes.toSortedList().makeString(IPersistable.VALUE_SEPARATOR));
        } else {
            properties.remove(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
        }

        super.saveState(prefix, properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        // Bug 396925: always clear the state of the hidden columns, whether
        // there is a state saved or not
        this.hiddenColumnIndexes = IntSets.mutable.empty();
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
        if (property != null) {
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                this.hiddenColumnIndexes.add(Integer.parseInt(index));
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
        return this.hiddenColumnIndexes.contains(columnIndex);
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        return ArrayUtil.asIntegerList(this.hiddenColumnIndexes.toSortedArray());
    }

    @Override
    public int[] getHiddenColumnIndexesArray() {
        return this.hiddenColumnIndexes.toSortedArray();
    }

    @Override
    public boolean hasHiddenColumns() {
        return !this.hiddenColumnIndexes.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    @Override
    public void hideColumnPositions(int... columnPositions) {
        int[] columnIndexes = Arrays.stream(columnPositions)
                .map(this::getColumnIndexByPosition)
                .sorted()
                .toArray();
        this.hiddenColumnIndexes.addAll(columnIndexes);
        invalidateCache();
        fireLayerEvent(new HideColumnPositionsEvent(this, columnPositions, columnIndexes));
    }

    @Override
    public void hideColumnPositions(Collection<Integer> columnPositions) {
        hideColumnPositions(columnPositions.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void hideColumnIndexes(int... columnIndexes) {
        int[] filteredIndexes = Arrays.stream(columnIndexes)
                .filter(index -> !this.hiddenColumnIndexes.contains(index))
                .toArray();

        // only fire an update if something will change
        if (filteredIndexes.length > 0) {
            int[] columnPositions = Arrays.stream(filteredIndexes)
                    .filter(index -> !this.hiddenColumnIndexes.contains(index))
                    .map(this::getColumnPositionByIndex)
                    .sorted()
                    .toArray();

            this.hiddenColumnIndexes.addAll(filteredIndexes);
            invalidateCache();
            fireLayerEvent(new HideColumnPositionsEvent(this, columnPositions, filteredIndexes));
        }
    }

    @Override
    public void hideColumnIndexes(Collection<Integer> columnIndexes) {
        hideColumnIndexes(columnIndexes.stream().mapToInt(Integer::intValue).toArray());
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6
     */
    @Override
    public void showColumnIndexes(int... columnIndexes) {
        MutableIntList toProcess = IntLists.mutable.of(columnIndexes);

        // only handle column indexes that are hidden
        toProcess.retainAll(this.hiddenColumnIndexes);

        this.hiddenColumnIndexes.removeAll(toProcess);
        invalidateCache();
        int[] positions = getColumnPositionsByIndexes(toProcess.toArray());
        fireLayerEvent(new ShowColumnPositionsEvent(this, positions));
    }

    @Override
    public void showColumnIndexes(Collection<Integer> columnIndexes) {
        showColumnIndexes(columnIndexes.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void showColumnPosition(int columnPosition, boolean showToLeft, boolean showAll) {
        MutableIntSet columnIndexes = IntSets.mutable.empty();
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
            showColumnIndexes(columnIndexes.toArray());
        }
    }

    @Override
    public void showAllColumns() {
        int[] hidden = this.hiddenColumnIndexes.toSortedArray();
        this.hiddenColumnIndexes = IntSets.mutable.empty();
        invalidateCache();
        fireLayerEvent(new ShowColumnPositionsEvent(this, getColumnPositionsByIndexes(hidden)));
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();
        result.add(HideIndicatorConstants.COLUMN_LEFT_HIDDEN);
        result.add(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN);
        return result;
    }
}
