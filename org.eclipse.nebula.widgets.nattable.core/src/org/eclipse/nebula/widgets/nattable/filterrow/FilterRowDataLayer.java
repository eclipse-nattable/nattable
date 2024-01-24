/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
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
     * The {@link ILayer} to which this {@link FilterRowDataLayer} is dependent.
     * Typically the {@link ColumnHeaderLayer}.
     */
    private ILayer columnHeaderLayer;

    /**
     * Creates a {@link FilterRowDataLayer} with the default configuration
     * {@link DefaultFilterRowConfiguration}.
     *
     * @param filterStrategy
     *            The {@link IFilterStrategy} to which the set filter value
     *            should be applied.
     * @param columnHeaderLayer
     *            The {@link ILayer} to which this {@link FilterRowDataLayer} is
     *            dependent. Typically the {@link ColumnHeaderLayer}.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the
     *            {@link IDisplayConverter} for converting the values on state
     *            save/load operations.
     */
    public FilterRowDataLayer(
            IFilterStrategy<T> filterStrategy,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry) {

        this(filterStrategy, columnHeaderLayer, columnHeaderDataProvider, configRegistry, true);
    }

    /**
     * Creates a {@link FilterRowDataLayer}.
     *
     * @param filterStrategy
     *            The {@link IFilterStrategy} to which the set filter value
     *            should be applied.
     * @param columnHeaderLayer
     *            The {@link ILayer} to which this {@link FilterRowDataLayer} is
     *            dependent. Typically the {@link ColumnHeaderLayer}.
     * @param columnHeaderDataProvider
     *            The {@link IDataProvider} of the column header needed to
     *            retrieve the real column count of the column header and not a
     *            transformed one.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to retrieve the
     *            {@link IDisplayConverter} for converting the values on state
     *            save/load operations.
     * @param useDefaultConfiguration
     *            <code>true</code> if the default configuration should be
     *            applied, <code>false</code> if a custom configuration will be
     *            applied afterwards.
     *
     * @since 2.3
     */
    public FilterRowDataLayer(
            IFilterStrategy<T> filterStrategy,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration) {

        super(new FilterRowDataProvider<T>(
                filterStrategy, columnHeaderLayer, columnHeaderDataProvider, configRegistry));

        this.columnHeaderLayer = columnHeaderLayer;

        if (useDefaultConfiguration) {
            addConfiguration(new DefaultFilterRowConfiguration());
        }

        // register an UpdateDataCommandHandler that does not check for equality
        // and always performs an update so the filtering can be triggered by
        // simply applying the same value again
        unregisterCommandHandler(UpdateDataCommand.class);
        registerCommandHandler(new UpdateDataCommandHandler(this, false));
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
