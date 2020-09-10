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
package org.eclipse.nebula.widgets.nattable.filterrow;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;

/**
 * 1 column x 2 rows Composite layer
 * <ul>
 * <li>First row is the {@link ColumnHeaderLayer}</li>
 * <li>Second row is the composite is the filter row layer. The filter row layer
 * is a {@link DimensionallyDependentLayer} dependent on the
 * {@link ColumnHeaderLayer}</li>
 * </ul>
 *
 * @see FilterRowDataLayer
 */
public class FilterRowHeaderComposite<T> extends CompositeLayer {

    private final FilterRowDataLayer<T> filterRowDataLayer;
    private boolean filterRowVisible = true;

    public FilterRowHeaderComposite(
            IFilterStrategy<T> filterStrategy,
            ILayer columnHeaderLayer,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry) {

        super(1, 2);

        setChildLayer("columnHeader", columnHeaderLayer, 0, 0); //$NON-NLS-1$

        this.filterRowDataLayer =
                new FilterRowDataLayer<>(
                        filterStrategy,
                        columnHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry);

        setChildLayer(GridRegion.FILTER_ROW, this.filterRowDataLayer, 0, 1);
    }

    public FilterRowDataLayer<T> getFilterRowDataLayer() {
        return this.filterRowDataLayer;
    }

    @Override
    public int getHeight() {
        if (this.filterRowVisible) {
            return super.getHeight();
        } else {
            return getHeightOffset(1);
        }
    }

    @Override
    public int getRowCount() {
        if (this.filterRowVisible) {
            return super.getRowCount();
        } else {
            return super.getRowCount() - 1;
        }
    }

    public boolean isFilterRowVisible() {
        return this.filterRowVisible;
    }

    public void setFilterRowVisible(boolean filterRowVisible) {
        this.filterRowVisible = filterRowVisible;
        fireLayerEvent(new RowStructuralRefreshEvent(this.filterRowDataLayer));
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ToggleFilterRowCommand) {
            setFilterRowVisible(!this.filterRowVisible);
            return true;
        }
        return super.doCommand(command);
    }
}
