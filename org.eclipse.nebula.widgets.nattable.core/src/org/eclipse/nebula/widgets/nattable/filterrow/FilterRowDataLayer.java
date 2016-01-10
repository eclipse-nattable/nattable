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
package org.eclipse.nebula.widgets.nattable.filterrow;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

/**
 * {@link DataLayer} with a custom {@link IDataProvider} which stores/tracks the
 * filter text on columns. Applies region name of {@link GridRegion#FILTER_ROW}
 * to the filter row. Persists the filter text to the properties file.
 *
 * @param <T>
 *            type of the underlying row object
 */
public class FilterRowDataLayer<T> extends DataLayer {

    /** Prefix of the column label applied to each column in the filter row */
    public static final String FILTER_ROW_COLUMN_LABEL_PREFIX = "FILTER_COLUMN_"; //$NON-NLS-1$

    /** Prefix for the persistence key in the properties file */
    public static final String PERSISTENCE_KEY_FILTER_ROW_TOKENS = ".filterTokens"; //$NON-NLS-1$

    /**
     * The ILayer to which this FilterRowDataLayer is dependent. Typically the
     * ColumnHeaderLayer.
     */
    private ILayer columnHeaderLayer;

    public FilterRowDataLayer(
            IFilterStrategy<T> filterStrategy,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry) {

        super(new FilterRowDataProvider<T>(
                filterStrategy, columnHeaderLayer, columnHeaderDataProvider, configRegistry));

        this.columnHeaderLayer = columnHeaderLayer;

        addConfiguration(new DefaultFilterRowConfiguration());
    }

    @SuppressWarnings("unchecked")
    public FilterRowDataProvider<T> getFilterRowDataProvider() {
        return (FilterRowDataProvider<T>) this.dataProvider;
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        boolean handled = false;
        if (command instanceof ClearFilterCommand
                && command.convertToTargetLayer(this)) {
            int columnPosition = ((ClearFilterCommand) command).getColumnPosition();
            setDataValueByPosition(columnPosition, 0, null);
            handled = true;
        } else if (command instanceof ClearAllFiltersCommand) {
            getFilterRowDataProvider().clearAllFilters();
            handled = true;
        }

        if (handled) {
            fireLayerEvent(new RowStructuralRefreshEvent(this));
            return true;
        } else {
            return super.doCommand(command);
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        // At the data layer level position == index
        final LabelStack labels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
        // the label needs to be index based as the position changes on
        // scrolling
        labels.addLabel(FILTER_ROW_COLUMN_LABEL_PREFIX + getColumnIndexByPosition(columnPosition));
        labels.addLabel(GridRegion.FILTER_ROW);
        return labels;
    }

    // There is no multiple inheritance in Java, but the FilterRowDataLayer
    // needs to be a DimensionallyDependentLayer aswell. Wrapping it in a
    // DimensionallyDependentLayer together with the ColumnHeaderLayer causes
    // several position-transformation-issues.

    // Columns

    @Override
    public int getColumnCount() {
        return this.columnHeaderLayer.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return this.columnHeaderLayer.getPreferredColumnCount();
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        return this.columnHeaderLayer.getColumnIndexByPosition(columnPosition);
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return this.columnHeaderLayer.localToUnderlyingColumnPosition(localColumnPosition);
    }

    @Override
    public int underlyingToLocalColumnPosition(
            ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {

        if (sourceUnderlyingLayer == this.columnHeaderLayer) {
            return underlyingColumnPosition;
        }
        return this.columnHeaderLayer.underlyingToLocalColumnPosition(
                sourceUnderlyingLayer, underlyingColumnPosition);
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {

        if (sourceUnderlyingLayer == this.columnHeaderLayer) {
            return underlyingColumnPositionRanges;
        }
        return this.columnHeaderLayer.underlyingToLocalColumnPositions(
                sourceUnderlyingLayer, underlyingColumnPositionRanges);
    }

    // Width

    @Override
    public int getWidth() {
        return this.columnHeaderLayer.getWidth();
    }

    @Override
    public int getPreferredWidth() {
        return this.columnHeaderLayer.getPreferredWidth();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.columnHeaderLayer.getColumnWidthByPosition(columnPosition);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.columnHeaderLayer.isColumnPositionResizable(columnPosition);
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return this.columnHeaderLayer.getColumnPositionByX(x);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.columnHeaderLayer.getStartXOfColumnPosition(columnPosition);
    }

    /**
     * @since 1.4
     */
    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> labels = super.getProvidedLabels();

        labels.add(GridRegion.FILTER_ROW);
        for (int i = 0; i < getColumnCount(); i++) {
            labels.add(FILTER_ROW_COLUMN_LABEL_PREFIX + i);
        }

        return labels;
    }
}
